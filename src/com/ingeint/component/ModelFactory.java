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
 * Contributor(s): Saul Piña <sauljabin@gmail.com>                            *
 * Contributor(s): Freddy Heredia <freddyheredia4@gmail.com>                  *
 *****************************************************************************/
package com.ingeint.component;

import java.lang.reflect.Constructor;
import java.sql.ResultSet;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Level;

import org.adempiere.base.IModelFactory;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.Env;

import com.ingeint.model.MHR_Basic_Factor_Type;
import com.ingeint.model.MHR_GAP;

public class ModelFactory implements IModelFactory {

	private final static CLogger log = CLogger.getCLogger(ModelFactory.class);
	private static Hashtable<String, Class<?>> cache = new Hashtable<String, Class<?>>();

	static {
		cache.put(MHR_Basic_Factor_Type.Table_Name, MHR_Basic_Factor_Type.class);
		cache.put(MHR_GAP.Table_Name, MHR_GAP.class);
		
	}

	@Override
	public Class<?> getClass(String tableName) {
		if (tableName == null)
			return null;
		Class<?> clazz = cache.get(tableName);
		return clazz;
	}

	@Override
	public PO getPO(String tableName, int Record_ID, String trxName) {

		Class<?> clazz = getClass(tableName);
		if (clazz == null)
			return null;

		PO model = null;
		Constructor<?> constructor = null;

		try {
			constructor = clazz.getDeclaredConstructor(new Class[] { Properties.class, int.class, String.class });
			model = (PO) constructor.newInstance(new Object[] { Env.getCtx(), new Integer(Record_ID), trxName });
		} catch (Exception e) {
			log.warning(String.format("Plugin: %s -> Class can not be instantiated for table: %s", "IR_Ecuador", tableName));
		}

		return model;
	}

	@Override
	public PO getPO(String tableName, ResultSet rs, String trxName) {

		Class<?> clazz = getClass(tableName);
		if (clazz == null)
			return null;

		PO model = null;
		Constructor<?> constructor = null;

		try {
			constructor = clazz.getDeclaredConstructor(new Class[] { Properties.class, ResultSet.class, String.class });
			model = (PO) constructor.newInstance(new Object[] { Env.getCtx(), rs, trxName });
		} catch (Exception e) {
			if (log.isLoggable(Level.WARNING))
				log.warning(String.format("Plugin: %s -> Class can not be instantiated for table: %s","IR_Ecuador", tableName));
		}

		return model;
	}
}