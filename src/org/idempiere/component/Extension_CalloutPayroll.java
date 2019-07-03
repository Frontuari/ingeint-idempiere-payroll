package org.idempiere.component;

import java.util.Properties;

import org.adempiere.base.IColumnCallout;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.eevolution.model.CalloutPayroll;

public class Extension_CalloutPayroll extends CalloutPayroll implements IColumnCallout {

	@Override
	public String start(Properties ctx, int WindowNo, GridTab mTab,
			GridField mField, Object value, Object oldValue) {
		if (mField.getColumnName().equals("HR_Concept_ID"))
			return ColumnType(ctx, WindowNo, mTab, mField,value);
		return null;
	}

}
