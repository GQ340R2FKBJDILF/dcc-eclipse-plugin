package ch.arktos.dcc;

import org.eclipse.osgi.util.NLS;

/**
 * The Class Messages.
 */
public class Messages extends NLS {

	private static final String BUNDLE_NAME = "ch.arktos.dcc.messages"; //$NON-NLS-1$

	public static String ProjectLabel;

	public static String ProjectErr;

	public static String ProjectSelectionTitle;

	public static String ProjectSelectionMessage;

	public static String SelectButton;

	public static String SelectDialog;

	public static String BinDirLabel;

	public static String BinDirErr;

	public static String LibDirLabel;

	public static String LibDirErr;

	public static String SrcDirLabel;

	public static String SrcDirErr;

	public static String DocDirLabel;

	public static String DocDirErr;

	public static String LibRegexLabel;

	public static String LibRegexErr;

	public static String SrcRegexLabel;

	public static String SrcRegexErr;

	public static String DocRegexLabel;

	public static String DocRegexErr;

	public static String IsExportedLabel;

	public static String InvalidContainer;

	public static String PageDesc;

	public static String PageName;

	public static String PageTitle;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
