package halamish.reem.budget.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import halamish.reem.budget.MyAdapter;
import halamish.reem.budget.R;
import halamish.reem.budget.misc.Utils;

/**
 * Created by Re'em on 10/17/2015.
 *
 * this class handles sqlite operations
 */
public class DatabaseHandler extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHandler";

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 8;
    // version 4: added column MA_KEY_ORDER_ID
    // version 5: added column RE_KEY_EVENT_TYPE and switched column RE_KEY_DATE to INTEGER
    // version 6: column MA_KEY_CUR_VALUE only stores values from non-Archived lines
    // version 7: after every AUTO_UPDATE budget line there is MOVER_FROM_LAST budget line
    // version 8: added column MA_KEY_NAME_TO_SHOW to master table

    // Database Name
    private static final String DATABASE_NAME = "budget";

    // master table name
    private static final String TABLE_ALL_TALBES = "all_tables";

    // master Table Columns names
    private static final String MA_KEY_ID = "ma_id";
    private static final String MA_KEY_NAME = "name";
    private static final String MA_KEY_CUR_VALUE = "budget";
    private static final String MA_KEY_AUTO_UPDATE = "auto_update";
    private static final String MA_KEY_UPDATE_AMOUNT = "auto_update_amount";
    private static final String MA_KEY_ORDER_ID = "order_by";
    private static final String MA_KEY_NAME_TO_SHOW = "name_to_show";

    // regular table (i.e. action submitted) Columns names
    private static final String RE_KEY_TITLE = "title";
    private static final String RE_KEY_DETAILS = "details";
    private static final String RE_KEY_DATE = "date";
    private static final String RE_KEY_ID = "re_id";
    private static final String RE_KEY_AMOUNT = "amount";
    private static final String RE_KEY_EVENT_TYPE = "type"; // 0. change auto-update 1. user input  2. archive ends here


    private static List<MyAdapter> s_adapters;
    private Context context;

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        if (s_adapters == null)
            s_adapters = new ArrayList<>();
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_MAIN_TABLE = "CREATE TABLE " + TABLE_ALL_TALBES + "(" +
                MA_KEY_ID + " INTEGER PRIMARY KEY," +
                MA_KEY_NAME + " TEXT," +
                MA_KEY_CUR_VALUE + " INTEGER," +
                MA_KEY_AUTO_UPDATE + " TEXT," +
                MA_KEY_UPDATE_AMOUNT + " INTEGER," +
                MA_KEY_ORDER_ID + " INTEGER," +
                MA_KEY_NAME_TO_SHOW + " TEXT" +
                ")";
        sqLiteDatabase.execSQL(CREATE_MAIN_TABLE);
        notifyAdapters();
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        List<BudgetItem> open_items = getAllBudgetItems(db);
        HashMap<BudgetItem, List<BudgetLine>> item_to_lines = new HashMap<>();
        for (BudgetItem item : open_items) {
                item_to_lines.put(item, tblGetAllBudgetLines(item, db));
                db.execSQL("DROP TABLE IF EXISTS " + item.getName());
        }
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALL_TALBES);

        onCreate(db);
        for (BudgetItem item : open_items) {
            addBudgetItem(item, db);
//            updateFromOutside(item, item_to_lines.get(item), db);
            tblAddAllBudgetLines(item, item_to_lines.get(item), db);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void deleteAll(SQLiteDatabase db) {
        boolean externalDb = db != null;
        if (!externalDb)
            db = this.getWritableDatabase();

        List<BudgetItem> open_items = getAllBudgetItems(db);

        for (BudgetItem item : open_items) {
            if (item.getName() != null)
                db.execSQL("DROP TABLE IF EXISTS " + item.getName());
        }
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALL_TALBES);

        onCreate(db);

        if (!externalDb)
            db.close();
    }


    private void _dbUpdateAllItemsCurAmount() {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ALL_TALBES, null);
        List<BudgetItem> budgetItems = new ArrayList<>();
        if (cursor == null) { db.close(); }
        else if (cursor.moveToFirst()) {
            do {
                BudgetItem item = new BudgetItem();
                item.setId(Integer.parseInt(cursor.getString(0)));
                item.setName(cursor.getString(1));
                item.setCur_value(Integer.parseInt(cursor.getString(2)));
                item.setAuto_update(cursor.getString(3));
                item.setAuto_update_amount(Integer.parseInt(cursor.getString(4)));

                // Adding item to list
                budgetItems.add(item);
            } while (cursor.moveToNext());
        }
        assert cursor != null;
        cursor.close();

        // step 2: for each item, update it's curValue
        for (BudgetItem item : budgetItems) {
            int curAmount = 0;
            String selectQuery = "SELECT  * FROM " + item.getName();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor == null) {
                continue;
            }
            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
//                    BudgetLine line = new BudgetLine();
//                    line.setId(Integer.parseInt(cursor.getString(0)));
//                    line.setTitle(cursor.getString(1));
//                    line.setDetails(cursor.getString(2));
                    curAmount += cursor.getInt(3);
//                    line.setDate(cursor.getString(4));
                } while (cursor.moveToNext());
            }

            cursor.close();

            // update the item
            ContentValues values = new ContentValues();
            values.put(MA_KEY_CUR_VALUE, curAmount);

            // updating row
            db.update(TABLE_ALL_TALBES, values, MA_KEY_NAME + " = ?",
                    new String[] { item.getName() });
        }


    }

    private boolean _dbCheckIfInside(BudgetItem item, SQLiteDatabase db) {
        Boolean retval;

        Cursor cursor = db.query(TABLE_ALL_TALBES, new String[]{MA_KEY_ID,
                        MA_KEY_NAME, MA_KEY_CUR_VALUE}, MA_KEY_NAME + "=?",
                new String[]{String.valueOf(item.getName())}, null, null, null, null);
        retval = (cursor != null && cursor.moveToFirst());

        if (cursor != null) {cursor.close();}

        return retval;
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations on TABLES
     */

    // Adding new item - external
    // includes also the header line "budget <?> created!"
    public void addBudgetItem(BudgetItem item) {
        SQLiteDatabase db = getWritableDatabase();
        addBudgetItem(item, db);

        BudgetLine firstLine = new BudgetLine(
                context.getString(R.string.db_budget_created),
                context.getString(R.string.db_automatic),
                item.getAuto_update_amount(),
                Utils.getMillisecondNow(),
                BudgetLine.BudgetLineEventType.BUDGET_CREATED
        );

        tblAddBudgetLineActual(item, firstLine, db);
        db.close();
        notifyAdapters();
    }

    // Adding new item - internal
    private void addBudgetItem(BudgetItem item, SQLiteDatabase db) {
        boolean externalDB = (db != null);
        if (!externalDB) {
            db = this.getWritableDatabase();
        }
        item.setName(_helper_getNextItemName(db));

        if (_dbCheckIfInside(item, db)) {
            Toast.makeText(context, "Can't add item - name already exists!", Toast.LENGTH_LONG).show();
            if (!externalDB) {
                db.close();
            }
            return;
        }
        ContentValues values = new ContentValues();
        values.put(MA_KEY_NAME, _helper_getNextItemName(db));
        values.put(MA_KEY_CUR_VALUE, 0);
        values.put(MA_KEY_AUTO_UPDATE, item.getAuto_update());
        values.put(MA_KEY_UPDATE_AMOUNT, item.getAuto_update_amount());
        values.put(MA_KEY_ORDER_ID, _helper_getHighestID(db) + 1);
        values.put(MA_KEY_NAME_TO_SHOW, item.getPretty_name());


        // Inserting row to the main table
        db.insert(TABLE_ALL_TALBES, null, values);

        tblCreateTable(item, db);

        if (!externalDB) {
            db.close(); // Closing database connection
        }

    }

    private String _helper_getNextItemName(SQLiteDatabase db) {
        return MA_KEY_NAME + (_helper_getHighestID(db) + 1);
    }


    // external!
    public BudgetItem getBudgetItem(String budgetItemName) {
        return getBudgetItemActual(budgetItemName, null);
    }

    // internal actual
    private BudgetItem getBudgetItemActual(String budgetItemName, SQLiteDatabase db) {
        BudgetItem retval;
        boolean externalDB = (db != null);
        if (!externalDB) {
            db = this.getWritableDatabase();
        }
        Cursor cursor = db.query(TABLE_ALL_TALBES, new String[]{
                        MA_KEY_ID,
                        MA_KEY_NAME,
                        MA_KEY_CUR_VALUE,
                        MA_KEY_AUTO_UPDATE,
                        MA_KEY_UPDATE_AMOUNT,
                        MA_KEY_ORDER_ID,
                        MA_KEY_NAME_TO_SHOW}
                , MA_KEY_NAME + "=?",
                new String[]{String.valueOf(budgetItemName)}, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            // return item
            retval = new BudgetItem(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getInt(2),
                    cursor.getString(3),
                    cursor.getInt(4),
                    cursor.getInt(5),
                    cursor.getString(6)
            );
            cursor.close();
            if (!externalDB)
                db.close();
            return retval;
        }

        if(!externalDB)
            db.close();
        return null;
    }

    // public wrapper
    public List<BudgetItem> getAllBudgetItems() {
        SQLiteDatabase db = getWritableDatabase();
        return getAllBudgetItems(db);
    }

    // Getting All budgetItems
    private List<BudgetItem> getAllBudgetItems(SQLiteDatabase db) {
        List<BudgetItem> budgetItems = new ArrayList<>();
        boolean externalDB = db != null;


        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_ALL_TALBES + " ORDER BY " + MA_KEY_ORDER_ID;

        if (!externalDB)
            db = getWritableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor == null) {
            if (!externalDB)
                db.close();
            return budgetItems;
        }
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                BudgetItem item = new BudgetItem();
                item.setId(cursor.getInt(0));
                item.setName(cursor.getString(1));
                item.setCur_value(cursor.getInt(2));
                item.setAuto_update(cursor.getString(3));
                item.setAuto_update_amount(cursor.getInt(4));
                item.setPretty_name(cursor.getString(6)); // 5 is ORDER_BY

                // Adding item to list
                budgetItems.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        if (!externalDB)
            db.close();

        return budgetItems;
    }

    // Updating single item
    public int updateBudgetItem(BudgetItem old_item, BudgetItem new_item, SQLiteDatabase db) {
        // Log.d(TAG, "update method is : " + new_item.getAuto_update());
        int retval;
        boolean externalDb = db != null;
        if (!externalDb)
            db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MA_KEY_NAME_TO_SHOW, new_item.getPretty_name());
        values.put(MA_KEY_AUTO_UPDATE, new_item.getAuto_update());
        values.put(MA_KEY_UPDATE_AMOUNT, new_item.getAuto_update_amount());


        // updating row
        retval = db.update(TABLE_ALL_TALBES, values, MA_KEY_NAME + " = ?",
                new String[] { old_item.getName() });

        // edit item name if necessary
        // should not happen in normal state, but just in case...
        if (new_item.getName() == null) new_item.setName(old_item.getName());
        if (! old_item.getName().equals(new_item.getName())) {
            String rename_table = "ALTER TABLE " + old_item.getName() + " RENAME TO " + new_item.getName();
            db.execSQL(rename_table);

        }

        // add row of increase\decrease if necessary
        int diff = new_item.getAuto_update_amount() - old_item.getAuto_update_amount();
        if (diff != 0) {
            String title = (diff > 0) ? context.getString(R.string.db_budget_increase) : context.getString(R.string.db_budget_decrease);
            // max = (a > b) ? a : b;
            String details = context.getString(R.string.db_automatic);
            BudgetLine new_line = new BudgetLine(
                    title,
                    details,
                    diff,
                    Utils.getMillisecondNow(),
                    BudgetLine.BudgetLineEventType.BUDGET_CHANGE_AMOUNT
            );

            tblAddBudgetLineActual(new_item, new_line, db);
        }

        if (! externalDb)
            db.close();

        if (! old_item.getPretty_name().equals(new_item.getPretty_name()))
            notifyAdapters();
        return retval;
    }
    /** should be called only after the LINES have been updated! */
    private void updateItemCurAmount(BudgetItem item, SQLiteDatabase db) {
        boolean externalDB = db != null;
        if (!externalDB)
            db = getWritableDatabase();

        int updatedCurAmount = tblGetCurAmount(item, db);
        ContentValues values = new ContentValues();
        values.put(MA_KEY_CUR_VALUE, updatedCurAmount);
        db.update(TABLE_ALL_TALBES, values, MA_KEY_NAME + " = ?",
                new String[]{item.getName()});


        if (!externalDB)
            db.close();
    }


    // Deleting single budget item
    public void deleteBudgetItem(BudgetItem item, SQLiteDatabase db) {
        boolean externalDb = db != null;
        if (!externalDb)
            db = this.getWritableDatabase();

        tblDeleteTable(item, db);
        db.delete(TABLE_ALL_TALBES, MA_KEY_ID + " = ?",
                new String[]{String.valueOf(item.getId())});

        if (!externalDb)
            db.close();

        notifyAdapters();
    }

    // deleting multiple budget items:
    public void deleteBudgetItems(Collection<String> items, SQLiteDatabase db) {
        boolean externalDb = db != null;
        if (!externalDb)
            db = this.getWritableDatabase();

        for (String itemName : items) {
            BudgetItem item = getBudgetItemActual(itemName, db);
            tblDeleteTable(item, db);
            db.delete(TABLE_ALL_TALBES, MA_KEY_ID + " = ?",
                    new String[]{String.valueOf(item.getId())});
        }


        if (!externalDb)
            db.close();

        notifyAdapters();
    }

    // clearing some item's budget
    public void clearBudgetItem(BudgetItem item) {
        SQLiteDatabase db = this.getWritableDatabase();

//        BudgetLine header = tblGetBudgetItemHeaderLine(item, db);
        tblDeleteTable(item, db);
        tblCreateTable(item, db);
        BudgetLine clearedHeader = new BudgetLine(context.getString(R.string.db_budget_created), context.getString(R.string.db_cleared_as_user_requested), item.getAuto_update_amount(), Utils.getMillisecondNow(), BudgetLine.BudgetLineEventType.BUDGET_CREATED);
        tblAddBudgetLineActual(item, clearedHeader, db);

        db.close();
        notifyAdapters();
    }

    /**
     * swaps the positions of two budget items
     * @param a
     * @param b
     */
    public void swap_two_items_order(BudgetItem a, BudgetItem b, SQLiteDatabase db) {
        a.logMyself();
        b.logMyself();
        boolean externalDb = db != null;
        if (!externalDb)
            db = this.getWritableDatabase();

        int a_id = a.getOrder_id();
        int b_id = b.getOrder_id();

        // update a
        ContentValues values = new ContentValues();
        values.put(MA_KEY_ORDER_ID, b_id);
        db.update(TABLE_ALL_TALBES, values, MA_KEY_NAME + " = ?",
                new String[] { a.getName() });

        // update b
        values = new ContentValues();
        values.put(MA_KEY_ORDER_ID, a_id);
        db.update(TABLE_ALL_TALBES, values, MA_KEY_NAME + " = ?",
                new String[]{b.getName()});

        a.logMyself();
        b.logMyself();

        if (!externalDb)
            db.close();

        notifyAdapters();
    }



    // Getting budget items Count
    public int getItemsCount(SQLiteDatabase db) {
        int retval;
        String countQuery = "SELECT  * FROM " + TABLE_ALL_TALBES;

        boolean externalDb = db != null;
        if (!externalDb)
            db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);


        // return count
        retval = cursor.getCount();
        cursor.close();
        if (!externalDb)
            db.close();

        return retval;
    }

    private List<BudgetItem> getAllAutoUpdate(String type, SQLiteDatabase db) {
        List<BudgetItem> budgetItems = new ArrayList<>();
        boolean externalDB = db != null;
        if (!externalDB)
            db = getWritableDatabase();

        Cursor cursor = db.query(TABLE_ALL_TALBES, new String[]{MA_KEY_ID,
                        MA_KEY_NAME, MA_KEY_CUR_VALUE, MA_KEY_AUTO_UPDATE, MA_KEY_UPDATE_AMOUNT}, MA_KEY_AUTO_UPDATE + "=?",
                new String[]{type}, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                BudgetItem item = new BudgetItem();
                item.setId(Integer.parseInt(cursor.getString(0)));
                item.setName(cursor.getString(1));
                item.setCur_value(Integer.parseInt(cursor.getString(2)));
                item.setAuto_update(type);
                item.setAuto_update_amount(Integer.parseInt(cursor.getString(4)));

                // Adding item to list
                budgetItems.add(item);
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (!externalDB)
            db.close();

        return budgetItems;

    }

    public List<BudgetItem> getAllWeeklyUpdate() {
        return getAllAutoUpdate(BudgetItem.WEEKLY, null);
    }

    public List<BudgetItem> getAllMonthlyUpdate() {
        return getAllAutoUpdate(BudgetItem.MONTHLY, null);
    }

    /**
     * from now on, those are actions on the BudgetItem tables! not the main table!
     */

    private void tblCreateTable(BudgetItem item, SQLiteDatabase db) {
        // receives db, doesn't need to open and close it

        if (item.getName() == null) { return; }
            String CREATE_BUDGET_TABLE = "CREATE TABLE IF NOT EXISTS " + item.getName() + "(" +
                    RE_KEY_ID + " INTEGER PRIMARY KEY," +
                    RE_KEY_TITLE + " TEXT," +
                    RE_KEY_DETAILS + " TEXT," +
                    RE_KEY_AMOUNT + " INTEGER," +
                    RE_KEY_DATE +  " INTEGER," +
                    RE_KEY_EVENT_TYPE + " INTEGER" +
                    ")";
        db.execSQL(CREATE_BUDGET_TABLE);
    }

    private void tblDeleteTable(BudgetItem item, SQLiteDatabase db) {
        // receives db, doesn't need to open and close it
        db.execSQL("DROP TABLE IF EXISTS " + item.getName());
    }

    // Adding new line
    private void tblAddBudgetLineActual(BudgetItem item, BudgetLine line, SQLiteDatabase db) {
        boolean externalDB = db != null;
        if (!externalDB)
            db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(RE_KEY_TITLE, line.getTitle());
        values.put(RE_KEY_DETAILS, line.getDetails());
        values.put(RE_KEY_AMOUNT, line.getAmount());
        values.put(RE_KEY_DATE, line.getDate());
        values.put(RE_KEY_EVENT_TYPE, line.getEventType().toInt());

        // Inserting row to the relevant table
        db.insert(item.getName(), null, values);

        // updating the ITEM in the TABLE_ALL_TABLES
        updateItemCurAmount(item, db);

        if (!externalDB)
            db.close(); // Closing database connection
    }

    // wrapper for adding, includes notifiynig
    public void tblAddBudgetLine(BudgetItem item, BudgetLine line) {
        SQLiteDatabase db = getWritableDatabase();
        tblAddBudgetLineActual(item, line, db);
        db.close();
        notifyAdapters();
    }

    //Adding multiple lines
    public void tblAddAllBudgetLines(BudgetItem item, List<BudgetLine> lines, SQLiteDatabase db) {
        boolean externalDB = db != null;
        if (!externalDB)
            db = getWritableDatabase();

        for (BudgetLine line : lines)
            tblAddBudgetLineActual(item, line, db);
        if (!externalDB)
            db.close(); // Closing database connection


    }


    // Getting the header line
    private BudgetLine tblGetBudgetItemHeaderLine(BudgetItem item, SQLiteDatabase db) {
        BudgetLine retval;

        boolean externalDB = db != null;
        if (! externalDB)
            db = this.getWritableDatabase();

        String selectQuery = "SELECT  * FROM " + item.getName() + " ORDER BY ROWID ASC LIMIT 1";

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor == null) {
            if (!externalDB)
                db.close();
            return null;
        }
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            BudgetLine line = new BudgetLine(
                        Integer.parseInt(cursor.getString(0)),
                        cursor.getString(1),
                        cursor.getString(2),
                        Integer.parseInt(cursor.getString(3)),
                        Long.parseLong(cursor.getString(4)),
                    BudgetLine.BudgetLineEventType.getEnum(cursor.getString(5)));

                // Adding item to list
            retval = line;
            cursor.close();
            if (!externalDB)
                db.close();
            return retval;

        }

        if (!externalDB)
            db.close();
        return null;
    }

    // public wrapper
    public List<BudgetLine> tblGetAllBudgetLines(BudgetItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        return tblGetAllBudgetLines(item, db);
    }
    // Getting All budgetLines
    private List<BudgetLine> tblGetAllBudgetLines(BudgetItem item, SQLiteDatabase db) {
        List<BudgetLine> budgetLines = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + item.getName();

        boolean externalDB = db != null;
        if (! externalDB)
            db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor == null) {
            if (!externalDB)
                db.close();
            return budgetLines;
        }
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                BudgetLine line = new BudgetLine(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getInt(3),
                    cursor.getLong(4),
                    BudgetLine.BudgetLineEventType.getEnum(cursor.getInt(5))
                );

                // Adding item to list
                budgetLines.add(line);
            } while (cursor.moveToNext());
        }

        cursor.close();

        if (!externalDB)
            db.close();

        for (BudgetLine line : budgetLines) {
            if (line.getTitle() == null) throw new AssertionError(line.getDetails());
        }
        // return budgetLines list
        return budgetLines;
    }

    // Updating single line
    public int tblUpdateBudgetLine(BudgetItem item, BudgetLine line, SQLiteDatabase db) {
        boolean externalDB = db != null;
        if (! externalDB)
            db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(RE_KEY_TITLE, line.getTitle());
        values.put(RE_KEY_DETAILS, line.getDetails());
        values.put(RE_KEY_AMOUNT, line.getAmount());

        // updating row
        int retval = db.update(item.getName(), values, RE_KEY_ID + " = ?",
                new String[]{String.valueOf(line.getId())});


        // updating the item
        if (!line.isArchived())
            updateItemCurAmount(item, db); // curAmount only works with the nonArchived lines


        if (!externalDB)
            db.close();

        // Log.d(TAG, "adapters connected:");
        // for (MyAdapter adapter : s_adapters)
            // Log.d(TAG, adapter.toString());
        notifyAdapters();
        return retval;
    }

    // Deleting single line
    public void tblDeleteBudgetLine(BudgetItem item, BudgetLine line, SQLiteDatabase db) {
        boolean externalDB = db != null;
        if (! externalDB)
            db = this.getWritableDatabase();

        int retval = db.delete(item.getName(), RE_KEY_ID + " = ?",
                new String[]{String.valueOf(line.getId())});


        retval += 0;
        if (!line.isArchived())
            updateItemCurAmount(item, db); // curAmount only works with the nonArchived lines

        if (!externalDB)
            db.close();

        notifyAdapters();
    }


    // Getting lines count
    public int tblGetLinesCount(BudgetItem item, SQLiteDatabase db) {
        int retval = 0;

        boolean externalDB = db != null;
        if (!externalDB)
            db = this.getWritableDatabase();

        String countQuery = "SELECT  * FROM " + item.getName();
        Cursor cursor = db.rawQuery(countQuery, null);

        // return count
        if (cursor != null) {
            retval = cursor.getCount();
            cursor.close();
        }

        if (!externalDB)
            db.close();

        return retval;
    }

    public int tblGetCurAmount(BudgetItem item, SQLiteDatabase db) {
        int curAmount = new BudgetLinesParser(tblGetAllBudgetLines(item, db)).getNonArchivedAmount();
        // Log.d(TAG, "at item " + item.getName() + ", curAmount is " + curAmount);
        return curAmount;
    }

    public void insertAdapter(MyAdapter adapter) {
        s_adapters.add(adapter);
    }

    public void removeAdapter(MyAdapter adapter) {
        s_adapters.remove(adapter);
    }

    private void notifyAdapters() {
        for (MyAdapter a : s_adapters) {
            a.updateAdapter();
        }
    }

    public void updateFromOutside(BudgetItem item, Collection<BudgetLine> lines , SQLiteDatabase db) {
        String itemTable = item.getName();
        int lastValue = 0;
        for (BudgetLine line : lines) {
//            if (line.getTitle().toLowerCase().startsWith("budget")) {
////                tblDeleteTable(item, db);
////                tblCreateTable(item, db);
//            }
//            long milliseconds;
////            try {
////                Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(line.getDate());
////                milliseconds = date.getTime();
////            } catch (java.text.ParseException e) {
//                milliseconds = 0;
////            }
//            int eventType = 1; // userInput
//            if (line.getTitle().toLowerCase().startsWith("budget")
//                || line.getTitle().toLowerCase().startsWith("monthly")
//                || line.getTitle().toLowerCase().startsWith("weekly"))
//                eventType = 0; // autoUpdate

//
//
            ContentValues values = new ContentValues();
            values.put(RE_KEY_TITLE, line.getTitle());
            values.put(RE_KEY_DETAILS, line.getDetails());
            values.put(RE_KEY_AMOUNT, line.getAmount());
            values.put(RE_KEY_DATE, line.getDate());
            if (line.getTitle().toLowerCase().startsWith("budget")) {
                values.put(RE_KEY_EVENT_TYPE, BudgetLine.BudgetLineEventType.BUDGET_CREATED.toInt());
            } else {
                values.put(RE_KEY_EVENT_TYPE, line.getEventType().toInt());
            }
            // Inserting row to the relevant table
            db.insert(itemTable, null, values);
//
//            if (!line.getEventType().equals(BudgetLine.BudgetLineEventType.AUTO_UPDATE) && (
//                    line.getTitle().startsWith("weekly")||
//                            line.getTitle().startsWith("monthly")
//                    )) {
//                // create a "MOVE_FROM_LAST line right after
//                values = new ContentValues();
//                values.put(RE_KEY_TITLE, BudgetLine.BudgetLineEventType.MOVE_FROM_LAST_TIME.toString());
//                values.put(RE_KEY_DETAILS, context.getString(R.string.app_autoupdate_fromlast_details));
//                values.put(RE_KEY_AMOUNT, lastValue);
//                values.put(RE_KEY_DATE, line.getDate()+1);
//                values.put(RE_KEY_EVENT_TYPE, BudgetLine.BudgetLineEventType.MOVE_FROM_LAST_TIME.toInt());
//
//                // Inserting row to the relevant table
//                db.insert(itemTable, null, values);
//                lastValue = 0;
//            }
//            lastValue += line.getAmount();
//
//        }
//        String add_col = "ALTER TABLE " + tableName + " ADD COLUMN " + RE_KEY_EVENT_TYPE + " INTEGER";
//        db.execSQL(add_col);

//        String insert_values = "UPDATE " + tableName + " SET " + RE_KEY_EVENT_TYPE + " = " + RE_KEY_AMOUNT;
//        db.execSQL(insert_values);
        }

        updateItemCurAmount(item, db);
    }

    /**
     *
     * @param db needs to be openeed! and not null!
     * @return highest ID at TABLE_ALL_TABLES
     */
    private int _helper_getHighestID(@NonNull SQLiteDatabase db) {
        final String MY_QUERY = "SELECT MAX(" + MA_KEY_ID + ") FROM " + TABLE_ALL_TALBES;
        Cursor cur = db.rawQuery(MY_QUERY, null);
        cur.moveToFirst();
        int ID = cur.getInt(0);
        cur.close();
        return ID;
    }
}

