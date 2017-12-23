/******************************************************************************
 * Product: iDempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2012 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/

package org.eevolution.form;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.adempiere.exceptions.AdempiereException;
import org.adempiere.webui.component.Button;
import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.GridFactory;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.Listbox;
import org.adempiere.webui.component.ListboxFactory;
import org.adempiere.webui.component.Row;
import org.adempiere.webui.component.Rows;
import org.adempiere.webui.component.WListbox;
import org.adempiere.webui.editor.WDateEditor;
import org.adempiere.webui.editor.WNumberEditor;
import org.adempiere.webui.editor.WStringEditor;
import org.adempiere.webui.panel.ADForm;
import org.adempiere.webui.panel.CustomForm;
import org.adempiere.webui.panel.IFormController;
import org.compiere.minigrid.ColumnInfo;
import org.compiere.model.MBPartner;
import org.compiere.model.MOrg;
import org.compiere.model.MTable;
import org.compiere.model.Query;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;
import org.eevolution.model.I_HR_Attribute;
import org.eevolution.model.I_HR_Concept;
import org.eevolution.model.MHRAttribute;
import org.eevolution.model.MHRConcept;
import org.eevolution.model.MHREmployee;
import org.eevolution.model.MHRPeriod;
import org.eevolution.model.MHRProcess;
import org.eevolution.model.X_HR_Concept;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Center;
import org.zkoss.zul.North;
import org.zkoss.zul.Space;

/**
 * Payroll Notice Form
 * 
 * @author Double Click Sistemas C.A. - http://dcs.net.ve
 * @author Saul Pina - spina@dcs.net.ve
 */
public class HRActionNoticeForm implements IFormController, EventListener<Event> {

	private CustomForm form;
	private Borderlayout mainLayout;
	private Grid gridPanel;
	private Label labelProcess;
	private Label labelBPartner;
	private Label labelValidFrom;
	private Label labelValue;
	private Label labelConcept;
	private Label labelColumnType;
	private Label labelDescription;
	private Listbox fieldProcess;
	private Listbox fieldEmployee;
	private Listbox fieldConcept;
	private WDateEditor fieldValidFrom;
	private WDateEditor fieldDate;
	private WDateEditor fieldValidFromAtt;
	private WDateEditor fieldValidToAtt;
	private WStringEditor fieldColumnType;
	private WStringEditor fieldDescription;
	private WStringEditor fieldText;
	private WNumberEditor fieldQty;
	private WNumberEditor fieldAmount;
	private Button buttonAdd;
	private Button buttonDelete;
	private WListbox miniTable;
	private MHRProcess process;
	private MHRConcept concept;
	private MBPartner partner;
	private MHREmployee employee;
	private MHRPeriod period;
	private WDateEditor fieldValidTo;
	private List<MHRAttribute> listAttribute;
	private Label labelValidFromAtt;
	private Label labelValidToAtt;
	private int AD_Org_ID;
	private int AD_Client_ID;

	public HRActionNoticeForm() {
		createObjects();
		loadGlobalVariables();
		loadPayrollProcces();
	}

	private void createObjects() {
		form = new CustomForm();
		mainLayout = new Borderlayout();
		labelProcess = new Label();
		labelBPartner = new Label();
		labelValidFrom = new Label();
		labelValue = new Label();
		labelValidFromAtt = new Label();
		labelValidToAtt = new Label();
		labelConcept = new Label();
		labelColumnType = new Label();
		labelDescription = new Label();
		fieldProcess = ListboxFactory.newDropdownListbox();
		fieldEmployee = ListboxFactory.newDropdownListbox();
		fieldConcept = ListboxFactory.newDropdownListbox();
		fieldValidFrom = new WDateEditor();
		fieldValidTo = new WDateEditor();
		fieldValidFromAtt = new WDateEditor();
		fieldValidToAtt = new WDateEditor();
		fieldDate = new WDateEditor();
		fieldColumnType = new WStringEditor();
		fieldDescription = new WStringEditor();
		fieldText = new WStringEditor();
		fieldQty = new WNumberEditor();
		fieldAmount = new WNumberEditor();
		buttonAdd = new Button();
		buttonDelete = new Button();
		miniTable = ListboxFactory.newDataTable();
		gridPanel = GridFactory.newGridLayout();

		// CONFIGURATION

		miniTable.addActionListener(this);

		form.setWidth("100%");
		form.setHeight("100%");
		form.setStyle("position: absolute; padding: 0; margin: 0");
		form.appendChild(mainLayout);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("100%");
		mainLayout.setStyle("position: absolute");

		labelProcess.setText(Msg.translate(Env.getCtx(), "HR_Process_ID"));
		fieldProcess.addActionListener(this);

		labelBPartner.setText(Msg.translate(Env.getCtx(), "C_BPartner_ID"));
		fieldEmployee.addActionListener(this);

		labelValidFrom.setText(Msg.translate(Env.getCtx(), "ValidFrom"));
		labelValue.setText(Msg.translate(Env.getCtx(), "ValueNumber"));

		labelValidFromAtt.setText(Msg.translate(Env.getCtx(), "ValidFrom"));
		labelValidToAtt.setText(Msg.translate(Env.getCtx(), "ValidTo"));

		labelConcept.setText(Msg.translate(Env.getCtx(), "HR_Concept_ID"));
		fieldConcept.addActionListener(this);

		labelColumnType.setText(Msg.translate(Env.getCtx(), "ColumnType"));
		labelDescription.setText(Msg.translate(Env.getCtx(), "Description"));
		fieldDate.setVisible(false);
		fieldQty.setVisible(false);
		fieldAmount.setVisible(false);
		fieldText.setVisible(false);

		buttonAdd.setLabel(Msg.getMsg(Env.getCtx(), "add"));
		buttonAdd.addActionListener(this);

		buttonDelete.setLabel(Msg.getMsg(Env.getCtx(), "delete"));
		buttonDelete.addActionListener(this);

		North north = new North();
		north.setStyle("border: none");
		mainLayout.appendChild(north);
		north.appendChild(gridPanel);

		Rows rows = new Rows();
		rows.setParent(gridPanel);

		Row row = rows.newRow();
		row.appendChild(labelProcess.rightAlign());
		row.appendChild(fieldProcess);
		fieldProcess.setWidth("100%");

		row.appendChild(labelValidFrom.rightAlign());
		Cell cell = new Cell();
		row.appendChild(cell);
		cell.appendChild(fieldValidFrom.getComponent());
		cell.appendChild(new Space());
		cell.appendChild(fieldValidTo.getComponent());

		row = rows.newRow();
		row.appendChild(labelBPartner.rightAlign());
		row.appendChild(fieldEmployee);
		fieldEmployee.setWidth("100%");

		row = rows.newRow();
		row.appendChild(labelConcept.rightAlign());
		row.appendChild(fieldConcept);
		fieldConcept.setWidth("100%");

		row = rows.newRow();
		row.appendChild(labelColumnType.rightAlign());
		row.appendChild(fieldColumnType.getComponent());
		fieldColumnType.fillHorizontal();
		row.appendChild(labelValue.rightAlign());

		cell = new Cell();
		row.appendChild(cell);
		cell.appendChild(fieldDate.getComponent());
		cell.appendChild(fieldQty.getComponent());
		cell.appendChild(fieldAmount.getComponent());
		cell.appendChild(fieldText.getComponent());
		row.appendChild(new Space());

		row = rows.newRow();
		row.appendChild(labelValidFromAtt.rightAlign());
		row.appendChild(fieldValidFromAtt.getComponent());
		row.appendChild(labelValidToAtt.rightAlign());
		row.appendChild(fieldValidToAtt.getComponent());

		row = rows.newRow();
		row.appendChild(labelDescription.rightAlign());
		row.appendChild(fieldDescription.getComponent());
		fieldDescription.fillHorizontal();
		row.appendChild(new Space());
		cell = new Cell();
		row.appendChild(cell);
		cell.appendChild(buttonAdd);
		cell.appendChild(new Space());
		cell.appendChild(buttonDelete);

		Center center = new Center();
		center.appendChild(miniTable);
		mainLayout.appendChild(center);
		miniTable.setVflex(true);
		miniTable.setWidth("100%");
	  //miniTable.setHeight("100%");

		fieldValidFrom.setReadWrite(false);
		fieldValidTo.setReadWrite(false);
		fieldValidFromAtt.setReadWrite(false);
		fieldValidToAtt.setReadWrite(false);
		fieldColumnType.setReadWrite(false);
		fieldEmployee.setEnabled(false);
		fieldConcept.setEnabled(false);
		buttonAdd.setEnabled(false);
		buttonDelete.setEnabled(false);
		fieldDescription.setReadWrite(false);

		ColumnInfo[] layout = {
				new ColumnInfo("ID", "", String.class), new ColumnInfo(Msg.translate(Env.getCtx(), "AD_Org_ID"), "", String.class), new ColumnInfo(Msg.translate(Env.getCtx(), "HR_Concept_ID"), "", String.class), new ColumnInfo(Msg.translate(Env.getCtx(), "ValidFrom"), "", Timestamp.class),
				new ColumnInfo(Msg.translate(Env.getCtx(), "ValidTo"), "", Timestamp.class), new ColumnInfo(Msg.translate(Env.getCtx(), "ColumnType"), "", String.class), new ColumnInfo(Msg.getElement(Env.getCtx(), "Qty"), "", BigDecimal.class),
				new ColumnInfo(Msg.getElement(Env.getCtx(), "Amount"), "", BigDecimal.class), new ColumnInfo(Msg.translate(Env.getCtx(), "ServiceDate"), "", Timestamp.class), new ColumnInfo(Msg.translate(Env.getCtx(), "Text"), "", String.class),
				new ColumnInfo(Msg.translate(Env.getCtx(), "Description"), "", String.class)
		};

		miniTable.prepareTable(layout, "", "", false, "");
	}

	private void loadGlobalVariables() {
		AD_Org_ID = Env.getAD_Org_ID(Env.getCtx());
		AD_Client_ID = Env.getAD_Client_ID(Env.getCtx());
	}
	//Filter for AD_Client_ID ocurieles 19/11/2014
	private void loadPayrollProcces() {
		String sql = "SELECT HRP.HR_Process_ID, HRP.DocumentNo ||'-'|| HRP.Name, HRP.DocumentNo,"
				+ " HRP.Name FROM HR_Process HRP WHERE HRP.IsActive = 'Y' AND HRP.DocStatus IN ('DR', 'PR', 'IP') AND HRP.AD_Client_ID= " + Env.getAD_Client_ID(Env.getCtx()) ;

		if (AD_Org_ID > 0) {
			sql += " AND HRP.AD_Org_ID = " + AD_Org_ID;
		}

		sql += " ORDER BY HRP.DocumentNo, HRP.Name";
		KeyNamePair[] processData = DB.getKeyNamePairs(sql, true);
		for (KeyNamePair item : processData)
			fieldProcess.appendItem(item.getName(), item);
		fieldProcess.setSelectedIndex(0);
	}

	public void changeProcess() {
		fieldValidFrom.setValue(null);
		fieldValidTo.setValue(null);
		fieldValidFromAtt.setValue(null);
		fieldValidToAtt.setValue(null);
		fieldColumnType.setValue("");
		fieldEmployee.removeAllItems();
		fieldConcept.removeAllItems();

		fieldDescription.setValue("");
		fieldText.setValue("");
		fieldDate.setValue(null);
		fieldQty.setValue(Env.ZERO);
		fieldAmount.setValue(Env.ZERO);

		fieldQty.setVisible(false);
		fieldAmount.setVisible(false);
		fieldDate.setVisible(false);
		fieldText.setVisible(false);

		fieldDescription.setReadWrite(false);
		fieldValidFromAtt.setReadWrite(false);
		fieldValidToAtt.setReadWrite(false);
		fieldEmployee.setEnabled(true);
		fieldConcept.setEnabled(false);
		buttonAdd.setEnabled(false);
		buttonDelete.setEnabled(false);
		miniTable.setRowCount(0);

		int processId = ((KeyNamePair) fieldProcess.getSelectedItem().getValue()).getKey();
		process = new MHRProcess(Env.getCtx(), processId, null);
		listAttribute = new ArrayList<MHRAttribute>();

		if (process.getHR_Period_ID() > 0) {
			period = MHRPeriod.get(Env.getCtx(), process.getHR_Period_ID());
			fieldValidFrom.setValue(period.getStartDate());
			fieldValidTo.setValue(period.getEndDate());
		} else if (processId > 0) {
			throw new AdempiereException(Msg.translate(Env.getCtx(), "PeriodNotFound"));
		}

		StringBuilder sql = new StringBuilder("SELECT BP.C_BPartner_ID, BP.Name FROM HR_Employee HRE JOIN C_BPartner BP ON "
				+ "(BP.C_BPartner_ID = HRE.C_BPartner_ID) WHERE HRE.IsActive = 'Y'  ");
				
		// This payroll not content periods, NOT IS a Regular Payroll > ogi-cd 28Nov2007
		if(process.getHR_Payroll_ID() != 0 && process.getHR_Period_ID() != 0)
		{
			sql.append(" AND HRE.HR_Payroll_ID="+process.getHR_Payroll_ID()+" AND HRE.AD_Org_ID = "+process.getAD_Org_ID());
		}
		
		// Selected Department
		if (process.getHR_Department_ID() != 0) 
		{
			sql.append(" AND HRE.HR_Department_ID = "+process.getHR_Department_ID());
		}
		
		// Selected Employee
		if (process.getC_BPartner_ID() != 0)
		{
			sql.append(" AND BP.C_BPartner_ID = "+process.getC_BPartner_ID());
		}
				
		sql.append(" ORDER BY BP.Name");
		KeyNamePair[] processData = DB.getKeyNamePairs(sql.toString(), true);
		for (KeyNamePair item : processData)
			fieldEmployee.appendItem(item.getName(), item);
		fieldEmployee.setSelectedIndex(0);
	}

	public void changeEmployee() {
		fieldConcept.setEnabled(true);
		fieldConcept.removeAllItems();
		fieldColumnType.setValue("");

		fieldValidFromAtt.setValue(null);
		fieldValidToAtt.setValue(null);

		fieldDescription.setValue("");
		fieldText.setValue("");
		fieldDate.setValue(null);
		fieldQty.setValue(Env.ZERO);
		fieldAmount.setValue(Env.ZERO);

		buttonAdd.setEnabled(false);
		buttonDelete.setEnabled(false);

		fieldValidFromAtt.setReadWrite(false);
		fieldValidToAtt.setReadWrite(false);
		fieldDescription.setReadWrite(false);

		fieldQty.setVisible(false);
		fieldAmount.setVisible(false);
		fieldDate.setVisible(false);
		fieldText.setVisible(false);

		String sql = "SELECT DISTINCT HRC.HR_Concept_ID, HRC.Name, HRC.Value FROM HR_Concept HRC WHERE HRC.AD_Client_ID = " + AD_Client_ID + " AND HRC.IsActive = 'Y' AND HRC.Type = 'C' ORDER BY HRC.Name";

		KeyNamePair[] processData = DB.getKeyNamePairs(sql, true);
		for (KeyNamePair item : processData)
			fieldConcept.appendItem(item.getName(), item);
		fieldConcept.setSelectedIndex(0);

		partner = new MBPartner(Env.getCtx(), ((KeyNamePair) fieldEmployee.getSelectedItem().getValue()).getKey(), null);

		if (partner.get_ID() <= 0) {
			throw new AdempiereException(Msg.translate(Env.getCtx(), "SelectRecord"));
		}

		ArrayList<Object> parameters = new ArrayList<Object>();
		parameters.add(partner.get_ID());
		parameters.add(process.getAD_Org_ID());
		parameters.add(process.getHR_Payroll_ID());

		employee = new Query(Env.getCtx(), MHREmployee.Table_Name, " C_BPartner_ID=? AND AD_Org_ID=? AND HR_Payroll_ID=? ", null).setParameters(parameters).firstOnly();
		loadTable();
	}

	public void changeConcept() {
		fieldAmount.setValue(Env.ZERO);
		fieldDate.setValue(null);
		fieldText.setValue("");
		fieldQty.setValue(Env.ZERO);
		buttonAdd.setEnabled(true);
		buttonDelete.setEnabled(false);

		fieldValidFromAtt.setReadWrite(true);
		fieldValidToAtt.setReadWrite(true);
		fieldDescription.setReadWrite(true);

		concept = new MHRConcept(Env.getCtx(), ((KeyNamePair) fieldConcept.getSelectedItem().getValue()).getKey(), null);
		miniTable.setSelectedItem(null);
		fieldColumnType.setValue(getColumnType(concept.getColumnType()));

		if (concept.get_ID() <= 0) {
			throw new AdempiereException(Msg.translate(Env.getCtx(), "SelectRecord"));
		}

		if (concept.getColumnType().equals(X_HR_Concept.COLUMNTYPE_Quantity)) {
			fieldQty.setVisible(true);
			fieldAmount.setVisible(false);
			fieldDate.setVisible(false);
			fieldText.setVisible(false);
		} else if (concept.getColumnType().equals(X_HR_Concept.COLUMNTYPE_Amount)) {
			fieldQty.setVisible(false);
			fieldAmount.setVisible(true);
			fieldDate.setVisible(false);
			fieldText.setVisible(false);
		} else if (concept.getColumnType().equals(X_HR_Concept.COLUMNTYPE_Date)) {
			fieldQty.setVisible(false);
			fieldAmount.setVisible(false);
			fieldDate.setVisible(true);
			fieldText.setVisible(false);
		} else if (concept.getColumnType().equals(X_HR_Concept.COLUMNTYPE_Text)) {
			fieldText.setVisible(true);
			fieldQty.setVisible(false);
			fieldAmount.setVisible(false);
			fieldDate.setVisible(false);
		}
	}

	public void loadTable() {
		ArrayList<Object> parameters = new ArrayList<Object>();
		parameters.add(employee.getC_BPartner_ID());
		parameters.add(period.getStartDate());
		listAttribute = new Query(Env.getCtx(), I_HR_Attribute.Table_Name, " C_BPartner_ID = ? AND (ValidTo >= ? OR ValidTo IS NULL) ", null).setParameters(parameters).setOrderBy("ValidTo").setOnlyActiveRecords(true).list();

		int row = 0;
		int c = 0;
		miniTable.setRowCount(row);
		for (MHRAttribute mhrAttribute : listAttribute) {
			miniTable.setRowCount(row + 1);
			miniTable.setValueAt(String.valueOf(mhrAttribute.getHR_Attribute_ID()), row, c++); // ID
			miniTable.setValueAt(new MOrg(Env.getCtx(), mhrAttribute.getAD_Org_ID(), null).getName(), row, c++); // AD_Org_ID
			miniTable.setValueAt(mhrAttribute.getHR_Concept().getName(), row, c++); // HR_Concept_ID
			miniTable.setValueAt(mhrAttribute.getValidFrom(), row, c++); // ValidFrom
			miniTable.setValueAt(mhrAttribute.getValidTo(), row, c++); // ValidTo
			miniTable.setValueAt(getColumnType(mhrAttribute.getColumnType()), row, c++); // ColumnType
			miniTable.setValueAt(mhrAttribute.getQty() != null ? mhrAttribute.getQty() : Env.ZERO, row, c++); // Qty
			miniTable.setValueAt(mhrAttribute.getAmount() != null ? mhrAttribute.getAmount() : Env.ZERO, row, c++); // Amount
			miniTable.setValueAt(mhrAttribute.getServiceDate(), row, c++); // ServiceDate
			miniTable.setValueAt(mhrAttribute.getTextMsg(), row, c++); // TextMsg
			miniTable.setValueAt(mhrAttribute.getDescription(), row, c++); // Description
			row++;
			c = 0;
		}
		miniTable.repaint();
		miniTable.autoSize();
	}

	public static String getColumnType(String value) {
		int columnType_Ref_ID = MTable.get(Env.getCtx(), I_HR_Concept.Table_ID).getColumn(I_HR_Concept.COLUMNNAME_ColumnType).getAD_Reference_Value_ID();
		String sql = "SELECT RLT.Name FROM AD_Ref_List RL JOIN AD_Ref_List_Trl RLT ON RL.AD_Ref_List_ID = RLT.AD_Ref_List_ID WHERE RL.AD_Reference_ID = ? AND RL.Value = ?";
		return DB.getSQLValueStringEx(null, sql, columnType_Ref_ID, value);
	}

	private void selectAttribute() {
		int index = miniTable.getSelectedIndex();

		if (index >= 0) {
			MHRAttribute attribute = new MHRAttribute(Env.getCtx(), Integer.parseInt(miniTable.getValueAt(index, 0).toString()), null);

			fieldConcept.setSelectedIndex(0);
			fieldColumnType.setValue(getColumnType(attribute.getColumnType()));
			concept = null;

			buttonAdd.setEnabled(true);
			buttonDelete.setEnabled(true);
			fieldValidFromAtt.setReadWrite(true);
			fieldValidToAtt.setReadWrite(true);
			fieldDescription.setReadWrite(true);
			fieldDescription.setValue(attribute.getDescription());
			fieldValidToAtt.setValue(attribute.getValidTo());
			fieldValidFromAtt.setValue(attribute.getValidFrom());

			if (attribute.getColumnType().equals(X_HR_Concept.COLUMNTYPE_Quantity)) {
				fieldQty.setVisible(true);
				fieldAmount.setVisible(false);
				fieldDate.setVisible(false);
				fieldText.setVisible(false);

				fieldQty.setValue(attribute.getQty());
			} else if (attribute.getColumnType().equals(X_HR_Concept.COLUMNTYPE_Amount)) {
				fieldQty.setVisible(false);
				fieldAmount.setVisible(true);
				fieldDate.setVisible(false);
				fieldText.setVisible(false);

				fieldAmount.setValue(attribute.getAmount());
			} else if (attribute.getColumnType().equals(X_HR_Concept.COLUMNTYPE_Date)) {
				fieldQty.setVisible(false);
				fieldAmount.setVisible(false);
				fieldDate.setVisible(true);
				fieldText.setVisible(false);

				fieldDate.setValue(attribute.getServiceDate());
			} else if (attribute.getColumnType().equals(X_HR_Concept.COLUMNTYPE_Text)) {
				fieldQty.setVisible(false);
				fieldAmount.setVisible(false);
				fieldDate.setVisible(false);
				fieldText.setVisible(true);

				fieldText.setValue(attribute.getTextMsg());
			}
		}
	}

	private void addAttribute() {

		if (fieldValidFromAtt.getValue() == null) {
			throw new AdempiereException(Msg.translate(Env.getCtx(), "HRValidFromMandatary"));
		}

		MHRAttribute attribute;
		MHRConcept concept;

		int index = miniTable.getSelectedIndex();
		if (index >= 0 && this.concept == null) {
			attribute = new MHRAttribute(Env.getCtx(), Integer.parseInt(miniTable.getValueAt(index, 0).toString()), null);
			concept = (MHRConcept) attribute.getHR_Concept();
		} else {
			attribute = new MHRAttribute(Env.getCtx(), 0, null);
			concept = this.concept;
		}

		attribute.setAD_Org_ID(process.getAD_Org_ID());
		attribute.setC_BPartner_ID(partner.get_ID());
		attribute.setHR_Concept_ID(concept.get_ID());
		attribute.setDescription(fieldDescription.getValue().toString());
		attribute.setValidFrom((Timestamp) fieldValidFromAtt.getValue());
		attribute.setValidTo((Timestamp) fieldValidToAtt.getValue());
		attribute.setColumnType(concept.getColumnType());

		if (concept.getColumnType().equals(X_HR_Concept.COLUMNTYPE_Quantity)) {
			BigDecimal value = (BigDecimal) fieldQty.getValue();

			if (value == null || value.compareTo(new BigDecimal(0)) == 0) {
				throw new AdempiereException(Msg.translate(Env.getCtx(), "HRValueMandatary"));
			}
			attribute.setQty(value);

		} else if (concept.getColumnType().equals(X_HR_Concept.COLUMNTYPE_Amount)) {
			BigDecimal value = (BigDecimal) fieldAmount.getValue();

			if (value == null || value.compareTo(new BigDecimal(0)) == 0) {
				throw new AdempiereException(Msg.translate(Env.getCtx(), "HRValueMandatary"));
			}
			attribute.setAmount(value);

		} else if (concept.getColumnType().equals(X_HR_Concept.COLUMNTYPE_Date)) {
			Timestamp value = (Timestamp) fieldDate.getValue();

			if (value == null) {
				throw new AdempiereException(Msg.translate(Env.getCtx(), "HRValueMandatary"));
			}

			attribute.setServiceDate(value);
		} else if (concept.getColumnType().equals(X_HR_Concept.COLUMNTYPE_Text)) {
			String value = (String) fieldText.getValue();
			if (value == null || value.isEmpty()) {
				throw new AdempiereException(Msg.translate(Env.getCtx(), "HRValueMandatary"));
			}
			attribute.setTextMsg(value);
		}

		attribute.saveEx();

		fieldConcept.setSelectedIndex(0);
		this.concept = null;

		fieldValidFromAtt.setValue(null);
		fieldValidToAtt.setValue(null);
		fieldColumnType.setValue("");
		fieldDescription.setValue("");
		fieldText.setValue("");
		fieldDate.setValue(null);
		fieldQty.setValue(Env.ZERO);
		fieldAmount.setValue(Env.ZERO);
		buttonAdd.setEnabled(false);
		buttonDelete.setEnabled(false);
		fieldValidFromAtt.setReadWrite(false);
		fieldValidToAtt.setReadWrite(false);
		fieldDescription.setReadWrite(false);
		fieldQty.setVisible(false);
		fieldAmount.setVisible(false);
		fieldDate.setVisible(false);
		fieldText.setVisible(false);

		loadTable();
	}

	private void deleteAttribute() {
		int index = miniTable.getSelectedIndex();
		if (index >= 0) {
			MHRAttribute attribute = listAttribute.get(miniTable.getSelectedIndex());
			attribute.deleteEx(true);
		}

		fieldConcept.setSelectedIndex(0);
		this.concept = null;

		fieldValidFromAtt.setValue(null);
		fieldValidToAtt.setValue(null);
		fieldColumnType.setValue("");
		fieldDescription.setValue("");
		fieldText.setValue("");
		fieldDate.setValue(null);
		fieldQty.setValue(Env.ZERO);
		fieldAmount.setValue(Env.ZERO);
		buttonAdd.setEnabled(false);
		buttonDelete.setEnabled(false);
		fieldValidFromAtt.setReadWrite(false);
		fieldValidToAtt.setReadWrite(false);
		fieldDescription.setReadWrite(false);
		fieldQty.setVisible(false);
		fieldAmount.setVisible(false);
		fieldDate.setVisible(false);
		fieldText.setVisible(false);
		loadTable();
	}

	@Override
	public void onEvent(Event e) throws Exception {
		if (e.getTarget().equals(fieldProcess)) {
			changeProcess();
		} else if (e.getTarget().equals(fieldEmployee)) {
			changeEmployee();
		} else if (e.getTarget().equals(fieldConcept)) {
			changeConcept();
		} else if (e.getTarget().equals(miniTable)) {
			selectAttribute();
		} else if (e.getTarget().equals(buttonAdd)) {
			addAttribute();
		} else if (e.getTarget().equals(buttonDelete)) {
			deleteAttribute();
		}
	}

	@Override
	public ADForm getForm() {
		return form;
	}

}