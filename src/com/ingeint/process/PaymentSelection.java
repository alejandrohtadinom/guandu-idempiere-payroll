package com.ingeint.process;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.eevolution.model.MHRMovement;
import org.eevolution.model.MHRPayroll;

import com.ingeint.base.CustomProcess;
import com.ingeint.model.MHRPaymentSelection;
import com.ingeint.model.MHRPaymentSelectionLine;
import com.ingeint.model.X_ING_PaymentSelectionType;

public class PaymentSelection extends CustomProcess {
	
	BigDecimal p_Percent = Env.ONE;	
	
	@Override
	protected void prepare() {
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++) {
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("Percent"))
				p_Percent = para[i].getParameterAsBigDecimal().divide(Env.ONEHUNDRED, 2, RoundingMode.HALF_UP);
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
	}

	@Override
	protected String doIt() throws Exception {
		MHRPaymentSelection ps = new MHRPaymentSelection(getCtx(), getRecord_ID(), get_TrxName());

		StringBuilder deleteLines = new StringBuilder("DELETE FROM HR_PaymentSelectionLine WHERE HR_PaymentSelection_ID = ? ");
		List<Object> params = new ArrayList<>();
		params.add(ps.getHR_PaymentSelection_ID());
		if (ps.getC_BPartner_ID() > 0) {
			deleteLines.append(" AND C_BPartner_ID=? ");
			params.add(ps.getC_BPartner_ID());
		} else if (ps.getHR_Job_ID() > 0) {
			deleteLines.append(" AND HR_Job_ID=? ");
			params.add(ps.getHR_Job_ID());
		} else if (ps.getHR_Department_ID() > 0) {
			deleteLines.append(" AND HR_Department_ID=? ");
			params.add(ps.getHR_Department_ID());
		} else if (ps.getEmployeeGroup() != null) {
			deleteLines.append(" AND EmployeeGroup=? ");
			params.add(ps.getEmployeeGroup());
		}
		DB.executeUpdate(deleteLines.toString(), params.toArray(), true, get_TrxName());
		
		MHRPayroll payroll = new MHRPayroll(null, ps.getHR_Process().getHR_Payroll_ID(), get_TrxName());
		String accountSign = null;
		boolean isPrinted = false;
		int HR_Concept_ID = 0;
		X_ING_PaymentSelectionType psType = null;
		if(ps.getING_PaymentSelectionType_ID() > 0) {
			psType = (X_ING_PaymentSelectionType) ps.getING_PaymentSelectionType();
			HR_Concept_ID = psType.getHR_Concept_ID();
			if(psType.getGroupMovementsBy() != null) {
				isPrinted = true;
				if(X_ING_PaymentSelectionType.GROUPMOVEMENTSBY_Assignments.equals(psType.getGroupMovementsBy()))
					accountSign = "'D'";
				else if(X_ING_PaymentSelectionType.GROUPMOVEMENTSBY_Deductions.equals(psType.getGroupMovementsBy()))
					accountSign = "'C'";
				else if(X_ING_PaymentSelectionType.GROUPMOVEMENTSBY_TotalToPay.equals(psType.getGroupMovementsBy()))
					accountSign = "'C','D'";
			}
		} else
			HR_Concept_ID = payroll.get_ValueAsInt("PaymentConcept_ID");
					
		MHRMovement[] movement = getMovement(ps.getHR_Process_ID(), HR_Concept_ID, ps.getC_BPartner_ID(), ps.getHR_Job_ID(), ps.getHR_Department_ID(), 
				ps.getEmployeeGroup(), accountSign, isPrinted);
		if(isPrinted)
			return createGroupedSelectionLine(movement, ps);
		else
			return createSelectionLine(movement, ps);
	}

	/**
	 * Get Array of Movements
	 * @param HR_Process_ID
	 * @param HR_Concept_ID
	 * @param C_BPartner_ID
	 * @param HR_Job_ID
	 * @param HR_Department_ID
	 * @param employeeGroup
	 * @param accountSign
	 * @param isPrinted
	 * @return Array of Movements
	 */
	public MHRMovement[] getMovement(Integer HR_Process_ID, Integer HR_Concept_ID, Integer C_BPartner_ID,
			Integer HR_Job_ID, Integer HR_Department_ID, String employeeGroup, String accountSign, boolean isPrinted) {
		StringBuilder whereClauseFinal = new StringBuilder(MHRMovement.COLUMNNAME_HR_Process_ID + "=? ");
		List<Object> params = new ArrayList<>();
		params.add(HR_Process_ID);
	
		if(HR_Concept_ID > 0) {
			whereClauseFinal.append("AND " + MHRMovement.COLUMNNAME_HR_Concept_ID + "=? ");
			params.add(HR_Concept_ID);
		}
		if (C_BPartner_ID > 0) {
			whereClauseFinal.append("AND C_BPartner_ID =? ");
			params.add(C_BPartner_ID);
		}
		if (HR_Job_ID > 0) {
			whereClauseFinal.append("AND HR_Job_ID =? ");
			params.add(HR_Job_ID);
		}
		if (HR_Department_ID > 0) {
			whereClauseFinal.append("AND HR_Department_ID =? ");
			params.add(HR_Department_ID);
		}
		if (employeeGroup != null) {
			whereClauseFinal.append("AND EmployeeGroup =? ");
			params.add(employeeGroup);
		}
		if(accountSign != null) {
			whereClauseFinal.append("AND AccountSign IN (" + accountSign + ") ");
		}
		if(isPrinted) {
			whereClauseFinal.append("AND IsPrinted =? ");
			params.add(isPrinted);
		}
		
		List<MHRMovement> list = new Query(getCtx(), MHRMovement.Table_Name, whereClauseFinal.toString(), get_TrxName())
				.setParameters(params).setOrderBy("C_BPartner_ID").list();

		return list.toArray(new MHRMovement[list.size()]);
	}
	
	/**
	 * Create Grouped Selection Line
	 * @param movement Array of Movements
	 * @param ps Payroll Payment Selection
	 * @return NULL when no Error
	 */
	private String createGroupedSelectionLine(MHRMovement[] movements, MHRPaymentSelection ps) {
		int current_BPartner_ID = 0;
		MHRMovement currentMovement = null;
		String errorMsg = null;
		BigDecimal credit = BigDecimal.ZERO;
		BigDecimal debit = BigDecimal.ZERO;
		BigDecimal totalToPay = BigDecimal.ZERO;
		
		try {
			for(MHRMovement movement : movements) {
				if(!MHRMovement.COLUMNTYPE_Amount.equals(movement.getColumnType()))
					continue;
				credit = BigDecimal.ZERO;
				debit = BigDecimal.ZERO;
				if(current_BPartner_ID != movement.getC_BPartner_ID()) {
					if(current_BPartner_ID > 0) {
						errorMsg = createSelectionLine(currentMovement, ps, totalToPay);
						if(errorMsg != null)
							return errorMsg;
					}
					current_BPartner_ID = movement.getC_BPartner_ID();
					currentMovement = movement;
					totalToPay = BigDecimal.ZERO;
				}
				if(MHRMovement.ACCOUNTSIGN_Credit.equals(movement.getAccountSign()))
					credit = movement.getAmount();
				else if(MHRMovement.ACCOUNTSIGN_Debit.equals(movement.getAccountSign()))
					debit = movement.getAmount();
				totalToPay = totalToPay.add(debit.subtract(credit));
			}
			if(currentMovement != null)
				errorMsg = createSelectionLine(currentMovement, ps, totalToPay);
				if(errorMsg != null)
					return errorMsg;
		} catch (Exception e) {
			return "@Error@: Creando Lineas de Seleccion - " + e.getLocalizedMessage();
		}
		return null;
	}
	
	/**
	 * Create One Selection Line
	 * @param currentMovement One Movement of Employee for data
	 * @param ps Payroll Payment Selection
	 * @param totalToPay Total Amount to Pay
	 * @return
	 */
	private String createSelectionLine(MHRMovement currentMovement, MHRPaymentSelection ps, BigDecimal totalToPay) {
		MathContext mc = new MathContext(6); // 6 precision
		MHRPaymentSelectionLine psLine = new MHRPaymentSelectionLine(ps);
		psLine.setC_BPartner_ID(currentMovement.getC_BPartner_ID());
		psLine.setAmount(totalToPay);
		psLine.setHR_Department_ID(currentMovement.getHR_Department_ID());
		psLine.setHR_Job_ID(currentMovement.getHR_Job_ID());
		psLine.setEmployeeGroup(ps.getEmployeeGroup());
		psLine.setPayAmt(totalToPay.multiply(p_Percent).round(mc));
		psLine.setOpenAmt(totalToPay.subtract(psLine.getPayAmt()));
		psLine.setC_BP_BankAccount_ID(currentMovement.getC_BP_BankAccount_ID());
		if(!psLine.save())
			return "@Error@: No se pudo guardar Linea de Seleccion de Pago, para Empleado: " + currentMovement.getC_BPartner().getName();
		
		return null;
	}

	/**
	 * Create Selection Line
	 * @param movement Array of Movements
	 * @param ps Payroll Payment Selection
	 * @return NULL when no Error
	 */
	private String createSelectionLine(MHRMovement[] movement, MHRPaymentSelection ps) {
		MathContext mc = new MathContext(6); // 4 precision
		try {
			for (MHRMovement move : movement) {
				MHRPaymentSelectionLine psline = new MHRPaymentSelectionLine(ps);
				psline.setC_BPartner_ID(move.getC_BPartner_ID());
				psline.setAmount(move.getAmount());
				psline.setHR_Department_ID(move.getHR_Department_ID());
				psline.setHR_Job_ID(move.getHR_Job_ID());
				psline.setEmployeeGroup(ps.getEmployeeGroup());
				psline.setPayAmt(move.getAmount().multiply(p_Percent).round(mc));
				psline.setOpenAmt(move.getAmount().subtract(psline.getPayAmt()));
				psline.setHR_Movement_ID(move.getHR_Movement_ID());
				psline.setC_BP_BankAccount_ID(move.getC_BP_BankAccount_ID());
				if(!psline.save())
					return "@Error@: No se pudo guardar Linea de Seleccion de Pago, para Empleado: " + move.getC_BPartner().getName();
			}
		} catch (Exception e) {
			return "@Error@: Creando Lineas de Seleccion - " + e.getLocalizedMessage();
		}
		return null;
	}
}