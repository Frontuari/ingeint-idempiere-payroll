package com.ingeint.component;

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
		registerProcess(org.eevolution.process.HRCreatePeriods.class);

		registerProcess(com.ingeint.process.RecalculateLoan.class);
		registerProcess(com.ingeint.process.PaymentSelection.class);
		//	Added by Jorge Colmenarez, 2020-11-23 23:38
		registerProcess(net.frontuari.process.PrintProcessReport.class);
		registerProcess(net.frontuari.process.CreateConcepts.class);
		registerProcess(net.frontuari.process.CopyFrom.class);
		registerProcess(net.frontuari.process.PrintHRContract.class);
	}
}
