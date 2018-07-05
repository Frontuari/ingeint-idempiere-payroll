package com.ingeint.process;


import org.adempiere.exceptions.AdempiereException;
import org.compiere.util.DB;

import com.ingeint.base.CustomProcess;
import com.ingeint.model.MHRLoan;

public class RecalculateLoan extends CustomProcess {

	@Override
	protected void prepare() {
		// TODO Auto-generated method stub
	}

	@Override
	protected String doIt() throws Exception {

		MHRLoan loan = new MHRLoan(getCtx(), getRecord_ID(), get_TrxName());
		
		if(loan.getFeeNumbers()<=0)
			throw new AdempiereException("La cantidad de cuotas debe ser mayor a 0");

		if(loan.isProcessed())
			throw new AdempiereException("No puede ejecutar este proceso a un prestamo procesado");

		int deleteLines = DB.getSQLValue(loan.get_TrxName(), "DELETE FROM HR_LoanLines where HR_Loan_ID = ?",loan.get_ID());
		log.info("Deleted" +deleteLines);

		MHRLoan.createLoanLines(loan);

		return "Recalculado";
	}
}
