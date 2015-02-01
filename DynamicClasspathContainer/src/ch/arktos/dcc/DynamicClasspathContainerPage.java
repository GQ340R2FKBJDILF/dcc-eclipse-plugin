package ch.arktos.dcc;

import java.io.File;
import java.util.regex.Pattern;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * The Class DynamicClasspathContainerPage.
 * 
 * @author killert
 */
public class DynamicClasspathContainerPage extends WizardPage implements IClasspathContainerPage, IClasspathContainerPageExtension {

	private final static String PREFIX_BINARY_PATH = "org.eclipse.jdt.launching.CLASSPATH_ATTR_LIBRARY_PATH_ENTRY";

	private final static String DEFAULT_PROJECT = "";
	private final static String DEFAULT_BIN_DIR = "bin";
	private final static String DEFAULT_LIB_DIR = "lib";
	private final static String DEFAULT_LIB_REGEX = "(?!.*(-javadoc|-doc|-sources|-src).*).*\\.jar";
	private final static String DEFAULT_SRC_REGEX = ".*(-sources|-src)\\.(jar|zip)";
	private final static String DEFAULT_DOC_REGEX = ".*(-javadoc|-doc)\\.(jar|zip)";
	private final static boolean DEFAULT_IS_EXPORTED = true;

	private final IPath workspace;
	private IClasspathEntry currentEntry;
	private IJavaProject currentProject;
	private Label projectLabel;
	private Text binDirText;
	private Text libDirText;
	private Text libRegexText;
	private Text srcRegexText;
	private Text docRegexText;
	private Button exportedBox;

	/**
	 * Default Constructor - sets title, page name, description.
	 */
	public DynamicClasspathContainerPage() {
		super(Messages.PageName, Messages.PageTitle, null);
		this.workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation();

		setDescription(Messages.PageDesc);
		setPageComplete(true);
	}

	/**
	 * Initialize.
	 * 
	 * @param project
	 *            the project
	 * @param entries
	 *            the entries
	 */
	@Override
	public void initialize(IJavaProject project, IClasspathEntry[] entries) {
		this.currentProject = project;
	}

	/**
	 * Creates the control.
	 * 
	 * @param parent
	 *            the parent
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

		IPath projectPath = workspace.append(projectLabel.getText());

		if (!isProjectDir(projectPath)) {
			setErrorMessage(Messages.ProjectErr);
			return false;
		}
		if (!isDirValid(projectPath, binDirText.getText())) {
			setErrorMessage(Messages.BinDirErr);
			return false;
		}
		if (!isDirValid(projectPath, libDirText.getText())) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.ui.wizards.IClasspathContainerPage#getSelection()
	 */
	/**
	 * Gets the selection.
	 * 
	 * @return the selection
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

		IPath binPath = new Path(projectLabel.getText()).append(binDirText.getText());
		IClasspathAttribute attribute = JavaCore.newClasspathAttribute(PREFIX_BINARY_PATH, binPath.toString());

		return JavaCore.newContainerEntry(path, new IAccessRule[] {}, new IClasspathAttribute[] { attribute }, exportedBox.getSelection());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.ui.wizards.IClasspathContainerPage#setSelection(org.eclipse
	 * .jdt.core.IClasspathEntry)
	 */
	/**
	 * Sets the selection.
	 * 
	 * @param entry
	 *            the new selection
	 */
	@Override
	public void setSelection(IClasspathEntry entry) {
		this.currentEntry = entry;
	}

	/**
	 * Creates the directory label, combo, and browse button
	 * 
	 * @param parent
	 *            the parent widget
	 */
	private void createProjectGroup(final Composite parent) {
		String projectString = DEFAULT_PROJECT;
		if (currentProject != null) {
			projectString = currentProject.getElementName();
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

		Button button = new Button(composite, SWT.PUSH);
		button.setText(Messages.SelectButton);
		button.setLayoutData(new GridData(GridData.END));
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				DirectoryDialog dialog = new DirectoryDialog(parent.getShell(), SWT.OPEN);
				dialog.setMessage(Messages.SelectDialog);
				dialog.setFilterPath(workspace.append(projectLabel.getText()).toOSString());
				File file = new File(dialog.open());
				if (file != null && file.exists() && file.isDirectory()) {
					projectLabel.setText(file.getName());
				}
			}

		});
	}

	/**
	 * Creates the directory label, combo, and browse button
	 * 
	 * @param parent
	 *            the parent widget
	 */
	private void createBinDirGroup(final Composite parent) {
		String binDir = DEFAULT_BIN_DIR;
		if (currentEntry != null && currentEntry.getPath().segmentCount() > 2) {
			binDir = PathEncoder.decode(currentEntry.getPath().segment(2));
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
	 *            the parent widget
	 */
	private void createLibDirGroup(final Composite parent) {
		String libDir = DEFAULT_LIB_DIR;
		if (currentEntry != null && currentEntry.getPath().segmentCount() > 3) {
			libDir = PathEncoder.decode(currentEntry.getPath().segment(3));
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
	 *            parent widget
	 */
	private void createLibRegexGroup(final Composite parent) {
		String regex = DEFAULT_LIB_REGEX;
		if (currentEntry != null && currentEntry.getPath().segmentCount() > 4) {
			regex = PathEncoder.decode(currentEntry.getPath().segment(4));
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
	 *            parent widget
	 */
	private void createSrcRegexGroup(final Composite parent) {
		String regex = DEFAULT_SRC_REGEX;
		if (currentEntry != null && currentEntry.getPath().segmentCount() > 5) {
			regex = PathEncoder.decode(currentEntry.getPath().segment(5));
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
	 *            parent widget
	 */
	private void createDocRegexGroup(final Composite parent) {
		String regex = DEFAULT_DOC_REGEX;
		if (currentEntry != null && currentEntry.getPath().segmentCount() > 6) {
			regex = PathEncoder.decode(currentEntry.getPath().segment(6));
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
	 *            parent widget
	 */
	private void createExportedGroup(final Composite parent) {
		Boolean isexported = DEFAULT_IS_EXPORTED;
		if (currentEntry != null) {
			isexported = currentEntry.isExported();
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
	 *            regular expression string
	 * @return true if the regular expression is valid
	 */
	private static boolean isRegexValid(String regex) {
		try {
			Pattern.compile(regex);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * Checks that the chosen directory is valid. Must meet the following: -
	 * exists - is a directory
	 * 
	 * @param dir
	 *            chosen directory
	 * @return true if the directory is valid
	 */
	private boolean isDirValid(IPath root, String dir) {
		final File libPath = new File(root.toOSString(), dir);
		libPath.mkdirs();
		return libPath.isDirectory();
	}

	/**
	 * Checks that the chosen directory is valid. Must meet the following: -
	 * exists - is a directory
	 * 
	 * @param dir
	 *            chosen directory
	 * @return true if the directory is valid
	 */
	private boolean isProjectDir(IPath path) {
		File projectPath = new File(path.toOSString());
		if (!projectPath.exists() || !projectPath.isDirectory() || !workspace.toOSString().equals(projectPath.getParentFile().getAbsolutePath())) {
			return false;
		}

		return true;
	}
}
