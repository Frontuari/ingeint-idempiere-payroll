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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.compiere.model.MBPartner;
import org.compiere.model.Query;
import org.compiere.util.CCache;
import org.compiere.util.Env;

/**
 * HR Employee Model
 *
 * @author Victor Perez
 * @author Cristina Ghita, www.arhipac.ro
 */

public class MHREmployee extends X_HR_Employee
{
	private static final long serialVersionUID = -7083160315471023587L;

	public static MHREmployee get(Properties ctx, int HR_Employee_ID)
	{
		if (HR_Employee_ID <= 0)
			return null;
		//
		MHREmployee employee = s_cache.get(HR_Employee_ID);
		if (employee != null)
			return employee;
		//
		employee = new MHREmployee(ctx, HR_Employee_ID, null);
		if (employee.get_ID() == HR_Employee_ID)
		{
			s_cache.put(HR_Employee_ID, employee);	
		}
		else
		{
			employee = null;
		}
		return employee; 
	}
	
	/**
	 * 	Get Employees of Process
	 *  @param p HR Process
	 * 	@return Array of Business Partners
	 */
	public static MBPartner[] getEmployees (MHRProcess p)
	{
		boolean IsPayrollApplicableToEmployee = false;
		List<Object> params = new ArrayList<Object>();
		StringBuilder whereClause = new StringBuilder();
				
		whereClause.append(" C_BPartner.C_BPartner_ID IN (SELECT e.C_BPartner_ID FROM HR_Employee e WHERE e.IsActive=? AND e.AD_Org_ID = ?");
		// Just active employee
		params.add(true);
		params.add(p.getAD_Org_ID());

		// Valid if used definition of payroll per employee > ocurieles 18Nov2014
		
		MHRPayroll Payroll = MHRPayroll.get(Env.getCtx(),p.getHR_Payroll_ID());
		
		if (Payroll !=null || !Payroll.equals(null)){
			IsPayrollApplicableToEmployee = Payroll.get_ValueAsBoolean("IsEmployeeApplicable");
		}
		// This payroll not content periods, NOT IS a Regular Payroll > ogi-cd 28Nov2007
		if(p.getHR_Payroll_ID() != 0 && p.getHR_Period_ID() != 0 && IsPayrollApplicableToEmployee)
		
		{
		whereClause.append(" AND (e.HR_Payroll_ID IS NULL OR e.HR_Payroll_ID=?) " );
			params.add(p.getHR_Payroll_ID());
		}
		
		// HR Period
		if(p.getHR_Period_ID() == 0)
		{
			whereClause.append(" AND e.StartDate <=? ");
			params.add(p.getDateAcct());	
		}
		else
		{
			MHRPeriod period = new MHRPeriod(p.getCtx(), p.getHR_Period_ID(), p.get_TrxName());
			whereClause.append(" AND e.StartDate <=? ");
			params.add(period.getEndDate());
			whereClause.append(" AND (e.EndDate IS NULL OR (e.EndDate >=? AND (e.HR_Exclude='N' Or e.HR_Exclude IS NULL) OR (e.EndDate >?))) ");
			params.add(period.getStartDate());
			params.add(period.getEndDate());
		}
		
		// Selected Department
		if (p.getHR_Department_ID() != 0) 
		{
			whereClause.append(" AND e.HR_Department_ID =? ");
			params.add(p.getHR_Department_ID());
		}
		
		whereClause.append(" ) "); // end select from HR_Employee
		
		// Selected Employee
		if (p.getC_BPartner_ID() != 0)
		{
			whereClause.append(" AND C_BPartner_ID =? ");
			params.add(p.getC_BPartner_ID());
		}
		
		//client
		whereClause.append(" AND AD_Client_ID =? ");
		params.add(p.getAD_Client_ID());
		
		
		List<MBPartner> list = new Query(p.getCtx(), MBPartner.Table_Name, whereClause.toString(), p.get_TrxName())
								.setParameters(params)
								.setOnlyActiveRecords(true)
								.setOrderBy(COLUMNNAME_Name)
								.list();

		return list.toArray(new MBPartner[list.size()]);
	}	//	getEmployees
	
	public static MHREmployee getActiveEmployee(Properties ctx, int C_BPartner_ID, String trxName)
	{
		return new Query(ctx, Table_Name, COLUMNNAME_C_BPartner_ID+"=?", trxName)
							.setOnlyActiveRecords(true)
							.setParameters(new Object[]{C_BPartner_ID})
							.setOrderBy(COLUMNNAME_HR_Employee_ID+" DESC") // just in case...
							.first();
	}
	public static MHREmployee getActiveEmployee(Properties ctx, int C_BPartner_ID, int p_AD_Org_ID, String trxName)
	{
		return new Query(ctx, Table_Name, COLUMNNAME_C_BPartner_ID+"=? AND "+COLUMNNAME_AD_Org_ID +"=? ", trxName)
							.setOnlyActiveRecords(true)
							.setParameters(new Object[]{C_BPartner_ID, p_AD_Org_ID})
							.setOrderBy(COLUMNNAME_HR_Employee_ID+" DESC") // just in case...
							.first();
	}
	public static MHREmployee getActiveEmployee(Properties ctx, int C_BPartner_ID,int p_AD_Org_ID, String trxName,int p_Payroll_ID)
	{
		return new Query(ctx, Table_Name, COLUMNNAME_C_BPartner_ID+"=? AND HR_Payroll_ID = ? AND "+COLUMNNAME_AD_Org_ID+" =? ", trxName)
							.setOnlyActiveRecords(true)
							.setParameters(new Object[]{C_BPartner_ID,p_Payroll_ID,p_AD_Org_ID})
							.setOrderBy(COLUMNNAME_HR_Employee_ID+" DESC") // just in case...
							.first();
	}
	public static MHREmployee getActiveEmployee(Properties ctx, int C_BPartner_ID, String trxName,int p_Payroll_ID)
	{
		return new Query(ctx, Table_Name, COLUMNNAME_C_BPartner_ID+"=? AND HR_Payroll_ID = ?", trxName)
							.setOnlyActiveRecords(true)
							.setParameters(new Object[]{C_BPartner_ID,p_Payroll_ID})
							.setOrderBy(COLUMNNAME_HR_Employee_ID+" DESC") // just in case...
							.first();
	}
	public static MHREmployee getEmployee(Properties ctx, int C_BPartner_ID, int p_AD_Org_ID, String trxName)
	{
		return new Query(ctx, Table_Name, COLUMNNAME_C_BPartner_ID+"=? AND "+COLUMNNAME_AD_Org_ID +"=? ", trxName)
							.setOnlyActiveRecords(false)
							.setParameters(new Object[]{C_BPartner_ID, p_AD_Org_ID})
							.setOrderBy(COLUMNNAME_HR_Employee_ID+" DESC") // just in case...
							.first();
	}
	public static MHREmployee getEmployee(Properties ctx, int C_BPartner_ID,int p_AD_Org_ID, String trxName,int p_Payroll_ID)
	{
		return new Query(ctx, Table_Name, COLUMNNAME_C_BPartner_ID+"=? AND HR_Payroll_ID = ? AND "+COLUMNNAME_AD_Org_ID+" =? ", trxName)
							.setOnlyActiveRecords(false)
							.setParameters(new Object[]{C_BPartner_ID,p_Payroll_ID,p_AD_Org_ID})
							.setOrderBy(COLUMNNAME_HR_Employee_ID+" DESC") // just in case...
							.first();
	}
	public static MHREmployee getEmployee(Properties ctx, int C_BPartner_ID, String trxName)
	{
		return new Query(ctx, Table_Name, COLUMNNAME_C_BPartner_ID+"=? ", trxName)
							.setOnlyActiveRecords(false)
							.setParameters(new Object[]{C_BPartner_ID})
							.setOrderBy(COLUMNNAME_HR_Employee_ID+" DESC") // just in case...
							.first();
	}
	
	/** Cache */
	private static CCache<Integer, MHREmployee> s_cache = new CCache<Integer, MHREmployee>(Table_Name, 1000);
	
	/**************************************************************************
	 * 	Invoice Line Constructor
	 * 	@param ctx context
	 * 	@param HR_Employee_ID ID Employee
	 * 	@param trxName transaction name
	 */
	public MHREmployee (Properties ctx, int HR_Employee_ID, String trxName) //--
	{
		super (ctx, HR_Employee_ID, trxName);
		if (HR_Employee_ID == 0)
		{
			setClientOrg(Env.getAD_Client_ID(Env.getCtx()), Env.getAD_Org_ID(Env.getCtx()));
		}
	}	//	MHREmployee
	
	/**
	 *  Load Constructor
	 *  @param ctx context
	 *  @param rs result set record
	 */
	public MHREmployee (Properties ctx, ResultSet rs, String trxName)
	{
		super(ctx, rs, trxName);
	}	//	MHREmployee
	
	/**
	 * 	Get Employees of Process Regardless of its active state
	 *  @param p HR Process
	 * 	@return Array of Business Partners
	 */
	public static MBPartner[] getEmployeesAll (MHRProcess p,boolean hr_allorg)
	{
		
		List<Object> params = new ArrayList<Object>();
		StringBuilder whereClause = new StringBuilder();
		
		whereClause.append(" IsEmployee = 'Y' AND AD_Client_ID = ?  ");
		//				+ " (SELECT DISTINCT(m.C_BPartner_ID) FROM HR_Movement m");
		//whereClause.append(" JOIN HR_Process p on p.HR_Process_ID = m.HR_Process_ID ");
		//whereClause.append(" WHERE m.ValidFrom >= ? and m.ValidTo <= ? ");
		//params.add(period.getStartDate());
		//params.add(period.getEndDate());
		
		params.add(p.getAD_Client_ID());
		
		if (!hr_allorg){
			whereClause.append(" AND AD_Org_ID = ? ");
			params.add(p.getAD_Org_ID());
		}
		if (p.getC_BPartner_ID() != 0)
		{
			whereClause.append(" AND C_BPartner_ID =? ");
			params.add(p.getC_BPartner_ID());
		}
		//whereClause.append(" AND p.Docstatus in ('CO', 'CL'))");
		
		List<MBPartner> list = new Query(p.getCtx(), MBPartner.Table_Name, whereClause.toString(), p.get_TrxName())
								.setParameters(params)
								.setOnlyActiveRecords(false)
								.setOrderBy(COLUMNNAME_Name)
								.list();

		return list.toArray(new MBPartner[list.size()]);
	}	//	getEmployees
}	//	MHREmployee