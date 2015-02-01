
package ch.arktos.dcc;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;


/**
 * The Class DynamicClasspathContainerViewerFilter.
 * 
 * @author killert
 */
public class DynamicClasspathContainerViewerFilter extends ViewerFilter {

  /**
   * Instantiates a new dynamic classpath container viewer filter.
   */
  public DynamicClasspathContainerViewerFilter() {
  }

  /**
   * Select.
   * 
   * @param viewer
   *          the viewer
   * @param parent
   *          the parent
   * @param element
   *          the element
   * @return true, if successful
   */
  @Override
  public boolean select(Viewer viewer, Object parent, Object element) {
    return true;
  }

}
