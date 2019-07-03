package ve.net.dcs.component;

import org.adempiere.webui.factory.IFormFactory;
import org.adempiere.webui.panel.ADForm;
import org.adempiere.webui.panel.IFormController;
import org.compiere.util.CLogger;

public class HRZKFormFactory implements IFormFactory {

	private final static CLogger log = CLogger.getCLogger(HRZKFormFactory.class);

	public HRZKFormFactory() {

	}

	@Override
	public ADForm newFormInstance(String formName) {
		log.info("");

		Class<?> clazz;
		Object form;

		try {
			clazz = Class.forName(formName);
		} catch (Exception e) {
			log.warning(String.format("Class not found for Form: %s", formName));
			e.printStackTrace();
			return null;
		}

		try {
			form = clazz.newInstance();
		} catch (Exception e) {
			log.warning(String.format("Class cannot be instantiated for Form: %s", formName));
			e.printStackTrace();
			return null;
		}

		try {
			if (form instanceof ADForm) {
				return (ADForm) form;
			} else if (form instanceof IFormController) {
				IFormController controller = (IFormController) form;
				ADForm adForm = controller.getForm();
				adForm.setICustomForm(controller);
				return adForm;
			} else {
				return null;
			}
		} catch (Exception e) {
			log.warning(String.format("Class cannot be cast to Form: %s", formName));
			e.printStackTrace();
			return null;
		}
	}

}
