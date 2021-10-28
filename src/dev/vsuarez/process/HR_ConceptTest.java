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
		
		double turnodiurno = (process.getConcept("CC_SALARIO_HORA_NOCTURNA")*7) + process.getConcept("CC_PRIMA_COMIDA_NOCTURNO") +(process.getConcept("CC_SALARIO_HORA_EXTRA_NOCTURNA") *2.5);
		result= turnodiurno*process.getAttribute("A_TURNO_NOCTURNO");
		description="Cantidad: "+process.getAttribute("A_TURNO_NOCTURNO");
		long end = System.currentTimeMillis();
		long timeExecution = end - start;
		timeExecution = timeExecution / 1000;
		
		String msg = description + " - Result: " + String.valueOf(result);
		msg = msg + " - Tiempo de Ejecucion (seg): " + timeExecution;
		return msg;
	}

}
