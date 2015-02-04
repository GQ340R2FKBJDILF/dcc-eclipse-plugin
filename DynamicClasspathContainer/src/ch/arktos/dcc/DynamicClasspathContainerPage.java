
package ch.arktos.dcc;

import java.util.regex.Pattern;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;


/**
 * The Class DynamicClasspathContainerPage.
 * 
 * @author killert
 */
public class DynamicClasspathContainerPage extends WizardPage implements IClasspathContainerPage, IClasspathContainerPageExtension {

  private final static String  PREFIX_BINARY_PATH  = "org.eclipse.jdt.launching.CLASSPATH_ATTR_LIBRARY_PATH_ENTRY";

  private final static String  DEFAULT_PROJECT     = "";
  private final static String  DEFAULT_BIN_DIR     = "bin";
  private final static String  DEFAULT_LIB_DIR     = "lib";
  private final static String  DEFAULT_LIB_REGEX   = "(?!.*(-javadoc|-doc|-sources|-src).*).*\\.jar";
  private final static String  DEFAULT_SRC_REGEX   = ".*(-sources|-src)\\.(jar|zip)";
  private final static String  DEFAULT_DOC_REGEX   = ".*(-javadoc|-doc)\\.(jar|zip)";
  private final static boolean DEFAULT_IS_EXPORTED = true;

  private IClasspathEntry      classpathEntry;
  private IJavaProject         project;
  private Label                projectLabel;
  private Text                 binDirText;
  private Text                 libDirText;
  private Text                 libRegexText;
  private Text                 srcRegexText;
  private Text                 docRegexText;
  private Button               exportedBox;

  /**
   * Default Constructor - sets title, page name, description.
   */
  public DynamicClasspathContainerPage() {
    super(Messages.PageName, Messages.PageTitle, null);

    setDescription(Messages.PageDesc);
    setPageComplete(true);
  }

  /**
   * Initialize.
   * 
   * @param project
   *          the project
   * @param entries
   *          the entries
   */
  @Override
  public void initialize(IJavaProject project, IClasspathEntry[] entries) {
    this.project = project;
  }

  /**
   * Creates the control.
   * 
   * @param parent
   *          the parent
   */
  @Override
  public void createControl(Composite parent) {
    final Composite composite = new Composite(parent, SWT.NONE);
    composite.setFont(parent.getFont());
    composite.setLayout(new GridLayout(2, true));
    composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));

    createProjectGroup(composite);
    createBinDirGroup(composite);
    createLibDirGroup(composite);
    createLibRegexGroup(composite);
    createSrcRegexGroup(composite);
    createDocRegexGroup(composite);
    createExportedGroup(composite);

    setControl(composite);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jdt.ui.wizards.IClasspathContainerPage#finish()
   */
  /**
   * Finish.
   * 
   * @return true, if successful
   */
  @Override
  public boolean finish() {
    projectLabel.setText(projectLabel.getText().trim());
    binDirText.setText(binDirText.getText().trim());
    libDirText.setText(libDirText.getText().trim());
    libRegexText.setText(libRegexText.getText().trim());
    srcRegexText.setText(srcRegexText.getText().trim());
    docRegexText.setText(docRegexText.getText().trim());

    final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectLabel.getText());
    if (!project.exists()) {
      setErrorMessage(Messages.ProjectErr);
      return false;
    }
    if (!createProjectFolder(project, binDirText.getText())) {
      setErrorMessage(Messages.BinDirErr);
      return false;
    }
    if (!createProjectFolder(project, libDirText.getText())) {
      setErrorMessage(Messages.LibDirErr);
      return false;
    }
    if (!isRegexValid(libRegexText.getText())) {
      setErrorMessage(Messages.LibRegexErr);
      return false;
    }
    if (!isRegexValid(srcRegexText.getText())) {
      setErrorMessage(Messages.SrcRegexErr);
      return false;
    }
    if (!isRegexValid(docRegexText.getText())) {
      setErrorMessage(Messages.DocRegexErr);
      return false;
    }
    return true;
  }

  private boolean createProjectFolder(IProject project, String folderName) {
    final IFolder libFolder = project.getFolder(folderName);
    if (!libFolder.exists()) {
      try {
        libFolder.create(false, false, null);
      }
      catch (CoreException e) {
        return false;
      }
    }
    return libFolder.exists();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jdt.ui.wizards.IClasspathContainerPage#getSelection()
   */
  @Override
  public IClasspathEntry getSelection() {
    IPath path = new Path(DynamicClasspathContainer.ID); // 0
    path = path.append(PathEncoder.encode(projectLabel.getText())); // 1
    path = path.append(PathEncoder.encode(binDirText.getText())); // 2
    path = path.append(PathEncoder.encode(libDirText.getText())); // 3
    path = path.append(PathEncoder.encode(libRegexText.getText())); // 4
    path = path.append(PathEncoder.encode(srcRegexText.getText())); // 5
    path = path.append(PathEncoder.encode(docRegexText.getText())); // 6
    path = path.append(PathEncoder.encode(Boolean.toString(exportedBox.getSelection()))); // 7

    final IPath binPath = new Path(projectLabel.getText()).append(binDirText.getText());
    final IClasspathAttribute attribute = JavaCore.newClasspathAttribute(PREFIX_BINARY_PATH, binPath.toString());
    return JavaCore.newContainerEntry(path, new IAccessRule[] {}, new IClasspathAttribute[] {attribute}, exportedBox.getSelection());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.jdt.ui.wizards.IClasspathContainerPage#setSelection(org.eclipse
   * .jdt.core.IClasspathEntry)
   */
  @Override
  public void setSelection(IClasspathEntry classpathEntry) {
    this.classpathEntry = classpathEntry;
  }

  /**
   * Creates the directory label, combo, and browse button
   * 
   * @param parent
   *          the parent widget
   */
  private void createProjectGroup(final Composite parent) {
    String projectString = DEFAULT_PROJECT;
    if (project != null) {
      projectString = project.getElementName();
    }

    Label label = new Label(parent, SWT.NONE);
    label.setText(Messages.ProjectLabel);
    label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    final Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayout(new GridLayout(2, false));
    composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));

    this.projectLabel = new Label(composite, SWT.NONE);
    projectLabel.setText(projectString);
    projectLabel.setLayoutData(new GridData(GridData.BEGINNING | GridData.FILL_HORIZONTAL));

    if (project == null) {
      Button button = new Button(composite, SWT.PUSH);
      button.setText(Messages.SelectButton);
      button.setLayoutData(new GridData(GridData.END));
      button.addSelectionListener(new SelectionAdapter() {

        @Override
        public void widgetSelected(SelectionEvent event) {
          final IJavaProject javaProject = getJavaProject();
          if (javaProject != null) {
            projectLabel.setText(javaProject.getElementName());
          }
        }

      });
    }
  }

  private IJavaProject getJavaProject() {
    IJavaProject[] projects;
    try {
      projects = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProjects();
    }
    catch (JavaModelException e) {
      projects = new IJavaProject[0];
    }

    if (projects.length > 0) {
      final ILabelProvider labelProvider = new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
      final ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
      dialog.setTitle(Messages.ProjectSelectionTitle);
      dialog.setMessage(Messages.ProjectSelectionMessage);
      dialog.setElements(projects);
      dialog.setInitialSelections(new Object[] {projects[0]});

      if (dialog.open() == Window.OK) {
        final Object element = dialog.getFirstResult();
        if (element instanceof IJavaProject) {
          return (IJavaProject)element;
        }
      }
    }
    return null;
  }

  /**
   * Creates the directory label, combo, and browse button
   * 
   * @param parent
   *          the parent widget
   */
  private void createBinDirGroup(final Composite parent) {
    String binDir = DEFAULT_BIN_DIR;
    if (classpathEntry != null && classpathEntry.getPath().segmentCount() > 2) {
      binDir = PathEncoder.decode(classpathEntry.getPath().segment(2));
    }

    Label label = new Label(parent, SWT.NONE);
    label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    label.setText(Messages.BinDirLabel);

    this.binDirText = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.FILL);
    binDirText.setText(binDir);
    binDirText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
  }

  /**
   * Creates the directory label, combo, and browse button
   * 
   * @param parent
   *          the parent widget
   */
  private void createLibDirGroup(final Composite parent) {
    String libDir = DEFAULT_LIB_DIR;
    if (classpathEntry != null && classpathEntry.getPath().segmentCount() > 3) {
      libDir = PathEncoder.decode(classpathEntry.getPath().segment(3));
    }

    Label label = new Label(parent, SWT.NONE);
    label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    label.setText(Messages.LibDirLabel);

    this.libDirText = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.FILL);
    libDirText.setText(libDir);
    libDirText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
  }

  /**
   * Creates the regular expression label and text box
   * 
   * @param parent
   *          parent widget
   */
  private void createLibRegexGroup(final Composite parent) {
    String regex = DEFAULT_LIB_REGEX;
    if (classpathEntry != null && classpathEntry.getPath().segmentCount() > 4) {
      regex = PathEncoder.decode(classpathEntry.getPath().segment(4));
    }

    Label label = new Label(parent, SWT.NONE);
    label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    label.setText(Messages.LibRegexLabel);

    this.libRegexText = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.FILL);
    libRegexText.setText(regex);
    libRegexText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
  }

  /**
   * Creates the regular expression label and text box
   * 
   * @param parent
   *          parent widget
   */
  private void createSrcRegexGroup(final Composite parent) {
    String regex = DEFAULT_SRC_REGEX;
    if (classpathEntry != null && classpathEntry.getPath().segmentCount() > 5) {
      regex = PathEncoder.decode(classpathEntry.getPath().segment(5));
    }

    Label label = new Label(parent, SWT.NONE);
    label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    label.setText(Messages.SrcRegexLabel);

    this.srcRegexText = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.FILL);
    srcRegexText.setText(regex);
    srcRegexText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
  }

  /**
   * Creates the regular expression label and text box
   * 
   * @param parent
   *          parent widget
   */
  private void createDocRegexGroup(final Composite parent) {
    String regex = DEFAULT_DOC_REGEX;
    if (classpathEntry != null && classpathEntry.getPath().segmentCount() > 6) {
      regex = PathEncoder.decode(classpathEntry.getPath().segment(6));
    }

    Label label = new Label(parent, SWT.NONE);
    label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    label.setText(Messages.DocRegexLabel);

    this.docRegexText = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.FILL);
    docRegexText.setText(regex);
    docRegexText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
  }

  /**
   * Creates the regular expression label and text box
   * 
   * @param parent
   *          parent widget
   */
  private void createExportedGroup(final Composite parent) {
    Boolean isexported = DEFAULT_IS_EXPORTED;
    if (classpathEntry != null) {
      isexported = classpathEntry.isExported();
    }

    Label label = new Label(parent, SWT.NONE);
    label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

    this.exportedBox = new Button(parent, SWT.CHECK);
    exportedBox.setText(Messages.IsExportedLabel);
    exportedBox.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    exportedBox.setSelection(isexported);
  }

  /**
   * Checks that the regular expression is valid.
   * 
   * @param regex
   *          regular expression string
   * @return true if the regular expression is valid
   */
  private static boolean isRegexValid(String regex) {
    try {
      Pattern.compile(regex);
    }
    catch (Exception e) {
      return false;
    }
    return true;
  }

}
