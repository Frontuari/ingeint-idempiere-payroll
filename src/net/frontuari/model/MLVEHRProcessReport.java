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
import java.util.List;
import java.util.Properties;

import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.util.CCache;

/**
 * @author <a href="mailto:dixon.22martinez@gmail.com">Dixon Martinez</a>
 *
 */
public class MLVEHRProcessReport extends X_LVE_HR_ProcessReport {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8808324468399877187L;
	
	/**	Lines of Process Report	*/
	private MLVEHRProcessReportLine[] m_lines = null;

	/**
	 * *** Constructor ***
	 * @author <a href="mailto:dixon.22martinez@gmail.com">Dixon Martinez</a> 26/04/2014, 11:39:37
	 * @param ctx
	 * @param LVE_HR_ProcessReport_ID
	 * @param trxName
	 */
	public MLVEHRProcessReport(Properties ctx, int LVE_HR_ProcessReport_ID,
			String trxName) {
		super(ctx, LVE_HR_ProcessReport_ID, trxName);
	}

	/**
	 * *** Constructor ***
	 * @author <a href="mailto:dixon.22martinez@gmail.com">Dixon Martinez</a> 26/04/2014, 11:39:37
	 * @param ctx
	 * @param rs
	 * @param trxName
	 */
	public MLVEHRProcessReport(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}

	/** Cache */
	private static CCache<Integer, MLVEHRProcessReport> s_cache = new CCache<Integer, MLVEHRProcessReport>(Table_Name, 100);
	
	/**
	 * Get Optional Cache
	 * @author <a href="mailto:yamelsenih@gmail.com">Yamel Senih</a> 07/08/2014, 22:21:05
	 * @param ctx
	 * @param p_LVE_HR_ProcessReport_ID
	 * @return
	 * @return MLVEHRProcessReport
	 */
	public static MLVEHRProcessReport get(Properties ctx, int p_LVE_HR_ProcessReport_ID) {
		//	Valid ID
		if(p_LVE_HR_ProcessReport_ID == 0)
			return null;
		//	Get from Cache
		MLVEHRProcessReport report = s_cache.get(p_LVE_HR_ProcessReport_ID);
		if (report != null) {
			return report;
		}
		//	Get DB
		report = new MLVEHRProcessReport(ctx, p_LVE_HR_ProcessReport_ID, null);
		// Put in Cache
		s_cache.put(report.get_ID(), report);
			//	Return
		return report;
	}
	
	/**
	 * 	Get Lines
	 *	@param requery requery
	 *	@return lines
	 */
	public MLVEHRProcessReportLine[] getLines (boolean requery, String sqlWhere)
	{
		if (m_lines  != null && !requery)
		{
			set_TrxName(m_lines, get_TrxName());
			return m_lines;
		}
		List<MLVEHRProcessReportLine> list = new Query(getCtx(), 
				MLVEHRProcessReportLine.Table_Name, "LVE_HR_ProcessReport_ID=?", get_TrxName())
		.setParameters(getLVE_HR_ProcessReport_ID())
		.list();

		m_lines = new MLVEHRProcessReportLine[list.size ()];
		list.toArray (m_lines);
		return m_lines;
	}	//	getLines
	
	/**
	 * Copy Lines of Process Report
	 * @author <a href="mailto:dixon.22martinez@gmail.com">Dixon Martinez</a> 03/05/2014, 05:10:10
	 * @param from
	 * @param to
	 * @return
	 * @return int
	 */
	public int copyFrom (MLVEHRProcessReport from, MLVEHRProcessReport to){
		
		int count = 0;
		MLVEHRProcessReportLine[] concepts;
		
		/* Lines from payroll to copy*/
		concepts = from.getLines(false,null);
		
		/* Loop from process report to copy*/
		for (MLVEHRProcessReportLine line : concepts) {
			MLVEHRProcessReportLine m_LVEHRProcessReportLine =
					new MLVEHRProcessReportLine(getCtx(), 0, get_TrxName());
			
			/*Copy values*/
			PO.copyValues(line,m_LVEHRProcessReportLine, line.getAD_Client_ID(), line.getAD_Org_ID());

			//	Set parent
			m_LVEHRProcessReportLine.setLVE_HR_ProcessReport_ID(getLVE_HR_ProcessReport_ID());
			//	Save line
			m_LVEHRProcessReportLine.saveEx();
			
			count++;			
		}
		return count;
	}
	
	@Override
	public String toString() {
		return getLVE_HR_ProcessReport_ID() + " - " + getName();
	}
}
