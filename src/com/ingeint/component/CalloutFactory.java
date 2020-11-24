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

import com.ingeint.base.CustomCalloutFactory;
import com.ingeint.callout.UpdateAmtPaymentSel;
import com.ingeint.model.MHRPaymentSelectionLine;

import net.frontuari.callout.CalloutLVEHRProcessReport;
import net.frontuari.model.MLVEHRProcessReportLine;

/**
 * Callout Factory
 */
public class CalloutFactory extends CustomCalloutFactory {

	/**
	 * For initialize class. Register the custom callout to build
	 * 
	 * <pre>
	 * protected void initialize() {
	 * 	registerCallout(MTableExample.Table_Name, MTableExample.COLUMNNAME_Text, CPrintPluginInfo.class);
	 * }
	 * </pre>
	 */
	@Override
	protected void initialize() {
		registerCallout(MHRPaymentSelectionLine.Table_Name, MHRPaymentSelectionLine.COLUMNNAME_PayAmt, UpdateAmtPaymentSel.class);
		registerCallout(MLVEHRProcessReportLine.Table_Name, MLVEHRProcessReportLine.COLUMNNAME_HR_Concept_ID, CalloutLVEHRProcessReport.class);
	}

}
