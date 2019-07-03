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

/** Generated Model for HR_Basic_Factor_Type
 *  @author iDempiere (generated) 
 *  @version Release 2.1 - $Id$ */
public class X_HR_Basic_Factor_Type extends PO implements I_HR_Basic_Factor_Type, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20151016L;

    /** Standard Constructor */
    public X_HR_Basic_Factor_Type (Properties ctx, int HR_Basic_Factor_Type_ID, String trxName)
    {
      super (ctx, HR_Basic_Factor_Type_ID, trxName);
      /** if (HR_Basic_Factor_Type_ID == 0)
        {
			setHR_Basic_Factor_Type_ID (0);
			setName (null);
        } */
    }

    /** Load Constructor */
    public X_HR_Basic_Factor_Type (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_HR_Basic_Factor_Type[")
        .append(get_ID()).append("]");
      return sb.toString();
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

	/** Set HR_Basic_Factor_Type.
		@param HR_Basic_Factor_Type_ID HR_Basic_Factor_Type	  */
	public void setHR_Basic_Factor_Type_ID (int HR_Basic_Factor_Type_ID)
	{
		if (HR_Basic_Factor_Type_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_HR_Basic_Factor_Type_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_HR_Basic_Factor_Type_ID, Integer.valueOf(HR_Basic_Factor_Type_ID));
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

	/** Set HR_Basic_Factor_Type_UU.
		@param HR_Basic_Factor_Type_UU HR_Basic_Factor_Type_UU	  */
	public void setHR_Basic_Factor_Type_UU (String HR_Basic_Factor_Type_UU)
	{
		set_Value (COLUMNNAME_HR_Basic_Factor_Type_UU, HR_Basic_Factor_Type_UU);
	}

	/** Get HR_Basic_Factor_Type_UU.
		@return HR_Basic_Factor_Type_UU	  */
	public String getHR_Basic_Factor_Type_UU () 
	{
		return (String)get_Value(COLUMNNAME_HR_Basic_Factor_Type_UU);
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

	/** Set Percent.
		@param Percent 
		Percentage
	  */
	public void setPercent (BigDecimal Percent)
	{
		set_Value (COLUMNNAME_Percent, Percent);
	}

	/** Get Percent.
		@return Percentage
	  */
	public BigDecimal getPercent () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_Percent);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}
}