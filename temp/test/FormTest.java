package test;

import org.adempiere.webui.panel.ADForm;
import org.compiere.util.CLogger;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;

public class FormTest extends ADForm implements EventListener<Event> {
	
	private final static CLogger log = CLogger.getCLogger(FormTest.class);
	
	private Div centerPanel;

	public FormTest() {
		centerPanel = new Div();
	}

	@Override
	protected void initForm() {
		log.info("");
		this.appendChild(new Label("hola"));

	}

}
