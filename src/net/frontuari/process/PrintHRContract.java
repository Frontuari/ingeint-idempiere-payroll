/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
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
 * Copyright (C) 2003-2014 E.R.P. Consultores y Asociados, C.A.               *
 * All Rights Reserved.                                                       *
 * Contributor(s): Yamel Senih www.erpconsultoresyasociados.com               *
 *****************************************************************************/
package net.frontuari.process;

import java.util.Vector;
import java.util.logging.Level;

import org.adempiere.webui.apps.WProcessCtl;
import org.compiere.model.MQuery;
import org.compiere.model.PrintInfo;
import org.compiere.print.MPrintFormat;
import org.compiere.print.ReportCtl;
import org.compiere.print.ReportEngine;
import org.compiere.process.ProcessInfo;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.util.DB;
import org.compiere.util.Ini;
import org.compiere.util.Msg;
import org.eevolution.model.I_HR_Employee;
import org.eevolution.model.MHREmployee;

import com.ingeint.base.CustomProcess;

/**
 * @author <a href="mailto:dixon.22martinez@gmail.com">Dixon Martinez</a>
 *
 */
public class PrintHRContract extends CustomProcess {

	/**	Record ID								*/
	private int 	p_Record_ID 				= 0;
	/**	Business Partner Location 				*/
	private int		p_C_BPartner_Location_ID 	= 0;
	/**	Print Format							*/
	private int 	p_AD_PrintFormat_ID 		= 0;
	
	@Override
	protected void prepare() {
		for (ProcessInfoParameter para:getParameter()) {
			String name = para.getParameterName();
			
			if (para.getParameter() == null)
				;			
			else if(name.equals("C_BPartner_Location_ID"))
				p_C_BPartner_Location_ID = para.getParameterAsInt();
			else if(name.equals("AD_PrintFormat_ID"))
				p_AD_PrintFormat_ID = para.getParameterAsInt();
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);			
		}
		//	Set record Id
		p_Record_ID = getRecord_ID();
		//	
	}

	@Override
	protected String doIt() throws Exception {
		//	Set direct print of properties
		boolean directPrint = Ini.isPropertyBool(Ini.P_PRINTPREVIEW);
		//	Search ID from Print Format 
		String sql = ""; 

		if(p_AD_PrintFormat_ID == 0) {
			sql = "SELECT AD_PrintFormat_ID" +
					" FROM HR_Employee e" +
					" INNER JOIN HR_Payroll p ON (e.HR_Payroll_ID = p.HR_Payroll_ID)" +
					" WHERE" +
					"	e.HR_Employee_ID=?";
			p_AD_PrintFormat_ID = DB.getSQLValue(get_TrxName(), sql, p_Record_ID);
		}
		
		//	If Exists Print format
		if(p_AD_PrintFormat_ID > 0) {
			//	Get Format & Data
			MPrintFormat format = 
					MPrintFormat.get (getCtx(), p_AD_PrintFormat_ID, false);

			MQuery q = 
					new MQuery(MHREmployee.Table_Name);
			
			q.addRestriction(I_HR_Employee.COLUMNNAME_HR_Employee_ID, "=", p_Record_ID);
			
			//	Create object Print Info 
			PrintInfo i = 
					new PrintInfo(Msg.translate(getCtx(), I_HR_Employee.COLUMNNAME_HR_Employee_ID), getTable_ID(), p_Record_ID);
			
			i.setAD_Table_ID(getTable_ID());
			
			//	If exists Print Format 
			if(format != null)	{
				//	Engine
				ReportEngine re = 
						new ReportEngine(getCtx(), format, q , i, get_TrxName()); //	Instance report Engine 
				//	If report engine is not null
				if(format.getJasperProcess_ID() > 0) { 
					//		
					ProcessInfo pi = new ProcessInfo ("", format.getJasperProcess_ID());
					pi.setPrintPreview(!directPrint );
					pi.setRecord_ID ( p_Record_ID );
					Vector<ProcessInfoParameter> jasperPrintParams = new Vector<ProcessInfoParameter>();
					ProcessInfoParameter pip;
					//
					pip = new ProcessInfoParameter(ReportCtl.PARAM_PRINT_FORMAT, format, null, null, null);
					jasperPrintParams.add(pip);
					pip = new ProcessInfoParameter(ReportCtl.PARAM_PRINT_INFO, re.getPrintInfo(), null, null, null);
					jasperPrintParams.add(pip);
					pip = new ProcessInfoParameter("C_BPartner_Location_ID", p_C_BPartner_Location_ID, null, "", "");
					jasperPrintParams.add(pip);
					//
					pi.setParameter(jasperPrintParams.toArray(new ProcessInfoParameter[]{}));
					//	Execute Process
					WProcessCtl.process(null, 0, null, pi, null);
				}
			}
		}
		//	
		return "Ok";
	}

}
