package de.shoppinglist.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import de.shoppinglist.android.constant.ConfigurationConstants;
import de.shoppinglist.android.constant.GlobalValues;
import de.shoppinglist.android.datasource.ShoppinglistDataSource;

public class UserConfigurationActivity extends AbstractShoppinglistActivity {

	private Button buttonSaveConfiguration;

	private ShoppinglistDataSource datasource;

	private RadioButton radioButtonViewTypeAlphabetically;

	private RadioButton radioButtonViewTypeStore;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.datasource = super.getDatasource();

		this.setContentView(R.layout.user_configuration);

		this.radioButtonViewTypeStore = (RadioButton) this.findViewById(R.id.radioButtonViewStore);
		this.radioButtonViewTypeAlphabetically = (RadioButton) this
				.findViewById(R.id.radioButtonViewAlphabetically);
		final short viewType = this.datasource.getUserConfigurationViewType();

		if (viewType == ConfigurationConstants.STORE_VIEW) {
			this.radioButtonViewTypeStore.setChecked(GlobalValues.YES_BOOL);
		}
		if (viewType == ConfigurationConstants.ALPHABETICALLY_VIEW) {
			this.radioButtonViewTypeAlphabetically.setChecked(GlobalValues.YES_BOOL);
		}

		this.buttonSaveConfiguration = (Button) this.findViewById(R.id.buttonSaveUserConfiguration);
		this.buttonSaveConfiguration.setOnClickListener(new OnClickListener() {

			public void onClick(final View v) {
				short viewType = ConfigurationConstants.STORE_VIEW;

				if (UserConfigurationActivity.this.radioButtonViewTypeStore.isChecked()) {
					viewType = ConfigurationConstants.STORE_VIEW;

				} else if (UserConfigurationActivity.this.radioButtonViewTypeAlphabetically
						.isChecked()) {
					viewType = ConfigurationConstants.ALPHABETICALLY_VIEW;
				}

				UserConfigurationActivity.this.datasource.setUserConfiguration(viewType);
				UserConfigurationActivity.this.finish();
			}

		});
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {

		case android.R.id.home:
			Intent intent = new Intent(this, ShoppinglistActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;

		default:
			break;
		}
		return false;
	}
}
