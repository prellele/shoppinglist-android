package de.shoppinglist.android.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;
import de.shoppinglist.android.R;
import de.shoppinglist.android.datasource.ShoppinglistDataSourceModel;

/**
 * 
 * <p>
 * AppWidgetProvider for the Widget.
 * </p>
 * 
 * 
 */

public class ShoppinglistWidgetProvider extends AppWidgetProvider {

	public static String CLICK_ROW = "de.shoppinglist.android.widget.CLICK_ROW";

	public static String CLICKED_ROW_SHOPPINGLISTPRODUCTMAPPING_CHECKED = "de.shoppinglist.android.widget.CLICKED_ROW_SHOPPINGLISTPRODUCTMAPPING_CHECKED";

	public static String CLICKED_ROW_SHOPPINGLISTPRODUCTMAPPING_ID = "de.shoppinglist.android.widget.CLICKED_ROW_SHOPPINGLISTPRODUCTMAPPING_ID";

	public static String CLICKED_WIDGET_ID = "de.shoppinglist.android.widget.CLICKED_WIDGET_ID";

	public static String UPDATE_DATA = "de.shoppinglist.android.widget.UPDATE_DATA";

	/** {@inheritDoc} **/
	@Override
	public void onDeleted(final Context context, final int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);

		// close the datasource connection
		ShoppinglistDataSourceModel.closeDatasource();
	}

	/** {@inheritDoc} **/
	@Override
	public void onReceive(final Context context, final Intent intent) {
		super.onReceive(context, intent);

		// update the whole widget-data, if there is an update-broadcast
		// received.
		if (intent.getAction().equals("android.appwidget.action.APPWIDGET_UPDATE")) {
			final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,
					ShoppinglistWidgetProvider.class));
			this.onUpdate(context, appWidgetManager, appWidgetIds);
		}
	}

	/** {@inheritDoc} **/
	@Override
	public void onUpdate(final Context context, final AppWidgetManager appWidgetManager,
			final int[] appWidgetIds) {

		for (final int appWidgetId : appWidgetIds) {
			appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId,
					R.id.widgetListShoppinglistProductMappingsAlphabetically);

			final Intent svcIntent = new Intent(context, ShoppinglistWidgetService.class);

			svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

			final RemoteViews widget = new RemoteViews(context.getPackageName(),
					R.layout.widget_layout);

			// call the service and fill the listView
			widget.setRemoteAdapter(appWidgetId,
					R.id.widgetListShoppinglistProductMappingsAlphabetically, svcIntent);

			final Intent clickIntent = new Intent(ShoppinglistWidgetProvider.CLICK_ROW);
			final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, clickIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			widget.setPendingIntentTemplate(
					R.id.widgetListShoppinglistProductMappingsAlphabetically, pendingIntent);

			appWidgetManager.updateAppWidget(appWidgetId, widget);
		}

		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
}
