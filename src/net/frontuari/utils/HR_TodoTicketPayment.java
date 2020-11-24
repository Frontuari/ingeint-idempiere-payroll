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
import java.text.SimpleDateFormat;
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
 * Export class for TodoTicket Payment in payroll
 */
public class HR_TodoTicketPayment implements HRReportExport {
	/** Logger										*/
	static private CLogger	s_log = CLogger.getCLogger (HR_TodoTicketPayment.class);
	/** BPartner Info Index for Nationality	    	*/
	private static final int     BP_NATIONALITY 	= 0;
	/** BPartner Info Index for Tax ID		    	*/
	private static final int     BP_TAX_ID 			= 1;
	
	/**	Constant Payroll						*/
	private final String		PAYROLL_CONSTANT	= "ABONOS";
	/**	File Extension							*/
	private final String		FILE_EXTENSION		= ".txt";
	/**	Separator								*/
	private final String 		SEPARATOR 			= " ";
	/**	Current Process Report Line				*/
	private MLVERVHRProcessDetail 	m_Current_Pdl 	= null;
	/**	File Writer								*/
	private FileWriter 				m_FileWriter	= null;
	/**	Number Lines							*/
	private int 					noLines 		= 0;
	/**	Number Format							*/
	private DecimalFormat 			numberFormat 	= null;
	/**	Date Format								*/
	private SimpleDateFormat 		dateFormat 		= null;
	/**	Current Amount							*/
	private BigDecimal 				currentAmt		= null;
	/**	Current Date							*/
	private Timestamp 				currentDate		= null;
	/** Name File								*/
	private String 					m_NameFile		= null;
	
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
			//	Suffix
			MLVEHRProcessReport pr = (MLVEHRProcessReport) pdl.getLVE_HR_ProcessReport();
			String suffix = null;
			if(pr != null) {
				suffix = pr.getPrintName();
			}
			//	Number Format
			numberFormat = new DecimalFormat("#########.00");
			//	Date Format
			dateFormat = new SimpleDateFormat("ddMMyyyy");
			//	Get Current Date
			currentDate = pdl.getDateAcct();
			//	Add Separator
			pathName.append(File.separator)
				//	
				.append(PAYROLL_CONSTANT)
				//	Payroll Account
				.append(clientInfo.get_ValueAsString("Feeding_Code") == null? "": clientInfo.get_ValueAsString("Feeding_Code").trim())
				//	Suffix
				.append(suffix != null? suffix.trim(): "")
				//	Accounting Date in format dd-MM-yyyy
				.append(new SimpleDateFormat("dd-MM-yyyy").format(currentDate))
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
					if(!bpartner.get_ValueAsBoolean("IsFeedingEnrolled")) {
						continue;
					}
				}
				//	Get Amount
				currentAmt = pdl.getAmt();
				m_Current_Pdl = pdl;
				//	Write
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
		if(currentAmt == null)
			currentAmt = Env.ZERO;
		//	Amount format
		String amount = numberFormat.format(currentAmt.doubleValue())
				.toString()
				.replace(",", ".")
				.replace(".", "");
		
		//	New Line
		if(noLines > 0)
			line.append(Env.NL);
		
		//	Tax ID
		line.append(bpInfo[BP_NATIONALITY])
			.append(String.format("%1$" + 9 + "s", bpInfo[BP_TAX_ID]).replace(" ", "0"))
			.append(SEPARATOR).append(SEPARATOR)
		//	Amount
		.append(String.format("%1$" + 21 + "s", amount).replace(" ", "0"))
		//	Date
		.append(dateFormat.format(currentDate))
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
		String [] bpInfo = new String[2];
		//	
		//	Get Business Partner
		MBPartner bpartner = MBPartner.get(Env.getCtx(), p_C_BPartner_ID);
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
		//	Return
		return bpInfo;
	}
	
	@Override
	public String getNameFile() {
		return m_NameFile;
	}
}