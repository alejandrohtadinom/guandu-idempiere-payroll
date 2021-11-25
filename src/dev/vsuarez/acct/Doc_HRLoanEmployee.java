/**
 * 
 */
package dev.vsuarez.acct;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.compiere.acct.Doc;
import org.compiere.acct.Fact;
import org.compiere.acct.FactLine;
import org.compiere.model.MAccount;
import org.compiere.model.MAcctSchema;
import org.compiere.model.MBPartner;
import org.compiere.model.MCharge;
import org.compiere.util.Env;

import com.ingeint.model.MHRLoan;

/**
 *  Post Loan employee Documents.
 *  <pre>
 *  Table:              HR_Loan
 *  Document Types:     PLO
 *  </pre>
 * @author <a href="mailto:victor.suarez.is@gmail.com">Ing. Victor Suarez</a>
 *
 */
public class Doc_HRLoanEmployee extends Doc {
	
	/**		Loan		**/
	public MHRLoan m_loan = null;
	/** Document Type Loan **/
	public static final String	DOCTYPE_Loan = "PLO";
	
	

	/**
	 * @param as
	 * @param clazz
	 * @param rs
	 * @param defaultDocumentType
	 * @param trxName
	 */
	public Doc_HRLoanEmployee(MAcctSchema as, Class<?> clazz, ResultSet rs, String defaultDocumentType, String trxName) {
		super(as, clazz, rs, defaultDocumentType, trxName);
	}

	/**
	 * 
	 * @param as
	 * @param rs
	 * @param trxName
	 */
	public Doc_HRLoanEmployee(MAcctSchema as, ResultSet rs, String trxName) {
		super(as, MHRLoan.class, rs, null, trxName);
	}

	@Override
	protected String loadDocumentDetails() {
		m_loan = (MHRLoan) getPO();
		setDateDoc(getDateAcct());
		return null;
	}

	@Override
	public BigDecimal getBalance() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Create Facts (the accounting logic) for Loans
	 * <pre>
	 *  - Charge Account	DR
	 *  - Employee Account for Loan	CR
	 * </pre>
	 * @param as Accounting Schema
	 * @return Facts
	 */
	@Override
	public ArrayList<Fact> createFacts(MAcctSchema as) {
		Fact fact = new Fact(this, as, Fact.POST_Actual);
		
		MAccount acct = MCharge.getAccount(m_loan.getC_Charge_ID(), as);
		FactLine fl = fact.createLine(null, acct, m_loan.getC_Currency_ID(), m_loan.getAmt(), null);
		if(fl != null) {
			fl.setAD_Org_ID(m_loan.getAD_Org_ID());
			fl.setC_BPartner_ID(m_loan.getC_BPartner_ID());
		}
		//acct = getAccount(Doc., as)
		fl = fact.createLine(null, acct, m_loan.getC_Currency_ID(), null, m_loan.getAmt());
		if(fl != null) {
			fl.setAD_Org_ID(m_loan.getAD_Org_ID());
			fl.setC_BPartner_ID(m_loan.getC_BPartner_ID());
		}
		//
		ArrayList<Fact> facts = new ArrayList<Fact>();
		facts.add(fact);
		return facts;
	} 	//  createFact

}
