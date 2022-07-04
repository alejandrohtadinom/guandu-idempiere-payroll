/**
 * 
 */
package dev.vsuarez.utils;

import java.io.File;

import org.compiere.print.MPrintFormat;

import com.ingeint.model.MHRPaymentSelection;

/**
 * @author <a href="mailto:victor.suarez.is@gmail.com">Ing. Victor Suarez</a>
 *
 */
public interface I_ReportExport {
	
	/**************************************************************************
	 *  Export to File
	 *  @param Print Format
	 *  @param HR Payment Selection
	 *  @param Errors
	 *  @return number of lines
	 */
	public int exportToFile (MPrintFormat printFormat, MHRPaymentSelection hrPaySelection, StringBuffer err);

	/**
	 *  Get Name File
	 *  @return Name File
	 */
	public String getNameFile();
	
	/**
	 * Method that removes accents and special characters from a string of text, using the canonical method.
	 * @param String input
	 * @return string of clean text of accents and special characters.
	 */
	public String replaceAll(String input);
	
	/**
	 * Get Error Msg
	 * @return @Error@ Msg
	 */
	public String getErrorMsg();
	
	/**
	 * Get File
	 * @return file generated
	 */
	public File getFile();
	
}
