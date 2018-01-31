package com.example.android.presentor.db;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import com.example.android.presentor.db.ServicesContract.ServiceEntry;
import com.example.android.presentor.networkservicediscovery.NsdHelper;

import java.io.File;

/**
 * Created by Carlo on 21/10/2017.
 */

public class DatabaseUtility {

    public final static String LOG = "DatabaseUtility";
    public final static String DB_PATH = "data/data/" + ServicesContract.CONTENT_AUTHORITY
            + "/databases/" + ServicesDbHelper.DB_NAME;

    private static String getServiceName(NsdServiceInfo serviceInfo) {
        String serviceName = serviceInfo.getServiceName();
        int endPosition = serviceName.indexOf(NsdHelper.UNDERSCORE);

        return serviceName.substring(0, endPosition);
    }

    private static String getCreatorName(NsdServiceInfo serviceInfo) {
        String serviceName = serviceInfo.getServiceName();
        int startPosition = serviceName.indexOf(NsdHelper.UNDERSCORE) + 1;
        int lastPosition = serviceName.lastIndexOf(NsdHelper.UNDERSCORE);

        return serviceName.substring(startPosition, lastPosition);
    }

    private static String getServicePassword(NsdServiceInfo serviceInfo){
        String serviceName = serviceInfo.getServiceName();
        int startPosition = serviceName.lastIndexOf(NsdHelper.UNDERSCORE) + 1;

        return serviceName.substring(startPosition);
    }

    public static void addServiceToList(Context context, NsdServiceInfo serviceInfo) {
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
        String[] serviceName = new String[]{
                getServiceName(serviceInfo)
        };
        int rowsDeleted = context.getContentResolver().delete(ServiceEntry.CONTENT_URI_SERVICE,
                ServiceEntry.COL_SERVICE_NAME + "=?", serviceName);
        Log.v(LOG, rowsDeleted + " row deleted from service database");
    }

    public static void clearServiceList(Context context) {
        int rowsDeleted = context.getContentResolver().delete(ServiceEntry.CONTENT_URI_SERVICE,
                null, null);
        Log.v(LOG, rowsDeleted + " row deleted from service database");
    }

//    public static void addToConnectedDevice(Context context, DeviceDTO device, String ip) {
//        ContentValues values = new ContentValues();
//
//        values.put(ServicesContract.ServiceEntry.COL_DEV_MODEL, device.getDeviceName());
//        values.put(ServicesContract.ServiceEntry.COL_DEV_IP, device.getIp());
//        values.put(ServicesContract.ServiceEntry.COL_DEV_PORT, device.getPort());
//
//        Uri newUri = context.getContentResolver().insert(ServiceEntry.CONTENT_URI_DEVICE, values);
//
//        if (newUri == null) {
//            // If the new content URI is null, then there was an error with insertion.
//            Log.e(LOG, "Error adding device");
//        } else {
//            // Otherwise, the insertion was successful and we can display a toast.
//            Log.d(LOG, "Device added" + newUri);
//        }
//    }


    public static void clearConnectedDevice(Context context) {
        int rowsDeleted = context.getContentResolver().delete(ServiceEntry.CONTENT_URI_DEVICE,
                null, null);
        Log.v(LOG, rowsDeleted + " row deleted from service database");
    }

    public static void dbCheck() {
        try {
            File file = new File(DB_PATH);
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
