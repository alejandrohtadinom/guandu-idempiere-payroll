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
		
		if(frequency == 7 || frequency == 8)
			getTab().setValue(MHRLoan.COLUMNNAME_FrequencyDeduction, MHRLoan.FREQUENCYDEDUCTION_Weekly);
		else if(frequency >= 14 && frequency <= 16)
			getTab().setValue(MHRLoan.COLUMNNAME_FrequencyDeduction, MHRLoan.FREQUENCYDEDUCTION_Biweekly);
		else if(frequency >= 28 && frequency <=31)
			getTab().setValue(MHRLoan.COLUMNNAME_FrequencyDeduction, MHRLoan.FREQUENCYDEDUCTION_Monthly);
		else if(frequency > 31) {
			String frequencyDeduction = String.format("%03d", frequency);
			getTab().setValue(MHRLoan.COLUMNNAME_FrequencyDeduction, frequencyDeduction);
		}
		
		return null;
	}

}
