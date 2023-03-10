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
 * Contributor(s): Alejandro H Tadino M tadinomalejandroh@gmail.com           *
 *****************************************************************************/

package com.andetek.utils;

import java.math.BigDecimal;
import java.sql.Timestamp;

import org.compiere.model.MPeriod;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;

import com.ingeint.model.MHRPaymentSelection;

public class HR_R_BANAVIH extends SvrProcess {
	
	public int p_AD_Org_ID = 0;
	public int p_HR_Department_ID = 0;
	public int p_HR_Contract_ID = 0;
	public int p_HR_Process_ID = 0;
	public Timestamp p_DateAcct_From = null;
	public Timestamp p_DateAcct_To = null;

	public StringBuffer m_parameterWhere = new StringBuffer();
	public StringBuffer m_parameterGroupBy = new StringBuffer();
	public StringBuffer m_parameterOrderBy = new StringBuffer();
	
	protected void prepare() {
		StringBuilder sb = new StringBuilder("Record_ID=").append(getRecord_ID());
		
		ProcessInfoParameter[] param = getParameter();

		// Atraviesa el arreglo de parametros y los asigna a las variables
		for (ProcessInfoParameter p : param) {
			String name = p.getParameterName();

			// Asigna la  organizacion
			if (name.equals("AD_Org_ID")) {
				p_AD_Org_ID = ((BigDecimal) p.getParameter()).intValue();

			// Asigna el departamento
			} else if (name.equals("HR_Department_ID")) {
				p_HR_Department_ID = ((BigDecimal) p.getParameter()).intValue();

			// Asigna el contrato
			} else if (name.equals("HR_Contract_ID")) {
				p_HR_Contract_ID = ((BigDecimal) p.getParameter()).intValue();

			// Asigna el proceso de nomina
			} else if (name.equals("HR_Process_ID")) {
				p_HR_Process_ID = ((BigDecimal) p.getParameter()).intValue();

			// Asigna la fecha de inicio
			} else if (name.equals("DateAcct_From")) {
				p_DateAcct_From = ((Timestamp) p.getParameter());

			// Asigna la fecha de fin
			} else if (name.equals("DateAcct_To")) {
				p_DateAcct_To = ((Timestamp) p.getParameter());
			}
			
			m_parameterWhere.append("");
		}
	}

	// TODO: insertar datos en la tabla temporal para luego ser procesados
	protected String doIt() throws Exception {
		StringBuilder sb = new StringBuilder("INSERT INTO LVE_RV_HR_BANAVIH (BP_Nationality, BP_TaxID, BP_Name, BP_Name2, BP_LastName1, BP_LastName2, Amt, StartDate, EndDate) ");
		sb.append("SELECT SUBSTR(cbp.TaxID, 1, 1), SUBSTR(cbp.TaxID, 2), cbp.Name, cbp.Name2, cbp.LastName1, cbp.LastName2, pl.Amt, e.StartDate, e.EndDate "
				+ "FROM HR_PaymentSelectionLine pl "
				+ "INNER JOIN HR_PaymentSelection ps ON pl.HR_PaymentSelection_ID = ps.HR_PaymentSelection_ID "
				+ "INNER JOIN HR_Contract c ON ps.HR_Contract_ID = c.HR_Contract_ID "
				+ "INNER JOIN HR_Employee e ON c.HR_Employee_ID = e.HR_Employee_ID "
				+ "INNER JOIN C_BPartner cbp ON e.C_BPartner_ID = cbp.C_BPartner_ID "
				+ "INNER JOIN HR_Process p ON ps.HR_Process_ID = p.HR_Process_ID ");

		setWhere();
		setOrderBy();
		setGroupBy();

		sb.append(m_parameterWhere).append(m_parameterGroupBy).append(m_parameterOrderBy);

		int no = DB.executeUpdate(sb.toString(), get_TrxName());
		if (no > 0) {
			return "Se insertaron " + no + " registros";
		} else {
			return "No se insertaron registros";
		}

		log.fine("Se insertaron " + no + " registros");
		log.finest(sb.toString());
	}

	public void setWhere() {
		StringBuilder sb = new StringBuilder(" WHERE 1=1 ");
		if (p_AD_Org_ID > 0) {
			sb.append(" AND p.AD_Org_ID = ").append(p_AD_Org_ID);
		}
		if (p_HR_Department_ID > 0) {
			sb.append(" AND p.HR_Department_ID = ").append(p_HR_Department_ID);
		}
		if (p_HR_Contract_ID > 0) {
			sb.append(" AND p.HR_Contract_ID = ").append(p_HR_Contract_ID);
		}
		if (p_HR_Process_ID > 0) {
			sb.append(" AND p.HR_Process_ID = ").append(p_HR_Process_ID);
		}
		/*
		 * Valida las fechas de las transacciones
		 */
		if (p_DateAcct_From != null) {
			sb.append(" AND p.DateAcct >= '").append(p_DateAcct_From);
		} else if (p_DateAcct_To != null) {
			sb.append(" AND p.DateAcct BETWEEN '").append(p_DateAcct_From).append("' AND '").append(p_DateAcct_To).append("'");
		}

		m_parameterWhere.append(sb.toString());
	}

	public void setGroupBy() {
		StringBuilder sb = new StringBuilder(" GROUP BY cbp.TaxID, cbp.Name, cbp.Name2, cbp.LastName1, cbp.LastName2, pl.Amt, e.StartDate, e.EndDate");
		m_parameterGroupBy.append(sb.toString());
	}

	public void setOrderBy() {
		StringBuilder sb = new StringBuilder(" ORDER BY cbp.TaxID, cbp.Name, cbp.Name2, cbp.LastName1, cbp.LastName2, pl.Amt, e.StartDate, e.EndDate");
		m_parameterOrderBy.append(sb.toString());
	}
}
