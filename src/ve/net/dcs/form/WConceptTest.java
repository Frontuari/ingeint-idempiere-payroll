/******************************************************************************
 * Product: iDempiere ERP & CRM Smart Business Solution                       *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * Copyright (C) 2003-2007 Double Click Systemas C.A.. All Rights Reserved.   *
 * @author(s): Freddy Heredia Double Click Systemas C.A.   
 * @author ocurieles Double Click Sistemas C.A.                   *
 *****************************************************************************/

package ve.net.dcs.form;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;
import java.util.logging.Level;

import org.adempiere.webui.apps.AEnv;
import org.adempiere.webui.component.Button;
import org.adempiere.webui.component.Checkbox;
import org.adempiere.webui.component.ConfirmPanel;
import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.GridFactory;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.ListItem;
import org.adempiere.webui.component.ListModelTable;
import org.adempiere.webui.component.Listbox;
import org.adempiere.webui.component.ListboxFactory;
import org.adempiere.webui.component.Panel;
import org.adempiere.webui.component.Row;
import org.adempiere.webui.component.Rows;
import org.adempiere.webui.component.Textbox;
import org.adempiere.webui.component.WListbox;
import org.adempiere.webui.editor.WDateEditor;
import org.adempiere.webui.editor.WSearchEditor;
import org.adempiere.webui.editor.WStringEditor;
import org.adempiere.webui.editor.WTableDirEditor;
import org.adempiere.webui.event.ValueChangeEvent;
import org.adempiere.webui.event.ValueChangeListener;
import org.adempiere.webui.event.WTableModelEvent;
import org.adempiere.webui.event.WTableModelListener;
import org.adempiere.webui.panel.ADForm;
import org.adempiere.webui.panel.CustomForm;
import org.adempiere.webui.panel.IFormController;
import org.adempiere.webui.session.SessionManager;
import org.compiere.apps.form.FactReconcile;
import org.compiere.model.MClient;
import org.compiere.model.MLookup;
import org.compiere.model.MLookupFactory;
import org.compiere.model.MRule;
import org.compiere.model.Query;
import org.compiere.model.SystemIDs;
import org.compiere.util.CLogger;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;
import org.eevolution.model.MHRAttribute;
import org.eevolution.model.MHRConcept;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Center;
import org.zkoss.zul.North;
import org.zkoss.zul.South;

import ve.net.dcs.process.*;



public class WConceptTest 
implements IFormController,EventListener<Event>, WTableModelListener,  ValueChangeListener{
	
	private CustomForm form = new CustomForm();
	private int			m_AD_Client_ID = 0;
	/** Format                  */
	private DecimalFormat   m_format = DisplayType.getNumberFormat(DisplayType.Amount);
	/** Number of selected rows */
	private int             m_noSelected = 0;
	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(WConceptTest.class);
	
	/**
	 *	Initialize Panel
	 */
	 // 2014-08-19 Change IsSOTrx to "" to show only employee -- ocurieles 
	  
	public WConceptTest()
	{
		Env.setContext(Env.getCtx(), form.getWindowNo(), "IsSOTrx", "");   //  defaults to no
		try
		{
			m_AD_Client_ID = Env.getAD_Client_ID(Env.getCtx());
			dynInit();
			zkInit();


			
		}
		catch(Exception e)
		{
			log.log(Level.SEVERE, "", e);
		}
	}	//	init
	
	private Borderlayout mainLayout = new Borderlayout();
	private Panel parameterPanel = new Panel();
	private Panel centerPanel = new Panel();
	private Label labelHRPeriod = new Label();
	private WTableDirEditor fieldHRPeriod = null;
	
	private Grid parameterLayout = GridFactory.newGridLayout();
	private Grid centerLayout = GridFactory.newGridLayout();
	
	private Label labelOrg = new Label();
	private WTableDirEditor fieldOrg = null;
	
	
	private Label labelHRConcept = new Label();
	private WSearchEditor fieldConcept = null;
	
	private Label labelPayRoll = new Label();
	private WTableDirEditor fieldPayroll = null;
	
	private Label labelBPartner = new Label();
	private WSearchEditor fieldBPartner = null;
	
	
	// data panel
	private Label dataStatus = new Label();
	
	
	// command panel
	private Panel commandPanel = new Panel();
	ConfirmPanel cp = new ConfirmPanel();
	private Button bCancel = cp.createButton(ConfirmPanel.A_CANCEL);
	private Button bGenerate = cp.createButton(ConfirmPanel.A_PROCESS);
	private Button bReset = cp.createButton(ConfirmPanel.A_RESET);
	private Button bZoom = cp.createButton(ConfirmPanel.A_ZOOM);
	private Grid commandLayout = GridFactory.newGridLayout();
	private Button bRefresh = cp.createButton(ConfirmPanel.A_REFRESH);
	

	
	private Textbox m_txbSqlField = new Textbox();
	private Textbox m_evalField = new Textbox();
	
	private boolean loading = false;
	private Label resultLabel = new Label();
	private Textbox resultField = new Textbox();
	private Label descriptionLabel = new Label();
	private WStringEditor descriptionField = new WStringEditor();
	private WStringEditor scripField = new WStringEditor();;
	public int m_C_BPartner_ID = 0;
	public int m_HRPayroll_ID = 0;
	private int m_HRPeriod_ID = 0;
	private int columnPeriod_ID =54913;
	public int m_HR_Concept_ID = 0;
	private MLookup lookupP = null;
	/**
	 *  Static Init
	 *  @throws Exception
	 */
	private void zkInit() throws Exception
	{
		//
		 final int noColumns = 60;
	     final int maxStatementLength = 10000;
	     final int noStatementRows = 10;
	     final int noResultRows = 20;
		form.appendChild(mainLayout);
		parameterPanel.appendChild(parameterLayout);
		centerPanel.appendChild(centerLayout);
		bRefresh.addActionListener(this);
		bReset.addActionListener(this);
		bZoom.addActionListener(this);
		bGenerate.setEnabled(true);
		bReset.setEnabled(false);
		//bRefresh.setText(Msg.getMsg(Env.getCtx(), "Query"));
		bGenerate.setLabel(Msg.getMsg(Env.getCtx(),"Test"));
		bReset.setLabel(Msg.getMsg(Env.getCtx(),"Reset"));
		bZoom.setLabel(Msg.translate(Env.getCtx(), "HR_Concetp_ID"));
		
		//
		labelHRPeriod.setText(Msg.translate(Env.getCtx(), "HR_Period_ID"));
		
		labelBPartner.setText(Msg.translate(Env.getCtx(), "C_BPartner_ID"));
		
		labelHRConcept.setText(Msg.translate(Env.getCtx(), "HR_Concept_ID"));
		labelPayRoll.setText(Msg.translate(Env.getCtx(), "HR_Payroll_ID"));
		//
		labelOrg.setText(Msg.translate(Env.getCtx(), "AD_Org_ID"));
		
		dataStatus.setText(" ");
		
		
		descriptionLabel.setText(Msg.getMsg(Env.getCtx(), "Result"));
		resultLabel.setText(Msg.getMsg(Env.getCtx(), "Result"));
		//resultField.setBackground(AdempierePLAF.getFieldBackground_Inactive());
		//resultField.setEditable(false);
		descriptionField.setValue(null);
		descriptionField.getComponent().setWidth("100%");
		resultField.setText("0");
		//resultField.setColumns(8);
		//resultField.setHorizontalAlignment(SwingConstants.RIGHT);
		//
		scripField.setValue(null);
		scripField.getComponent().setWidth("100%");
		
		bGenerate.addActionListener(this);
		bCancel.addActionListener(this);
		////Scrip
		
		 m_txbSqlField.setMultiline(true);
	        m_txbSqlField.setMaxlength(maxStatementLength);
	        m_txbSqlField.setRows(noStatementRows);
	        m_txbSqlField.setHeight("100%");
	        m_txbSqlField.setHflex("1");
	        m_txbSqlField.setCols(noColumns);
	        m_txbSqlField.setReadonly(false);
	        
	        m_evalField.setMultiline(true);
	        m_evalField.setMaxlength(maxStatementLength);
	        m_evalField.setRows(noStatementRows);
	        m_evalField.setHeight("100%");
	        m_evalField.setHflex("1");
	        m_evalField.setCols(noColumns);
	        m_evalField.setReadonly(false);
	        
		// Parameter Panel
		North north = new North();
		north.setStyle("border: none");
		mainLayout.appendChild(north);
		north.appendChild(parameterPanel);
		
		Rows rows = null;
		Row row = null;
		parameterLayout.setWidth("90%");
		rows = parameterLayout.newRows();
		row = rows.newRow();
		
		row.appendCellChild(labelPayRoll.rightAlign());
		fieldPayroll.getComponent().setWidth("100%");	
		row.appendCellChild(fieldPayroll.getComponent(), 2);
		

		row.appendCellChild(labelOrg.rightAlign());
		//fieldOrg.getComponent().setHflex("true");
		row.appendCellChild(fieldOrg.getComponent(), 2);
		row = rows.newRow();
		
		row.appendCellChild(labelHRPeriod.rightAlign());
		//fieldHRPeriod.getComponent().setHflex("true");
		fieldHRPeriod.dynamicDisplay();
		row.appendCellChild(fieldHRPeriod.getComponent(), 2);
		

		row = rows.newRow();
		row.appendCellChild(labelBPartner.rightAlign());
		fieldBPartner.getComponent().setHflex("true");
		row.appendCellChild(fieldBPartner.getComponent(), 2);
		row.appendCellChild(labelHRConcept.rightAlign());
		fieldConcept.getComponent().setHflex("true");
		row.appendCellChild(fieldConcept.getComponent(), 2);
		row = rows.newRow();
		

		row.appendChild(bRefresh);
		
	
		// Script Panel

	
		Center mainPanelCenter = new Center();
		mainPanelCenter.setStyle("border: none");
		mainLayout.appendChild(mainPanelCenter);

		
		mainPanelCenter.appendChild(centerPanel);
		centerLayout.setWidth("90%");

		rows = centerLayout.newRows();
		row = rows.newRow();
		centerPanel.appendChild(centerLayout);

		row.appendChild(m_txbSqlField);
		row = rows.newRow();
		row.appendChild(m_evalField);
		

		
		// Command Panel
		South south = new South();
		south.setStyle("border: none");
		mainLayout.appendChild(south);
		south.appendChild(commandPanel);
		commandPanel.appendChild(commandLayout);
		commandLayout.setWidth("90%");
		rows = commandLayout.newRows();
		row = rows.newRow();
		row.appendCellChild(bZoom, 2);
		bZoom.setHflex("true");
		//bZoom.setWidth("100%");
		row.appendCellChild(descriptionLabel.rightAlign());
		descriptionLabel.setHflex("true");
		row.appendCellChild(descriptionField.getComponent(), 3);
		resultField.setHflex("true");
		row.appendCellChild(resultLabel.rightAlign());
		resultLabel.setHflex("true");
		row.appendCellChild(resultField, 2);
		resultField.setHflex("true");
		row.appendCellChild(bGenerate, 2);
		bGenerate.setHflex("true");

		row.appendCellChild(bCancel);
		bCancel.setHflex("true");
		// ***************************LISTENER****************************//
		fieldBPartner.addValueChangeListener(this);
		fieldOrg.addValueChangeListener(this);
		
		fieldConcept.addValueChangeListener(this);
		fieldPayroll.addValueChangeListener(this);
		fieldHRPeriod.addValueChangeListener(this);
		
		// ***************************LISTENER****************************//
	}
	
	/**
	 *  Dynamic Init (prepare dynamic fields)
	 *  @throws Exception if Lookups cannot be initialized
	 */
	public void dynInit() throws Exception
	{

		m_AD_Client_ID = Env.getAD_Client_ID(Env.getCtx());

		

		
		// Organization
		
		MLookup lookupOrg = MLookupFactory.get(Env.getCtx(), form.getWindowNo(), 0,  SystemIDs.COLUMN_C_PERIOD_AD_ORG_ID, DisplayType.TableDir);
		fieldOrg = new WTableDirEditor("AD_Org_ID", true, false, true, lookupOrg);
		if (lookupOrg.containsKey(Env.getAD_Org_ID(Env.getCtx())))
			fieldOrg.setValue(Env.getAD_Org_ID(Env.getCtx()));
		else
			fieldOrg.setValue(0);
		
		//  BPartner
		
		MLookup lookupBP = MLookupFactory.get (Env.getCtx(), form.getWindowNo(), 0, SystemIDs.COLUMN_C_INVOICE_C_BPARTNER_ID, DisplayType.Search);
		fieldBPartner = new WSearchEditor("C_BPartner_ID", false, false, true, lookupBP);
		
		// Concept
		//AD_Column_ID = 54945;        //  HR_PayrollConcept.HR_Concept_ID
		MLookup lookupConcept = MLookupFactory.get (Env.getCtx(), form.getWindowNo(), 0, 54945, DisplayType.Search);
		fieldConcept = new WSearchEditor("HR_Concept_ID", false, false, true, lookupConcept);
		fieldConcept.setValue(null);
		
		//  Payroll
		//AD_Column_ID =54872;
		MLookup lookupPayroll = MLookupFactory.get (Env.getCtx(), form.getWindowNo(), 0, 54872,  DisplayType.TableDir);
		fieldPayroll = new WTableDirEditor("HR_Payroll_ID", false, false, true, lookupPayroll);
		if (fieldPayroll.getLookup().getSize()>0){
			//fieldPayroll.setValue(Integer.parseInt(fieldPayroll.getComponent().getItemAtIndex(1).getValue().toString()));

		}
	//  Period  //  HR_Process.HR_Period_ID
		//lookupP = MLookupFactory.get (Env.getCtx(), form.getWindowNo(), 0, DisplayType.TableDir, Env.getLanguage(Env.getCtx()), "HR_Period_ID", 0, false, "(HR_Period.HR_Payroll_ID = "+fieldPayroll.getComponent().getSelectedItem().getValue()+")");
		lookupP = MLookupFactory.get (Env.getCtx(), form.getWindowNo(), 0, DisplayType.TableDir, Env.getLanguage(Env.getCtx()), "HR_Period_ID", 0, false, "");
		//lookupP = MLookupFactory.get (Env.getCtx(), form.getWindowNo(), 0, columnPeriod_ID, DisplayType.TableDir);
		fieldHRPeriod = new WTableDirEditor("HR_Period_ID", true, false, true, lookupP);
		//fieldHRPeriod.setReadWrite(true);
	}
	
	
	
	@Override
	public void valueChange(ValueChangeEvent evt) {
		// TODO Auto-generated method stub
		
		if (evt.getSource().equals(fieldBPartner)) {
			if (evt.getNewValue()!=null){
				m_C_BPartner_ID = Integer.valueOf(evt.getNewValue().toString());
			}
		}else if (evt.getSource().equals(fieldHRPeriod)){
			if (evt.getNewValue()!=null){
				m_HRPeriod_ID = Integer.valueOf(evt.getNewValue().toString());
			}
		}else if (evt.getSource().equals(fieldPayroll)){
			if (evt.getNewValue()!=null){
				m_HRPayroll_ID =Integer.valueOf(evt.getNewValue().toString());
				
				
				
				lookupP = (MLookup) fieldHRPeriod.getLookup();
				fieldHRPeriod.getComponent().setDisabled(false);
				try {
					lookupP = MLookupFactory.get (Env.getCtx(), form.getWindowNo(), 54872, DisplayType.TableDir, Env.getLanguage(Env.getCtx()), "HR_Period_ID", 0, false, "(HR_Period.HR_Payroll_ID = "+m_HRPayroll_ID+")");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//fieldHRPeriod = new WTableDirEditor("HR_Period_ID", true, true, true, lookupP);
				//fieldHRPeriod.getComponent().setSelectedItem(fieldHRPeriod.getComponent().getItemAtIndex(1));
				lookupP.refresh();
				lookupP.loadComplete();
				lookupP.fillComboBox(true);
				lookupP.fillComboBox(true,true,true, false,false);
				fieldHRPeriod.getComponent().removeAllItems();
				fieldHRPeriod.contentsChanged(null);
				fieldHRPeriod.actionRefresh();
				//fieldHRPeriod.setReadWrite(true);
				//fieldHRPeriod.getComponent().setEnabled(true);
				fieldHRPeriod.getComponent().setDisabled(false);
			}
		}else if (evt.getSource().equals(fieldConcept)){
			if (evt.getNewValue()!=null){
				m_HR_Concept_ID = Integer.valueOf(evt.getNewValue().toString());	 
				m_txbSqlField.setValue(getScriptFromConcept(m_HR_Concept_ID));
				descriptionField.setValue("");
				resultField.setValue("0");
			}
		}
	}

	

	@Override
	public void onEvent(Event event) throws Exception {
		log.config("");
		if (event.getTarget().equals(bGenerate))
			Test();
		
		else if (event.getTarget().equals(bReset))
			reset();

		else if (event.getTarget().equals(bZoom))
			zoom();
		
		else if (event.getTarget().equals(bCancel))
			SessionManager.getAppDesktop().closeActiveWindow();
		
		else if (event.getTarget().equals(bRefresh))
			dynInit();
		
			
		
	}
	
	private void Test() {
		log.info("");
		MHRConcept concept = null;
		MHRProcess_ConceptTest conceptTest = new MHRProcess_ConceptTest(Env.getCtx(),0,null);
		
		
		if (fieldBPartner.getValue()!=null)
			conceptTest.setC_BPartner_ID(Integer.parseInt(fieldBPartner.getValue().toString()));
		if (fieldPayroll.getValue()!=null)
			conceptTest.setHR_Payroll_ID(Integer.parseInt(fieldPayroll.getValue().toString()));
		if (fieldHRPeriod.getValue()!=null)
			conceptTest.setHR_Period_ID(Integer.parseInt(fieldHRPeriod.getValue().toString()));
		if (fieldConcept.getValue()!=null){
			conceptTest.setM_HR_Concept_ID(Integer.parseInt(fieldConcept.getValue().toString()));
			concept = MHRConcept.get(Env.getCtx(), Integer.parseInt(fieldConcept.getValue().toString()));
		}
		if (fieldOrg.getValue()!=null)
			conceptTest.setAD_Org_ID(Integer.parseInt(fieldOrg.getValue().toString()));
	
		if (m_txbSqlField.getValue()!=null)
			conceptTest.setScriptText(m_txbSqlField.getValue());
		conceptTest.setHR_Department_ID(0);
		conceptTest.loadParameter();
		if (concept!=null){
			if (!concept.getType().equals(MHRConcept.TYPE_RuleEngine)){
				resultField.setText(String.valueOf(conceptTest.testConcept(concept.getValue())));
			}else{
				resultField.setText(String.valueOf(conceptTest.executeScriptManual(0, "")));
			}	
		}else{
			resultField.setText(String.valueOf(conceptTest.executeScriptManual(0, "")));
		}
			
		descriptionField.setValue(conceptTest.getM_description()!=null?conceptTest.getM_description().toString():"");
		m_evalField.setValue(conceptTest.getM_eval().toString());
		
	}
	
	/**
	 *	Zoom to target
	 *  @param AD_Window_ID window id
	 *  @param zoomQuery zoom query
	 */
	protected void zoom ()
	{
		log.info("");
		if (fieldConcept.getValue()!=null)
			AEnv.zoom(MHRConcept.Table_ID, Integer.parseInt(fieldConcept.getValue().toString()));
	}	//	zoom
	
	private void reset() {
		
	}

	@Override
	public ADForm getForm() {
		return form;
	}

	@Override
	public void tableChanged(WTableModelEvent event) {
		// TODO Auto-generated method stub
		
	}

	private String getScriptFromConcept (int p_concept_ID){
		String script = "";
		MHRAttribute atributeR = null;
		if (p_concept_ID != 0){
			MHRConcept concept = MHRConcept.get(Env.getCtx(), p_concept_ID);
			if (concept!=null){
				atributeR = new Query (Env.getCtx(),MHRAttribute.Table_Name," HR_Concept_ID = ? ",null).setParameters(concept.get_ID()).first();
			}else{
				return "";
			}
			if (atributeR!=null){
				
			}else{
				return "";
			}
			MRule rule = new Query(Env.getCtx(), MRule.Table_Name," AD_Rule_ID = ?",null).setParameters(atributeR.getAD_Rule_ID()).first();
			
			script = rule!=null?rule.getScript():"";
		}
		return script;
	}

}

