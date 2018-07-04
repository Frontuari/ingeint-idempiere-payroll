package com.ingeint.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Properties;

import org.compiere.model.MOrder;
import org.compiere.util.DB;
import org.compiere.util.Env;

public class MHRLoan extends X_HR_Loan {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6899397581959009593L;

	public MHRLoan(Properties ctx, int HR_Loan_ID, String trxName) {
		super(ctx, HR_Loan_ID, trxName);
		// TODO Auto-generated constructor stub
	}

	public MHRLoan(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}

	public static MHRLoan createLoan(MOrder order) {
		
		MHRLoan loan = new MHRLoan(order.getCtx(), 0, order.get_TrxName());
		
		loan.setAD_Org_ID(order.getAD_Org_ID());
		loan.setC_BPartner_ID(order.getC_BPartner_ID());
		loan.setFeeNumbers(4);
		loan.setDateStart(order.getDateOrdered());
		loan.setAD_User_ID(order.getCreatedBy());
		loan.setC_Order_ID(order.getC_Order_ID());
		loan.setIsLoanActive(true);
		loan.setAmt(order.getGrandTotal());
		
		BigDecimal OpenAmt = DB.getSQLValueBD(order.get_TrxName(), "SELECT OpenAmt "
				+ "FROM HR_Loan "
				+ "WHERE C_BPartner_ID = ? AND IsLoanActive = 'Y' ", order.getC_BPartner_ID());

		if(OpenAmt==null)
			OpenAmt = Env.ZERO;
		
		loan.setOpenAmt(order.getGrandTotal().add(OpenAmt));
		loan.saveEx();
		
		//Generate Lines
		BigDecimal feeAmt = order.getGrandTotal().divide(BigDecimal.valueOf(loan.getFeeNumbers(), 0));
		
		for (int i=1; i <= loan.getFeeNumbers(); i= i+1) {
			MHRLoanLines lines = new MHRLoanLines(loan);
			lines.setFeeNumbers(i);
			lines.setAmt(feeAmt);
			lines.saveEx();			
		}//Generate Lines
		
		return loan;
	}	
}
