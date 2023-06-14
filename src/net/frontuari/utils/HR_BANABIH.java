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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Level;

import org.compiere.model.MBPartner;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.eevolution.model.MHREmployee;
import net.frontuari.model.MLVERVHRProcessDetail;

/**
 * @author <a href="mailto:yamelsenih@gmail.com">Yamel Senih</a>
 * Export class for BANAVIH in payroll
 */
public class HR_BANABIH implements HRReportExport {
	/** Logger										*/
	static private CLogger	s_log = CLogger.getCLogger (HR_BANABIH.class);
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
	
	/**	Constant Payroll						*/
	private final String		PAYROLL_CONSTANT	= "N";
	/**	Constant Payroll Account				*/
	private final String		PAYROLL_ACCOUNT		= "03213022183810020583";
	/**	File Extension							*/
	private final String		FILE_EXTENSION		= ".txt";
	/**	Separator								*/
	private final String 		SEPARATOR 			= ",";
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
	
	
	@Override
	public int exportToFile(MLVERVHRProcessDetail[] details, File file, StringBuffer err) {
		if (details == null || details.length == 0)
			return 0;
		MLVERVHRProcessDetail pdl = details[0];
		
		//  delete if exists
		try
		{
			if (file.exists())
				file.delete();
		}
		catch (Exception e)
		{
			s_log.log(Level.WARNING, "Could not delete - " + file.getAbsolutePath(), e);
		}
		
		//	Number Format
		m_NumberFormatt = new DecimalFormat("000000000.00");
		//	Date Format
		m_DateFormat = new SimpleDateFormat("ddMMyyyy");
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
					m_CurrentAmt = m_CurrentAmt.add(pdl.getAmt());
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
		//	Amount
		if(m_CurrentAmt == null)
			m_CurrentAmt = Env.ZERO;
		//	New Line
		if(m_NoLines > 1)
			line.append(Env.NL);
		//	Nationality
		line.append(bpInfo[BP_NATIONALITY])
			.append(SEPARATOR)
			//	Tax ID
			.append(bpInfo[BP_TAX_ID])
			.append(SEPARATOR)
			//	First Name 1
			.append(bpInfo[BP_FIRST_NAME_1])
			.append(SEPARATOR)
			//	First Name 2
			.append(bpInfo[BP_FIRST_NAME_2])
			.append(SEPARATOR)
			//	Last Name 1
			.append(bpInfo[BP_LAST_NAME_1])
			.append(SEPARATOR)
			//	Last Name 2
			.append(bpInfo[BP_LAST_NAME_2])
			.append(SEPARATOR)
			//	Amount
			.append(m_NumberFormatt.format(m_CurrentAmt.doubleValue())
												.toString()
												.replace(",", ".")
												.replace(".", ""))
			.append(SEPARATOR)
			//	Employee Start Date
			.append(bpInfo[EM_START_DATE])
			.append(SEPARATOR)
			//	Employee End Date
			.append(bpInfo[EM_END_DATE]);
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
		String [] bpInfo = new String[8];
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
		//	Set Array
		bpInfo[BP_NATIONALITY]	= bpartner.get_ValueAsString("Nationality");
		bpInfo[BP_TAX_ID]		= bpartner.getValue();
		bpInfo[BP_FIRST_NAME_1]	= m_FirstName1;
		bpInfo[BP_FIRST_NAME_2]	= m_FirstName2;
		bpInfo[BP_LAST_NAME_1]	= m_LastName1;
		bpInfo[BP_LAST_NAME_2]	= m_LastName2;
		bpInfo[EM_START_DATE]	= startDate;
		bpInfo[EM_END_DATE]		= endDate;
		//	Return
		return bpInfo;
	}
	
	@Override
	public String getNameFile() {
		
		return m_NameFile;
	}

	@Override
	public String getFilenamePrefix() {
		//Set new File Name
		StringBuffer pathName = new StringBuffer();
		//	Add Separator
		pathName.append(PAYROLL_CONSTANT)
			//	Payroll Account
			.append(PAYROLL_ACCOUNT)
			//	Accounting Date in format MM YYYY
			.append(new SimpleDateFormat("MMyyyy").format(System.currentTimeMillis()));
		return pathName.toString();
	}
	
	@Override
	public String getFilenameSuffix() {
		return FILE_EXTENSION;
	}

	@Override
	public String getContentType() {
		return "text/plain";
	}
	
	
}

