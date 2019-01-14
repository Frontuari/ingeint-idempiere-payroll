package com.ingeint.model;

import java.sql.ResultSet;
import java.util.Properties;

public class MHRSectorCode extends X_HR_SectorCode {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1506340376281080432L;

	public MHRSectorCode(Properties ctx, int HR_SectorCode_ID, String trxName) {
		super(ctx, HR_SectorCode_ID, trxName);
		// TODO Auto-generated constructor stub
	}
	
	public MHRSectorCode(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}

}
