package com.ingeint.event;

import java.sql.Timestamp;
import java.util.List;

import org.compiere.model.Query;
import org.eevolution.model.MHRProcess;

import com.ingeint.base.CustomEventHandler;
import com.ingeint.model.MHRLoan;
import com.ingeint.model.MHRLoanLines;

public class AfterCompletedHRProcess extends CustomEventHandler {

	@Override
	protected void doHandleEvent() {
		MHRProcess hrProcess = (MHRProcess) getPO();
		String whereClause = "DocStatus = 'CO' AND IsLoanActive = 'Y' AND OpenAmt > 0";
		List<MHRLoan> loans = new Query(hrProcess.getCtx(), MHRLoan.Table_Name, whereClause, hrProcess.get_TrxName())
				.setOnlyActiveRecords(true)
				.setOrderBy(MHRLoan.COLUMNNAME_DateStart)
				.list();
		Timestamp dateProcess = hrProcess.getHR_Period().getEndDate();
		for(MHRLoan loan : loans) {
			int qtyMov = new Query(loan.getCtx(), MHRLoan.Table_Name, "HR_Concept_ID =? AND C_BPartner_ID =?", loan.get_TrxName())
					.setOnlyActiveRecords(true)
					.setParameters(loan.getHR_Concept_ID(), loan.getC_BPartner_ID())
					.count();
			if(qtyMov > 0) {
				MHRLoanLines line = new Query(loan.getCtx(), MHRLoanLines.Table_Name, "HR_Loan_ID =? AND (IsPaid IS NULL OR IsPaid = 'N') AND DATE_TRUNC('day', DueDate) <=?", loan.get_TrxName())
						.setOnlyActiveRecords(true)
						.setParameters(loan.getHR_Loan_ID(), dateProcess)
						.setOrderBy(MHRLoanLines.COLUMNNAME_DueDate)
						.first();
				if(line != null) {
					line.setHR_Process_ID(hrProcess.getHR_Process_ID());
					line.setIsPaid(true);
					line.saveEx();
				}
			}
		}
	}

}
