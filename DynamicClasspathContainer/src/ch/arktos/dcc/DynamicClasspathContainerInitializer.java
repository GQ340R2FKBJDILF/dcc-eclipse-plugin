package ch.arktos.dcc;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * The Class DynamicClasspathContainerInitializer.
 * 
 * @author killert
 */
public class DynamicClasspathContainerInitializer extends ClasspathContainerInitializer {

	/** The monitor. */
	private final IProgressMonitor monitor;

	/**
	 * Instantiates a new dynamic classpath container initializer.
	 */
	public DynamicClasspathContainerInitializer() {
		this.monitor = new NullProgressMonitor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.core.ClasspathContainerInitializer#initialize(org.eclipse
	 * .core.runtime.IPath, org.eclipse.jdt.core.IJavaProject)
	 */
	@Override
	public void initialize(IPath path, IJavaProject project) throws CoreException {
		final DynamicClasspathContainer container = new DynamicClasspathContainer(path, project);
		if (container.isValid()) {
			JavaCore.setClasspathContainer(path, new IJavaProject[] { project }, new IClasspathContainer[] { container }, monitor);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.core.ClasspathContainerInitializer#getComparisonID(org
	 * .eclipse.core.runtime.IPath, org.eclipse.jdt.core.IJavaProject)
	 */
	@Override
	public Object getComparisonID(IPath containerPath, IJavaProject project) {
		return containerPath;
	}
}
