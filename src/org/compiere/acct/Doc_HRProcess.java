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
 * Contributor(s): Victor Perez www.e-evolution.com                           *
 *****************************************************************************/
package org.compiere.acct;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.logging.Level;

import org.compiere.model.MAccount;
import org.compiere.model.MAcctSchema;
import org.compiere.model.MCharge;
import org.compiere.model.MElementValue;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.eevolution.model.MHRConcept;
import org.eevolution.model.MHRMovement;
import org.eevolution.model.MHRProcess;
import org.eevolution.model.X_HR_Concept_Acct;


/**
 *  Post Payroll Documents.
 *  <pre>
 *  Table:              HR_Process (??)
 *  Document Types:     HR_Process
 *  </pre>
 *  @author Oscar Gomez Islas
 *  @author victor.perez@e-evolution.com,www.e-evolution.com
 *  @version  $Id: Doc_Payroll.java,v 1.1 2007/01/20 00:40:02 ogomezi Exp $
 *  @author Cristina Ghita, www.arhipac.ro
 */
public class Doc_HRProcess extends Doc
{
	public MHRProcess process = null;
	
	/** Process Payroll **/
	public static final String	DOCTYPE_Payroll			= "HRP";
	/**
	 *  Constructor
	 * 	@param as accounting schema
	 * 	@param rs record
	 * 	@parem trxName trx
	 */
	public Doc_HRProcess (MAcctSchema as, ResultSet rs, String trxName)
	{
		super(as, MHRProcess.class, rs, null, trxName);
	}	//	Doc_Payroll

	@Override
	protected String loadDocumentDetails ()
	{
		process = (MHRProcess)getPO();
		setDateDoc(getDateAcct());
		//	Contained Objects
		p_lines = loadLines(process);
		log.fine("Lines=" + p_lines.length);
		return null;
	}   //  loadDocumentDetails


	/**
	 *	Load Payroll Line
	 *	@param Payroll Process
	 *  @return DocLine Array
	 */
	private DocLine[] loadLines(MHRProcess process)
	{
		ArrayList<DocLine> list = new ArrayList<DocLine>();
		MHRMovement[] lines = process.getLines(true);
		for (int i = 0; i < lines.length; i++)
		{
			MHRMovement line = lines[i];
			DocLine_Payroll docLine = new DocLine_Payroll (line, this);
			//
			log.fine(docLine.toString());
			list.add(docLine);
		}
		//	Return Array
		DocLine[] dls = new DocLine[list.size()];
		list.toArray(dls);
		return dls;
	}	//	loadLines

	@Override
	public BigDecimal getBalance()
	{
		BigDecimal retValue = Env.ZERO;
		return retValue; 
	}   //  getBalance

	@Override
	public ArrayList<Fact> createFacts (MAcctSchema as)
	{
		Fact fact = new Fact(this, as, Fact.POST_Actual);
		
		BigDecimal totalamt = Env.ZERO;
		for (int i = 0; i < p_lines.length; i++)
		{
			DocLine docLine = p_lines[i];
			DocLine_Payroll line = (DocLine_Payroll) docLine;
			int HR_Concept_ID = line.getHR_Concept_ID();
			BigDecimal sumAmount = line.getAmount();
			// round amount according to currency
			sumAmount = sumAmount.setScale(as.getStdPrecision(), BigDecimal.ROUND_HALF_UP);
			String AccountSign = line.getAccountSign();
			boolean isBalancing = isBalancing(as.getC_AcctSchema_ID(), HR_Concept_ID);
			int AD_OrgTrx_ID = line.getAD_Org_ID();
			int C_Activity_ID = line.getC_Activity_ID();
			int C_BPartner_ID = line.getC_BPartner_ID();
			//
			if (AccountSign != null && AccountSign.length() > 0 
					&& (MHRConcept.ACCOUNTSIGN_Debit.equals(AccountSign) 
							|| MHRConcept.ACCOUNTSIGN_Credit.equals(AccountSign))) 
			{
				if (isBalancing)
				{
					MAccount accountBPD = MAccount.get (getCtx(), getAccountBalancing(as.getC_AcctSchema_ID(),HR_Concept_ID,MHRConcept.ACCOUNTSIGN_Debit));
					FactLine debit=fact.createLine(docLine, accountBPD,as.getC_Currency_ID(),sumAmount, null);
					debit.setAD_OrgTrx_ID(AD_OrgTrx_ID);
					debit.setC_Activity_ID(C_Activity_ID);
					debit.setC_BPartner_ID(C_BPartner_ID);
					MAccount accountBPC = MAccount.get (getCtx(),this.getAccountBalancing(as.getC_AcctSchema_ID(),HR_Concept_ID, MHRConcept.ACCOUNTSIGN_Credit));
					FactLine credit = fact.createLine(docLine,accountBPC ,as.getC_Currency_ID(),null,sumAmount);
					credit.setAD_OrgTrx_ID(AD_OrgTrx_ID);
					credit.setC_Activity_ID(C_Activity_ID);
					credit.setC_BPartner_ID(C_BPartner_ID);
				}
				else
				{
					if (MHRConcept.ACCOUNTSIGN_Debit.equals(AccountSign))
					{
						MAccount accountBPD = MAccount.get (getCtx(), getAccountBalancing(as.getC_AcctSchema_ID(),HR_Concept_ID,MHRConcept.ACCOUNTSIGN_Debit));
						FactLine debit=fact.createLine(docLine, accountBPD,as.getC_Currency_ID(),sumAmount, null);
						debit.setAD_OrgTrx_ID(AD_OrgTrx_ID);
						debit.setC_Activity_ID(C_Activity_ID);
						debit.setC_BPartner_ID(C_BPartner_ID);
						sumAmount = sumAmount.abs();
					}
					else if (MHRConcept.ACCOUNTSIGN_Credit.equals(AccountSign))
					{
						MAccount accountBPC = MAccount.get (getCtx(),this.getAccountBalancing(as.getC_AcctSchema_ID(),HR_Concept_ID,MHRConcept.ACCOUNTSIGN_Credit));
						FactLine credit = fact.createLine(docLine,accountBPC ,as.getC_Currency_ID(),null,sumAmount);
						credit.setAD_OrgTrx_ID(AD_OrgTrx_ID);
						credit.setC_Activity_ID(C_Activity_ID);
						credit.setC_BPartner_ID(C_BPartner_ID);
						sumAmount = sumAmount.abs().negate();
					}
					totalamt = totalamt.add(sumAmount);
				}
			}
		}

		if (totalamt.signum() != 0)
		{
			int C_Charge_ID = process.getHR_Payroll().getC_Charge_ID();
			if (C_Charge_ID > 0) {
				MAccount acct = MCharge.getAccount(C_Charge_ID, as, totalamt);
				FactLine regTotal = null;
				if(totalamt.signum() > 0)
					regTotal = fact.createLine(null, acct ,as.getC_Currency_ID(), null, totalamt);
				else
					regTotal = fact.createLine(null, acct ,as.getC_Currency_ID(), totalamt, null);
				regTotal.setAD_Org_ID(getAD_Org_ID());
			}
		}

		ArrayList<Fact> facts = new ArrayList<Fact>();
		facts.add(fact);
		return facts;
	}

	/**
	 * get account balancing
	 * @param AcctSchema_ID
	 * @param HR_Concept_ID
	 * @param AccountSign Debit or Credit only
	 * @return Account ID 
	 */
	private int getAccountBalancing (int AcctSchema_ID, int HR_Concept_ID, String AccountSign)
	{
		String field;
		if (MElementValue.ACCOUNTSIGN_Debit.equals(AccountSign))
		{
			field = X_HR_Concept_Acct.COLUMNNAME_HR_Expense_Acct;
		}
		else if (MElementValue.ACCOUNTSIGN_Credit.equals(AccountSign))
		{
			field = X_HR_Concept_Acct.COLUMNNAME_HR_Revenue_Acct;
		}
		else
		{
			throw new IllegalArgumentException("Invalid value for AccountSign="+AccountSign);
		}
		final StringBuilder sqlAccount = new StringBuilder("SELECT ").append(field).append(" FROM HR_Concept_Acct")
											.append(" WHERE HR_Concept_ID=? AND C_AcctSchema_ID=?");
		int Account_ID = DB.getSQLValueEx(getTrxName(), sqlAccount.toString(), HR_Concept_ID, AcctSchema_ID);		
		return Account_ID;
	}

	private boolean isBalancing (int AcctSchema_ID, int HR_Concept_ID)
	{
		final String sqlAccount = "SELECT IsBalancing FROM HR_Concept_Acct WHERE HR_Concept_ID=? AND C_AcctSchema_ID=?";
		String isBalancing = DB.getSQLValueStringEx(getTrxName(), sqlAccount, HR_Concept_ID, AcctSchema_ID);		
		return "Y".equals(isBalancing);
	}

}   //  Doc_Payroll
