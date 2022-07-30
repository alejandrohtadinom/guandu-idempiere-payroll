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
package org.compiere.acct;

import java.math.BigDecimal;

import org.compiere.model.MBPartner;
import org.compiere.util.Env;
import org.eevolution.model.MHRConcept;
import org.eevolution.model.MHRMovement;

import com.ingeint.model.MINGMovement;

/**
 * Payroll Line
 *
 * @author Jorg Janke
 * @version $Id: DocLine_Payroll.java,v 1.4 2005/10/17 23:43:52 jjanke Exp $
 */
public class DocLine_Payroll extends DocLine {
	/**
	 * Constructor
	 * 
	 * @param line Payroll line
	 * @param doc  header
	 */
	public DocLine_Payroll(MHRMovement line, Doc_HRProcess doc) {
		super(line, doc);
		int C_BPartner_ID = line.getC_BPartner_ID();
		MBPartner bpartner = new MBPartner(Env.getCtx(), C_BPartner_ID, null);
		MHRConcept concept = MHRConcept.get(Env.getCtx(), line.getHR_Concept_ID());
		//
		m_HR_Concept_ID = concept.getHR_Concept_ID();
		m_HR_Process_ID = line.getHR_Process_ID();
		m_C_BPartner_ID = C_BPartner_ID;
		m_HR_Department_ID = line.getHR_Department_ID();
		m_C_Activity_ID = line.getC_Activity_ID();
		m_User1_ID = line.getUser1_ID();
		m_C_BP_Group_ID = bpartner.getC_BP_Group_ID();
		m_AccountSign = concept.getAccountSign();
		m_Amount = line.getAmount();
		setAmount(line.getAmount());
		m_HR_Job_ID = line.getHR_Job_ID();
		m_C_Currency_ID = line.getC_Currency_ID();
		setC_ConversionType_ID(line.getC_ConversionType_ID());
	} // DocLine_Payroll

	public DocLine_Payroll(MINGMovement line, Doc_HRProcess doc) {
		super(line, doc);
		MHRConcept concept = MHRConcept.get(Env.getCtx(), line.getHR_Concept_ID());
		//
		m_HR_Concept_ID = concept.getHR_Concept_ID();
		m_HR_Process_ID = line.getHR_Process_ID();
		m_AccountSign = concept.getAccountSign();
		m_Amount = line.getAmount();
		m_User1_ID = line.getUser1_ID();
		m_C_Activity_ID = line.getC_Activity_ID();
		m_Amount = line.getAmount();
		BigDecimal m_CumulatedAmt = BigDecimal.ZERO;
		if(line.get_Value("ConvertedAmt") != null)
			m_CumulatedAmt = (BigDecimal) line.get_Value("ConvertedAmt");
		m_C_Currency_ID = line.getC_Currency_ID();
		m_C_Conversion_Rate_ID = line.getC_Conversion_Rate_ID();
		setAmount(m_Amount);
		setM_CumulatedAmt(m_CumulatedAmt);
	}

	// References
	private int m_HR_Process_ID = 0;
	private int m_HR_Concept_ID = 0;
	private int m_C_BPartner_ID = 0;
	private int m_C_Activity_ID = 0;
	private String m_AccountSign = "";
	private BigDecimal m_Amount = Env.ZERO;
	private int m_HR_Department_ID = 0;
	private int m_C_BP_Group_ID = 0;
	private int m_User1_ID = 0;
	private BigDecimal m_CumulatedAmt = Env.ZERO;
	private int m_C_Currency_ID = 0;
	private int m_C_Conversion_Rate_ID = 0;
	private int m_HR_Job_ID = 0;

	public int getHR_Process_ID() {
		return m_HR_Process_ID;
	}

	public int getHR_Concept_ID() {
		return m_HR_Concept_ID;
	}

	public String getAccountSign() {
		return m_AccountSign;
	}

	public int getC_BPartner_ID() {
		return m_C_BPartner_ID;
	}

	public int getC_Activity_ID() {
		return m_C_Activity_ID;
	}

	public BigDecimal getAmount() {
		return m_Amount;
	}

	public int getHR_Department_ID() {
		return m_HR_Department_ID;
	}

	public int getC_BP_Group_ID() {
		return m_C_BP_Group_ID;
	}

	public int getUser1_ID() {
		return m_User1_ID;
	}

	public BigDecimal getM_CumulatedAmt() {
		return m_CumulatedAmt;
	}

	public void setM_CumulatedAmt(BigDecimal m_CumulatedAmt) {
		this.m_CumulatedAmt = m_CumulatedAmt;
	}

	public int getM_C_Currency_ID() {
		return m_C_Currency_ID;
	}

	public void setM_C_Currency_ID(int m_C_Currency_ID) {
		this.m_C_Currency_ID = m_C_Currency_ID;
	}

	public int getM_C_Conversion_Rate_ID() {
		return m_C_Conversion_Rate_ID;
	}

	public void setM_C_Conversion_Rate_ID(int m_C_Conversion_Rate_ID) {
		this.m_C_Conversion_Rate_ID = m_C_Conversion_Rate_ID;
	}
	
	public int getHR_Job_ID() {
		return m_HR_Job_ID;
	}

	public void setHR_Job_ID(int HR_Job_ID) {
		this.m_HR_Job_ID = HR_Job_ID;
	}

	
} // DocLine_Payroll
