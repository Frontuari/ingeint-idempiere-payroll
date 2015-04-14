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

package org.eevolution.process;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;





import org.adempiere.report.jasper.ReportStarter;
import org.compiere.model.MBPartner;
import org.compiere.model.MClient;
import org.compiere.model.MInterestArea;
import org.compiere.model.MMailText;
import org.compiere.model.MPInstance;
import org.compiere.model.MPInstancePara;
import org.compiere.model.MProcess;
import org.compiere.model.MUser;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfo;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.ServerProcessCtl;
import org.compiere.process.SvrProcess;
import org.compiere.util.AdempiereUserError;
import org.compiere.util.DB;
import org.compiere.util.EMail;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.compiere.util.Trx;

/**
 *  Send Mail to Interest Area Subscribers
 *
 *  @author Antonio Canaveral, www.e-evolution.com 
 */
public class PayrollViaEMail extends SvrProcess 
{
	/** What to send			*/
	private int				m_R_MailText_ID = -1;
	/**	Mail Text				*/
	private MMailText		m_MailText = null;

	/**	From (sender)			*/
	private int				m_AD_User_ID = -1;
	/** Client Info				*/
	private MClient			m_client = null;
	/**	From					*/
	private MUser			m_from = null;
	/** Recipient List to prevent duplicate mails	*/
	private ArrayList<Integer>	m_list = new ArrayList<Integer>();

	
	private int 			m_counter = 0;
	private int 			m_errors = 0;
	/**	To Subscribers 			*/
	private int				m_HR_Process_ID = -1;
	/** Interest Area			*/
	private MInterestArea 	m_ia = null;
	/** To Customer Type		*/
	private int				m_C_BP_Group_ID = -1;
	/** To Purchaser of Product	*/
	//	comes here
	private int 			m_AD_Process_ID=-1;


	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
			{
				log.fine("Null paramater: " + name);
			}
			else if (name.equals("HR_Process_ID"))
			{
				m_HR_Process_ID = para[i].getParameterAsInt();
			}
			else if (name.equals("R_MailText_ID"))
			{
				m_R_MailText_ID = para[i].getParameterAsInt();
			}
			else if (name.equals("C_BP_Group_ID"))
			{
				m_C_BP_Group_ID = para[i].getParameterAsInt();
			}
			else if (name.equals("AD_User_ID"))
			{
				m_AD_User_ID = para[i].getParameterAsInt();
			}
			else if (name.equals("AD_Process_ID"))
			{
				m_AD_Process_ID = para[i].getParameterAsInt();
			}
			else
			{
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
			}
		}
	}	//	prepare

	/**
	 *  Perform process.
	 *  @return Message
	 *  @throws Exception
	 */
	protected String doIt() throws Exception
	{
		log.info("R_MailText_ID=" + m_R_MailText_ID);
		//	Mail Test
		m_MailText = new MMailText (getCtx(), m_R_MailText_ID, get_TrxName());
		if (m_MailText.getR_MailText_ID() == 0)
			throw new Exception ("Not found @R_MailText_ID@=" + m_R_MailText_ID);
		//	Client Info
		m_client = MClient.get (getCtx());
		if (m_client.getAD_Client_ID() == 0)
			throw new Exception ("Not found @AD_Client_ID@");
		if (m_client.getSMTPHost() == null || m_client.getSMTPHost().length() == 0)
			throw new Exception ("No SMTP Host found");
		//
		long start = System.currentTimeMillis();
		
		m_from = new MUser(getCtx(),Env.getAD_User_ID(getCtx()),get_TrxName());
		if (m_from.getAD_User_ID() == 0)
			throw new Exception ("No found @AD_User_ID@=" + m_AD_User_ID);
		if (m_AD_User_ID > 0)
		{
				MUser tmpUser = new MUser(getCtx(),m_AD_User_ID,get_TrxName());
				sendIndividualMail (m_from.getName(), tmpUser.getC_BPartner_ID(), null);
		}else if (m_C_BP_Group_ID > 0)
			sendBPGroup();
		else 
			sendBPOfHRProcess();
		log.fine("From " + m_from);
			

		return "@Created@=" + m_counter + ", @Errors@=" + m_errors + " - "
			+ (System.currentTimeMillis()-start) + "ms";
	}	//	doIt
	
	/**
	 * 	Send to BPGroup
	 */
	private void sendBPGroup()
	{
		log.info("C_BP_Group_ID=" + m_C_BP_Group_ID);
		StringBuilder sql = new StringBuilder("SELECT bp.Name, bp.url, bp.c_bpartner_id ") 
					.append("FROM C_BPartner bp ")
					.append("WHERE bp.IsActive='Y' ")
					.append("AND bp.url IS NOT NULL ");	
			if (m_C_BP_Group_ID > 0)
				sql.append("AND bp.C_BP_Group_ID=? ");
			
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql.toString(), get_TrxName());
			if (m_C_BP_Group_ID > 0)
				pstmt.setInt(1, m_C_BP_Group_ID);
			ResultSet rsMail = pstmt.executeQuery();
			List <Integer> tabla = new  ArrayList<Integer>();
			while (rsMail.next())
			{
				tabla.add(new Integer(rsMail.getInt(3)));
			}
			for(int i=0;i<tabla.size();i++)
			{
				
				Boolean ok = sendIndividualMail ("", tabla.get(i).intValue(), null);
				if (ok == null)
				{
					//nothing to do
				}
				else if (ok.booleanValue())
				{
					m_counter++;
				}
				else
				{
					m_errors++;
				}
			}
			rsMail.close();
			pstmt.close();
			pstmt = null;
		}
		catch (SQLException ex)
		{
			log.log(Level.SEVERE, sql.toString(), ex);
		}
		//	Clean Up
		try
		{
			if (pstmt != null)
				pstmt.close();
		}
		catch (SQLException ex1)
		{
			log.log(Level.SEVERE, sql.toString(), ex1);
		}
		pstmt = null;
	}	//	sendBPGroup
	
	/**
	 * 	Send Individual Mail
	 *	@param Name user name
	 *	@param AD_User_ID user
	 *	@param unsubscribe unsubscribe message
	 *	@return true if mail has been sent
	 */
	private Boolean sendIndividualMail (String Name, int C_BPartner_ID,String unsubscribe)
	{
		//	Prevent two email
		String emailAddr = "";
		try
		{
			Integer ii = new Integer (C_BPartner_ID);
			//int BPartner_ID=0;
			if (m_list.contains(ii))
				return null;
			m_list.add(ii);
			
			MBPartner to = new MBPartner(getCtx(), C_BPartner_ID, null);
			if (to.getURL()!=null)
				emailAddr = to.getURL();
			else{
				MUser userDefault = new Query (getCtx(),MUser.Table_Name," C_BPartner_ID = ? AND IsHRDefault = ? AND IsActive = ? ",get_TrxName()).setParameters(C_BPartner_ID, "Y","Y").first();
				if (userDefault != null)
					emailAddr = userDefault.getEMail();
				else{
					addLog(0, null, null, ("@ERROR@") + " - "+to.getName()+": "+Msg.translate(Env.getCtx(), "FillMandatory")+ MUser.COLUMNNAME_EMail);
					return Boolean.FALSE;
				}
			}
			
			String message = m_MailText.getMailText(true);
			//	Unsubscribe
			if (unsubscribe != null)
				message += unsubscribe;
			//
			//EMail email = new EMail(m_client,m_from.getEMail(),to.getURL(),m_MailText.getMailHeader(), message);
			EMail email = m_client.createEMail(m_from, emailAddr, m_MailText.getMailHeader(), message);
			if (m_MailText.isHtml())
				email.setMessageHTML(m_MailText.getMailHeader(), message);
			else
			{
				email.setSubject (m_MailText.getMailHeader());
				email.setMessageText (message);
			}
			email.addAttachment(CreatePDF(C_BPartner_ID));
			if (!email.isValid() && !email.isValid(true))
			{
				log.warning("NOT VALID - " + email);
				return Boolean.FALSE;
			}
			
			boolean OK = EMail.SENT_OK.equals(email.send());
			//new MUserMail(m_MailText, AD_User_ID, email).save();
			//
			
			if (OK)
				log.fine(emailAddr);
			else
				log.warning("FAILURE - " + emailAddr);
			addLog(0, null, null, (OK ? "@OK@" : "@ERROR@") + " - " +to.getName()+": "+ emailAddr);
			return OK;
		}catch(Exception e)
		{
			return Boolean.FALSE;
		}
	}	//	sendIndividualMail
	
	private File CreatePDF(int BPartner_ID)
	{
		File attachment = null;
		int AD_Process_ID = m_AD_Process_ID;
		MPInstance instance = new MPInstance(Env.getCtx(), AD_Process_ID, 0);
		if (!instance.save())
		{
			return null;
		}
		//call process
		ProcessInfo pi = new ProcessInfo ("PH_SendEmail", AD_Process_ID);
		pi.setAD_PInstance_ID (instance.getAD_PInstance_ID());

		//	Add Parameter - Selection=Y
		//MPInstancePara ip = new MPInstancePara(instance, 10);
		ProcessInfoParameter[] para = {new ProcessInfoParameter("HR_Process_ID", m_HR_Process_ID, null, null, null),new ProcessInfoParameter("C_BPartner_ID",BPartner_ID , null, null, null)};
		pi.setParameter(para);
		pi.setRecord_ID(m_HR_Process_ID);
		
		pi.setIsBatch(true);
		MProcess worker = new MProcess(getCtx(),AD_Process_ID,get_TrxName());
		worker.processIt(pi, Trx.get(get_TrxName(), true));
		if (worker.getJasperReport()!=null || worker.getJasperReport().equals(""))
			ServerProcessCtl.process(pi,Trx.get(get_TrxName(), true));
		attachment=pi.getPDFReport();
		return attachment;
	}
	/**
	 * 	Send to BP of  HR_Process
	 */
	private void sendBPOfHRProcess()
	{
		//log.info("C_BP_Group_ID=" + m_C_BP_Group_ID);
		StringBuilder sql = new StringBuilder("SELECT Distinct(C_BPartner_ID) From HR_Movement m where m.HR_Process_ID = ? "); 
					
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql.toString(), get_TrxName());
			if (m_HR_Process_ID > 0)
				pstmt.setInt(1, m_HR_Process_ID);
			ResultSet rsMail = pstmt.executeQuery();
			List <Integer> tabla = new  ArrayList<Integer>();
			while (rsMail.next())
			{
				tabla.add(new Integer(rsMail.getInt(1)));
			}
			for(int i=0;i<tabla.size();i++)
			{
				
				Boolean ok = sendIndividualMail ("", tabla.get(i).intValue(), null);
				if (ok == null)
				{
					//nothing to do
				}
				else if (ok.booleanValue())
				{
					m_counter++;
				}
				else
				{
					m_errors++;
				}
			}
			rsMail.close();
			pstmt.close();
			pstmt = null;
		}
		catch (SQLException ex)
		{
			log.log(Level.SEVERE, sql.toString(), ex);
		}
		//	Clean Up
		try
		{
			if (pstmt != null)
				pstmt.close();
		}
		catch (SQLException ex1)
		{
			log.log(Level.SEVERE, sql.toString(), ex1);
		}
		pstmt = null;
	}	//	sendBPOfHRProcess

	
}	//	SendMailText