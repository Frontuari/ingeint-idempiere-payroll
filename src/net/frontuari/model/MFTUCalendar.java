package net.frontuari.model;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.compiere.model.I_C_NonBusinessDay;
import org.compiere.model.MCalendar;
import org.compiere.model.MClient;
import org.compiere.model.MClientInfo;

import net.frontuari.model.MNonBusinessDay;
import org.compiere.model.Query;
import org.compiere.util.CCache;
import org.compiere.util.Env;

public class MFTUCalendar extends org.compiere.model.MCalendar {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9130623060934907863L;

	public MFTUCalendar(Properties ctx, int C_Calendar_ID, String trxName) {
		super(ctx, C_Calendar_ID, trxName);
	}

	public MFTUCalendar(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}

	public MFTUCalendar(MClient client) {
		super(client);
	}
	
	/**	Non Business Days			*/
	private HashMap<String, MNonBusinessDay> nonBusinessDays;
	private SimpleDateFormat format;
	/**
	 * Get Non Business Days
	 * @author <a href="mailto:yamelsenih@gmail.com">Yamel Senih</a> Oct 12, 2016, 4:01:27 PM
	 * @return
	 */
	private void loadNonBusinessDay() {
		if(nonBusinessDays != null) {
			return;
		}
		//	Get
		List<MNonBusinessDay> nonBusinessDaysList = new Query(getCtx(), I_C_NonBusinessDay.Table_Name, 
				I_C_NonBusinessDay.COLUMNNAME_C_Calendar_ID + " = ?", get_TrxName())
			.setParameters(getC_Calendar_ID())
			.setOnlyActiveRecords(true)
			.setOrderBy(I_C_NonBusinessDay.COLUMNNAME_Date1)
			.<MNonBusinessDay>list();
		//	To HashMap
		nonBusinessDays = new HashMap<String, MNonBusinessDay>();
		format = new SimpleDateFormat("yyyyMMdd");
		//	Add
		for(MNonBusinessDay nonBusinessDay : nonBusinessDaysList) {
			nonBusinessDays.put(format.format(nonBusinessDay.getDate1()) + "|" + nonBusinessDay.getAD_Org_ID(), nonBusinessDay);
		}
	}
	
	/**
	 * Verify if it is a not business day from Timestamp
	 * @author <a href="mailto:yamelsenih@gmail.com">Yamel Senih</a> Oct 12, 2016, 5:52:14 PM
	 * @param day
	 * @return
	 * @return boolean
	 */
	public boolean isNonBusinessDay(Timestamp day) {
		//	Validate null before
		if(day == null)
			return false;
		//	
		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(day.getTime());
		//	REturn
		return isNonBusinessDay(date.getTime());
	}
	
	/**
	 * Verify if it is a not business day from Date by Org
	 * @author <a href="mailto:yamelsenih@gmail.com">Yamel Senih</a> Oct 12, 2016, 6:41:02 PM
	 * @param day
	 * @return
	 * @return boolean
	 */
	public boolean isNonBusinessDay(Date day, int orgId) {
		//	Validate null before
		if(day == null)
			return false;
		//	Load
		loadNonBusinessDay();
		//	
		String keyOrg = format.format(day) + "|" + orgId;
		String keyGlobal = format.format(day) + "|" + 0;
		//	
		MNonBusinessDay nonBusinessDay = nonBusinessDays.get(keyOrg);
		//	Validate
		if(nonBusinessDay == null
				&& orgId != 0) {
			nonBusinessDay = nonBusinessDays.get(keyGlobal);
		}
		//	Default return
		return nonBusinessDay != null;
	}
	
	/**
	 * Verify if it is a not business day from Date
	 * @author <a href="mailto:yamelsenih@gmail.com">Yamel Senih</a> Oct 12, 2016, 6:41:02 PM
	 * @param day
	 * @return
	 * @return boolean
	 */
	public boolean isNonBusinessDay(Date day) {
		return isNonBusinessDay(day, 0);
	}
	
	/**
	 * 	Get MCalendar from Cache
	 *	@param ctx context
	 *	@param C_Calendar_ID id
	 *	@return MCalendar
	 */
	public static MFTUCalendar get (Properties ctx, int C_Calendar_ID)
	{
		Integer key = Integer.valueOf(C_Calendar_ID);
		MFTUCalendar retValue = (MFTUCalendar) s_cache.get (key);
		if (retValue != null)
			return retValue;
		retValue = new MFTUCalendar (ctx, C_Calendar_ID, null);
		if (retValue.get_ID () != 0)
			s_cache.put (key, retValue);
		return retValue;
	}	//	get
	
	/**
	 * 	Get Default Calendar for Client
	 *	@param ctx context
	 *	@param AD_Client_ID id
	 *	@return MCalendar
	 */
	public static MFTUCalendar getDefault (Properties ctx, int AD_Client_ID)
	{
		MClientInfo info = MClientInfo.get(ctx, AD_Client_ID);
		return get (ctx, info.getC_Calendar_ID());
	}	//	getDefault
	
	/**
	 * 	Get Default Calendar for Client
	 *	@param ctx context
	 *	@return MCalendar
	 */
	public static MFTUCalendar getDefault (Properties ctx)
	{
		return getDefault(ctx, Env.getAD_Client_ID(ctx));
	}	//	getDefault
	
	/**	Cache						*/
	private static CCache<Integer,MFTUCalendar> s_cache
		= new CCache<Integer,MFTUCalendar>(Table_Name, 20);

}
