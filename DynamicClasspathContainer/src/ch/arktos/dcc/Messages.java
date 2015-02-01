package ch.arktos.dcc;

import org.eclipse.osgi.util.NLS;

/**
 * The Class Messages.
 */
public class Messages extends NLS {

	private static final String BUNDLE_NAME = "ch.arktos.dcc.messages"; //$NON-NLS-1$

	/** The Project label. */
	public static String ProjectLabel;

	/** The Project err. */
	public static String ProjectErr;

	/** The Select button. */
	public static String SelectButton;

	/** The Select dialog. */
	public static String SelectDialog;

	/** The Bin dir label. */
	public static String BinDirLabel;

	/** The Bin dir err. */
	public static String BinDirErr;

	/** The Lib dir label. */
	public static String LibDirLabel;

	/** The Lib dir err. */
	public static String LibDirErr;

	/** The Src dir label. */
	public static String SrcDirLabel;

	/** The Src dir err. */
	public static String SrcDirErr;

	/** The Doc dir label. */
	public static String DocDirLabel;

	/** The Doc dir err. */
	public static String DocDirErr;

	/** The Lib regex label. */
	public static String LibRegexLabel;

	/** The Lib regex err. */
	public static String LibRegexErr;

	/** The Src regex label. */
	public static String SrcRegexLabel;

	/** The Src regex err. */
	public static String SrcRegexErr;

	/** The Doc regex label. */
	public static String DocRegexLabel;

	/** The Doc regex err. */
	public static String DocRegexErr;

	/** The Is exported label. */
	public static String IsExportedLabel;

	/** The Invalid container. */
	public static String InvalidContainer;

	/** The Page desc. */
	public static String PageDesc;

	/** The Page name. */
	public static String PageName;

	/** The Page title. */
	public static String PageTitle;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
