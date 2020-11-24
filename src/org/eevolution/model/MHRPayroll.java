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
 * Copyright (C) 2003-2007 e-Evolution,SC. All Rights Reserved.               *
 * Contributor(s): Victor Perez www.e-evolution.com                           *
 *****************************************************************************/
package org.eevolution.model;

import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;

import org.compiere.model.MCalendar;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.util.CCache;
import org.compiere.util.Env;
import org.compiere.util.Util;

/**
 *	Payroll for HRayroll Module
 *	
 *  @author Oscar Gómez Islas
 *  @version $Id: HRPayroll.java,v 1.0 2005/10/05 ogomezi
 *  
 *  @author Cristina Ghita, www.arhipac.ro
 */
public class MHRPayroll extends X_HR_Payroll
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1407037967021019961L;
	/** Cache */
	private static CCache<Integer, MHRPayroll> s_cache = new CCache<Integer, MHRPayroll>(Table_Name, 10);
	/** Cache */
	private static CCache<String, MHRPayroll> s_cacheValue = new CCache<String, MHRPayroll>(Table_Name+"_Value", 10);
	/** Lines					*/
	private MHRPayrollConcept[]		m_lines 	 = null;
	
	/**
	 * Get Payroll by Value
	 * @param ctx
	 * @param value
	 * @return payroll
	 */
	public static MHRPayroll forValue(Properties ctx, String value)
	{
		if (Util.isEmpty(value, true))
		{
			return null;
		}
		
		int AD_Client_ID = Env.getAD_Client_ID(ctx);
		final String key = AD_Client_ID+"#"+value;
		MHRPayroll payroll = s_cacheValue.get(key);
		if (payroll != null)
		{
			return payroll;
		}
		
		final String whereClause = COLUMNNAME_Value+"=? AND AD_Client_ID IN (?,?)"; 
		payroll = new Query(ctx, Table_Name, whereClause, null)
							.setParameters(new Object[]{value, 0, AD_Client_ID})
							.setOnlyActiveRecords(true)
							.setOrderBy("AD_Client_ID DESC")
							.first();
		if (payroll != null)
		{
			s_cacheValue.put(key, payroll);
			s_cache.put(payroll.get_ID(), payroll);
		}
		return payroll;
	}
	
	/**
	 * Get Payroll by ID
	 * @param ctx
	 * @param HR_Payroll_ID
	 * @return payroll
	 */
	public static MHRPayroll get(Properties ctx, int HR_Payroll_ID)
	{
		if (HR_Payroll_ID <= 0)
			return null;
		//
		MHRPayroll payroll = s_cache.get(HR_Payroll_ID);
		if (payroll != null)
			return payroll;
		//
		payroll = new MHRPayroll(ctx, HR_Payroll_ID, null);
		if (payroll.get_ID() == HR_Payroll_ID)
		{
			s_cache.put(HR_Payroll_ID, payroll);
		}
		else
		{
			payroll = null;
		}
		return payroll;
	}
	
	/**
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param HR_Payroll_ID id
	 */
	public MHRPayroll (Properties ctx, int HR_Payroll_ID, String trxName)
	{
		super (ctx, HR_Payroll_ID, trxName);
		if (HR_Payroll_ID == 0)
		{
			setProcessing (false);	// N
		}		
	}	//	HRPayroll

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 */
	public MHRPayroll (Properties ctx, ResultSet rs, String trxName)
	{
		super(ctx, rs, trxName);
	}
	
	/**
	 * 	Parent Constructor
	 *	@param parent parent
	 */
	public MHRPayroll (MCalendar calendar)
	{
		this (calendar.getCtx(), 0, calendar.get_TrxName());
		setClientOrg(calendar);
		//setC_Calendar_ID(calendar.getC_Calendar_ID());
	}	//	HRPayroll
	
	/**
	 * Copy Lines of Payroll
	 * @author <a href="mailto:dixon.22martinez@gmail.com">Dixon Martinez</a> 22/03/2014, 16:28:06
	 * @param from
	 * @param to
	 * @return
	 * @return int
	 */
	public int copyFrom (MHRPayroll from, MHRPayroll to){
		
		int count = 0;
		MHRPayrollConcept[] concepts;
		
		/* Lines from payroll to copy*/
		concepts = from.getLines(false);
		
		/* Loop from concepts to copy*/
		for (MHRPayrollConcept line : concepts) {
			MHRPayrollConcept m_HR_PayrollConcept = new MHRPayrollConcept(getCtx(), 0, get_TrxName());
			
			/*Copy values*/
			PO.copyValues(line,m_HR_PayrollConcept, line.getAD_Client_ID(), line.getAD_Org_ID());

			/*	set payroll*/
			m_HR_PayrollConcept.setHR_Payroll_ID(getHR_Payroll_ID());

			/* save payroll concept*/
	 		//
			m_HR_PayrollConcept.saveEx();
	 			count++;			
		}
		return count;
	}
	
	/**
	 * Get Lines
	 * @author <a href="mailto:dixon.22martinez@gmail.com">Dixon Martinez</a> 22/03/2014, 16:28:06
	 * @param requery
	 * @return
	 * @return MHRPayrollConcept[]
	 */
	private MHRPayrollConcept[] getLines(boolean requery) {
		
		if (m_lines != null && !requery)
		{
			set_TrxName(m_lines, get_TrxName());
			return m_lines;
		}
		List<MHRPayrollConcept> list = new Query(getCtx(), X_HR_PayrollConcept.Table_Name, "HR_Payroll_ID = ?",get_TrxName()
				).setParameters(getHR_Payroll_ID())
				.list();
		
		
		m_lines = new MHRPayrollConcept[list.size ()];
		list.toArray (m_lines);
		return m_lines;
	}//Get Lines
	
}	//	MPayroll
