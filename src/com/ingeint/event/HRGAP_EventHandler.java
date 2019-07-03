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
package com.ingeint.event;


import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.util.Env;
import com.ingeint.base.CustomEventHandler;

public class HRGAP_EventHandler extends CustomEventHandler {

	
	
	protected void doHandleEvent() {
		
		PO po = getPO();
		
		if (po.get_TableName().equals("HR_GAP_Sector")){
			double education = 0,food=0,clothing=0,health=0,housing=0;
			int gapID = 0;
			double gapMax = 0,conceptAmount=0;
			PO gap;
			gapID =po.get_ValueAsInt("HR_GAP_ID");
			
			gap = (PO)new Query(Env.getCtx(),"HR_GAP","HR_GAP_ID = ?",null).setParameters(gapID).first();
			if (gap!=null){
				double gapBasic=gap.get_ValueAsString("HR_GAP_Current_Amount").equals("")?0:Double.parseDouble(gap.get_ValueAsString("HR_GAP_Current_Amount"));
				double gapConceptFactor=gap.get_ValueAsString("HR_ConceptFactor").equals("")?0:Double.parseDouble(gap.get_ValueAsString("HR_ConceptFactor"));
				
				int gapFactorTypeID=gap.get_ValueAsInt("HR_Basic_Factor_Type_ID")==0?0:gap.get_ValueAsInt("HR_Basic_Factor_Type_ID");
				PO gapFactorType = (PO)new Query(Env.getCtx(),"HR_Basic_Factor_Type","HR_Basic_Factor_Type_ID = ?",null).setParameters(gapFactorTypeID).first();
				double gapFactorTypePercent=gapFactorType.get_ValueAsString("Percent").equals("")?0:Double.parseDouble(gapFactorType.get_ValueAsString("Percent"));
				gapMax = gapBasic*gapFactorTypePercent;
				conceptAmount = gapBasic*gapConceptFactor;
				
			}
			education = po.get_ValueAsString("HR_Education").equals("")?0:Double.parseDouble(po.get_ValueAsString("HR_Education"));
			food = po.get_ValueAsString("HR_Food").equals("")?0:Double.parseDouble(po.get_ValueAsString("HR_Food"));
			clothing = po.get_ValueAsString("HR_Clothing").equals("")?0:Double.parseDouble(po.get_ValueAsString("HR_Clothing"));
			health = po.get_ValueAsString("HR_Health").equals("")?0:Double.parseDouble(po.get_ValueAsString("HR_Health"));
			housing = po.get_ValueAsString("HR_Housing").equals("")?0:Double.parseDouble(po.get_ValueAsString("HR_Housing"));
			
			if (education>conceptAmount){
				throw new AdempiereException("El Monto del Rubro Educacion Supera El Monto Maximo Permitido");
			}
			if (food > conceptAmount ){
				throw new AdempiereException("El Monto del Rubro Alimentacion Supera El Monto Maximo Permitido");
			}
			if (clothing > conceptAmount ){
				throw new AdempiereException("El Monto del Rubro Vestimenta Supera El Monto Maximo Permitido");
			}
			if (housing > conceptAmount ){
				throw new AdempiereException("El Monto del Rubro Vivienda Supera El Monto Maximo Permitido");
			}
			if (health+education+food+clothing+housing > gapMax){
				throw new AdempiereException("La Sumatoria De Todos Los Rubros Supera El Monto Maximo Permitido");
			}
		}	
		
	}

}
