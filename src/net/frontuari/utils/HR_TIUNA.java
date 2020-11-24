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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.compiere.model.MBPartner;
import org.compiere.model.MSysConfig;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.eevolution.model.MHREmployee;
import org.eevolution.model.X_HR_Job;
import net.frontuari.model.MLVERVHRProcessDetail;

/**
 * @author <a href="mailto:yamelsenih@gmail.com">Yamel Senih</a>
 * Export class for BANAVIH in payroll
 */
public class HR_TIUNA implements HRReportExport {
	/** Logger										*/
	static private CLogger	s_log = CLogger.getCLogger (HR_TIUNA.class);
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
	/** BPartner Info Index for Employee Start Date	*/
	private static final int     EM_START_DATE 		= 6;
	/** BPartner Info Index for Employee End Date	*/
	private static final int     EM_END_DATE 		= 7;
	/** BPartner Info Index for Employee Worker Type*/
	private static final int     EM_WORKER_TYPE 	= 8;
	/** BPartner Info Index for Employee Job Name	*/
	private static final int     EM_JOB_NAME	 	= 9;
	/** BPartner Info Index for Employee Working Condition	*/
	private static final int     EM_WORKING_CONDITION	 	= 10;
	/** BPartner Info Index for Driving Skill	*/
	private static final int     BP_DRIVING_SKILL	 = 11;
	/** BPartner Info Index for State*/
	private static final int     BP_STATE	 = 12;
	/** BPartner Info Index for Municipality Name*/
	private static final int     BP_MUNICIPALITY_NAME = 13;
	/** BPartner Info Index for Location*/
	private static final int     BP_LOCATION	 = 14;
	/** BPartner Info Index for Phone*/
	private static final int     BP_PHONE	 = 15;
	/** BPartner Info Index for Phone*/
	private static final int     BP_PHONE2	 = 16;
	/** BPartner Info Index for Email*/
	private static final int     BP_EMAIL	 = 17;
	/** BPartner Info Index for Parish Name*/
	private static final int     BP_PARISH_NAME = 18;
	
	/**	Constant Payroll						*/
	private final String		PAYROLL_CONSTANT	= "N";
	/**	Constant Payroll Account				*/
	private String				PAYROLL_ACCOUNT		= "03213022183810020583";
	/**	File Extension							*/
	private final String		FILE_EXTENSION		= ".txt";
	/**	Separator								*/
	private final String 		SEPARATOR 			= ";";
	/**	Number Format							*/
//	private DecimalFormat 		m_NumberFormatt 	= null;
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
			String payrollAccount = MSysConfig.getValue("PAYROLL_ACCOUNT", Env.getAD_Client_ID(Env.getCtx()));
			if(payrollAccount != null) {
				PAYROLL_ACCOUNT = payrollAccount;
			}			
			//	Add Separator
			pathName.append(File.separator)
				//	
				.append(PAYROLL_CONSTANT)
				//	Payroll Account
				.append(PAYROLL_ACCOUNT)
				//	Accounting Date in format MM YYYY
				.append(new SimpleDateFormat("ddMMyyyy").format(pdl.getDateAcct()))
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
//		m_NumberFormatt = new DecimalFormat("000000000.00");
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
				//	Verify Current Business Partner and Month
				if(m_Current_BPartner_ID != pdl.getC_BPartner_ID()) {
					writeLine();
					m_Current_BPartner_ID = pdl.getC_BPartner_ID();
					m_CurrentMonth = m_ProcessDateFormat.format(pdl.getDateAcct());
					m_CurrentAmt = pdl.getAmt();
					m_Current_Pdl = pdl;
				} else if(m_CurrentMonth != null
						&& m_CurrentMonth.equals(m_ProcessDateFormat.format(pdl.getDateAcct()))) {
					//	m_CurrentAmt = m_CurrentAmt.add(pdl.getAmt());
					m_CurrentAmt = pdl.getAmt();
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
			//	Employee Start Date
			.append(bpInfo[EM_START_DATE])
			.append(SEPARATOR)
			//	Amount
			/*.append(m_NumberFormatt.format(m_CurrentAmt.doubleValue())
												.toString()
												.replace(",", ".")
												//.replace(".", "")
												)*/
			.append(m_CurrentAmt.toString().replace(",", "."))
			
			.append(SEPARATOR)
			//	Worker Type
			.append(bpInfo[EM_WORKER_TYPE])
			.append(SEPARATOR)
			//	Job
			.append(bpInfo[EM_JOB_NAME])
			.append(SEPARATOR)
			//	Working Condition
			.append(bpInfo[EM_WORKING_CONDITION])
			.append(SEPARATOR)
			//	Driving Skill
			.append(bpInfo[BP_DRIVING_SKILL])
			.append(SEPARATOR)
			//	State
			.append(bpInfo[BP_STATE] )
			.append(SEPARATOR)
			//	Municipality Name
			.append(bpInfo[BP_MUNICIPALITY_NAME])
			.append(SEPARATOR)
			//	Parish
			.append(bpInfo[BP_PARISH_NAME])
			.append(SEPARATOR)
			//	Location
			.append(bpInfo[BP_LOCATION])
			.append(SEPARATOR)
			//	Phone
			.append(bpInfo[BP_PHONE].toString().replace("-", ""))
			.append(SEPARATOR)
			//	Phone 2
			.append(bpInfo[BP_PHONE2].toString().replace("-", ""))
			.append(SEPARATOR)			
			//	Email
			.append(bpInfo[BP_EMAIL])
			.append(SEPARATOR)	
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
		//	Get Start Date
		String startDate = m_DateFormat.format(employee.getStartDate());
		String endDate = "";
		//	Get End Date
		if(employee.get_Value("DateFinish") != null)
			endDate = m_DateFormat.format(employee.get_Value("DateFinish"));
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
		bpInfo[BP_FIRST_NAME_1]	= HR_TIUNA.replaceAll(m_FirstName1);
		bpInfo[BP_FIRST_NAME_2]	= HR_TIUNA.replaceAll(m_FirstName2);
		bpInfo[BP_LAST_NAME_1]	= HR_TIUNA.replaceAll(m_LastName1);
		bpInfo[BP_LAST_NAME_2]	= HR_TIUNA.replaceAll(m_LastName2);
		bpInfo[EM_START_DATE]	= startDate;
		bpInfo[EM_END_DATE]		= endDate;
		bpInfo[EM_WORKER_TYPE]	= employee.get_ValueAsString("WorkerType");
		if(bpInfo[EM_WORKER_TYPE] == null)
			bpInfo[EM_WORKER_TYPE] = "NO WORKER TYPE";
		bpInfo[EM_JOB_NAME] = job;
		bpInfo[EM_WORKING_CONDITION] = employee.get_ValueAsString("WorkingCondition");
		if(bpInfo[EM_WORKING_CONDITION] == null)
			bpInfo[EM_WORKING_CONDITION] = "NO WORKING CONDITION";
		bpInfo[BP_DRIVING_SKILL] = bpartner.get_ValueAsString("DrivingSkill");
		if(bpInfo[BP_DRIVING_SKILL] == null)
			bpInfo[BP_DRIVING_SKILL] = "NO DRIVING SKILL";
		
		//	Sql
		String sql = "SELECT "
				+ "r.Value RegionValue,"	//	1
				+ "cnt.Name CountryName, "//	2
				+ "c.Name CityName, "	//	3	
				+ "parish.Value ParishValue, "	//	4
				+ "	municipality.Value MunicipalityValue,"	// 5
				+ "(COALESCE(l.Address1,'') || ' ' || COALESCE(l.Address2,'')|| ' ' || COALESCE(l.Address3,'') || ' ' || COALESCE(l.Address4,'') ) AS Location,  "	//	6
				+ "bpl.Phone, "	//	7
				+ "bpl.Phone2, " //	8
				+ "bpl.Email " //	9
				+ "FROM C_BPartner_Location bpl "
				+ "INNER JOIN C_Location l ON (bpl.C_Location_ID = l.C_Location_ID) "
				+ "INNER JOIN C_Country cnt ON (l.C_Country_ID = cnt.C_Country_ID) "
				+ "INNER JOIN C_Region r ON (l.C_Region_ID = r.C_Region_ID) "
				+ "INNER JOIN C_City c ON (l.C_City_ID = c.C_City_ID) "
				+ "INNER JOIN (SELECT l.Value, COALESCE(trl.Name, l.Name) AS Name "
				+ "FROM AD_Ref_List l "
				+ "INNER JOIN AD_Ref_List_Trl trl ON(l.AD_Ref_List_ID = trl.AD_Ref_List_ID)"
				+ "WHERE AD_Reference_ID=3000244 )  parish ON (bpl.Parish = parish.Value) "
				+ "INNER JOIN (SELECT l.Value, COALESCE(trl.Name, l.Name) AS Name "
				+ "FROM AD_Ref_List l"
				+ "	INNER JOIN AD_Ref_List_Trl trl ON(l.AD_Ref_List_ID = trl.AD_Ref_List_ID)"
				+ "	WHERE AD_Reference_ID=3000245"
				+ ")  municipality ON (bpl.Municipality = municipality.Value)"
				+ "WHERE C_BPartner_ID = ? AND bpl.IsActive ='Y' "
				+ "ORDER BY bpl.C_BPartner_Location_ID DESC ";
		
		s_log.fine("SQL=" + sql);
		
		try {
			PreparedStatement pstmt = DB.prepareStatement(sql, null);
			pstmt.setInt(1, bpartner.getC_BPartner_ID());
			ResultSet rs = pstmt.executeQuery();
			//
			if (rs.next()) {
				bpInfo[BP_STATE] = rs.getString("RegionValue");
				if (bpInfo[BP_STATE] == null)
					bpInfo[BP_STATE] = "NO ESTADO";
				bpInfo[BP_MUNICIPALITY_NAME] = rs.getString("MunicipalityName");
				if (bpInfo[BP_MUNICIPALITY_NAME] == null)
					bpInfo[BP_MUNICIPALITY_NAME] = "NO MUNICIPALITY";
				bpInfo[BP_PARISH_NAME] = rs.getString("ParisValue");
					if (bpInfo[BP_PARISH_NAME] == null)
						bpInfo[BP_PARISH_NAME] = "NO PARISH";
				bpInfo[BP_LOCATION] = rs.getString("Location");
				if (bpInfo[BP_LOCATION] == null)
					bpInfo[BP_LOCATION] = "NO LOCATION";
				bpInfo[BP_PHONE] = rs.getString("Phone2");
				if (bpInfo[BP_PHONE] == null)
					bpInfo[BP_PHONE] = "NO PHONE";
				bpInfo[BP_PHONE2] = rs.getString("Phone");
				if (bpInfo[BP_PHONE2] == null)
					bpInfo[BP_PHONE2] = "NO PHONE2";
				bpInfo[BP_EMAIL] = rs.getString("Email");
				if (bpInfo[BP_EMAIL] == null)
					bpInfo[BP_EMAIL] = "NO EMAIL";
			} else {
				bpInfo[BP_STATE] = "NO ESTADO";
				bpInfo[BP_MUNICIPALITY_NAME] = "NO MUNICIPALITY";
				bpInfo[BP_LOCATION] = "NO LOCATION";
				bpInfo[BP_PHONE] = "NO PHONE";
				bpInfo[BP_PHONE2] = "NO PHONE2";
				bpInfo[BP_EMAIL] = "NO EMAIL";
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e) {
			s_log.log(Level.SEVERE, sql, e);
		}
		
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

