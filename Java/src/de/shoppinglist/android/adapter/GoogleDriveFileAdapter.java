package de.shoppinglist.android.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.api.services.drive.model.File;

import de.shoppinglist.android.R;

public class GoogleDriveFileAdapter extends ArrayAdapter<File> {

	@SuppressWarnings("unused")
	private final Context context;

	private final List<File> values;

	public GoogleDriveFileAdapter(final Context context, final List<File> values) {
		super(context, R.layout.list_row, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final TextView textView = (TextView) super.getView(position, convertView, parent);

		final File fileToBeShown = this.values.get(position);

		textView.setText(fileToBeShown.getTitle());

		return textView;
	}
}
