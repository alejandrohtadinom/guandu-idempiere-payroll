package com.ingeint.event;

import java.util.List;

import org.compiere.model.Query;
import org.eevolution.model.MHRProcess;

import com.ingeint.base.CustomEventHandler;
import com.ingeint.model.MHRLoanLines;

public class AfterReactiveItHRProcess extends CustomEventHandler {

	@Override
	protected void doHandleEvent() {
		MHRProcess hrProcess = (MHRProcess) getPO();
		List<MHRLoanLines> Lines = new Query(hrProcess.getCtx(), MHRLoanLines.Table_Name, "HR_Process_ID = ?", hrProcess.get_TrxName())
				.setParameters(hrProcess.get_ID())
				.list();
		
		for (MHRLoanLines line : Lines) {
			line.setIsPaid(false);
			line.setHR_Process_ID(0);
			line.saveEx();
		}
	}

}
