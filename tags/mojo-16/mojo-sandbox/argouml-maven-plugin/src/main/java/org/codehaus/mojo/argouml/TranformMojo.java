package org.codehaus.mojo.argouml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.argouml.application.helpers.ResourceLoaderWrapper;
import org.argouml.kernel.Project;
import org.argouml.model.Model;
import org.argouml.model.ModelImplementation;
import org.argouml.notation.Notation;
import org.argouml.notation.NotationName;
import org.argouml.notation.NotationProviderFactory2;
import org.argouml.persistence.AbstractFilePersister;
import org.argouml.persistence.OpenException;
import org.argouml.persistence.PersistenceManager;
import org.argouml.uml.notation.java.AssociationEndNameNotationJava;
import org.argouml.uml.notation.java.AssociationNameNotationJava;
import org.argouml.uml.notation.java.AttributeNotationJava;
import org.argouml.uml.notation.java.ModelElementNameNotationJava;
import org.argouml.uml.notation.java.OperationNotationJava;
import org.argouml.uml.notation.uml.ActionStateNotationUml;
import org.argouml.uml.notation.uml.AssociationEndNameNotationUml;
import org.argouml.uml.notation.uml.AssociationNameNotationUml;
import org.argouml.uml.notation.uml.AssociationRoleNotationUml;
import org.argouml.uml.notation.uml.AttributeNotationUml;
import org.argouml.uml.notation.uml.CallStateNotationUml;
import org.argouml.uml.notation.uml.ClassifierRoleNotationUml;
import org.argouml.uml.notation.uml.ComponentInstanceNotationUml;
import org.argouml.uml.notation.uml.ExtensionPointNotationUml;
import org.argouml.uml.notation.uml.MessageNotationUml;
import org.argouml.uml.notation.uml.ModelElementNameNotationUml;
import org.argouml.uml.notation.uml.NodeInstanceNotationUml;
import org.argouml.uml.notation.uml.NotationUtilityUml;
import org.argouml.uml.notation.uml.ObjectFlowStateStateNotationUml;
import org.argouml.uml.notation.uml.ObjectFlowStateTypeNotationUml;
import org.argouml.uml.notation.uml.ObjectNotationUml;
import org.argouml.uml.notation.uml.OperationNotationUml;
import org.argouml.uml.notation.uml.StateBodyNotationUml;
import org.argouml.uml.notation.uml.TransitionNotationUml;
import org.argouml.uml.ui.SaveGraphicsManager;
import org.tigris.gef.base.CmdSaveGraphics;
import org.tigris.gef.base.Diagram;
import org.tigris.gef.base.Editor;
import org.tigris.gef.base.Globals;

/**
 * Transforms ArgoUml files into images.
 *
 * @goal transform
 */
public class TranformMojo extends AbstractMojo {
  /**
   * Paths to input files. If any of specified files is a directory then all ArgoUml files in it are used. If this
   * parameter is not specified then <code>${project.build.sourceDirectory}/argouml</code> is used. Supported ArgoUml
   * formats: .zargo, .xmi, .uml, .zip
   *
   * @parameter
   */
  private File[] inputFiles;

  /**
   * Default input directory if no input files were specified.
   *
   * @parameter expression="${project.basedir}/src/argouml"
   * @readonly
   */
  private File defaultInputDirectory;

  /**
   * If <code>inputFiles</code> contains folders and this parameter is set to <code>true</code> then those folders are
   * parsed recursively.
   *
   * @parameter default-value="false"
   */
  private boolean recursive;

  /**
   * Output directory for created images.
   *
   * @parameter default-value="${project.build.directory}/argouml"
   */
  private File outputDirectory;

  /**
   * Output image format. Supported values are: ps, eps, png, gif, svg.
   *
   * @parameter default-value="png"
   */
  private String outputFormat;

  /**
   * Image scaling factor.
   *
   * @parameter default-value="1"
   */
  private int scale;

  public void execute() throws MojoExecutionException, MojoFailureException {
    //Prepare input files.
    if (this.inputFiles == null || this.inputFiles.length == 0) {
      this.inputFiles = new File[]{ this.defaultInputDirectory };
    }

    //Prepare output directory
    final File output = this.outputDirectory;
    if (!output.exists() && !output.mkdirs()) {
      throw new MojoExecutionException("could not create output directory " + output.getAbsolutePath());
    }

    //Init argouml stuff.
    //new InitNotationUml().init();
    //new InitNotationJava().init();
    initNotationUml();
    initNotationJava();
    try {
      Model.setImplementation(
          (ModelImplementation) Class.forName("org.argouml.model.mdr.MDRModelImplementation").newInstance()
      );
    } catch (ClassNotFoundException e) {
      throw new MojoExecutionException("Could not init argouml", e);
    } catch (IllegalAccessException e) {
      throw new MojoExecutionException("Could not init argouml", e);
    } catch (InstantiationException e) {
      throw new MojoExecutionException("Could not init argouml", e);
    }

    //Process files
    for (int i = 0; i < this.inputFiles.length; i++) {
      this.process(this.inputFiles[i]);
    }
  }

  private void process(final File fileOrDirectory) throws MojoExecutionException {
    if (!fileOrDirectory.exists()) {
      throw new MojoExecutionException("Input " + fileOrDirectory.getAbsolutePath() + " does not exist");
    } else if (fileOrDirectory.isDirectory()) {
      final File[] files = fileOrDirectory.listFiles();
      for (int i = 0; i < files.length; i++) {
        if (this.recursive) {
          this.process(files[i]);
        } else {
          this.convert(files[i]);
        }
      }
    } else if (fileOrDirectory.isFile()) {
      if (!this.convert(fileOrDirectory)) {
        this.getLog().warn("Could not convert " + fileOrDirectory.getAbsolutePath());
      }
    } else {
      //Oops.
      throw new MojoExecutionException(fileOrDirectory.getAbsolutePath() + " is not file and not a directory.");
    }
  }

  /**
   * Perform conversion of single file.
   *
   * @param input input file.
   * @return true if conversion completed successfully, false otherwise.
   */
  private boolean convert(final File input) throws MojoExecutionException {
    final AbstractFilePersister persister =
        PersistenceManager.getInstance().getPersisterFromFileName(input.getAbsolutePath());
    if (persister == null) {
      return false;
    }
    final Project project;
    try {
      project = persister.doLoad(input);
    } catch (OpenException e) {
      throw new MojoExecutionException("Could not load file " + input.getAbsolutePath(), e);
    } catch (InterruptedException e) {
      throw new MojoExecutionException("Could not load file " + input.getAbsolutePath(), e);
    }
    final Iterator it = project.getDiagrams().iterator();
    while (it.hasNext()) {
      Diagram dia = (Diagram) it.next();
      Globals.curEditor(new Editor(dia));
      CmdSaveGraphics cmd = SaveGraphicsManager.getInstance().getSaveCommandBySuffix(this.outputFormat);
      cmd.setScale(this.scale);
      final File file = new File(this.outputDirectory, dia.getName() + "." + this.outputFormat);
      this.getLog().info("Writing " + file.getAbsolutePath());
      OutputStream os = null;
      try {
        os = new FileOutputStream(file);
        cmd.setStream(os);
        cmd.actionPerformed(null);
      } catch (FileNotFoundException e) {
        throw new MojoExecutionException("Could not write " + file, e);
      } finally {
        if (os != null) {
          try {
            os.close();
          } catch (IOException e) {
            //no-op
          }
        }
      }
    }
    return true;
  }

  /**
   * TODO: remove this method when {@link org.argouml.notation.InitNotationUml} becomes public.
   */
  private static void initNotationUml() {
    NotationProviderFactory2 npf = NotationProviderFactory2.getInstance();
    NotationName name =
        Notation.makeNotation(
            "UML",
            "1.4",
            ResourceLoaderWrapper.lookupIconResource("UmlNotation"));

    npf.addNotationProvider(
        NotationProviderFactory2.TYPE_NAME,
        name, ModelElementNameNotationUml.class);
    npf.addNotationProvider(
        NotationProviderFactory2.TYPE_TRANSITION,
        name, TransitionNotationUml.class);
    npf.addNotationProvider(
        NotationProviderFactory2.TYPE_STATEBODY,
        name, StateBodyNotationUml.class);
    npf.addNotationProvider(
        NotationProviderFactory2.TYPE_ACTIONSTATE,
        name, ActionStateNotationUml.class);
    npf.addNotationProvider(
        NotationProviderFactory2.TYPE_OBJECT,
        name, ObjectNotationUml.class);
    npf.addNotationProvider(
        NotationProviderFactory2.TYPE_COMPONENTINSTANCE,
        name, ComponentInstanceNotationUml.class);
    npf.addNotationProvider(
        NotationProviderFactory2.TYPE_NODEINSTANCE,
        name, NodeInstanceNotationUml.class);
    npf.addNotationProvider(
        NotationProviderFactory2.TYPE_OBJECTFLOWSTATE_TYPE,
        name, ObjectFlowStateTypeNotationUml.class);
    npf.addNotationProvider(
        NotationProviderFactory2.TYPE_OBJECTFLOWSTATE_STATE,
        name, ObjectFlowStateStateNotationUml.class);
    npf.addNotationProvider(
        NotationProviderFactory2.TYPE_CALLSTATE,
        name, CallStateNotationUml.class);
    npf.addNotationProvider(
        NotationProviderFactory2.TYPE_CLASSIFIERROLE,
        name, ClassifierRoleNotationUml.class);
    npf.addNotationProvider(
        NotationProviderFactory2.TYPE_MESSAGE,
        name, MessageNotationUml.class);
    npf.addNotationProvider(
        NotationProviderFactory2.TYPE_ATTRIBUTE,
        name, AttributeNotationUml.class);
    npf.addNotationProvider(
        NotationProviderFactory2.TYPE_OPERATION,
        name, OperationNotationUml.class);
    npf.addNotationProvider(
        NotationProviderFactory2.TYPE_EXTENSION_POINT,
        name, ExtensionPointNotationUml.class);
    npf.addNotationProvider(
        NotationProviderFactory2.TYPE_ASSOCIATION_END_NAME,
        name, AssociationEndNameNotationUml.class);
    npf.addNotationProvider(
        NotationProviderFactory2.TYPE_ASSOCIATION_ROLE,
        name, AssociationRoleNotationUml.class);
    npf.addNotationProvider(
        NotationProviderFactory2.TYPE_ASSOCIATION_NAME,
        name, AssociationNameNotationUml.class);
    NotationProviderFactory2.getInstance().setDefaultNotation(name);
    /* Initialise the NotationUtilityUml: */
    (new NotationUtilityUml()).init();
  }

  /**
   * TODO: remove this method when {@link org.argouml.notation.InitNotationJava} becomes public.
   */
  private static void initNotationJava() {
    NotationProviderFactory2 npf = NotationProviderFactory2.getInstance();
    NotationName name = /*Notation.findNotation("Java");*/
        Notation.makeNotation(
            "Java",
            null,
            ResourceLoaderWrapper.lookupIconResource("JavaNotation"));

    npf.addNotationProvider(
        NotationProviderFactory2.TYPE_NAME,
        name, ModelElementNameNotationJava.class);
    npf.addNotationProvider(
        NotationProviderFactory2.TYPE_ATTRIBUTE,
        name, AttributeNotationJava.class);
    npf.addNotationProvider(
        NotationProviderFactory2.TYPE_OPERATION,
        name, OperationNotationJava.class);
    npf.addNotationProvider(
        NotationProviderFactory2.TYPE_ASSOCIATION_END_NAME,
        name, AssociationEndNameNotationJava.class);
    npf.addNotationProvider(
        NotationProviderFactory2.TYPE_ASSOCIATION_NAME,
        name, AssociationNameNotationJava.class);
  }
}
