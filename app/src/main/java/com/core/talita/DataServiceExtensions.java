package com.core.talita;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.io.File;
import java.util.*;

/**
 * Extension methods for UniversalDataService and LocalDataManager
 * These are meant to be added to the respective classes
 */
public class DataServiceExtensions {
    private static final String TAG = "DataServiceExtensions";
    
    /**
     * NOTE: These methods should be copied into UniversalDataService and LocalDataManager
     * They are kept here for reference
     */
    
    // For UniversalDataService:
    public static List<PersonalData> getTodaysData(UniversalDataService dataService) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        long startOfDay = cal.getTimeInMillis();
        long endOfDay = System.currentTimeMillis();
        
        return dataService.getDataInRange(startOfDay, endOfDay);
    }
    
    // The actual implementation should be in the respective classes
}
