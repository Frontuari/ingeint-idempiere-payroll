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
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.compiere.model.MBPartner;
import org.compiere.model.MSysConfig;
import org.compiere.model.Query;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.eevolution.model.I_HR_Attribute;
import org.eevolution.model.MHRAttribute;
import org.eevolution.model.MHREmployee;
import org.eevolution.model.X_HR_Employee;
import org.eevolution.model.X_HR_Job;
import net.frontuari.model.MLVERVHRProcessDetail;


/**
 * @author <a href="mailto:dmartinez@erpcya.com">Dixon Martinez</a>
 * Export class for HR_TIUNA_ChangeSalary in payroll
 */
public class HR_TIUNA_ChangeSalary implements HRReportExport {
	/** Logger										*/
	static private CLogger	s_log = CLogger.getCLogger (HR_TIUNA_ChangeSalary.class);
	/** BPartner Info Index for Nationality	    	*/
	private static final int     BP_NATIONALITY 	= 0;
	/** BPartner Info Index for Tax ID		    	*/
	private static final int     BP_TAX_ID 			= 1;
	/** BPartner Info Index for First Name 1    	*/
	private static final int     BP_FIRST_NAME_1 	= 2;
	/** BPartner Info Index for First Name 1    	*/
	private static final int     BP_FIRST_NAME_2 	= 3;
	/** BPartner Info Index for First Name 1    	*/
	private static final int     BP_LAST_NAME_1 	= 4;
	/** BPartner Info Index for First Name 1    	*/
	private static final int     BP_LAST_NAME_2 	= 5;
	
	/**	File Extension							*/
	private final String		FILE_EXTENSION		= ".txt";
	/**	Separator								*/
	private final String 		SEPARATOR 			= ";";
	/**	Number Format							*/
	private DecimalFormat 		m_NumberFormatt 	= null;
	/**	Date Format								*/
	private SimpleDateFormat 	m_DateFormat 		= null;
	/**	Date Format for Process					*/
	private SimpleDateFormat 	m_ProcessDateFormat = null;
	/**	Current Amount							*/
	private BigDecimal 			m_CurrentAmt		= null;
	/**	Current Process Report Line				*/
	private MLVERVHRProcessDetail 	m_Current_Pdl 	= null;
	/**	File Writer								*/
	private FileWriter 				m_FileWriter	= null;
	/**	Number Lines							*/
	private int 					m_NoLines 		= 0;
	/** Name File								*/
	private String 					m_NameFile		= null; 
	
	private BigDecimal 				m_OldSalary		= Env.ZERO;
	
	private Timestamp 				m_Date			= null;
	
	public final static char CR  = (char) 0x0D;
	public final static char LF  = (char) 0x0A; 

	public final static String CRLF  = "" + CR + LF; 
	
	@Override
	public int exportToFile(MLVERVHRProcessDetail[] details, File file, StringBuffer err) {
		if (details == null || details.length == 0)
			return 0;
		MLVERVHRProcessDetail pdl = details[0];
		//  delete if exists
		try {
			//	Set new File Name
			StringBuffer pathName = new StringBuffer(file.isFile() || !file.exists()
												? file.getParent()
														: file.getAbsolutePath());
			String name = MSysConfig.getValue("TIUNA_SALARY_CHANGE", "CAMBIODESALARIO", Env.getAD_Client_ID(Env.getCtx()));
			//	Add Separator
			pathName.append(File.separator)
				.append(name)
				//	Accounting Date in format DD MM YYYY
				.append("UPLOADCSA" + new SimpleDateFormat("ddMMyyyy").format(new Date(System.currentTimeMillis())))
				//	Extension
				.append(FILE_EXTENSION);
			
			file = new File(pathName.toString());
			//	Delete if Exists
			if (file.exists())
				file.delete();
		} catch (Exception e) {
			s_log.log(Level.WARNING, "Could not delete - " + file.getAbsolutePath(), e);
		}
		//	Number Format
		m_NumberFormatt = new DecimalFormat("###.00");
		//	Date Format
		m_DateFormat = new SimpleDateFormat("dd/MM/yyyy");
		m_ProcessDateFormat = new SimpleDateFormat("MMyyyy");
		//	Current Business Partner
		int m_Current_BPartner_ID = 0;
		//	Current Month
		String m_CurrentMonth = null;
		//	
		try {
			//	
			m_FileWriter = new FileWriter(file);
			//  write header
			m_NoLines ++;
			//  write lines
			for (int i = 0; i < details.length; i++) {
				pdl = details[i];
				if (pdl == null)
					continue;
				if(pdl.get_Value("EmployeeStatus") == null 
						|| pdl.get_Value("EmployeeStatus").equals("LS")) { //pdl.get_Value("EmployeeStatus").equals(X_HR_Employee.EMPLOYEESTATUS_LeftService)) {
					continue;
				}
				if(pdl.getC_BPartner() != null 
						&& !pdl.getC_BPartner().isActive()) {
					continue;
				}
				//	Verify Current Business Partner and Month
				if(m_Current_BPartner_ID != pdl.getC_BPartner_ID()) {
					writeLine();
					m_Current_BPartner_ID = pdl.getC_BPartner_ID();
					m_CurrentMonth = m_ProcessDateFormat.format(pdl.getDateAcct());
					ArrayList<Object> params = new ArrayList<Object>();
					
					String whereClause ="	HR_Concept_ID = ? "
							+ "AND C_BPartner_ID = ? "
							;
					params.add(pdl.getHR_Concept_ID());
					params.add(pdl.getC_BPartner_ID());
					
					MHRAttribute att = new Query(Env.getCtx(), I_HR_Attribute.Table_Name, whereClause.toString(), null)
							.setParameters(params)
							.setOnlyActiveRecords(true)
							.setOrderBy(I_HR_Attribute.COLUMNNAME_ValidFrom + " DESC")
							.first();
					if(att == null)
						m_CurrentAmt  = Env.ZERO;
					else {
						m_CurrentAmt = att.getAmount();
						m_Date = att.getValidFrom();
					}
					
					whereClause ="	HR_Concept_ID = ? "
							+ "AND C_BPartner_ID = ? "
							+ "AND (HR_Attribute_ID <> ? OR HR_Attribute_ID IS NULL) "
							;
					params = new ArrayList<Object>();
					params.add(pdl.getHR_Concept_ID());
					params.add(pdl.getC_BPartner_ID());
					params.add(att != null ? att.getHR_Attribute_ID() : null);
					
					att = new Query(Env.getCtx(), I_HR_Attribute.Table_Name, whereClause.toString(), null)
							.setParameters(params)
							.setOnlyActiveRecords(true)
							.setOrderBy(I_HR_Attribute.COLUMNNAME_ValidFrom + " DESC")
							.first();
					if(att == null)
						m_OldSalary = Env.ZERO;
					else
						m_OldSalary = att.getAmount();
					m_Current_Pdl = pdl;
				} else if(m_CurrentMonth != null
						&& !m_CurrentMonth.equals(m_ProcessDateFormat.format(pdl.getDateAcct()))) {
					
					ArrayList<Object> params = new ArrayList<Object>();
					
					String whereClause ="	HR_Concept_ID = ? "
							+ "AND C_BPartner_ID = ? "
							;
					params.add(pdl.getHR_Concept_ID());
					params.add(pdl.getC_BPartner_ID());
					// params.add(pdl.getValidFrom());
					
					MHRAttribute att = new Query(Env.getCtx(), I_HR_Attribute.Table_Name, whereClause.toString(), null)
							.setParameters(params)
							.setOnlyActiveRecords(true)
							.setOrderBy(I_HR_Attribute.COLUMNNAME_ValidFrom + " DESC")
							.first();
					if(att == null)
						m_CurrentAmt  = Env.ZERO;
					else {
						m_CurrentAmt = att.getAmount();
						m_Date = att.getValidFrom();
					}
					
					whereClause ="	HR_Concept_ID = ? "
							+ "AND C_BPartner_ID = ? "
							+ "AND (HR_Attribute_ID <> ? OR HR_Attribute_ID IS NULL) "
							;
					params = new ArrayList<Object>();
					params.add(pdl.getHR_Concept_ID());
					params.add(pdl.getC_BPartner_ID());
					params.add(att != null ? att.getHR_Attribute_ID() : null);
					
					att = new Query(Env.getCtx(), I_HR_Attribute.Table_Name, whereClause.toString(), null)
							.setParameters(params)
							.setOnlyActiveRecords(true)
							.setOrderBy(I_HR_Attribute.COLUMNNAME_ValidFrom + " DESC")
							.first();
					if(att == null)
						m_OldSalary = Env.ZERO;
					else
						m_OldSalary = att.getAmount();
				}
			}  
			//  write last line
			writeLine();
			//	Close
			m_FileWriter.flush();
			m_FileWriter.close();
			
			// Raul Munoz set Name file for zk 
			m_NameFile = file.getName();
			
		} catch (Exception e) {
			err.append(e.toString());
			s_log.log(Level.SEVERE, "", e);
			return -1;
		}
		//
		return m_NoLines;
	}
	
	/**
	 * Write Line
	 * @author <a href="mailto:yamelsenih@gmail.com">Yamel Senih</a> 8/12/2014, 15:08:36
	 * @return void
	 * @throws IOException 
	 */
	private void writeLine() throws IOException {
		//	Valid Null Value
		if(m_Current_Pdl == null)
			return;
		//	Process Business Partner
		String [] bpInfo = processBPartner(m_Current_Pdl.getC_BPartner_ID(), 
				m_Current_Pdl.getAD_Org_ID(), m_Current_Pdl.get_TrxName());
		//	Line
		StringBuffer line = new StringBuffer();
		
		//Business PArtner Tax ID
		String bPartnerTax = bpInfo[BP_TAX_ID];
		bPartnerTax = bPartnerTax.substring(1, bPartnerTax.length());
		bPartnerTax =String.format("%0"+ 9 +"d",Integer.parseInt(bPartnerTax));
		//	Amount
		if(m_CurrentAmt == null)
			m_CurrentAmt = Env.ZERO;
		//	New Line
		if(m_NoLines > 1)
			line.append(CRLF);
		//	Nationality
		line.append(bpInfo[BP_NATIONALITY])
			.append(SEPARATOR)
			//	Tax ID
			.append(bPartnerTax)
			.append(SEPARATOR)
			//	Last Name 1
			.append(bpInfo[BP_LAST_NAME_1])
			.append(" ")
			//	Last Name 2
			.append(bpInfo[BP_LAST_NAME_2])
			.append(" ")
			//	First Name 1
			.append(bpInfo[BP_FIRST_NAME_1])
			.append(" ")
			//	First Name 2
			.append(bpInfo[BP_FIRST_NAME_2])
			.append(SEPARATOR)
			//	Old Salary
			.append(m_NumberFormatt.format(
					((m_OldSalary.doubleValue() * 30) *12) / 52)
					.toString()
					.replace(",", ".")
					)
			.append(SEPARATOR)
			//	New Salary
			.append(m_NumberFormatt.format(
					((m_CurrentAmt.doubleValue() * 30) *12) / 52)
												.toString()
												.replace(",", ".")
												)
			.append(SEPARATOR)
			//	Date new Salary
			.append(m_DateFormat.format(m_Date))
			;
		//	Write Line
		m_FileWriter.write(line.toString());
		m_NoLines ++;
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
		String [] bpInfo = new String[19];
		//	
		//	Get Business Partner
		MBPartner bpartner = MBPartner.get(Env.getCtx(), p_C_BPartner_ID);
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
		if(m_FirstName1.length() > 25)
			m_FirstName1 = m_FirstName1.substring(0, 24);
		else if(m_FirstName1.length() == 0)
			m_FirstName1 = "";
		if(m_FirstName2.length() > 25)
			m_FirstName2 = m_FirstName2.substring(0, 24);
		else if(m_FirstName2.length() == 0)
			m_FirstName2 = "";
		if(m_LastName1.length() > 25)
			m_LastName1 = m_LastName1.substring(0, 24);
		else if(m_LastName1.length() == 0)
			m_LastName1 = "";
		if(m_LastName2.length() > 25)
			m_LastName2 = m_LastName2.substring(0, 24);
		else if(m_LastName2.length() == 0)
			m_LastName2 = "";
		
		//	Get Active Employee
		MHREmployee employee = MHREmployee.getActiveEmployee(Env.getCtx(), 
				bpartner.getC_BPartner_ID(), p_AD_Org_ID, p_TrxName);
		//	Valid Employee
		if(employee == null)
			return null;
		//	Job
		X_HR_Job m_Job = (X_HR_Job) employee.getHR_Job();
		String job = "";
		if(m_Job != null) {
			job = m_Job.get_ValueAsString("ReferenceNo");
			if(job == null)
				job = m_Job.getValue();
		}
		//	Set Array
		bpInfo[BP_NATIONALITY]	= bpartner.get_ValueAsString("Nationality");
		bpInfo[BP_TAX_ID]		= bpartner.getValue();
		bpInfo[BP_FIRST_NAME_1]	= replaceAll(m_FirstName1);
		bpInfo[BP_FIRST_NAME_2]	= replaceAll(m_FirstName2);
		bpInfo[BP_LAST_NAME_1]	= replaceAll(m_LastName1);
		bpInfo[BP_LAST_NAME_2]	= replaceAll(m_LastName2);
		//	Return
		return bpInfo;
	}
	
	@Override
	public String getNameFile() {
		
		return m_NameFile;
	}

	/**
	 * Function that removes accents and special characters from a string of text, using the canonical method.
	 * @param input
	 * @return string of clean text of accents and special characters.
	 */
	public static String replaceAll(String input) {
	    String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
	    Pattern pattern = Pattern.compile("[^\\p{ASCII}]");
	    return pattern.matcher(normalized).replaceAll("");
	}
}

