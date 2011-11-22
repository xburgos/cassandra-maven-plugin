/*
 * Copyright (C) 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.mojo.groovy.extractor;

import java.net.URL;
import java.io.IOException;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import groovyjarjarantlr.collections.AST;
import groovyjarjarantlr.TokenStreamException;
import groovyjarjarantlr.RecognitionException;

import org.codehaus.groovy.antlr.parser.GroovyTokenTypes;
import org.codehaus.groovy.antlr.parser.GroovyLexer;
import org.codehaus.groovy.antlr.parser.GroovyRecognizer;
import org.codehaus.groovy.antlr.SourceBuffer;
import org.codehaus.groovy.antlr.ASTRuntimeException;
import org.codehaus.groovy.antlr.UnicodeEscapingReader;
import org.codehaus.groovy.antlr.AntlrASTProcessor;
import org.codehaus.groovy.antlr.LineColumn;
import org.codehaus.groovy.antlr.treewalker.Visitor;
import org.codehaus.groovy.antlr.treewalker.PreOrderTraversal;
import org.codehaus.groovy.antlr.java.JavaLexer;
import org.codehaus.groovy.antlr.java.JavaRecognizer;
import org.codehaus.groovy.antlr.java.Java2GroovyConverter;

/**
 * Support for CST Groovy parsers.
 *
 * @version $Rev$ $Date$
 */
public abstract class GroovyParserSupport
    implements GroovyTokenTypes
{
    public static final int SOURCE_TYPE_GROOVY = 1;

    public static final int SOURCE_TYPE_JAVA = 2;

    /**
     * Provides access to the source code to extract Javadocs.
     */
    protected SourceBuffer sourceBuffer;

    /**
     * Map of token names for producing meaningful error messages.
     */
    protected String[] tokenNames;

    //
    // Parsing Helpers
    //

    protected String getTokenName(final int token) {
        return tokenNames[token];
    }

    protected String getTokenName(final AST node) {
        if (node == null) {
            return "null";
        }
        return getTokenName(node.getType());
    }

    protected void assertNodeType(final int type, final AST node) {
        if (node == null) {
            throw new ASTRuntimeException(node, "No child node available in AST when expecting type: " + getTokenName(type));
        }
        if (node.getType() != type) {
            throw new ASTRuntimeException(node, "Unexpected node type: " + getTokenName(node) + " found when expecting type: " + getTokenName(type));
        }
    }

    protected boolean isType(int typeCode, AST node) {
        return node != null && node.getType() == typeCode;
    }

    protected String qualifiedName(final AST qualifiedNameNode) {
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

    protected String identifier(final AST node) {
        assert node != null;
        assertNodeType(IDENT, node);

        return node.getText();
    }

    /** Pattern to capture javadocs. */
    private static final Pattern JAVADOCS_PATTERN = Pattern.compile("(?s).*/\\*\\*(.*?)\\*/[^\\*/}]*$");

    /** The last node which were looked for Javadocs for. */
    private AST lastJavadocNode;

    protected String javadocs(final AST node) {
        assert node != null;

        String javadocs = null;

        // Figure out where we should start looking
        LineColumn startAt;
        if (lastJavadocNode != null) {
            startAt = new LineColumn(lastJavadocNode.getLine(), lastJavadocNode.getColumn());
        }
        else {
            startAt = new LineColumn(1,1);
        }
        LineColumn stopAt = new LineColumn(node.getLine(), node.getColumn());

        // Remember where we last looked
        lastJavadocNode = node;

        String text = sourceBuffer.getSnippet(startAt, stopAt);
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

    protected int doParse(final URL source) throws IOException, TokenStreamException, RecognitionException {
        assert source != null;

        int sourceType;
        String fileName = source.getPath().toLowerCase();

        //
        // NOTE: Allow .java.txt to parse for tests
        //
        if (fileName.endsWith(".java") || fileName.endsWith(".java.txt")) {
            sourceType = SOURCE_TYPE_JAVA;
        }
        else {
            sourceType = SOURCE_TYPE_GROOVY;
        }

        doParse(source, sourceType);

        return sourceType;
    }

    protected void doParse(final URL source, final int sourceType) throws IOException, TokenStreamException, RecognitionException {
        assert source != null;

        Reader reader = new BufferedReader(new InputStreamReader(source.openStream()));
        try {
            doParse(reader, sourceType);
        }
        finally {
            reader.close();
        }
    }

    protected JavaRecognizer createJavaParser(final UnicodeEscapingReader reader) {
        assert reader != null;

        JavaLexer lexer = new JavaLexer(reader);
        reader.setLexer(lexer);

        JavaRecognizer parser = JavaRecognizer.make(lexer);
        parser.setSourceBuffer(sourceBuffer);
        tokenNames = parser.getTokenNames();

        return parser;
    }

    protected GroovyRecognizer createGroovyParser(final UnicodeEscapingReader reader) {
        assert reader != null;

        GroovyLexer lexer = new GroovyLexer(reader);
        reader.setLexer(lexer);

        GroovyRecognizer parser = GroovyRecognizer.make(lexer);
        parser.setSourceBuffer(sourceBuffer);
        tokenNames = parser.getTokenNames();

        return parser;
    }

    protected void doParse(final Reader reader, final int sourceType) throws TokenStreamException, RecognitionException {
        assert reader != null;

        resetState();

        AST node;
        UnicodeEscapingReader unicodeReader = new UnicodeEscapingReader(reader, sourceBuffer);

        switch (sourceType) {
            case SOURCE_TYPE_JAVA: {
                JavaRecognizer parser = createJavaParser(unicodeReader);
                parser.compilationUnit();
                node = parser.getAST();

                // Convert the Java AST into Groovy AST
                Visitor converter = new Java2GroovyConverter(tokenNames);
                AntlrASTProcessor processor = new PreOrderTraversal(converter);
                processor.process(node);
                break;
            }

            case SOURCE_TYPE_GROOVY: {
                GroovyRecognizer parser = createGroovyParser(unicodeReader);
                parser.compilationUnit();
                node = parser.getAST();
                break;
            }

            default:
                throw new IllegalArgumentException("Invalid source type: " + sourceType);
        }

        process(node, sourceType);
    }

    protected void resetState() {
        sourceBuffer = new SourceBuffer();
        tokenNames = null;
        lastJavadocNode = null;
    }
    
    protected abstract void process(AST node, final int sourceType);
}
