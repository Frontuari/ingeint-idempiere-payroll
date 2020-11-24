package net.frontuari.model;

import java.sql.ResultSet;
import java.util.Properties;

import org.compiere.model.X_C_NonBusinessDay;

public class MNonBusinessDay extends X_C_NonBusinessDay {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1627146687893910869L;

	public MNonBusinessDay(Properties ctx, int C_NonBusinessDay_ID, String trxName) {
		super(ctx, C_NonBusinessDay_ID, trxName);
	}

	public MNonBusinessDay(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}

}
