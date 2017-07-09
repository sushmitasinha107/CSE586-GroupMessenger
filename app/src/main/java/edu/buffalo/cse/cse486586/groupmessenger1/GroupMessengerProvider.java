package edu.buffalo.cse.cse486586.groupmessenger1;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentProvider;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * GroupMessengerProvider is a key-value table. Once again, please note that we do not implement
 * full support for SQL as a usual ContentProvider does. We re-purpose ContentProvider's interface
 * to use it as a key-value table.
 * 
 * Please read:
 * 
 * http://developer.android.com/guide/topics/providers/content-providers.html
 * http://developer.android.com/reference/android/content/ContentProvider.html
 * 
 * before you start to get yourself familiarized with ContentProvider.
 * 
 * There are two methods you need to implement---insert() and query(). Others are optional and
 * will not be tested.
 * 
 * @author stevko
 *
 */
public class GroupMessengerProvider extends ContentProvider {


     class GroupMessengerProviderHelper extends SQLiteOpenHelper {

         public GroupMessengerProviderHelper(Context context, String name,SQLiteDatabase.CursorFactory factory, int version) {
             super(context, name, factory, version);
         }

         @Override
         public void onUpgrade(SQLiteDatabase db,int oldVersion, int newVersion) {
             onCreate(db);
         }

         @Override
         public void onCreate(SQLiteDatabase db) {
             db.execSQL("DROP TABLE IF EXISTS" + tableName);
             db.execSQL("CREATE TABLE  IF NOT EXISTS "+tableName+" (key TEXT, value TEXT);");

         }

    }
    private GroupMessengerProviderHelper createDB;
    private static String tableName= "message_value";

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // You do not need to implement this.
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /*
         * TODO: You need to implement this method. Note that values will have two columns (a key
         * column and a value column) and one row that contains the actual (key, value) pair to be
         * inserted.
         * 
         * For actual storage, you can use any option. If you know how to use SQL, then you can use
         * SQLite. But this is not a requirement. You can use other storage options, such as the
         * internal storage option that we used in PA1. If you want to use that option, please
         * take a look at the code for PA1.
         */
        SQLiteDatabase database = createDB.getWritableDatabase();
        String [] selectionArgs ={values.getAsString("key")};
        Cursor cursor = database.query(tableName, null , "key = ?",selectionArgs,null,null,null);
        int count = cursor.getCount();
        if(count<1)
            database.insert(tableName, null ,values);
        else
            database.update(tableName ,values,"key = ?", selectionArgs );
        Log.v("insert", values.toString());
        return uri;
    }

    @Override
    public boolean onCreate() {
        // If you need to perform any one-time initialization task, please do it here.
        createDB = new GroupMessengerProviderHelper(getContext(),tableName,null,1);
        return false;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        /*
         * TODO: You need to implement this method. Note that you need to return a Cursor object
         * with the right format. If the formatting is not correct, then it is not going to work.
         *
         * If you use SQLite, whatever is returned from SQLite is a Cursor object. However, you
         * still need to be careful because the formatting might still be incorrect.
         *
         * If you use a file storage option, then it is your job to build a Cursor * object. I
         * recommend building a MatrixCursor described at:
         * http://developer.android.com/reference/android/database/MatrixCursor.html
         */
        SQLiteDatabase database = createDB.getWritableDatabase();
        String [] selectionArgs1 ={selection};
        Cursor cursor = database.query(tableName, null , "key = ?",selectionArgs1,null,null,null);

        Log.v("query", selection);
        return cursor;
    }

}
