package test;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;

import org.compiere.apps.form.FormFrame;
import org.compiere.apps.form.FormPanel;
import org.compiere.swing.CPanel;
import org.compiere.util.CLogger;

public class FormTest implements FormPanel, ActionListener {

	private final static CLogger log = CLogger.getCLogger(FormTest.class);

	private CPanel mainPanel;
	private int WindowNo;
	private FormFrame frame;

	public FormTest() {
		mainPanel = new CPanel();
	}

	@Override
	public void init(int WindowNo, FormFrame frame) {
		log.info("");
		this.WindowNo = WindowNo;
		this.frame = frame;
		frame.getContentPane().add(mainPanel, BorderLayout.CENTER);
		initForm();
	}

	private void initForm() {
		
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {

	}

	@Override
	public void dispose() {

	}

}
