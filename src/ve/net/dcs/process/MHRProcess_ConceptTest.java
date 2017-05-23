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
 * Contributor(s): Freddy Heredia Double Click Systemas C.A.                  *
 *****************************************************************************/
package ve.net.dcs.process;

import java.io.File;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MBPartner;
import org.compiere.model.MDocType;
import org.compiere.model.MFactAcct;
import org.compiere.model.MPeriod;
import org.compiere.model.MPeriodControl;
import org.compiere.model.MRule;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.Query;
import org.compiere.model.Scriptlet;
import org.compiere.print.ReportEngine;
import org.compiere.process.DocAction;
import org.compiere.process.DocumentEngine;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.TimeUtil;
import org.eevolution.model.*;
import org.eevolution.process.*;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.NameSpace;
/**
 * HR Process Model
 *
 *  @author oscar.gomez@e-evolution.com, e-Evolution http://www.e-evolution.com
 *			<li> Original contributor of Payroll Functionality
 *  @author victor.perez@e-evolution.com, e-Evolution http://www.e-evolution.com
 * 			<li> FR [ 2520591 ] Support multiple calendar for Org 
 *			@see http://sourceforge.net/tracker2/?func=detail&atid=879335&aid=2520591&group_id=176962
 * @contributor Cristina Ghita, www.arhipac.ro
 * 
 * @contributor Jenny Rodriguez - jrodriguez@dcsla.com, Double Click Sistemas http://www.dcsla.com
 *			<li> 
 * @contributor Rafael Salazar C. - rsalazar@dcsla.com, Double Click Sistemas http://www.dcsla.com
 *			<li> 
 *			<li> 
 * @contributor Freddy Heredia. - fheredia@dcs.net.ve, Double Click Sistemas http://www.dcsla.com
 *			<li> 
 */
public class MHRProcess_ConceptTest extends MHRProcess implements DocAction
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5310991830396703407L;

	public int m_C_BPartner_ID = 0;
	public int m_AD_User_ID = 0;
	public int m_HR_Concept_ID = 0;
	public String m_columnType   = "";
	public Timestamp m_dateFrom;
	public Timestamp m_dateTo;	
	/** HR_Concept_ID->MHRMovement */
	public Hashtable<Integer, MHRMovement> m_movement = new Hashtable<Integer, MHRMovement>();
	public MHRPayrollConcept[] linesConcept;
	/** The employee being processed */
	private MHREmployee m_employee;
	/** the context for rules */
	HashMap<String, Object> m_scriptCtx = new HashMap<String, Object>();
	/* stack of concepts executing rules - to check loop in recursion */
	private List<MHRConcept> activeConceptRule = new ArrayList<MHRConcept>();
	private MHRPeriod period =null; 
	/**	Static Logger	*/
	private static CLogger	s_log	= CLogger.getCLogger (MHRProcess_ConceptTest.class);
	public static final String CONCEPT_PP_COST_COLLECTOR_LABOR = "PP_COST_COLLECTOR_LABOR"; // HARDCODED
	Object m_description = null;
	private String m_eval = "";


	private static StringBuilder s_scriptImport = new StringBuilder(" import ve.net.dcs.process.*;") 
													.append(" import org.eevolution.model.*;") 
													.append(" import org.compiere.model.*;")
													.append(" import org.adempiere.model.*;")
													.append(" import org.compiere.util.*;")
													.append(" import java.math.*;")
													.append(" import org.globalqss.model.*;")
													.append(" import java.sql.*;");
													

	public static void addScriptImportPackage(String packageName)
	{
		s_scriptImport.append(" import ").append(packageName).append(";");
	}

	private String scriptText = "";
	
	/**************************************************************************
	 *  Default Constructor
	 *  @param ctx context
	 *  @param  HR_Process_ID    To load, (0 create new order)
	 */
	public MHRProcess_ConceptTest(Properties ctx, int HR_Process_ID, String trxName) 
	{
		super(ctx, HR_Process_ID,trxName);
		if (HR_Process_ID == 0)
		{
			setDocStatus(DOCSTATUS_Drafted);
			setDocAction(DOCACTION_Prepare);
			setC_DocType_ID(0);
			set_ValueNoCheck ("DocumentNo", null);
			setProcessed(false);
			setProcessing(false);
			setPosted(false);
			setHR_Department_ID(0);
			setC_BPartner_ID(0);
		}
		
	}

	/**
	 *  Load Constructor
	 *  @param ctx context
	 *  @param rs result set record
	 */
	public MHRProcess_ConceptTest(Properties ctx, ResultSet rs, String trxName) 
	{
		super(ctx, rs,trxName);
	}	//	MHRProcess_ConceptTest


	
	/**
	 * Execute the script
	 * @param AD_Rule_ID
	 * @param string 
	 * @return
	 */
	public Object executeScriptManual(int AD_Rule_ID, String columnType)
	{
		MRule rulee =null;
		if (AD_Rule_ID!=0)
			rulee = MRule.get(getCtx(), AD_Rule_ID);
		Object result = null;
		m_description = null;

		try
		{
			
			if (AD_Rule_ID!=0 && scriptText.length()==0)
			{
				scriptText = rulee.getScript().trim().replaceAll("\\bget", "process.get")
				.replace(".process.get", ".get");
			}else{
				scriptText = scriptText.trim().replaceAll("\\bget", "process.get")
						.replace(".process.get", ".get");
			}	
			
			String resultType = "double result = 0;";
			if (MHRAttribute.COLUMNTYPE_Text.equals(columnType))
				resultType = "String result = null;";
			if (MHRAttribute.COLUMNTYPE_Date.equals(columnType) || (scriptText.toString().contains("Timestamp result = null;"))) {
				resultType = "Timestamp result = null;";
			}
			
			 String script =
				s_scriptImport.toString()
				+" "+ resultType 
				+" String description = null;"
				+ scriptText;
			Scriptlet engine = new Scriptlet (Scriptlet.VARIABLE, script, m_scriptCtx);	
			
			//Eval
			Interpreter i = new Interpreter();
			
			try
			{
				log.config(script);
				//i.debug(m_script);
				loadEnvironment(i);
				i.eval(script);
			}
			catch (Exception e)
			{
				log.warning(e.toString());
				m_eval = e.toString();
				//return Double.parseDouble("0");
			}
			Exception ex = engine.execute();
			if (ex != null)
			{
				throw ex;
			}
			result = engine.getResult(false);
			m_description = engine.getDescription();
		}
		catch (Exception e)
		{
			m_eval = e.toString();
			return new Double(0);
		//	throw new AdempiereException("Execution error - @AD_Rule_ID@="+rulee.getValue());
		}
		return result;
	}

	public Object executeScript(int AD_Rule_ID, String columnType)
	{
		MRule rulee =null;
		if (AD_Rule_ID!=0)
			rulee = MRule.get(getCtx(), AD_Rule_ID);
		Object result = null;
		m_description = null;

		try
		{
			
			if (AD_Rule_ID!=0)
			{
				scriptText = rulee.getScript().trim().replaceAll("\\bget", "process.get")
				.replace(".process.get", ".get");
			}
			
			String resultType = "double result = 0;";
			if (MHRAttribute.COLUMNTYPE_Text.equals(columnType))
				resultType = "String result = null;";
			if (MHRAttribute.COLUMNTYPE_Date.equals(columnType)) {
				resultType = "Timestamp result = null;";
			}
			 String script =
				s_scriptImport.toString()
				+" " + resultType
				+" String description = null; "
				+ " Timestamp serviceDate = null; "
				+ scriptText;
			Scriptlet engine = new Scriptlet (Scriptlet.VARIABLE, script, m_scriptCtx);	
			
			//Eval
			Interpreter i = new Interpreter();
			
			try
			{
				log.config(script);
				//i.debug(m_script);
				loadEnvironment(i);
				i.eval(script);
			}
			catch (Exception e)
			{
				log.warning(e.toString());
				m_eval = e.toString();
				//return Double.parseDouble("0");
			}
			Exception ex = engine.execute();
			if (ex != null)
			{
				throw ex;
			}
			result = engine.getResult(false);
			m_description = engine.getDescription();
		}
		catch (Exception e)
		{
			m_eval = e.toString();
			return new Double(0);
		//	throw new AdempiereException("Execution error - @AD_Rule_ID@="+rulee.getValue());
		}
		return result;
	}


	public Object testConcept(String pconcept) {
		MHRAttribute att = null;
		loadParameter();
		log.info("Parametros: _Process="+getHR_Process_ID()+", _To="+period.getEndDate());
		MHRConcept concept = MHRConcept.forValue(getCtx(), pconcept.trim());
		int AD_Rule_ID = 0;
		String ColunmType = "";
		Object result = new Object();
		if (concept == null)
		{   
			concept = new MHRConcept(getCtx(), 0, null);
			//concept.setType(MHRConcept.TYPE_RuleEngine);
			log.info("Testing concept  ");
		}else{
			log.info("Calculating concept " + concept.getValue());
			m_columnType       = concept.getColumnType();

			List<Object> params = new ArrayList<Object>();
			StringBuilder whereClause = new StringBuilder();
			whereClause.append("? >= ValidFrom AND ( ? <= ValidTo OR ValidTo IS NULL)");
			params.add(m_dateFrom);
			params.add(m_dateTo);
			whereClause.append(" AND HR_Concept_ID = ? ");
			params.add(concept.getHR_Concept_ID());
			whereClause.append(" AND EXISTS (SELECT 1 FROM HR_Concept conc WHERE conc.HR_Concept_ID = HR_Attribute.HR_Concept_ID )");


				whereClause.append(" AND (C_BPartner_ID = ? OR C_BPartner_ID IS NULL) AND (HR_Employee_ID = ? OR HR_Employee_ID IS NULL)");
				params.add(m_employee.getC_BPartner_ID());
				params.add(m_employee.get_ID());
			

			att = new Query(getCtx(), MHRAttribute.Table_Name, whereClause.toString(), get_TrxName())
			.setParameters(params)
			.setOnlyActiveRecords(true)
			.setOrderBy(MHRAttribute.COLUMNNAME_ValidFrom + " DESC")
			.first();
			if (att == null || concept.isRegistered())
			{
				log.info("Skip concept "+concept+" - attribute not found");
				
			}else{
				AD_Rule_ID = att.getAD_Rule_ID();
				ColunmType = att.getColumnType();
			}

			log.info("Concept - " + concept.getName());

		}
		
		
		if (MHRConcept.TYPE_RuleEngine.equals(concept.getType()))
		{
			log.info("Executing rule for concept " + concept.getValue());
			if (activeConceptRule.contains(concept)) {
				throw new AdempiereException("Recursion loop detected in concept " + concept.getValue());
			}
			activeConceptRule.add(concept);
			result = executeScript(AD_Rule_ID, ColunmType);
			activeConceptRule.remove(concept);
			if (result == null)
			{
				// TODO: throw exception ???
				log.warning("Variable (result) is null");
				return 0;
			}
			
			if (m_description == null)
				m_description = "N/A";
		}else if (MHRConcept.TYPE_Concept.equals(concept.getType()) && att!=null){
			if (concept.getColumnType().equals(MHRConcept.COLUMNTYPE_Amount))
				result = Double.parseDouble(att.getAmount().toString());
			if (concept.getColumnType().equals(MHRConcept.COLUMNTYPE_Quantity))
				result = Double.parseDouble(att.getQty().toString());
			if (concept.getColumnType().equals(MHRConcept.COLUMNTYPE_Text))
				m_description =att.getTextMsg();
			
		}else if (scriptText.toString().length()>0){
		
			result = executeScript(AD_Rule_ID, ColunmType);
			
		}else{
			result=new Double(0);
		}
		if (m_description == null)
			m_description = "N/A";
		log.info("Result: "+result.toString()+", Descripcion: "+m_description);
		
		return result.toString();
	}



	// Helper methods -------------------------------------------------------------------------------

	/**
	 * Helper Method : sets the value of a concept
	 * @param conceptValue
	 * @param value
	 */
	public void setConcept (String conceptValue, double value)
	{
		try
		{
			MHRConcept c = MHRConcept.forValue(getCtx(), conceptValue); 
			if (c == null)
			{
				return; // TODO throw exception
			}
			MHRMovement m = new MHRMovement(getCtx(), 0, get_TrxName());
			MHREmployee employee = MHREmployee.getActiveEmployee(getCtx(), m_C_BPartner_ID, get_TrxName());
			m.setColumnType(c.getColumnType());
			m.setColumnValue(BigDecimal.valueOf(value));

			m.setHR_Process_ID(getHR_Process_ID());
			m.setHR_Concept_ID(m_HR_Concept_ID);
			m.setC_BPartner_ID(m_C_BPartner_ID);
			m.setDescription("Added From Rule"); // TODO: translate
			m.setValidFrom(m_dateTo);
			m.setValidTo(m_dateTo);

			m.setHR_Concept_Category_ID(c.getHR_Concept_Category_ID());
			m.setHR_Department_ID(employee.getHR_Department_ID());
			m.setHR_Job_ID(employee.getHR_Job_ID());
			m.setIsRegistered(c.isRegistered());
			m.setC_Activity_ID(employee.getC_Activity_ID());
			// m.setProcessed(true);  ??			
			
			m.saveEx();
		} 
		catch(Exception e)
		{
			s_log.warning(e.getMessage());
		}
	} // setConcept
	
	/* Helper Method : sets the value of a concept and set if isRegistered 
	* @param conceptValue
	* @param value
	* @param isRegistered
	*/
	public void setConcept (String conceptValue,double value,boolean isRegistered)
	{
		try
		{
			MHRConcept c = MHRConcept.forValue(getCtx(), conceptValue); 
			if (c == null)
			{
				return; // TODO throw exception
			}
			MHRMovement m = new MHRMovement(Env.getCtx(),0,get_TrxName());
			MHREmployee employee = MHREmployee.getActiveEmployee(getCtx(), m_C_BPartner_ID, get_TrxName());
			m.setColumnType(c.getColumnType());
			if (c.getColumnType().equals(MHRConcept.COLUMNTYPE_Amount))
				m.setAmount(BigDecimal.valueOf(value));
			else if (c.getColumnType().equals(MHRConcept.COLUMNTYPE_Quantity))
				m.setQty(BigDecimal.valueOf(value));
			else
				return;
			m.setHR_Process_ID(getHR_Process_ID());
			m.setHR_Concept_ID(c.getHR_Concept_ID());
			m.setC_BPartner_ID(m_C_BPartner_ID);
			m.setDescription("Added From Rule"); // TODO: translate
			m.setValidFrom(m_dateTo);
			m.setValidTo(m_dateTo);
			m.setIsRegistered(isRegistered);
			
			m.setHR_Concept_Category_ID(c.getHR_Concept_Category_ID());
			m.setHR_Department_ID(employee.getHR_Department_ID());
			m.setHR_Job_ID(employee.getHR_Job_ID());
			m.setIsRegistered(c.isRegistered());
			m.setC_Activity_ID(employee.getC_Activity_ID());
			// m.setProcessed(true);  ??			
			
			m.saveEx();
		} 
		catch(Exception e)
		{
			s_log.warning(e.getMessage());
		}
	} // setConcept

	
	/**
	 * Helper Method : get the value of the concept
	 * @param pconcept
	 * @return
	 */
	public double getConcept (String pconcept)
	{
		MHRConcept concept = MHRConcept.forValue(getCtx(), pconcept.trim());
		MHRProcess_ConceptTest conceptTest = new MHRProcess_ConceptTest(getCtx(), 0, null);
		
		conceptTest.setC_BPartner_ID(getC_BPartner_ID());
		
		conceptTest.setHR_Payroll_ID(getHR_Payroll_ID());
		
		conceptTest.setHR_Period_ID(getHR_Period_ID());
		conceptTest.setM_HR_Concept_ID(concept.get_ID());
		
		conceptTest.setScriptText("");
		conceptTest.setHR_Department_ID(0);
		return Double.valueOf(testConcept(pconcept).toString());
	} // getConcept

	
	/**
	 * Helper Method : Get Attribute [get Attribute to search key concept ]
	 * @param pConcept - Value to Concept
	 * @return	Amount of concept, applying to employee
	 */ 
	public double getAttribute (String pConcept)
	{
		MHRConcept concept = MHRConcept.forValue(getCtx(), pConcept);
		if (concept == null)
			return 0;

		ArrayList<Object> params = new ArrayList<Object>();
		StringBuilder whereClause = new StringBuilder();
		// check ValidFrom:
		whereClause.append(MHRAttribute.COLUMNNAME_ValidFrom + "<=?");
		params.add(m_dateFrom);
		//check client
		whereClause.append(" AND AD_Client_ID = ?");
		params.add(getAD_Client_ID());
		//check concept
		whereClause.append(" AND EXISTS (SELECT 1 FROM HR_Concept c WHERE c.HR_Concept_ID=HR_Attribute.HR_Concept_ID AND HR_Attribute.IsActive='Y' " 
				+ " AND c.Value = ?)");
		params.add(pConcept);
		//
		if (!concept.getType().equals(MHRConcept.TYPE_Information))
		{
			whereClause.append(" AND " + MHRAttribute.COLUMNNAME_C_BPartner_ID + " = ?");
			params.add(getC_BPartner_ID());
		}
		// LVE Localización Venezuela
		// when is employee, it is necessary to check if the organization of the employee is equal to that of the attribute
		if (concept.isEmployee()){
			whereClause.append(" AND ( " + MHRAttribute.COLUMNNAME_AD_Org_ID + "=? OR " + MHRAttribute.COLUMNNAME_AD_Org_ID + "= 0 )");
			params.add(getAD_Org_ID());
		}

		MHRAttribute attribute = new Query(getCtx(), MHRAttribute.Table_Name, whereClause.toString(), get_TrxName())
		.setParameters(params)
		.setOrderBy(MHRAttribute.COLUMNNAME_ValidFrom + " DESC")
		.first();
		if (attribute == null)
			return 0.0;

		// if column type is Quantity return quantity
		if (concept.getColumnType().equals(MHRConcept.COLUMNTYPE_Quantity))
			return attribute.getQty().doubleValue();

		// if column type is Amount return amount
		if (concept.getColumnType().equals(MHRConcept.COLUMNTYPE_Amount))
			return attribute.getAmount().doubleValue();

		//something else
		return 0.0; //TODO throw exception ?? 
	} // getAttribute


	public double getAttribute (String pConcept,String pDescription)
	{
		MHRConcept concept = MHRConcept.forValue(getCtx(), pConcept);
		if (concept == null)
			return 0;

		ArrayList<Object> params = new ArrayList<Object>();
		StringBuilder whereClause = new StringBuilder();
		// check ValidFrom:
		whereClause.append(MHRAttribute.COLUMNNAME_ValidFrom + "<=?");
		params.add(m_dateFrom);
		//check client
		whereClause.append(" AND AD_Client_ID = ?");
		params.add(getAD_Client_ID());
		//check concept
		whereClause.append(" AND EXISTS (SELECT 1 FROM HR_Concept c WHERE c.HR_Concept_ID=HR_Attribute.HR_Concept_ID AND HR_Attribute.IsActive='Y' " 
				+ " AND c.Value = ? AND HR_Attribute.Description = ?)");
		params.add(pConcept);
		params.add(pDescription);
		//
		if (!concept.getType().equals(MHRConcept.TYPE_Information))
		{
			whereClause.append(" AND " + MHRAttribute.COLUMNNAME_C_BPartner_ID + " = ?");
			params.add(getC_BPartner_ID());
		}
		// LVE Localización Venezuela
		// when is employee, it is necessary to check if the organization of the employee is equal to that of the attribute
		if (concept.isEmployee()){
			whereClause.append(" AND ( " + MHRAttribute.COLUMNNAME_AD_Org_ID + "=? OR " + MHRAttribute.COLUMNNAME_AD_Org_ID + "= 0 )");
			params.add(getAD_Org_ID());
		}

		MHRAttribute attribute = new Query(getCtx(), MHRAttribute.Table_Name, whereClause.toString(), get_TrxName())
		.setParameters(params)
		.setOrderBy(MHRAttribute.COLUMNNAME_ValidFrom + " DESC")
		.first();
		if (attribute == null)
			return 0.0;

		// if column type is Quantity return quantity
		if (concept.getColumnType().equals(MHRConcept.COLUMNTYPE_Quantity))
			return attribute.getQty().doubleValue();

		// if column type is Amount return amount
		if (concept.getColumnType().equals(MHRConcept.COLUMNTYPE_Amount))
			return attribute.getAmount().doubleValue();

		//something else
		return 0.0; //TODO throw exception ?? 
	} // getAttribute
	// LVE Localización Venezuela - RTSC: 14/03/2011
	/**
	 * Helper Method : Get Attribute [get Attribute to search key concept and date ]
	 * @param pConcept - Value to Concept
	 * @param date
	 * @return	Amount of concept, applying to employee
	 */ 
	public double getAttribute (String pConcept, Timestamp date)
	{
		MHRConcept concept = MHRConcept.forValue(getCtx(), pConcept);
		if (concept == null)
			return 0;
		ArrayList<Object> params = new ArrayList<Object>();
		StringBuilder whereClause = new StringBuilder();
		//check client
		whereClause.append(" AD_Client_ID = ?");
		params.add(getAD_Client_ID());
		//check concept
		whereClause.append(" AND EXISTS (SELECT 1 FROM HR_Concept c WHERE c.HR_Concept_ID=HR_Attribute.HR_Concept_ID  AND HR_Attribute.IsActive='Y' AND c.Value = ? AND ((? >= HR_Attribute.validfrom AND HR_Attribute.validto IS NULL) OR (? >= HR_Attribute.validfrom AND ? <= HR_Attribute.validto)))");
		params.add(pConcept);
		params.add(date);
		params.add(date);
		params.add(date);
		//
		if (!concept.getType().equals(MHRConcept.TYPE_Information))
		{
			whereClause.append(" AND " + MHRAttribute.COLUMNNAME_C_BPartner_ID + " = ?");
			params.add(getC_BPartner_ID());
		}
		// LVE Localización Venezuela
		// when is employee, it is necessary to check if the organization of the employee is equal to that of the attribute
		if (concept.isEmployee()){
			whereClause.append(" AND ( " + MHRAttribute.COLUMNNAME_AD_Org_ID + "=? OR " + MHRAttribute.COLUMNNAME_AD_Org_ID + "= 0 )");
			params.add(getAD_Org_ID());
		}
		log.info("********** Where Clause **********");
		log.info(whereClause.toString());
		log.info("********** Params ****************");
		log.info(params.toString());
		log.info("**********");
		MHRAttribute attribute = new Query(getCtx(), MHRAttribute.Table_Name, whereClause.toString(), get_TrxName())
		.setParameters(params)
		.setOrderBy(MHRAttribute.COLUMNNAME_ValidFrom + " DESC")
		.first();
		if (attribute == null)
			return 0.0;

		// if column type is Quantity return quantity
		if (concept.getColumnType().equals(MHRConcept.COLUMNTYPE_Quantity))
			return attribute.getQty().doubleValue();

		// if column type is Amount return amount
		if (concept.getColumnType().equals(MHRConcept.COLUMNTYPE_Amount))
			return attribute.getAmount().doubleValue();

		//something else
		return 0.0; //TODO throw exception ?? 
	} // getAttribute

	// LVE Localización Venezuela - JCRA: 14/03/2011
    /**
	* Helper Method : Get Attribute [get Attribute to search key concept and date ] 
	* @param pConcept - Value to Concept
	* @param date1
	* @param date2
	* @return	Amount of concept, applying to employee
	*/ 
	public double getAttribute (String pConcept, Timestamp date1, Timestamp date2)
	{
		MHRConcept concept = MHRConcept.forValue(getCtx(), pConcept);
		if (concept == null)
			return 0;
		ArrayList<Object> params = new ArrayList<Object>();
		StringBuilder whereClause = new StringBuilder();
		// check ValidFrom:
		whereClause.append(MHRAttribute.COLUMNNAME_ValidFrom + "<=?");
		params.add(date2);
		//check client
		whereClause.append(" AND AD_Client_ID = ?");
		params.add(getAD_Client_ID());
		//check concept
		whereClause.append(" AND EXISTS (SELECT 1 FROM HR_Concept c WHERE c.HR_Concept_ID=HR_Attribute.HR_Concept_ID AND HR_Attribute.IsActive='Y' AND c.Value = ? " 
		+ " AND (HR_Attribute.validto IS NULL OR HR_Attribute.validto >= ?) )");
		params.add(pConcept);
		params.add(date1);
		//
		if (!concept.getType().equals(MHRConcept.TYPE_Information))
		{
			whereClause.append(" AND " + MHRAttribute.COLUMNNAME_C_BPartner_ID + " = ?");
			params.add(getC_BPartner_ID());
		}
		// LVE Localización Venezuela
		// when is employee, it is necessary to check if the organization of the employee is equal to that of the attribute
		if (concept.isEmployee()){
			whereClause.append(" AND ( " + MHRAttribute.COLUMNNAME_AD_Org_ID + "=? OR " + MHRAttribute.COLUMNNAME_AD_Org_ID + "= 0 )");
			params.add(getAD_Org_ID());
		}
		
		MHRAttribute attribute = new Query(getCtx(), MHRAttribute.Table_Name, whereClause.toString(), get_TrxName())
		.setParameters(params)
		.setOrderBy(MHRAttribute.COLUMNNAME_ValidFrom + " DESC")
		.first();
		if (attribute == null)
			return 0.0;
	
		// if column type is Quantity return quantity
		if (concept.getColumnType().equals(MHRConcept.COLUMNTYPE_Quantity))
			return attribute.getQty().doubleValue();
	
		// if column type is Amount return amount
		if (concept.getColumnType().equals(MHRConcept.COLUMNTYPE_Amount))
			return attribute.getAmount().doubleValue();
	
		//something else
		return 0.0; //TODO throw exception ?? 
	} // getAttribute
	
	/**
	* Helper Method : Get Attribute [get Attribute to search key concept and date ] 
	* @param pConcept - Value to Concept
	* @param date1
	* @param date2
	* @param Job
	* @return	Amount of concept, applying to employee
	*/ 
	public double getAttribute (String pConcept, Timestamp date1, Timestamp date2,int pJob)
	{
		MHRConcept concept = MHRConcept.forValue(getCtx(), pConcept);
		if (concept == null)
			return 0;
		ArrayList<Object> params = new ArrayList<Object>();
		StringBuilder whereClause = new StringBuilder();
		// check ValidFrom:
		whereClause.append(MHRAttribute.COLUMNNAME_ValidFrom + "<=?");
		params.add(date2);
		//check client
		whereClause.append(" AND AD_Client_ID = ?");
		params.add(getAD_Client_ID());
		//check concept
		whereClause.append(" AND EXISTS (SELECT 1 FROM HR_Concept c WHERE c.HR_Concept_ID=HR_Attribute.HR_Concept_ID AND HR_Attribute.IsActive='Y' AND c.Value = ? " 
		+ " AND (HR_Attribute.validto IS NULL OR HR_Attribute.validto >= ?) )");
		params.add(pConcept);
		params.add(date1);
		//
		if (!concept.getType().equals(MHRConcept.TYPE_Information))
		{
			whereClause.append(" AND " + MHRAttribute.COLUMNNAME_C_BPartner_ID + " = ?");
			params.add(getC_BPartner_ID());
		}
		// LVE Localización Venezuela
		// when is employee, it is necessary to check if the organization of the employee is equal to that of the attribute
		if (concept.isEmployee()){
			whereClause.append(" AND ( " + MHRAttribute.COLUMNNAME_AD_Org_ID + "=? OR " + MHRAttribute.COLUMNNAME_AD_Org_ID + "= 0 )");
			params.add(getAD_Org_ID());
		}
		if (pJob != 0){
			whereClause.append(" AND "+ MHRAttribute.COLUMNNAME_HR_Job_ID + " = ? ");
			params.add(pJob);
		}
		
		MHRAttribute attribute = new Query(getCtx(), MHRAttribute.Table_Name, whereClause.toString(), get_TrxName())
		.setParameters(params)
		.setOrderBy(MHRAttribute.COLUMNNAME_ValidFrom + " DESC")
		.first();
		if (attribute == null)
			return 0.0;
	
		// if column type is Quantity return quantity
		if (concept.getColumnType().equals(MHRConcept.COLUMNTYPE_Quantity))
			return attribute.getQty().doubleValue();
	
		// if column type is Amount return amount
		if (concept.getColumnType().equals(MHRConcept.COLUMNTYPE_Amount))
			return attribute.getAmount().doubleValue();
	
		//something else
		return 0.0; //TODO throw exception ?? 
	} // getAttribute
	
    /**
	* Helper Method : Get Attribute [get Attribute to search key concept and date ] 
	* @param pConcept - Value to Concept
	* @param date1
	* @param date2
	* @return	SUM(Amount) of concept, applying to employee
	*/ 
	public double getAttributeSUM (String pConcept, Timestamp date1, Timestamp date2)
	{
		MHRConcept concept = MHRConcept.forValue(getCtx(), pConcept);
		if (concept == null)
			return 0;
		ArrayList<Object> params = new ArrayList<Object>();
		StringBuilder whereClause = new StringBuilder();
		// check ValidFrom:
		whereClause.append(MHRAttribute.COLUMNNAME_ValidFrom + "<=?");
		params.add(date2);
		//check client
		whereClause.append(" AND AD_Client_ID = ?");
		params.add(getAD_Client_ID());
		//check concept
		whereClause.append(" AND EXISTS (SELECT 1 FROM HR_Concept c WHERE c.HR_Concept_ID=HR_Attribute.HR_Concept_ID AND HR_Attribute.IsActive='Y' AND c.Value = ? " 
		+ " AND (HR_Attribute.validto IS NULL OR HR_Attribute.validto >= ?) )");
		params.add(pConcept);
		params.add(date1);
		//
		if (!concept.getType().equals(MHRConcept.TYPE_Information))
		{
			whereClause.append(" AND " + MHRAttribute.COLUMNNAME_C_BPartner_ID + " = ?");
			params.add(getC_BPartner_ID());
		}
		// LVE Localización Venezuela
		// when is employee, it is necessary to check if the organization of the employee is equal to that of the attribute
		if (concept.isEmployee()){
			whereClause.append(" AND ( " + MHRAttribute.COLUMNNAME_AD_Org_ID + "=? OR " + MHRAttribute.COLUMNNAME_AD_Org_ID + "= 0 )");
			params.add(getAD_Org_ID());
		}
		
		
	
		// if column type is Quantity return quantity
		if (concept.getColumnType().equals(MHRConcept.COLUMNTYPE_Quantity)){
			BigDecimal quantity = new Query(getCtx(), MHRAttribute.Table_Name, whereClause.toString(), get_TrxName())
			.setParameters(params)
			.setOrderBy(MHRAttribute.COLUMNNAME_ValidFrom + " DESC")
			.sum("Qty");
			
			if (quantity == null)
				return 0.0;
			else
				return quantity.doubleValue();
		}
			
	
		// if column type is Amount return amount
		if (concept.getColumnType().equals(MHRConcept.COLUMNTYPE_Amount)){
			BigDecimal amount = new Query(getCtx(), MHRAttribute.Table_Name, whereClause.toString(), get_TrxName())
			.setParameters(params)
			.setOrderBy(MHRAttribute.COLUMNNAME_ValidFrom + " DESC")
			.sum("Amount");
		
			if (amount == null)
				return 0.0;
			else
				return amount.doubleValue();
		}
		//something else
		return 0.0; //TODO throw exception ?? 
	} // getAttribute	
	
	
	/**
	 * 	Helper Method : Get Attribute [get Attribute to search key concept ]
	 *  @param conceptValue
	 *  @return ServiceDate
	 */ 
	public Timestamp getAttributeDate (String conceptValue, Timestamp date)
	{
		MHRConcept concept = MHRConcept.forValue(getCtx(), conceptValue);
		if (concept == null)
			return null;

		ArrayList<Object> params = new ArrayList<Object>();
		StringBuilder whereClause = new StringBuilder();
		//check client
		whereClause.append("AD_Client_ID = ?");
		params.add(getAD_Client_ID());
		//check concept
		whereClause.append(" AND EXISTS (SELECT 1 FROM HR_Concept c WHERE c.HR_Concept_ID=HR_Attribute.HR_Concept_ID AND c.Value = ? AND ((? >= HR_Attribute.validfrom AND HR_Attribute.validto IS NULL) OR (? >= HR_Attribute.validfrom AND ? <= HR_Attribute.validto)))");
		params.add(conceptValue);
		params.add(date);
		params.add(date);
		params.add(date);
		//
		if (!concept.getType().equals(MHRConcept.TYPE_Information))
		{
			whereClause.append(" AND " + MHRAttribute.COLUMNNAME_C_BPartner_ID + " = ?");
			params.add(getC_BPartner_ID());
		}
         // LVE Localización Venezuela
		// when is employee, it is necessary to check if the organization of the employee is equal to that of the attribute
		if (concept.isEmployee()){
			whereClause.append(" AND ( " + MHRAttribute.COLUMNNAME_AD_Org_ID + "=? OR " + MHRAttribute.COLUMNNAME_AD_Org_ID + "= 0 )");
			params.add(getAD_Org_ID());
		}
		
		MHRAttribute attribute = new Query(getCtx(), MHRAttribute.Table_Name, whereClause.toString(), get_TrxName())
		.setParameters(params)
		.setOrderBy(MHRAttribute.COLUMNNAME_ValidFrom + " DESC")
		.first();
		if (attribute == null)
			return null;

		return attribute.getServiceDate();
	} // getAttributeDate


	/**
	 * 	Helper Method : Get Attribute [get Attribute to search key concept ]
	 *  @param conceptValue
	 *  @return ServiceDate
	 */ 
	public Timestamp getAttributeDate (String conceptValue, String pRegion)
	{
		MHRConcept concept = MHRConcept.forValue(getCtx(), conceptValue);
		if (concept == null)
			return null;

		ArrayList<Object> params = new ArrayList<Object>();
		StringBuilder whereClause = new StringBuilder();
		//check client
		whereClause.append("AD_Client_ID = ?");
		params.add(getAD_Client_ID());
		//check concept
		whereClause.append(" AND EXISTS (SELECT 1 FROM HR_Concept c WHERE c.HR_Concept_ID=HR_Attribute.HR_Concept_ID AND c.Value = ? "
				+ "AND HR_Region = ?)");
		params.add(conceptValue);
		params.add(pRegion);
		
		//
		if (!concept.getType().equals(MHRConcept.TYPE_Information))
		{
			whereClause.append(" AND " + MHRAttribute.COLUMNNAME_C_BPartner_ID + " = ?");
			params.add(getC_BPartner_ID());
		}
         // LVE Localización Venezuela
		// when is employee, it is necessary to check if the organization of the employee is equal to that of the attribute
		if (concept.isEmployee()){
			whereClause.append(" AND ( " + MHRAttribute.COLUMNNAME_AD_Org_ID + "=? OR " + MHRAttribute.COLUMNNAME_AD_Org_ID + "= 0 )");
			params.add(getAD_Org_ID());
		}
		
		MHRAttribute attribute = new Query(getCtx(), MHRAttribute.Table_Name, whereClause.toString(), get_TrxName())
		.setParameters(params)
		.setOrderBy(MHRAttribute.COLUMNNAME_ValidFrom + " DESC")
		.first();
		if (attribute == null)
			return null;

		return attribute.getServiceDate();
	} // getAttributeDate
	/**
	 * 	Helper Method : Get Attribute [get Attribute to search key concept ]
	 *  @param conceptValue
	 *  @return ServiceDate
	 */ 
	public Timestamp getAttributeDate (String conceptValue)
	{
		MHRConcept concept = MHRConcept.forValue(getCtx(), conceptValue);
		if (concept == null)
			return null;

		ArrayList<Object> params = new ArrayList<Object>();
		StringBuilder whereClause = new StringBuilder();
		//check client
		whereClause.append("AD_Client_ID = ?");
		params.add(getAD_Client_ID());
		//check concept
		whereClause.append(" AND EXISTS (SELECT 1 FROM HR_Concept c WHERE c.HR_Concept_ID=HR_Attribute.HR_Concept_ID" 
				+ " AND c.Value = ?)");
		params.add(conceptValue);
		//
		if (!concept.getType().equals(MHRConcept.TYPE_Information))
		{
			whereClause.append(" AND " + MHRAttribute.COLUMNNAME_C_BPartner_ID + " = ?");
			params.add(getC_BPartner_ID());
		}
         // LVE Localización Venezuela
		// when is employee, it is necessary to check if the organization of the employee is equal to that of the attribute
		if (concept.isEmployee()){
			whereClause.append(" AND ( " + MHRAttribute.COLUMNNAME_AD_Org_ID + "=? OR " + MHRAttribute.COLUMNNAME_AD_Org_ID + "= 0 )");
			params.add(getAD_Org_ID());
		}

		MHRAttribute attribute = new Query(getCtx(), MHRAttribute.Table_Name, whereClause.toString(), get_TrxName())
		.setParameters(params)
		.setOrderBy(MHRAttribute.COLUMNNAME_ValidFrom + " DESC")
		.first();
		if (attribute == null)
			return null;

		return attribute.getServiceDate();
	} // getAttributeDate

	

	/**
	 * 	Helper Method : Get Attribute [get Attribute to search key concept ]
	 *  @param conceptValue
	 *  @return TextMsg
	 */ 
	public String getAttributeString (String conceptValue)
	{
		MHRConcept concept = MHRConcept.forValue(getCtx(), conceptValue);
		if (concept == null)
			return null;

		ArrayList<Object> params = new ArrayList<Object>();
		StringBuilder whereClause = new StringBuilder();
		//check client
		whereClause.append("AD_Client_ID = ?");
		params.add(getAD_Client_ID());
		//check concept
		whereClause.append(" AND EXISTS (SELECT 1 FROM HR_Concept c WHERE c.HR_Concept_ID=HR_Attribute.HR_Concept_ID" 
				+ " AND c.Value = ?)");
		params.add(conceptValue);
		//
		if (!concept.getType().equals(MHRConcept.TYPE_Information))
		{
			whereClause.append(" AND " + MHRAttribute.COLUMNNAME_C_BPartner_ID + " = ?");
			params.add(getC_BPartner_ID());
		}
		// LVE Localización Venezuela
		// when is employee, it is necessary to check if the organization of the employee is equal to that of the attribute
		if (concept.isEmployee()){
			whereClause.append(" AND ( " + MHRAttribute.COLUMNNAME_AD_Org_ID + "=? OR " + MHRAttribute.COLUMNNAME_AD_Org_ID + "= 0 )");
			params.add(getAD_Org_ID());
		}

		MHRAttribute attribute = new Query(getCtx(), MHRAttribute.Table_Name, whereClause.toString(), get_TrxName())
		.setParameters(params)
		.setOrderBy(MHRAttribute.COLUMNNAME_ValidFrom + " DESC")
		.first();
		if (attribute == null){
			
			return null;
		}
		System.out.println(params.toString());
		return attribute.getTextMsg();
	} // getAttributeString
	
	/**
	 * 	Helper Method : Get Attribute [get Attribute to search key concept ]
	 *  @param String conceptValue,Timestamp From,Timestamp To
	 *  @return TextMsg
	 */ 
	public String getAttributeString (String conceptValue,Timestamp from,Timestamp to)
	{
		MHRConcept concept = MHRConcept.forValue(getCtx(), conceptValue);
		if (concept == null)
			return null;

		ArrayList<Object> params = new ArrayList<Object>();
		StringBuilder whereClause = new StringBuilder();
		//check client
		whereClause.append("AD_Client_ID = ?");
		params.add(getAD_Client_ID());
		//check concept
		
		whereClause.append(" AND EXISTS (SELECT 1 FROM HR_Concept c WHERE c.HR_Concept_ID=HR_Attribute.HR_Concept_ID AND HR_Attribute.IsActive='Y' AND c.Value = ? " 
		+ " AND (HR_Attribute.validto IS NULL OR HR_Attribute.validto >= ?) AND HR_Attribute.ValidFrom <=?"+" )");
		params.add(conceptValue);
		params.add(from);
		params.add(to);
		//
		
		
		if (!concept.getType().equals(MHRConcept.TYPE_Information))
		{
			whereClause.append(" AND " + MHRAttribute.COLUMNNAME_C_BPartner_ID + " = ?");
			params.add(getC_BPartner_ID());
		}
		// LVE Localizaci��n Venezuela
		// when is employee, it is necessary to check if the organization of the employee is equal to that of the attribute
		
			whereClause.append(" AND ( " + MHRAttribute.COLUMNNAME_AD_Org_ID + "=? OR " + MHRAttribute.COLUMNNAME_AD_Org_ID + "= 0 )");
			params.add(getAD_Org_ID());
		

		MHRAttribute attribute = new Query(getCtx(), MHRAttribute.Table_Name, whereClause.toString(), get_TrxName())
		.setParameters(params)
		.setOrderBy(MHRAttribute.COLUMNNAME_AD_Org_ID + " DESC")
		.first();
		if (attribute == null){
			System.out.println(whereClause.toString()+",Parmeter"+params.toString());
			return null;
		}
		return attribute.getTextMsg();
	} // getAttributeString
	/**
	 * 	Helper Method : Get the number of days between start and end, in Timestamp format
	 *  @param date1 
	 *  @param date2
	 *  @return no. of days
	 */ 
	public int getDays (Timestamp date1, Timestamp date2)
	{		
		// adds one for the last day
		return org.compiere.util.TimeUtil.getDaysBetween(date1,date2) + 1;
	} // getDays


	/**
	 * 	Helper Method : Get the number of days between start and end, in String format
	 *  @param date1 
	 *  @param date2
	 *  @return no. of days
	 */  
	public  int getDays (String date1, String date2)
	{		
		Timestamp dat1 = Timestamp.valueOf(date1);
		Timestamp dat2 = Timestamp.valueOf(date2);
		return getDays(dat1, dat2);
	}  // getDays

	/**
	 * 	Helper Method : Get Months, Date in Format Timestamp
	 *  @param start
	 *  @param end
	 *  @return no. of month between two dates
	 */ 
	public int getMonths(Timestamp startParam,Timestamp endParam)
	{
		boolean negative = false;
		Timestamp start = startParam;
		Timestamp end = endParam;
		if (end.before(start))
		{
			negative = true;
			Timestamp temp = start;
			start = end;
			end = temp;
		}

		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(start);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		GregorianCalendar calEnd = new GregorianCalendar();

		calEnd.setTime(end);
		calEnd.set(Calendar.HOUR_OF_DAY, 0);
		calEnd.set(Calendar.MINUTE, 0);
		calEnd.set(Calendar.SECOND, 0);
		calEnd.set(Calendar.MILLISECOND, 0);

		if (cal.get(Calendar.YEAR) == calEnd.get(Calendar.YEAR))
		{
			if (negative)
				return (calEnd.get(Calendar.MONTH) - cal.get(Calendar.MONTH)) * -1;
			return calEnd.get(Calendar.MONTH) - cal.get(Calendar.MONTH);
		}

		//	not very efficient, but correct
		int counter = 0;
		while (calEnd.after(cal))
		{
			cal.add (Calendar.MONTH, 1);
			counter++;
		}
		if (negative)
			return counter * -1;
		return counter;
	} // getMonths


	/**
	 * Helper Method : Concept for a range from-to in periods.
	 * Periods with values of 0 -1 1, etc. actual previous one period, next period
	 * 0 corresponds to actual period.
	 * @param conceptValue concept key(value)
	 * @param periodFrom the search is done by the period value, it helps to search from previous years
	 * @param periodTo
	 */
	public double getConcept (String conceptValue, int periodFrom, int periodTo)
	{
		return getConcept(conceptValue, null, periodFrom,periodTo);
	} // getConcept

	/**
	 *  Helper Method : Concept by range from-to in periods from a different payroll
	 *  periods with values 0 -1 1, etc. actual previous one period, next period
	 *  0 corresponds to actual period
	 *  @param conceptValue 
	 *  @param pFrom 
	 *  @param pTo the search is done by the period value, it helps to search from previous years
	 *  @param payrollValue is the value of the payroll.
	 */
	public double getConcept(String conceptValue, String payrollValue,int periodFrom,int periodTo)
	{
		int payroll_id;
		if (payrollValue == null)
		{
			payroll_id = getHR_Payroll_ID();
		}
		else
		{
			payroll_id = MHRPayroll.forValue(getCtx(), payrollValue).get_ID();
		}

		MHRConcept concept = MHRConcept.forValue(getCtx(), conceptValue);
		if (concept == null)
			return 0.0;
		//
		// Detect field name
		final String fieldName;
		if (MHRConcept.COLUMNTYPE_Quantity.equals(concept.getColumnType()))
		{
			fieldName = MHRMovement.COLUMNNAME_Qty;
		}
		else if (MHRConcept.COLUMNTYPE_Amount.equals(concept.getColumnType()))
		{
			fieldName = MHRMovement.COLUMNNAME_Amount;
		}
		else
		{
			return 0; // TODO: throw exception?
		}
		//
		MHRPeriod p = MHRPeriod.get(getCtx(), getHR_Period_ID());
		ArrayList<Object> params = new ArrayList<Object>();
		StringBuilder whereClause = new StringBuilder();
		//check client
		whereClause.append("AD_Client_ID = ?");
		params.add(getAD_Client_ID());
		//check concept
		whereClause.append(" AND " + MHRMovement.COLUMNNAME_HR_Concept_ID + "=?");
		params.add(concept.get_ID());
		//check partner
		whereClause.append(" AND " + MHRMovement.COLUMNNAME_C_BPartner_ID  + "=?");
		params.add(m_C_BPartner_ID);
		//
		//check process and payroll
		whereClause.append(" AND EXISTS (SELECT 1 FROM HR_Process p"
				+" INNER JOIN HR_Period pr ON (pr.HR_Period_id=p.HR_Period_ID)"
				+" WHERE HR_Movement.HR_Process_ID = p.HR_Process_ID" 
				+" AND p.HR_Payroll_ID=?");

		params.add(payroll_id);
		if (periodFrom < 0)
		{
			whereClause.append(" AND pr.PeriodNo >= ?");
			params.add(p.getPeriodNo() +periodFrom);
		}
		if (periodTo > 0)
		{
			whereClause.append(" AND pr.PeriodNo <= ?");
			params.add(p.getPeriodNo() +periodTo);
		}
		whereClause.append(")");
		//
		StringBuilder sql = new StringBuilder("SELECT COALESCE(SUM(").append(fieldName).append("),0) FROM ").append(MHRMovement.Table_Name)
		.append(" WHERE ").append(whereClause);
		BigDecimal value = DB.getSQLValueBDEx(get_TrxName(), sql.toString(), params);
		return value.doubleValue();

	} // getConcept

	/**
	 * Helper Method: gets Concept value of a payrroll between 2 dates
	 * @param pConcept
	 * @param pPayrroll
	 * @param from
	 * @param to
	 * */
	public double getConcept (String conceptValue, String payrollValue,Timestamp from,Timestamp to)
	{
		int payroll_id;
		if (payrollValue == null)
		{
			payroll_id = getHR_Payroll_ID();
		}
		else
		{
			payroll_id = MHRPayroll.forValue(getCtx(), payrollValue).get_ID();
		}
		
		MHRConcept concept = MHRConcept.forValue(getCtx(), conceptValue);
		if (concept == null)
			return 0.0;
		//
		// Detect field name
		final String fieldName;
		if (MHRConcept.COLUMNTYPE_Quantity.equals(concept.getColumnType()))
		{
			fieldName = MHRMovement.COLUMNNAME_Qty;
		}
		else if (MHRConcept.COLUMNTYPE_Amount.equals(concept.getColumnType()))
		{
			fieldName = MHRMovement.COLUMNNAME_Amount;
		}
		else
		{
			return 0; // TODO: throw exception?
		}
		//
		ArrayList<Object> params = new ArrayList<Object>();
		StringBuilder whereClause = new StringBuilder();
		//check client
		whereClause.append("AD_Client_ID = ?");
		params.add(getAD_Client_ID());
		//check concept
		whereClause.append(" AND " + MHRMovement.COLUMNNAME_HR_Concept_ID + "=?");
		params.add(concept.get_ID());
		//check partner
		whereClause.append(" AND " + MHRMovement.COLUMNNAME_C_BPartner_ID  + "=?");
		params.add(m_C_BPartner_ID);
		//Adding dates 
		whereClause.append(" AND validTo BETWEEN ? AND ?");
		params.add(from);
		params.add(to);
		//
		//check process and payroll
		whereClause.append(" AND EXISTS (SELECT 1 FROM HR_Process p"
							+" INNER JOIN HR_Period pr ON (pr.HR_Period_id=p.HR_Period_ID)"
							+" WHERE HR_Movement.HR_Process_ID = p.HR_Process_ID" 
							+" AND p.HR_Payroll_ID=?");

		params.add(payroll_id);
		
		whereClause.append(")");
		//
		StringBuilder sql = new StringBuilder("SELECT COALESCE(SUM(").append(fieldName).append("),0) FROM ").append(MHRMovement.Table_Name)
								.append(" WHERE ").append(whereClause);
		BigDecimal value = DB.getSQLValueBDEx(get_TrxName(), sql.toString(), params);
		return value.doubleValue();
		
	} // getConcept
	
	public double getConceptGroup (String pconcept)
	{
		final MHRConceptCategory category = MHRConceptCategory.forValue(getCtx(), pconcept);
		if (category == null)
		{
			return 0.0; // TODO: need to throw exception ?
		}
		//
		double value = 0.0;
		for(MHRPayrollConcept pc : linesConcept)
		{
			MHRConcept con = MHRConcept.get(getCtx(), pc.getHR_Concept_ID());
			if(con.getHR_Concept_Category_ID() == category.get_ID())
			{
				value += getConcept(con.getValue());
				
			}
		}
		return value;
	} // getConceptGroup
	/** TODO QSS Reviewme
	 * Helper Method: gets Concept value of payrroll(s) between 2 dates
	 * if payrollValue is null then sum all payrolls between 2 dates
	 * if dates range are null then set them based on first and last day of period
	 * @param pConcept
	 * @param from
	 * @param to
	 * */
	public double getConceptRangeOfPeriod (String conceptValue, String payrollValue, String dateFrom, String dateTo)
	{
		
		int payroll_id = -1;
		if (payrollValue == null)
		{
			// payroll_id = getHR_Payroll_ID();
			payroll_id = 0; // all payrrolls
		}
		else
		{
			payroll_id = MHRPayroll.forValue(getCtx(), payrollValue).get_ID();
		}
				
		MHRConcept concept = MHRConcept.forValue(getCtx(), conceptValue);
		
		if (concept == null)
			return 0.0;
		
		Timestamp from = null;
		Timestamp to = null;

		if (dateFrom != null)
			from = Timestamp.valueOf(dateFrom);
		if (dateTo != null)
			to = Timestamp.valueOf(dateTo);
		
		// Detect field name
		final String fieldName;
		if (MHRConcept.COLUMNTYPE_Quantity.equals(concept.getColumnType()))
		{
			fieldName = MHRMovement.COLUMNNAME_Qty;
		}
		else if (MHRConcept.COLUMNTYPE_Amount.equals(concept.getColumnType()))
		{
			fieldName = MHRMovement.COLUMNNAME_Amount;
		}
		else
		{
			return 0; // TODO: throw exception?
		}
		//
		MHRPeriod p = MHRPeriod.get(getCtx(), getHR_Period_ID());
		ArrayList<Object> params = new ArrayList<Object>();
		StringBuffer whereClause = new StringBuffer();
		//check client
		whereClause.append("AD_Client_ID = ?");
		params.add(getAD_Client_ID());
		//check concept
		whereClause.append(" AND " + MHRMovement.COLUMNNAME_HR_Concept_ID + "=?");
		params.add(concept.get_ID());
		//check partner
		whereClause.append(" AND " + MHRMovement.COLUMNNAME_C_BPartner_ID  + "=?");
		params.add(getC_BPartner_ID());
		//Adding Organization
		whereClause.append(" AND ( " + MHRMovement.COLUMNNAME_AD_Org_ID + "=? OR " + MHRMovement.COLUMNNAME_AD_Org_ID + "= 0 )");
		params.add(getAD_Org_ID());
		//Adding dates 
		whereClause.append(" AND validTo BETWEEN ? AND ?");
		if (from == null)
			from = getFirstDayOfPeriod(p.getHR_Period_ID());
		if (to == null)
			to = getLastDayOfPeriod(p.getHR_Period_ID());
		params.add(from);
		params.add(to);
		//
		// check process and payroll
		if (payroll_id > 0) {
			whereClause.append(" AND EXISTS (SELECT 1 FROM HR_Process p"
								+" INNER JOIN HR_Period pr ON (pr.HR_Period_id=p.HR_Period_ID)"
								+" WHERE HR_Movement.HR_Process_ID = p.HR_Process_ID"
								+" AND p.HR_Payroll_ID=?");
			
			params.add(payroll_id);
			whereClause.append(")");
		//
		}
		StringBuffer sql = new StringBuffer("SELECT COALESCE(SUM(").append(fieldName).append("),0) FROM ").append(MHRMovement.Table_Name)
								.append(" WHERE ").append(whereClause);
		BigDecimal value = DB.getSQLValueBDEx(get_TrxName(), sql.toString(), params);
		return value.doubleValue();
		
	} // getConceptRangeOfPeriod

	/** Helper Method: gets Commission summary value of history between 2 dates
	 * if dates range are null then set them based on start and end  of period
	 * @param from
	 * @param to
	 * */
	public double getCommissionHistory (Timestamp from, Timestamp to)
	{
	
		MHRPeriod p = MHRPeriod.get(getCtx(), getHR_Period_ID());
		MHREmployee e = MHREmployee.getActiveEmployee(getCtx(), m_C_BPartner_ID, get_TrxName());
		
		// TODO: throw exception?
		if (from == null)
			from = p.getStartDate();
		if (to == null)
			to = p.getEndDate();
		
		BigDecimal value = DB.getSQLValueBD(null, "SELECT COALESCE(SUM(cr.grandtotal),0) FROM C_Commission c JOIN c_CommissionRun cr on c.C_Commission_ID = cr.C_Commission_ID WHERE c.AD_Client_ID = ? AND c.AD_ORG_ID = ? AND c.C_BPartner_ID = ? AND startdate BETWEEN ? AND ? GROUP BY c.AD_Client_ID, c.AD_ORG_ID, c.C_BPartner_ID", e.getAD_Client_ID(), e.getAD_Org_ID(), m_C_BPartner_ID, from, to);
		
		if (value == null)
			value = Env.ZERO;
		
		return value.doubleValue();
		
	} // getCommissionHistory
	
	/** Helper Method: gets Commission summary value of history between 2 dates
	 * if dates range are null then set them based on start and end  of period
	 * @param bpfilter
	 * */
	public double getFamilyCharge (boolean bpfilter)
	{
		MHREmployee e = MHREmployee.getActiveEmployee(getCtx(), m_C_BPartner_ID, get_TrxName());
		
		ArrayList<Object> params = new ArrayList<Object>();
		StringBuffer whereClause = new StringBuffer();
		
		whereClause.append("AD_Client_ID = ?");
		params.add(e.getAD_Client_ID());
		whereClause.append(" AND AD_Org_ID = ?");
		params.add(e.getAD_Org_ID());
		if (bpfilter) {
			whereClause.append(" AND C_BPartner_ID = ?");
			params.add(m_C_BPartner_ID);
		}
		whereClause.append(" AND IsInPayroll = 'Y' AND IsActive = 'Y'");
		// TODO Needed for Sismode customisation
		// whereClause.append(" AND IsFamilyCharge = 'Y'");		
		StringBuffer sql = new StringBuffer("SELECT COUNT(*) FROM AD_User ").append(" WHERE ").append(whereClause);
		
		BigDecimal value = DB.getSQLValueBDEx(get_TrxName(), sql.toString(), params);
		
		if (value == null)
			value = Env.ZERO;
		
		return value.doubleValue();
		
	} // getFamilyCharge
	

	/**
	 * Helper Method : Attribute that had from some date to another to date,
	 * if it finds just one period it's seen for the attribute of such period 
	 * if there are two or more attributes based on the days
	 * @param ctx
	 * @param vAttribute
	 * @param dateFrom
	 * @param dateTo
	 * @return attribute value
	 */
	public double getAttribute (Properties ctx, String vAttribute, Timestamp dateFrom, Timestamp dateTo)
	{
		// TODO ???
		log.warning("not implemented yet -> getAttribute (Properties, String, Timestamp, Timestamp)");
		return 0;
	} // getAttribute

	/**
	 *  Helper Method : Attribute that had from some period to another to period,
	 *   periods with values 0 -1 1, etc. actual previous one period, next period
	 *  0 corresponds to actual period
	 *  Value of HR_Attribute
	 *  if it finds just one period it's seen for the attribute of such period 
	 *  if there are two or more attributes 
	 *  pFrom and pTo the search is done by the period value, it helps to search 
	 *  from previous year based on the days
	 *  @param ctx
	 *  @param vAttribute
	 *  @param periodFrom
	 *  @param periodTo
	 *  @param pFrom
	 *  @param pTo
	 *  @return attribute value	  
	 */
	public double getAttribute (Properties ctx, String vAttribute, int periodFrom,int periodTo,
			String pFrom,String pTo)
	{
		// TODO ???
		log.warning("not implemented yet -> getAttribute (Properties, String, int, int, String, String)");
		return 0;
	} // getAttribute
	
	
		
	/**
	 * Helper Method : Get AttributeInvoice 
	 * @param pConcept - Value to Concept
	 * @return	C_Invoice_ID, 0 if does't
	 */ 
	public int getAttributeInvoice (String pConcept)
	{
		MHRConcept concept = MHRConcept.forValue(getCtx(), pConcept);
		if (concept == null)
			return 0;
		
		ArrayList<Object> params = new ArrayList<Object>();
		StringBuilder whereClause = new StringBuilder();
		// check ValidFrom:
		whereClause.append(MHRAttribute.COLUMNNAME_ValidFrom + "<=?");
		params.add(m_dateFrom);
		//check client
		whereClause.append(" AND AD_Client_ID = ?");
		params.add(getAD_Client_ID());
		//check concept
		whereClause.append(" AND EXISTS (SELECT 1 FROM HR_Concept c WHERE c.HR_Concept_ID=HR_Attribute.HR_Concept_ID" 
						   + " AND c.Value = ?)");
		params.add(pConcept);
		//
		if (!MHRConcept.TYPE_Information.equals(concept.getType()))
		{
			whereClause.append(" AND " + MHRAttribute.COLUMNNAME_C_BPartner_ID + " = ?");
			params.add(m_C_BPartner_ID);
		}
		// LVE Localización Venezuela
		// when is employee, it is necessary to check if the organization of the employee is equal to that of the attribute
		if (concept.isEmployee()){
			whereClause.append(" AND ( " + MHRAttribute.COLUMNNAME_AD_Org_ID + "=? OR " + MHRAttribute.COLUMNNAME_AD_Org_ID + "= 0 )");
			params.add(getAD_Org_ID());
		}
		
		MHRAttribute attribute = new Query(getCtx(), MHRAttribute.Table_Name, whereClause.toString(), get_TrxName())
		.setParameters(params)
		.setOrderBy(MHRAttribute.COLUMNNAME_ValidFrom + " DESC")
		.first();
		
		if(attribute!=null)
			return (Integer) attribute.get_Value("C_Invoice_ID");
		else
			return 0;
		
	} // getAttributeInvoice
		
	/**
	 * Helper Method : Get AttributeDocType
	 * @param pConcept - Value to Concept
	 * @return	C_DocType_ID, 0 if does't
	 */ 
	public int getAttributeDocType (String pConcept)
	{
		MHRConcept concept = MHRConcept.forValue(getCtx(), pConcept);
		if (concept == null)
			return 0;
		
		ArrayList<Object> params = new ArrayList<Object>();
		StringBuilder whereClause = new StringBuilder();
		// check ValidFrom:
		whereClause.append(MHRAttribute.COLUMNNAME_ValidFrom + "<=?");
		params.add(m_dateFrom);
		//check client
		whereClause.append(" AND AD_Client_ID = ?");
		params.add(getAD_Client_ID());
		//check concept
		whereClause.append(" AND EXISTS (SELECT 1 FROM HR_Concept c WHERE c.HR_Concept_ID=HR_Attribute.HR_Concept_ID" 
						   + " AND c.Value = ?)");
		params.add(pConcept);
		//
		if (!MHRConcept.TYPE_Information.equals(concept.getType()))
		{
			whereClause.append(" AND " + MHRAttribute.COLUMNNAME_C_BPartner_ID + " = ?");
			params.add(m_C_BPartner_ID);
		}
		// LVE Localización Venezuela
		// when is employee, it is necessary to check if the organization of the employee is equal to that of the attribute
		if (concept.isEmployee()){
			whereClause.append(" AND ( " + MHRAttribute.COLUMNNAME_AD_Org_ID + "=? OR " + MHRAttribute.COLUMNNAME_AD_Org_ID + "= 0 )");
			params.add(getAD_Org_ID());
		}
		
		MHRAttribute attribute = new Query(getCtx(), MHRAttribute.Table_Name, whereClause.toString(), get_TrxName())
		.setParameters(params)
		.setOrderBy(MHRAttribute.COLUMNNAME_ValidFrom + " DESC")
		.first();
		
		if(attribute!=null)
			return (Integer) attribute.get_Value("C_DocType_ID");
		else
			return 0;
		 
	} // getAttributeDocType

	/**
	 * Helper Method : Get Attribute [get Attribute to search key concept ]
	 * @param pConcept - Value to Concept
	 * @return	Max Value of concept, applying to employee
	 */ 
	
	public double getAttributeMax (String pConcept, Timestamp date1, Timestamp date2)
	{
		MHRConcept concept = MHRConcept.forValue(getCtx(), pConcept);
		if (concept == null)
			return 0;
		ArrayList<Object> params = new ArrayList<Object>();
		StringBuilder whereClause = new StringBuilder();
		// check ValidFrom:
		whereClause.append(MHRAttribute.COLUMNNAME_ValidFrom + "<=?");
		params.add(date2);
		//check client
		whereClause.append(" AND AD_Client_ID = ?");
		params.add(getAD_Client_ID());
		//check concept
		whereClause.append(" AND EXISTS (SELECT 1 FROM HR_Concept c WHERE c.HR_Concept_ID=HR_Attribute.HR_Concept_ID AND HR_Attribute.IsActive='Y' AND c.Value = ? " 
		+ " AND (HR_Attribute.validto IS NULL OR HR_Attribute.validto >= ?) )");
		params.add(pConcept);
		params.add(date1);
		//
		if (!concept.getType().equals(MHRConcept.TYPE_Information))
		{
			whereClause.append(" AND " + MHRAttribute.COLUMNNAME_C_BPartner_ID + " = ?");
			params.add(getC_BPartner_ID());
		}
		// LVE Localizacion Venezuela
		// when is employee, it is necessary to check if the organization of the employee is equal to that of the attribute
		if (concept.isEmployee()){
			whereClause.append(" AND ( " + MHRAttribute.COLUMNNAME_AD_Org_ID + "=? OR " + MHRAttribute.COLUMNNAME_AD_Org_ID + "= 0 )");
			params.add(getAD_Org_ID());
		}
		
		MHRAttribute attribute = new Query(getCtx(), MHRAttribute.Table_Name, whereClause.toString(), get_TrxName())
		.setParameters(params)
		.setOrderBy(MHRAttribute.COLUMNNAME_ValidFrom + " DESC")
		.first();
		if (attribute == null)
			return 0.0;
	
		BigDecimal bd = (BigDecimal)attribute.get_Value(I_HR_Attribute.COLUMNNAME_MaxValue);
		if (bd == null)
			 return 0.0;
		return bd.doubleValue();
	
		
	} // getAttribute MAX
	/**
	 * Helper Method : Get Attribute [get Attribute to search key concept ]
	 * @param pConcept - Value to Concept
	 * @return	Min Value of concept, applying to employee
	 */ 
	
	public double getAttributeMin (String pConcept, Timestamp date1, Timestamp date2)
	{
		MHRConcept concept = MHRConcept.forValue(getCtx(), pConcept);
		if (concept == null)
			return 0;
		ArrayList<Object> params = new ArrayList<Object>();
		StringBuilder whereClause = new StringBuilder();
		// check ValidFrom:
		whereClause.append(MHRAttribute.COLUMNNAME_ValidFrom + "<=?");
		params.add(date2);
		//check client
		whereClause.append(" AND AD_Client_ID = ?");
		params.add(getAD_Client_ID());
		//check concept
		whereClause.append(" AND EXISTS (SELECT 1 FROM HR_Concept c WHERE c.HR_Concept_ID=HR_Attribute.HR_Concept_ID AND HR_Attribute.IsActive='Y' AND c.Value = ? " 
		+ " AND (HR_Attribute.validto IS NULL OR HR_Attribute.validto >= ?) )");
		params.add(pConcept);
		params.add(date1);
		//
		if (!concept.getType().equals(MHRConcept.TYPE_Information))
		{
			whereClause.append(" AND " + MHRAttribute.COLUMNNAME_C_BPartner_ID + " = ?");
			params.add(getC_BPartner_ID());
		}
		// LVE Localizacion Venezuela
		// when is employee, it is necessary to check if the organization of the employee is equal to that of the attribute
		if (concept.isEmployee()){
			whereClause.append(" AND ( " + MHRAttribute.COLUMNNAME_AD_Org_ID + "=? OR " + MHRAttribute.COLUMNNAME_AD_Org_ID + "= 0 )");
			params.add(getAD_Org_ID());
		}
		
		MHRAttribute attribute = new Query(getCtx(), MHRAttribute.Table_Name, whereClause.toString(), get_TrxName())
		.setParameters(params)
		.setOrderBy(MHRAttribute.COLUMNNAME_ValidFrom + " DESC")
		.first();
		if (attribute == null)
			return 0.0;
	
		BigDecimal bd = (BigDecimal)attribute.get_Value(I_HR_Attribute.COLUMNNAME_MinValue);
		if (bd == null)
			 return 0.0;
		return bd.doubleValue();
	
		
	} // getAttribute Min
	
	
	/**
	 * Helper Method : get days from specific period
	 * @param period
	 * @return no. of days
	 */
	public double getDays (int period)
	{
		/* TODO: This getter could have an error as it's not using the parameter, and it doesn't what is specified in help */
		log.warning("instead of using getDays in the formula it's recommended to use _DaysPeriod+1");
		return Env.getContextAsInt(getCtx(), "_DaysPeriod") + 1;
	} // getDays
	
	/**
	 * Helper Method : get actual period
	 * @param N/A
	 * @return period id
	 */
	public int getPayrollPeriod ()
	{
			
		MHRPeriod p = MHRPeriod.get(getCtx(), getHR_Period_ID());
		return p.getHR_Period_ID();
		
	} // getPayrollPeriod


	/**
	 * Helper Method : get first date from specific period
	 * @param period
	 * @return date from
	 */
	public Timestamp getFirstDayOfPeriod (int period_id)
	{
		
		MHRPeriod period = new MHRPeriod(getCtx(), period_id, get_TrxName());
		Calendar firstdayofperiod = Calendar.getInstance();
		Timestamp datefromofperiod = period.getStartDate();
		firstdayofperiod.setTime(datefromofperiod);
		firstdayofperiod.set(Calendar.DAY_OF_MONTH, 1);
		datefromofperiod.setTime(firstdayofperiod.getTimeInMillis());
		return datefromofperiod;
		
	} // getFirstDayOfPeriod

	/**
	 * Helper Method : Date of the first period of the employee in the system
	 * @param period
	 * @return date from
	 */
	public Timestamp getFirstDayOfFistPeriodOfEmployee (int employeeId,int payrollID)
	{
		Timestamp date = null;
		String sQuery = "Select COALESCE((Select pd.StartDate From HR_Period pd Where pd.HR_Period_ID = "
					+ " COALESCE((Select MIN(pr.HR_Period_ID) from HR_Process pr " 
					+ " JOIN HR_Movement m ON pr.HR_Process_ID = m.HR_Process_ID AND m.C_BPartner_ID = ? "
					+ " where pr.HR_Payroll_ID = ?),0)),now())";
		date = DB.getSQLValueTS(get_TrxName(),sQuery,new Object[] {employeeId,payrollID});

		return date;
		
	} // getFirstDayOfPeriod
	/**
	 * Helper Method : get last date to specific period
	 * @param period
	 * @return date to
	 */
	public Timestamp getLastDayOfPeriod (int period_id)
	{
		
		MHRPeriod period = new MHRPeriod(getCtx(), period_id, get_TrxName());
		Calendar firstdayofperiod = Calendar.getInstance();
		Timestamp datetoofperiod = period.getEndDate();
		firstdayofperiod.setTime(datetoofperiod);
		firstdayofperiod.set(Calendar.DAY_OF_MONTH, firstdayofperiod.getActualMaximum(Calendar.DAY_OF_MONTH));
		datetoofperiod.setTime(firstdayofperiod.getTimeInMillis());
		return datetoofperiod;

	} // getLastDayOfPeriod

	/**
	 * Helper Method : get first year date from specific period
	 * @param period
	 * @return date from
	 */
	public Timestamp getFirstDayOfPeriodYear (int period_id)
	{
		
		MHRPeriod period = new MHRPeriod(getCtx(), period_id, get_TrxName());
		Calendar firstdayofperiod = Calendar.getInstance();
		Timestamp datefromofperiod = period.getStartDate();
		firstdayofperiod.setTime(datefromofperiod);
		firstdayofperiod.set(Calendar.DAY_OF_YEAR, 1);
		datefromofperiod.setTime(firstdayofperiod.getTimeInMillis());
		return datefromofperiod;
		
	} // getFirstDayOfPeriodYear

	/**
	 * Helper Method : get last year date to specific period
	 * @param period
	 * @return date to
	 */
	public Timestamp getLastDayOfPeriodYear (int period_id)
	{
		
		MHRPeriod period = new MHRPeriod(getCtx(), period_id, get_TrxName());
		Calendar firstdayofperiod = Calendar.getInstance();
		Timestamp datetoofperiod = period.getEndDate();
		firstdayofperiod.setTime(datetoofperiod);
		firstdayofperiod.set(Calendar.DAY_OF_YEAR, firstdayofperiod.getActualMaximum(Calendar.DAY_OF_YEAR));
		datetoofperiod.setTime(firstdayofperiod.getTimeInMillis());
		return datetoofperiod;

	} // getLastDayOfPeriodYear

	/**
	 * Helper Method : get first history date from specific period
	 * @param period, servicedate, months
	 * @return date from
	 */
	public Timestamp getFirstDayOfPeriodHistory (int period_id, Timestamp servicedate, Integer months)
	{
		
		if (months == null)
			months = 12;
		
		MHRPeriod period = new MHRPeriod(getCtx(), period_id, get_TrxName());
		Calendar firstdayofhistory = Calendar.getInstance();
		Timestamp datefromofhistory = period.getStartDate();
		firstdayofhistory.setTime(datefromofhistory);
		firstdayofhistory.add(Calendar.MONTH, months * -1);
		firstdayofhistory.set(Calendar.DAY_OF_MONTH, 1);
		datefromofhistory.setTime(firstdayofhistory.getTimeInMillis());
		
		if (servicedate != null && datefromofhistory.before(servicedate))
			return servicedate;
		
		return datefromofhistory;
		
	} // getFirstDayOfPeriodHistory

	/**
	 * Helper Method : get first history date from specific period
	 * @param period, servicedate, months
	 * @return date to
	 */
	public Timestamp getLastDayOfPeriodHistory (int period_id, Timestamp servicedate, Integer months)
	{
		
		if (months == null)
			months = 1;
		
		MHRPeriod period = new MHRPeriod(getCtx(), period_id, get_TrxName());
		Calendar lastdayofhistory = Calendar.getInstance();
		Timestamp datetoofhistory = period.getStartDate();
		lastdayofhistory.setTime(datetoofhistory);
		lastdayofhistory.add(Calendar.MONTH, months * -1);
		lastdayofhistory.set(Calendar.DAY_OF_MONTH, lastdayofhistory.getActualMaximum(Calendar.DAY_OF_MONTH));
		datetoofhistory.setTime(lastdayofhistory.getTimeInMillis());
		
		if (servicedate != null && datetoofhistory.before(servicedate))
			return servicedate;
		
		return datetoofhistory;
		
	} // getLastDayOfPeriodHistory
	
	/**
	 * Helper Method : get timestamp date
	 * @param sdate
	 * @return sdate Timestamp
	 */
	public Timestamp getStringToTimestamp (String sdate)
	{
		return Timestamp.valueOf(sdate);
	} // getStringToTimestamp

	/**
	 * Helper Method : get string date
	 * @param tsdate
	 * @return tsdate String
	 */
	public String getTimestampToString (Timestamp tsdate)
	{
		return tsdate.toString();
	} // getTimestampToString

	public int getM_C_BPartner_ID() {
		return m_C_BPartner_ID;
	}

	public void setM_C_BPartner_ID(int m_C_BPartner_ID) {
		this.m_C_BPartner_ID = m_C_BPartner_ID;
	}

	public int getM_HR_Concept_ID() {
		return m_HR_Concept_ID;
	}

	public void setM_HR_Concept_ID(int m_HR_Concept_ID) {
		this.m_HR_Concept_ID = m_HR_Concept_ID;
	}

	public Timestamp getM_dateFrom() {
		return m_dateFrom;
	}

	public void setM_dateFrom(Timestamp m_dateFrom) {
		this.m_dateFrom = m_dateFrom;
	}

	public Timestamp getM_dateTo() {
		return m_dateTo;
	}

	public void setM_dateTo(Timestamp m_dateTo) {
		this.m_dateTo = m_dateTo;
	}

	public Object getM_description() {
		return m_description;
	}

	public void setM_description(Object m_description) {
		this.m_description = m_description;
	}

	public String getScriptText() {
		return scriptText;
	}

	public void setScriptText(String scriptText) {
		this.scriptText = scriptText;
	}

	public String getM_eval() {
		return m_eval;
	}

	public void setM_eval(String m_eval) {
		this.m_eval = m_eval;
	}
	private void loadEnvironment (Interpreter i)
	{
		if (m_scriptCtx == null)
			return;
		Iterator<String> it = m_scriptCtx.keySet().iterator();
		while (it.hasNext())
		{
			String key = it.next();
			//
			// If key contains ".", skip it - teo_sarca BF [ 2031461 ] 
			if (key.indexOf(".") >= 0)
				continue;
			//
			Object value = m_scriptCtx.get(key);
			try
			{
				if (value instanceof Boolean)
					i.set(key, ((Boolean)value).booleanValue());
				else if (value instanceof Integer)
					i.set(key, ((Integer)value).intValue());
				else if (value instanceof Double)
					i.set(key, ((Double)value).doubleValue());
				else
					i.set(key, value);
			}
			catch (EvalError ee)
			{
				log.log(Level.SEVERE, "", ee);
			}
		}
	}   //  setEnvironment
	public void loadParameter(){
		
		m_scriptCtx.clear();
		m_scriptCtx.put("process", this);
		m_scriptCtx.put("_Process", getHR_Process_ID());
		m_scriptCtx.put("_Period", getHR_Period_ID());
		m_scriptCtx.put("_Payroll", getHR_Payroll_ID());
		m_scriptCtx.put("_Department", getHR_Department_ID());
		
		m_employee = new Query(getCtx(),MHREmployee.Table_Name," C_BPartner_ID =? AND HR_Payroll_ID = ? ",null).setParameters(getC_BPartner_ID(),getHR_Payroll_ID()).first();
		if (m_employee!=null){

			m_scriptCtx.put("_DateStart", m_employee.getStartDate());
			m_scriptCtx.put("_DateEnd", m_employee.getEndDate() == null ? TimeUtil.getDay(2999, 12, 31) : m_employee.getEndDate());
			
		}
		m_scriptCtx.put("_JobEmployee", m_employee.getHR_Job_ID());
		
		m_scriptCtx.put("_C_BPartner_ID", getC_BPartner_ID());
		if (period ==null)
			period = new MHRPeriod(getCtx(), getHR_Period_ID(), get_TrxName());
		if (period != null)
		{
			m_dateFrom = period.getStartDate();
			m_dateTo   = period.getEndDate();
			m_scriptCtx.put("_From", period.getStartDate());
			m_scriptCtx.put("_To", period.getEndDate());
			m_scriptCtx.put("_Days", org.compiere.util.TimeUtil.getDaysBetween(period.getStartDate(),period.getEndDate())+1);
		}
	}
	public void setHR_Payroll_ID (int HR_Payroll_ID)
	{
		if (HR_Payroll_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_HR_Payroll_ID, null);
		else {
			set_ValueNoCheck (COLUMNNAME_HR_Payroll_ID, Integer.valueOf(HR_Payroll_ID));
			linesConcept = MHRPayrollConcept.getPayrollConcepts(this);
		}
		
	}
	/**
	 * Helper Method : Get Attribute [get Attribute to search key concept and
	 * date ]
	 * 
	 * @param pConcept
	 *            - Value to Concept
	 * @param date1
	 * @param date2
	 * @return Amount of concept, applying to employee
	 */
	public double getAttributeDaysBetween(String pConcept, Timestamp date1, Timestamp date2) {
		MHRConcept concept = MHRConcept.forValue(getCtx(), pConcept);
		if (concept == null)
			return 0;
		ArrayList<Object> params = new ArrayList<Object>();
		StringBuilder whereClause = new StringBuilder();
		// check ValidFrom:
		whereClause.append(MHRAttribute.COLUMNNAME_ValidFrom + "<=?");
		params.add(date2);
		// check client
		whereClause.append(" AND AD_Client_ID = ?");
		params.add(getAD_Client_ID());
		// check concept
		whereClause
				.append(" AND EXISTS (SELECT 1 FROM HR_Concept c WHERE c.HR_Concept_ID=HR_Attribute.HR_Concept_ID AND HR_Attribute.IsActive='Y' AND c.Value = ? "
						+ " AND (HR_Attribute.validto IS NULL OR HR_Attribute.validto >= ?) )");
		params.add(pConcept);
		params.add(date1);
		//
		if (!concept.getType().equals(MHRConcept.TYPE_Information)) {
			whereClause.append(" AND " + MHRAttribute.COLUMNNAME_C_BPartner_ID
					+ " = ?");
			params.add(getC_BPartner_ID());
		}
		// LVE Localizacion Venezuela
		// when is employee, it is necessary to check if the organization of the
		// employee is equal to that of the attribute
		// if (concept.isEmployee()){
		whereClause.append(" AND ( " + MHRAttribute.COLUMNNAME_AD_Org_ID
				+ "=? OR " + MHRAttribute.COLUMNNAME_AD_Org_ID + "= 0 )");
		params.add(getAD_Org_ID());
		// }

		MHRAttribute attribute = new Query(getCtx(), MHRAttribute.Table_Name,
				whereClause.toString(), get_TrxName()).setParameters(params)
				// .setOrderBy(MHRAttribute.COLUMNNAME_ValidFrom + " DESC")
				.setOrderBy(MHRAttribute.COLUMNNAME_AD_Org_ID + " DESC")
				.first();
		if (attribute != null){
			if (attribute.getValidTo()!=null)
				return getDays(attribute.getValidFrom(),attribute.getValidTo());
		}
			

		// something else
		return 0.0; // TODO throw exception ??
	} // getAttribute
	
	/**
	 * Helper Method : get the value of the concept
	 * @param pconcept
	 * @return
	 */
	public Timestamp getConceptDate (String pconcept)
	{
		MHRConcept concept = MHRConcept.forValue(getCtx(), pconcept.trim());
		MHRProcess_ConceptTest conceptTest = new MHRProcess_ConceptTest(getCtx(), 0, null);
		
		conceptTest.setC_BPartner_ID(getC_BPartner_ID());
		
		conceptTest.setHR_Payroll_ID(getHR_Payroll_ID());
		
		conceptTest.setHR_Period_ID(getHR_Period_ID());
		conceptTest.setM_HR_Concept_ID(concept.get_ID());
		
		conceptTest.setScriptText("");
		conceptTest.setHR_Department_ID(0);
		String date = testConcept(pconcept).toString();
		if (!date.equals("0"))
			return Timestamp.valueOf(date.toString());
		else
			return null;
	} // getConcept
	
	 /* TODO QSS Reviewme Helper Method: gets Concept value of payrroll(s)
	 * between 2 dates if payrollValue is null then sum all payrolls between 2
	 * dates if dates range are null then set them based on first and last day
	 * of period
	 * 
	 * @param pConcept
	 * @param from
	 * @param to
	 * */
	public double getConceptRangeOfPeriodExcludeFirstAndLast(String conceptValue,
			String payrollValue, String dateFrom, String dateTo) {
		int payroll_id = -1;
		if (payrollValue == null) {
			// payroll_id = getHR_Payroll_ID();
			payroll_id = 0; // all payrrolls
		} else {
			payroll_id = MHRPayroll.forValue(getCtx(), payrollValue).get_ID();
		}

		MHRConcept concept = MHRConcept.forValue(getCtx(), conceptValue);

		if (concept == null)
			return 0.0;

		Timestamp from = null;
		Timestamp to = null;

		if (dateFrom != null)
			from = Timestamp.valueOf(dateFrom);
		if (dateTo != null)
			to = Timestamp.valueOf(dateTo);

		// Detect field name
		final String fieldName;
		if (MHRConcept.COLUMNTYPE_Quantity.equals(concept.getColumnType())) {
			fieldName = MHRMovement.COLUMNNAME_Qty;
		} else if (MHRConcept.COLUMNTYPE_Amount.equals(concept.getColumnType())) {
			fieldName = MHRMovement.COLUMNNAME_Amount;
		} else {
			return 0; // TODO: throw exception?
		}
		//
		MHRPeriod p = MHRPeriod.get(getCtx(), getHR_Period_ID());
		ArrayList<Object> params = new ArrayList<Object>();
		StringBuffer whereClause = new StringBuffer();
		// check client
		whereClause.append("AD_Client_ID = ?");
		params.add(getAD_Client_ID());
		// check concept
		whereClause.append(" AND " + MHRMovement.COLUMNNAME_HR_Concept_ID
				+ "=?");
		params.add(concept.get_ID());
		// check partner
		whereClause.append(" AND " + MHRMovement.COLUMNNAME_C_BPartner_ID
				+ "=?");
		params.add(getC_BPartner_ID());
		// Adding Organization
		whereClause.append(" AND ( " + MHRMovement.COLUMNNAME_AD_Org_ID
				+ "=? OR " + MHRMovement.COLUMNNAME_AD_Org_ID + "= 0 )");
		params.add(getAD_Org_ID());
		// Adding dates
		whereClause.append(" AND validFrom >= ? ");
		params.add(from);
		whereClause.append(" AND validTo <= ? ");
		params.add(to);
	
		if (payroll_id > 0) {
			whereClause
					.append(" AND EXISTS (SELECT 1 FROM HR_Process p"
							+ " INNER JOIN HR_Period pr ON (pr.HR_Period_id=p.HR_Period_ID)"
							+ " WHERE HR_Movement.HR_Process_ID = p.HR_Process_ID"
							+ " AND p.HR_Payroll_ID=?");

			params.add(payroll_id);
			whereClause.append(")");
			//
		}
		StringBuffer sql = new StringBuffer("SELECT COALESCE(SUM(")
				.append(fieldName).append("),0) FROM ")
				.append(MHRMovement.Table_Name).append(" WHERE ")
				.append(whereClause);
		BigDecimal value = DB.getSQLValueBDEx(get_TrxName(), sql.toString(),
				params);
		if (value != null)
			return value.doubleValue();
		else
			return 0;

	} // getConceptRangeOfPeriod
	
}	//	MHRProcess_ConceptTest
