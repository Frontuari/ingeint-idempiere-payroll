package com.ingeint.model;

import java.io.File;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.model.MDocType;
import org.compiere.model.MOrder;
import org.compiere.model.MPeriod;
import org.compiere.model.MSysConfig;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.process.DocAction;
import org.compiere.process.DocOptions;
import org.compiere.process.DocumentEngine;
import org.compiere.util.DB;
import org.compiere.util.Env;

public class MHRLoan extends X_HR_Loan implements DocAction, DocOptions {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6899397581959009593L;

	public MHRLoan(Properties ctx, int HR_Loan_ID, String trxName) {
		super(ctx, HR_Loan_ID, trxName);
		if (HR_Loan_ID==0) {
			setDocStatus(DOCSTATUS_Drafted);
			setDocAction (DOCACTION_Prepare);
		}
	}

	public MHRLoan(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}
	
	/**	Process Message 			*/
	protected String		m_processMsg = null;
	/**	Just Prepared Flag			*/
	protected boolean		m_justPrepared = false;

	public static MHRLoan createLoan(MOrder order) {
		int HRLoanFeeNumbers = MSysConfig.getIntValue("HRLoanFeeNumbers", 4,order.getAD_Client_ID());
		
		MHRLoan loan = new MHRLoan(order.getCtx(), 0, order.get_TrxName());
		
		loan.setAD_Org_ID(order.getAD_Org_ID());
		loan.setC_BPartner_ID(order.getC_BPartner_ID());
		loan.setDocStatus(DOCSTATUS_Drafted);
		loan.setC_DocTypeTarget_ID(MSysConfig.getIntValue("C_DocTypeForLoanOrder", 0, loan.getAD_Client_ID()));
		loan.setFeeNumbers(HRLoanFeeNumbers);
		loan.setDateStart(order.getDateOrdered());
		loan.setAD_User_ID(order.getCreatedBy());
		loan.setC_Order_ID(order.getC_Order_ID());
		loan.setDateAcct(order.getDateAcct());
		loan.setIsLoanActive(true);
		loan.setAmt(order.getGrandTotal());
		
		BigDecimal OpenAmt = DB.getSQLValueBD(order.get_TrxName(), "SELECT SUM(OpenAmt) "
				+ "FROM HR_Loan "
				+ "WHERE C_BPartner_ID = ? AND IsLoanActive = 'Y' "
				+ "AND DocStatus IN ('CO','CL') AND OpenAmt >0 ", order.getC_BPartner_ID());

		if(OpenAmt==null)
			OpenAmt = Env.ZERO;
		
		loan.setOpenAmt(order.getGrandTotal().add(OpenAmt));
		loan.saveEx();
		
		//Generate Lines
		BigDecimal feeAmt = order.getGrandTotal().divide(BigDecimal.valueOf(loan.getFeeNumbers(), 0));
		Date StartDate=new Date(loan.getDateStart().getTime());
		
		for (int i=1; i <= loan.getFeeNumbers(); i= i+1) {
			MHRLoanLines lines = new MHRLoanLines(loan);
			lines.setFeeNumbers(i);
			lines.setAmt(feeAmt);
			lines.setDueDate(new java.sql.Timestamp(calculateDate(StartDate,30).getTime()));
			StartDate = new Date(lines.getDueDate().getTime());	
			lines.saveEx();			
		}//Generate Lines
		
		return loan;
	}	
		
	public static void createLoanLines(MHRLoan loan) {
		//Generate Lines
		BigDecimal feeAmt = loan.getAmt().divide(BigDecimal.valueOf(loan.getFeeNumbers()), 2, BigDecimal.ROUND_HALF_UP);
	    Date StartDate=new Date(loan.getDateStart().getTime());
		
		for (int i=1; i <= loan.getFeeNumbers(); i= i+1) {
			MHRLoanLines lines = new MHRLoanLines(loan);
			lines.setFeeNumbers(i);
			lines.setAmt(feeAmt);
			lines.setDueDate(new java.sql.Timestamp(calculateDate(StartDate,30).getTime()));
			StartDate = new Date(lines.getDueDate().getTime());
			lines.saveEx();			
		}
		
	}//Generate Lines
	
	public static Date calculateDate(Date date, int days){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date); 
		calendar.add(Calendar.DAY_OF_YEAR, days);
		
		return calendar.getTime(); 
	}
	
	@Override
	public int customizeValidActions(String docStatus, Object processing, String orderType, String isSOTrx, int AD_Table_ID,
			String[] docAction, String[] options, int index) {
		if (options == null)
			throw new IllegalArgumentException("Option array parameter is null");
		if (docAction == null)
			throw new IllegalArgumentException("Doc action array parameter is null");

		// If a document is drafted or invalid, the users are able to complete, prepare or void
		if (docStatus.equals(DocumentEngine.STATUS_Drafted) || docStatus.equals(DocumentEngine.STATUS_Invalid)) {
			options[index++] = DocumentEngine.ACTION_Complete;
			options[index++] = DocumentEngine.ACTION_Prepare;
			options[index++] = DocumentEngine.ACTION_Reject;

			// If the document is already completed, we also want to be able to reactivate or void it instead of only closing it
		} else if (docStatus.equals(DocumentEngine.STATUS_Completed)) {
			options[index++] = DocumentEngine.ACTION_Void;
			options[index++] = DocumentEngine.ACTION_ReActivate;
		}

		return index;
	}
	
	@Override
	public String prepareIt() {
		
		if (log.isLoggable(Level.INFO)) log.info(toString());
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_BEFORE_PREPARE);
		if (m_processMsg != null)
			return DocAction.STATUS_Invalid;
		MDocType dt = MDocType.get(getCtx(), getC_DocTypeTarget_ID());

		//	Std Period open?
		if (!MPeriod.isOpen(getCtx(), getDateAcct(), dt.getDocBaseType(), getAD_Org_ID()))
		{
			m_processMsg = "@PeriodClosed@";
			return DocAction.STATUS_Invalid;
		}		
		setC_DocType_ID(getC_DocTypeTarget_ID());
		setIsLoanActive(true);
		setIsApproved(true);
		
		Timestamp maxDate = DB.getSQLValueTS(get_TrxName(), "SELECT Max(DueDate) "
				+ "FROM HR_LoanLines WHERE HR_Loan_ID = ? ", get_ID());
		
		setDateFinish(maxDate);
		setOpenAmt(getAmt());		
		return DocAction.STATUS_InProgress;
		
	}	
	
	@Override
	public boolean processIt(String action) throws Exception {
		log.warning("Processing Action=" + action + " - DocStatus=" + getDocStatus() + " - DocAction=" + getDocAction());
		DocumentEngine engine = new DocumentEngine(this, getDocStatus());
		return engine.processIt(action, getDocAction());
	}

	@Override
	public String completeIt() {
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_BEFORE_COMPLETE);
		if (m_processMsg != null)
			return DocAction.STATUS_Invalid;
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_AFTER_COMPLETE);
		if (m_processMsg != null)
			return DocAction.STATUS_Invalid;
		setProcessed(true);
		setDocAction(DOCACTION_Close);
		return DocAction.STATUS_Completed;
	}
	
	@Override
	public boolean approveIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean rejectIt() {
		
		if (getC_Order_ID()>0) {
			
			
			
		}
		
		return false;
	}

	@Override
	public boolean voidIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean closeIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean reverseCorrectIt() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean unlockIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean invalidateIt() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean reverseAccrualIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean reActivateIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getSummary() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDocumentInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File createPDF() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProcessMsg() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getDoc_User_ID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getC_Currency_ID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public BigDecimal getApprovalAmt() {
		// TODO Auto-generated method stub
		return null;
	}	
}
