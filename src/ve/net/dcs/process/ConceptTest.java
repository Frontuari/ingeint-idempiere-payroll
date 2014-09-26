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
		
		
		////Haber jubilacion
		
double aniosServicio = getConcept ("CC_AÑOS_ANTIGUEDAD");
double totalAsignaciones =0;
double mesesTrabajados = 0;
double remuneracionPromedio = 0;
double fondoReserva = 0;
String ConsultaNominaPrincipal = "";
String NominaPrincipal = "";
if (aniosServicio>=25){
	GregorianCalendar cincoAniosAtras= new GregorianCalendar();
	if (_DateEnd.compareTo(new GregorianCalendar().getTime())<0){
		cincoAniosAtras.setTime(_DateEnd);
		cincoAniosAtras.roll(Calendar.YEAR, -5);	
	}
	//fondoReserva = getConcept("CC_PAGO_FONDO_RESER");
	mesesTrabajados = getConcept("CC_MESES_TRABAJADOS");
	
	 ConsultaNominaPrincipal = ""
		+ " Select HR_Payroll.Value From HR_Payroll where HR_Payroll.HR_Payroll_ID IN  " 
		+ " (Select e.HR_Payroll_ID From HR_Employee e where e.C_BPartner_ID = ? ) "
		+ " AND Exists(Select 1 From HR_Contract hc Where hc.IsMain = ? AND hc.HR_Contract_ID = HR_Payroll.HR_Contract_ID)";
NominaPrincipal = DB.getSQLValueString(get_TrxName(),ConsultaNominaPrincipal,new Object[] {_C_BPartner_ID,"Y"});
	
	totalAsignaciones = getConceptRangeOfPeriod("CC_TOTAL_ASIGNACION", NominaPrincipal, getTimestampToString(new Timestamp (cincoAniosAtras.getTime().getTime())), getTimestampToString(_DateEnd));
	remuneracionPromedio = totalAsignaciones / mesesTrabajados;
}
	result = (remuneracionPromedio*0.5*aniosServicio);
	description = "Años Aplicados: "+aniosServicio+ ", Remuneracion Promedio: "+remuneracionPromedio+", Fondo Reserva: "+fondoReserva+" Nomina:"+NominaPrincipal;
		
	
	
	
	///////Inpuesto
	result = 0.0;
	double totalremuneracion=0.0;
	double totalasigbas=0.0;
	double totalnovhor=0.0;
	double totalnovdia=0.0;
	double prov_fondo_reser=0.0;
	double ultimo=0.0;
	Timestamp FechaIni= null;
	StringBuffer des = new StringBuffer();
	des.append("");
	String sNomina = getHR_Payroll().getValue();
	int periodo=getPayrollPeriod ();
	//System.out.println("Periodo id::::::::::::::::::::::::::::::::::"+periodo);
	FechaIni=getFirstDayOfPeriod (periodo);
	//System.out.println("Fecha inicial::::::::::::::::::::::::::::::::::"+ getTimestampToString (FechaIni));
	ultimo = getConcept("CC_ULTIMA_SEMANA");
	double añoserv=getConcept("CC_AÑOS_ANTIGUEDAD");
	//System.out.println("Antiguedad::::::::::::::::::::::::::::::::::"+añoserv);
	double añoscon=getAttribute("C_AÑOS_SERV_PAGO_RESERVA");
	prov_fondo_reser=getAttribute("C_FACTOR_FONDO_RESER");
	//System.out.println("Factor Fondo Reserva::::::::::::::::::::::::::::::::::"+añoscon);
	String acumfondo=getAttributeString("A_ACUM_FONDO_RESERVA");
	totalremuneracion=getConceptRangeOfPeriod("CC_TOTAL_ASIGNACION_POR_QUINCENA",sNomina,getTimestampToString(FechaIni),getTimestampToString(_To));
	totalremuneracion=totalremuneracion+getConcept("CC_TOTAL_ASIGNACION_POR_QUINCENA");
	//System.out.println("TOTAL REMUNERA::::::::::::::::::::::::::::::::::"+totalremuneracion);
	if(totalremuneracion > 0 && (añoserv>=añoscon) && ("N".equals(acumfondo)) && ultimo>0 )
	{
		result=totalremuneracion*(prov_fondo_reser);
		//System.out.println("Result:::::::::::::::::::::::::::::::::"+result);
	}else{
	//	System.out.println("totalremuneracion = "+totalremuneracion + " añoserv " + añoserv + " añoscon" + añoscon +  " acumfondo  " + acumfondo +" ultimo" +ultimo) ;
	}
	description = "totalremuneracion = "+totalremuneracion + " añoserv " + añoserv + " añoscon" + añoscon +  " acumfondo  " + acumfondo +" ultimo" +ultimo;
	
}
	/////impuesto
	
	
	
	
}	//ConceptTest
