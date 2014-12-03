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

result = 0.0;
double baseimp=0,totalprov=0,utilidad=0,baseimpa=0,diferenciaIngresoGasto=0,excesoTotal=0,baseimpg=0;
BigDecimal factorImpuesto=new BigDecimal(0),excesoDesde=new BigDecimal(0);
String impuestoFraccion=  "0",sql1="",sql3="";
StringBuffer sQuery=new StringBuffer();
int periodo=getPayrollPeriod ();
int meses=0;
if (getFirstDayOfPeriodYear(periodo).after(_DateStart))
	meses=getMonths(getFirstDayOfPeriodYear(periodo),getLastDayOfPeriod(periodo));
else 
	meses=getMonths(_DateStart,getLastDayOfPeriod(periodo));
Timestamp FechaIni=getFirstDayOfPeriodYear(periodo);
double gastoproyem=getAttribute("GAP");
	String sQueryYear = "Select FiscalYear FROM C_Year WHERE C_Year_ID= COALESCE((select C_Year_ID from HR_Period WHERE HR_Period_ID = ?),1000000)";
	String year =  DB.getSQLValueString(get_TrxName(),sQueryYear,new Object[]{periodo});
MBPartner bp = new MBPartner(getCtx(), _C_BPartner_ID, get_TrxName());
int HR_Basic_Factor_Type_ID = bp.get_Value("HR_Basic_Factor_Type_ID")!=null?bp.get_ValueAsInt("HR_Basic_Factor_Type_ID"):-1;
if (HR_Basic_Factor_Type_ID<0||year.equals(""))
	throw new AdempiereException("Año: "+year+", TipFactor: "+HR_Basic_Factor_Type_ID);
PO gap = (PO)new Query(Env.getCtx(),"HR_GAP","HR_Basic_Factor_Type_ID = ? AND C_Year_ID IN (Select C_Year_ID From C_Year Where FiscalYear = ? )",null).setParameters(new Object[]{HR_Basic_Factor_Type_ID,year}).first();
if (gap!=null){
	PO gapSector = (PO)new Query(Env.getCtx(),"HR_GAP_Sector","HR_GAP_ID = ? AND C_BPartner_ID = ? ",null).setParameters(new Object[]{gap.get_ID(),_C_BPartner_ID}).first();
	if (gapSector!=null){
		double education = gapSector.get_ValueAsString("HR_Education").equals("")?0:Double.parseDouble(gapSector.get_ValueAsString("HR_Education"));
		double food = gapSector.get_ValueAsString("HR_Food").equals("")?0:Double.parseDouble(gapSector.get_ValueAsString("HR_Food"));
		double clothing = gapSector.get_ValueAsString("HR_Clothing").equals("")?0:Double.parseDouble(gapSector.get_ValueAsString("HR_Clothing"));
		double health = gapSector.get_ValueAsString("HR_Health").equals("")?0:Double.parseDouble(gapSector.get_ValueAsString("HR_Health"));
		double housing = gapSector.get_ValueAsString("HR_Housing").equals("")?0:Double.parseDouble(gapSector.get_ValueAsString("HR_Housing"));	
gastoproyem = education + food + clothing +health +housing;
	}
}
double ultimo=getConcept("CC_ULTIMA_SEMANA");
double mesesOtroEmpleador=0;
 mesesOtroEmpleador= getAttribute("A_ACUM_INICIAL_MESES_TRAB_OTROS");
double acuminiasi=getAttribute("A_ACUM_INICIAL_ASIGNACION");
double acumIESS = 0,baseAcumuladaProyectada=0;
double factorAporte_Iess=getAttribute("C_FACTOR_IESS_PERSONAL");
baseimpa=getConcept("CC_TOTAL_ASIGNACION_POR_QUINCENA");
baseimpg=getConceptRangeOfPeriod("CC_TOTAL_ASIGNACION_POR_QUINCENA",null,getTimestampToString(FechaIni),getTimestampToString(_From));
utilidad=getConceptRangeOfPeriod("CC_PAGO_UTILIDAD","NOMINA_UTILIDADES",getTimestampToString(FechaIni),getTimestampToString(_To));
baseimp=baseimp+acuminiasi+baseimpa+baseimpg+utilidad;
int mesesfaltantes=0;
if (_DateEnd.equals(TimeUtil.getDay(2999, 12, 31))){
	mesesfaltantes = getMonths(getLastDayOfPeriod(getHR_Period_ID()), getLastDayOfPeriodYear(getHR_Period_ID()));
}else{
	mesesfaltantes = getMonths(getLastDayOfPeriod(getHR_Period_ID()), _DateEnd);
}
baseAcumuladaProyectada = ((baseimp/(meses+1+mesesOtroEmpleador))*((meses+1+mesesOtroEmpleador)+mesesfaltantes));
acumIESS = baseAcumuladaProyectada*(factorAporte_Iess/100);
if(baseAcumuladaProyectada>0) 
	diferenciaIngresoGasto=(baseAcumuladaProyectada-(gastoproyem+acumIESS));
if (diferenciaIngresoGasto>0){
sql1 = "Select COALESCE(att.";
sql3 = " from HR_Attribute att where att.HR_Concept_ID = "
+ "(Select c.HR_Concept_ID from HR_Concept c where c.Value = ?) and  "
+ " ? > minValue and ?  <= MaxValue";
sQuery.append(sql1).append("description ,'') ").append(sql3);
impuestoFraccion =  DB.getSQLValueString(get_TrxName(),sQuery.toString(),new Object[] {"IMPUESTO",BigDecimal.valueOf(diferenciaIngresoGasto),BigDecimal.valueOf(diferenciaIngresoGasto)});
if (impuestoFraccion==null){
impuestoFraccion ="0";
}
sQuery=new StringBuffer();
sQuery.append(sql1).append("MinValue ,0) ").append(sql3);
excesoDesde =  DB.getSQLValueBD(get_TrxName(),sQuery.toString(),new Object[] {"IMPUESTO",BigDecimal.valueOf(diferenciaIngresoGasto),BigDecimal.valueOf(diferenciaIngresoGasto)});
if (excesoDesde==null){
excesoDesde = new BigDecimal(0);
}
sQuery=new StringBuffer();
sQuery.append(sql1).append("Amount ,0) ").append(sql3);
factorImpuesto =  DB.getSQLValueBD(get_TrxName(),sQuery.toString(),new Object[] {"IMPUESTO",BigDecimal.valueOf(diferenciaIngresoGasto),BigDecimal.valueOf(diferenciaIngresoGasto)});
if (factorImpuesto==null){
factorImpuesto = new BigDecimal(0);
}
excesoTotal = diferenciaIngresoGasto -excesoDesde.doubleValue();	
excesoTotal = excesoTotal * factorImpuesto.doubleValue()/100;
totalprov =  Double.parseDouble(impuestoFraccion) +excesoTotal;
totalprov = totalprov/((meses+1+mesesOtroEmpleador)+mesesfaltantes);
if(totalprov>0 && ultimo==1.0)
result=totalprov;
description = "Base"+baseimp+",Meses"+(meses+1)+"Proyectada:"+baseAcumuladaProyectada+"GAP:"+gastoproyem+",IESS:"+ acumIESS+",Fraccion: "+impuestoFraccion+ ",Exceso :"+excesoTotal+",Factor Imp:"+factorImpuesto.doubleValue()+" MesesFaltantes:"+mesesfaltantes;
}else{
result = 0;
}	


	
	}/////End Test
	
	
	
	
	
}	//ConceptTest
