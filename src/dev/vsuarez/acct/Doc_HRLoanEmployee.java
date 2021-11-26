/**
 * 
 */
package dev.vsuarez.acct;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;

import org.compiere.acct.Doc;
import org.compiere.acct.Fact;
import org.compiere.acct.FactLine;
import org.compiere.model.MAccount;
import org.compiere.model.MAcctSchema;
import org.compiere.model.MCharge;
import org.compiere.util.DB;
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
		BigDecimal retValue = Env.ZERO;
		return retValue; 
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
		if(m_loan.getC_Charge_ID() <=0)
			return null;
		Fact fact = new Fact(this, as, Fact.POST_Actual);
		MAccount acct = MCharge.getAccount(m_loan.getC_Charge_ID(), as);
		FactLine fl = fact.createLine(null, acct, m_loan.getC_Currency_ID(), m_loan.getAmt(), null);
		
		if(fl != null) {
			fl.setAD_Org_ID(m_loan.getAD_Org_ID());
			fl.setC_BPartner_ID(m_loan.getC_BPartner_ID());
		}
		acct = getAccountBP(as);
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
	
	/**
	 *	Get the account for Accounting Schema of Employee
	 *  @param AcctType see ACCTTYPE_*
	 *  @param as accounting schema
	 *  @return Account BP
	 */
	public MAccount getAccountBP(MAcctSchema as) {
		int C_ValidCombination_ID = getValidCombination_ID(as);
		if (C_ValidCombination_ID == 0)
			return null;
		//	Return Account
		MAccount acct = MAccount.get (as.getCtx(), C_ValidCombination_ID);
		return acct;
	}	//	getAccountBP

	public int getValidCombination_ID(MAcctSchema as) {
		String sql = "SELECT E_Prepayment_Acct FROM C_BP_Employee_Acct WHERE C_BPartner_ID=? AND C_AcctSchema_ID=?";
		//  Get Acct
		int Account_ID = 0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = DB.prepareStatement(sql, null);
			pstmt.setInt (1, getC_BPartner_ID());
			pstmt.setInt (2, as.getC_AcctSchema_ID());
			rs = pstmt.executeQuery();
			if (rs.next())
				Account_ID = rs.getInt(1);
		}
		catch (SQLException e) {
			log.log(Level.SEVERE, "Error - SQL=" + sql, e);
			return 0;
		}
		finally {
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}
		//	No account
		if (Account_ID == 0) {
			log.warning("NO account, Record=" + p_po.get_ID());
			return 0;
		}
		return Account_ID;
	}

}
