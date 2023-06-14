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

import net.frontuari.model.MLVERVHRProcessDetail;

/**
 * 	Custom Report Export Interface
 *	
 *  @author Jorge Colmenarez - Frontuari
 *  @version HRReportExport.java
 */
public interface HRReportExport {
	/**************************************************************************
	 *  Export to File
	 *  @param checks array of checks
	 *  @param file file to export checks
	 *  @return number of lines
	 */
	public int exportToFile (MLVERVHRProcessDetail[] checks, File file, StringBuffer err);

	/**
	 *  Get Name File
	 *  @author Raul Muñoz
	 *  @return Name File
	 */
	public String getNameFile();
	
	/**
	 * Get the filename prefix from plugin
	 * e.g. "SEPA-Credit-Transfer-"
	 * @return prefix for filename 
	 */
	public default String getFilenamePrefix() {
		return "payrollExport";
	}

	/**
	 * Get the filename suffix from plugin
	 * e.g. ".xml" 
	 * @return suffix for filename
	 */
	public default String getFilenameSuffix() {
		return ".txt";
	}

	/**
	 * Get the content type from plugin
	 * e.g. "text/xml" or "text/csv"
	 * @return content type delivered to browser
	 */
	public default String getContentType() {
		return "text/plain";
	}
}
