package edu.buffalo.cse.cse486586.groupmessenger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class GMessengerDataBase {
    private static final String LOG_TAG = GMessengerDataBase.class.getName();

	private static final String COL_KEY = "key";
	private static final String COL_VALUE = "value";
	private static final String DATABASE_NAME ="GMessenger.db";
	private static final int DATABASE_VERSION = 5;
	
	private GMessengerDbHelper mDbHelper;
	
	public GMessengerDataBase(Context context) {
	    mDbHelper = new GMessengerDbHelper(context);
    }
	
	public Cursor getValue(String key) {
	    return query(key);
	}
	
	public Uri putValue(ContentValues values) {
	    SQLiteDatabase db= mDbHelper.getWritableDatabase();
	    long id = db.insertWithOnConflict(mDbHelper.getTableName(), null, values,SQLiteDatabase.CONFLICT_REPLACE);
	    return Uri.parse("/" + id);
	}
	
	private Cursor query(String selection) {
	    
	    String[] projectionIn = { COL_KEY, COL_VALUE };
	    String selectionClause = COL_KEY + " = ? ";
	    String[] selectionArgs = { selection } ;
	    
	    SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
	    qBuilder.setTables(mDbHelper.getTableName());
	    
	    Cursor c = qBuilder.query(mDbHelper.getReadableDatabase(), 
	            projectionIn, 
	            selectionClause, 
	            selectionArgs, 
	            null, 
	            null, 
	            null);
	    
	    return c;
	}

	public class GMessengerDbHelper extends SQLiteOpenHelper {
	    
		private static final String GM_TABLE_NAME = "group_messenger";
		private static final String GM_TABLE_CREATE = "CREATE TABLE "
				+ GM_TABLE_NAME + " (" + COL_KEY + " TEXT PRIMARY KEY, "
				+ COL_VALUE + " TEXT);";
		
		//private final Context mDbHelperContext ;
		private SQLiteDatabase mSqliteDatabase;

		public GMessengerDbHelper(Context context) {
			super(context, null, null, DATABASE_VERSION);
			//mDbHelperContext = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
		    mSqliteDatabase = db;
		    db.execSQL("DROP TABLE IF EXISTS " + GM_TABLE_NAME);
		    mSqliteDatabase.execSQL(GM_TABLE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		    Log.d(LOG_TAG, "Upgrading Database to " + newVersion );
		    //db.execSQL("DROP TABLE IF EXISTS " + GM_TABLE_NAME);
		    onCreate(db);
		}
		
		public String getTableName() {
		    return GM_TABLE_NAME;
		}
		
		public SQLiteDatabase getDataBase() {
            return mSqliteDatabase;
        }

	}

}
