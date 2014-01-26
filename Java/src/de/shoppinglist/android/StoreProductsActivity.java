package de.shoppinglist.android;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import de.shoppinglist.android.adapter.ShoppinglistProductMappingAdapter;
import de.shoppinglist.android.bean.ShoppinglistProductMapping;
import de.shoppinglist.android.bean.Store;
import de.shoppinglist.android.constant.ConfigurationConstants;
import de.shoppinglist.android.constant.DBConstants;
import de.shoppinglist.android.constant.GlobalValues;
import de.shoppinglist.android.datasource.ShoppinglistDataSource;
import de.shoppinglist.android.helper.ProcessColorHelper;

public class StoreProductsActivity extends AbstractShoppinglistActivity {

	int clickedStoreId;

	TextView storeProductsTitleTextView;

	private Context context;

	private ShoppinglistDataSource datasource;

	private TextView labelProcessStoreProducts;

	private ListView listShoppinglistProductMapping;

	private ShoppinglistProductMappingAdapter shoppinglistProductMappingAdapter;

	private List<ShoppinglistProductMapping> shoppinglistProductMappings;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.datasource = super.getDatasource();
		this.context = super.getContext();

		this.setContentView(R.layout.store_products);

		// get the name and the id of the clicked store
		this.clickedStoreId = this.getIntent().getIntExtra(DBConstants.COL_STORE_ID, -1);
		final String clickedStoreName = this.getIntent().getStringExtra(DBConstants.COL_STORE_NAME);

		// update the title - show the clicked/shown store
		String updateTitle = this.getResources().getString(R.string.store_products_view);
		updateTitle += " " + clickedStoreName;
		this.storeProductsTitleTextView = ((TextView) this.findViewById(R.id.storeProductsTextView));
		this.storeProductsTitleTextView.setText(updateTitle);

		this.shoppinglistProductMappings = this.datasource
				.getProductsOnShoppingList(this.clickedStoreId);

		// show the process
		this.setProcessTextInStoreView();

		// show the products in the view
		this.shoppinglistProductMappingAdapter = new ShoppinglistProductMappingAdapter(this,
				this.shoppinglistProductMappings);
		this.listShoppinglistProductMapping = (ListView) this
				.findViewById(R.id.listShoppinglistProductMappingsStore);
		this.listShoppinglistProductMapping.setAdapter(this.shoppinglistProductMappingAdapter);

		// handle "normal" clicks on shoppinglistItems -> mark them as checked
		this.listShoppinglistProductMapping.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(final AdapterView<?> arg0, final View v, final int position,
					final long id) {

				final ShoppinglistProductMapping clickedMapping = StoreProductsActivity.this.shoppinglistProductMappingAdapter
						.getItem(position);

				if (clickedMapping.isChecked() == GlobalValues.NO) {

					StoreProductsActivity.this.shoppinglistProductMappings.get(
							StoreProductsActivity.this.shoppinglistProductMappings
									.indexOf(clickedMapping)).setChecked(GlobalValues.YES);
					StoreProductsActivity.this.datasource
							.markShoppinglistProductMappingAsChecked(clickedMapping.getId());
				} else if (clickedMapping.isChecked() == GlobalValues.YES) {

					StoreProductsActivity.this.shoppinglistProductMappings.get(
							StoreProductsActivity.this.shoppinglistProductMappings
									.indexOf(clickedMapping)).setChecked(GlobalValues.NO);
					StoreProductsActivity.this.datasource
							.markShoppinglistProductMappingAsUnchecked(clickedMapping.getId());
				}

				StoreProductsActivity.this.shoppinglistProductMappingAdapter.notifyDataSetChanged();

				// update the process
				StoreProductsActivity.this.setProcessTextInStoreView();
			}
		});

		// handle long-clicks on shoppinglistItems
		this.listShoppinglistProductMapping
				.setOnItemLongClickListener(new OnItemLongClickListener() {

					public boolean onItemLongClick(final AdapterView<?> arg0, final View v,
							final int position, final long id) {

						final PopupMenu popup = new PopupMenu(StoreProductsActivity.this.context, v);
						final MenuInflater inflater = popup.getMenuInflater();
						inflater.inflate(R.menu.popupmenu_products_overview, popup.getMenu());
						popup.show();
						// handle clicks on the popup-buttons
						popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {

							public boolean onMenuItemClick(final MenuItem item) {
								ShoppinglistProductMapping shoppinglistProductMapping = StoreProductsActivity.this.shoppinglistProductMappingAdapter
										.getItem(position);

								switch (item.getItemId()) {

								// buttonEditProduct - Popup (longClick)
								case R.id.popupEditProduct:

									// switch to the addProductActivity
									final Intent intent = new Intent(
											StoreProductsActivity.this.context,
											EditProductActivity.class);

									// put the values of the mapping in the
									// intent, so they can used by the other
									// activity
									intent.putExtra(
											DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_ID,
											shoppinglistProductMapping.getId());
									intent.putExtra(
											DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_QUANTITY,
											shoppinglistProductMapping.getQuantity());
									intent.putExtra(DBConstants.COL_UNIT_ID,
											shoppinglistProductMapping.getProduct().getUnit()
													.getId());
									intent.putExtra(DBConstants.COL_PRODUCT_NAME,
											shoppinglistProductMapping.getProduct().getName());
									intent.putExtra(DBConstants.COL_PRODUCT_ID,
											shoppinglistProductMapping.getProduct().getId());
									intent.putExtra(DBConstants.COL_STORE_ID,
											shoppinglistProductMapping.getStore().getId());

									StoreProductsActivity.this.startActivityForResult(intent, 0);

									return true;

									// buttonDeleteProduct - Popup (longClick)
								case R.id.popupDeleteProduct:
									// delete from mapping
									shoppinglistProductMapping = StoreProductsActivity.this.shoppinglistProductMappingAdapter
											.getItem(position);
									StoreProductsActivity.this.datasource
											.deleteShoppinglistProductMapping(shoppinglistProductMapping
													.getId());
									StoreProductsActivity.this.shoppinglistProductMappingAdapter
											.remove(shoppinglistProductMapping);

									if (StoreProductsActivity.this.shoppinglistProductMappingAdapter
											.getCount() == 0) {
										StoreProductsActivity.this.finish();
									} else {
										// update the process
										StoreProductsActivity.this.setProcessTextInStoreView();
									}

									return true;
								default:
									return false;
								}

							}

						});

						return false;
					}

				});
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		final MenuInflater inflater = this.getMenuInflater();
		inflater.inflate(R.menu.actionbar_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {

		case android.R.id.home:
			final Intent intentHome = new Intent(this, ShoppinglistActivity.class);
			intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intentHome);
			break;

		// AddProductbutton - Actionbar
		case R.id.actionbarAddProduct:
			// switch to the addProductActivity
			final Intent intent = new Intent(this, AddProductActivity.class);
			this.startActivityForResult(intent, 0);
			break;

		// ManageStoresButton - Actionbar
		case R.id.actionbarManageStores:
			final Intent intentManageStores = new Intent(this, ManageStoresActivity.class);
			this.startActivityForResult(intentManageStores, 0);
			break;

		// ManageUnitsButton - Actionbar
		case R.id.actionbarManageUnits:
			final Intent intentManageUnits = new Intent(this, ManageUnitsActivity.class);
			this.startActivityForResult(intentManageUnits, 0);
			break;

		// ManageFavoritesButton - Actionbar
		case R.id.actionbarManageFavorites:
			final Intent intentManageFavorites = new Intent(this, ManageFavoritesActivity.class);
			this.startActivityForResult(intentManageFavorites, 0);
			break;

		// deleteShoppinglistMappingsButton - Actionbar
		case R.id.actionbarDeleteShoppinglist:
			final AlertDialog.Builder alertBox = new AlertDialog.Builder(this);
			alertBox.setMessage(this.getString(R.string.msg_really_delete_shoppinglist));
			alertBox.setPositiveButton(this.getString(R.string.msg_yes), new OnClickListener() {

				public void onClick(final DialogInterface dialog, final int which) {
					StoreProductsActivity.this.datasource.deleteAllShoppinglistProductMappings();
					StoreProductsActivity.this.datasource.createNewShoppinglist();
					StoreProductsActivity.this.refreshLayout();
				}
			});

			alertBox.setNegativeButton(this.getString(R.string.msg_no), new OnClickListener() {

				public void onClick(final DialogInterface dialog, final int which) {
					// do nothing here
				}
			});

			alertBox.show();

			break;

		// ViewHistory - Actionbar
		case R.id.actionbarShowHistory:
			// switch to the UserConfigurationActivity
			final Intent intentHistoryOverview = new Intent(this, ShowHistoryOverviewActivity.class);
			this.startActivityForResult(intentHistoryOverview, 0);
			break;

		// OptionsMenu - Actionbar
		case R.id.actionbarOptions:
			// switch to the UserConfigurationActivity
			final Intent intentUserConfiguration = new Intent(this, UserConfigurationActivity.class);
			this.startActivityForResult(intentUserConfiguration, 0);
			break;

		default:
			break;
		}

		return false;
	}

	@Override
	public void onResume() {
		super.onResume();
		this.refreshLayout();
	}

	private void refreshLayout() {
		if (this.datasource.getUserConfigurationViewType() == ConfigurationConstants.ALPHABETICALLY_VIEW) {
			this.finish();
		} else {

			this.shoppinglistProductMappings = this.datasource
					.getProductsOnShoppingList(this.clickedStoreId);
			this.shoppinglistProductMappingAdapter = new ShoppinglistProductMappingAdapter(this,
					this.shoppinglistProductMappings);
			this.listShoppinglistProductMapping.setAdapter(this.shoppinglistProductMappingAdapter);

			// May the storeName has changed - so update title
			final Store store = this.datasource.getStoreById(this.clickedStoreId);

			// update the title - show the clicked/shown store
			String updateTitle = this.getResources().getString(R.string.store_products_view);
			updateTitle += " \"" + store.getName() + " \"";
			this.storeProductsTitleTextView.setText(updateTitle);

			if (this.shoppinglistProductMappingAdapter.getCount() == 0) {
				this.finish();
			} else {
				// update the process
				this.setProcessTextInStoreView();
			}
		}
	}

	private void setProcessTextInStoreView() {
		// update the title with the actual status
		final int allMappingsCount = this.shoppinglistProductMappings.size();
		int checkedMappingsCount = 0;

		for (final ShoppinglistProductMapping mapping : this.shoppinglistProductMappings) {
			if (mapping.isChecked() == GlobalValues.YES) {
				checkedMappingsCount++;
			}
		}

		final int colorToShow = ProcessColorHelper.getColorForProcess(checkedMappingsCount,
				allMappingsCount);

		this.labelProcessStoreProducts = (TextView) this
				.findViewById(R.id.labelStoreProductsStatus);
		this.labelProcessStoreProducts.setText("( " + checkedMappingsCount + " / "
				+ allMappingsCount + " )");
		this.labelProcessStoreProducts.setTextColor(colorToShow);
	}
}
