package mcgee.contentprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;

public class BirthdayProvider extends ContentProvider {

    //fields for my content provider
    static final String PROVIDER_NAME = "mcgee.contentprovider.BirthdayProvider";
    static final String URL = "content://" + PROVIDER_NAME + "/friends";
    static final Uri CONTEXT_URI = Uri.parse(URL);

    //fields for the database
    static final String ID = "id";      //primary key
    static final String NAME = "name";
    static final String BIRTHDAY = "birthday";

    //integer values used in content URI
    static final int FRIENDS = 1;
    static final int FRIENDS_ID = 2;

    DBHelper dbHelper;

    //project map for a query
    private static HashMap<String, String> BirthdayMap;

    //maps content URI "patterns" to the integer values that were set above
    static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "friends", FRIENDS);
        uriMatcher.addURI(PROVIDER_NAME, "friends/#", FRIENDS_ID);
    }

    //database decorations
    private SQLiteDatabase database;
    static final String DATABASE_NAME = "BirthdayReminder.db";
    static final String TABLE_NAME = "birthdayTable";
    static final int DATABASE_VERSION = 1;
    static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (id INTEGER PRIMARY KEY AUTOINCREMENT, " + " name TEXT NOT NULL, " + " birthday TEXT NOT NULL);";

    public BirthdayProvider() {
    }

    //class that creates and manages the content provider's database
    private static class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.v(DBHelper.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ". Old data will be destroyed");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        int count = 0;

        switch(uriMatcher.match(uri)) {
            //maps all database column names
            case FRIENDS:
                count = database.delete(TABLE_NAME, selection, selectionArgs);
                break;
            case FRIENDS_ID:
                //TextUtils.isEmpty checks whether the user entered anything into the field
                //getLastPathSegment gets the last decoded segment in the path, so if the last segment is ID, it will return the ID
                count = database.delete(TABLE_NAME, ID + " = " + uri.getLastPathSegment() + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;

    }

    @Override
    public String getType(Uri uri) {

        switch(uriMatcher.match(uri)) {
            //get all friends birthday records
            case FRIENDS:
                return "vnd.android.cursor.dir/vnd.mcgee.friends";
            //gets a particular friend
            case FRIENDS_ID:
                return "vnd.android.cursor.item/vnd.mcgee.friends";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long row = database.insert(TABLE_NAME, "", values);

        //if record is added successfully
        if (row > 0) {
            Uri newUri = ContentUris.withAppendedId(CONTEXT_URI, row);
            getContext().getContentResolver().notifyChange(newUri, null);

            return newUri;
        }

        throw new SQLiteException("Fail to add a new record into " + uri);
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        dbHelper = new DBHelper(context);
        //permissions to be writable
        database = dbHelper.getWritableDatabase();
        if(database == null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        //the TABLE_NAME to query on
        queryBuilder.setTables(TABLE_NAME);

        switch(uriMatcher.match(uri)) {
            //maps all database column names
            case FRIENDS:
                queryBuilder.setProjectionMap(BirthdayMap);
                break;
            case FRIENDS_ID:
                queryBuilder.appendWhere(ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if(sortOrder == null || sortOrder == "") {
            //no sorting -> sort names by default
            sortOrder = NAME;
        }

        Cursor cursor = queryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);

        //register to watch a content URI for changes
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        int count = 0;

        switch(uriMatcher.match(uri)) {
            //maps all database column names
            case FRIENDS:
                count = database.update(TABLE_NAME, values, selection, selectionArgs);
                break;
            case FRIENDS_ID:
                //TextUtils.isEmpty checks whether the user entered anything into the field
                //getLastPathSegment gets the last decoded segment in the path, so if the last segment is ID, it will return the ID
                count = database.update(TABLE_NAME, values, ID + " = " + uri.getLastPathSegment() + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;

    }
}
