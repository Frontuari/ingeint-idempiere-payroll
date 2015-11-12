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
 * Copyright (C) 2003-2007 e-Evolution,SC. All Rights Reserved.               *
 * Contributor(s): Freddy Heredia <freddyheredia4@gmail.com>                  *
 *****************************************************************************/
package com.ingeint.model;

import java.sql.ResultSet;
import java.util.Properties;

public class MHR_GAP extends X_HR_GAP {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3309466943900583962L;

	public MHR_GAP(Properties ctx, int HR_GAP_ID, String trxName) {
		super(ctx, HR_GAP_ID, trxName);
		// TODO Auto-generated constructor stub
	}

	public MHR_GAP(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}

}
