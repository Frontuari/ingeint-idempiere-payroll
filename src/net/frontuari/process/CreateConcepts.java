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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import org.adempiere.exceptions.DBException;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Util;
import org.eevolution.model.MHRConcept;
import org.eevolution.model.MHRPayrollConcept;
import org.eevolution.model.X_HR_Concept_Category;
import org.eevolution.model.X_HR_Payroll;

import com.ingeint.base.CustomProcess;

import net.frontuari.model.MLVEHRProcessReport;
import net.frontuari.model.MLVEHRProcessReportLine;
import net.frontuari.model.X_LVE_HR_ProcessReport;

/**
 * @author <a href="mailto:dixon.22martinez@gmail.com">Dixon Martinez</a> 
 *
 */
public class CreateConcepts extends CustomProcess {

	/** Payroll From				*/
	private int p_HR_Payroll_ID 					= 0;
	
	/** Concept Category			*/
	private int p_HR_Concept_Category_ID 			= 0;
	
	/** Payroll To					*/
	private int p_LVE_HR_ProcessReport_ID_To 		= 0;
	
	/**	Process Report				*/
	private int p_LVE_HR_ProcessReport_ID 			= 0; 
	
	/**	Concepts of Payroll	*/
	private MHRPayrollConcept [] conceptsPayroll;
	
	@Override
	protected void prepare() {
		String name;
		for (ProcessInfoParameter parameter : getParameter()){
			name = parameter.getParameterName();
			
			if(parameter.getParameter() == null)
				;
			else if(name.equals(X_HR_Concept_Category.COLUMNNAME_HR_Concept_Category_ID))
				p_HR_Concept_Category_ID = parameter.getParameterAsInt();
			else if(name.equals(X_HR_Payroll.COLUMNNAME_HR_Payroll_ID))
				p_HR_Payroll_ID = parameter.getParameterAsInt();
			else if(name.equals(X_LVE_HR_ProcessReport.COLUMNNAME_LVE_HR_ProcessReport_ID))
				p_LVE_HR_ProcessReport_ID = parameter.getParameterAsInt();
			else
				log.log(Level.SEVERE,"Unknow Parameter "+name);				
		}
		p_LVE_HR_ProcessReport_ID_To = getRecord_ID();
	}

	@Override
	protected String doIt() throws Exception {
		
		int count = 0;
		String sqlWhere = null;
		MHRConcept concepts [] = null;
		
		sqlWhere = (p_HR_Concept_Category_ID != 0? 
				" AND HR_Concept.HR_Concept_Category_ID = " + p_HR_Concept_Category_ID:
					null);
		
		if(p_HR_Concept_Category_ID != 0
				&& p_HR_Payroll_ID == 0)
			concepts = MHRConcept.getConcepts(0, 0, sqlWhere);
		else if(p_HR_Concept_Category_ID != 0
					&& p_HR_Payroll_ID != 0)
			conceptsPayroll = getConceptsOfPayroll(p_HR_Payroll_ID, sqlWhere);
		else if(p_HR_Concept_Category_ID == 0
				&& p_HR_Payroll_ID != 0)
			concepts = MHRConcept.getConcepts(p_HR_Payroll_ID, 0, sqlWhere);
		else{
			MLVEHRProcessReport from = 
					new MLVEHRProcessReport(getCtx(), p_LVE_HR_ProcessReport_ID, get_TrxName());
			MLVEHRProcessReport to = 
					new MLVEHRProcessReport(getCtx(), p_LVE_HR_ProcessReport_ID_To, get_TrxName());
			
			count = to.copyFrom(from, to);
		}
		
		if(concepts != null)
			for (MHRConcept m_HR_Concept : concepts) {
				if(m_HR_Concept.isPaid()
						|| p_HR_Concept_Category_ID != 0){
					if(!existsConceptProcessReport(m_HR_Concept.get_ID())){
						MLVEHRProcessReportLine m_LVE_HR_ProcessReportLine = 
								new MLVEHRProcessReportLine(m_HR_Concept, p_LVE_HR_ProcessReport_ID_To, get_TrxName());
					
						m_LVE_HR_ProcessReportLine.saveEx();
						count++;
					}
				}
				
			}
		
		if(conceptsPayroll != null)
			for (MHRPayrollConcept mhrPayrollConcept : conceptsPayroll) {
				MLVEHRProcessReportLine m_LVE_ProcessReportLine =
						new MLVEHRProcessReportLine(mhrPayrollConcept, p_LVE_HR_ProcessReport_ID_To, get_TrxName());
				;
				m_LVE_ProcessReportLine.saveEx();
				count++;
				
			}
		
		return "@Created@/@Updated@ #" + count;
	}

	/**
	 * 
	 * @author <a href="mailto:dixon.22martinez@gmail.com">Dixon Martinez</a> 03/05/2014, 05:11:43
	 * @param p_HR_Payroll_ID
	 * @param sqlWhere
	 * @return
	 * @return MHRPayrollConcept[]
	 */
	private MHRPayrollConcept[] getConceptsOfPayroll(int p_HR_Payroll_ID, String sqlWhere) {
		Properties ctx = Env.getCtx();
		List<Object> params = new ArrayList<Object>();
		StringBuffer whereClause = new StringBuffer();
		List<MHRPayrollConcept> list ;
		
		whereClause.append("pc.AD_Client_ID in (?,?)");   
		
		whereClause.append(" AND (pc." + X_HR_Payroll.COLUMNNAME_HR_Payroll_ID + " =? OR pc. "
				+X_HR_Payroll.COLUMNNAME_HR_Payroll_ID +" IS NULL)");
		
		
		if (!Util.isEmpty(sqlWhere))
		{
			whereClause.append(sqlWhere);
		}
		
		String sql = "SELECT HR_PayrollConcept_ID "
				+ " FROM HR_PayrollConcept pc"
				+ " LEFT JOIN HR_Concept ON (pc.HR_Concept_ID = HR_Concept.HR_Concept_ID )"
				+ "	WHERE "
				+ " "+whereClause;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			ps = DB.prepareStatement(sql.toString(),get_TrxName());
			ps.setInt(1, 0);
			ps.setInt(2, Env.getAD_Client_ID(Env.getCtx()));
			ps.setInt(3, p_HR_Payroll_ID);
			
			rs = ps.executeQuery();
			
			while (rs.next()){
				params.add(rs.getInt(1));
			}
			
			ps.close();
			rs.close();
		}catch (SQLException e){
			log.severe(e.getMessage());
			throw new DBException(e, sql.toString());      
		}
		finally{
			  DB.close(rs, ps);
		      rs = null; ps = null;
		}
		
		whereClause = new StringBuffer();
		whereClause.append(" HR_PayrollConcept_ID IN ( ");
		String param = params.toString().replace("[", "");
		param = param.replace("]", "");
		whereClause.append(param);
		whereClause.append(")");
		//	Get List
		list = new Query(ctx, MHRPayrollConcept.Table_Name, whereClause.toString(), null)
										.setOnlyActiveRecords(true)
										.setOrderBy(MHRPayrollConcept.COLUMNNAME_SeqNo)
										.list();
		return list.toArray(new MHRPayrollConcept[list.size()]);
	}

	/**
	 * 
	 * @author <a href="mailto:dixon.22martinez@gmail.com">Dixon Martinez</a>  28/04/2014, 18:54:17
	 * @param p_HR_Concept_ID
	 * @return
	 * @return boolean
	 */
	private boolean existsConceptProcessReport(int p_HR_Concept_ID){
		final String whereClause = "LVE_HR_ProcessReport_ID=? AND HR_Concept_ID=?";
		
		return new Query(getCtx(), MLVEHRProcessReportLine.Table_Name, whereClause,get_TrxName())
					.setParameters(new Object[]{p_LVE_HR_ProcessReport_ID_To, p_HR_Concept_ID})
					.match()
				;
	}
	
}
