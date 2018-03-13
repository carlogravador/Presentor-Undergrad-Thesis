package com.example.android.presentor.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.presentor.db.ServicesContract.ServiceEntry;
import com.example.android.presentor.db.ServicesContract.DeviceEntry;

/**
 * Created by Carlo on 17/10/2017.
 */

public class ServicesProvider extends ContentProvider {

    private final static int DEVICES = 200;

    private final static int DEVICE_ID = 201;

    private final static int SERVICES = 100;

    private final static int SERVICE_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {

        sUriMatcher.addURI(ServicesContract.CONTENT_AUTHORITY, ServicesContract.PATH_SERVICES, SERVICES);

        sUriMatcher.addURI(ServicesContract.CONTENT_AUTHORITY, ServicesContract.PATH_SERVICES + "/#", SERVICE_ID);

        sUriMatcher.addURI(ServicesContract.CONTENT_AUTHORITY, ServicesContract.PATH_DEVICES, DEVICES);

        sUriMatcher.addURI(ServicesContract.CONTENT_AUTHORITY, ServicesContract.PATH_DEVICES + "/#", DEVICE_ID);

    }

    private ServicesDbHelper mServicesDbHelper;

    @Override
    public boolean onCreate() {
        mServicesDbHelper = new ServicesDbHelper(getContext());
        return false;
    }


    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mServicesDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case SERVICES:
                // For the PETS code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.
                cursor = db.query(ServiceEntry.TABLE_SERVICES, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case SERVICE_ID:
                // For the PET_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.pets/pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = ServiceEntry.COL_SERVICE_ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = db.query(ServiceEntry.TABLE_SERVICES, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case DEVICES:
                // For the PETS code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.
                cursor = db.query(DeviceEntry.TABLE_DEVICES, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case DEVICE_ID:
                // For the PET_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.pets/pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = DeviceEntry.COL_DEV_ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = db.query(DeviceEntry.TABLE_DEVICES, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }



    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SERVICES:
                return ServiceEntry.CONTENT_LIST_TYPE_SERVICE;
            case SERVICE_ID:
                return ServiceEntry.CONTENT_ITEM_TYPE_SERVICE;
            case DEVICES:
                return DeviceEntry.CONTENT_LIST_TYPE_DEVICE;
            case DEVICE_ID:
                return DeviceEntry.CONTENT_ITEM_TYPE_DEVICE;
            default:
                throw new IllegalStateException("Unknown URI" + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch(match){
            case SERVICES:
                return insertService(uri, values);
            case DEVICES:
                return insertDevice(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    public Uri insertService(Uri uri, ContentValues contentValues) {
        SQLiteDatabase db = mServicesDbHelper.getWritableDatabase();

        long id = db.insert(ServiceEntry.TABLE_SERVICES, null, contentValues);

        if(id==-1){
            Log.e("ServicesProvider", "Failed to Insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    public Uri insertDevice(Uri uri, ContentValues contentValues) {
        SQLiteDatabase db = mServicesDbHelper.getWritableDatabase();

        long id = db.insert(DeviceEntry.TABLE_DEVICES, null, contentValues);

        if(id==-1){
            Log.e("ServicesProvider", "Failed to Insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = mServicesDbHelper.getWritableDatabase();
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SERVICES:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(ServiceEntry.TABLE_SERVICES, selection, selectionArgs);
                break;
            case DEVICES:
                rowsDeleted = database.delete(DeviceEntry.TABLE_DEVICES, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0 ) {
            getContext().getContentResolver().notifyChange(uri, null);
        }



        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
