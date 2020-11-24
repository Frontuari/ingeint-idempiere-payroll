/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
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
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package net.frontuari.utils;

import java.io.File;
import java.io.FileWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import net.frontuari.model.MLVERVHRProcessDetail;

/**
 * 	Generic Payment Export
 *  Sample implementation of Payment Export Interface - brought here from MPaySelectionCheck
 *	
 * 	@author 	Jorg Janke
 * 
 *  Contributors:
 *    Carlos Ruiz - GlobalQSS - FR 3132033 - Make payment export class configurable per bank
 */
public class GenericReportExport implements HRReportExport
{
	/** Logger								*/
	static private CLogger	s_log = CLogger.getCLogger (GenericReportExport.class);
	private static final int     BP_ID 		= 0;
	/** Info Index for BPartner       		*/
	private static final int     BP_NAME 	= 1;
	/** Info Index for Contact Name    		*/
	private static final int     BP_TAXID 	= 2;
	/** Info Index for Taxid   				*/
	
	/** Name File							*/
	private String 				 m_NameFile = null; 
	

	
	/**************************************************************************
	 *  Export to File
	 *  @param checks array of checks
	 *  @param file file to export checks
	 *  @return number of lines
	 */
	public int exportToFile (MLVERVHRProcessDetail[] checks, File file, StringBuffer err)
	{
		if (checks == null || checks.length == 0)
			return 0;
		//  Must be a file
		if (file.isDirectory())
		{
			err.append("No se puede escribir, el archivo seleccionado es un directorio - " + file.getAbsolutePath());
			s_log.log(Level.SEVERE, err.toString());
			return -1;
		}
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

		char x = '"';      //  ease
		int noLines = 0;
		StringBuffer line = null;
		try
		{
			FileWriter fw = new FileWriter(file);

			//  write header
			line = new StringBuffer();
			line.append(x).append("BPartner_ID").append(x).append(",")
			.append(x).append("Name").append(x).append(",")
			.append(x).append("TaxId").append(x).append(",")
			.append(Env.NL);
			fw.write(line.toString());
			noLines++;

			//  write lines
			for (int i = 0; i < checks.length; i++)
			{
				MLVERVHRProcessDetail mpp = checks[i];
				if (mpp == null)
					continue;
				//  BPartner Info
				String bp[] = getBPartnerInfo(mpp.getC_BPartner_ID());

				//  Comment - list of invoice document no
				StringBuffer comment = new StringBuffer();
				//	
				line = new StringBuffer();
				line.append(x).append(bp[BP_ID]).append(x).append(",")   // Value
				.append(x).append(bp[BP_NAME]).append(x).append(",") // Name
				.append(x).append(bp[BP_TAXID]).append(x).append(",")// Contact
				.append(x).append(comment.toString()).append(x)     // Comment
				.append(Env.NL);
				fw.write(line.toString());
				noLines++;
			}   //  write line

			fw.flush();
			fw.close();
			
			// Raul Munoz set Name file for zk 
			m_NameFile = file.getName();
		}
		catch (Exception e)
		{
			err.append(e.toString());
			s_log.log(Level.SEVERE, "", e);
			return -1;
		}

		return noLines;
	}   //  exportToFile

	/**
	 *  Get Customer/Vendor Info.
	 *  Based on BP_ static variables
	 *  @param C_BPartner_ID BPartner
	 *  @return info array
	 */
	private static String[] getBPartnerInfo (int C_BPartner_ID)
	{
		String[] bp = new String[1];

		String sql = "SELECT pd.C_BPartner_ID " +
				"from LVE_RV_HR_ProcessDetail pd";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql, null);
			pstmt.setInt(1, C_BPartner_ID);
			ResultSet rs = pstmt.executeQuery();
			//
			if (rs.next())
			{
				
				bp[BP_ID] = rs.getString(1);
				if (bp[BP_ID] == null)
					bp[BP_ID] = "";
				}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.log(Level.SEVERE, sql, e);
		}
		return bp;
	}   //  getBPartnerInfo

	@Override
	public String getNameFile() {
		return m_NameFile;
	}

	
}	//	PaymentExport
