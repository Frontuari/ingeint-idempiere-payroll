package net.frontuari.callout;

import org.compiere.util.DB;

import com.ingeint.base.CustomCallout;

public class CalloutLVEHRProcessReport extends CustomCallout {

	public CalloutLVEHRProcessReport() {
	}

	@Override
	protected String start() {
		
		if(getValue() == null)
			return "";
		//	
		int p_HR_Concept_ID = (Integer) getValue();
		String sql = "SELECT Value ||'-'|| Name AS PrintName "
				+ "	FROM HR_Concept "
				+ " WHERE HR_Concept_ID = ? ";
		
		String printName = DB.getSQLValueString(null, sql, p_HR_Concept_ID);
		
		getTab().setValue("PrintName", printName);
		
		return null;
	}

}
