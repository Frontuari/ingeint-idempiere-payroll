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

/** Generated Model for HR_SectorCode
 *  @author iDempiere (generated) 
 *  @version Release 5.1 - $Id$ */
public class X_HR_SectorCode extends PO implements I_HR_SectorCode, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20181226L;

    /** Standard Constructor */
    public X_HR_SectorCode (Properties ctx, int HR_SectorCode_ID, String trxName)
    {
      super (ctx, HR_SectorCode_ID, trxName);
      /** if (HR_SectorCode_ID == 0)
        {
			setHR_SectorCode_ID (0);
        } */
    }

    /** Load Constructor */
    public X_HR_SectorCode (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_HR_SectorCode[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set Charge Name.
		@param ChargeName 
		Name of the Charge
	  */
	public void setChargeName (String ChargeName)
	{
		set_Value (COLUMNNAME_ChargeName, ChargeName);
	}

	/** Get Charge Name.
		@return Name of the Charge
	  */
	public String getChargeName () 
	{
		return (String)get_Value(COLUMNNAME_ChargeName);
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

	/** Set EstimatedSalary.
		@param EstimatedSalary EstimatedSalary	  */
	public void setEstimatedSalary (BigDecimal EstimatedSalary)
	{
		set_Value (COLUMNNAME_EstimatedSalary, EstimatedSalary);
	}

	/** Get EstimatedSalary.
		@return EstimatedSalary	  */
	public BigDecimal getEstimatedSalary () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_EstimatedSalary);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set HR_SectorCode.
		@param HR_SectorCode_ID HR_SectorCode	  */
	public void setHR_SectorCode_ID (int HR_SectorCode_ID)
	{
		if (HR_SectorCode_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_HR_SectorCode_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_HR_SectorCode_ID, Integer.valueOf(HR_SectorCode_ID));
	}

	/** Get HR_SectorCode.
		@return HR_SectorCode	  */
	public int getHR_SectorCode_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_HR_SectorCode_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set HR_SectorCode_UU.
		@param HR_SectorCode_UU HR_SectorCode_UU	  */
	public void setHR_SectorCode_UU (String HR_SectorCode_UU)
	{
		set_Value (COLUMNNAME_HR_SectorCode_UU, HR_SectorCode_UU);
	}

	/** Get HR_SectorCode_UU.
		@return HR_SectorCode_UU	  */
	public String getHR_SectorCode_UU () 
	{
		return (String)get_Value(COLUMNNAME_HR_SectorCode_UU);
	}

	/** Set Comment/Help.
		@param Help 
		Comment or Hint
	  */
	public void setHelp (String Help)
	{
		set_Value (COLUMNNAME_Help, Help);
	}

	/** Get Comment/Help.
		@return Comment or Hint
	  */
	public String getHelp () 
	{
		return (String)get_Value(COLUMNNAME_Help);
	}

	/** Set OccupationalStructure.
		@param OccupationalStructure OccupationalStructure	  */
	public void setOccupationalStructure (String OccupationalStructure)
	{
		set_Value (COLUMNNAME_OccupationalStructure, OccupationalStructure);
	}

	/** Get OccupationalStructure.
		@return OccupationalStructure	  */
	public String getOccupationalStructure () 
	{
		return (String)get_Value(COLUMNNAME_OccupationalStructure);
	}

	/** Set Search Key.
		@param Value 
		Search key for the record in the format required - must be unique
	  */
	public void setValue (String Value)
	{
		set_Value (COLUMNNAME_Value, Value);
	}

	/** Get Search Key.
		@return Search key for the record in the format required - must be unique
	  */
	public String getValue () 
	{
		return (String)get_Value(COLUMNNAME_Value);
	}
}