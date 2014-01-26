package de.shoppinglist.android.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * <p>
 * Service for the Widget, just calls the ViewFactory.
 * </p>
 * 
 */

public class ShoppinglistWidgetService extends RemoteViewsService {

	/**
	 * 
	 * <p>
	 * Service for the WidgetProvider. Calls the ViewsFactory.
	 * </p>
	 * 
	 * @param Intent
	 *            intent
	 * @return RemoteViewsFactory
	 * 
	 * @see android.widget.RemoteViewsService#onGetViewFactory(android.content.Intent)
	 */

	@Override
	public RemoteViewsFactory onGetViewFactory(final Intent intent) {
		return (new ShoppinglistViewsFactory(this.getApplicationContext(), intent));
	}

}
