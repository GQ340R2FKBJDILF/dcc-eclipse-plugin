/* Java class source file
 * Copyright©              : Haag-Streit International
 * Created on              : 29.06.2011 by killert
 *
 * Revision of last commit : $Rev$
 * Author of last commit   : $Author$
 * Date of last commit     : $Date$
 * ==============================================================================
 */

package ch.arktos.dcc;

import java.net.URLDecoder;
import java.net.URLEncoder;


/**
 * The Class PathEncoder.
 * 
 * @author killert
 */
public class PathEncoder {

  /**
   * Encode.
   * 
   * @param input
   *          the input
   * @return the string
   */
  public static String encode(String input) {
    String output = "";
    try {
      // address the problem of conflicting '/' character in regular expressions for path compatibility
      output = URLEncoder.encode(input.replace("/", "@"), "UTF-8");
    }
    catch (Exception e) {
    }
    return output;
  }

  /**
   * Decode.
   * 
   * @param input
   *          the input
   * @return the string
   */
  public static String decode(String input) {
    String output = "";
    try {
      output = URLDecoder.decode(input, "UTF-8");
      // address the problem of conflicting '/' character in regular expressions for path compatibility
      output = output.replace("@", "/");
    }
    catch (Exception e) {
    }
    return output;
  }
}
