package de.shoppinglist.android;

import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import de.shoppinglist.android.datasource.ShoppinglistDataSource;

/**
 * 
 * <p>
 * AbstractActivity for all ShoppinglistActivities. Implements the default
 * methods like "onPause()".
 * </p>
 * 
 */

public abstract class AbstractShoppinglistActivity extends Activity {

	private Context context;

	private ShoppinglistDataSource datasource;

	@Override
	public void finish() {
		super.finish();
		this.datasource.close();
	}

	/**
	 * Returns the context.
	 * 
	 * @return Returns the context.
	 */
	public Context getContext() {
		return this.context;
	}

	/**
	 * Returns the datasource.
	 * 
	 * @return Returns the datasource.
	 */
	public ShoppinglistDataSource getDatasource() {
		return this.datasource;
	}

	public TextWatcher getTextWatcher(final int editTextId) {
		final EditText editText = (EditText) this.findViewById(editTextId);

		final TextWatcher textWatcher = new TextWatcher() {

			public void afterTextChanged(final Editable editable) {
				// no operation
			}

			public void beforeTextChanged(final CharSequence s, final int start, final int count,
					final int after) {
				// no operation

			}

			public void onTextChanged(final CharSequence s, final int start, final int before,
					final int count) {
				// if the text changed clear the error message in the edittext.
				editText.setError(null);
			}
		};
		return textWatcher;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (this.getClass() != ShoppinglistActivity.class) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		this.datasource = new ShoppinglistDataSource(this);
		this.datasource.open();
		this.context = this;
	}

	@Override
	protected void onPause() {
		super.onPause();
		this.datasource.close();
		this.sendBroadcast(new Intent("android.appwidget.action.APPWIDGET_UPDATE"));
	}

	@Override
	public void onResume() {
		super.onResume();
		this.datasource.close();
		this.datasource.open();
	}

	/**
	 * Sets an errorMessage on given EditTexts, if the EditText is empty.
	 * 
	 * @param editTextIds
	 * @return false, if there are empty EditTexts
	 */
	public boolean setErrorOnEmptyEditTexts(final List<Integer> editTextIds) {
		boolean noEmptyEditText = true;
		for (final Integer editTextId : editTextIds) {
			if (this.findViewById(editTextId) instanceof EditText) {
				final EditText editText = (EditText) this.findViewById(editTextId);

				if ((editText.getText().toString() == null)
						|| editText.getText().toString().isEmpty()) {
					editText.setError(this.getString(R.string.msg_cannot_be_emtpy));
					noEmptyEditText = false;
				}
			}
		}
		return noEmptyEditText;
	}
}
