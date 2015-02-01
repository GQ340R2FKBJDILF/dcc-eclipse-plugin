/* Java class source file
 * Copyright©              : Haag-Streit International
 * Created on              : 08.07.2011 by killert
 *
 * Revision of last commit : $Rev$
 * Author of last commit   : $Author$
 * Date of last commit     : $Date$
 * ==============================================================================
 */

package ch.arktos.dcc;

import java.io.File;
import java.io.FilenameFilter;

/**
 * This filename filter will be used to determine which files will be included
 * in the container. The decision is made according to a regular expression.
 * 
 * @author killert
 */
public class AttachmentRegexFilter implements FilenameFilter {

	private final String _regex;

	/**
	 * Instantiates a new attachment regex filter.
	 * 
	 * @param regex
	 *            regular expression that is used to filter files
	 */
	public AttachmentRegexFilter(String regex) {
		_regex = regex;
	}

	/**
	 * Accept.
	 * 
	 * @param dir
	 *            the dir
	 * @param name
	 *            the name
	 * @return true, if successful
	 */
	@Override
	public boolean accept(File dir, String name) {
		if (name == null) {
			return false;
		}

		final File file = new File(dir, name);
		return !file.isDirectory() && name.matches(_regex);
	}
}
