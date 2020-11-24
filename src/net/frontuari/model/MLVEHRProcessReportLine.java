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
package net.frontuari.model;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.compiere.model.Query;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Util;
import org.eevolution.model.MHRConcept;
import org.eevolution.model.MHRPayrollConcept;

/**
 * @author <a href="mailto:dixon.22martinez@gmail.com">Dixon Martinez</a>
 *
 */
public class MLVEHRProcessReportLine extends X_LVE_HR_ProcessReportLine {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4217921928884207299L;
	
	/**
	 * *** Constructor ***
	 * @author <a href="mailto:dixon.22martinez@gmail.com">Dixon Martinez</a>28/04/2014, 18:41:15
	 * @param ctx
	 * @param LVE_HR_ProcessReportLine_ID
	 * @param trxName
	 */
	public MLVEHRProcessReportLine(Properties ctx,
			int LVE_HR_ProcessReportLine_ID, String trxName) {
		super(ctx, LVE_HR_ProcessReportLine_ID, trxName);
	}

	/**
	 * *** Constructor ***
	 * @author <a href="mailto:dixon.22martinez@gmail.com">Dixon Martinez</a>28/04/2014, 18:41:15
	 * @param ctx
	 * @param rs
	 * @param trxName
	 */
	public MLVEHRProcessReportLine(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}

	/**
	 * 
	 * *** Constructor ***
	 * @author <a href="mailto:dixon.22martinez@gmail.com">Dixon Martinez</a> 28/04/2014, 18:52:57
	 * @param concept
	 * @param p_LVE_HR_ProcessReport_ID
	 * @param trxName
	 */
	public MLVEHRProcessReportLine(MHRConcept concept, int p_LVE_HR_ProcessReport_ID, String trxName){
		super(concept.getCtx(), 0, trxName);
		setLVE_HR_ProcessReport_ID(p_LVE_HR_ProcessReport_ID);
		setHR_Concept_ID(concept.get_ID());
		setSeqNo(concept.getSeqNo());
		setIsActive(true);
		
	}
	
	/**
	 * 
	 * *** Constructor ***
	 * @author <a href="mailto:dixon.22martinez@gmail.com">Dixon Martinez</a> 28/04/2014, 18:52:57
	 * @param concept
	 * @param p_LVE_HR_ProcessReport_ID
	 * @param trxName
	 */
	public MLVEHRProcessReportLine(MLVEHRProcessReportLine concept,int p_HR_Concept, int p_LVE_HR_ProcessReport_ID, String trxName){
		super(concept.getCtx(), 0, trxName);
		setLVE_HR_ProcessReport_ID(p_LVE_HR_ProcessReport_ID);
		setHR_Concept_ID(p_HR_Concept);
		setSeqNo(concept.getSeqNo());
		setIsActive(true);
		
	}
	
	public MLVEHRProcessReportLine(MHRPayrollConcept concept, int p_LVE_HR_ProcessReport_ID, String trxName){
		super(concept.getCtx(), 0, trxName);
		setLVE_HR_ProcessReport_ID(p_LVE_HR_ProcessReport_ID);
		setHR_Concept_ID(concept.getHR_Concept_ID());
		setSeqNo(concept.getSeqNo());
		setIsActive(true);
		
	}
	/**
	 * 	Get Employee's of Payroll Type
	 *  @param payroll_id Payroll ID
	 *  @param department_id Department ID
	 *  @param employee_id Employee_ID
	 * 	@param sqlwhere Clause SQLWhere
	 * 	@return lines
	 */
	public static MLVEHRProcessReportLine[] getConceptsReport (int p_LVE_HR_ProcessReport_ID, String sqlWhere)
	{
		Properties ctx = Env.getCtx();
		List<Object> params = new ArrayList<Object>();
		StringBuffer whereClause = new StringBuffer();
		
		whereClause.append("AD_Client_ID in (?,?)");   
		params.add(0);
		params.add(Env.getAD_Client_ID(Env.getCtx()));
		
		whereClause.append(" AND (" + COLUMNNAME_LVE_HR_ProcessReport_ID + " =? OR "
				+COLUMNNAME_LVE_HR_ProcessReport_ID +" IS NULL)");
		params.add(p_LVE_HR_ProcessReport_ID);
		
		if (!Util.isEmpty(sqlWhere))
		{
			whereClause.append(sqlWhere);
		}
		
		List<MHRConcept> list = new Query(ctx, Table_Name, whereClause.toString(), null)
										.setParameters(params)
										.setOnlyActiveRecords(true)
										.setOrderBy("COALESCE("+COLUMNNAME_SeqNo + ",999999999999) DESC ")
										.list();
		return list.toArray(new MLVEHRProcessReportLine[list.size()]);
	}	//	getConcept	


	@Override
	protected boolean beforeSave(boolean newRecord) {
		super.beforeSave(newRecord);
		if(getPrintName() == null){
			int p_HR_Concept_ID = getHR_Concept_ID();
			String sql = "SELECT Name "
					+ "	FROM HR_Concept "
					+ " WHERE HR_Concept_ID = ? ";
			
			String printName = DB.getSQLValueString(null, sql, p_HR_Concept_ID);
			
			setPrintName(printName);
		}
		return true;
	}//	End beforeSave
	
}
