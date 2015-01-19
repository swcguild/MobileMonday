package swcguild.com.contactbook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;


/**
 * Helper class for interacting with the database
 */
public class DatabaseConnector {
    private static final String DATABASE_NAME = "UserContacts";

    // we'll use this to interact with the database
    private SQLiteDatabase database;
    // helper class for opening and creating the database (defined below)
    private DatabaseOpenHelper databaseOpenHelper;

    // Our public constructor - it instantiates the DatabaseOpenHelper
    public DatabaseConnector(Context context) {
        databaseOpenHelper = new DatabaseOpenHelper(context, DATABASE_NAME, null, 1);
    }

    /**
     * Helper method for opening the database.  Calls getWrtiableDatabase which will
     * create our database if it has not already been created.  If our database exists,
     * it will simply open it.
     *
     * @throws SQLException if the connection attempt to the database fails
     */
    public void open() throws SQLException {
        database = databaseOpenHelper.getWritableDatabase();
    }

    /**
     * Helper method for closing the database
     */
    public void close() {
        if (database != null) {
            database.close();
        }
    }

    /**
     * Inserts a new contact into the database
     * @param name
     * @param phone
     * @param email
     * @return the row id of the newly inserted contact
     */
    public long insertContact(String name, String phone, String email) {
        // Put contact values into a new ContentValues object.  The ContentValues
        // object is a map of key/value pairs that we can pass to the database.
        ContentValues newContact = new ContentValues();
        newContact.put("name", name);
        newContact.put("phone", phone);
        newContact.put("email", email);

        open();
        // Inserts the data in the newly created ContentValues object into the table "contact"
        // The second parameter is not used here - it is a way to tell SQLite about any nullable
        // columns.  Returns the rowID of the newly insert row.
        long rowID = database.insert("contacts", null, newContact);
        close();
        return rowID;
    }

    /**
     * Updates a contact in the database
     * @param id id of contact to be updated
     * @param name
     * @param phone
     * @param email
     */
    public void updateContact(long id, String name, String phone, String email) {
        ContentValues editContact = new ContentValues();
        editContact.put("name", name);
        editContact.put("phone", phone);
        editContact.put("email", email);

        open();
        // Similar to the insert method above.  First parameter is the table name, the
        // second parameter is the map of key/value pairs to send to the database, the
        // third parameter is the WHERE clause (in this case it will be WHERE _id = <id>),
        // and the fourth parameter is an optional list of prepared statement style WHERE
        // clause parameters (you can have question marks in your WHERE clause - the fourth
        // argument contains the values to be used to replace the questions marks in the
        // WHERE clause)
        database.update("contacts", editContact, "_id=" + id, null);
        close();
    }

    /**
     * Retrieves information associated with the contact having the given id
     * @param id id of contact to be retrieved
     * @return the requested contact information
     */
    public Cursor getOneContact(long id) {
        // The 'query' method takes the following parameters:
        // 1 - table name
        // 2 - String array of the column names to return - null indicates all columns
        // 3 - The WHERE clause (in this case we'll match the value of the given id)
        // 4 - Array of values that can replace the ? placeholder values in the WHERE clause (if
        //     any)
        // 5 - GROUP BY clause - null indicates no GROUP BY clause
        // 6 - HAVING clause - must be null if GROUP BY is null
        // 7 - ORDER BY clause - null indicates no ORDER BY clause
        return database.query("contacts", null, "_id=" + id, null, null, null, null);
    }

    /**
     * Retrieves the information for contacts in the database
     * @return information for all contacts in the database
     */
    public Cursor getAllContacts() {
        // See getOneContact comment for explanation of 'query' parameters.  In this case
        // we are retrieving _id and name for every row in the database, ordered by name
        return database.query("contacts", new String[] {"_id", "name"}, null, null, null, null, "name");
    }

    /**
     * Deletes the contact with the given id from the database
     * @param id id of contact to delete
     */
    public void deleteContact(long id) {
        open();
        // The three parameters for delete are:
        // 1 - table name
        // 2 - WHERE clause
        // 3 - values to substitute into WHERE clause (if needed)
        database.delete("contacts", "_id=" + id, null);
        close();
    }

    /**
     * Helper class for opening, creating, and upgrading the database.
     */
    private class DatabaseOpenHelper extends SQLiteOpenHelper {

        /**
         * Creates the class
         * @param context Context in which the database is being opened/created
         * @param name Name of the database
         * @param factory CursorFactory to be used - null indicates that the default should be used
         *                which is typical for most apps
         * @param version version of the database to open
         */
        public DatabaseOpenHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        /**
         * Creates the database
         * @param db
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            // Creates the contacts table in the newly created database
            String createQuery = "CREATE TABLE contacts (_id integer primary key autoincrement, name TEXT, phone TEXT, email TEXT)";
            db.execSQL(createQuery);
        }

        /**
         * This method is called if someone tries to open our database with a higher version number than
         * the existing database.  We have no need for this feature so we're going to just supply
         * an empty implementation.
         * @param db
         * @param oldVersion
         * @param newVersion
         */
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
