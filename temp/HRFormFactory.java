package ve.net.dcs.component;

import org.adempiere.ui.swing.factory.IFormFactory;
import org.compiere.apps.form.FormPanel;
import org.compiere.util.CLogger;

public class HRFormFactory implements IFormFactory {

	private final static CLogger log = CLogger.getCLogger(HRFormFactory.class);


	public HRFormFactory() {
	}

	@Override
	public FormPanel newFormInstance(String formName) {
		log.info("");

		Class<?> clazz = null;
		FormPanel form = null;

		try {
			clazz = Class.forName(formName);
		} catch (ClassNotFoundException e) {
			log.warning(String.format("Class not found for Form: %s", formName));
			return null;
		}

		try {
			form = (FormPanel) clazz.newInstance();
			return form;
		} catch (Exception e) {
			log.warning(String.format("Class can not be instantiated for Form: %s", formName));
			return null;
		}

	}
}
