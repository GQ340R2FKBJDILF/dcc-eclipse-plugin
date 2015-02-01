package ch.arktos.dcc;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * The Class DynamicClasspathContainer.
 * 
 * @author killert
 */
public class DynamicClasspathContainer implements IClasspathContainer {

	/** The Constant ID. */
	public final static String ID = "ch.arktos.dcc.DYNAMIC_CLASSPATH_CONTAINER";

	private final IPath currentPath;
	private final String projectName;
	private final String libDir;
	private final String libRegex;
	private final String srcRegex;
	private final String docRegex;
	private final Boolean isExported;

	/**
	 * This constructor uses the provided IPath and IJavaProject arguments to
	 * assign the instance variables that are used for determining the classpath
	 * entries included in this container. The provided IPath comes from the
	 * classpath entry element in project's .classpath file. It is a three
	 * segment path with the following segments: [0] - Unique container ID [1] -
	 * project relative directory that this container will collect files from
	 * [2] - comma separated list of extensions to include in this container
	 * (extensions do not include the preceding ".")
	 * 
	 * @param path
	 *            unique path for this container instance, including directory
	 *            and extensions a segments
	 * @param project
	 *            the Java project that is referencing this container
	 * @throws JavaModelException
	 */
	public DynamicClasspathContainer(IPath path, IJavaProject project) throws JavaModelException {
		this.currentPath = path;
		this.projectName = PathEncoder.decode(path.segment(1));
		this.libDir = PathEncoder.decode(path.segment(3));
		this.libRegex = PathEncoder.decode(path.segment(4));
		this.srcRegex = PathEncoder.decode(path.segment(5));
		this.docRegex = PathEncoder.decode(path.segment(6));
		this.isExported = Boolean.valueOf(PathEncoder.decode(path.segment(7)));
	}

	/**
	 * This method is used to determine if the directory specified in the
	 * container path is valid, i.e. it exists relative to the project and it is
	 * a directory.
	 * 
	 * @return true if the configured directory is valid
	 */
	public boolean isValid() {
		return currentPath.segmentCount() == 8;
	}

	/**
	 * Gets the class path entries.
	 * 
	 * @return the class path entries
	 */
	@Override
	public IClasspathEntry[] getClasspathEntries() {
		final ArrayList<IClasspathEntry> entryList = new ArrayList<IClasspathEntry>();

		// retrieve library files
		final IPath projectPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(projectName).append(libDir);
		final File projectFile = projectPath.toFile();
		final File[] libraries = projectFile.listFiles(new LibraryRegexFilter(libRegex));
		if (libraries != null) {
			for (File library : libraries) {

				// retrieve source attachment for library
				final Path src = getPathWithPart(library, srcRegex);
				final IPath srcPath = getRootPath(src);

				// retrieve javadoc attachment for library
				final Path javadocPath = getPathWithPart(library, docRegex);
				final IClasspathAttribute[] attribs = getJavadocAttibute(javadocPath);

				entryList.add(JavaCore.newLibraryEntry(new Path(library.getAbsolutePath()), src, srcPath, new IAccessRule[] {}, attribs, isExported));
			}
		}
		return entryList.toArray(new IClasspathEntry[entryList.size()]);
	}

	/**
	 * This method returns the Path for a library attachment built from the
	 * library file and the attachment specific file part.
	 * 
	 * @param file
	 *            library file in the container
	 * @param part
	 *            specific file part for this attachment
	 * @return path to the library attachment or null.
	 */
	private Path getPathWithPart(File file, String part) {
		if (part == null || part.equals("")) {
			return null;
		}

		final IPath projectPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(projectName);
		final String filePrefix = file.getName().substring(0, file.getName().lastIndexOf("."));
		final File libRoot = projectPath.append(libDir).toFile();

		final FilenameFilter regexFilter = new AttachmentRegexFilter(".*" + filePrefix + part);
		final ArrayList<File> dirs = new ArrayList<>();
		dirs.add(libRoot);
		dirs.addAll(Arrays.asList(libRoot.listFiles(new DirectoryFilter())));

		Path resultPath = null;
		for (File dir : dirs) {
			final File[] matches = dir.listFiles(regexFilter);
			if (matches != null && matches.length > 0) {
				resultPath = new Path(matches[0].getAbsolutePath());
			}
		}
		return resultPath;
	}

	/**
	 * Returns the root path if the input path exists
	 * 
	 * @param path
	 *            file path
	 * @return root path
	 */
	private Path getRootPath(Path path) {
		if (path == null) {
			return null;
		}
		return new Path("/");
	}

	/**
	 * Returns the javadoc file as a classpathentry attribute if the input path
	 * exists
	 * 
	 * @param path
	 * @return
	 */
	private IClasspathAttribute[] getJavadocAttibute(Path path) {
		if (path == null) {
			return null;
		}
		return new IClasspathAttribute[] { JavaCore.newClasspathAttribute("javadoc_location", path.toFile().toURI().toString()) };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.IClasspathContainer#getDescription()
	 */
	@Override
	public String getDescription() {
		final IPath projectPath = new Path(projectName);
		return projectPath.append(libDir).toPortableString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.IClasspathContainer#getKind()
	 */
	@Override
	public int getKind() {
		return IClasspathContainer.K_APPLICATION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.IClasspathContainer#getPath()
	 */
	@Override
	public IPath getPath() {
		return currentPath;
	}

}
