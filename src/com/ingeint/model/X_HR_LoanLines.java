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
import java.sql.Timestamp;
import java.util.Properties;
import org.compiere.model.*;
import org.compiere.util.Env;

/** Generated Model for HR_LoanLines
 *  @author iDempiere (generated) 
 *  @version Release 5.1 - $Id$ */
public class X_HR_LoanLines extends PO implements I_HR_LoanLines, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20180705L;

    /** Standard Constructor */
    public X_HR_LoanLines (Properties ctx, int HR_LoanLines_ID, String trxName)
    {
      super (ctx, HR_LoanLines_ID, trxName);
      /** if (HR_LoanLines_ID == 0)
        {
			setHR_LoanLines_ID (0);
        } */
    }

    /** Load Constructor */
    public X_HR_LoanLines (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_HR_LoanLines[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set Amount.
		@param Amt 
		Amount
	  */
	public void setAmt (BigDecimal Amt)
	{
		set_Value (COLUMNNAME_Amt, Amt);
	}

	/** Get Amount.
		@return Amount
	  */
	public BigDecimal getAmt () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_Amt);
		if (bd == null)
			 return Env.ZERO;
		return bd;
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

	/** Set Due Date.
		@param DueDate 
		Date when the payment is due
	  */
	public void setDueDate (Timestamp DueDate)
	{
		set_Value (COLUMNNAME_DueDate, DueDate);
	}

	/** Get Due Date.
		@return Date when the payment is due
	  */
	public Timestamp getDueDate () 
	{
		return (Timestamp)get_Value(COLUMNNAME_DueDate);
	}

	/** Set Fee Numbers.
		@param FeeNumbers Fee Numbers	  */
	public void setFeeNumbers (int FeeNumbers)
	{
		set_Value (COLUMNNAME_FeeNumbers, Integer.valueOf(FeeNumbers));
	}

	/** Get Fee Numbers.
		@return Fee Numbers	  */
	public int getFeeNumbers () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_FeeNumbers);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set HR Loan Lines.
		@param HR_LoanLines_ID HR Loan Lines	  */
	public void setHR_LoanLines_ID (int HR_LoanLines_ID)
	{
		if (HR_LoanLines_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_HR_LoanLines_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_HR_LoanLines_ID, Integer.valueOf(HR_LoanLines_ID));
	}

	/** Get HR Loan Lines.
		@return HR Loan Lines	  */
	public int getHR_LoanLines_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_HR_LoanLines_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set HR_LoanLines_UU.
		@param HR_LoanLines_UU HR_LoanLines_UU	  */
	public void setHR_LoanLines_UU (String HR_LoanLines_UU)
	{
		set_Value (COLUMNNAME_HR_LoanLines_UU, HR_LoanLines_UU);
	}

	/** Get HR_LoanLines_UU.
		@return HR_LoanLines_UU	  */
	public String getHR_LoanLines_UU () 
	{
		return (String)get_Value(COLUMNNAME_HR_LoanLines_UU);
	}

	public I_HR_Loan getHR_Loan() throws RuntimeException
    {
		return (I_HR_Loan)MTable.get(getCtx(), I_HR_Loan.Table_Name)
			.getPO(getHR_Loan_ID(), get_TrxName());	}

	/** Set HR_Loan.
		@param HR_Loan_ID HR_Loan	  */
	public void setHR_Loan_ID (int HR_Loan_ID)
	{
		if (HR_Loan_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_HR_Loan_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_HR_Loan_ID, Integer.valueOf(HR_Loan_ID));
	}

	/** Get HR_Loan.
		@return HR_Loan	  */
	public int getHR_Loan_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_HR_Loan_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
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
}