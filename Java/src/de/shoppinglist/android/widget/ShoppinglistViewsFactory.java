package de.shoppinglist.android.widget;

import java.util.List;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;
import de.shoppinglist.android.R;
import de.shoppinglist.android.R.color;
import de.shoppinglist.android.bean.ShoppinglistProductMapping;
import de.shoppinglist.android.constant.GlobalValues;
import de.shoppinglist.android.datasource.ShoppinglistDataSource;
import de.shoppinglist.android.datasource.ShoppinglistDataSourceModel;

/**
 * <p>
 * The ViewsFactory for the widget. Builds the ListView for the widget.
 * </p>
 * 
 * 
 */

public class ShoppinglistViewsFactory implements RemoteViewsFactory {

	private final int appWidgetId;

	private final Context context;

	private ShoppinglistDataSource datasource;

	private List<ShoppinglistProductMapping> shoppinglistProductMappings;

	/**
	 * Constructor
	 * 
	 * @param context
	 * @param intent
	 **/
	public ShoppinglistViewsFactory(final Context context, final Intent intent) {
		this.context = context;
		this.appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
				AppWidgetManager.INVALID_APPWIDGET_ID);
	}

	/** {@inheritDoc} **/
	public int getCount() {
		return this.shoppinglistProductMappings.size();
	}

	/** {@inheritDoc} **/
	public long getItemId(final int position) {
		return position;
	}

	/** {@inheritDoc} **/
	public RemoteViews getLoadingView() {
		// no operation
		return null;
	}

	/** {@inheritDoc} **/
	public RemoteViews getViewAt(final int position) {
		// set the shoppinglistProductMapping-details on the row
		final RemoteViews row = new RemoteViews(this.context.getPackageName(), R.layout.widget_row);

		final String quantity = this.shoppinglistProductMappings.get(position).getQuantity();
		final String unitName = this.shoppinglistProductMappings.get(position).getProduct()
				.getUnit().getName();
		final String productName = this.shoppinglistProductMappings.get(position).getProduct()
				.getName();
		final String storeName = this.shoppinglistProductMappings.get(position).getStore()
				.getName();

		String text = quantity + " " + unitName + " " + productName;
		if (this.shoppinglistProductMappings.get(position).getStore().getId() != 1)
			text += " (" + storeName + ")";

		row.setTextViewText(R.id.widgetRowText, text);

		// strikethrough the text, if the item is already checked and show the
		// checked_checkbox
		if (this.shoppinglistProductMappings.get(position).isChecked() == GlobalValues.YES) {
			row.setTextColor(R.id.widgetRowText, Color.GRAY);
			row.setImageViewResource(R.id.widgetRowCheckBox, R.drawable.checked_box);
		} else {
			row.setTextColor(R.id.widgetRowText, Color.BLACK);
			row.setImageViewResource(R.id.widgetRowCheckBox, R.drawable.check_box);
		}

		final Intent i = new Intent();
		final Bundle extras = new Bundle();

		extras.putShort(ShoppinglistWidgetProvider.CLICKED_ROW_SHOPPINGLISTPRODUCTMAPPING_CHECKED,
				this.shoppinglistProductMappings.get(position).isChecked());

		extras.putInt(ShoppinglistWidgetProvider.CLICKED_ROW_SHOPPINGLISTPRODUCTMAPPING_ID,
				this.shoppinglistProductMappings.get(position).getId());

		extras.putInt(ShoppinglistWidgetProvider.CLICKED_WIDGET_ID, this.appWidgetId);
		i.putExtras(extras);
		row.setOnClickFillInIntent(R.id.widgetRowCheckBox, i);

		return (row);

	}

	/** {@inheritDoc} **/
	public int getViewTypeCount() {
		return 1;
	}

	/** {@inheritDoc} **/
	public boolean hasStableIds() {
		return true;
	}

	/** {@inheritDoc} **/
	public void onCreate() {
		// no operation
	}

	/** {@inheritDoc} **/
	public void onDataSetChanged() {
		this.datasource = ShoppinglistDataSourceModel.openDatasource(this.context);
		if (this.datasource != null) {
			this.shoppinglistProductMappings = this.datasource.getProductsOnShoppingList(-1);
		}
	}

	/** {@inheritDoc} **/
	public void onDestroy() {
		// no operation
	}

}
