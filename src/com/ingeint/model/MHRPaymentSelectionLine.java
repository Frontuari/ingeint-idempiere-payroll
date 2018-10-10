package com.ingeint.model;

import java.sql.ResultSet;
import java.util.Properties;

public class MHRPaymentSelectionLine extends X_HR_PaymentSelectionLine {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7853801398421134130L;
	
	protected MHRPaymentSelection m_parent;

	public MHRPaymentSelectionLine(Properties ctx, int HR_PaymentSelectionLine_ID, String trxName) {
		super(ctx, HR_PaymentSelectionLine_ID, trxName);
		// TODO Auto-generated constructor stub
	}
	
	public MHRPaymentSelectionLine(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}

	public MHRPaymentSelectionLine(MHRPaymentSelection ps) {
		
		this (ps.getCtx(), 0, ps.get_TrxName());
		if (ps.get_ID()==0) 
			throw new IllegalArgumentException("Header not saved");
		
		setHR_PaymentSelection_ID(ps.get_ID());
		setPaymentSelection(ps);
		// TODO Auto-generated constructor stub
	}
	
	public void setPaymentSelection (MHRPaymentSelection ps)
	{
		setClientOrg(ps);		
	}	//	setPaymentSelection

	public void setHeaderInfo(MHRPaymentSelection mhrPaymentSelection) {
		m_parent = mhrPaymentSelection;
	}
}
