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
import android.widget.Toast;
import de.shoppinglist.android.bean.Store;
import de.shoppinglist.android.datasource.ShoppinglistDataSource;

public class AddStoreActivity extends AbstractShoppinglistActivity {

	private Button buttonAddStore;

	private Context context;

	private ShoppinglistDataSource datasource;

	private EditText editTextStoreName;

	private List<Integer> editTextIds = new LinkedList<Integer>(
			Arrays.asList(R.id.editTextNameAddStore));

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.datasource = super.getDatasource();
		this.context = super.getContext();

		this.setContentView(R.layout.add_or_edit_store);

		this.editTextStoreName = (EditText) this.findViewById(R.id.editTextNameAddStore);
		this.editTextStoreName.addTextChangedListener(super
				.getTextWatcher(R.id.editTextNameAddStore));

		this.buttonAddStore = (Button) this.findViewById(R.id.buttonConfirmAddStore);
		this.buttonAddStore.setOnClickListener(new OnClickListener() {

			public void onClick(final View v) {
				if (AddStoreActivity.super.setErrorOnEmptyEditTexts(editTextIds)) {

					// check whether there is already a store with this name
					final Store alreadyExistingStore = AddStoreActivity.this.datasource
							.getStoreByName(AddStoreActivity.this.editTextStoreName.getText()
									.toString());

					if (alreadyExistingStore == null) {
						// save new store, when there is no store with this name
						AddStoreActivity.this.datasource
								.saveStore(AddStoreActivity.this.editTextStoreName.getText()
										.toString());
						AddStoreActivity.this.finish();

					} else {
						Toast.makeText(AddStoreActivity.this.context,
								AddStoreActivity.this.getString(R.string.msg_store_already_exists),
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
