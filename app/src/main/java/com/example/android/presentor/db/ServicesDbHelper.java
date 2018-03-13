package com.example.android.presentor.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.presentor.db.ServicesContract.ServiceEntry;
import com.example.android.presentor.db.ServicesContract.DeviceEntry;
/**
 * Authored by KaushalD on 8/27/2016.
 */
public class ServicesDbHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "services.db";



    public ServicesDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_SERVICE_TABLE = "CREATE TABLE " + ServiceEntry.TABLE_SERVICES + "("
                + ServiceEntry.COL_SERVICE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ServiceEntry.COL_PORT_NUMBER + "  INTEGER, "
                + ServiceEntry.COL_SERVICE_NAME + " TEXT NOT NULL, "
                + ServiceEntry.COL_CREATOR_NAME + " TEXT NOT NULL, "
                + ServiceEntry.COL_PASSWORD + " TEXT, "
                + ServiceEntry.COL_IP_ADDRESS + " TEXT NOT NULL );" ;

        String SQL_CREATE_DEVICES_TABLE = "CREATE TABLE " + DeviceEntry.TABLE_DEVICES + "("
                + DeviceEntry.COL_DEV_ID+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DeviceEntry.COL_DEV_PORT + "  INTEGER, "
                + DeviceEntry.COL_DEV_NAME+ " TEXT NOT NULL, "
                + DeviceEntry.COL_DEV_IP+ " TEXT NOT NULL );";


        db.execSQL(SQL_CREATE_SERVICE_TABLE);
        db.execSQL(SQL_CREATE_DEVICES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        //TODO handle db upgrade for existing users here
    }
}
