package org.eevolution.model;

import java.sql.ResultSet;
import java.util.Properties;

public class MHRJob extends X_HR_Job {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5843741831603604478L;

	public MHRJob(Properties ctx, int HR_Job_ID, String trxName) {
		super(ctx, HR_Job_ID, trxName);
	}

	public MHRJob(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}

}
