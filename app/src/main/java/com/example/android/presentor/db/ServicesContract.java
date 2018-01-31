package com.example.android.presentor.db;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Carlo on 17/10/2017.
 */

public class ServicesContract {

    private ServicesContract() {}

    public final static String CONTENT_AUTHORITY = "com.example.android.presentor";

    public final static Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public final static String PATH_SERVICES = "services";

    public final static String PATH_DEVICES = "devices";

    public static final class ServiceEntry implements BaseColumns {

        public static final Uri CONTENT_URI_SERVICE = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_SERVICES);
        public static final Uri CONTENT_URI_DEVICE = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_DEVICES);
        


        public static final String CONTENT_LIST_TYPE_SERVICE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SERVICES;

        public static final String CONTENT_ITEM_TYPE_SERVICE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SERVICES;

        public static final String CONTENT_LIST_TYPE_DEVICE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SERVICES;

        public static final String CONTENT_ITEM_TYPE_DEVICE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SERVICES;

        public static final String TABLE_DEVICES = "devices";
        
        public static final String COL_DEV_ID = BaseColumns._ID;
        public static final String COL_DEV_IP = "ipaddress";
        public static final String COL_DEV_MODEL = "devicemodel";
        public static final String COL_DEV_PORT = "port";
        public static final String COL_DEV_VERSION = "osversion";
        public static final String COL_DEV_PLAYER = "player";

        
        //DATABASE SERVICES COLUMNS
        public static final String TABLE_SERVICES = "services";
        
        public final static String COL_SERVICE_ID = BaseColumns._ID;
        public static final String COL_IP_ADDRESS = "ipaddress";
        public static final String COL_PORT_NUMBER = "portnumber";
        public static final String COL_SERVICE_NAME = "servicename";
        public static final String COL_CREATOR_NAME = "creatorname";
        public static final String COL_PASSWORD = "password";



    }

}
