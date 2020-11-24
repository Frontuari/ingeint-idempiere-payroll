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
package net.frontuari.utils;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Level;

import org.compiere.model.MBPartner;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.eevolution.model.I_HR_Job;
import org.eevolution.model.MHREmployee;
import net.frontuari.model.MLVERVHRProcessDetail;

/**
 * @author <a href="mailto:yamelsenih@gmail.com">Yamel Senih</a>
 * Export class for MINPPTRASS in payroll
 */
public class HR_MINPPTRASS implements HRReportExport {
	/** Logger										*/
	static private CLogger			s_log 				= CLogger.getCLogger (HR_MINPPTRASS.class);
	/** BPartner Info Index for First Name 1    	*/
	private static final int     	BP_FIRST_NAME_1 	= 0;
	/** BPartner Info Index for First Name 1    	*/
	private static final int     	BP_FIRST_NAME_2 	= 1;
	/** BPartner Info Index for First Name 1    	*/
	private static final int     	BP_LAST_NAME_1 		= 2;
	/** BPartner Info Index for First Name 1    	*/
	private static final int     	BP_LAST_NAME_2 		= 3;
	/** BPartner Info Index for Nationality	    	*/
	private static final int     	BP_NATIONALITY 		= 4;
	/** BPartner Info Index for Tax ID				*/
	private static final int     	BP_TAX_ID 			= 5;
	/** BPartner Info Index for Gender		    	*/
	private static final int     	BP_GENDER 			= 6;
	/** BPartner Info Index for Birthday			*/
	private static final int     	BP_BIRTHDAY 		= 7;
	/** BPartner Info Index for Job					*/
	private static final int     	EM_JOB 				= 8;
	/** BPartner Info Index for Employee Type		*/
	private static final int     	EM_TYPE 			= 9;
	/** BPartner Info Index for Employee Start Date	*/
	private static final int     	EM_START_DATE 		= 10;
	/** BPartner Info Index for Employee Status		*/
	private static final int     	EM_STATUS 			= 11;
	/** BPartner Info Index for Employee Salary		*/
	private static final int     	EM_SALARY 			= 12;
	/**	Concept for									*/
	private static final String     EM_CONCEPT_SALARY	= "F_SD";
	/**	Days of Month								*/
	private static final int     	EM_DAYS_MONTH		= 30;
	/**	Constant Payroll							*/
	private final String		PAYROLL_CONSTANT		= "N";
	/**	File Extension								*/
	private final String		FILE_EXTENSION			= ".txt";
	/**	Number Format								*/
	private DecimalFormat 			m_NumberFormatt 	= null;
	/**	Date Format									*/
	private SimpleDateFormat 		m_DateFormat 		= null;
	/**	Process Detail Line							*/
	private MLVERVHRProcessDetail 	m_currentPDL		= null;
	/** Name File									*/
	private String 					m_NameFile			= null; 
	
	@Override
	public int exportToFile(MLVERVHRProcessDetail[] details, File file, StringBuffer err) {
		if (details == null || details.length == 0)
			return 0;
		
		MLVERVHRProcessDetail pdl = details[0];
		//	Set new File Name
		StringBuffer pathName = new StringBuffer(file.isFile() || !file.exists()
											? file.getParent()
													: file.getAbsolutePath());
		//	Add Separator
		pathName.append(File.separator)
			//	
			.append(PAYROLL_CONSTANT)
			//	Accounting Date in format MM YYYY
			.append(new SimpleDateFormat("MMyyyy").format(pdl.getDateAcct()))
			//	Extension
			.append(FILE_EXTENSION);
		
		file = new File(pathName.toString());
	
		//  delete if exists
		try {
			if (file.exists())
				file.delete();
		} catch (Exception e) {
			s_log.log(Level.WARNING, "Could not delete - " + file.getAbsolutePath(), e);
		}
		String separator = ";";
		int noLines = 0;
		//	
		StringBuffer line = null;
		//	Number Format
		m_NumberFormatt = new DecimalFormat("#########.00");
		//	Date Format
		m_DateFormat = new SimpleDateFormat("ddMMyyyy");
		//	
		try {
			//	
			FileWriter fw = new FileWriter(file);
			
			//  write header
			line = new StringBuffer();
			noLines++;
			//  write lines
			for (int i = 0; i < details.length; i++)
			{
				pdl = details[i];
				if (pdl == null)
					continue;
				//	Set Process Detail Line
				if(m_currentPDL == null
						|| m_currentPDL.getC_BPartner_ID() != pdl.getC_BPartner_ID())
					m_currentPDL = pdl;
				else if(m_currentPDL.getC_BPartner_ID() == pdl.getC_BPartner_ID())
					continue;
				//	Process Business Partner
				String [] bpInfo = processBPartner(m_currentPDL.getC_BPartner_ID(), m_currentPDL.getAD_Org_ID(), m_currentPDL.get_TableName());
				//	Line
				line = new StringBuffer();
				//	Amount
				BigDecimal m_Amt = m_currentPDL.getAmt();
				if(m_Amt == null)
					m_Amt = Env.ZERO;
				//	New Line
				if(i != 0)
					line.append(Env.NL);
				//	First Name
				line.append(bpInfo[BP_FIRST_NAME_1])
					.append(separator)
					//	First Name 2
					.append(bpInfo[BP_FIRST_NAME_2])
					.append(separator)
					//	Last Name 1
					.append(bpInfo[BP_LAST_NAME_1])
					.append(separator)
					//	Last Name 2
					.append(bpInfo[BP_LAST_NAME_2])
					.append(separator)
					//	Nationality
					.append(bpInfo[BP_NATIONALITY])
					.append(separator)
					//	Tax ID
					.append(bpInfo[BP_TAX_ID])
					.append(separator)
					//	Gender
					.append(bpInfo[BP_GENDER])
					.append(separator)
					//	Birth date
					.append(bpInfo[BP_BIRTHDAY])
					.append(separator)
					//	Employee Job
					.append(bpInfo[EM_JOB])
					.append(separator)
					//	Employee Type
					.append(bpInfo[EM_TYPE])
					.append(separator)
					//	Start Date
					.append(bpInfo[EM_START_DATE])
					.append(separator)
					//	Employee Status
					.append(bpInfo[EM_STATUS])
					.append(separator)
					//	Salary
					.append(bpInfo[EM_SALARY]);
				//	Write Line
				fw.write(line.toString());
				noLines++;
			}   //  write line
			//	Close
			fw.flush();
			fw.close();
			
			// Raul Munoz set Name file for zk 
			m_NameFile = file.getName();
			
		} catch (Exception e) {
			err.append(e.toString());
			s_log.log(Level.SEVERE, "", e);
			return -1;
		}
		//	
		return noLines;
	}
	
	/**
	 * Process Business Partner
	 * @author <a href="mailto:yamelsenih@gmail.com">Yamel Senih</a> 16/08/2014, 12:27:09
	 * @param p_C_BPartner_ID
	 * @param p_AD_Org_ID
	 * @param p_TrxName
	 * @return String []
	 */
	private String [] processBPartner(int p_C_BPartner_ID, int p_AD_Org_ID, String p_TrxName) {
		String [] bpInfo = new String[13];
		//	
		//	Get Business Partner
		MBPartner bpartner = MBPartner.get(Env.getCtx(), p_C_BPartner_ID);
		//	Get Active Employee
		MHREmployee employee = MHREmployee.getActiveEmployee(Env.getCtx(), 
				bpartner.getC_BPartner_ID(), p_AD_Org_ID, p_TrxName);
		//	Valid Employee
		if(employee == null)
			return null;
		//	Get Name
		String name = bpartner.getName();
		String name2 = bpartner.getName2();
		//	Valid Null
		if(name == null)
			name = "";
		if(name2 == null)
			name2 = "";
		//	End Index for First Name
		int endIndex = name.indexOf(" ");
		if(endIndex < 0)
			endIndex = name.length();
		//	Extract First Name 1
		String m_FirstName1 = name.substring(0, endIndex);
		//	Extract First Name 2
		String m_FirstName2 = ((endIndex + 1) > name.length()
										? " "
										: name.substring(endIndex + 1));
		endIndex = m_FirstName2.indexOf(" ");
		//	Cut First Name 2
		if(endIndex < 0)
			endIndex = m_FirstName2.length();
		m_FirstName2 = m_FirstName2.substring(0, endIndex);
		//	End Index for Last Name
		endIndex = name2.indexOf(" ");
		if(endIndex < 0)
			endIndex = name2.length();
		//	Extract Last Name 1
		String m_LastName1 = name2.substring(0, endIndex);
		//	Extract Last Name 2
		String m_LastName2 = ((endIndex + 1) > name2.length()
										? " "
										: name2.substring(endIndex + 1));
		endIndex = m_LastName2.indexOf(" ");
		//	Cut Last Name 2
		if(endIndex < 0)
			endIndex = m_LastName2.length();
		m_LastName2 = m_LastName2.substring(0, endIndex);
		//	Valid length
		if(m_FirstName1.length() > 20)
			m_FirstName1 = m_FirstName1.substring(0, 19);
		if(m_FirstName2.length() > 20)
			m_FirstName2 = m_FirstName2.substring(0, 19);
		if(m_LastName1.length() > 20)
			m_LastName1 = m_LastName1.substring(0, 19);
		if(m_LastName2.length() > 20)
			m_LastName2 = m_LastName2.substring(0, 19);
		//	Nationality
		String nationality = bpartner.get_ValueAsString("Nationality");
		//	Gender
		String gender = bpartner.get_ValueAsString("Gender");
		if(gender != null
				&& gender.equals("M"))
			gender = "1";
		else
			gender = "2";
		//	Get Birthdate
		Timestamp birthday = (Timestamp)bpartner.get_Value("Birthday");
		if(birthday == null)
			birthday = new Timestamp(System.currentTimeMillis());
		//	Job
		I_HR_Job m_Job = employee.getHR_Job();
		String job = "";
		if(m_Job != null)
			job = m_Job.getName();
		if(job.length() > 100)
			job = job.substring(0, 99);
		//	Get Employee Status
		String employStatus = bpartner.get_ValueAsString("EmployeeStatus");
		if(employStatus == null
				|| employStatus.equals("RT"))
			employStatus = "2";
		else
			employStatus = "1";
		//	Get Start Date
		String startDate = m_DateFormat.format(employee.getStartDate());
		//	Get Salary
		BigDecimal salaryAmt = DB.getSQLValueBD(null, "SELECT CASE " +
				"												WHEN c.ColumnType = 'Q' " +
				"													THEN a.Qty " +
				"												WHEN c.ColumnType = 'A' " +
				"													THEN a.Amount " +
				"											END Amt " +
				"FROM HR_Concept c " +
				"INNER JOIN HR_Attribute a ON(a.HR_Concept_ID = c.HR_Concept_ID) " +
				"WHERE a.C_BPartner_ID = ? " +
				"AND c.IsEmployee = 'Y' " +
				"AND c.IsActive = 'Y' " +
				"AND a.IsActive = 'Y' " +
				"AND c.Value = ? " +
				"AND a.ValidFrom <= ? " +
				"ORDER BY a.ValidFrom DESC", 
				new Object[]{p_C_BPartner_ID, EM_CONCEPT_SALARY, m_currentPDL.getDateAcct()});
		//	Valid Null
		if(salaryAmt == null)
			salaryAmt = Env.ZERO;
		//	Format
		String salary = m_NumberFormatt.format(salaryAmt.doubleValue() * EM_DAYS_MONTH)
				.toString()
				.replace(",", ".")
				.replace(".", "");
		//	Set Array
		bpInfo[BP_FIRST_NAME_1]	= m_FirstName1;
		bpInfo[BP_FIRST_NAME_2]	= m_FirstName2;
		bpInfo[BP_LAST_NAME_1]	= m_LastName1;
		bpInfo[BP_LAST_NAME_2]	= m_LastName2;
		bpInfo[BP_NATIONALITY]	= nationality;
		bpInfo[BP_TAX_ID]		= bpartner.getValue();
		bpInfo[BP_GENDER]		= gender;
		bpInfo[BP_BIRTHDAY]		= m_DateFormat.format(birthday);
		bpInfo[EM_JOB]			= job;
		bpInfo[EM_TYPE]			= "1";	//	TODO what is????
		bpInfo[EM_START_DATE]	= startDate;
		bpInfo[EM_STATUS]		= employStatus;
		bpInfo[EM_SALARY]		= salary;
		//	Return
		return bpInfo;
	}

	@Override
	public String getNameFile() {
		return m_NameFile;
	}
}

