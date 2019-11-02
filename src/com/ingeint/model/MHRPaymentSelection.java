package com.ingeint.model;

import java.io.File;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.model.MDocType;
import org.compiere.model.MPayment;
import org.compiere.model.MPeriod;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.Query;
import org.compiere.process.DocAction;
import org.compiere.process.DocOptions;
import org.compiere.process.DocumentEngine;
import org.compiere.util.Util;

import com.ingeint.utils.PayrollUtils;

public class MHRPaymentSelection extends X_HR_PaymentSelection implements DocAction, DocOptions {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7827181203737421447L;

	public MHRPaymentSelection(Properties ctx, int HR_PaymentSelection_ID, String trxName) {
		super(ctx, HR_PaymentSelection_ID, trxName);
		if (HR_PaymentSelection_ID == 0) {
			setDocStatus(DOCSTATUS_Drafted);
			setDocAction(DOCACTION_Prepare);
		}
	}

	public MHRPaymentSelection(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}

	/** Process Message */
	private String m_processMsg = null;
	/** Just Prepared Flag */
	private boolean m_justPrepared = false;

	/** Load Lines */
	protected MHRPaymentSelectionLine[] t_lines = null;

	MHRPaymentSelectionLine[] getLines(String whereClause, String orderClause) {
		StringBuilder whereClauseFinal = new StringBuilder(
				MHRPaymentSelectionLine.COLUMNNAME_HR_PaymentSelection_ID + "=? ");
		if (!Util.isEmpty(whereClause, true))
			whereClauseFinal.append(whereClause);
		if (orderClause.length() == 0)
			orderClause = MHRPaymentSelectionLine.COLUMNNAME_Line;
		//
		List<MHRPaymentSelectionLine> list = new Query(getCtx(), MHRPaymentSelectionLine.Table_Name,
				whereClauseFinal.toString(), get_TrxName()).setParameters(get_ID()).setOrderBy(orderClause).list();

		for (MHRPaymentSelectionLine ol : list) {
			ol.setHeaderInfo(this);
		}
		//
		return list.toArray(new MHRPaymentSelectionLine[list.size()]);
	} // getLines

	public MHRPaymentSelectionLine[] getLines(boolean requery, String orderBy) {
		if (t_lines != null && !requery) {
			set_TrxName(t_lines, get_TrxName());
			return t_lines;
		}
		//
		String orderClause = "";
		if (orderBy != null && orderBy.length() > 0)
			orderClause += orderBy;
		else
			orderClause += "Line";
		t_lines = getLines(null, orderClause);
		return t_lines;
	} // getLines

	public MHRPaymentSelectionLine[] getLines() {
		return getLines(false, null);
	} // getLines

	@Override
	public int customizeValidActions(String docStatus, Object processing, String orderType, String isSOTrx,
			int AD_Table_ID, String[] docAction, String[] options, int index) {
		if (options == null)
			throw new IllegalArgumentException("Option array parameter is null");
		if (docAction == null)
			throw new IllegalArgumentException("Doc action array parameter is null");

		// If a document is drafted or invalid, the users are able to complete, prepare
		// or void
		if (docStatus.equals(DocumentEngine.STATUS_Drafted) || docStatus.equals(DocumentEngine.STATUS_Invalid)) {
			options[index++] = DocumentEngine.ACTION_Complete;
			options[index++] = DocumentEngine.ACTION_Prepare;
			options[index++] = DocumentEngine.ACTION_Reject;
			// If the document is already completed, we also want to be able to reactivate
			// or void it instead of only closing it
		} else if (docStatus.equals(DocumentEngine.STATUS_Completed)) {
			options[index++] = DocumentEngine.ACTION_Reverse_Correct;
		}

		return index;
	}

	@Override
	public String prepareIt() {
		if (log.isLoggable(Level.INFO))
			log.info(toString());
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_BEFORE_PREPARE);
		if (m_processMsg != null)
			return DocAction.STATUS_Invalid;
		MDocType dt = MDocType.get(getCtx(), getC_DocTypeTarget_ID());

		// Std Period open?
		if (!MPeriod.isOpen(getCtx(), getDateDoc(), dt.getDocBaseType(), getAD_Org_ID())) {
			m_processMsg = "@PeriodClosed@";
			return DocAction.STATUS_Invalid;
		}

		MHRPaymentSelectionLine[] lines = getLines(true, null);
		if (lines.length == 0) {
			m_processMsg = "@NoLines@";
			return DocAction.STATUS_Invalid;
		}
		setC_DocType_ID(getC_DocTypeTarget_ID());
		return DocAction.STATUS_InProgress;
	} // prepareIt

	@Override
	public boolean processIt(String action) throws Exception {
		log.warning(
				"Processing Action=" + action + " - DocStatus=" + getDocStatus() + " - DocAction=" + getDocAction());
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

		// Generate Payments

		MHRPaymentSelectionLine[] plines = getLines();

		for (MHRPaymentSelectionLine pline : plines) {
			PayrollUtils.createPayment(pline);
		}
		setProcessed(true);
		setDocAction(DOCACTION_Close);
		return DocAction.STATUS_Completed;
	} // completeIt

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
	public boolean approveIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean rejectIt() {
		// TODO Auto-generated method stub
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

		MHRPaymentSelectionLine[] plines = getLines();

		for (MHRPaymentSelectionLine pline : plines) {

			MPayment payment = new MPayment(getCtx(), pline.getC_Payment_ID(), get_TrxName());
			if (payment.getDocStatus().equals(DOCSTATUS_Drafted)) {
				pline.setC_Payment_ID(-1);
				pline.saveEx();
				payment.deleteEx(true);
			} else {
				payment.reverseCorrectIt();
				payment.saveEx();
			}
		}
		setDocStatus(STATUS_Reversed);
		saveEx();
		return true;
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
