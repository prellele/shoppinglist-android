package de.shoppinglist.android.datasource;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import de.shoppinglist.android.bean.Favorite;
import de.shoppinglist.android.bean.FavoriteProductMapping;
import de.shoppinglist.android.bean.History;
import de.shoppinglist.android.bean.Product;
import de.shoppinglist.android.bean.Shoppinglist;
import de.shoppinglist.android.bean.ShoppinglistProductMapping;
import de.shoppinglist.android.bean.Store;
import de.shoppinglist.android.bean.Unit;
import de.shoppinglist.android.constant.ConfigurationConstants;
import de.shoppinglist.android.constant.DBConstants;
import de.shoppinglist.android.constant.GlobalValues;
import de.shoppinglist.android.helper.SQLiteHelper;
import de.shoppinglist.android.helper.TranslateUmlauts;

public class ShoppinglistDataSource {

	private SQLiteDatabase database;

	private final SQLiteHelper dbHelper;

	/**
	 * Constructor
	 * 
	 * @param context
	 */
	public ShoppinglistDataSource(final Context context) {
		this.dbHelper = new SQLiteHelper(context);
	}

	/**
	 * adds the current shoppinglist with all its relations to the table.history
	 */
	public void addAllToHistory() {
		this.isDbLockedByThread();

		final String replaceUmlautsHistoryPart1 = "replace(replace(replace(replace(replace(replace(replace(";
		final String replaceUmlautsHistoryPart2 = ",'&auml;','ä'),'&Auml;','Ä'),'&ouml;','ö'),'&Ouml;','Ö'),'&uuml;','ü'),'&Uuml;','Ü'),'&szlig;','ß')";

		final String sqlInsertHistory = "INSERT INTO " + DBConstants.TAB_HISTORY_NAME + " ("
				+ DBConstants.COL_HISTORY_SHOPPINGLIST_ID + ", " + DBConstants.COL_HISTORY_STORE
				+ ", " + DBConstants.COL_HISTORY_PRODUCT + ", " + DBConstants.COL_HISTORY_UNIT
				+ ", " + DBConstants.COL_HISTORY_QUANTITY + ") SELECT "
				+ DBConstants.COL_SHOPPINGLIST_ID + ", " + replaceUmlautsHistoryPart1
				+ DBConstants.COL_STORE_NAME + replaceUmlautsHistoryPart2 + ", "
				+ replaceUmlautsHistoryPart1 + DBConstants.COL_PRODUCT_NAME
				+ replaceUmlautsHistoryPart2 + ", " + replaceUmlautsHistoryPart1
				+ DBConstants.COL_UNIT_NAME + replaceUmlautsHistoryPart2 + ", "
				+ DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_QUANTITY + " FROM "
				+ DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME + " INNER JOIN "
				+ DBConstants.TAB_SHOPPINGLIST_NAME + " on "
				+ DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME + "."
				+ DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_SHOPPINGLIST_ID + " = "
				+ DBConstants.TAB_SHOPPINGLIST_NAME + "." + DBConstants.COL_SHOPPINGLIST_ID
				+ " INNER JOIN " + DBConstants.TAB_STORE_NAME + " on "
				+ DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME + "."
				+ DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_STORE_ID + " = "
				+ DBConstants.TAB_STORE_NAME + "." + DBConstants.COL_STORE_ID + " INNER JOIN "
				+ DBConstants.TAB_PRODUCT_NAME + " on "
				+ DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME + "."
				+ DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_PRODUCT_ID + " = "
				+ DBConstants.TAB_PRODUCT_NAME + "." + DBConstants.COL_PRODUCT_ID + " INNER JOIN "
				+ DBConstants.TAB_UNIT_NAME + " on " + DBConstants.TAB_PRODUCT_NAME + "."
				+ DBConstants.COL_PRODUCT_UNIT_ID + " = " + DBConstants.TAB_UNIT_NAME + "."
				+ DBConstants.COL_UNIT_ID;

		this.database.execSQL(sqlInsertHistory);

		this.deleteAllShoppinglistProductMappings();
	}

	/**
	 * checks whether there is a favoriteProductMapping (with combination of
	 * given storeId and productId) in this favorite (given favoriteId)
	 * 
	 * @param favoriteId
	 * @param storeId
	 * @param productId
	 * @return favoriteProductMapping returns null when there exists no mapping
	 */
	public FavoriteProductMapping checkWhetherFavoriteProductMappingExists(final int favoriteId,
			final int storeId, final int productId) {
		this.isDbLockedByThread();

		final String sqlQuery = "SELECT " + DBConstants.COL_FAVORITE_PRODUCT_MAPPING_ID + ", "
				+ DBConstants.COL_FAVORITE_PRODUCT_MAPPING_FAVORITE_ID + ", "
				+ DBConstants.COL_FAVORITE_PRODUCT_MAPPING_STORE_ID + ", "
				+ DBConstants.COL_FAVORITE_PRODUCT_MAPPING_PRODUCT_ID + ", "
				+ DBConstants.COL_FAVORITE_PRODUCT_MAPPING_QUANTITY + " FROM "
				+ DBConstants.TAB_FAVORITE_PRODUCT_MAPPING_NAME + " WHERE "
				+ DBConstants.COL_FAVORITE_PRODUCT_MAPPING_STORE_ID + " = " + storeId + " AND "
				+ DBConstants.COL_FAVORITE_PRODUCT_MAPPING_PRODUCT_ID + " = " + productId + " AND "
				+ DBConstants.COL_FAVORITE_PRODUCT_MAPPING_FAVORITE_ID + " = " + favoriteId;

		final Cursor cursor = this.database.rawQuery(sqlQuery, null);

		FavoriteProductMapping favoriteProductMapping = null;

		while ((cursor.getCount() != 0) && cursor.moveToNext()) {
			favoriteProductMapping = new FavoriteProductMapping();

			final Store store = new Store();
			store.setId(cursor.getInt(cursor
					.getColumnIndex(DBConstants.COL_FAVORITE_PRODUCT_MAPPING_STORE_ID)));
			favoriteProductMapping.setStore(store);

			final Product product = new Product();
			product.setId(cursor.getInt(cursor
					.getColumnIndex(DBConstants.COL_FAVORITE_PRODUCT_MAPPING_PRODUCT_ID)));
			favoriteProductMapping.setProduct(product);

			final Favorite favorite = new Favorite();
			favorite.setId(cursor.getInt(cursor
					.getColumnIndex(DBConstants.COL_FAVORITE_PRODUCT_MAPPING_FAVORITE_ID)));
			favoriteProductMapping.setFavorite(favorite);

			favoriteProductMapping.setQuantity(cursor.getString(cursor
					.getColumnIndex(DBConstants.COL_FAVORITE_PRODUCT_MAPPING_QUANTITY)));
			favoriteProductMapping.setId(cursor.getInt(cursor
					.getColumnIndex(DBConstants.COL_FAVORITE_PRODUCT_MAPPING_ID)));

		}
		cursor.close();
		return favoriteProductMapping;
	}

	/**
	 * checks whether the product is in use in the shoppinglist_product_mapping
	 * or favorite_product_mapping
	 * 
	 * @param productId
	 * @return true when the product is NOT in use and could be deleted, false
	 *         when it is in use
	 */
	public boolean checkWhetherProductIsNotInUse(final int productId) {
		this.isDbLockedByThread();

		boolean isNotInShoppingListProductMappingInUse = true;
		final String sqlShoppingListProductMapping = "SELECT "
				+ DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_PRODUCT_ID + " FROM "
				+ DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME + " WHERE "
				+ DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME + "."
				+ DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_PRODUCT_ID + " = " + productId;

		final Cursor shoppingListProductMappingCursor = this.database.rawQuery(
				sqlShoppingListProductMapping, null);
		if (shoppingListProductMappingCursor.getCount() != 0) {
			isNotInShoppingListProductMappingInUse = false;
		}
		shoppingListProductMappingCursor.close();

		boolean isNotInFavoriteProductMappingInUse = true;
		final String sqlFavoriteProductMapping = "SELECT "
				+ DBConstants.COL_FAVORITE_PRODUCT_MAPPING_PRODUCT_ID + " FROM "
				+ DBConstants.TAB_FAVORITE_PRODUCT_MAPPING_NAME + " WHERE "
				+ DBConstants.TAB_FAVORITE_PRODUCT_MAPPING_NAME + "."
				+ DBConstants.COL_FAVORITE_PRODUCT_MAPPING_PRODUCT_ID + " = " + productId;

		final Cursor favoriteProductMappingCursor = this.database.rawQuery(
				sqlFavoriteProductMapping, null);
		if (favoriteProductMappingCursor.getCount() != 0) {
			isNotInFavoriteProductMappingInUse = false;
		}
		favoriteProductMappingCursor.close();

		return (isNotInShoppingListProductMappingInUse && isNotInFavoriteProductMappingInUse);
	}

	/**
	 * checks whether there is a shoppinglistProductMapping (with combination of
	 * given storeId and productId)
	 * 
	 * @param storeId
	 * @param productId
	 * @return ShoppinglistProductMapping shoppinglistProductMapping returns
	 *         null when there exists no mapping
	 */
	public ShoppinglistProductMapping checkWhetherShoppinglistProductMappingExists(
			final int storeId, final int productId) {
		this.isDbLockedByThread();

		final String sqlQuery = "SELECT * FROM "
				+ DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME + " WHERE "
				+ DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_STORE_ID + " = " + storeId + " AND "
				+ DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_PRODUCT_ID + " = " + productId;

		final Cursor cursor = this.database.rawQuery(sqlQuery, null);

		ShoppinglistProductMapping shoppinglistProductMapping = null;

		while ((cursor.getCount() != 0) && cursor.moveToNext()) {
			shoppinglistProductMapping = new ShoppinglistProductMapping();

			final Store store = new Store();
			store.setId(cursor.getInt(cursor
					.getColumnIndex(DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_STORE_ID)));
			shoppinglistProductMapping.setStore(store);

			final Product product = new Product();
			product.setId(cursor.getInt(cursor
					.getColumnIndex(DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_PRODUCT_ID)));
			shoppinglistProductMapping.setProduct(product);

			final Shoppinglist shoppinglist = new Shoppinglist();
			shoppinglist.setId(cursor.getInt(cursor
					.getColumnIndex(DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_SHOPPINGLIST_ID)));
			shoppinglistProductMapping.setShoppinglist(shoppinglist);

			shoppinglistProductMapping.setQuantity(cursor.getString(cursor
					.getColumnIndex(DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_QUANTITY)));
			shoppinglistProductMapping.setId(cursor.getInt(cursor
					.getColumnIndex(DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_ID)));

		}
		cursor.close();

		return shoppinglistProductMapping;
	}

	/**
	 * checks whether the store is in use in the shoppinglist_product_mapping or
	 * favorite_product_mapping
	 * 
	 * @param storeId
	 * @return true when the store is NOT in use and could be deleted, false
	 *         when it is in use
	 */
	public boolean checkWhetherStoreIsNotInUse(final int storeId) {
		this.isDbLockedByThread();

		boolean isNotInShoppingListProductMappingInUse = true;
		final String sqlShoppingListProductMapping = "SELECT "
				+ DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_STORE_ID + " FROM "
				+ DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME + " WHERE "
				+ DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME + "."
				+ DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_STORE_ID + " = " + storeId;

		final Cursor shoppingListProductMappingCursor = this.database.rawQuery(
				sqlShoppingListProductMapping, null);
		if (shoppingListProductMappingCursor.getCount() != 0) {
			isNotInShoppingListProductMappingInUse = false;
		}
		shoppingListProductMappingCursor.close();

		boolean isNotInFavoriteProductMappingInUse = true;
		final String sqlFavoriteProductMapping = "SELECT "
				+ DBConstants.COL_FAVORITE_PRODUCT_MAPPING_STORE_ID + " FROM "
				+ DBConstants.TAB_FAVORITE_PRODUCT_MAPPING_NAME + " WHERE "
				+ DBConstants.TAB_FAVORITE_PRODUCT_MAPPING_NAME + "."
				+ DBConstants.COL_FAVORITE_PRODUCT_MAPPING_STORE_ID + " = " + storeId;

		final Cursor favoriteProductMappingCursor = this.database.rawQuery(
				sqlFavoriteProductMapping, null);
		if (favoriteProductMappingCursor.getCount() != 0) {
			isNotInFavoriteProductMappingInUse = false;
		}
		favoriteProductMappingCursor.close();

		return (isNotInShoppingListProductMappingInUse && isNotInFavoriteProductMappingInUse);
	}

	/**
	 * 
	 * checks whether the unit is in use in the table product
	 * 
	 * @param unitId
	 * @return true when the unit is NOT in use and could be deleted, false when
	 *         it is in use
	 */
	public boolean checkWhetherUnitIsNotInUse(final int unitId) {
		this.isDbLockedByThread();

		boolean isNotInProductInUse = true;
		final String sqlQuery = "SELECT " + DBConstants.COL_PRODUCT_UNIT_ID + " FROM "
				+ DBConstants.TAB_PRODUCT_NAME + " WHERE " + DBConstants.TAB_PRODUCT_NAME + "."
				+ DBConstants.COL_PRODUCT_UNIT_ID + " = " + unitId;

		final Cursor cursor = this.database.rawQuery(sqlQuery, null);
		if (cursor.getCount() != 0) {
			isNotInProductInUse = false;
		}
		cursor.close();

		return isNotInProductInUse;
	}

	/**
	 * closes the db-connection
	 */
	public void close() {
		// this.isDbLockedByThread();

		if ((this.database != null) && !this.database.isDbLockedByOtherThreads()
				&& this.database.isOpen()) {
			if (this.dbHelper != null) {
				this.dbHelper.close();
			}

		}
	}

	/**
	 * creates a new shoppinglist (Table: shoppinglist)
	 */
	public void createNewShoppinglist() {
		this.isDbLockedByThread();

		// at first set the old shoppinglist to finished (current_timestamp)
		final String sqlMarkShoppinglistFinished = "UPDATE " + DBConstants.TAB_SHOPPINGLIST_NAME
				+ " SET " + DBConstants.COL_SHOPPINGLIST_FINISHED_TIME
				+ " = CURRENT_TIMESTAMP WHERE " + DBConstants.COL_SHOPPINGLIST_ID
				+ " = (SELECT MAX(" + DBConstants.COL_SHOPPINGLIST_ID + ") AS "
				+ DBConstants.COL_SHOPPINGLIST_ID + " FROM " + DBConstants.TAB_SHOPPINGLIST_NAME
				+ ")";

		this.database.execSQL(sqlMarkShoppinglistFinished);

		// then insert a new one
		final String sqlInsertNew = "INSERT INTO " + DBConstants.TAB_SHOPPINGLIST_NAME + " ("
				+ DBConstants.COL_SHOPPINGLIST_CREATED_TIME + ") VALUES (CURRENT_TIMESTAMP)";

		this.database.execSQL(sqlInsertNew);
	}

	/**
	 * deletes all mappings from shoppinglistProductMapping
	 * 
	 */
	public void deleteAllShoppinglistProductMappings() {
		this.isDbLockedByThread();

		// temporary save the product id to delete, for the check, whether it
		// could be deleted in table: product
		final String sqlNoteProductsIdForFurtherCheck = "SELECT DISTINCT "
				+ DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_PRODUCT_ID + " FROM "
				+ DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME;

		final Cursor productIdCursor = this.database.rawQuery(sqlNoteProductsIdForFurtherCheck,
				null);

		// delete the mapping entries for this id
		final String sqlDeleteFromShoppinglistProductMapping = "DELETE FROM "
				+ DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME;

		this.database.execSQL(sqlDeleteFromShoppinglistProductMapping);

		// delete the products which could be deleted
		while (productIdCursor.moveToNext()) {
			final int productId = productIdCursor.getInt(productIdCursor
					.getColumnIndex(DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_PRODUCT_ID));
			if (this.checkWhetherProductIsNotInUse(productId)) {
				this.deleteProduct(productId);
			}
		}
		productIdCursor.close();

	}

	/**
	 * deletes a favorite and all the mapping belong to this favorite with given
	 * favoriteId
	 * 
	 * @param favoriteId
	 */
	public void deleteFavoriteAndItsMappings(final int favoriteId) {
		this.isDbLockedByThread();

		// temporary save the product id to delete, for the check, whether it
		// could be deleted in table: product
		final String sqlNoteProductsIdForFurtherCheck = "SELECT "
				+ DBConstants.COL_FAVORITE_PRODUCT_MAPPING_PRODUCT_ID + " FROM "
				+ DBConstants.TAB_FAVORITE_PRODUCT_MAPPING_NAME + " WHERE "
				+ DBConstants.TAB_FAVORITE_PRODUCT_MAPPING_NAME + "."
				+ DBConstants.COL_FAVORITE_PRODUCT_MAPPING_FAVORITE_ID + " = " + favoriteId;

		final Cursor productIdCursor = this.database.rawQuery(sqlNoteProductsIdForFurtherCheck,
				null);

		final String sqlDeleteMappings = "DELETE FROM "
				+ DBConstants.TAB_FAVORITE_PRODUCT_MAPPING_NAME + " WHERE "
				+ DBConstants.TAB_FAVORITE_PRODUCT_MAPPING_NAME + "."
				+ DBConstants.COL_FAVORITE_PRODUCT_MAPPING_FAVORITE_ID + " = " + favoriteId;

		this.database.execSQL(sqlDeleteMappings);

		final String sqlDeleteFavorite = "DELETE FROM " + DBConstants.TAB_FAVORITE_NAME + " WHERE "
				+ DBConstants.TAB_FAVORITE_NAME + "." + DBConstants.COL_FAVORITE_ID + " = "
				+ favoriteId;

		this.database.execSQL(sqlDeleteFavorite);

		// delete the products which could be deleted
		while (productIdCursor.moveToNext()) {
			final int productId = productIdCursor.getInt(productIdCursor
					.getColumnIndex(DBConstants.COL_FAVORITE_PRODUCT_MAPPING_PRODUCT_ID));
			if (this.checkWhetherProductIsNotInUse(productId)) {
				this.deleteProduct(productId);
			}
		}
		productIdCursor.close();

	}

	/**
	 * deletes a favoriteProductMapping with given favoriteProductMappingId
	 * 
	 * @param favoriteProductMappingId
	 */
	public void deleteFavoriteProductMapping(final int favoriteProductMappingId) {
		this.isDbLockedByThread();

		// temporary save the product id to delete, for the check, whether it
		// could be deleted in table: product
		final String sqlNoteProductsIdForFurtherCheck = "SELECT "
				+ DBConstants.COL_FAVORITE_PRODUCT_MAPPING_PRODUCT_ID + " FROM "
				+ DBConstants.TAB_FAVORITE_PRODUCT_MAPPING_NAME + " WHERE "
				+ DBConstants.TAB_FAVORITE_PRODUCT_MAPPING_NAME + "."
				+ DBConstants.COL_FAVORITE_PRODUCT_MAPPING_ID + " = " + favoriteProductMappingId;

		final Cursor productIdCursor = this.database.rawQuery(sqlNoteProductsIdForFurtherCheck,
				null);

		final String sqlDeleteMappings = "DELETE FROM "
				+ DBConstants.TAB_FAVORITE_PRODUCT_MAPPING_NAME + " WHERE "
				+ DBConstants.TAB_FAVORITE_PRODUCT_MAPPING_NAME + "."
				+ DBConstants.COL_FAVORITE_PRODUCT_MAPPING_ID + " = " + favoriteProductMappingId;

		this.database.execSQL(sqlDeleteMappings);

		// delete the products which could be deleted
		while (productIdCursor.moveToNext()) {
			final int productId = productIdCursor.getInt(productIdCursor
					.getColumnIndex(DBConstants.COL_FAVORITE_PRODUCT_MAPPING_PRODUCT_ID));
			if (this.checkWhetherProductIsNotInUse(productId)) {
				this.deleteProduct(productId);
			}
		}
		productIdCursor.close();

	}

	/**
	 * deletes the whole history
	 * 
	 */
	public void deleteHistory() {
		this.isDbLockedByThread();

		final String sqlDeleteHistory = "DELETE FROM " + DBConstants.TAB_HISTORY_NAME;

		this.database.execSQL(sqlDeleteHistory);
	}

	/**
	 * deletes a product with given productId
	 * 
	 * @param productId
	 */
	public void deleteProduct(final int productId) {
		this.isDbLockedByThread();

		final String sqlDeleteProduct = "DELETE FROM " + DBConstants.TAB_PRODUCT_NAME + " WHERE "
				+ DBConstants.TAB_PRODUCT_NAME + "." + DBConstants.COL_PRODUCT_ID + " = "
				+ productId;

		this.database.execSQL(sqlDeleteProduct);
	}

	/**
	 * deletes the products from the given store
	 * 
	 * @param storeId
	 */
	public void deleteProductsFromStoreList(final int storeId) {
		this.isDbLockedByThread();

		// temporary save the product ids to delete, for the check, whether they
		// could be deleted in table: product
		final String sqlNoteProductsIdForFurtherCheck = "SELECT "
				+ DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_PRODUCT_ID + " FROM "
				+ DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME + " WHERE "
				+ DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME + "."
				+ DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_STORE_ID + " = " + storeId;

		final Cursor productIdsCursor = this.database.rawQuery(sqlNoteProductsIdForFurtherCheck,
				null);

		// delete the mapping entries for this store
		final String sqlDeleteFromShoppinglistProductMapping = "DELETE FROM "
				+ DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME + " WHERE "
				+ DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME + "."
				+ DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_STORE_ID + " = " + storeId;

		this.database.execSQL(sqlDeleteFromShoppinglistProductMapping);

		// delete the products which could be deleted
		while (productIdsCursor.moveToNext()) {
			final int productId = productIdsCursor.getInt(productIdsCursor
					.getColumnIndex(DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_PRODUCT_ID));
			if (this.checkWhetherProductIsNotInUse(productId)) {
				this.deleteProduct(productId);
			}
		}
		productIdsCursor.close();

	}

	/**
	 * deletes the shoppinglistProductMapping with given id
	 * 
	 * @param shoppinglistProductMappingId
	 */
	public void deleteShoppinglistProductMapping(final int shoppinglistProductMappingId) {
		this.isDbLockedByThread();

		// temporary save the product id to delete, for the check, whether it
		// could be deleted in table: product
		final String sqlNoteProductsIdForFurtherCheck = "SELECT "
				+ DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_PRODUCT_ID + " FROM "
				+ DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME + " WHERE "
				+ DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME + "."
				+ DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_ID + " = "
				+ shoppinglistProductMappingId;

		final Cursor productIdCursor = this.database.rawQuery(sqlNoteProductsIdForFurtherCheck,
				null);

		// delete the mapping entries for this id
		final String sqlDeleteFromShoppinglistProductMapping = "DELETE FROM "
				+ DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME + " WHERE "
				+ DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME + "."
				+ DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_ID + " = "
				+ shoppinglistProductMappingId;

		this.database.execSQL(sqlDeleteFromShoppinglistProductMapping);

		// delete the products which could be deleted
		while (productIdCursor.moveToNext()) {
			final int productId = productIdCursor.getInt(productIdCursor
					.getColumnIndex(DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_PRODUCT_ID));
			if (this.checkWhetherProductIsNotInUse(productId)) {
				this.deleteProduct(productId);
			}
		}
		productIdCursor.close();

	}

	/**
	 * deletes a store with given storeId
	 * 
	 * @param storeId
	 */
	public void deleteStore(final int storeId) {
		this.isDbLockedByThread();

		final String sqlQuery = "DELETE FROM " + DBConstants.TAB_STORE_NAME + " WHERE "
				+ DBConstants.COL_STORE_ID + " = " + storeId;

		this.database.execSQL(sqlQuery);
	}

	/**
	 * deletes an unit with given unitId
	 * 
	 * @param unitId
	 */
	public void deleteUnit(final int unitId) {
		this.isDbLockedByThread();

		final String sqlQuery = "DELETE FROM " + DBConstants.TAB_UNIT_NAME + " WHERE "
				+ DBConstants.COL_UNIT_ID + " = " + unitId;

		this.database.execSQL(sqlQuery);
	}

	/**
	 * gets all the favorites (table: favorite)
	 * 
	 * @return List<Favorite> favorites
	 */
	public List<Favorite> getAllFavorites() {
		this.isDbLockedByThread();

		final String sqlQuery = "SELECT " + DBConstants.COL_FAVORITE_ID + ", "
				+ DBConstants.COL_FAVORITE_NAME + " FROM " + DBConstants.TAB_FAVORITE_NAME
				+ " ORDER BY " + DBConstants.COL_FAVORITE_NAME;

		final Cursor cursor = this.database.rawQuery(sqlQuery, null);

		final List<Favorite> favorites = new LinkedList<Favorite>();
		while (cursor.moveToNext()) {
			final Favorite favorite = new Favorite();

			favorite.setId(cursor.getInt(cursor.getColumnIndex(DBConstants.COL_FAVORITE_ID)));
			favorite.setName(TranslateUmlauts.translateIntoGermanUmlauts(cursor.getString(cursor
					.getColumnIndex(DBConstants.COL_FAVORITE_NAME))));

			favorites.add(favorite);
		}
		cursor.close();

		return favorites;
	}

	/**
	 * gets all stores from the DB (Table: Store)
	 * 
	 * @return List<Store> stores
	 */
	public List<Store> getAllStores() {
		this.isDbLockedByThread();

		final String sqlQuery = "SELECT " + DBConstants.COL_STORE_ID + ", "
				+ DBConstants.COL_STORE_NAME + " FROM " + DBConstants.TAB_STORE_NAME + " ORDER BY "
				+ DBConstants.COL_STORE_NAME;

		final Cursor cursor = this.database.rawQuery(sqlQuery, null);

		final List<Store> stores = new LinkedList<Store>();

		while (cursor.moveToNext()) {
			final Store store = new Store();
			store.setId(cursor.getInt(cursor.getColumnIndex(DBConstants.COL_STORE_ID)));
			store.setName(TranslateUmlauts.translateIntoGermanUmlauts(cursor.getString(cursor
					.getColumnIndex(DBConstants.COL_STORE_NAME))));
			store.setCountProducts(this.getProductCountForStore(store.getId()));
			store.setAlreadyCheckedProducts(this.getCheckedProductCountForStore(store.getId()));
			stores.add(store);
		}
		cursor.close();

		return stores;
	}

	/**
	 * gets all units from DB (Table: Unit)
	 * 
	 * @return List<Unit> units
	 */
	public List<Unit> getAllUnits() {
		this.isDbLockedByThread();

		final String sqlQuery = "SELECT " + DBConstants.COL_UNIT_ID + ", "
				+ DBConstants.COL_UNIT_NAME + " FROM " + DBConstants.TAB_UNIT_NAME + " ORDER BY "
				+ DBConstants.COL_UNIT_NAME;

		final Cursor cursor = this.database.rawQuery(sqlQuery, null);

		final List<Unit> units = new LinkedList<Unit>();

		while (cursor.moveToNext()) {
			final Unit unit = new Unit();
			unit.setId(cursor.getInt(cursor.getColumnIndex(DBConstants.COL_UNIT_ID)));
			unit.setName(TranslateUmlauts.translateIntoGermanUmlauts(cursor.getString(cursor
					.getColumnIndex(DBConstants.COL_UNIT_NAME))));
			units.add(unit);
		}
		cursor.close();

		return units;
	}

	/**
	 * 
	 * gets the product count (checked) for this storeId
	 * 
	 * @param storeId
	 * @return int productCount
	 */
	public int getCheckedProductCountForStore(final int storeId) {
		this.isDbLockedByThread();

		final String sqlQuery = "SELECT COUNT("
				+ DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_PRODUCT_ID + ") AS "
				+ DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_PRODUCT_ID + " FROM "
				+ DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME + " WHERE "
				+ DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME + "."
				+ DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_STORE_ID + " = " + storeId + " AND "
				+ DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME + "."
				+ DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_CHECKED + " = " + GlobalValues.YES;

		final Cursor checkedProductCountCursor = this.database.rawQuery(sqlQuery, null);

		int checkedProductCount = 0;
		while (checkedProductCountCursor.moveToNext()) {
			checkedProductCount = checkedProductCount
					+ checkedProductCountCursor
							.getInt(checkedProductCountCursor
									.getColumnIndex(DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_PRODUCT_ID));
		}
		checkedProductCountCursor.close();

		return checkedProductCount;
	}

	/**
	 * gets a favorite with given favoriteName
	 * 
	 * @param favoriteName
	 * @return Favorite favorite
	 */
	public Favorite getFavoriteByName(String favoriteName) {
		this.isDbLockedByThread();
		favoriteName = TranslateUmlauts.translateFromGermanUmlauts(favoriteName);

		final String sqlQuery = "SELECT " + DBConstants.COL_FAVORITE_ID + ", "
				+ DBConstants.COL_FAVORITE_NAME + " FROM " + DBConstants.TAB_FAVORITE_NAME
				+ " WHERE UPPER(" + DBConstants.TAB_FAVORITE_NAME + "."
				+ DBConstants.COL_FAVORITE_NAME + ") = '" + favoriteName.toUpperCase().trim() + "'";

		final Cursor cursor = this.database.rawQuery(sqlQuery, null);

		Favorite favorite = null;

		if (cursor.getCount() == 1) {
			cursor.moveToNext();
			favorite = new Favorite();
			favorite.setId(cursor.getInt(cursor.getColumnIndex(DBConstants.COL_FAVORITE_ID)));
			favorite.setName(TranslateUmlauts.translateIntoGermanUmlauts(cursor.getString(cursor
					.getColumnIndex(DBConstants.COL_FAVORITE_NAME))));
		}
		cursor.close();

		return favorite;
	}

	/**
	 * gets the mappings for given favoriteId
	 * 
	 * @param favoriteId
	 * @return List<FavoriteProductMapping> favoriteProductMappings (should be
	 *         only one entry)
	 */
	public List<FavoriteProductMapping> getFavoriteProductMappingsByFavoriteId(final int favoriteId) {
		this.isDbLockedByThread();

		String sqlQuery = "SELECT * FROM " + DBConstants.TAB_FAVORITE_PRODUCT_MAPPING_NAME
				+ " INNER JOIN " + DBConstants.TAB_FAVORITE_NAME + " on "
				+ DBConstants.TAB_FAVORITE_PRODUCT_MAPPING_NAME + "."
				+ DBConstants.COL_FAVORITE_PRODUCT_MAPPING_FAVORITE_ID + " = "
				+ DBConstants.TAB_FAVORITE_NAME + "." + DBConstants.COL_FAVORITE_ID
				+ " INNER JOIN " + DBConstants.TAB_STORE_NAME + " on "
				+ DBConstants.TAB_FAVORITE_PRODUCT_MAPPING_NAME + "."
				+ DBConstants.COL_FAVORITE_PRODUCT_MAPPING_STORE_ID + " = "
				+ DBConstants.TAB_STORE_NAME + "." + DBConstants.COL_STORE_ID + " INNER JOIN "
				+ DBConstants.TAB_PRODUCT_NAME + " on "
				+ DBConstants.TAB_FAVORITE_PRODUCT_MAPPING_NAME + "."
				+ DBConstants.COL_FAVORITE_PRODUCT_MAPPING_PRODUCT_ID + " = "
				+ DBConstants.TAB_PRODUCT_NAME + "." + DBConstants.COL_PRODUCT_ID + " INNER JOIN "
				+ DBConstants.TAB_UNIT_NAME + " on " + DBConstants.TAB_PRODUCT_NAME + "."
				+ DBConstants.COL_PRODUCT_UNIT_ID + " = " + DBConstants.TAB_UNIT_NAME + "."
				+ DBConstants.COL_UNIT_ID + " WHERE " + DBConstants.TAB_FAVORITE_NAME + "."
				+ DBConstants.COL_FAVORITE_ID + " = " + favoriteId;

		sqlQuery += " ORDER BY " + DBConstants.COL_PRODUCT_NAME;

		final Cursor cursor = this.database.rawQuery(sqlQuery, null);

		final List<FavoriteProductMapping> favoriteProductMappings = new LinkedList<FavoriteProductMapping>();
		while (cursor.moveToNext()) {

			final FavoriteProductMapping favoriteProductMapping = new FavoriteProductMapping();

			final Favorite favorite = new Favorite();
			favorite.setId(cursor.getInt(cursor.getColumnIndex(DBConstants.COL_FAVORITE_ID)));
			favorite.setName(TranslateUmlauts.translateIntoGermanUmlauts(cursor.getString(cursor
					.getColumnIndex(DBConstants.COL_FAVORITE_NAME))));

			final Unit unit = new Unit();
			unit.setId(cursor.getInt(cursor.getColumnIndex(DBConstants.COL_UNIT_ID)));
			unit.setName(TranslateUmlauts.translateIntoGermanUmlauts(cursor.getString(cursor
					.getColumnIndex(DBConstants.COL_UNIT_NAME))));

			final Product product = new Product();
			product.setId(cursor.getInt(cursor.getColumnIndex(DBConstants.COL_PRODUCT_ID)));
			product.setName(TranslateUmlauts.translateIntoGermanUmlauts(cursor.getString(cursor
					.getColumnIndex(DBConstants.COL_PRODUCT_NAME))));
			product.setUnit(unit);

			final Store store = new Store();
			store.setId(cursor.getInt(cursor.getColumnIndex(DBConstants.COL_STORE_ID)));
			store.setName(TranslateUmlauts.translateIntoGermanUmlauts(cursor.getString(cursor
					.getColumnIndex(DBConstants.COL_STORE_NAME))));
			store.setCountProducts(this.getProductCountForStore(store.getId()));
			store.setAlreadyCheckedProducts(this.getCheckedProductCountForStore(store.getId()));

			favoriteProductMapping.setId(cursor.getInt(cursor
					.getColumnIndex(DBConstants.COL_FAVORITE_PRODUCT_MAPPING_ID)));
			favoriteProductMapping.setQuantity(cursor.getString(cursor
					.getColumnIndex(DBConstants.COL_FAVORITE_PRODUCT_MAPPING_QUANTITY)));
			favoriteProductMapping.setProduct(product);
			favoriteProductMapping.setFavorite(favorite);
			favoriteProductMapping.setStore(store);

			favoriteProductMappings.add(favoriteProductMapping);
		}
		cursor.close();

		return favoriteProductMappings;
	}

	/**
	 * gets the history for a given shoppinglistId(Table.History)
	 * 
	 * @param shoppinglistId
	 * 
	 * @return List<History> historyList
	 */
	public List<History> getHistoryByShoppinglistId(final int shoppinglistId) {
		this.isDbLockedByThread();

		final String sqlQuery = "SELECT * FROM " + DBConstants.TAB_HISTORY_NAME + " INNER JOIN "
				+ DBConstants.TAB_SHOPPINGLIST_NAME + " ON "
				+ DBConstants.COL_HISTORY_SHOPPINGLIST_ID + " = " + DBConstants.COL_SHOPPINGLIST_ID
				+ " WHERE " + DBConstants.COL_HISTORY_SHOPPINGLIST_ID + " = " + shoppinglistId
				+ " ORDER BY " + DBConstants.COL_HISTORY_PRODUCT;

		final Cursor cursor = this.database.rawQuery(sqlQuery, null);

		final List<History> historyList = new LinkedList<History>();

		while (cursor.moveToNext()) {
			final History history = new History();

			history.setId(cursor.getInt(cursor.getColumnIndex(DBConstants.COL_HISTORY_ID)));
			history.setProduct(cursor.getString(cursor
					.getColumnIndex(DBConstants.COL_HISTORY_PRODUCT)));
			history.setStore(cursor.getString(cursor.getColumnIndex(DBConstants.COL_HISTORY_STORE)));
			history.setUnit(cursor.getString(cursor.getColumnIndex(DBConstants.COL_HISTORY_UNIT)));
			history.setQuantity(cursor.getString(cursor
					.getColumnIndex(DBConstants.COL_HISTORY_QUANTITY)));

			final Shoppinglist shoppinglist = new Shoppinglist();
			shoppinglist
					.setId(cursor.getInt(cursor.getColumnIndex(DBConstants.COL_SHOPPINGLIST_ID)));
			shoppinglist.setCreatedTime(Timestamp.valueOf(cursor.getString(cursor
					.getColumnIndex(DBConstants.COL_SHOPPINGLIST_CREATED_TIME))));
			shoppinglist.setFinishedTime(Timestamp.valueOf(cursor.getString(cursor
					.getColumnIndex(DBConstants.COL_SHOPPINGLIST_FINISHED_TIME))));

			history.setShoppinglist(shoppinglist);

			historyList.add(history);
		}
		cursor.close();

		return historyList;
	}

	/**
	 * gets the history-shoppinglists [distincted] (Table.History)
	 * 
	 * @return List<Shoppinglist> shoppinglists
	 */
	public List<Shoppinglist> getHistoryShoppinglists() {
		this.isDbLockedByThread();

		final String sqlQuery = "SELECT DISTINCT " + DBConstants.COL_SHOPPINGLIST_ID + ", "
				+ DBConstants.COL_SHOPPINGLIST_FINISHED_TIME + " FROM "
				+ DBConstants.TAB_HISTORY_NAME + " INNER JOIN " + DBConstants.TAB_SHOPPINGLIST_NAME
				+ " ON " + DBConstants.COL_HISTORY_SHOPPINGLIST_ID + " = "
				+ DBConstants.COL_SHOPPINGLIST_ID;

		final Cursor cursor = this.database.rawQuery(sqlQuery, null);

		final List<Shoppinglist> shoppinglists = new LinkedList<Shoppinglist>();

		while (cursor.moveToNext()) {
			final Shoppinglist shoppinglist = new Shoppinglist();

			shoppinglist
					.setId(cursor.getInt(cursor.getColumnIndex(DBConstants.COL_SHOPPINGLIST_ID)));
			shoppinglist.setFinishedTime(Timestamp.valueOf(cursor.getString(cursor
					.getColumnIndex(DBConstants.COL_SHOPPINGLIST_FINISHED_TIME))));

			shoppinglists.add(shoppinglist);
		}
		cursor.close();

		return shoppinglists;
	}

	/**
	 * gets a product with given productName and unitId
	 * 
	 * @param productName
	 * @param unitId
	 * @return Product product
	 */
	public Product getProductByNameAndUnit(String productName, final int unitId) {
		this.isDbLockedByThread();
		productName = TranslateUmlauts.translateFromGermanUmlauts(productName);

		final String sqlQuery = "SELECT " + DBConstants.COL_PRODUCT_ID + ", "
				+ DBConstants.COL_PRODUCT_NAME + ", " + DBConstants.COL_PRODUCT_UNIT_ID + " FROM "
				+ DBConstants.TAB_PRODUCT_NAME + " WHERE UPPER(" + DBConstants.TAB_PRODUCT_NAME
				+ "." + DBConstants.COL_PRODUCT_NAME + ") = '" + productName.toUpperCase(Locale.getDefault()).trim()
				+ "' AND " + DBConstants.COL_PRODUCT_UNIT_ID + " = " + unitId;

		final Cursor cursor = this.database.rawQuery(sqlQuery, null);
		Product product = null;

		if (cursor.getCount() == 1) {
			cursor.moveToNext();
			product = new Product();
			product.setId(cursor.getInt(cursor.getColumnIndex(DBConstants.COL_PRODUCT_ID)));
			product.setName(TranslateUmlauts.translateIntoGermanUmlauts(cursor.getString(cursor
					.getColumnIndex(DBConstants.COL_PRODUCT_NAME))));
		}
		return product;
	}

	/**
	 * 
	 * gets the product count (all) for this storeId
	 * 
	 * @param storeId
	 * @return int productCount
	 */
	public int getProductCountForStore(final int storeId) {
		this.isDbLockedByThread();

		final String sqlQuery = "SELECT COUNT("
				+ DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_PRODUCT_ID + ") AS "
				+ DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_PRODUCT_ID + " FROM "
				+ DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME + " WHERE "
				+ DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME + "."
				+ DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_STORE_ID + " = " + storeId;

		final Cursor productCountCursor = this.database.rawQuery(sqlQuery, null);

		int productCount = 0;
		while (productCountCursor.moveToNext()) {
			productCount = productCount
					+ productCountCursor
							.getInt(productCountCursor
									.getColumnIndex(DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_PRODUCT_ID));
		}
		productCountCursor.close();

		return productCount;
	}

	/**
	 * gets the shoppinglistProductMapping (for the given storeId, if the store
	 * is not specified -1 should be given)
	 * 
	 * @param storeId
	 * @return List<ShoppinglistProductMapping>
	 */
	public List<ShoppinglistProductMapping> getProductsOnShoppingList(final int storeId) {
		this.isDbLockedByThread();

		String sqlQuery = "SELECT * FROM " + DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME
				+ " INNER JOIN " + DBConstants.TAB_SHOPPINGLIST_NAME + " on "
				+ DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME + "."
				+ DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_SHOPPINGLIST_ID + " = "
				+ DBConstants.TAB_SHOPPINGLIST_NAME + "." + DBConstants.COL_SHOPPINGLIST_ID
				+ " INNER JOIN " + DBConstants.TAB_STORE_NAME + " on "
				+ DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME + "."
				+ DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_STORE_ID + " = "
				+ DBConstants.TAB_STORE_NAME + "." + DBConstants.COL_STORE_ID + " INNER JOIN "
				+ DBConstants.TAB_PRODUCT_NAME + " on "
				+ DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME + "."
				+ DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_PRODUCT_ID + " = "
				+ DBConstants.TAB_PRODUCT_NAME + "." + DBConstants.COL_PRODUCT_ID + " INNER JOIN "
				+ DBConstants.TAB_UNIT_NAME + " on " + DBConstants.TAB_PRODUCT_NAME + "."
				+ DBConstants.COL_PRODUCT_UNIT_ID + " = " + DBConstants.TAB_UNIT_NAME + "."
				+ DBConstants.COL_UNIT_ID + " WHERE " + DBConstants.TAB_SHOPPINGLIST_NAME + "."
				+ DBConstants.COL_SHOPPINGLIST_FINISHED_TIME + " is null";
		if (storeId != -1) {
			sqlQuery += " AND " + DBConstants.TAB_STORE_NAME + "." + DBConstants.COL_STORE_ID
					+ " = " + storeId;
		}
		sqlQuery += " ORDER BY LOWER(" + DBConstants.COL_PRODUCT_NAME + ")";

		final Cursor cursor = this.database.rawQuery(sqlQuery, null);

		final List<ShoppinglistProductMapping> shoppinglistProductMappings = new LinkedList<ShoppinglistProductMapping>();
		while (cursor.moveToNext()) {

			final ShoppinglistProductMapping shoppinglistProductMapping = new ShoppinglistProductMapping();

			final Shoppinglist shoppinglist = new Shoppinglist();
			shoppinglist
					.setId(cursor.getInt(cursor.getColumnIndex(DBConstants.COL_SHOPPINGLIST_ID)));
			if (cursor
					.getString((cursor.getColumnIndex(DBConstants.COL_SHOPPINGLIST_FINISHED_TIME))) != null) {
				shoppinglist.setFinishedTime(Timestamp.valueOf(cursor.getString((cursor
						.getColumnIndex(DBConstants.COL_SHOPPINGLIST_FINISHED_TIME)))));
			}

			final Unit unit = new Unit();
			unit.setId(cursor.getInt(cursor.getColumnIndex(DBConstants.COL_UNIT_ID)));
			unit.setName(TranslateUmlauts.translateIntoGermanUmlauts(cursor.getString(cursor
					.getColumnIndex(DBConstants.COL_UNIT_NAME))));

			final Product product = new Product();
			product.setId(cursor.getInt(cursor.getColumnIndex(DBConstants.COL_PRODUCT_ID)));
			product.setName(TranslateUmlauts.translateIntoGermanUmlauts(cursor.getString(cursor
					.getColumnIndex(DBConstants.COL_PRODUCT_NAME))));
			product.setUnit(unit);

			final Store store = new Store();
			store.setId(cursor.getInt(cursor.getColumnIndex(DBConstants.COL_STORE_ID)));
			store.setName(TranslateUmlauts.translateIntoGermanUmlauts(cursor.getString(cursor
					.getColumnIndex(DBConstants.COL_STORE_NAME))));
			store.setCountProducts(this.getProductCountForStore(store.getId()));
			store.setAlreadyCheckedProducts(this.getCheckedProductCountForStore(store.getId()));

			shoppinglistProductMapping.setId(cursor.getInt(cursor
					.getColumnIndex(DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_ID)));
			shoppinglistProductMapping.setQuantity(cursor.getString(cursor
					.getColumnIndex(DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_QUANTITY)));
			shoppinglistProductMapping.setChecked(cursor.getShort(cursor
					.getColumnIndex(DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_CHECKED)));
			shoppinglistProductMapping.setProduct(product);
			shoppinglistProductMapping.setShoppinglist(shoppinglist);
			shoppinglistProductMapping.setStore(store);

			shoppinglistProductMappings.add(shoppinglistProductMapping);
		}
		cursor.close();

		return shoppinglistProductMappings;
	}

	/**
	 * gets a store with given storeId
	 * 
	 * @param storeId
	 * @return Store store
	 */
	public Store getStoreById(final int storeId) {
		this.isDbLockedByThread();

		final String sqlQuery = "SELECT " + DBConstants.COL_STORE_ID + ", "
				+ DBConstants.COL_STORE_NAME + " FROM " + DBConstants.TAB_STORE_NAME + " WHERE "
				+ DBConstants.TAB_STORE_NAME + "." + DBConstants.COL_STORE_ID + " = " + storeId
				+ ";";

		final Cursor cursor = this.database.rawQuery(sqlQuery, null);
		final Store store = new Store();

		cursor.moveToNext();
		store.setId(storeId);
		store.setName(TranslateUmlauts.translateIntoGermanUmlauts(cursor.getString(cursor
				.getColumnIndex(DBConstants.COL_STORE_NAME))));
		store.setCountProducts(this.getProductCountForStore(store.getId()));
		store.setAlreadyCheckedProducts(this.getCheckedProductCountForStore(store.getId()));

		cursor.close();

		return store;
	}

	/**
	 * gets a store with given storeName
	 * 
	 * @param storeName
	 * @return Store store
	 */
	public Store getStoreByName(String storeName) {
		this.isDbLockedByThread();
		storeName = TranslateUmlauts.translateFromGermanUmlauts(storeName);

		final String sqlQuery = "SELECT " + DBConstants.COL_STORE_ID + ", "
				+ DBConstants.COL_STORE_NAME + " FROM " + DBConstants.TAB_STORE_NAME
				+ " WHERE UPPER(" + DBConstants.TAB_STORE_NAME + "." + DBConstants.COL_STORE_NAME
				+ ") = '" + storeName.toUpperCase().trim() + "'";

		final Cursor cursor = this.database.rawQuery(sqlQuery, null);

		Store store = null;

		if (cursor.getCount() == 1) {
			cursor.moveToNext();
			store = new Store();
			store.setId(cursor.getInt(cursor.getColumnIndex(DBConstants.COL_STORE_ID)));
			store.setName(TranslateUmlauts.translateIntoGermanUmlauts(cursor.getString(cursor
					.getColumnIndex(DBConstants.COL_STORE_NAME))));
		}
		cursor.close();

		return store;
	}

	/**
	 * Gets all stores from the DB (Table: Store) for the overview
	 * "for the overview" means that this are only the stores which are in use
	 * 
	 * @return List<Store> stores
	 */
	public List<Store> getStoresForOverview() {
		this.isDbLockedByThread();

		final String sqlQuery = "SELECT DISTINCT " + DBConstants.COL_STORE_ID + ", "
				+ DBConstants.COL_STORE_NAME + " FROM " + DBConstants.TAB_STORE_NAME
				+ " INNER JOIN " + DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME + " ON "
				+ DBConstants.TAB_STORE_NAME + "." + DBConstants.COL_STORE_ID + " = "
				+ DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME + "."
				+ DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_STORE_ID + " ORDER BY "
				+ DBConstants.COL_STORE_NAME;

		final Cursor cursor = this.database.rawQuery(sqlQuery, null);

		final List<Store> stores = new LinkedList<Store>();
		while (cursor.moveToNext()) {

			final Store store = new Store();
			store.setId(cursor.getInt(cursor.getColumnIndex(DBConstants.COL_STORE_ID)));
			store.setName(TranslateUmlauts.translateIntoGermanUmlauts(cursor.getString(cursor
					.getColumnIndex(DBConstants.COL_STORE_NAME))));
			store.setCountProducts(this.getProductCountForStore(store.getId()));
			store.setAlreadyCheckedProducts(this.getCheckedProductCountForStore(store.getId()));
			stores.add(store);
		}
		cursor.close();

		return stores;
	}

	/**
	 * gets an unit with given unitName
	 * 
	 * @param unitName
	 * @return Unit unit
	 */
	public Unit getUnitByName(String unitName) {
		this.isDbLockedByThread();
		unitName = TranslateUmlauts.translateFromGermanUmlauts(unitName);

		final String sqlQuery = "SELECT " + DBConstants.COL_UNIT_ID + ", "
				+ DBConstants.COL_UNIT_NAME + " FROM " + DBConstants.TAB_UNIT_NAME
				+ " WHERE UPPER(" + DBConstants.TAB_UNIT_NAME + "." + DBConstants.COL_UNIT_NAME
				+ ") = '" + unitName.toUpperCase().trim() + "'";

		final Cursor cursor = this.database.rawQuery(sqlQuery, null);

		Unit unit = null;

		if (cursor.getCount() == 1) {
			cursor.moveToNext();
			unit = new Unit();
			unit.setId(cursor.getInt(cursor.getColumnIndex(DBConstants.COL_UNIT_ID)));
			unit.setName(TranslateUmlauts.translateIntoGermanUmlauts(cursor.getString(cursor
					.getColumnIndex(DBConstants.COL_UNIT_NAME))));
		}
		cursor.close();

		return unit;
	}

	/**
	 * gets the viewType the user has set up
	 * 
	 * @return short viewType
	 */
	public short getUserConfigurationViewType() {
		this.isDbLockedByThread();

		final String sqlQuery = "SELECT " + DBConstants.COL_USER_CONFIGURATION_VIEW_TYPE + " FROM "
				+ DBConstants.TAB_USER_CONFIGURATION;
		final Cursor cursor = this.database.rawQuery(sqlQuery, null);

		short viewType = ConfigurationConstants.STORE_VIEW;

		while (cursor.moveToNext()) {
			viewType = cursor.getShort(cursor
					.getColumnIndex(DBConstants.COL_USER_CONFIGURATION_VIEW_TYPE));
		}
		cursor.close();

		return viewType;
	}

	/**
	 * marks the mapping as checked
	 * 
	 * @param shoppinglistProductMappingId
	 */
	public void markShoppinglistProductMappingAsChecked(final int shoppinglistProductMappingId) {
		this.isDbLockedByThread();

		final String sqlQuery = "UPDATE " + DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME
				+ " SET " + DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_CHECKED + " = "
				+ GlobalValues.YES + " WHERE " + DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME
				+ "." + DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_ID + " = "
				+ shoppinglistProductMappingId;

		this.database.execSQL(sqlQuery);
	}

	/**
	 * marks the mapping as unchecked
	 * 
	 * @param shoppinglistProductMappingId
	 */
	public void markShoppinglistProductMappingAsUnchecked(final int shoppinglistProductMappingId) {
		this.isDbLockedByThread();

		final String sqlQuery = "UPDATE " + DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME
				+ " SET " + DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_CHECKED + " = "
				+ GlobalValues.NO + " WHERE " + DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME
				+ "." + DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_ID + " = "
				+ shoppinglistProductMappingId;

		this.database.execSQL(sqlQuery);
	}

	/**
	 * opens the database
	 */
	public void open() {
		try {
			this.database = this.dbHelper.getWritableDatabase();
		} catch (final SQLException se) {
			se.printStackTrace();
		}
	}

	/**
	 * Saves a favorite with the given name
	 * 
	 * @param name
	 */
	public void saveFavorite(String name) {
		this.isDbLockedByThread();
		name = TranslateUmlauts.translateFromGermanUmlauts(name);

		final String sqlQuery = "INSERT INTO " + DBConstants.TAB_FAVORITE_NAME + " ("
				+ DBConstants.COL_FAVORITE_NAME + ") VALUES ('" + name.trim() + "')";

		this.database.execSQL(sqlQuery);
	}

	/**
	 * Saves the mapping for the favorite (all information)
	 * 
	 * @param favoriteId
	 * @param storeId
	 * @param productId
	 * @param quantity
	 */
	public void saveFavoriteProductMapping(final int favoriteId, final int storeId,
			final int productId, final String quantity) {
		this.isDbLockedByThread();

		final String sqlQuery = "INSERT INTO " + DBConstants.TAB_FAVORITE_PRODUCT_MAPPING_NAME
				+ " (" + DBConstants.COL_FAVORITE_PRODUCT_MAPPING_FAVORITE_ID + ", "
				+ DBConstants.COL_FAVORITE_PRODUCT_MAPPING_STORE_ID + ", "
				+ DBConstants.COL_FAVORITE_PRODUCT_MAPPING_PRODUCT_ID + ", "
				+ DBConstants.COL_FAVORITE_PRODUCT_MAPPING_QUANTITY + ") VALUES (" + favoriteId
				+ ", " + storeId + ", " + productId + ", " + quantity + ")";
		this.database.execSQL(sqlQuery);
	}

	/**
	 * Saves a product with the given name and unitId
	 * 
	 * @param name
	 * @param unitId
	 */
	public void saveProduct(String name, final int unitId) {
		this.isDbLockedByThread();
		name = TranslateUmlauts.translateFromGermanUmlauts(name);

		final String sqlQuery = "INSERT INTO " + DBConstants.TAB_PRODUCT_NAME + " ("
				+ DBConstants.COL_PRODUCT_NAME + ", " + DBConstants.COL_PRODUCT_UNIT_ID
				+ ") VALUES ('" + name.trim() + "', " + unitId + ")";
		this.database.execSQL(sqlQuery);
	}

	/**
	 * Saves the mapping for the overview (all information)
	 * 
	 * @param storeId
	 * @param productId
	 * @param quantity
	 * @param checked
	 */
	public void saveShoppingListProductMapping(final int storeId, final int productId,
			final String quantity, final short checked) {
		this.isDbLockedByThread();

		final String queryToGetShoppinglistId = "SELECT MAX(" + DBConstants.COL_SHOPPINGLIST_ID
				+ ") as " + DBConstants.COL_SHOPPINGLIST_ID + " FROM "
				+ DBConstants.TAB_SHOPPINGLIST_NAME + " WHERE "
				+ DBConstants.COL_SHOPPINGLIST_FINISHED_TIME + " is null";

		final Cursor shoppinglistIdCursor = this.database.rawQuery(queryToGetShoppinglistId, null);
		int shoppinglistId = -1;

		if (shoppinglistIdCursor.getCount() == 1) {
			shoppinglistIdCursor.moveToNext();
			shoppinglistId = shoppinglistIdCursor.getInt(shoppinglistIdCursor
					.getColumnIndex(DBConstants.COL_SHOPPINGLIST_ID));
		}
		shoppinglistIdCursor.close();

		final String sqlQuery = "INSERT INTO " + DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME
				+ " (" + DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_SHOPPINGLIST_ID + ", "
				+ DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_STORE_ID + ", "
				+ DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_PRODUCT_ID + ", "
				+ DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_QUANTITY + ", "
				+ DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_CHECKED + ") VALUES ("
				+ shoppinglistId + ", " + storeId + ", " + productId + ", " + quantity + ", "
				+ checked + ")";
		this.database.execSQL(sqlQuery);
	}

	/**
	 * Saves a store with the given name
	 * 
	 * @param name
	 */
	public void saveStore(String name) {
		this.isDbLockedByThread();
		name = TranslateUmlauts.translateFromGermanUmlauts(name);

		final String sqlQuery = "INSERT INTO " + DBConstants.TAB_STORE_NAME + " ("
				+ DBConstants.COL_STORE_NAME + ") VALUES ('" + name.trim() + "')";

		this.database.execSQL(sqlQuery);
	}

	/**
	 * Saves a unit with the given name
	 * 
	 * @param name
	 */
	public void saveUnit(String name) {
		this.isDbLockedByThread();
		name = TranslateUmlauts.translateFromGermanUmlauts(name);

		final String sqlQuery = "INSERT INTO " + DBConstants.TAB_UNIT_NAME + " ("
				+ DBConstants.COL_UNIT_NAME + ") VALUES ('" + name.trim() + "')";

		this.database.execSQL(sqlQuery);
	}

	/**
	 * gets the viewType the user has set up
	 * 
	 * @param viewType
	 * 
	 */
	public void setUserConfiguration(final short viewType) {
		this.isDbLockedByThread();

		final String sqlQuery = "UPDATE " + DBConstants.TAB_USER_CONFIGURATION + " SET "
				+ DBConstants.COL_USER_CONFIGURATION_VIEW_TYPE + " = " + viewType;

		this.database.execSQL(sqlQuery);
	}

	/**
	 * updates a favorite with given favorite
	 * 
	 * @param favorite
	 * 
	 */
	public void updateFavorite(final Favorite favorite) {
		this.isDbLockedByThread();

		final String sqlQuery = "UPDATE " + DBConstants.TAB_FAVORITE_NAME + " SET "
				+ DBConstants.COL_FAVORITE_NAME + " = '" + favorite.getName().trim() + "' WHERE "
				+ DBConstants.TAB_FAVORITE_NAME + "." + DBConstants.COL_FAVORITE_ID + " = "
				+ favorite.getId();

		this.database.execSQL(sqlQuery);
	}

	/**
	 * updates a favoriteProductMapping with given id by given values
	 * 
	 * @param favoriteProductMappingId
	 * @param storeId
	 * @param productId
	 * @param quantity
	 */
	public void updateFavoriteProductMapping(final int favoriteProductMappingId, final int storeId,
			final int productId, final String quantity) {
		this.isDbLockedByThread();

		final String sqlQuery = "UPDATE " + DBConstants.TAB_FAVORITE_PRODUCT_MAPPING_NAME + " SET "
				+ DBConstants.COL_FAVORITE_PRODUCT_MAPPING_STORE_ID + " = " + storeId + ", "
				+ DBConstants.COL_FAVORITE_PRODUCT_MAPPING_PRODUCT_ID + " = " + productId + ", "
				+ DBConstants.COL_FAVORITE_PRODUCT_MAPPING_QUANTITY + " = " + quantity + " WHERE "
				+ DBConstants.COL_FAVORITE_PRODUCT_MAPPING_ID + " = " + favoriteProductMappingId;

		this.database.execSQL(sqlQuery);
	}

	/**
	 * updates a product with given product
	 * 
	 * @param product
	 * 
	 */
	public void updateProduct(final Product product) {
		this.isDbLockedByThread();

		final String sqlQuery = "UPDATE " + DBConstants.TAB_PRODUCT_NAME + " SET "
				+ DBConstants.COL_PRODUCT_NAME + " = '" + product.getName().trim() + "' , "
				+ DBConstants.COL_PRODUCT_UNIT_ID + " = " + product.getUnit().getId() + " WHERE "
				+ DBConstants.TAB_PRODUCT_NAME + "." + DBConstants.COL_PRODUCT_ID + " = "
				+ product.getId();

		this.database.execSQL(sqlQuery);
	}

	/**
	 * updates a shoppinglistProductMapping with given id by given values
	 * 
	 * @param shoppinglistProductMappingId
	 * @param storeId
	 * @param productId
	 * @param quantity
	 */
	public void updateShoppinglistProductMapping(final int shoppinglistProductMappingId,
			final int storeId, final int productId, final String quantity) {
		this.isDbLockedByThread();

		final String sqlQuery = "UPDATE " + DBConstants.TAB_SHOPPINGLIST_PRODUCT_MAPPING_NAME
				+ " SET " + DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_STORE_ID + " = " + storeId
				+ ", " + DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_PRODUCT_ID + " = "
				+ productId + ", " + DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_QUANTITY + " = "
				+ quantity + " WHERE " + DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_ID + " = "
				+ shoppinglistProductMappingId;

		this.database.execSQL(sqlQuery);
	}

	/**
	 * updates a store with given store
	 * 
	 * @param store
	 * 
	 * @param Store
	 *            store
	 */
	public void updateStore(final Store store) {
		this.isDbLockedByThread();

		final String sqlQuery = "UPDATE " + DBConstants.TAB_STORE_NAME + " SET "
				+ DBConstants.COL_STORE_NAME + " = '" + store.getName().trim() + "' WHERE "
				+ DBConstants.TAB_STORE_NAME + "." + DBConstants.COL_STORE_ID + " = "
				+ store.getId();

		this.database.execSQL(sqlQuery);
	}

	/**
	 * updates an unit with given unit
	 * 
	 * @param unit
	 * 
	 */
	public void updateUnit(final Unit unit) {
		this.isDbLockedByThread();

		final String sqlQuery = "UPDATE " + DBConstants.TAB_UNIT_NAME + " SET "
				+ DBConstants.COL_UNIT_NAME + " = '" + unit.getName().trim() + "' WHERE "
				+ DBConstants.TAB_UNIT_NAME + "." + DBConstants.COL_UNIT_ID + " = " + unit.getId();

		this.database.execSQL(sqlQuery);
	}

	/**
	 * gets all product names for Auto-Complete from the DB (Table: Product)
	 * 
	 * @return List<String> productNames
	 */
	public List<String> getAllProductNames() {
		this.isDbLockedByThread();

		final String sqlQuery = "SELECT " + DBConstants.COL_PRODUCT_NAME + " FROM "
				+ DBConstants.TAB_PRODUCT_NAME + " ORDER BY " + DBConstants.COL_PRODUCT_NAME;

		final Cursor cursor = this.database.rawQuery(sqlQuery, null);

		final List<String> productNames = new ArrayList<String>();

		while (cursor.moveToNext()) {
			String productName = cursor.getString(cursor
					.getColumnIndex(DBConstants.COL_PRODUCT_NAME));
			productNames.add(productName);
		}
		cursor.close();

		return productNames;
	}

	/**
	 * 
	 * <p>
	 * Checks whether the DB is locked by a thread (current or other). To avoid
	 * deadlock-infinite-loop there's a counter.
	 * </p>
	 * 
	 */
	private void isDbLockedByThread() {
		int counter = 0;
		while (((this.database != null)
				&& (this.database.isDbLockedByCurrentThread() || this.database
						.isDbLockedByOtherThreads()) && (counter < 1000))) {
			counter++;
		}
	}
}
