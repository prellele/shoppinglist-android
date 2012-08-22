package de.shoppinglist.android.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import de.shoppinglist.android.R;
import de.shoppinglist.android.bean.Favorite;

public class FavoriteAdapter extends ArrayAdapter<Favorite> {

	@SuppressWarnings("unused")
	private final Context context;

	private final List<Favorite> values;

	public FavoriteAdapter(final Context context, final List<Favorite> values) {
		super(context, R.layout.list_row, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final TextView textView = (TextView) super.getView(position, convertView, parent);

		final Favorite favoriteToBeShown = this.values.get(position);

		textView.setText(favoriteToBeShown.toString());

		return textView;
	}
}
