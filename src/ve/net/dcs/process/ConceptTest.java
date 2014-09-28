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
import org.codehaus.groovy.runtime.MethodRankHelper;
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
import org.python.antlr.PythonParser.attr_return;

import bsh.EvalError;
import bsh.Interpreter;
/**
 * HR Process Model
 *
 *  @author oscar.gomez@e-evolution.com, e-Evolution http://www.e-evolution.com
 *			<li> Original contributor of Payroll Functionality
 *  @author victor.perez@e-evolution.com, e-Evolution http://www.e-evolution.com
 * 			<li> FR [ 2520591 ] Support multiple calendar for Org 
 *			@see http://sourceforge.net/tracker2/?func=detail&atid=879335&aid=2520591&group_id=176962
 * @contributor Cristina Ghita, www.arhipac.ro
 * 			<li> 
 * @contributor Freddy Heredia. - fheredia@dcs.net.ve, Double Click Sistemas http://www.dcsla.com
 *			<li> 
 *  @contributor Orlando Curieles - ocurieles@dcs.net.ve Double Click Sistemas CA www.dcsla.com
 *          <li> 
 */	
 
public class ConceptTest extends MHRProcess_ConceptTest implements DocAction
{
	
	private int _Process_Period,_Payroll,_Department,_Days,	_C_BPartner_ID;
	private double result;
	private String description;
	private Timestamp _From,	_To,_DateStart,	_DateEnd;
	public ConceptTest(Properties ctx, int HR_Process_ID, String trxName) 
	{
		super(ctx, HR_Process_ID,trxName);
		if (HR_Process_ID == 0)
		{
			setDocStatus(DOCSTATUS_Drafted);
			setDocAction(DOCACTION_Prepare);
			setC_DocType_ID(0);
			set_ValueNoCheck ("DocumentNo", null);
			setProcessed(false);
			setProcessing(false);
			setPosted(false);
			setHR_Department_ID(0);
			setC_BPartner_ID(0);
		}
		
	}

	/**
	 *  Load Constructor
	 *  @param ctx context
	 *  @param rs result set record
	 */
	public ConceptTest(Properties ctx, ResultSet rs, String trxName) 
	{
		super(ctx, rs,trxName);
	}	//	MHRProcess_ConceptTest


	public void test(){
//		
//result = 0.0;
//Timestamp FechaIniv=_From;
//Timestamp FechaIng=_DateStart;
//Timestamp fechaInicioVacacionesActual = null;
//double mesesTrabajadosVacacionesActual=0;
//Timestamp FechaActual =  Env.getContextAsDate(getCtx(), "#Date");
//
//String anioActual ="";
//String sConsultai = ""
//		+ "SELECT a.validfrom FROM HR_Attribute a " 
//		+ " inner join HR_Concept c ON a.hr_concept_id = c.hr_concept_id "
//		+ "WHERE a.c_bpartner_id = ? AND a.ad_org_id = ? "
//		+ " AND c.value = ? AND a.validfrom >= ? AND COALESCE(a.validto,now()) <= ? ";
//String fechaIniVac = DB.getSQLValueString(get_TrxName(),sConsultai,new Object[] {_C_BPartner_ID,getAD_Org_ID(),"A_DIAS_VACACIONES_TRABAJADOR", _From, _To});
//if (fechaIniVac !=null){
//	fechaInicioVacacionesActual =Timestamp.valueOf(fechaIniVac);
//}else{
//	fechaInicioVacacionesActual =FechaActual;
//	throw new AdempiereException("El Empleado no tiene Registros en A_DIAS_VACACIONES_TRABAJADOR");
//}
//	
//
//
//sConsultai ="SELECT COALESCE((select SUM(amount) from HR_Movement m "
//		+ " JOIN HR_Process p ON p.HR_Process_ID = m.HR_Process_ID "
//		+ " JOIN HR_Payroll py ON py.HR_Payroll_ID = p.HR_Payroll_ID "
//		+ " JOIN HR_Period pr ON pr.HR_Period_ID = p.HR_Period_ID"
//		+ " where py.HR_Contract_ID IN (Select c.HR_Contract_ID From HR_Contract c where c.Value = ?)"
//		+ " AND m.HR_Concept_ID = (Select cp.HR_Concept_ID From HR_Concept cp where cp.value = ? )"
//		+ " AND m.C_BPartner_ID=?"
//		+ " AND pr.C_Year_ID IN (Select y.C_Year_ID From C_Year y where y.FiscalYear= ? )),	0)";
//
//anioActual = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
//Object[] para  = new Object[] {"Vacaciones","CC_MESES_TRAB_VACACIONES",getM_C_BPartner_ID(),anioActual};
//mesesTrabajadosVacacionesActual = DB.getSQLValueBD(get_TrxName(),sConsultai,para).doubleValue();
//
//if (fechaInicioVacacionesActual.getTime()>= FechaActual.getTime() && mesesTrabajadosVacacionesActual == 0){
//	Calendar fechaAnioAnterior = Calendar.getInstance();
//	fechaAnioAnterior.setTime(fechaInicioVacacionesActual);
//	fechaAnioAnterior.roll(Calendar.YEAR, -1);
//	Timestamp fechaInicioVacacionesAnterior = new Timestamp (fechaAnioAnterior.getTimeInMillis());
//	result = getMonths(fechaInicioVacacionesAnterior,FechaActual);
//	description="Fecha Desde: " +fechaInicioVacacionesAnterior.toString()+", Fecha Hasta: "+FechaActual;
//}
//if (fechaInicioVacacionesActual.getTime()< FechaActual.getTime() && mesesTrabajadosVacacionesActual == 0){
//	result = 12;
//	description="Vacaciones completas Hasta: "+_From;
//}
//	
//String contrato = getHR_Payroll().getHR_Contract().getValue();
//if (fechaInicioVacacionesActual.getTime()< FechaActual.getTime() && mesesTrabajadosVacacionesActual > 0 && contrato.equals("Liquidaciones")){
//	result = getMonths(fechaInicioVacacionesActual,_DateEnd);
//	description="Fecha Desde: " +fechaInicioVacacionesActual.toString()+", Fecha Hasta: "+_DateEnd;
//}
//if (fechaInicioVacacionesActual.getTime()< FechaActual.getTime() && mesesTrabajadosVacacionesActual > 0 && !contrato.equals("Liquidaciones")){
//	result = getMonths(fechaInicioVacacionesActual,FechaActual);
//	description="Fecha Desde: " +fechaInicioVacacionesActual.toString()+", Fecha Hasta: "+FechaActual;
//}
//
//////Sueldo vacaciones
//
//
//result = 0.0;
//double totalremuneracion=0.0;
//double sueldoV=0.0;
//double mesesPeriodo=0.0;
//double diasmes=getAttribute("C_DIAS_DEL_MES");
//double mesesAcum = 0.0;
//double montoAsignacionesAcum = 0.0;
//Timestamp fechaIniVac =null;
//String sConsultai = ""
//		+ "SELECT a.validfrom FROM HR_Attribute a " 
//		+ " inner join HR_Concept c ON a.hr_concept_id = c.hr_concept_id "
//		+ "WHERE a.c_bpartner_id = ? AND a.ad_org_id = ? "
//		+ " AND c.value = ? AND a.validfrom >= ? AND COALESCE(a.validto,now()) <= ? ";
//String fechaIniVacStr = DB.getSQLValueString(get_TrxName(),sConsultai,new Object[] {_C_BPartner_ID,getAD_Org_ID(),"A_DIAS_VACACIONES_TRABAJADOR", _From, _To});
//if (fechaIniVacStr!=null){
//	fechaIniVac = Timestamp.valueOf(fechaIniVacStr);
//}else{
//	MBPartner bp = MBPartner.get(getCtx(), _C_BPartner_ID);
//	description ="AdempiereException: El Empleado:"+bp.getName()+" No tiene registros en A_DIAS_VACACIONES_TRABAJADOR";
//	return;
//}
//mesesPeriodo=getMonths(_From , fechaIniVac);
//
//String ConsultaNominaPrincipal = ""
//		+ " Select HR_Payroll.Value From HR_Payroll where HR_Payroll.HR_Payroll_ID IN  " 
//		+ " (Select e.HR_Payroll_ID From HR_Employee e where e.C_BPartner_ID = ? ) "
//		+ " AND Exists(Select 1 From HR_Contract hc Where hc.IsMain = ? AND hc.HR_Contract_ID = HR_Payroll.HR_Contract_ID)";
//String NominaPrincipal = DB.getSQLValueString(get_TrxName(),ConsultaNominaPrincipal,new Object[] {_C_BPartner_ID,"Y"});
//totalremuneracion =totalremuneracion + getConceptRangeOfPeriod("CC_TOTAL_ASIGNACION_POR_QUINCENA",NominaPrincipal,getTimestampToString(_From),getTimestampToString(fechaIniVac));
//montoAsignacionesAcum = getAttribute("A_ACUM_MONTO_BASE_ASIGNACION_VAC");
//mesesAcum = getAttribute("A_ACUM_MESES_BASE_VAC");
//if(totalremuneracion>0 && (diasmes*mesesPeriodo)>0)
//	sueldoV=(totalremuneracion+montoAsignacionesAcum)/(diasmes*(mesesAcum +mesesPeriodo));
//if (totalremuneracion==0 && montoAsignacionesAcum>0 && mesesAcum>0)
//	sueldoV= montoAsignacionesAcum/mesesAcum;
//if(sueldoV>0)
//   result=sueldoV;
//description="Sueldo Diario para Vacaiones: " +result+ ", Nomina: "+NominaPrincipal;
//				
		////Calculo
}
	/////impuesto
	
	
	
	
}	//ConceptTest
