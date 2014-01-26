package de.shoppinglist.android.adapter;

import java.util.Date;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import de.shoppinglist.android.R;
import de.shoppinglist.android.bean.Shoppinglist;
import de.shoppinglist.android.helper.GMTToLocalTimeConverter;

public class HistoryOverviewAdapter extends ArrayAdapter<Shoppinglist> {

	@SuppressWarnings("unused")
	private final Context context;

	private final List<Shoppinglist> values;

	public HistoryOverviewAdapter(final Context context, final List<Shoppinglist> values) {
		super(context, R.layout.list_row, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final TextView textView = (TextView) super.getView(position, convertView, parent);

		final Shoppinglist shoppinglistToBeShown = this.values.get(position);

		final Date finishedTime = GMTToLocalTimeConverter.convert(shoppinglistToBeShown
				.getFinishedTime());

		textView.setText(finishedTime.toLocaleString());

		return textView;
	}
}
