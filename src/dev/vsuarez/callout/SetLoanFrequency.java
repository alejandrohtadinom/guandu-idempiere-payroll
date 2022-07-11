/**
 * 
 */
package dev.vsuarez.callout;

import org.eevolution.model.MHREmployee;

import com.ingeint.base.CustomCallout;
import com.ingeint.model.MHRLoan;

/**
 * Set Frequency of Deduction of Loan Employee from Contract
 * @author <a href="mailto:victor.suarez.is@gmail.com">Ing. Victor Suarez</a>
 */
public class SetLoanFrequency extends CustomCallout {

	@Override
	protected String start() {
		if(getValue() == null)
			return null;
		int C_BPartner_ID = (int) getValue();
		MHREmployee employee = MHREmployee.getActiveEmployee(getCtx(), C_BPartner_ID, null);
		int frequency = employee.getHR_Payroll().getHR_Contract().getNetDays();
		String frequencyDeduction = String.format("%03d", frequency);
		
		getTab().setValue(MHRLoan.COLUMNNAME_FrequencyDeduction, frequencyDeduction);
		
		return null;
	}

}
