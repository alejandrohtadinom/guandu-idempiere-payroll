package com.ingeint.component;

import org.eevolution.process.HRCreateConcept;
import org.eevolution.process.HRCreatePeriods;
import com.ingeint.process.RecalculateLoan;
import com.ingeint.process.PaymentSelection;

import com.ingeint.base.CustomProcessFactory;

public class ProcessFactory extends CustomProcessFactory{
	/**
	 * For initialize class. Register the process to build
	 * 
	 * <pre>
	 * protected void initialize() {
	 * 	registerProcess(PPrintPluginInfo.class);
	 * }
	 * </pre>
	 */
	@Override
	protected void initialize() {
		registerProcess(HRCreatePeriods.class);
		registerProcess(HRCreateConcept.class);
		registerProcess(RecalculateLoan.class);
		registerProcess(PaymentSelection.class);
	}
}
