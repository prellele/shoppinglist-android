package de.shoppinglist.android.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import de.shoppinglist.android.R;
import de.shoppinglist.android.bean.History;

public class HistoryShoppinglistAdapter extends ArrayAdapter<History> {

	@SuppressWarnings("unused")
	private final Context context;

	private final List<History> values;

	public HistoryShoppinglistAdapter(final Context context, final List<History> values) {
		super(context, R.layout.list_row, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final TextView textView = (TextView) super.getView(position, convertView, parent);

		final History historyToBeShown = this.values.get(position);

		String textToShow = "";

		textToShow = historyToBeShown.getQuantity() + " " + historyToBeShown.getUnit() + " "
				+ historyToBeShown.getProduct() + " bei " + historyToBeShown.getStore();

		textView.setText(textToShow);

		return textView;
	}

}
