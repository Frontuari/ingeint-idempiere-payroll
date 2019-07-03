package org.idempiere.component;

import java.lang.reflect.Constructor;
import java.sql.ResultSet;
import java.util.Properties;
import java.util.logging.Level;

import org.adempiere.base.IModelFactory;
import org.compiere.model.MEntityType;
import org.compiere.model.MTable;
import org.compiere.model.PO;
import org.compiere.util.CCache;
import org.compiere.util.CLogger;
import org.compiere.util.Env;

public class HRModelFactory implements IModelFactory {

	private final static CLogger log = CLogger.getCLogger(HRModelFactory.class);
	private static CCache<String, Class<?>> cache = new CCache<String, Class<?>>("PO_Class", 20);

	private final static String prefixTable = "HR_";
	private final static String prefixModel = "M";
	private final static String prefixModelDefault = "X_";
	private static final String entityTypeTable = "EE02";

	@Override
	public Class<?> getClass(String tableName) {

		if (tableName == null)
			return null;

		Class<?> clazz = cache.get(tableName);

		if (clazz == null) {

			MTable table = MTable.get(Env.getCtx(), tableName);
			String entityType = table.getEntityType();

			if (!entityType.equals(entityTypeTable))
				return null;

			MEntityType et = MEntityType.get(Env.getCtx(), entityType);
			String modelPackage = et.getModelPackage();

			String classNameFormat = "%s.%s%s";

			try {
				clazz = Class.forName(String.format(classNameFormat, modelPackage, prefixModel, tableName.replace("_", "")));
				cache.put(tableName, clazz);
			} catch (Exception e1) {
				try {
					clazz = Class.forName(String.format(classNameFormat, modelPackage, prefixModelDefault, tableName));
					cache.put(tableName, clazz);
				} catch (Exception e2) {
					if (log.isLoggable(Level.WARNING))
						log.warning(String.format("Class not found for table: %s", tableName));
				}
			}
		}

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
			if (log.isLoggable(Level.WARNING))
				log.warning(String.format("Class can not be instantiated for table: %s", tableName));
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
				log.warning(String.format("Class can not be instantiated for table: %s", tableName));
		}

		return model;
	}

}
