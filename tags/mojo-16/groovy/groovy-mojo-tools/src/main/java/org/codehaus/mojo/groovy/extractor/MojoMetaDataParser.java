/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.codehaus.mojo.groovy.extractor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import groovyjarjarantlr.collections.AST;
import groovyjarjarantlr.TokenStreamException;
import groovyjarjarantlr.RecognitionException;

import org.codehaus.groovy.antlr.ASTRuntimeException;
import org.codehaus.groovy.antlr.AntlrASTProcessor;
import org.codehaus.groovy.antlr.LineColumn;
import org.codehaus.groovy.antlr.SourceBuffer;
import org.codehaus.groovy.antlr.UnicodeEscapingReader;
import org.codehaus.groovy.antlr.java.Java2GroovyConverter;
import org.codehaus.groovy.antlr.java.JavaLexer;
import org.codehaus.groovy.antlr.java.JavaRecognizer;
import org.codehaus.groovy.antlr.parser.GroovyLexer;
import org.codehaus.groovy.antlr.parser.GroovyRecognizer;
import org.codehaus.groovy.antlr.parser.GroovyTokenTypes;
import org.codehaus.groovy.antlr.treewalker.PreOrderTraversal;
import org.codehaus.groovy.antlr.treewalker.Visitor;

/**
 * Parses Mojo meta-data (minimal class/param detail + javadocs) from Groovy and Java sources.
 *
 * @version $Rev$ $Date$
 */
public class MojoMetaDataParser
    implements GroovyTokenTypes
{
    /** Pattern to capture javadocs. */
    private static final Pattern JAVADOCS_PATTERN = Pattern.compile("(?s).*/\\*\\*(.*?)\\*/[^\\*/}]*$");

    /**
     * Container for collected meta-data.
     */
    private MojoMetaData metaData;

    /**
     * Provides access to the source code to extract Javadocs.
     */
    private SourceBuffer sourceBuffer;

    /**
     * Map of token names for producing meaningful error messages.
     */
    private String[] tokenNames;

    /** The last node which were looked for Javadocs for. */
    private AST lastNode;
    
    //
    // Parsing Helpers
    //

    private String getTokenName(final int token) {
        return tokenNames[token];
    }

    private String getTokenName(final AST node) {
        if (node == null) {
            return "null";
        }
        return getTokenName(node.getType());
    }

    private void assertNodeType(final int type, final AST node) {
        if (node == null) {
            throw new ASTRuntimeException(node, "No child node available in AST when expecting type: " + getTokenName(type));
        }
        if (node.getType() != type) {
            throw new ASTRuntimeException(node, "Unexpected node type: " + getTokenName(node) + " found when expecting type: " + getTokenName(type));
        }
    }

    private boolean isType(int typeCode, AST node) {
        return node != null && node.getType() == typeCode;
    }

    private String qualifiedName(final AST qualifiedNameNode) {
        assert qualifiedNameNode != null;

        if (isType(IDENT, qualifiedNameNode)) {
            return qualifiedNameNode.getText();
        }

        if (isType(DOT, qualifiedNameNode)) {
            AST node = qualifiedNameNode.getFirstChild();
            StringBuffer buffer = new StringBuffer();
            boolean first = true;

            for (; node != null; node = node.getNextSibling()) {
                if (first) {
                    first = false;
                }
                else {
                    buffer.append(".");
                }
                buffer.append(qualifiedName(node));
            }
            return buffer.toString();
        }
        else {
            return qualifiedNameNode.getText();
        }
    }

    private String identifier(final AST node) {
        assert node != null;
        assertNodeType(IDENT, node);

        return node.getText();
    }

    private String javadocs(final AST node) {
        assert node != null;

        String javadocs = null;

        // Figure out where we should start looking
        LineColumn startAt;
        if (lastNode != null) {
            startAt = new LineColumn(lastNode.getLine(), lastNode.getColumn());
        }
        else {
            startAt = new LineColumn(1,1);
        }
        LineColumn stopAt = new LineColumn(node.getLine(), node.getColumn());

        // Remember where we last looked
        lastNode = node;

        String text = sourceBuffer.getSnippet(startAt, stopAt);

        /*
        System.err.println("----8<----");
        System.err.println(text);
        System.err.println("---->8----");
        */
        
        Matcher m = JAVADOCS_PATTERN.matcher(text);
        if (m.matches()) {
            int lastGroupIndex = m.groupCount();
            if (lastGroupIndex > 0) {
                javadocs = m.group(lastGroupIndex);
            }
        }

        return javadocs;
    }

    //
    // Parsing
    //

    public MojoMetaData parse(final URL source) throws IOException, TokenStreamException, RecognitionException {
        assert source != null;

        int sourceType;
        String fileName = source.getPath().toLowerCase();

        //
        // NOTE: Allow .java.txt to parse for tests
        //
        if (fileName.endsWith(".java") || fileName.endsWith(".java.txt")) {
            sourceType = MojoMetaData.SOURCE_TYPE_JAVA;
        }
        else {
            sourceType = MojoMetaData.SOURCE_TYPE_GROOVY;
        }

        return parse(source, sourceType);
    }

    public MojoMetaData parse(final URL source, final int sourceType) throws IOException, TokenStreamException, RecognitionException {
        assert source != null;

        Reader reader = new BufferedReader(new InputStreamReader(source.openStream()));
        try {
            return parse(reader, sourceType);
        }
        finally {
            reader.close();
        }
    }

    private void resetState() {
        metaData = new MojoMetaData();
        sourceBuffer = new SourceBuffer();
        tokenNames = null;
        lastNode = null;
    }
    
    public MojoMetaData parse(final Reader reader, final int sourceType) throws TokenStreamException, RecognitionException {
        assert reader != null;

        resetState();

        AST node;
        UnicodeEscapingReader unicodeReader = new UnicodeEscapingReader(reader, sourceBuffer);

        switch (sourceType) {
            case MojoMetaData.SOURCE_TYPE_JAVA: {
                JavaLexer lexer = new JavaLexer(unicodeReader);
                unicodeReader.setLexer(lexer);

                JavaRecognizer parser = JavaRecognizer.make(lexer);
                parser.setSourceBuffer(sourceBuffer);
                tokenNames = parser.getTokenNames();

                parser.compilationUnit();
                node = parser.getAST();

                // Convert the Java AST into Groovy AST
                Visitor converter = new Java2GroovyConverter(tokenNames);
                AntlrASTProcessor processor = new PreOrderTraversal(converter);
                processor.process(node);
                break;
            }

            case MojoMetaData.SOURCE_TYPE_GROOVY: {
                GroovyLexer lexer = new GroovyLexer(unicodeReader);
                unicodeReader.setLexer(lexer);

                GroovyRecognizer parser = GroovyRecognizer.make(lexer);
                parser.setSourceBuffer(sourceBuffer);
                tokenNames = parser.getTokenNames();

                parser.compilationUnit();
                node = parser.getAST();
                break;
            }

            default:
                throw new IllegalArgumentException("Invalid source type: " + sourceType);
        }

        metaData.setSourceType(sourceType);

        process(node);

        return metaData;
    }

    private void process(AST node) {
        assert node != null;

        while (node != null) {
            int type = node.getType();

            switch (type) {
                case PACKAGE_DEF:
                    packageDef(node);
                    break;

                case IMPORT:
                    importDef(node);
                    break;

                case CLASS_DEF:
                    classDef(node);
                    break;

                default:
                    // Ignore anything else
                    break;
            }

            node = node.getNextSibling();
        }
    }

    private void packageDef(final AST packageDef) {
        assert packageDef != null;

        AST node = packageDef.getFirstChild();

        // Skip annotations
        if (isType(ANNOTATIONS, node)) {
            node = node.getNextSibling();
        }

        metaData.setPackageName(qualifiedName(node));
    }

    private void importDef(final AST importNode) {
        assert importNode != null;

        AST node = importNode.getFirstChild();

        if (isType(LITERAL_as, node)) {
            throw new RuntimeException("The import 'as' clause is not supported");
        }

        if (node.getNumberOfChildren() == 0) {
            // import is like  "import Foo"
            String name = identifier(node);
            metaData.addImport(name);
        }
        else {
            AST packageNode = node.getFirstChild();
            String packageName = qualifiedName(packageNode);
            
            AST nameNode = packageNode.getNextSibling();
            if (isType(STAR, nameNode)) {
                metaData.addImport(packageName + ".*");
            }
            else {
                // import is like "import foo.Bar"
                metaData.addImport(packageName + "." + identifier(nameNode));
            }
        }
    }

    private void classDef(final AST classDef) {
        assert classDef != null;

        AST node = classDef.getFirstChild();

        // Skip modifiers, we don't care about them really
        if (isType(MODIFIERS, node)) {
            node = node.getNextSibling();
        }

        // Get the class name
        String name = identifier(node);
        node = node.getNextSibling();
        String javadocs = javadocs(classDef);

        // Get the optional super class
        String superClass = null;
        if (isType(EXTENDS_CLAUSE, node)) {
            if (node.getNumberOfChildren() != 0) {
                superClass = qualifiedName(node.getFirstChild());
            }
            node = node.getNextSibling();
        }

        // Skip implements
        if (isType(IMPLEMENTS_CLAUSE, node)) {
            node = node.getNextSibling();
        }

        metaData.addClass(name, superClass, javadocs);

        objectBlock(node);
    }

    private void objectBlock(final AST objectBlock) {
        assert objectBlock != null;

        assertNodeType(OBJBLOCK, objectBlock);

        for (AST node = objectBlock.getFirstChild(); node != null; node = node.getNextSibling()) {
            int type = node.getType();
            switch (type) {
                case VARIABLE_DEF:
                    fieldDef(node);
                    break;

                default:
                    // ignore everything else
                    break;
            }
        }
    }
    private void fieldDef(final AST fieldDef) {
        assert fieldDef != null;

        AST node = fieldDef.getFirstChild();

        // Skip modifiers, we don't care about them for fields
        if (isType(MODIFIERS, node)) {
            node = node.getNextSibling();
        }

        // Get the field type
        String type = null;
        if (isType(TYPE, node)) {
            if (node.getNumberOfChildren() != 0) {
                AST typeNode = node.getFirstChild();
                if (isType(ARRAY_DECLARATOR, typeNode)) {
                    type = qualifiedName(typeNode.getFirstChild()) + "[]";
                }
                else {
                    type = qualifiedName(typeNode);
                }
            }
            node = node.getNextSibling();
        }

        String name = identifier(node);
        String javadocs = javadocs(fieldDef);

        metaData.addParameter(name, type, javadocs);
    }
}
