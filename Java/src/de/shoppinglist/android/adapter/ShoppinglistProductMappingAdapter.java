package de.shoppinglist.android.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.shoppinglist.android.R;
import de.shoppinglist.android.bean.ShoppinglistProductMapping;
import de.shoppinglist.android.constant.GlobalValues;

public class ShoppinglistProductMappingAdapter extends ArrayAdapter<ShoppinglistProductMapping> {

	private final Context context;

	private final List<ShoppinglistProductMapping> values;

	public ShoppinglistProductMappingAdapter(final Context context, final List<ShoppinglistProductMapping> values) {
		super(context, R.layout.list_row, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {

		final LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View rowView = inflater.inflate(R.layout.row_shoppinglist_product_mapping, parent, false);

		// final TextView textView = (TextView) super.getView(position,
		// convertView, parent);
		final TextView textView = (TextView) rowView.findViewById(R.id.outputShoppinglistProductMapping);
		final ImageView imageView = (ImageView) rowView.findViewById(R.id.imageShoppinglistProductMappingChecked);

		final ShoppinglistProductMapping shoppinglistProductMappingToShow = this.values.get(position);

		textView.setText(shoppinglistProductMappingToShow.toString());

		if (shoppinglistProductMappingToShow.isChecked() == GlobalValues.YES) {
			// paint the strikethrough
			textView.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
			imageView.setImageResource(R.drawable.ic_checked);

		} else if (shoppinglistProductMappingToShow.isChecked() == GlobalValues.NO) {
			// remove the strikethrough
			textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
			imageView.setImageBitmap(null);

		}

		return rowView;
	}
}
