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
 * Copyright (C) 2003-2015 E.R.P. Consultores y Asociados, C.A.               *
 * All Rights Reserved.                                                       *
 * Contributor(s): Yamel Senih www.erpcya.com                                 *
 *****************************************************************************/
package net.frontuari.utils;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.compiere.util.Language;
import org.compiere.util.TimeUtil;

/**
 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
 */
public class HRTimeUtil extends TimeUtil {

	/**
	 * Add Month to Date
	 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
	 * @param p_From
	 * @param months
	 * @return
	 * @return Timestamp
	 */
	public static  Timestamp getMonthTo(Timestamp p_From, int months) {
		if(p_From == null)
			return p_From;
		
		Calendar d_From = Calendar.getInstance();
		d_From.setTime(p_From);
		//	Add Yeas
		d_From.add(Calendar.MONTH, months);
		
		return new Timestamp(d_From.getTimeInMillis());
	}
	
	/**
	 * Get Month First Date
	 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
	 * @param day
	 * @return
	 * @return Timestamp
	 */
	public static Timestamp getMonthFirstDay(Timestamp day) {
		if (day == null)
			day = new Timestamp(System.currentTimeMillis());
		GregorianCalendar cal = new GregorianCalendar(Language.getLoginLanguage().getLocale());
		cal.setTimeInMillis(day.getTime());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		//
		cal.set(Calendar.DAY_OF_MONTH, 1);	//	first
		return new Timestamp (cal.getTimeInMillis());
	}	//	getNextDay

	
	/**
	 * Valid Non Business Day
	 * <li> Calendar.SUNDAY
	 * <li> Calendar.MONDAY
	 * <li> Calendar.TUESDAY
	 * <li> Calendar.WEDNESDAY
	 * <li> Calendar.THURSDAY
	 * <li> Calendar.FRIDAY
	 * <li> Calendar.SATURDAY
	 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
	 * @param day
	 * @param nonBusinessDays
	 * @return
	 * @return boolean
	 */
	public static boolean nonBusinessDay(int day, int [] nonBusinessDays){
		if(nonBusinessDays == null)
			return false;
		for (int i = 0; i < nonBusinessDays.length; i++) {
			if(day == nonBusinessDays[i])
				return true;
		}
		return false;
	}
	
	/**
	 * Get Years from two date
	 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
	 * @param p_From
	 * @param p_To
	 * @return
	 * @return int
	 */
	public static int getYearDiff(Timestamp p_From, Timestamp p_To){
		//	Set Date From
		Calendar dateFrom=Calendar.getInstance();
		dateFrom.setTime(p_From);
        //	Set Date To
		Calendar dateTo = Calendar.getInstance();
        dateTo.setTime(p_To);
        //	Calculate Difference
        int yearDiff = dateTo.get(Calendar.YEAR) - dateFrom.get(Calendar.YEAR);
        int diferMes = dateTo.get(Calendar.MONTH) - dateFrom.get(Calendar.MONTH);
        int diferDia = dateTo.get(Calendar.DAY_OF_MONTH) - dateFrom.get(Calendar.DAY_OF_MONTH);
        if (diferMes < 0 ||(diferMes == 0 && diferDia < 0)){
            yearDiff -= 1;
        }
        //	Value
        return yearDiff;
	}
	
	/**
	 * Add Year to Date
	 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
	 * @param p_From
	 * @param years
	 * @return
	 * @return Timestamp
	 */
	public static Timestamp getYearTo(Timestamp p_From, int years){
		if(p_From == null)
			return p_From;
		
		Calendar d_From = Calendar.getInstance();
		d_From.setTime(p_From);
		//	Add Yeas
		d_From.add(Calendar.YEAR, years);
		//	
		return new Timestamp(d_From.getTimeInMillis());
	}
	
	/**
	 * Is Same Month
	 * @author <a href="mailto:yamelsenih@gmail.com">Yamel Senih</a> 20/06/2014, 08:23:56
	 * @param one
	 * @param two
	 * @return
	 * @return boolean
	 */
	public static boolean isSameMonth (Timestamp one, Timestamp two) {
		GregorianCalendar calOne = new GregorianCalendar();
		if (one != null)
			calOne.setTimeInMillis(one.getTime());
		GregorianCalendar calTwo = new GregorianCalendar();
		if (two != null)
			calTwo.setTimeInMillis(two.getTime());
		if (calOne.get(Calendar.YEAR) == calTwo.get(Calendar.YEAR)
			&& calOne.get(Calendar.MONTH) == calTwo.get(Calendar.MONTH))
			return true;
		return false;
	}	//	isSameDay
}
