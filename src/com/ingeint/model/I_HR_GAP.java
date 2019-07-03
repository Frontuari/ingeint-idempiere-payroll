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
package com.ingeint.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import org.compiere.model.*;
import org.compiere.util.KeyNamePair;

/** Generated Interface for HR_GAP
 *  @author iDempiere (generated) 
 *  @version Release 2.1
 */
@SuppressWarnings("all")
public interface I_HR_GAP 
{

    /** TableName=HR_GAP */
    public static final String Table_Name = "HR_GAP";

    /** AD_Table_ID=1000004 */
    public static final int Table_ID = MTable.getTable_ID(Table_Name);

    KeyNamePair Model = new KeyNamePair(Table_ID, Table_Name);

    /** AccessLevel = 3 - Client - Org 
     */
    BigDecimal accessLevel = BigDecimal.valueOf(3);

    /** Load Meta Data */

    /** Column name AD_Client_ID */
    public static final String COLUMNNAME_AD_Client_ID = "AD_Client_ID";

	/** Get Client.
	  * Client/Tenant for this installation.
	  */
	public int getAD_Client_ID();

    /** Column name AD_Org_ID */
    public static final String COLUMNNAME_AD_Org_ID = "AD_Org_ID";

	/** Set Organization.
	  * Organizational entity within client
	  */
	public void setAD_Org_ID (int AD_Org_ID);

	/** Get Organization.
	  * Organizational entity within client
	  */
	public int getAD_Org_ID();

    /** Column name Created */
    public static final String COLUMNNAME_Created = "Created";

	/** Get Created.
	  * Date this record was created
	  */
	public Timestamp getCreated();

    /** Column name CreatedBy */
    public static final String COLUMNNAME_CreatedBy = "CreatedBy";

	/** Get Created By.
	  * User who created this records
	  */
	public int getCreatedBy();

    /** Column name C_Year_ID */
    public static final String COLUMNNAME_C_Year_ID = "C_Year_ID";

	/** Set Year.
	  * Calendar Year
	  */
	public void setC_Year_ID (int C_Year_ID);

	/** Get Year.
	  * Calendar Year
	  */
	public int getC_Year_ID();

	public org.compiere.model.I_C_Year getC_Year() throws RuntimeException;

    /** Column name Description */
    public static final String COLUMNNAME_Description = "Description";

	/** Set Description.
	  * Optional short description of the record
	  */
	public void setDescription (String Description);

	/** Get Description.
	  * Optional short description of the record
	  */
	public String getDescription();

    /** Column name HR_Basic_Factor_Type_ID */
    public static final String COLUMNNAME_HR_Basic_Factor_Type_ID = "HR_Basic_Factor_Type_ID";

	/** Set HR_Basic_Factor_Type	  */
	public void setHR_Basic_Factor_Type_ID (int HR_Basic_Factor_Type_ID);

	/** Get HR_Basic_Factor_Type	  */
	public int getHR_Basic_Factor_Type_ID();

	public com.ingeint.model.I_HR_Basic_Factor_Type getHR_Basic_Factor_Type() throws RuntimeException;

    /** Column name HR_ConceptAmount */
    public static final String COLUMNNAME_HR_ConceptAmount = "HR_ConceptAmount";

	/** Set HR_ConceptAmount	  */
	public void setHR_ConceptAmount (BigDecimal HR_ConceptAmount);

	/** Get HR_ConceptAmount	  */
	public BigDecimal getHR_ConceptAmount();

    /** Column name HR_ConceptFactor */
    public static final String COLUMNNAME_HR_ConceptFactor = "HR_ConceptFactor";

	/** Set HR_ConceptFactor	  */
	public void setHR_ConceptFactor (BigDecimal HR_ConceptFactor);

	/** Get HR_ConceptFactor	  */
	public BigDecimal getHR_ConceptFactor();

    /** Column name HR_GAP_Current_Amount */
    public static final String COLUMNNAME_HR_GAP_Current_Amount = "HR_GAP_Current_Amount";

	/** Set HR_GAP_Current_Amount	  */
	public void setHR_GAP_Current_Amount (BigDecimal HR_GAP_Current_Amount);

	/** Get HR_GAP_Current_Amount	  */
	public BigDecimal getHR_GAP_Current_Amount();

    /** Column name HR_GAP_ID */
    public static final String COLUMNNAME_HR_GAP_ID = "HR_GAP_ID";

	/** Set HR_GAP	  */
	public void setHR_GAP_ID (int HR_GAP_ID);

	/** Get HR_GAP	  */
	public int getHR_GAP_ID();

    /** Column name HR_GAP_UU */
    public static final String COLUMNNAME_HR_GAP_UU = "HR_GAP_UU";

	/** Set HR_GAP_UU	  */
	public void setHR_GAP_UU (String HR_GAP_UU);

	/** Get HR_GAP_UU	  */
	public String getHR_GAP_UU();

    /** Column name HR_GAPxFactor */
    public static final String COLUMNNAME_HR_GAPxFactor = "HR_GAPxFactor";

	/** Set HR_GAPxFactor	  */
	public void setHR_GAPxFactor (BigDecimal HR_GAPxFactor);

	/** Get HR_GAPxFactor	  */
	public BigDecimal getHR_GAPxFactor();

    /** Column name IsActive */
    public static final String COLUMNNAME_IsActive = "IsActive";

	/** Set Active.
	  * The record is active in the system
	  */
	public void setIsActive (boolean IsActive);

	/** Get Active.
	  * The record is active in the system
	  */
	public boolean isActive();

    /** Column name Name */
    public static final String COLUMNNAME_Name = "Name";

	/** Set Name.
	  * Alphanumeric identifier of the entity
	  */
	public void setName (String Name);

	/** Get Name.
	  * Alphanumeric identifier of the entity
	  */
	public String getName();

    /** Column name Updated */
    public static final String COLUMNNAME_Updated = "Updated";

	/** Get Updated.
	  * Date this record was updated
	  */
	public Timestamp getUpdated();

    /** Column name UpdatedBy */
    public static final String COLUMNNAME_UpdatedBy = "UpdatedBy";

	/** Get Updated By.
	  * User who updated this records
	  */
	public int getUpdatedBy();
}
