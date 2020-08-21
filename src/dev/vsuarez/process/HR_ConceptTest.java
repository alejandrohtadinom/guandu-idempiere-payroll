/**
 * 
 */
package dev.vsuarez.process;

import java.util.logging.Level;

import org.compiere.process.ProcessInfoParameter;

import com.ingeint.base.CustomProcess;

import ve.net.dcs.process.MHRProcess_ConceptTest;

/**
 * @author <a href="mailto:victor.suarez.is@gmail.com">Ing. Victor Suarez</a>
 *
 */
public class HR_ConceptTest extends CustomProcess {
	
	public int p_HR_Process_ID = 0;

	@Override
	protected void prepare() {
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null);
			else if (name.equals("HR_Process_ID"))
				p_HR_Process_ID = para[i].getParameterAsInt();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}	
	}

	@Override
	protected String doIt() throws Exception {
		long start = System.currentTimeMillis();
		
		if(p_HR_Process_ID == 0)
			return null;
		String description = "";
		double result=0.0;
		MHRProcess_ConceptTest process = new MHRProcess_ConceptTest(getCtx(), p_HR_Process_ID, get_TrxName());
		
		for(int i=0; i<10; i++) {
			double turnodiurno = (20*7) + 35 + 45 + (10 *2.5);
			result= turnodiurno*8;
			description = "Cantidad: "+8;
		}
		long end = System.currentTimeMillis();
		long timeExecution = end - start;
		timeExecution = timeExecution / 1000;
		
		String msg = description + " - Result: " + String.valueOf(result);
		msg = msg + " - Tiempo de Ejecucion (seg): " + timeExecution;
		return msg;
	}

}
