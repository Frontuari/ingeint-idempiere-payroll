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

/**
 * @author <a href="mailto:Waditzar.c@gmail.com">Waditza Rivas</a>
 *
 */
public final class MLVERVHRProcessDetail extends X_LVE_RV_HR_ProcessDetail 
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8081268581381223550L;

	/**
	 * *** Constructor ***
	 * @author <a href="mailto:Waditzar.c@gmail.com">Waditza Rivas</a> 01/08/2014, 15:07:15
	 * @param ctx
	 * @param LVE_RV_HR_ProcessDetail_ID
	 * @param trxName
	 */
	public MLVERVHRProcessDetail(Properties ctx,int C_BPartner_ID, String trxName) 
	{
		super(ctx, C_BPartner_ID, trxName);
	}

	/**
	 * *** Constructor ***
	 * @author <a href="mailto:Waditzar.c@gmail.com">Waditza Rivas</a> 01/08/2014, 15:07:15
	 * @param ctx
	 * @param rs
	 * @param trxName
	 */
	public MLVERVHRProcessDetail(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}
	
	/**
	 * Get List from table with where clause
	 * @author <a href="mailto:yamelsenih@gmail.com">Yamel Senih</a> 07/08/2014, 20:50:40
	 * @param ctx
	 * @param whereClause
	 * @param params
	 * @param trxName
	 * @return
	 * @return MLVERVHEProcessDetail[]
	 */
	public static MLVERVHRProcessDetail[] getArray(Properties ctx, String whereClause, ArrayList<Object> params, String trxName) {
		//	Get List
		List<MLVERVHRProcessDetail> list = new Query(ctx, I_LVE_RV_HR_ProcessDetail.Table_Name, 
				whereClause, trxName)
			.setOnlyActiveRecords(true)
			.setParameters(params)
			.setOrderBy(COLUMNNAME_C_BPartner_ID)
			.<MLVERVHRProcessDetail>list();
		//	Convert to Array
		MLVERVHRProcessDetail [] m_details = new MLVERVHRProcessDetail[list.size()];
		list.toArray(m_details);
		//	Return
		return m_details;
	}
}