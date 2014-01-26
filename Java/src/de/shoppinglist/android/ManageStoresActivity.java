package de.shoppinglist.android;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Toast;
import de.shoppinglist.android.adapter.StoreAdapter;
import de.shoppinglist.android.bean.Store;
import de.shoppinglist.android.constant.DBConstants;
import de.shoppinglist.android.datasource.ShoppinglistDataSource;

public class ManageStoresActivity extends AbstractShoppinglistActivity {

	private List<Store> allStores;

	private Context context;

	private ShoppinglistDataSource datasource;

	private ListView listStores;

	private ArrayAdapter<Store> storeListAdapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.datasource = super.getDatasource();
		this.context = super.getContext();

		this.setContentView(R.layout.manage_stores);

		this.allStores = this.datasource.getAllStores();

		this.storeListAdapter = new StoreAdapter(this, this.allStores);

		this.listStores = (ListView) this.findViewById(R.id.listViewManageStores);
		this.listStores.setAdapter(this.storeListAdapter);

		// handle long clicks on the stores
		this.listStores.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(final AdapterView<?> arg0, final View v,
					final int position, final long id) {
				final PopupMenu popup = new PopupMenu(ManageStoresActivity.this.context, v);
				final MenuInflater inflater = popup.getMenuInflater();
				inflater.inflate(R.menu.popupmenu_manage_stores, popup.getMenu());
				popup.show();
				// handle clicks on the popup-buttons
				popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {

					public boolean onMenuItemClick(final MenuItem item) {
						final Store selectedStore = ManageStoresActivity.this.storeListAdapter
								.getItem(position);

						switch (item.getItemId()) {

						case R.id.popupEditStore:
							// switch to the AddStoreActivity
							final Intent intentEditStore = new Intent(
									ManageStoresActivity.this.context, EditStoreActivity.class);

							// put the store attributes in here, so we
							// can show it in the edit-layout
							intentEditStore.putExtra(DBConstants.COL_STORE_ID,
									selectedStore.getId());
							intentEditStore.putExtra(DBConstants.COL_STORE_NAME,
									selectedStore.getName());

							ManageStoresActivity.this.startActivityForResult(intentEditStore, 0);
							break;

						case R.id.popupDeleteStore:
							// prüfen ob store in Benutzung
							// (fav_mapping, shop_mapping)
							// wenn in Benutzung, Toast. mit nachricht
							// wenn nicht, löschen
							if (ManageStoresActivity.this.datasource
									.checkWhetherStoreIsNotInUse(selectedStore.getId())) {
								if (ManageStoresActivity.this.storeListAdapter.getCount() > 1) {
									ManageStoresActivity.this.datasource.deleteStore(selectedStore
											.getId());
									ManageStoresActivity.this.storeListAdapter
											.remove(selectedStore);
								} else {
									Toast.makeText(
											ManageStoresActivity.this.context,
											ManageStoresActivity.this
													.getString(R.string.msg_last_store_cant_delete),
											Toast.LENGTH_SHORT).show();
								}
							} else {
								Toast.makeText(
										ManageStoresActivity.this.context,
										ManageStoresActivity.this
												.getString(R.string.msg_store_in_use_cant_delete),
										Toast.LENGTH_SHORT).show();
							}

							break;

						default:
							break;

						}
						return false;
					}

				});

				return false;
			}

		});
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		final MenuInflater inflater = this.getMenuInflater();
		inflater.inflate(R.menu.actionbar_menu_manage_stores, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {

		case android.R.id.home:
			Intent intent = new Intent(this, ShoppinglistActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;

		case R.id.actionbarAddStore:
			// switch to the AddStoreActivity
			final Intent intentAddStore = new Intent(this, AddStoreActivity.class);
			this.startActivityForResult(intentAddStore, 0);
			break;

		default:
			break;
		}
		return false;
	}

	@Override
	public void onResume() {
		super.onResume();

		this.allStores = this.datasource.getAllStores();
		this.storeListAdapter = new StoreAdapter(this, this.allStores);
		this.listStores.setAdapter(this.storeListAdapter);
	}
}
