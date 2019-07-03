/**********************************************************************
* This file is part of iDempiere project                              *
* http://www.idempiere.com                                            *
*                                                                     *
* Copyright (C) Carlos Ruiz - globalqss                               *
*                                                                     *
* This program is free software; you can redistribute it and/or       *
* modify it under the terms of the GNU General Public License         *
* as published by the Free Software Foundation; either version 2      *
* of the License, or (at your option) any later version.              *
*                                                                     *
* This program is distributed in the hope that it will be useful,     *
* but WITHOUT ANY WARRANTY; without even the implied warranty of      *
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the        *
* GNU General Public License for more details.                        *
*                                                                     *
* You should have received a copy of the GNU General Public License   *
* along with this program; if not, write to the Free Software         *
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,          *
* MA 02110-1301, USA.                                                 *
*                                                                     *
* Contributors:                                                       *
* - Carlos Ruiz - globalqss                                           *
* - Diego Ruiz  - globalqss                                           *
*                                                                     *
* Sponsors:                                                           *
* - Quality Systems & Solutions (http://www.globalqss.com)            *
**********************************************************************/

package org.idempiere.process;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.compiere.model.MBPartner;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MUser;
import org.compiere.model.Query;
import org.compiere.process.DocAction;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.eevolution.model.MHRConcept;
import org.eevolution.model.MHREmployee;
import org.eevolution.model.MHRMovement;
import org.eevolution.model.MHRProcess;

public class GeneratePayrollInvoices  extends SvrProcess {

	private int p_HR_Process_ID = 0;
	private int p_C_DocType_ID = 0;	
	private String p_DocAction = null;
	private Timestamp p_DateInvoiced = null;
	private int p_C_BPartner_ID = 0;

	/**	Logger							*/
	CLogger log = CLogger.getCLogger (getClass());

	protected void prepare()
	{
		for (ProcessInfoParameter para : getParameter()) {
			String name = para.getParameterName();
			if (name.equals("HR_Process_ID"))
				p_HR_Process_ID = para.getParameterAsInt();
			else if (name.equals("C_DocType_ID"))
				p_C_DocType_ID = para.getParameterAsInt();
			else if (name.equals("DocAction"))
				p_DocAction = (String) para.getParameter();
			else if (name.equals("DateInvoiced"))
				p_DateInvoiced = (Timestamp) para.getParameter();
			else if (name.equals("C_BPartner_ID"))
				p_C_BPartner_ID = para.getParameterAsInt();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
	}
	
	/**
	 * Return getHR_ConceptName
	  * @param trx_name
	  * @param c_bpartner_id
	 */	
	public static String getHR_ConceptName(String trx_name, int hr_concept_id) {
		
		String sConsulta = "SELECT value FROM hr_concept WHERE  hr_concept_id = ? ";
	  	String result = DB.getSQLValueString(trx_name ,sConsulta,new Object[] {hr_concept_id });
	
		return result;			   
	}

	protected String doIt() throws Exception
	{
		log.info("HR_Process_ID=" + p_HR_Process_ID 
				+ ", C_DocType_ID=" + p_C_DocType_ID
				//+ ", DocAction=" + p_DocAction
				+ ", DateInvoiced=" + p_DateInvoiced
				+ ", C_BPartner_ID=" + p_C_BPartner_ID
				);

		int created = 0;
		
		/* Validate mandatory parameters */
		if (   p_HR_Process_ID <= 0
			|| p_C_DocType_ID <= 0
			//|| p_DocAction == null
			|| p_DateInvoiced == null) {
    		throw new IllegalArgumentException ("Fill mandatory parameters");
		}

		p_DocAction = DocAction.ACTION_Complete;
		
		List<Object> parameters = new ArrayList<Object>();
		StringBuilder whereClause = new StringBuilder("HR_Process_ID=? AND HR_Concept_ID IN (SELECT HR_Concept_ID FROM HR_Concept WHERE IsActive='Y' AND IsPrinted='Y')"); //red1  AND C_Charge_ID>0
		parameters.add(p_HR_Process_ID);
		if (p_C_BPartner_ID > 0) {
			whereClause.append(" AND C_BPartner_ID=?");
			parameters.add(p_C_BPartner_ID);
		}
		whereClause.append(" AND C_InvoiceLine_ID IS NULL");
		whereClause.append(" AND hr_process_id IN (SELECT hr_process_id FROM hr_process WHERE docstatus = 'CL')");	
		
		/* Main query */
		List<MHRMovement> movements = new Query(getCtx(), MHRMovement.Table_Name, whereClause.toString(), get_TrxName())
			.setOnlyActiveRecords(true)
			.setParameters(parameters)
			.setOrderBy("C_BPartner_ID,(select seqno from hr_concept where hr_concept.hr_concept_id=hr_movement.hr_concept_id)")
			.list();
		
		int oldBP = 0;
		MInvoice invoice = null;
		boolean hasLines = false;
		MBPartner bp = null;
		int startLine = 0;
		for (MHRMovement movement : movements) {
			if (oldBP != movement.getC_BPartner_ID()) {
				if (invoice != null) {
					if (!invoice.processIt(p_DocAction))
					{
						log.warning("completeInvoice - failed: " + invoice);
						addLog("completeInvoice - failed: " +	 invoice);
					}
					invoice.saveEx();
					addLog("@C_Invoice_ID@: " + invoice.getDocumentNo());
					created++;
				}
				MHRProcess process = (MHRProcess) movement.getHR_Process();
				bp = (MBPartner) movement.getC_BPartner();
				MHREmployee em = MHREmployee.getActiveEmployee(getCtx(), movement.getC_BPartner_ID(), get_TrxName());
				invoice = new MInvoice(getCtx(), 0, get_TrxName());
				invoice.setC_BPartner_ID(movement.getC_BPartner_ID());
				invoice.setC_BPartner_Location_ID(bp.getLocation(0).getC_BPartner_Location_ID());

				int userID = new Query(getCtx(), MUser.Table_Name, "C_BPartner_ID=? AND IsInPayroll=?", get_TrxName())
					.setOnlyActiveRecords(true)
					.setParameters(movement.getC_BPartner_ID(), true)
					.firstId();
				
				invoice.setAD_User_ID(userID);
				invoice.setC_DocTypeTarget_ID(p_C_DocType_ID);
				invoice.setAD_Org_ID(em.getAD_Org_ID());
				invoice.setIsSOTrx(false);
				invoice.setPaymentRule(bp.getPaymentRulePO());
				invoice.setDateInvoiced(p_DateInvoiced);
				invoice.setDateAcct(p_DateInvoiced);
				invoice.setDescription(process.getName());
				invoice.saveEx();

				hasLines = false;
				startLine = 10;
				oldBP=movement.getC_BPartner_ID();
			}
			MHRConcept concept = (MHRConcept) movement.getHR_Concept();
			int chargeID = concept.get_ValueAsInt("C_Charge_ID");
			
			String HR_ConceptName = getHR_ConceptName(get_TrxName(), movement.getHR_Concept_ID());
			MInvoiceLine line = new MInvoiceLine(invoice);
			line.setC_Charge_ID(chargeID);
			line.setQty(Env.ONE);
			line.setDescription(HR_ConceptName);
			line.setLine(startLine);
			startLine += 10;
			if (MHRMovement.ACCOUNTSIGN_Credit.equals(movement.getAccountSign()))
				line.setPrice(movement.getAmount().negate());
			else
				line.setPrice(movement.getAmount());
			
			line.setAD_OrgTrx_ID(movement.getAD_OrgTrx_ID());
			line.setC_Project_ID(movement.getC_Project_ID());
			line.setC_Campaign_ID(movement.getC_Campaign_ID());
			line.setC_Activity_ID(movement.getC_Activity_ID());
			line.setUser1_ID(movement.getUser1_ID());
			line.setUser2_ID(movement.getUser2_ID());
			line.saveEx();
			
			movement.set_ValueOfColumn("C_InvoiceLine_ID", line.getC_InvoiceLine_ID());
			movement.saveEx();
			
			hasLines = true;
		}
		if (invoice != null && hasLines) {
			if (!invoice.processIt(p_DocAction))
			{
				log.warning("completeInvoice - failed: " + invoice);
				addLog("completeInvoice - failed: " + invoice);
			}
			invoice.saveEx();
			addLog("@C_Invoice_ID@: " + invoice.getDocumentNo());
			created++;
		}

    	return "@Created@=" + created;
	}

} // GeneratePayrollInvoices
