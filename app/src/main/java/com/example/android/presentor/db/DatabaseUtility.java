package com.example.android.presentor.db;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;
import android.widget.TextView;

import com.example.android.presentor.R;
import com.example.android.presentor.db.ServicesContract.ServiceEntry;
import com.example.android.presentor.db.ServicesContract.DeviceEntry;
import com.example.android.presentor.utils.Utility;


/**
 * Created by Carlo on 21/10/2017.
 */

public class DatabaseUtility {

    private final static String LOG = "DatabaseUtility";
    public static final String UNDERSCORE = "_";

    private static String getServicePassword(NsdServiceInfo serviceInfo) {
        String serviceName = serviceInfo.getServiceName();
        int endPosition = serviceName.indexOf(UNDERSCORE);

        return serviceName.substring(0, endPosition);
    }

    private static String getCreatorName(NsdServiceInfo serviceInfo) {
        String serviceName = serviceInfo.getServiceName();
        int startPosition = serviceName.indexOf(UNDERSCORE) + 1;
        int lastPosition = serviceName.lastIndexOf(UNDERSCORE);

        return serviceName.substring(startPosition, lastPosition);
    }

    private static String getServiceName(NsdServiceInfo serviceInfo){
        String serviceName = serviceInfo.getServiceName();
        int startPosition = serviceName.lastIndexOf(UNDERSCORE) + 1;

        return serviceName.substring(startPosition);
    }

    public static void addServiceToList(Context context, NsdServiceInfo serviceInfo) {

        ServicesDbHelper dbHelper = new ServicesDbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                ServiceEntry.COL_SERVICE_ID,
                ServiceEntry.COL_SERVICE_NAME,
        };
        String selection = ServiceEntry.COL_SERVICE_NAME + "=?";
        String[] selectionArgs = {getServiceName(serviceInfo)};

        Cursor c = db.query(ServiceEntry.TABLE_SERVICES, projection, selection, selectionArgs,
                null, null, null);



        Log.e("DatabaseUtility", "Cursor count: " + c.getCount());

        if(c.getCount() > 0){
            c.close();
            db.close();
            dbHelper.close();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(ServiceEntry.COL_IP_ADDRESS, serviceInfo.getHost().getHostAddress());
        values.put(ServiceEntry.COL_PORT_NUMBER, serviceInfo.getPort());
        values.put(ServiceEntry.COL_SERVICE_NAME, getServiceName(serviceInfo));
        values.put(ServiceEntry.COL_CREATOR_NAME, getCreatorName(serviceInfo));
        values.put(ServiceEntry.COL_PASSWORD, getServicePassword(serviceInfo));



        Uri newUri = context.getContentResolver().insert(ServiceEntry.CONTENT_URI_SERVICE, values);

        if (newUri == null) {
            // If the new content URI is null, then there was an error with insertion.
            Log.e(LOG, "Error adding service");
        } else {
            // Otherwise, the insertion was successful and we can display a toast.
            Log.d(LOG, "Service added" + newUri);
        }

    }

    public static void removeServiceToList(Context context, NsdServiceInfo serviceInfo) {
        String[] serviceDetails = new String[]{
                getServiceName(serviceInfo)
                //serviceInfo.getHost().getHostAddress()
        };
        int rowsDeleted = context.getContentResolver().delete(ServiceEntry.CONTENT_URI_SERVICE,
                ServiceEntry.COL_SERVICE_NAME + "=?", serviceDetails);
        Log.v(LOG, rowsDeleted + " row deleted from service database");
    }

    public static void clearServiceList(Context context) {
        int rowsDeleted = context.getContentResolver().delete(ServiceEntry.CONTENT_URI_SERVICE,
                null, null);
        Log.v(LOG, rowsDeleted + " row deleted from service database");
    }

    public static void addDeviceToList(Context context, String name, String ip, int port){
        ContentValues values = new ContentValues();

        values.put(DeviceEntry.COL_DEV_NAME, name);
        values.put(DeviceEntry.COL_DEV_IP, ip);
        values.put(DeviceEntry.COL_DEV_PORT, port);

        //TODO: Add string literals to strings.xml. Use placeholders
        Utility.showToast(context.getApplicationContext(), name + " is connected.");

        Uri newUri = context.getContentResolver().insert(DeviceEntry.CONTENT_URI_DEVICE, values);


        if (newUri == null) {
            // If the new content URI is null, then there was an error with insertion.
            Log.e(LOG, "Error adding device");
        } else {
            // Otherwise, the insertion was successful and we can display a toast.
            Log.d(LOG, "Device added" + newUri);
        }
    }

    public static void removeDeviceToList(final Context context, String ip, int port){
        ServicesDbHelper dbHelper = new ServicesDbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                DeviceEntry.COL_DEV_ID,
                DeviceEntry.COL_DEV_NAME,
        };

        String selection = DeviceEntry.COL_DEV_IP + "=? AND " + DeviceEntry.COL_DEV_PORT + "=?";
        String[] selectionArgs = {
                ip,
                Integer.toString(port)
        };
        Cursor c = db.query(DeviceEntry.TABLE_DEVICES,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null);
        if(c.getCount() == 1) c.moveToFirst();
        int index = c.getColumnIndex(DeviceEntry.COL_DEV_NAME);
        Log.e("DatabaseUtility", "index = " + index);
        String name = c.getString(index);
        c.close();
        db.close();
        dbHelper.close();

        //TODO: Get the name of the device to be removed.
        Utility.showToast(context.getApplicationContext(), name + " leave the room.");

        int rowsDeleted = context.getContentResolver().delete(DeviceEntry.CONTENT_URI_DEVICE,
                selection, selectionArgs);

        Log.v(LOG, rowsDeleted + " row deleted from device database");
    }

    public static void clearDeviceList(Context context){
        int rowsDeleted = context.getContentResolver().delete(ServicesContract.DeviceEntry.CONTENT_URI_DEVICE,
                null, null);
        Log.v(LOG, rowsDeleted + " row deleted from device database");
    }

    public static long getDeviceCount(Context c){
       ServicesDbHelper dbHelper = new ServicesDbHelper(c);
       SQLiteDatabase db = dbHelper.getReadableDatabase();

       long count = DatabaseUtils.queryNumEntries(db, DeviceEntry.TABLE_DEVICES);
       db.close();
       dbHelper.close();
       return count;
    }



}
