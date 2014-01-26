package de.shoppinglist.android.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import de.shoppinglist.android.constant.GlobalValues;
import de.shoppinglist.android.datasource.ShoppinglistDataSource;
import de.shoppinglist.android.datasource.ShoppinglistDataSourceModel;

public class ShoppinglistWidgetBroadcastReceiver extends BroadcastReceiver {

	private ShoppinglistDataSource datasource;

	@Override
	public void onReceive(final Context context, final Intent intent) {

		// mark the clicked shoppinglistProductMapping as checked /
		// unchecked.
		if (intent.getAction().equals(ShoppinglistWidgetProvider.CLICK_ROW)) {
			this.datasource = ShoppinglistDataSourceModel.openDatasource(context);

			// get Extras from the intent
			final short mappingChecked = intent.getExtras().getShort(
					ShoppinglistWidgetProvider.CLICKED_ROW_SHOPPINGLISTPRODUCTMAPPING_CHECKED);

			final int mappingId = intent.getExtras().getInt(
					ShoppinglistWidgetProvider.CLICKED_ROW_SHOPPINGLISTPRODUCTMAPPING_ID);

			// update database
			if (mappingChecked == GlobalValues.YES) {
				this.datasource.markShoppinglistProductMappingAsUnchecked(mappingId);
			} else {
				this.datasource.markShoppinglistProductMappingAsChecked(mappingId);
			}

			// update widget data
			context.sendBroadcast(new Intent("android.appwidget.action.APPWIDGET_UPDATE"));
		}
	}
}
