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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.compiere.model.MBPartner;
import org.compiere.model.MClientInfo;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.eevolution.model.MHREmployee;
import net.frontuari.model.MLVEHRProcessReport;
import net.frontuari.model.MLVERVHRProcessDetail;

/**
 * @author <a href="mailto:yamelsenih@gmail.com">Yamel Senih</a>
 * Export class for TodoTicket Enrollment in payroll
 */
public class HR_TodoTicketEnrollment implements HRReportExport {
	/** Logger										*/
	static private CLogger	s_log = CLogger.getCLogger (HR_TodoTicketEnrollment.class);
	/** BPartner Info Index for Nationality	    	*/
	private static final int     BP_NATIONALITY 	= 0;
	/** BPartner Info Index for Tax ID		    	*/
	private static final int     BP_TAX_ID 			= 1;
	/** BPartner Info Index for First Name 1    	*/
	private static final int     BP_FIRST_NAME 		= 2;
	/** BPartner Info Index for First Name 1    	*/
	private static final int     BP_FIRST_NAME_2 	= 3;
	/** BPartner Info Index for First Name 1    	*/
	private static final int     BP_LAST_NAME 		= 4;
	/** BPartner Info Index for First Name 1    	*/
	private static final int     BP_LAST_NAME_2 	= 5;
	/** BPartner Info Index for First Name 1    	*/
	private static final int     BP_SHOW_NAME 		= 6;
	/** BPartner Info Index for Employee Marital St	*/
	private static final int     EM_MARITAL_STATUS 	= 7;
	/** BPartner Info Index for Employee Gender		*/
	private static final int     EM_GENDER 			= 8;
	/** BPartner Info Index for Employee End Date	*/
	private static final int     EM_BIRTHDAY 		= 9;
	
	/**	Constant Payroll						*/
	private String				feeding_Name		= null;
	/** Name File								*/
	private String 				m_NameFile		= null;
	
	/**	Constant Payroll						*/
	private final String		PAYROLL_CONSTANT	= "EMITAR";
	/**	File Extension							*/
	private final String		FILE_EXTENSION		= ".txt";
	/**	Separator								*/
	private final String 		SEPARATOR 			= " ";
	/**	Current Amount							*/
	private BigDecimal 			m_CurrentAmt		= null;
	/**	Current Process Report Line				*/
	private MLVERVHRProcessDetail 	m_Current_Pdl 	= null;
	/**	File Writer								*/
	private FileWriter 				m_FileWriter	= null;
	/**	Number Lines							*/
	private int 					noLines 		= 0;
	
	
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
			//	Get Client account
			MClientInfo clientInfo = MClientInfo.get(Env.getCtx(), pdl.getAD_Client_ID());
			//	Feeding Name
			feeding_Name = clientInfo.get_ValueAsString("Feeding_Name") == null
								? ""
								: clientInfo.get_ValueAsString("Feeding_Name").trim();
			//	Suffix
			MLVEHRProcessReport pr = (MLVEHRProcessReport) pdl.getLVE_HR_ProcessReport();
			String suffix = null;
			if(pr != null) {
				suffix = pr.getPrintName();
			}
			//	Add Separator
			pathName.append(File.separator)
				//	
				.append(PAYROLL_CONSTANT)
				//	Payroll Account
				.append(clientInfo.get_ValueAsString("Feeding_Code") == null? "": clientInfo.get_ValueAsString("Feeding_Code").trim())
				//	Suffix
				.append(suffix != null? suffix.trim(): "")
				//	Extension
				.append(FILE_EXTENSION);
			
			file = new File(pathName.toString());
			//	Delete if Exists
			if (file.exists())
				file.delete();
		} catch (Exception e) {
			s_log.log(Level.WARNING, "Could not delete - " + file.getAbsolutePath(), e);
		}
		//	Current Business Partner
		int currentBPartnerId = 0;
		//	
		try {
			//	
			m_FileWriter = new FileWriter(file);
			//  write lines
			for (int i = 0; i < details.length; i++) {
				pdl = details[i];
				if (pdl == null)
					continue;
				//	Verify Current Business Partner and Month
				if(currentBPartnerId != pdl.getC_BPartner_ID()) {
					currentBPartnerId = pdl.getC_BPartner_ID();
					MBPartner bpartner = new MBPartner(Env.getCtx(), currentBPartnerId, null);
					if(bpartner.get_ValueAsBoolean("IsFeedingEnrolled")) {
						continue;
					}
					bpartner.set_ValueOfColumn("IsFeedingEnrolled", true);
					bpartner.saveEx();
				}
				//	Write
				m_Current_Pdl = pdl;
				writeLine();
			}
			// Raul Munoz set Name file for zk 
			m_NameFile = file.getName();
			//	Close
			m_FileWriter.flush();
			m_FileWriter.close();
		} catch (Exception e) {
			err.append(e.toString());
			s_log.log(Level.SEVERE, "", e);
			return -1;
		}
		//
		return noLines;
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
		
		if(bpInfo[BP_NATIONALITY] == null)
			return;
		
		//	Line
		StringBuffer line = new StringBuffer();
		//	Amount
		if(m_CurrentAmt == null)
			m_CurrentAmt = Env.ZERO;
		//	New Line
		if(noLines > 0)
			line.append(Env.NL);
		
		//	Tax ID
		line.append(bpInfo[BP_NATIONALITY])
			.append(String.format("%1$" + 9 + "s", bpInfo[BP_TAX_ID]).replace(" ", "0"))
			.append(SEPARATOR).append(SEPARATOR)
		//	Card Name
		.append(String.format("%1$-" + 21 + "s", bpInfo[BP_SHOW_NAME]))
		//	First Name 1
		.append(String.format("%1$-" + 20 + "s", bpInfo[BP_FIRST_NAME]))
		//	First Name 2
		.append(String.format("%1$-" + 20 + "s", bpInfo[BP_FIRST_NAME_2]))
		//	Last Name 1
		.append(String.format("%1$-" + 20 + "s", bpInfo[BP_LAST_NAME]))
		//	Last Name 2
		.append(String.format("%1$-" + 20 + "s", bpInfo[BP_LAST_NAME_2]))
		//	BirthDay
		.append(String.format("%1$-" + 8 + "s", bpInfo[EM_BIRTHDAY]))
		//	Marital Status
		.append(bpInfo[EM_MARITAL_STATUS])
		//	Gender
		.append(bpInfo[EM_GENDER])
		//	Feeding Name
		.append(feeding_Name)
		;
		//	Write Line
		m_FileWriter.write(line.toString());
		noLines ++;
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
		String [] bpInfo = new String[10];
		//	
		//	Get Business Partner
		MBPartner bpartner = MBPartner.get(Env.getCtx(), p_C_BPartner_ID);
		//	Get Name
		String name = bpartner.getName();
		String name2 = bpartner.getName2();
		//	Valid Null
		if(name == null) {
			name = "";
		}
		//	For name 2
		if(name2 == null) {
			name2 = "";
		}
		//	Get Names
		String names[] = name.split(" ");
		String names2[] = name2.split(" ");
		//	Convert name to List
		List<String> nameList = new ArrayList<String>();
		for(String nameForList : names) {
			if(nameForList != null
					&& nameForList.trim().length() > 0) {
				nameList.add(nameForList);
			}
		}
		//	Convert Name 2 to list
		List<String> name2List = new ArrayList<String>();
		for(String nameForList : names2) {
			if(nameForList != null
					&& nameForList.trim().length() > 0) {
				name2List.add(nameForList);
			}
		}
		//	Extract First Name 1
		String m_FirstName1 = (nameList.size() > 0? nameList.get(0): "").toUpperCase();
		//	Extract First Name 2
		String m_FirstName2 = (nameList.size() > 1? nameList.get(1): "").toUpperCase();
		//	Extract Last Name 1
		String m_LastName1 = (name2List.size() > 0? name2List.get(0): "").toUpperCase();
		//	Extract Last Name 2
		String m_LastName2 = (name2List.size() > 1? name2List.get(1): "").toUpperCase();
		//	Valid length
		if(m_FirstName1.length() > 20)
			m_FirstName1 = m_FirstName1.substring(0, 19);
		if(m_FirstName2.length() > 20)
			m_FirstName2 = m_FirstName2.substring(0, 19);
		if(m_LastName1.length() > 20)
			m_LastName1 = m_LastName1.substring(0, 19);
		if(m_LastName2.length() > 20)
			m_LastName2 = m_LastName2.substring(0, 19);
		//	Get Active Employee
		MHREmployee employee = MHREmployee.getActiveEmployee(Env.getCtx(), 
				bpartner.getC_BPartner_ID(), p_AD_Org_ID, p_TrxName);
		//	Valid Employee
		if(employee == null)
			return null;
		//	Get BP data
		//	Set Array
		bpInfo[BP_NATIONALITY]		= bpartner.get_ValueAsString("Nationality");
		bpInfo[BP_TAX_ID]			= bpartner.getValue().replaceAll("[A-Z]","")
															.replaceAll("[a-z]","")
															.replaceAll("[+^:&áàäéèëíìïóòöúùuñÁÀÄÉÈËÍÌÏÓÒÖÚÙÜÑçÇ$#]","");
		bpInfo[BP_FIRST_NAME]		= m_FirstName1;
		bpInfo[BP_FIRST_NAME_2]		= m_FirstName2;
		bpInfo[BP_LAST_NAME]		= m_LastName1;
		bpInfo[BP_LAST_NAME_2]		= m_LastName2;
		bpInfo[BP_SHOW_NAME]		= m_LastName1 + SEPARATOR + m_FirstName1;
		bpInfo[EM_MARITAL_STATUS]	= bpartner.get_ValueAsString("MaritalStatus");
		bpInfo[EM_GENDER]			= bpartner.get_ValueAsString("Gender");
		//	For Marital Status
		if(bpInfo[EM_MARITAL_STATUS] == null 
				|| bpInfo[EM_MARITAL_STATUS].equals("S")) {
			bpInfo[EM_MARITAL_STATUS] = "SO";
		} else {
			bpInfo[EM_MARITAL_STATUS] = "CA";
		}
		//	For Gender
		if(bpInfo[EM_GENDER] == null) {
			bpInfo[EM_GENDER] = "M";
		}
		//	For Birthday
		if(bpartner.get_Value("BirthDay") != null) {
			bpInfo[EM_BIRTHDAY]	= new SimpleDateFormat("ddMMyyyy")
											.format(((Timestamp)bpartner.get_Value("BirthDay")));
		}
		//	Return
		return bpInfo;
	}
	
	@Override
	public String getNameFile() {
		return m_NameFile;
	}
}