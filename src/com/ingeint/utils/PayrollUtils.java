package com.ingeint.utils;

import java.sql.Timestamp;
import java.util.Calendar;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MBankAccount;
import org.compiere.model.MPayment;
import org.compiere.util.DB;

import com.ingeint.model.MHRPaymentSelection;
import com.ingeint.model.MHRPaymentSelectionLine;;


public class PayrollUtils {
	
	public static MPayment createPayment(MHRPaymentSelectionLine psline) {		
		
		MHRPaymentSelection ps = new MHRPaymentSelection(psline.getCtx(), psline.getHR_PaymentSelection_ID(), psline.get_TrxName());
		MBankAccount ba = null;
		
		Integer C_BP_BankAccount_ID = DB.getSQLValue(psline.get_TrxName(), "SELECT C_BP_BankAccount_ID "
				+ "FROM C_BP_BankAccount "
				+ "WHERE C_BPartner_ID = ? AND C_Bank_ID = ? ", new Object[] {psline.getC_BPartner_ID(),psline.get_Value("C_BankAccountTo_ID")});
		
		MPayment payment = new MPayment(null, 0, psline.get_TrxName());
		
		if (C_BP_BankAccount_ID>0) {
			ba = new MBankAccount(psline.getCtx(), C_BP_BankAccount_ID, psline.get_TrxName());
			payment.setAccountNo(ba.getAccountNo());
			payment.setC_BP_BankAccount_ID(C_BP_BankAccount_ID);
		}
		payment.setAD_Org_ID(psline.getAD_Org_ID());
		payment.setDateAcct(now());
		payment.setC_BPartner_ID(psline.get_ValueAsInt("C_BPartner_ID"));
		payment.setC_BankAccount_ID(ps.getC_BankAccount_ID());
		if (psline.getC_BPartner().getPaymentRulePO()==null)
			throw new AdempiereException("Debe especificar el tipo de pago para el empleado "+psline.getC_BPartner().getTaxID()+"_"+psline.getC_BPartner().getName());
		payment.setTenderType(psline.getC_BPartner().getPaymentRulePO());
		payment.setDateTrx(payment.getDateAcct());
		payment.setC_DocType_ID(ps.getC_DocTypePayment_ID());
		payment.setDescription(psline.getDescription());
		payment.setC_Currency_ID(100);
		
		payment.setPayAmt(psline.getAmount());
		payment.setC_Charge_ID(ps.getC_Charge_ID());
		payment.saveEx();
		
		if (!psline.getC_BPartner().getPaymentRulePO().equals("K")) {
			payment.processIt("CO");
			payment.setProcessed(true);
			payment.saveEx();			
		}
		
		psline.setC_Payment_ID(payment.get_ID());
		psline.saveEx();
		
		return payment;
	}
	
	public static Timestamp now() {
		return new Timestamp(Calendar.getInstance().getTimeInMillis());
	}	
}
