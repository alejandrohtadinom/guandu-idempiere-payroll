/**
 * 
 */
package dev.vsuarez.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.compiere.model.MBPartner;
import org.compiere.print.MPrintFormat;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Util;
import org.eevolution.model.MHRProcess;

import com.ingeint.model.MHRPaymentSelection;
import com.ingeint.model.MHRPaymentSelectionLine;

/**
 * Export TXT TodoTicket for Payroll Payment
 * @author <a href="mailto:victor.suarez.is@gmail.com">Ing. Victor Suarez</a>
 *
 */
public class HR_ExportTXT_TodoTicket implements I_ReportExport {
	
	/** Logger										*/
	static private CLogger	s_log = CLogger.getCLogger (HR_ExportTXT_TodoTicket.class);
	
	/**	File Extension							*/
	private final String		FILE_EXTENSION		= ".txt";
	/**	File Writer								*/
	private FileWriter 				m_FileWriter	= null;
	/**	File 									*/
	private File m_File = null;
	/** Name File								*/
	private StringBuilder m_NameFile = new StringBuilder("TodoTicket_");
	/**	Error Msg								*/
	private StringBuffer m_ErrorMsg = new StringBuffer("");
	/**	Break Lines								*/
	private final static char CR  = (char) 0x0D;
	private final static char LF  = (char) 0x0A; 
	private final static String CRLF  = "" + CR + LF;
	private final String HEADER_SHORT_DATE_FORMAT = "ddMMyyyy";
	/**	No Lines								*/
	private int m_NoLines = 0;
	
	/**				DATA				*/
	/**			   HEADER				*/
	private String m_DateTrx = "";
	/**				LINES				*/
	private static final int EM_TAXID_TYPE = 0;
	private static final int EM_TAXID = 1;
	private static final int EM_AMOUNT = 2;
	private static final int EM_DATE = 3;

	@Override
	public int exportToFile(MPrintFormat printFormat, MHRPaymentSelection hrPaySelection, StringBuffer err) {
		if(err == null)
			err = new StringBuffer("");
		if(printFormat == null || hrPaySelection == null) {
			err.append("Debe existir un Formato de Impresion y una Seleccion de Pago para este proceso");
			m_ErrorMsg = err;
			return -1;
		}
		//	Format Date
		SimpleDateFormat shortFormat = new SimpleDateFormat(HEADER_SHORT_DATE_FORMAT);
		m_DateTrx = shortFormat.format(hrPaySelection.getDateDoc());
		
		MHRProcess hrProcess = (MHRProcess) hrPaySelection.getHR_Process();
		m_NameFile.append(hrProcess.getDocumentNo());
		m_NameFile.append(shortFormat.format(hrProcess.getDateAcct()));
		
		try {
			File tmpFile = File.createTempFile(m_NameFile.toString(), FILE_EXTENSION);
			m_File = new File(tmpFile.getParent()+File.separator+m_NameFile.toString()+FILE_EXTENSION);
			tmpFile.renameTo(m_File);
			if(m_File.exists())
				m_File.delete();
			//	
			m_FileWriter = new FileWriter(m_File);
			// write lines
			for(MHRPaymentSelectionLine line : hrPaySelection.getLines()) {
				String[] employee = processEmployee(line);
				if(employee == null)
					return -1;
				writeLine(employee);
			}
			//	Close
			m_FileWriter.flush();
			m_FileWriter.close();
		} catch (Exception e) {
			err.append(e.toString());
			s_log.log(Level.SEVERE, "", e);
			m_ErrorMsg = err;
			return -1;
		}
		return m_NoLines;
	}
	
	/**
	 * Write Lines
	 * @throws IOException
	 */
	private void writeLine(String[] employee) throws IOException {
		StringBuffer line = new StringBuffer("");
		line.append(employee[EM_TAXID_TYPE])		// TaxID Type
			.append(employee[EM_TAXID])				// TaxID
			.append(rightPadding("", 2, " "))		// Separator
			.append(employee[EM_AMOUNT])			// Amount
			.append(employee[EM_DATE])				// Date
			.append(CRLF)
			;
		m_FileWriter.write(line.toString());
		m_NoLines++;
	}

	@Override
	public String getNameFile() {
		return m_NameFile.toString();
	}

	@Override
	public String replaceAll(String input) {
	    String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
	    Pattern pattern = Pattern.compile("[^\\p{ASCII}]");
	    return pattern.matcher(normalized).replaceAll("");
	}

	@Override
	public String getErrorMsg() {
		return m_ErrorMsg.toString();
	}

	@Override
	public File getFile() {
		return m_File;
	}
	
	/**
	 * Process Employee Data
	 * @param psLine Payment Selection Line
	 * @return Array Data of one Employee
	 */
	private String[] processEmployee(MHRPaymentSelectionLine psLine) {
		String[] employeeInf = new String[4];
		MBPartner bpEmployee = (MBPartner) psLine.getC_BPartner();

	    String amountStr = String.format("%.2f", psLine.getAmount().abs()).replace(".", "").replace(",", "");
	    amountStr = leftPadding(amountStr, 21, "0", true);
	    
	    String taxID = replaceAll(bpEmployee.getTaxID());

		employeeInf[EM_TAXID_TYPE] = bpEmployee.getTaxID().substring(0, 1);								// TaxID Type
	    employeeInf[EM_TAXID] = leftPadding(taxID.substring(1, taxID.length()-1), 9, "0");	// TaxID
	    employeeInf[EM_AMOUNT] = amountStr;																// Amount
		employeeInf[EM_DATE] = m_DateTrx;																// Date
		
		return employeeInf;
	}
	
	/**
	 * Left padding optional fixed length
	 * @param text
	 * @param length
	 * @param padding
	 * @param isFixedLength
	 * @return
	 */
	public String leftPadding(String text, int length, String padding, boolean isFixedLength) {
		return leftPadding(text, length, padding, isFixedLength, false, null);
	}
	
	/**
	 * Left padding, it also cut text if it is necessary
	 * @param text
	 * @param length
	 * @param padding
	 * @param isFixedLength
	 * @param isMandatory
	 * @param mandatoryMessage
	 * @return
	 */
	public String leftPadding(String text, int length, String padding, boolean isFixedLength, boolean isMandatory, String mandatoryMessage) {
		return addPadding(text, length, padding, isFixedLength, isMandatory, mandatoryMessage, true);
	}
	
	/**
	 * Add Padding, for using internal
	 * @param text
	 * @param length
	 * @param padding
	 * @param isFixedLength
	 * @param isMandatory
	 * @param mandatoryMessage
	 * @param isLeft
	 * @return
	 */
	private String addPadding(String text, int length, String padding, boolean isFixedLength, boolean isMandatory, String mandatoryMessage, boolean isLeft) {
		if(Util.isEmpty(text)) {
			if(isMandatory
					&& !Util.isEmpty(mandatoryMessage)) {
				addError(mandatoryMessage);
			}
			//	Return void text
			return text;
		}
		String processedText = text;
		//	Process it
		if(isFixedLength) {
			processedText = processedText.substring(0, processedText.length() >= length? length: processedText.length());
		}
		//	For padding 
		if(isLeft) {
			processedText = leftPadding(processedText, length, padding);
		} else {
			processedText = rightPadding(processedText, length, padding);
		}
		//	Return
		return processedText;
	}
	
	/**
	 * Add right padding
	 * @param text
	 * @param length
	 * @param padd
	 * @return
	 */
	public String rightPadding(String text, int length, String padd) {
		return addPadding(text, length, padd, false);
	}
	
	/**
	 * Add left padding
	 * @param text
	 * @param length
	 * @param padd
	 * @return
	 */
	public String leftPadding(String text, int length, String padd) {
		return addPadding(text, length, padd, true);
	}
	
	/**
	 * Add padding, used for add to left and right padding
	 * @param text
	 * @param length
	 * @param padd
	 * @param left
	 * @return
	 */
	private String addPadding(String text, int length, String padd, boolean left) {
		if(text == null
				|| padd ==null) {
			return text;
		}
		//
		String format = "%1$";
		if(!left) {
			format = "%1$-";
		}
		return String.format(format + length + "s", text).replace(" ", padd);
	}
	
	/**
	 * Add error to buffer
	 * @param error
	 */
	public void addError(String error) {
		if(m_ErrorMsg.length() > 0) {
			m_ErrorMsg.append(Env.NL);
		}
		//	Add error
		m_ErrorMsg.append(error);
	}

}
