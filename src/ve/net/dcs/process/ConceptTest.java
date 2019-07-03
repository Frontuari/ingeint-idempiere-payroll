/******************************************************************************
 * Product: iDempiere ERP & CRM Smart Business Solution                       *
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
 * Copyright (C) 2003-2007 Double Click Systemas C.A.. All Rights Reserved.   *
 * Contributor(s): Freddy Heredia Double Click Systemas C.A.                  *
 *****************************************************************************/
package ve.net.dcs.process;

import java.io.File;
import java.math.*;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MBPartner;
import org.compiere.model.MDocType;
import org.compiere.model.MFactAcct;
import org.compiere.model.MInvoice;
import org.compiere.model.MPeriod;
import org.compiere.model.MPeriodControl;
import org.compiere.model.MRule;
import org.compiere.model.MUser;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.model.Scriptlet;
import org.compiere.print.ReportEngine;
import org.compiere.process.DocAction;
import org.compiere.process.DocumentEngine;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.TimeUtil;
import org.eevolution.model.*;
import org.eevolution.process.*;

import bsh.EvalError;
import bsh.Interpreter;

/**
 * HR Process Model
 * 
 * @author oscar.gomez@e-evolution.com, e-Evolution http://www.e-evolution.com
 *         <li>Original contributor of Payroll Functionality
 * @author victor.perez@e-evolution.com, e-Evolution http://www.e-evolution.com
 *         <li>FR [ 2520591 ] Support multiple calendar for Org
 * @see http 
 *      ://sourceforge.net/tracker2/?func=detail&atid=879335&aid=2520591&group_id
 *      =176962
 * @contributor Cristina Ghita, www.arhipac.ro <li>
 * @contributor Freddy Heredia. - fheredia@dcs.net.ve, Double Click Sistemas
 *              http://www.dcsla.com <li>
 * @contributor Orlando Curieles - ocurieles@dcs.net.ve Double Click Sistemas CA
 *              www.dcsla.com <li>
 */

public class ConceptTest extends MHRProcess_ConceptTest implements DocAction {

	private int _Process_Period, _Payroll, _Department, _Days, _C_BPartner_ID,_Process,
			_JobEmployee;
	private Object result;
	private String description,_RegionEmployee;
	private Timestamp _From, _To, _DateStart, _DateEnd,serviceDate;

	public ConceptTest(Properties ctx, int HR_Process_ID, String trxName) {
		super(ctx, HR_Process_ID, trxName);
		if (HR_Process_ID == 0) {
			setDocStatus(DOCSTATUS_Drafted);
			setDocAction(DOCACTION_Prepare);
			setC_DocType_ID(0);
			set_ValueNoCheck("DocumentNo", null);
			setProcessed(false);
			setProcessing(false);
			setPosted(false);
			setHR_Department_ID(0);
			setC_BPartner_ID(0);
		}

	}

	/**
	 * Load Constructor
	 * 
	 * @param ctx
	 *            context
	 * @param rs
	 *            result set record
	 */
	public ConceptTest(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	} // MHRProcess_ConceptTest

	public void test() {

result = 0.0;


//*************************************************************
//*CC_SUELDO_ULTIMANOMINA_HASTA_LIQ*
//*************************************************************
double sueldoDiario = getConcept("CC_SUELDO_DIARIO");
double diasPendientes= getDays(getConceptDate("C_FECHA_INICIO_PERIODO"),_DateEnd);
result =sueldoDiario*diasPendientes;
//************************************************************************************************



	
//Add to each credit		

//conceptoTemp="D_CUOTA_PRESTAMO_HIPOTECARIO";
//descriptionTemp = description;
//double totalDeduccionActual = result;
//double totalDeduccionProximoPeriodo= getCreditForNextPeriod(_Process,_C_BPartner_ID,totalDeduccionActual,conceptoTemp,"D_DEDUCCION_FALTANTE_PERIODO_ANTERIOR","C_ACUMULADO_CREDITOS_TEMP","C_ACUMULADO_DEBITOS_TEMP");
//result =  (totalDeduccionActual-totalDeduccionProximoPeriodo);
//description = descriptionTemp+". "+getMessageCreditForNextPeriod(totalDeduccionProximoPeriodo);
	}// ///End Test
} // ConceptTest
