/**
 * This file is part of iDempiere ERP <http://www.idempiere.org>.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Copyright (C) 2015 INGEINT <http://www.ingeint.com>.
 * Copyright (C) Contributors.
 * 
 * Contributors:
 *    - 2015 Saúl Piña <spina@ingeint.com>.
 */

package com.ingeint.component;

import org.adempiere.base.event.IEventTopics;
import org.compiere.model.MFactAcct;
import org.eevolution.model.MHRProcess;

import com.ingeint.base.CustomEventManager;
import com.ingeint.event.Acct_Payroll;
import com.ingeint.event.EmployeeLoans;
import com.ingeint.event.HRGAP_EventHandler;

/**
 * Event Manager
 */
public class EventManager extends CustomEventManager {

	@Override
	protected void initialize() {
		// Register the custom events handler to build
		registerTableEvent(IEventTopics.PO_AFTER_CHANGE, "HR_GAP_Sector", HRGAP_EventHandler.class);
		registerTableEvent(IEventTopics.PO_AFTER_NEW, "HR_GAP_Sector", HRGAP_EventHandler.class);
		registerTableEvent(IEventTopics.PO_AFTER_CHANGE, "HR_Loan", EmployeeLoans.class);
		registerTableEvent(IEventTopics.DOC_AFTER_POST, MHRProcess.Table_Name, Acct_Payroll.class);
	}
}
