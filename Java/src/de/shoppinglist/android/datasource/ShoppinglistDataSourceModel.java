package de.shoppinglist.android.datasource;

import android.content.Context;

/**
 * <p>
 * Holds the datasource, for connection pooling. (widget)
 * </p>
 * 
 */

public class ShoppinglistDataSourceModel {

	private static ShoppinglistDataSource datasource;

	/**
	 * 
	 * <p>
	 * Closes the dataSource.
	 * </p>
	 * 
	 */
	public static void closeDatasource() {
		ShoppinglistDataSourceModel.datasource.close();
	}

	/**
	 * Returns the datasource.
	 * 
	 * @param context
	 * 
	 * @return Returns the datasource.
	 */
	public static ShoppinglistDataSource openDatasource(final Context context) {
		if (ShoppinglistDataSourceModel.datasource == null) {
			ShoppinglistDataSourceModel.datasource = new ShoppinglistDataSource(context);
		}
		ShoppinglistDataSourceModel.datasource.open();
		return ShoppinglistDataSourceModel.datasource;
	}

}
