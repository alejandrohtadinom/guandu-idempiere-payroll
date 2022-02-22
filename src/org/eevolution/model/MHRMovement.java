/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * Copyright (C) 2003-2007 e-Evolution,SC. All Rights Reserved.               *
 * Contributor(s): Victor Perez www.e-evolution.com                           *
 *****************************************************************************/
package org.eevolution.model;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Properties;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.I_C_BP_BankAccount;
import org.compiere.model.MAcctSchema;
import org.compiere.model.MClientInfo;
import org.compiere.model.MConversionRate;
import org.compiere.model.MCurrency;
import org.compiere.model.Query;
import org.compiere.util.Env;
import org.compiere.util.Util;

/**
 *	Payroll Concept for HRPayroll Module
 *	
 *  @author Oscar Gómez Islas
 *  @author Teo Sarca, www.arhipac.ro
 */
public class MHRMovement extends X_HR_Movement
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -9074136731316014532L;

	/**
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param HR_Concept_ID
	 *	@param trxName
	 */
	public MHRMovement (Properties ctx, int HR_Movement_ID, String trxName)
	{
		super (ctx, HR_Movement_ID, trxName);
	}

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 *	@param trxName
	 */
	public MHRMovement (Properties ctx, ResultSet rs, String trxName)
	{
		super(ctx, rs, trxName);
	}
	
	/**
	 * 	Import Constructor
	 *	@param impHRm import
	 */
	public MHRMovement (X_I_HR_Movement impHRm)
	{
		this (impHRm.getCtx(), 0, impHRm.get_TrxName());

		MHRConcept hrconcept = new MHRConcept(getCtx(), impHRm.getHR_Concept_ID(), get_TrxName());
		MHREmployee employee  = MHREmployee.getActiveEmployee(getCtx(), impHRm.getC_BPartner_ID(), get_TrxName());
		MHRProcess process = new MHRProcess(getCtx(), impHRm.getHR_Process_ID(), get_TrxName());
		
		setAD_Org_ID(process.getAD_Org_ID());
		setUpdatedBy(impHRm.getUpdatedBy());
		//
		setHR_Process_ID(impHRm.getHR_Process_ID());
		setC_BPartner_ID(impHRm.getC_BPartner_ID());
		setHR_Concept_ID(impHRm.getHR_Concept_ID());

		setHR_Concept_Category_ID(hrconcept.getHR_Concept_Category_ID());
		setDescription(impHRm.getDescription());
		
		setHR_Job_ID(employee.getHR_Job_ID());
		setHR_Department_ID(employee.getHR_Department_ID());
		setC_Activity_ID(employee.getC_Activity_ID());
		setColumnType(hrconcept.getColumnType());
		setValidFrom(impHRm.getValidFrom());
		setIsRegistered(hrconcept.isRegistered());
		setIsPrinted(hrconcept.isPrinted());

		// set corresponding values
		setAmount(null);
		setQty(null);
		setServiceDate(null);
		setTextMsg(null);
		if (hrconcept.getColumnType().equals(MHRConcept.COLUMNTYPE_Quantity)){				// Concept Type
			setQty(impHRm.getQty());
		} else if (hrconcept.getColumnType().equals(MHRConcept.COLUMNTYPE_Amount)){
			setAmount(impHRm.getAmount());
		} else if (hrconcept.getColumnType().equals(MHRConcept.COLUMNTYPE_Date)){
			setServiceDate(impHRm.getServiceDate());
		} else if (hrconcept.getColumnType().equals(MHRConcept.COLUMNTYPE_Text)){
			setTextMsg(impHRm.getTextMsg());
		}
	}	//	MHRMovement

	public MHRMovement (MHRProcess proc, I_HR_Concept concept)
	{
		this(proc.getCtx(), 0, proc.get_TrxName());
		// Process
		this.setHR_Process_ID(proc.getHR_Process_ID());
		// Concept
		this.setHR_Concept_Category_ID(concept.getHR_Concept_Category_ID());
		this.setHR_Concept_ID(concept.getHR_Concept_ID());
		this.setColumnType(concept.getColumnType());
		this.setAccountSign(concept.getAccountSign());
	}
	
	public void addAmount(BigDecimal amount)
	{
		setAmount(getAmount().add(amount == null ? Env.ZERO : amount));
	}
	
	public void addQty(BigDecimal qty)
	{
		setQty(getAmount().add(qty == null ? Env.ZERO : qty));
	}
	
	/**
	 * @return true if all movement values (Amount, Qty, Text) are empty 
	 */
	public boolean isEmpty()
	{
		return getQty().signum() == 0
				&& getAmount().signum() == 0
				&& Util.isEmpty(getTextMsg())
				&& getServiceDate() == null;
	}
	
	/**
	 * According to the concept type, it's saved in the column specified for the purpose
	 * @param columnType column type (see MHRConcept.COLUMNTYPE_*)
	 * @param value
	 */
	public void setColumnValue(Object value) {
		if(value == null)
			return;
		try {
			final String columnType = getColumnType();
			if (MHRConcept.COLUMNTYPE_Quantity.equals(columnType)) {
				BigDecimal qty = new BigDecimal(value.toString()); 
				setQty(qty);
				setAmount(Env.ZERO);
			} else if(MHRConcept.COLUMNTYPE_Amount.equals(columnType)) {
				MHRProcess process = new MHRProcess(p_ctx, getHR_Process_ID(), get_TrxName());
				MHRPayroll pa = new MHRPayroll(p_ctx, process.getHR_Payroll_ID(), get_TrxName());
				int precision = pa.get_ValueAsInt("StdPrecision");
				BigDecimal amount = new BigDecimal(value.toString()).setScale(precision, RoundingMode.HALF_UP);
				setAmount(amount);
				setQty(Env.ZERO);
			} 
			else if(MHRConcept.COLUMNTYPE_Text.equals(columnType))
			{
				setTextMsg(value.toString().trim());
			}
			else if(MHRConcept.COLUMNTYPE_Date.equals(columnType))
			{
				if (value instanceof Timestamp)
				{
					setServiceDate((Timestamp)value);
				}
				else
				{
					setServiceDate(Timestamp.valueOf(value.toString().trim().substring(0, 10)+ " 00:00:00.0"));	
				}
			}
			else
			{
				throw new AdempiereException("@NotSupported@ @ColumnType@ - "+columnType);
			}
		}
		catch (Exception e) 
		{
			throw new AdempiereException("@Script Error@ " + e.getLocalizedMessage());
		}
	}
	
	@Override
	protected boolean beforeSave(boolean newRecord)
	{
		MHREmployee employee  = MHREmployee.getActiveEmployee(Env.getCtx(), getC_BPartner_ID(),getAD_Org_ID(), get_TrxName());
		if (employee != null) {
			setAD_Org_ID(employee.getAD_Org_ID());
		}
		setC_BP_Group_ID(getC_BPartner().getC_BP_Group_ID());
		// BankAccount
		int C_BP_BankAccount_ID = new Query(getCtx(), I_C_BP_BankAccount.Table_Name, COLUMNNAME_C_BPartner_ID+"=?", get_TrxName())
			.setOnlyActiveRecords(true)
			.setParameters(getC_BPartner_ID())
			.setOrderBy(I_C_BP_BankAccount.COLUMNNAME_C_BP_BankAccount_ID+" DESC") // just in case...
			.firstId();
		
		if(C_BP_BankAccount_ID > 0)
			setC_BP_BankAccount_ID(C_BP_BankAccount_ID);
			
		setAccountSign(getHR_Concept().getAccountSign());

		return true;
	}
	
	/**
	 * Get Converted Amount
	 * 
	 * @param BigDecimal amount
	 * @param int Currency
	 * @return Converted Amount
	 */
	public BigDecimal getConvertedAmount(BigDecimal amount, int from_C_Currency_ID) {
		MHRProcess process = new MHRProcess(p_ctx, getHR_Process_ID(), get_TrxName());
		MHRPayroll pa = new MHRPayroll(p_ctx, process.getHR_Payroll_ID(), get_TrxName());
		if(from_C_Currency_ID <=0)
			from_C_Currency_ID = pa.get_ValueAsInt("C_Currency_ID");
		int prCurrencyID = process.getC_Currency_ID();
		BigDecimal convAmt = BigDecimal.ZERO;
		if(from_C_Currency_ID <= 0 || from_C_Currency_ID == prCurrencyID)
			convAmt = amount;
		else {
			BigDecimal currencyRate = BigDecimal.ZERO;
			if(process.get_Value("CurrencyRate") != null)
				currencyRate = (BigDecimal) process.get_Value("CurrencyRate");
			if(!process.get_ValueAsBoolean("IsOverrideCurrencyRate") || currencyRate.signum() <= 0) {
				convAmt = MConversionRate.convert(getCtx(), amount, from_C_Currency_ID, prCurrencyID, process.getDateAcct(), 
						process.get_ValueAsInt("C_ConversionType_ID"), getAD_Client_ID(), getAD_Org_ID());
			} else {
				MClientInfo info = MClientInfo.get(getAD_Client_ID());
				MAcctSchema invAcctShema = MAcctSchema.get(info.getC_AcctSchema1_ID());
				BigDecimal divideRate = BigDecimal.ZERO;
				if(process.get_Value("DivideRate") != null)
					divideRate = (BigDecimal) process.get_Value("DivideRate");
				if(divideRate.signum() > 0) {
					if(prCurrencyID == invAcctShema.getC_Currency_ID())
						convAmt = amount.divide(divideRate, MCurrency.getStdPrecision(getCtx(), prCurrencyID), RoundingMode.HALF_UP);
					else
						convAmt = amount.multiply(divideRate)
							.setScale(MCurrency.getStdPrecision(getCtx(), prCurrencyID), RoundingMode.HALF_UP);
				} else
					convAmt = amount.multiply(currencyRate)
						.setScale(MCurrency.getStdPrecision(getCtx(), prCurrencyID), RoundingMode.HALF_UP);
			}
		}
		return convAmt;
	}
	
	int m_C_Currency_ID = 0;
	
	/**
	 * Currency of Attribute
	 * @return the m_C_Currency_ID
	 */
	public int getC_Currency_ID() {
		return m_C_Currency_ID;
	}

	/**
	 * Currency of Attribute
	 * @param m_C_Currency_ID the m_C_Currency_ID to set
	 */
	public void setC_Currency_ID(int C_Currency_ID) {
		this.m_C_Currency_ID = C_Currency_ID;
	}

	@Override
	public void setAmount(BigDecimal amount) {
		super.setAmount(getConvertedAmount(amount, getC_Currency_ID()));
	}
	
	
}	//	HRMovement