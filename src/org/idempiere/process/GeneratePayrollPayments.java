/***************************************************************************************
* This file is part of Localization Venezuela project                                  *
* Author: Jenny Cecilia Rodriguez - jrodriguez@dcs.net.ve / jennycecilia24@hotmail.com *
* Double Click Sistemas, C.A. (http://www.dcs.net.ve)                                  *
* Date: 13/06/2012
***************************************************************************************/
package org.idempiere.process;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.compiere.model.MBPartner;
import org.compiere.model.MCurrency;
import org.compiere.model.MPayment;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.eevolution.model.MHREmployee;
import org.eevolution.model.MHRMovement;

public class GeneratePayrollPayments  extends SvrProcess {
	
	private int p_LVE_PayrollPayments_ID = 0;
	private Timestamp p_DateTrx = null;
	private int p_C_BPartner_ID = 0;
	private int p_C_BankAccount_ID = 0;
	

	/**	Logger							*/
	CLogger log = CLogger.getCLogger (getClass());

	protected void prepare()
	{
		for (ProcessInfoParameter para : getParameter()) {
			String name = para.getParameterName();
			if (name.equals("LVE_PayrollPayments_ID"))
				p_LVE_PayrollPayments_ID = para.getParameterAsInt();
			else if (name.equals("DateTrx"))
				p_DateTrx = (Timestamp) para.getParameter();
			else if (name.equals("C_BPartner_ID"))
				p_C_BPartner_ID = para.getParameterAsInt();
			else if (name.equals("C_BankAccount_ID"))
				p_C_BankAccount_ID = para.getParameterAsInt();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
	}
	
	/**
	 * Return getCharge
	  * @param trx_name
	  * @param LVE_PayrollPayments_id
	 */	
	public static Integer getCharge(String trx_name, int LVE_PayrollPayments_id) {
		
		String sConsulta = ""
			+ "SELECT c_charge_id "
			+ "FROM   LVE_PayrollPayments "
			+ "WHERE  LVE_PayrollPayments_id = ? ";
	  	Integer ID= DB.getSQLValue(trx_name ,sConsulta,new Object[] {LVE_PayrollPayments_id });
	
		return ID;			   
	}
	
	/**
	 * Return getDocType
	  * @param trx_name
	  * @param LVE_PayrollPayments_id
	 */	
	public static Integer getDocType(String trx_name, int LVE_PayrollPayments_id) {
		
		String sConsulta = ""
			+ "SELECT c_doctype_id "
			+ "FROM   LVE_PayrollPayments "
			+ "WHERE  LVE_PayrollPayments_id = ? ";
	  	Integer ID= DB.getSQLValue(trx_name ,sConsulta,new Object[] {LVE_PayrollPayments_id });
	
		return ID;			   
	}
	
	
	/**
	 * Return getC_Bpartner
	  * @param trx_name
	  * @param LVE_PayrollPayments_id
	 */	
	public static Integer getC_Bpartner(String trx_name, int LVE_PayrollPayments_id) {
		
		String sConsulta = ""
			+ "SELECT c_bpartner_id "
			+ "FROM   LVE_PayrollPayments "
			+ "WHERE  LVE_PayrollPayments_id = ? ";
	  	Integer ID= DB.getSQLValue(trx_name ,sConsulta,new Object[] {LVE_PayrollPayments_id });
	
		return ID;			   
	}
	
	/**
	 * Return get_BPName
	  * @param trx_name
	  * @param c_bpartner_id
	 */	
	public static String get_BPName(String trx_name, int c_bpartner_id) {
		
		String sConsulta = ""
			+ "SELECT name "
			+ "FROM   c_bpartner "
			+ "WHERE  c_bpartner_id = ? ";
	  	String result = DB.getSQLValueString(trx_name ,sConsulta,new Object[] {c_bpartner_id });
	
	  	return result;				   
	}

	/**
	 * Return getHR_ConceptName
	  * @param trx_name
	  * @param c_bpartner_id
	 */	
	public static String getHR_ConceptName(String trx_name, int hr_concept_id) {
		
		String sConsulta = ""
			+ "SELECT value "
			+ "FROM   hr_concept "
			+ "WHERE  hr_concept_id = ? ";
	  	String result = DB.getSQLValueString(trx_name ,sConsulta,new Object[] {hr_concept_id });
	
		return result;			   
	}
	
	protected String doIt() throws Exception
	{
		log.info("LVE_PayrollPayments_ID=" + p_LVE_PayrollPayments_ID 
				+ ", DateTrx=" + p_DateTrx
				+ ", C_BPartner_ID=" + p_C_BPartner_ID
				+ ", C_BankAccount_ID=" + p_C_BankAccount_ID
				);
		int created = 0;
		
		/* Validate mandatory parameters */
		if (   p_LVE_PayrollPayments_ID <= 0
			|| p_C_BankAccount_ID <= 0
			|| p_DateTrx == null) {
    		throw new IllegalArgumentException ("Fill mandatory parameters");
		}
		
		Integer C_BPartner_ID = p_C_BPartner_ID > 0 ? (Integer) p_C_BPartner_ID : getC_Bpartner(get_TrxName(), p_LVE_PayrollPayments_ID);
		
		List<Object> parameters = new ArrayList<Object>();
		StringBuilder whereClause = new StringBuilder("HR_Concept_ID IN (SELECT c.hr_concept_id FROM LVE_ConceptsPayrollPayments c ")
										.append("INNER JOIN LVE_PayrollPayments l ON c.LVE_PayrollPayments_id = l.LVE_PayrollPayments_id ")
										.append("WHERE c.IsActive='Y' AND l.LVE_PayrollPayments_id = ? ) ")
										.append("AND HR_Process_ID in (select hr_process_id from hr_process where docstatus = 'CL' ) ");
		parameters.add(p_LVE_PayrollPayments_ID);
		
		if (C_BPartner_ID > 0) {
			whereClause.append("AND C_BPartner_ID=? ");
			parameters.add(C_BPartner_ID);
		}
		
		whereClause.append("AND C_Payment_ID IS NULL ");
		
		/* Main query */
		List<MHRMovement> movements = new Query(getCtx(), MHRMovement.Table_Name, whereClause.toString(), get_TrxName())
											.setOnlyActiveRecords(true)
											.setParameters(parameters)
											.setOrderBy("C_BPartner_ID,(select seqno from hr_concept where hr_concept.hr_concept_id=hr_movement.hr_concept_id)")
											.list();
		
		MBPartner bp = (C_BPartner_ID!=null) ? new MBPartner(getCtx(), C_BPartner_ID, get_TrxName()):null;
		MPayment payment = null;
		int oldBP = -1;
		
		for (MHRMovement movement : movements) {
			if (C_BPartner_ID==0) 
				bp = (MBPartner) movement.getC_BPartner(); 
			
			if (oldBP != bp.getC_BPartner_ID()) {
				if (payment != null) {
					payment.saveEx();
					addLog("@C_Payment_ID@: " + payment.getDocumentNo());
					created++;
				}
							
				MHREmployee em = MHREmployee.getActiveEmployee(getCtx(), movement.getC_BPartner_ID(), get_TrxName());				
				payment = new MPayment(getCtx(), 0, get_TrxName());
				payment.setC_BPartner_ID(bp.getC_BPartner_ID());
				
				int chargeID = getCharge(get_TrxName(), p_LVE_PayrollPayments_ID);
				int C_DocType_ID = getDocType(get_TrxName(), p_LVE_PayrollPayments_ID);
				int C_Currency_ID =  new Integer(getCtx().getProperty("$C_Currency_ID"));	 //red1 TODO check if this is proper way to get AcctSchema				
				
				String C_BPName = get_BPName(get_TrxName(), movement.getC_BPartner_ID());
				String HR_ConceptName = getHR_ConceptName(get_TrxName(), movement.getHR_Concept_ID());			
				payment.setC_Project_ID(movement.getC_Project_ID());
				payment.setC_Campaign_ID(movement.getC_Campaign_ID());
				payment.setC_Activity_ID(movement.getC_Activity_ID());
				payment.setUser1_ID(movement.getUser1_ID());	
				payment.setUser2_ID(movement.getUser2_ID());
				payment.setC_DocType_ID(C_DocType_ID);
				payment.setAD_Org_ID(em.getAD_Org_ID());
				payment.setC_BankAccount_ID(p_C_BankAccount_ID);
				payment.setDateAcct(p_DateTrx);
				payment.setDateTrx(p_DateTrx);
				payment.setC_Charge_ID(chargeID);
				payment.setPayAmt(movement.getAmount());
				payment.setDescription("Employee: " + C_BPName + " Variable: " + HR_ConceptName + " Process ID: " + movement.getHR_Process_ID());
				payment.setC_Currency_ID(C_Currency_ID);
				payment.processIt("CO");
				payment.saveEx();
				oldBP = bp.getC_BPartner_ID();
			}
			movement.set_ValueOfColumn("C_Payment_ID", payment.getC_Payment_ID());
			movement.saveEx();
		}
		if (payment != null) {
			addLog("@C_Payment_ID@: " + payment.getDocumentNo());
			created++;
		}
    	return "@Created@=" + created;
	}

}
