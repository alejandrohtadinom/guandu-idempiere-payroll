/**
 * 
 */
package dev.vsuarez.process;

import java.io.File;
import java.util.logging.Level;

import org.compiere.model.MAttachment;
import org.compiere.model.MBankAccount;
import org.compiere.model.Query;
import org.compiere.print.MPrintFormat;

import com.ingeint.base.CustomProcess;
import com.ingeint.model.MHRPaymentSelection;

import dev.vsuarez.utils.I_ReportExport;

/**
 * Process for Print / Export Payment Selection of Payroll
 * @author <a href="mailto:victor.suarez.is@gmail.com">Ing. Victor Suarez</a>
 *
 */
public class HR_PrintExportPaySelection extends CustomProcess {

	@Override
	protected void prepare() { }

	@Override
	protected String doIt() throws Exception {
		MHRPaymentSelection hrPaySelection = new MHRPaymentSelection(getCtx(), getRecord_ID(), get_TrxName());
		MBankAccount bankAccount = (MBankAccount) hrPaySelection.getC_BankAccount();
		MPrintFormat printFormat = new Query(getCtx(), MPrintFormat.Table_Name, "C_BankAccountDoc.C_BankAccount_ID=?", get_TrxName())
				.addJoinClause("JOIN C_BankAccountDoc C_BankAccountDoc ON C_BankAccountDoc.Payroll_PrintFormat_ID = AD_PrintFormat.AD_PrintFormat_ID")
				.setOnlyActiveRecords(true)
				.setParameters(bankAccount.getC_BankAccount_ID())
				.first();
		
		if(printFormat == null) 
			return "@Error@: Cuenta Bancaria " + bankAccount.getName() + ", no tiene Formato de Impresion para Nomina";
		
		return exportToFile(printFormat, hrPaySelection);
	}
	
	 /**
	  * 
	  * @param Print Format
	  * @param HR Payment Selection
	  * @return
	  */
	private String exportToFile(MPrintFormat printFormat, MHRPaymentSelection hrPaySelection) {
		String className = printFormat.get_ValueAsString("Classname");
		int no = 0;
		StringBuffer err = new StringBuffer("");
		if(className == null || className == "")
			return "@Error@: El Formato de Impresion debe indicar el Nombre de la Clase";
		
		I_ReportExport reportExport = null;
		try {
			Class<?> clazz = Class.forName(className);
			reportExport = (I_ReportExport)clazz.getConstructor().newInstance();
			//	Generate File
			no = reportExport.exportToFile(printFormat, hrPaySelection, err);
			if(no == -1)
				return "@Error@: " + reportExport.getErrorMsg();
			attachFile(reportExport.getFile());
		} catch (ClassNotFoundException e) {
			no = -1;
			err.append("@Error@: No custom ReportExport class " + className + " - " + e.toString());
			log.log(Level.SEVERE, err.toString(), e);
		} catch (Exception e) {
			no = -1;
			err.append("@Error@: in " + className + " check log, " + e.toString());
			log.log(Level.SEVERE, err.toString(), e);
		}
		//	
		if (no >= 0) {
			return "Archivo generado y anexado, con " + no + " lineas";
		} else {
			return "@Error@ " + err.toString();
		}
	}

	/**
	 * Attach file Generated
	 * @param file
	 */
	private void attachFile(File file) {
		if(file == null)
			return;
		
		MAttachment attach = new  MAttachment(getCtx(), MHRPaymentSelection.Table_ID, getRecord_ID(), get_TrxName());
		attach.addEntry(file);
		attach.save();
	}

}
