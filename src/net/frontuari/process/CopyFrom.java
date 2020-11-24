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
 * Copyright (C) 2003-2014 E.R.P. Consultores y Asociados, C.A.               *
 * All Rights Reserved.                                                       *
 * Contributor(s): Yamel Senih www.erpconsultoresyasociados.com               *
 *****************************************************************************/
package net.frontuari.process;

import java.util.logging.Level;

import org.compiere.process.ProcessInfoParameter;
import org.eevolution.model.MHRPayroll;
import org.eevolution.model.X_HR_Payroll;

import com.ingeint.base.CustomProcess;

/**
 * @author <a href="mailto:yamelsenih@gmail.com">Yamel Senih</a>
 *
 */
public class CopyFrom extends CustomProcess {

	/** Payroll From				*/
	int p_HR_Payroll_ID 			= 0;
	
	/** Payroll To					*/
	int p_HR_Payroll_ID_To 			= 0;
	
	/**	Delete Old Records			*/
	boolean isDeleteOld				= false;
	
	@Override
	protected void prepare() {
		String name;
		for (ProcessInfoParameter parameter : getParameter()){
			name = parameter.getParameterName();
			
			if(parameter.getParameter() == null)
				;
			else if(name.equals(X_HR_Payroll.COLUMNNAME_HR_Payroll_ID))
				p_HR_Payroll_ID = parameter.getParameterAsInt();
			else if(name.equals("DeleteOld"))
				isDeleteOld = parameter.getParameterAsBoolean();
			else
				log.log(Level.SEVERE,"Unknow Parameter "+name);				
		}
		/**	Payroll To	initialize			*/
		if(p_HR_Payroll_ID != 0)
			p_HR_Payroll_ID_To = getRecord_ID();
		
	}

	@Override
	protected String doIt() throws Exception {

		int count 	= 0; //, delete = 0;
		log.info("From Payroll =" + p_HR_Payroll_ID + " to " + p_HR_Payroll_ID_To);
		if (p_HR_Payroll_ID_To == 0)
			throw new IllegalArgumentException("Target Payroll  == 0");
		
		if (p_HR_Payroll_ID == 0)
			throw new IllegalArgumentException("Source Payroll  == 0");
		
		/**	Instanced Payroll From	*/
		MHRPayroll m_HR_Payroll_From = new MHRPayroll(getCtx(), p_HR_Payroll_ID, get_TrxName());
		
		/**	Instanced Payroll to	*/
		MHRPayroll m_HR_Payroll_To = new MHRPayroll(getCtx(), p_HR_Payroll_ID_To, get_TrxName());
		
		if(isDeleteOld){
			//	delete = m_HR_Payroll_To.deleteTo(m_HR_Payroll_To);
			count = m_HR_Payroll_To.copyFrom(m_HR_Payroll_From, m_HR_Payroll_To);
		}else
			count = m_HR_Payroll_To.copyFrom(m_HR_Payroll_From, m_HR_Payroll_To);
		//
		return "@HR_PayrollConcept_ID@ @Copied@ = " +count;
}

}
