package com.ingeint.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Properties;

public class MHRLoanLines extends X_HR_LoanLines {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7896971551329826595L;

	public MHRLoanLines(Properties ctx, int HR_LoanLines_ID, String trxName) {
		super(ctx, HR_LoanLines_ID, trxName);
		// TODO Auto-generated constructor stub
	}

	public MHRLoanLines(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}
	
	public MHRLoanLines (MHRLoan loan) {
		this (loan.getCtx(), 0, loan.get_TrxName());
		if (loan.get_ID()==0)
			throw new IllegalArgumentException("Header not saved");
		setHR_Loan_ID(loan.get_ID()); //parent
		setLoan(loan);
	}
	
	/**
	 * 	Set Defaults from Order.
	 * 	Does not set Parent !!
	 * 	@param order order
	 */
	public void setLoan (MHRLoan loan)
	{
		setClientOrg(loan);
		
	}	//	setLoan

	@Override
	protected boolean afterSave(boolean newRecord, boolean success) {
		if(!newRecord && is_ValueChanged(COLUMNNAME_IsPaid)) {
			MHRLoan loan = (MHRLoan) getHR_Loan();
			BigDecimal amount = loan.getOpenAmt();
			if(isPaid())
				amount = amount.subtract(getAmt());
			else
				amount = amount.add(getAmt());
			if(amount.signum() > 0) {
				loan.setOpenAmt(amount);
				loan.saveEx();
			}
		}
		
		return super.afterSave(newRecord, success);
	}
	
	/*
	 * public MOrderLine (MOrder order)
	{
		this (order.getCtx(), 0, order.get_TrxName());
		if (order.get_ID() == 0)
			throw new IllegalArgumentException("Header not saved");
		setC_Order_ID (order.getC_Order_ID());	//	parent
		setOrder(order);
	}	//	MOrderLine

	 */
	
}
