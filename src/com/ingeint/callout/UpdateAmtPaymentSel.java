package com.ingeint.callout;

import java.math.BigDecimal;

import com.ingeint.base.CustomCallout;
import com.ingeint.model.MHRPaymentSelectionLine;

public class UpdateAmtPaymentSel extends CustomCallout {

	@Override
	protected String start() {
		
		String total = getTab().get_ValueAsString(MHRPaymentSelectionLine.COLUMNNAME_PayAmt);
		if (total.equals("0"))
			return null;
		
		BigDecimal totalPayment = new BigDecimal(getTab().get_ValueAsString(MHRPaymentSelectionLine.COLUMNNAME_PayAmt));
		BigDecimal amount = new BigDecimal(getTab().get_ValueAsString(MHRPaymentSelectionLine.COLUMNNAME_Amount));
		
		if (totalPayment.compareTo(amount)==1) {
			getTab().setValue(MHRPaymentSelectionLine.COLUMNNAME_PayAmt, null);
			getTab().setValue(MHRPaymentSelectionLine.COLUMNNAME_OpenAmt,amount);
			getTab().fireDataStatusEEvent("El Monto del pago no puede ser mayor al valor de la nomina", "", true);
			return null;
		}		
		getTab().setValue(MHRPaymentSelectionLine.COLUMNNAME_OpenAmt, amount.subtract(totalPayment));
		
		return null;
	}

}
