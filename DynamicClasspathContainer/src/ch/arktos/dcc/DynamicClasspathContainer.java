
package ch.arktos.dcc;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
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

  private final IPath        path;

  private final String       projectName;
  private final String       libDir;
  private final String       libRegex;
  private final String       srcRegex;
  private final String       docRegex;
  private final Boolean      isExported;

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
   *          unique path for this container instance, including directory
   *          and extensions a segments
   * @param project
   *          the Java project that is referencing this container
   * @throws JavaModelException
   */
  public DynamicClasspathContainer(IPath path, IJavaProject project) throws JavaModelException {
    this.path = path;

    this.projectName = PathEncoder.decode(path.segment(1));
    this.libDir = PathEncoder.decode(path.segment(3));
    this.libRegex = PathEncoder.decode(path.segment(4));
    this.srcRegex = PathEncoder.decode(path.segment(5));
    this.docRegex = PathEncoder.decode(path.segment(6));
    this.isExported = Boolean.valueOf(PathEncoder.decode(path.segment(7)));
  }

  @Override
  public IClasspathEntry[] getClasspathEntries() {
    final ArrayList<IClasspathEntry> entryList = new ArrayList<IClasspathEntry>();

    // retrieve library files
    final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
    if (project.exists()) {
      final IFolder libFolder = project.getProject().getFolder(libDir);
      if (libFolder.exists()) {
        final File libFile = new File(libFolder.getLocationURI());
        final File[] libraries = libFile.listFiles(new LibraryRegexFilter(libRegex));
        if (libraries != null) {
          for (File library : libraries) {

            // retrieve source attachment for library
            final Path src = getPathWithPart(library, srcRegex);

            // retrieve javadoc attachment for library
            final Path javadocPath = getPathWithPart(library, docRegex);
            final IClasspathAttribute[] attribs = getJavadocAttibute(javadocPath);

            entryList.add(JavaCore.newLibraryEntry(new Path(library.getAbsolutePath()), src, new Path("/"), new IAccessRule[] {}, attribs, isExported));
          }
        }
      }
    }
    return entryList.toArray(new IClasspathEntry[entryList.size()]);
  }

  private Path getPathWithPart(File library, String part) {
    if (part == null || part.equals("")) {
      return null;
    }

    final String filePrefix = library.getName().substring(0, library.getName().lastIndexOf("."));
    final FilenameFilter regexFilter = new AttachmentRegexFilter(".*".concat(filePrefix).concat(part));
    final ArrayList<File> dirs = new ArrayList<>();

    final File libRoot = library.getParentFile();
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

  private IClasspathAttribute[] getJavadocAttibute(Path path) {
    if (path == null) {
      return null;
    }
    return new IClasspathAttribute[] {JavaCore.newClasspathAttribute("javadoc_location", path.toFile().toURI().toString())};
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
    return path;
  }

}
