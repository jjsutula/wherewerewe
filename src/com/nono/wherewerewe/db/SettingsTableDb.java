package com.nono.wherewerewe.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;

import com.nono.wherewerewe.data.DbConst;
import com.nono.wherewerewe.data.SettingsConst;
import com.nono.wherewerewe.data.table.SettingsTable;

public class SettingsTableDb {
	
	public static final String TABLE_NAME = DbConst.SETTINGS_TABLE;

	public static final String CREATE_TABLE_CMD = 
        "create table " + TABLE_NAME + " (" +
	        DbConst.KEY_ID + " integer primary key autoincrement, " +
	        DbConst.TYPE + " integer not null, " +
	        DbConst.SETTING + " text not null" +
        ");";
	public static final String DROP_TABLE_CMD = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";

	private final DbAdapter dbAdapter;
	
	public SettingsTableDb(DbAdapter dbAdapter) {
		this.dbAdapter = dbAdapter;
	}

	/**
     * Create a new settings record.
     * @param type The type of the setting to add.
     * @param setting The setting to insert.
     * @return The created settings table or null if the insert failed.
     */
    public SettingsTable createSettings(int type, String setting) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbConst.TYPE, type);
        contentValues.put(DbConst.SETTING, setting);
        long id = dbAdapter.getSqldb().insert(TABLE_NAME, null, contentValues); 
        if (id < 0) {
        	return null;
        }
        
        SettingsTable table = new SettingsTable();
        table.setId(id);
        table.setType(type);
        table.setSetting(setting);
        table.resetContentsChanged();

        return table;
    }

    /**
     * Retrieve all the settings in the database.
     * @return A cursor over all returned settings.
     */
    public Cursor fetchSettingsList() {
        return dbAdapter.getSqldb().query(TABLE_NAME, new String[] {DbConst.KEY_ID, DbConst.TYPE, DbConst.SETTING}, null, null,
                null, null, null, null);
    }

    /**
     * Return a specific setting.
     * @param keyId The ID of the settings to retrieve.
     * @return The found matching settings.
     * @throws SQLException if the settings could not be found.
     */
    public SettingsTable fetchSetting(int type) throws SQLException {

        Cursor cursor =
        	dbAdapter.getSqldb().query(true, TABLE_NAME, new String[] {DbConst.KEY_ID, DbConst.SETTING}, DbConst.TYPE + "=" + type, null,
                        null, null, null, null);
        if (cursor == null) {
        	return getDefaultSetting(type);
        }

        SettingsTable table;
        if (cursor.moveToFirst()) {
	        int columnIndex = 0;
	        table = new SettingsTable();
	        table.setId(cursor.getLong(columnIndex++));
	        table.setType(type);
	        table.setSetting(cursor.getString(columnIndex++));
	        table.resetContentsChanged();
        }
        else {
        	table = getDefaultSetting(type);
        }

        cursor.close();

        return table;
    }

    public SettingsTable getDefaultSetting(int type) {
    	String defaultSetting;
    	switch (type) {
    	case SettingsConst.MEASUREMENT_UNITS:
    		defaultSetting = SettingsConst.METRIC;
    		break;
    	case SettingsConst.COORDINATE_UNITS:
    		defaultSetting = SettingsConst.DECIMAL;
    		break;
    	case SettingsConst.CURRENT_TRIP:
    	case SettingsConst.CURRENT_WAYPOINT:
    		defaultSetting = null;
    		break;
    	default:
    		return null;
    	}

    	SettingsTable settingsTable = new SettingsTable();
    	settingsTable.setType(type);
    	settingsTable.setSetting(defaultSetting);

    	return settingsTable;
    }

    /**
     * Update the settings.
     * @param keyId The ID of the settings to update. If this is -1 the settings will be
     * inserted. 
     * @param type The value to set.
     * @param body value to set note body to
     * @return false if the settings could not be updated.
     */
    public boolean updateSettings(SettingsTable table) {
    	if (table.getId() < 0) {
    		SettingsTable newSettingsTable = createSettings(table.getType(), table.getSetting());
    		if (newSettingsTable != null) {
    			table.setId(newSettingsTable.getId());
    			table.markNoAction();
    			return true;
    		}
    		else {
    			return false;
    		}
    	}
    	
    	boolean success = true;
       	ContentValues contentValues = table.getContentValues();
       	if (contentValues != null) {
    	   	success = dbAdapter.getSqldb().update(TABLE_NAME, contentValues, DbConst.KEY_ID + "=" + table.getId(), null) > 0;
       	}
		table.resetContentsChanged();

       	return success;
   	}

	/**
     * Remove a settings record.
     * @param type The type of the setting to remove.
    * @return true if deleted, false otherwise.
     */
    public boolean removeSetting(int type) {
   		return dbAdapter.getSqldb().delete(TABLE_NAME, DbConst.TYPE + "=" + type, null) > 0;
    }

   /**
    * Delete the item.
    * @param table The item to delete.
    * @return true if deleted, false otherwise.
    */
   	public boolean delete(SettingsTable table) {

   		return dbAdapter.getSqldb().delete(TABLE_NAME, DbConst.KEY_ID + "=" + table.getId(), null) > 0;
   	}
}
