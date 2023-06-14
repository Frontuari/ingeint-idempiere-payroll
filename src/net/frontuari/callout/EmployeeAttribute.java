package net.frontuari.callout;

import org.eevolution.model.MHRConcept;

import com.ingeint.base.CustomCallout;

public class EmployeeAttribute extends CustomCallout {

	@Override
	protected String start() {
		
		if(getValue()==null)
			return "";
		
		int HR_Concept_ID = (int) getValue();
		MHRConcept c = new MHRConcept(getCtx(), HR_Concept_ID, null);
		getTab().setValue("ColumnType", c.getColumnType());
		
		return null;
	}

}
