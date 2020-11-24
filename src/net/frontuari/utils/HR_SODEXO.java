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
import java.text.DecimalFormat;
import java.util.logging.Level;

import org.compiere.util.CLogger;
import org.compiere.util.Env;
import net.frontuari.model.MLVERVHRProcessDetail;


/**
 * @author <a href="mailto:yamelsenih@gmail.com">Yamel Senih</a>
 * Export class for SODEXO PASS in payroll
 */
public class HR_SODEXO implements HRReportExport {
	/** Logger									*/
	static private CLogger	s_log = CLogger.getCLogger (HR_SODEXO.class);
	/** Name File								*/
	private String 			m_NameFile		= null;
	
	@Override
	public int exportToFile(MLVERVHRProcessDetail[] details, File file, StringBuffer err) {
		if (details == null || details.length == 0)
			return 0;
		//  delete if exists
		try {
			if (file.exists())
				file.delete();
		} catch (Exception e) {
			s_log.log(Level.WARNING, "Could not delete - " + file.getAbsolutePath(), e);
		}
		String separator = ",";
		int noLines = 0;
		//	
		StringBuffer line = null;
		DecimalFormat numberFormatt = new DecimalFormat("#########.##");
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
				MLVERVHRProcessDetail pdl = details[i];
				if (pdl == null)
					continue;
				//	Line
				line = new StringBuffer();
				//	Amount
				BigDecimal m_Amt = pdl.getAmt();
				if(m_Amt == null)
					m_Amt = Env.ZERO;
				//	New Line
				if(i != 0)
					line.append(Env.NL);
				//	Tax ID
				line.append(pdl.getBPTaxID())
					.append(separator)
					//	Last Name
					.append(pdl.getName2())
					.append(separator)
					//	Name
					.append(pdl.getName())
					.append(separator)
					//	Amount
					.append(numberFormatt.format(m_Amt.doubleValue())
														.toString()
														.replaceAll(",", "."));
				//	Write Line
				fw.write(line.toString());
				noLines++;
				
				// Raul Munoz set Name file for zk 
				m_NameFile = file.getName();
			}   //  write line
			//	Close
			fw.flush();
			fw.close();
		} catch (Exception e) {
			err.append(e.toString());
			s_log.log(Level.SEVERE, "", e);
			return -1;
		}
		//	
		return noLines;
	}

	@Override
	public String getNameFile() {
		return m_NameFile;
	}
}