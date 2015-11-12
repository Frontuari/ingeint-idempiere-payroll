/******************************************************************************
 * Product: iDempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2012 ComPiere, Inc. All Rights Reserved.                *
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
package com.ingeint.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Properties;
import org.compiere.model.*;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;

/** Generated Model for HR_GAP
 *  @author iDempiere (generated) 
 *  @version Release 2.1 - $Id$ */
public class X_HR_GAP extends PO implements I_HR_GAP, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20151016L;

    /** Standard Constructor */
    public X_HR_GAP (Properties ctx, int HR_GAP_ID, String trxName)
    {
      super (ctx, HR_GAP_ID, trxName);
      /** if (HR_GAP_ID == 0)
        {
			setHR_Basic_Factor_Type_ID (0);
// 1000000
			setHR_GAP_ID (0);
			setName (null);
        } */
    }

    /** Load Constructor */
    public X_HR_GAP (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_HR_GAP[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	public org.compiere.model.I_C_Year getC_Year() throws RuntimeException
    {
		return (org.compiere.model.I_C_Year)MTable.get(getCtx(), org.compiere.model.I_C_Year.Table_Name)
			.getPO(getC_Year_ID(), get_TrxName());	}

	/** Set Year.
		@param C_Year_ID 
		Calendar Year
	  */
	public void setC_Year_ID (int C_Year_ID)
	{
		if (C_Year_ID < 1) 
			set_Value (COLUMNNAME_C_Year_ID, null);
		else 
			set_Value (COLUMNNAME_C_Year_ID, Integer.valueOf(C_Year_ID));
	}

	/** Get Year.
		@return Calendar Year
	  */
	public int getC_Year_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Year_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Description.
		@param Description 
		Optional short description of the record
	  */
	public void setDescription (String Description)
	{
		set_Value (COLUMNNAME_Description, Description);
	}

	/** Get Description.
		@return Optional short description of the record
	  */
	public String getDescription () 
	{
		return (String)get_Value(COLUMNNAME_Description);
	}

	public com.ingeint.model.I_HR_Basic_Factor_Type getHR_Basic_Factor_Type() throws RuntimeException
    {
		return (com.ingeint.model.I_HR_Basic_Factor_Type)MTable.get(getCtx(), com.ingeint.model.I_HR_Basic_Factor_Type.Table_Name)
			.getPO(getHR_Basic_Factor_Type_ID(), get_TrxName());	}

	/** Set HR_Basic_Factor_Type.
		@param HR_Basic_Factor_Type_ID HR_Basic_Factor_Type	  */
	public void setHR_Basic_Factor_Type_ID (int HR_Basic_Factor_Type_ID)
	{
		if (HR_Basic_Factor_Type_ID < 1) 
			set_Value (COLUMNNAME_HR_Basic_Factor_Type_ID, null);
		else 
			set_Value (COLUMNNAME_HR_Basic_Factor_Type_ID, Integer.valueOf(HR_Basic_Factor_Type_ID));
	}

	/** Get HR_Basic_Factor_Type.
		@return HR_Basic_Factor_Type	  */
	public int getHR_Basic_Factor_Type_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_HR_Basic_Factor_Type_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set HR_ConceptAmount.
		@param HR_ConceptAmount HR_ConceptAmount	  */
	public void setHR_ConceptAmount (BigDecimal HR_ConceptAmount)
	{
		throw new IllegalArgumentException ("HR_ConceptAmount is virtual column");	}

	/** Get HR_ConceptAmount.
		@return HR_ConceptAmount	  */
	public BigDecimal getHR_ConceptAmount () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_HR_ConceptAmount);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set HR_ConceptFactor.
		@param HR_ConceptFactor HR_ConceptFactor	  */
	public void setHR_ConceptFactor (BigDecimal HR_ConceptFactor)
	{
		set_Value (COLUMNNAME_HR_ConceptFactor, HR_ConceptFactor);
	}

	/** Get HR_ConceptFactor.
		@return HR_ConceptFactor	  */
	public BigDecimal getHR_ConceptFactor () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_HR_ConceptFactor);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set HR_GAP_Current_Amount.
		@param HR_GAP_Current_Amount HR_GAP_Current_Amount	  */
	public void setHR_GAP_Current_Amount (BigDecimal HR_GAP_Current_Amount)
	{
		set_Value (COLUMNNAME_HR_GAP_Current_Amount, HR_GAP_Current_Amount);
	}

	/** Get HR_GAP_Current_Amount.
		@return HR_GAP_Current_Amount	  */
	public BigDecimal getHR_GAP_Current_Amount () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_HR_GAP_Current_Amount);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set HR_GAP.
		@param HR_GAP_ID HR_GAP	  */
	public void setHR_GAP_ID (int HR_GAP_ID)
	{
		if (HR_GAP_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_HR_GAP_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_HR_GAP_ID, Integer.valueOf(HR_GAP_ID));
	}

	/** Get HR_GAP.
		@return HR_GAP	  */
	public int getHR_GAP_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_HR_GAP_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set HR_GAP_UU.
		@param HR_GAP_UU HR_GAP_UU	  */
	public void setHR_GAP_UU (String HR_GAP_UU)
	{
		set_Value (COLUMNNAME_HR_GAP_UU, HR_GAP_UU);
	}

	/** Get HR_GAP_UU.
		@return HR_GAP_UU	  */
	public String getHR_GAP_UU () 
	{
		return (String)get_Value(COLUMNNAME_HR_GAP_UU);
	}

	/** Set HR_GAPxFactor.
		@param HR_GAPxFactor HR_GAPxFactor	  */
	public void setHR_GAPxFactor (BigDecimal HR_GAPxFactor)
	{
		throw new IllegalArgumentException ("HR_GAPxFactor is virtual column");	}

	/** Get HR_GAPxFactor.
		@return HR_GAPxFactor	  */
	public BigDecimal getHR_GAPxFactor () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_HR_GAPxFactor);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Name.
		@param Name 
		Alphanumeric identifier of the entity
	  */
	public void setName (String Name)
	{
		set_Value (COLUMNNAME_Name, Name);
	}

	/** Get Name.
		@return Alphanumeric identifier of the entity
	  */
	public String getName () 
	{
		return (String)get_Value(COLUMNNAME_Name);
	}

    /** Get Record ID/ColumnName
        @return ID/ColumnName pair
      */
    public KeyNamePair getKeyNamePair() 
    {
        return new KeyNamePair(get_ID(), getName());
    }
}