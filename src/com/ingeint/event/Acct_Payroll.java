package com.ingeint.event;

import java.sql.Timestamp;

import org.compiere.model.MBPartner;
import org.compiere.util.DB;
import org.eevolution.model.MHRProcess;

import com.ingeint.base.CustomEventHandler;

public class Acct_Payroll extends CustomEventHandler {

	@Override
	protected void doHandleEvent() {
		MHRProcess pa = (MHRProcess) getPO();

		MBPartner bp = new MBPartner(pa.getCtx(), pa.getC_BPartner_ID(), pa.get_TrxName());

		if (bp.get_Value("EndDate") != null) {

			Timestamp EndDate = (Timestamp) bp.get_Value("EndDate");

			if (EndDate.compareTo(pa.getHR_Period().getEndDate()) == 0
					|| EndDate.compareTo(pa.getHR_Period().getEndDate()) == 1) {

				if (pa.getHR_Payroll().getValue().equals("LIQUIDACIONES")
						|| pa.getHR_Payroll().getValue().equals("QUINCENAL")) {
					return;
				}
				DB.executeUpdateEx(
						"DELETE FROM Fact_Acct WHERE C_BPartner_ID = ? AND AD_Table_ID = ? AND Record_ID = ? ",
						new Object[] { pa.getC_BPartner_ID(), MHRProcess.Table_ID, pa.getHR_Payroll_ID() },
						pa.get_TrxName());
			}
		}
	}
}
