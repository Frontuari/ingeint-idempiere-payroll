/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2007 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
/** Generated Model - DO NOT CHANGE */
package net.frontuari.model;

import java.sql.ResultSet;
import java.util.Properties;
import org.compiere.model.*;
import org.compiere.util.KeyNamePair;

/** Generated Model for LVE_HR_ProcessReportLine
 *  @author Adempiere (generated) 
 *  @version Release 3.7.0LTS (1252452765) - $Id$ */
public class X_LVE_HR_ProcessReportLine extends PO implements I_LVE_HR_ProcessReportLine, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20150119L;

    /** Standard Constructor */
    public X_LVE_HR_ProcessReportLine (Properties ctx, int LVE_HR_ProcessReportLine_ID, String trxName)
    {
      super (ctx, LVE_HR_ProcessReportLine_ID, trxName);
      /** if (LVE_HR_ProcessReportLine_ID == 0)
        {
			setHR_Concept_ID (0);
			setLVE_HR_ProcessReport_ID (0);
			setLVE_HR_ProcessReportLine_ID (0);
        } */
    }

    /** Load Constructor */
    public X_LVE_HR_ProcessReportLine (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 3 - Client - Org 
      */
    protected int get_AccessLevel()
    {
      return accessLevel.intValue();
    }

    /** Load Meta Data */
    protected POInfo initPO (Properties ctx)
    {
      POInfo poi = POInfo.getPOInfo (ctx, Table_ID, get_TrxName());
      return poi;
    }

    public String toString()
    {
      StringBuffer sb = new StringBuffer ("X_LVE_HR_ProcessReportLine[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	public org.eevolution.model.I_HR_Concept getHR_Concept() throws RuntimeException
    {
		return (org.eevolution.model.I_HR_Concept)MTable.get(getCtx(), org.eevolution.model.I_HR_Concept.Table_Name)
			.getPO(getHR_Concept_ID(), get_TrxName());	}

	/** Set Payroll Concept.
		@param HR_Concept_ID Payroll Concept	  */
	public void setHR_Concept_ID (int HR_Concept_ID)
	{
		if (HR_Concept_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_HR_Concept_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_HR_Concept_ID, Integer.valueOf(HR_Concept_ID));
	}

	/** Get Payroll Concept.
		@return Payroll Concept	  */
	public int getHR_Concept_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_HR_Concept_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public net.frontuari.model.I_LVE_HR_ProcessReport getLVE_HR_ProcessReport() throws RuntimeException
    {
		return (net.frontuari.model.I_LVE_HR_ProcessReport)MTable.get(getCtx(), net.frontuari.model.I_LVE_HR_ProcessReport.Table_Name)
			.getPO(getLVE_HR_ProcessReport_ID(), get_TrxName());	}

	/** Set HR Process Report.
		@param LVE_HR_ProcessReport_ID HR Process Report	  */
	public void setLVE_HR_ProcessReport_ID (int LVE_HR_ProcessReport_ID)
	{
		if (LVE_HR_ProcessReport_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_LVE_HR_ProcessReport_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_LVE_HR_ProcessReport_ID, Integer.valueOf(LVE_HR_ProcessReport_ID));
	}

	/** Get HR Process Report.
		@return HR Process Report	  */
	public int getLVE_HR_ProcessReport_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_LVE_HR_ProcessReport_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

    /** Get Record ID/ColumnName
        @return ID/ColumnName pair
      */
    public KeyNamePair getKeyNamePair() 
    {
        return new KeyNamePair(get_ID(), String.valueOf(getLVE_HR_ProcessReport_ID()));
    }

	/** Set HR Process Report Detail.
		@param LVE_HR_ProcessReportLine_ID HR Process Report Detail	  */
	public void setLVE_HR_ProcessReportLine_ID (int LVE_HR_ProcessReportLine_ID)
	{
		if (LVE_HR_ProcessReportLine_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_LVE_HR_ProcessReportLine_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_LVE_HR_ProcessReportLine_ID, Integer.valueOf(LVE_HR_ProcessReportLine_ID));
	}

	/** Get HR Process Report Detail.
		@return HR Process Report Detail	  */
	public int getLVE_HR_ProcessReportLine_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_LVE_HR_ProcessReportLine_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Print Text.
		@param PrintName 
		The label text to be printed on a document or correspondence.
	  */
	public void setPrintName (String PrintName)
	{
		set_Value (COLUMNNAME_PrintName, PrintName);
	}

	/** Get Print Text.
		@return The label text to be printed on a document or correspondence.
	  */
	public String getPrintName () 
	{
		return (String)get_Value(COLUMNNAME_PrintName);
	}

	/** Set Sequence.
		@param SeqNo 
		Method of ordering records; lowest number comes first
	  */
	public void setSeqNo (int SeqNo)
	{
		set_Value (COLUMNNAME_SeqNo, Integer.valueOf(SeqNo));
	}

	/** Get Sequence.
		@return Method of ordering records; lowest number comes first
	  */
	public int getSeqNo () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_SeqNo);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}
}