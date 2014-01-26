package de.shoppinglist.android;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import de.shoppinglist.android.bean.Store;
import de.shoppinglist.android.constant.DBConstants;
import de.shoppinglist.android.datasource.ShoppinglistDataSource;

public class EditStoreActivity extends AbstractShoppinglistActivity {

	private Button buttonConfirmEdit;

	private Context context;

	private ShoppinglistDataSource datasource;

	private EditText editTextStoreName;

	private List<Integer> editTextIds = new LinkedList<Integer>(
			Arrays.asList(R.id.editTextNameAddStore));

	private TextView textViewTitle;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.datasource = super.getDatasource();
		this.context = super.getContext();

		this.setContentView(R.layout.add_or_edit_store);

		// get values of calling activity
		final int selectedStoreId = this.getIntent().getIntExtra(DBConstants.COL_STORE_ID, -1);
		final String selectedStoreName = this.getIntent()
				.getStringExtra(DBConstants.COL_STORE_NAME);

		// set the title to match activity
		this.textViewTitle = (TextView) this.findViewById(R.id.titleAddStore);
		this.textViewTitle.setText(R.string.title_edit_store);

		this.editTextStoreName = (EditText) this.findViewById(R.id.editTextNameAddStore);
		this.editTextStoreName.setText(selectedStoreName);
		this.editTextStoreName.addTextChangedListener(super
				.getTextWatcher(R.id.editTextNameAddStore));

		this.buttonConfirmEdit = (Button) this.findViewById(R.id.buttonConfirmAddStore);
		this.buttonConfirmEdit.setText(R.string.button_text_save);
		this.buttonConfirmEdit.setOnClickListener(new OnClickListener() {

			public void onClick(final View v) {
				if (EditStoreActivity.super.setErrorOnEmptyEditTexts(editTextIds)) {

					// check whether there is a store with this name already
					final Store alreadyExistingStore = EditStoreActivity.this.datasource
							.getStoreByName(EditStoreActivity.this.editTextStoreName.getText()
									.toString());

					if (alreadyExistingStore == null) {

						final Store storeToUpdate = new Store();
						storeToUpdate.setId(selectedStoreId);
						storeToUpdate.setName(EditStoreActivity.this.editTextStoreName.getText()
								.toString());

						EditStoreActivity.this.datasource.updateStore(storeToUpdate);
						EditStoreActivity.this.finish();

					} else {
						Toast.makeText(
								EditStoreActivity.this.context,
								EditStoreActivity.this.getString(R.string.msg_store_already_exists),
								Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {

		case android.R.id.home:
			Intent intent = new Intent(this, ManageStoresActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;

		default:
			break;
		}
		return false;
	}
}
