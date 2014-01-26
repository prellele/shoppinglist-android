package de.shoppinglist.android.helper;

import de.shoppinglist.android.R;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import de.shoppinglist.android.constant.ConfigurationConstants;
import de.shoppinglist.android.constant.DBConstants;

public class SQLiteHelper extends SQLiteOpenHelper {

	Resources resources;

	private static String DB_NAME = "shoppinglist.db";

	public SQLiteHelper(final Context context) {
		super(context, SQLiteHelper.DB_NAME, null, 1);
		resources = context.getResources();

	}

	@Override
	public void onCreate(final SQLiteDatabase db) {

		// Table: Store
		db.execSQL("CREATE TABLE " + DBConstants.TAB_STORE_NAME + " (" + DBConstants.COL_STORE_ID
				+ " INTEGER PRIMARY KEY NOT NULL," + DBConstants.COL_STORE_NAME
				+ " VARCHAR(75) NOT NULL);");

		// Table: Unit
		db.execSQL("CREATE TABLE " + DBConstants.TAB_UNIT_NAME + " (" + DBConstants.COL_UNIT_ID
				+ " INTEGER PRIMARY KEY NOT NULL, " + DBConstants.COL_UNIT_NAME
				+ " VARCHAR(50) NOT NULL);");

		// Table: Shoppinglist
		db.execSQL("CREATE TABLE " + DBConstants.TAB_SHOPPINGLIST_NAME + " ("
				+ DBConstants.COL_SHOPPINGLIST_ID + " INTEGER PRIMARY KEY NOT NULL, "
				+ DBConstants.COL_SHOPPINGLIST_CREATED_TIME
				+ " TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
				+ DBConstants.COL_SHOPPINGLIST_FINISHED_TIME + " TIMESTAMP DEFAULT null);");

		// Table: Favorite
		db.execSQL("CREATE TABLE " + DBConstants.TAB_FAVORITE_NAME + " ("
				+ DBConstants.COL_FAVORITE_ID + " INTEGER PRIMARY KEY NOT NULL, "
				+ DBConstants.COL_FAVORITE_NAME + " VARCHAR(75) NOT NULL);");

		// Table: Product
		db.execSQL("CREATE TABLE " + DBConstants.TAB_PRODUCT_NAME + " ("
				+ DBConstants.COL_PRODUCT_ID + " INTEGER PRIMARY KEY NOT NULL, "
				+ DBConstants.COL_PRODUCT_NAME + " VARCHAR(100) NOT NULL, "
				+ DBConstants.COL_PRODUCT_UNIT_ID + " INTEGER NOT NULL, FOREIGN KEY ("
				+ DBConstants.COL_PRODUCT_UNIT_ID + ") REFERENCES " + DBConstants.TAB_UNIT_NAME
				+ "(" + DBConstants.COL_UNIT_ID + "));");

		// Table: Shoppinglist_product_mapping
		db.execSQL("CREATE TABLE " + DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME + " ("
				+ DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_ID
				+ " INTEGER PRIMARY KEY NOT NULL, "
				+ DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_SHOPPINGLIST_ID
				+ " INTEGER NOT NULL, " + DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_STORE_ID
				+ " INTEGER NOT NULL, " + DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_PRODUCT_ID
				+ " INTEGER NOT NULL, " + DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_QUANTITY
				+ " FLOAT NOT NULL, " + DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_CHECKED
				+ " SMALLINT DEFAULT 0, FOREIGN KEY ("
				+ DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_SHOPPINGLIST_ID + ") REFERENCES "
				+ DBConstants.TAB_SHOPPINGLIST_NAME + "(" + DBConstants.COL_SHOPPINGLIST_ID
				+ "), FOREIGN KEY (" + DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_STORE_ID
				+ ") REFERENCES " + DBConstants.TAB_STORE_NAME + "(" + DBConstants.COL_STORE_ID
				+ "), FOREIGN KEY (" + DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_STORE_ID
				+ ") REFERENCES " + DBConstants.TAB_PRODUCT_NAME + "(" + DBConstants.COL_PRODUCT_ID
				+ "));");

		// Table: Favorite_product_mapping
		db.execSQL("CREATE TABLE " + DBConstants.TAB_FAVORITE_PRODUCT_MAPPING_NAME + " ("
				+ DBConstants.COL_FAVORITE_PRODUCT_MAPPING_ID + " INTEGER PRIMARY KEY NOT NULL, "
				+ DBConstants.COL_FAVORITE_PRODUCT_MAPPING_FAVORITE_ID + " INTEGER NOT NULL, "
				+ DBConstants.COL_FAVORITE_PRODUCT_MAPPING_STORE_ID + " INTEGER NOT NULL, "
				+ DBConstants.COL_FAVORITE_PRODUCT_MAPPING_PRODUCT_ID + " INTEGER NOT NULL, "
				+ DBConstants.COL_FAVORITE_PRODUCT_MAPPING_QUANTITY
				+ " FLOAT NOT NULL, FOREIGN KEY ("
				+ DBConstants.COL_FAVORITE_PRODUCT_MAPPING_FAVORITE_ID + ") REFERENCES "
				+ DBConstants.TAB_FAVORITE_NAME + "(" + DBConstants.COL_FAVORITE_ID
				+ "), FOREIGN KEY (" + DBConstants.COL_FAVORITE_PRODUCT_MAPPING_STORE_ID
				+ ") REFERENCES " + DBConstants.TAB_STORE_NAME + "(" + DBConstants.COL_STORE_ID
				+ "), FOREIGN KEY (" + DBConstants.COL_FAVORITE_PRODUCT_MAPPING_PRODUCT_ID
				+ ") REFERENCES " + DBConstants.TAB_PRODUCT_NAME + "(" + DBConstants.COL_PRODUCT_ID
				+ "));");

		// Table: History
		db.execSQL("CREATE TABLE " + DBConstants.TAB_HISTORY_NAME + " ("
				+ DBConstants.COL_HISTORY_ID + " INTEGER PRIMARY KEY NOT NULL, "
				+ DBConstants.COL_HISTORY_SHOPPINGLIST_ID + " INTEGER NOT NULL, "
				+ DBConstants.COL_HISTORY_STORE + " VARCHAR(75) NOT NULL, "
				+ DBConstants.COL_HISTORY_PRODUCT + " VARCHAR(100) NOT NULL, "
				+ DBConstants.COL_HISTORY_UNIT + " VARCHAR(50) NOT NULL, "
				+ DBConstants.COL_HISTORY_QUANTITY + " FLOAT NOT NULL, FOREIGN KEY ("
				+ DBConstants.COL_HISTORY_SHOPPINGLIST_ID + ") REFERENCES "
				+ DBConstants.TAB_SHOPPINGLIST_NAME + "(" + DBConstants.COL_SHOPPINGLIST_ID + "));");

		// Table: User_Configuration
		db.execSQL("CREATE TABLE " + DBConstants.TAB_USER_CONFIGURATION + " ("
				+ DBConstants.COL_USER_CONFIGURATION_ID + " INTEGER PRIMARY KEY NOT NULL, "
				+ DBConstants.COL_USER_CONFIGURATION_VIEW_TYPE + " SMALLINT DEFAULT 0 NOT NULL);");

		// Pre Inserted Values

		// UserConfiguration (Standards)
		db.execSQL("INSERT INTO " + DBConstants.TAB_USER_CONFIGURATION + " ("
				+ DBConstants.COL_USER_CONFIGURATION_VIEW_TYPE + ") VALUES ("
				+ ConfigurationConstants.STORE_VIEW + ");");

		// Units
		// Liter
		db.execSQL("INSERT INTO " + DBConstants.TAB_UNIT_NAME + " (" + DBConstants.COL_UNIT_NAME
				+ ") VALUES ('" + resources.getString(R.string.sqlstatement_liter) + "');");
		// Gramm
		db.execSQL("INSERT INTO " + DBConstants.TAB_UNIT_NAME + " (" + DBConstants.COL_UNIT_NAME
				+ ") VALUES ('" + resources.getString(R.string.sqlstatement_gram) + "');");
		// Kilo
		db.execSQL("INSERT INTO " + DBConstants.TAB_UNIT_NAME + " (" + DBConstants.COL_UNIT_NAME
				+ ") VALUES ('" + resources.getString(R.string.sqlstatement_kilogram) + "');");
		// Paket(e)
		db.execSQL("INSERT INTO " + DBConstants.TAB_UNIT_NAME + " (" + DBConstants.COL_UNIT_NAME
				+ ") VALUES ('" + resources.getString(R.string.sqlstatement_packets) + "');");
		// Stores
		// keine Angabe - Standard Markt
		db.execSQL("INSERT INTO " + DBConstants.TAB_STORE_NAME + " (" + DBConstants.COL_STORE_NAME
				+ ") VALUES ('" + resources.getString(R.string.sqlstatement_not_specified) + "');");

		// Shoppinglist
		// need to have an initial shoppinglist
		db.execSQL("INSERT INTO " + DBConstants.TAB_SHOPPINGLIST_NAME + " ("
				+ DBConstants.COL_SHOPPINGLIST_CREATED_TIME + ") VALUES (CURRENT_TIMESTAMP);");
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {

	}
}
