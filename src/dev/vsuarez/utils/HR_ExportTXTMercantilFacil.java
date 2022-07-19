package dev.vsuarez.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
 * Export TXT Mercantil for Payroll Payment
 * @author <a href="mailto:victor.suarez.is@gmail.com">Ing. Victor Suarez</a>
 *
 */
public class HR_ExportTXTMercantilFacil implements I_ReportExport {
	
	/** Name File								*/
	private StringBuilder m_NameFile = new StringBuilder("NominaFacil_");
	/** Logger									*/
	static private CLogger	s_log = CLogger.getCLogger (HR_ExportTXTMercantilFacil.class);
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
	private final String HEADER_SHORT_DATE_FORMAT = "yyyyMMdd";
	
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

	@Override
	public int exportToFile(MPrintFormat printFormat, MHRPaymentSelection hrPaySelection, StringBuffer err) {
		if(err == null)
			err = new StringBuffer("");
		if(printFormat == null || hrPaySelection == null) {
			err.append("Debe existir un Formato de Impresion y una Seleccion de Pago para este proceso");
			m_ErrorMsg = err;
			return -1;
		}
		m_DocumentNo = hrPaySelection.getDocumentNo();
		m_OrgTaxID = DB.getSQLValueString(hrPaySelection.get_TrxName(), "SELECT TaxID FROM AD_OrgInfo WHERE AD_Org_ID =?", hrPaySelection.getAD_Org_ID());
		if(!Util.isEmpty(m_OrgTaxID)) {
//			m_OrgTaxID = m_OrgTaxID.replace("-", "").trim();
			m_OrgTaxIDType = m_OrgTaxID.substring(0, 1);
//			m_OrgTaxID = m_OrgTaxID.replaceAll("\\D+", "");
			m_OrgTaxID = leftPadding(replaceAll(m_OrgTaxID).substring(1), 15, "0", true);
		} else {
			m_ErrorMsg.append("Organizacion no tiene Identificacion RIF");
			return -1;
		}
		MBankAccount bankAccount = (MBankAccount) hrPaySelection.getC_BankAccount(); 
		MHRProcess hrProcess = (MHRProcess) hrPaySelection.getHR_Process();
		m_NameFile.append(bankAccount.getC_Bank().getName().trim().replaceAll(" ", ""));
		m_NameFile.append(bankAccount.getAccountNo().substring(16));
		m_NameFile.append(hrProcess.getDocumentNo());
		m_NameFile.append(new SimpleDateFormat("ddMMyyyy").format(hrProcess.getDateAcct()));
		
		m_QtyLines = DB.getSQLValue(hrPaySelection.get_TrxName(), "SELECT Count(HR_PaymentSelectionLine_ID) FROM HR_PaymentSelectionLine WHERE HR_PaymentSelection_ID =? AND IsActive = 'Y'", hrPaySelection.getHR_PaymentSelection_ID());
		m_QtyLinesStr = String.valueOf(m_QtyLines);
		m_QtyLinesStr = leftPadding(m_QtyLinesStr, 8, "0", true);
		
		m_TotalAmtStr = String.format("%.2f", hrPaySelection.getTotalLines().abs()).replace(".", "").replace(",", "");
		m_TotalAmtStr = leftPadding(m_TotalAmtStr, 17, "0", true);
		
		//	Format Date
		SimpleDateFormat shortFormat = new SimpleDateFormat(HEADER_SHORT_DATE_FORMAT);
		m_DateTrx = shortFormat.format(hrPaySelection.getDateDoc());
		
		m_BankAccountOrg = bankAccount.getAccountNo();
		m_BankOrg_ID = bankAccount.getC_Bank_ID();
		
		X_HR_Period period = (X_HR_Period) hrProcess.getHR_Period();
		String from = new SimpleDateFormat("dd-MM-yyyy").format(period.getStartDate());
		String to = new SimpleDateFormat("dd-MM-yyyy").format(period.getEndDate());
		m_PaymentConcept = "Pago Nomina desde la fecha " + from + " a la fecha " + to;
		m_PaymentConcept = rightPadding(m_PaymentConcept, 80, " ");
		
		try {
			File tmpFile = File.createTempFile(m_NameFile.toString(), FILE_EXTENSION);
			m_File = new File(tmpFile.getParent()+File.separator+m_NameFile.toString()+FILE_EXTENSION);
			tmpFile.renameTo(m_File);
			if(m_File.exists())
				m_File.delete();
			//	
			m_FileWriter = new FileWriter(m_File);
			//  write header
			writeHeader();
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
	 * Write Header
	 * @throws IOException
	 */
	private void writeHeader() throws IOException {
		StringBuffer header = new StringBuffer("");
		header.append(TYPE_HEADER)					// Type Header - Constant
			.append(rightPadding(SWIFTCODE, 12, " ", true))	// Swift Code of Bank
			.append(rightPadding(replaceAll(m_DocumentNo), 15, " ", true))	// Document No of Payment Selection
			.append(PRODUCT_TYPE)					// Product Type for Payroll: NOMIN
			.append(PAYMENT_TYPE)					// Payment Type for Payroll
			.append(m_OrgTaxIDType)					// Tax ID Type for Org
			.append(m_OrgTaxID)						// Tax ID of Org
			.append(m_QtyLinesStr)					// Qty Lines of Payment Details
			.append(m_TotalAmtStr)					// Total Amount of Payment
			.append(m_DateTrx)						// Date Transaction
			.append(m_BankAccountOrg)				// Bank Account of Org
			.append(leftPadding("", 7, "0"))		//  Reserved
			.append(leftPadding("", 8, "0"))		//  Reserved Note Serial Number Company
			.append(leftPadding("", 4, "0")) 		//	Reserved Response Code (Data Output)
			.append(leftPadding("", 8, "0")) 		//	Reserved Date process (Data Output)
			.append(leftPadding("", 261, "0"))		// 	Reserved
			.append(CRLF)
			;
		
		m_FileWriter.write(header.toString());
		m_NoLines++;
	}
	
	/**
	 * Process Employee Data
	 * @param psLine Payment Selection Line
	 * @return Array Data of one Employee
	 */
	private String[] processEmployee(MHRPaymentSelectionLine psLine) {
		String[] employee = new String[8];
		MBPartner bpEmployee = (MBPartner) psLine.getC_BPartner();
		MBPBankAccount bpBankAccount = (MBPBankAccount) psLine.getC_BP_BankAccount();
		if(bpBankAccount == null || (bpBankAccount.getAccountNo() == null && bpBankAccount.getA_Name() == null)) {
			addError("Empleado " + bpEmployee.getName() + ", no Tiene Cuenta Bancaria");
			return null;
		}
		String payForm = "1";
		if(m_BankOrg_ID != bpBankAccount.getC_Bank_ID())
			payForm = "3";
		String bankAccountNo = bpBankAccount.getAccountNo();
		if(Util.isEmpty(bankAccountNo, true))
			bankAccountNo = bpBankAccount.getA_Name();
		
		String payAmtStr = String.format("%.2f", psLine.getPayAmt().abs()).replace(".", "").replace(",", "");
		payAmtStr = leftPadding(payAmtStr, 17, "0", true);
		MHREmployee hrEmployee = MHREmployee.getActiveEmployee(psLine.getCtx(), bpEmployee.getC_BPartner_ID(), psLine.getAD_Org_ID(), psLine.get_TrxName());
		String employeeCode = hrEmployee.getNationalCode();
		if(Util.isEmpty(employeeCode, true))
			employeeCode = hrEmployee.getCode();
		if(Util.isEmpty(employeeCode, true))
			employeeCode = bpEmployee.getValue();
		if(Util.isEmpty(employeeCode, true))
			employeeCode = bpEmployee.getTaxID();
		
		String email = bpEmployee.getURL();
		if(Util.isEmpty(email, true) || !email.contains("@"))
			email = DB.getSQLValueString(bpEmployee.get_TrxName(), "SELECT Email FROM AD_User WHERE C_BPartner_ID =? AND IsActive = 'Y' AND Email LIKE '%@%' "
					+ "ORDER BY AD_User_ID DESC", bpEmployee.getC_BPartner_ID());
		if(Util.isEmpty(email, true))
			email = "";
		
		employee[EM_TAXID_TYPE] = bpEmployee.getTaxID().substring(0, 1);
		employee[EM_TAXID] = leftPadding(replaceAll(bpEmployee.getTaxID()).substring(1), 15, "0");
		employee[EM_PAYMENT_FORM] = payForm;
		employee[EM_BANK_ACCOUNTNO] = replaceAll(bankAccountNo);
		employee[EM_PAY_AMT] = payAmtStr;
		employee[EM_EMPLOYEE_CODE] = rightPadding(replaceAll(employeeCode), 16, " ");
		employee[EM_EMPLOYEE_NAME] = rightPadding(replaceAll(bpEmployee.getName()), 60, " ");
		employee[EM_EMPLOYEE_MAIL] = rightPadding(email, 50, " ");
		
		return employee;
	}
	
	/**
	 * Write Lines
	 * @throws IOException
	 */
	private void writeLine(String[] employee) throws IOException {			
		StringBuffer line = new StringBuffer("");
		line.append(TYPE_LINE)						// Type Line - Constant
			.append(employee[EM_TAXID_TYPE])		// TaxID Type for Employee
			.append(employee[EM_TAXID])				// TaxID
			.append(employee[EM_PAYMENT_FORM])   	// Payment Form
			.append(leftPadding("", 12, "0")) 		// Reserved
			.append(rightPadding("", 30, " "))		// Reserved
			.append(employee[EM_BANK_ACCOUNTNO])	// Bank Account No Employee
			.append(employee[EM_PAY_AMT]) 			// Payment Amount
			.append(employee[EM_EMPLOYEE_CODE]) 	// Employee Code
			.append(PAYMENT_TYPE) 					// Payment Type
			.append(leftPadding("", 3, "0")) 		// Reserved
			.append(employee[EM_EMPLOYEE_NAME]) 	// Name Employee
			.append(leftPadding("", 15, "0")) 		// Reserved
			.append(employee[EM_EMPLOYEE_MAIL]) 	// Email
			.append(leftPadding("", 4, "0"))		// Reserved Response Code
			.append(rightPadding("", 30, " ")) 		// Reserved Response Message
			.append(m_PaymentConcept) 				// Payment Concept
			.append(leftPadding("", 35, "0")) 		// Reserved
			.append(CRLF)
			;
		m_FileWriter.write(line.toString());
		m_NoLines++;
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
