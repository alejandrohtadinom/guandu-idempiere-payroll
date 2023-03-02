package dev.vsuarez.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.compiere.model.MBPBankAccount;
import org.compiere.model.MBPartner;
import org.compiere.model.MBankAccount;
import org.compiere.print.MPrintFormat;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Util;
import org.eevolution.model.MHREmployee;
import org.eevolution.model.MHRProcess;
import org.eevolution.model.X_HR_Period;

import com.ingeint.model.MHRPaymentSelection;
import com.ingeint.model.MHRPaymentSelectionLine;

/**
 * Export TXT BANABIH for Payroll Payment
 * @author <a href="mailto:victor.suarez.is@gmail.com">Ing. Victor Suarez</a>
 *
 */
public class HR_ExportBANABIH implements I_ReportExport {
	
	/** Name File								*/
	private StringBuilder m_NameFile = new StringBuilder("BANABIH_");
	/** Logger									*/
	static private CLogger	s_log = CLogger.getCLogger (HR_ExportBANABIH.class);
	/**	File Extension							*/
	private final String FILE_EXTENSION = ".txt";
	/**	Error Msg								*/
	private StringBuffer m_ErrorMsg = new StringBuffer("");
	/**	File 									*/
	private File m_File = null;
	/**	File Writer								*/
	private FileWriter m_FileWriter = null;
	/**	No Lines								*/
	private int m_NoLines = 0;
	
	/**	Break Lines								*/
	private final static char CR  = (char) 0x0D;
	private final static char LF  = (char) 0x0A; 
	private final static String CRLF  = "" + CR + LF;
	private final String DATE_FORMAT = "yyyyMMdd";
	
	/**				DATA				*/
	/**			   HEADER				*/
	private static final String TYPE_HEADER = "1";
	private static final String TYPE_LINE = "2";
	private static final String SWIFTCODE = "BAMRVECA";
	private static final String PRODUCT_TYPE = "NOMIN";
	private static final String PAYMENT_TYPE = "0000000222";
	private String m_DocumentNo = "";
	private String m_OrgTaxID = "";
	private String m_OrgTaxIDType = "";
	private int m_QtyLines = 0;
	private String m_QtyLinesStr = "";
	private String m_TotalAmtStr = "";
	private String m_DateTrx = "";
	private String m_BankAccountOrg = "";
	private int m_BankOrg_ID = 0;
	private String m_PaymentConcept = "";
	/**				LINES				*/
	private static final int EM_TAXID_TYPE = 0;
	private static final int EM_TAXID = 1;
	private static final int EM_PAYMENT_FORM = 2;
	private static final int EM_BANK_ACCOUNTNO = 3;
	private static final int EM_PAY_AMT = 4;
	private static final int EM_EMPLOYEE_CODE = 5;
	private static final int EM_EMPLOYEE_NAME = 6;
	private static final int EM_EMPLOYEE_MAIL = 7;
	/** BPartner Info Index for Nationality	    	*/
	private static final int     BP_NATIONALITY 	= 0;
	/** BPartner Info Index for Tax ID		    	*/
	private static final int     BP_TAX_ID 			= 1;
	/** BPartner Info Index for First Name 1    	*/
	private static final int     BP_FIRST_NAME_1 	= 2;
	/** BPartner Info Index for First Name 1    	*/
	private static final int     BP_FIRST_NAME_2 	= 3;
	/** BPartner Info Index for First Name 1    	*/
	private static final int     BP_LAST_NAME_1 	= 4;
	/** BPartner Info Index for First Name 1    	*/
	private static final int     BP_LAST_NAME_2 	= 5;
	/** BPartner Info Index for First Name 1    	*/
	private static final int     EM_CURRENT_AMT 	= 6;
	/** BPartner Info Index for Employee Start Date	*/
	private static final int     EM_START_DATE 		= 7;
	/** BPartner Info Index for Employee End Date	*/
	private static final int     EM_END_DATE 		= 8;
	/**	Separator								*/
	private final String 		SEPARATOR 			= ",";
	/**	Number Format							*/
	private DecimalFormat 		m_NumberFormatt 	= null;
	/**	Date Format								*/
	private SimpleDateFormat 	m_DateFormat 		= null;
	/**	Date Format								*/
	private String 	            m_CurrentAmt 		= null;

	@Override
	public int exportToFile(MPrintFormat printFormat, MHRPaymentSelection hrPaySelection, StringBuffer err) {
		if(err == null)
			err = new StringBuffer("");
		
		// formarto de impresion
		if(printFormat == null || hrPaySelection == null) {
			err.append("Debe existir un Formato de Impresion y una Seleccion de Pago para este proceso");
			m_ErrorMsg = err;
			return -1;
		}
		
		m_DocumentNo = hrPaySelection.getDocumentNo();
		m_OrgTaxID = DB.getSQLValueString(hrPaySelection.get_TrxName(), "SELECT TaxID FROM AD_OrgInfo WHERE AD_Org_ID =?", hrPaySelection.getAD_Org_ID());
		
		if(!Util.isEmpty(m_OrgTaxID)) {
			m_OrgTaxIDType = m_OrgTaxID.substring(0, 1);
			m_OrgTaxID = leftPadding(replaceAll(m_OrgTaxID).substring(1), 15, "0", true);
		} else {
			m_ErrorMsg.append("Organizacion no tiene Identificacion RIF");
			return -1;
		}
		
		m_NumberFormatt = new DecimalFormat("000000000.00");
		m_DateFormat = new SimpleDateFormat("ddMMyyyy");
		

		m_NameFile.append(hrPaySelection.getDocumentNo());
		m_NameFile.append(new SimpleDateFormat("ddMMyyyy").format(hrPaySelection.getDateDoc()));
		
		m_QtyLines = DB.getSQLValue(hrPaySelection.get_TrxName(), "SELECT Count(HR_PaymentSelectionLine_ID) FROM HR_PaymentSelectionLine WHERE HR_PaymentSelection_ID =? AND IsActive = 'Y'", hrPaySelection.getHR_PaymentSelection_ID());
		m_QtyLinesStr = String.valueOf(m_QtyLines);
		m_QtyLinesStr = leftPadding(m_QtyLinesStr, 8, "0", true);
		
		// Append ANDETEK
		
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
	 * Process Employee Data
	 * @param psLine Payment Selection Line
	 * @return Array Data of one Employee
	 */
	private String[] processEmployee(MHRPaymentSelectionLine psLine) {
		
		int p_C_BPartner_ID = psLine.getC_BPartner_ID();
		int p_AD_Org_ID = psLine.getAD_Org_ID(); 
		String p_TrxName = psLine.get_TrxName();
		
		String [] bpInfo = new String[8];
		//	
		//	Get Business Partner
		MBPartner bpartner = MBPartner.get(Env.getCtx(), p_C_BPartner_ID);
		//	Get Name
		String name = bpartner.getName();
		String name2 = bpartner.getName2();
		//	Valid Null
		if(name == null)
			name = "";
		if(name2 == null)
			name2 = "";
		//	End Index for First Name
		int endIndex = name.indexOf(" ");
		if(endIndex < 0)
			endIndex = name.length();
		//	Extract First Name 1
		String m_FirstName1 = name.substring(0, endIndex);
		//	Extract First Name 2
		String m_FirstName2 = ((endIndex + 1) > name.length() ? " " : name.substring(endIndex + 1));
		endIndex = m_FirstName2.indexOf(" ");
		//	Cut First Name 2
		if(endIndex < 0)
			endIndex = m_FirstName2.length();
		m_FirstName2 = m_FirstName2.substring(0, endIndex);
		//	End Index for Last Name
		endIndex = name2.indexOf(" ");
		if(endIndex < 0)
			endIndex = name2.length();
		//	Extract Last Name 1
		String m_LastName1 = name2.substring(0, endIndex);
		//	Extract Last Name 2
		String m_LastName2 = ((endIndex + 1) > name2.length() ? " " : name2.substring(endIndex + 1));
		endIndex = m_LastName2.indexOf(" ");
		//	Cut Last Name 2
		if(endIndex < 0)
			endIndex = m_LastName2.length();
		m_LastName2 = m_LastName2.substring(0, endIndex);
		//	Valid length
		if(m_FirstName1.length() > 25)
			m_FirstName1 = m_FirstName1.substring(0, 24);
		else if(m_FirstName1.length() == 0)
			m_FirstName1 = "";
		if(m_FirstName2.length() > 25)
			m_FirstName2 = m_FirstName2.substring(0, 24);
		else if(m_FirstName2.length() == 0)
			m_FirstName2 = "";
		if(m_LastName1.length() > 25)
			m_LastName1 = m_LastName1.substring(0, 24);
		else if(m_LastName1.length() == 0)
			m_LastName1 = "";
		if(m_LastName2.length() > 25)
			m_LastName2 = m_LastName2.substring(0, 24);
		else if(m_LastName2.length() == 0)
			m_LastName2 = "";
			
		//	Get Active Employee
		MHREmployee employee = MHREmployee.getActiveEmployee(Env.getCtx(), bpartner.getC_BPartner_ID(), p_AD_Org_ID, p_TrxName);
		//	Valid Employee
		if(employee == null)
			return null;
		//	Get Start Date
		String startDate = m_DateFormat.format(employee.getStartDate());
		String endDate = "";
		//	Get End Date
		if(employee.get_Value("DateFinish") != null)
			endDate = m_DateFormat.format(employee.get_Value("DateFinish"));
		
		if (psLine.getAmount() == null) {
			m_CurrentAmt = m_NumberFormatt.format(Env.ZERO).toString().replace(",", ".").replace(".", "");
		} else {
			m_CurrentAmt = m_NumberFormatt.format(psLine.getAmount()).toString().replace(",", ".").replace(".", "");
		}
		
		//	Set Array
		bpInfo[BP_NATIONALITY]	= bpartner.getTaxID().substring(0, 1);
		bpInfo[BP_TAX_ID]		= bpartner.getValue().substring(1, -1);
		bpInfo[BP_FIRST_NAME_1]	= m_FirstName1;
		bpInfo[BP_FIRST_NAME_2]	= m_FirstName2;
		bpInfo[BP_LAST_NAME_1]	= m_LastName1;
		bpInfo[BP_LAST_NAME_2]	= m_LastName2;
		bpInfo[EM_CURRENT_AMT]	= m_CurrentAmt;
		bpInfo[EM_START_DATE]	= startDate;
		bpInfo[EM_END_DATE]		= endDate;
			
		//	Return
		return bpInfo;
	}
	
	/**
	 * Write Lines
	 * @throws IOException
	 */
	private void writeLine(String[] employee) throws IOException {			
		StringBuffer line = new StringBuffer();

		//	Nationality
		line.append(employee[BP_NATIONALITY])
			.append(SEPARATOR)
			//	Tax ID
			.append(employee[BP_TAX_ID])
			.append(SEPARATOR)
			//	First Name 1
			.append(employee[BP_FIRST_NAME_1])
			.append(SEPARATOR)
			//	First Name 2
			.append(employee[BP_FIRST_NAME_2])
			.append(SEPARATOR)
			//	Last Name 1
			.append(employee[BP_LAST_NAME_1])
			.append(SEPARATOR)
			//	Last Name 2
			.append(employee[BP_LAST_NAME_2])
			.append(SEPARATOR)
			//	Amount
			.append(employee[EM_CURRENT_AMT])
			.append(SEPARATOR)
			//	Employee Start Date
			.append(employee[EM_START_DATE])
			.append(SEPARATOR)
			//	Employee End Date
			.append(employee[EM_END_DATE]);
		//	Write Line
		m_FileWriter.write(line.toString());
		m_NoLines ++;
	}


	@Override
	public String getNameFile() {
		return m_NameFile.toString();
	}

	/**
	 * Method that removes accents and special characters from a string of text, using the canonical method.
	 * @param String input
	 * @return string of clean text of accents and special characters.
	 */
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
	 * Right padding optional fixed length
	 * @param text
	 * @param length
	 * @param padding
	 * @param isFixedLength
	 * @return
	 */
	public String rightPadding(String text, int length, String padding, boolean isFixedLength) {
		return rightPadding(text, length, padding, isFixedLength, false, null);
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
	 * Right padding, it also cut text if it is necessary
	 * @param text
	 * @param length
	 * @param padding
	 * @param isFixedLength
	 * @param isMandatory
	 * @param mandatoryMessage
	 * @return
	 */
	public String rightPadding(String text, int length, String padding, boolean isFixedLength, boolean isMandatory, String mandatoryMessage) {
		return addPadding(text, length, padding, isFixedLength, isMandatory, mandatoryMessage, false);
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
