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
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import de.shoppinglist.android.adapter.ShoppinglistProductMappingAdapter;
import de.shoppinglist.android.adapter.StoreAdapter;
import de.shoppinglist.android.bean.ShoppinglistProductMapping;
import de.shoppinglist.android.bean.Store;
import de.shoppinglist.android.constant.ConfigurationConstants;
import de.shoppinglist.android.constant.DBConstants;
import de.shoppinglist.android.constant.GlobalValues;
import de.shoppinglist.android.datasource.ShoppinglistDataSource;
import de.shoppinglist.android.helper.ProcessColorHelper;

public class ShoppinglistActivity extends AbstractShoppinglistActivity {

	private Button buttonAddToHistoryAlphabeticallyView;

	private Button buttonAddToHistoryStoreView;

	private Context context;

	private ShoppinglistDataSource datasource;

	private TextView labelProcessAlphabetically;

	private ListView listAlphabetically;

	private ListView listStore;

	private ShoppinglistProductMappingAdapter shoppinglistMappingAdapter;

	private List<ShoppinglistProductMapping> shoppinglistProductMappingsToShow;

	private StoreAdapter storeListAdapter;

	private List<Store> storesToShowInOverview;

	private int viewType;

	/**
	 * because this activity is the "Home" of the app, but we have two different
	 * viewTypes, here are the actions to perform when the viewtype =
	 * alphabetically
	 */
	public void actionsToPerformInAlphabeticallyViewType() {
		// get the mappings (storeId = -1 because there's no store specified)
		this.shoppinglistProductMappingsToShow = this.datasource.getProductsOnShoppingList(-1);

		this.shoppinglistMappingAdapter = new ShoppinglistProductMappingAdapter(this,
				this.shoppinglistProductMappingsToShow);

		// show the process
		this.setProcessTextInAlphabeticallyView();

		// show historybutton?
		this.setVisibilityOfHistoryButton();

		this.listAlphabetically = (ListView) this
				.findViewById(R.id.listShoppinglistProductMappingsAlphabetically);
		this.listAlphabetically.setAdapter(this.shoppinglistMappingAdapter);

		// handle clicks on addToHistory button
		this.buttonAddToHistoryAlphabeticallyView = (Button) this
				.findViewById(R.id.buttonAddToHistoryAlphabetOverview);
		this.buttonAddToHistoryAlphabeticallyView.setOnClickListener(new View.OnClickListener() {

			public void onClick(final View v) {
				final AlertDialog.Builder alertBox = new AlertDialog.Builder(
						ShoppinglistActivity.this.context);
				alertBox.setMessage(ShoppinglistActivity.this
						.getString(R.string.msg_really_add_shoppinglist_to_history));
				alertBox.setPositiveButton(ShoppinglistActivity.this.getString(R.string.msg_yes),
						new OnClickListener() {

							public void onClick(final DialogInterface dialog, final int which) {
								ShoppinglistActivity.this.datasource.addAllToHistory();
								ShoppinglistActivity.this.datasource
										.deleteAllShoppinglistProductMappings();
								ShoppinglistActivity.this.datasource.createNewShoppinglist();
								ShoppinglistActivity.this.refreshLayout();
							}
						});

				alertBox.setNegativeButton(ShoppinglistActivity.this.getString(R.string.msg_no),
						new OnClickListener() {

							public void onClick(final DialogInterface dialog, final int which) {
								// do nothing here
							}
						});

				alertBox.show();
			}

		});

		// handle long clicks on the list items
		this.listAlphabetically.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(final AdapterView<?> arg0, final View v,
					final int position, final long id) {
				final PopupMenu popup = new PopupMenu(ShoppinglistActivity.this.context, v);
				final MenuInflater inflater = popup.getMenuInflater();
				inflater.inflate(R.menu.popupmenu_products_overview, popup.getMenu());
				popup.show();

				// handle clicks on the popup-buttons
				popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {

					public boolean onMenuItemClick(final MenuItem item) {
						ShoppinglistProductMapping shoppinglistProductMapping = ShoppinglistActivity.this.shoppinglistMappingAdapter
								.getItem(position);

						switch (item.getItemId()) {

						// buttonEditProduct - Popup (longClick)
						case R.id.popupEditProduct:

							// switch to the addProductActivity
							final Intent intent = new Intent(ShoppinglistActivity.this.context,
									EditProductActivity.class);

							// put the values of the mapping in the
							// intent, so they can used by the other
							// activity
							intent.putExtra(DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_ID,
									shoppinglistProductMapping.getId());
							intent.putExtra(DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_QUANTITY,
									shoppinglistProductMapping.getQuantity());
							intent.putExtra(DBConstants.COL_UNIT_ID, shoppinglistProductMapping
									.getProduct().getUnit().getId());
							intent.putExtra(DBConstants.COL_PRODUCT_NAME,
									shoppinglistProductMapping.getProduct().getName());
							intent.putExtra(DBConstants.COL_STORE_ID, shoppinglistProductMapping
									.getStore().getId());

							ShoppinglistActivity.this.startActivityForResult(intent, 0);

							// show historybutton?
							ShoppinglistActivity.this.setVisibilityOfHistoryButton();

							return true;

							// buttonDeleteProduct - Popup (longClick)
						case R.id.popupDeleteProduct:
							// delete from mapping
							shoppinglistProductMapping = ShoppinglistActivity.this.shoppinglistMappingAdapter
									.getItem(position);
							ShoppinglistActivity.this.datasource
									.deleteShoppinglistProductMapping(shoppinglistProductMapping
											.getId());
							ShoppinglistActivity.this.shoppinglistMappingAdapter
									.remove(shoppinglistProductMapping);

							// update the process
							ShoppinglistActivity.this.setProcessTextInAlphabeticallyView();

							// show historybutton?
							ShoppinglistActivity.this.setVisibilityOfHistoryButton();

							return true;

						default:
							return false;
						}
					}
				});

				return false;
			}

		});

		// handle "normal" clicks on the list items
		this.listAlphabetically.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(final AdapterView<?> arg0, final View v, final int position,
					final long id) {

				final ShoppinglistProductMapping clickedMapping = ShoppinglistActivity.this.shoppinglistMappingAdapter
						.getItem(position);

				if (clickedMapping.isChecked() == GlobalValues.NO) {

					ShoppinglistActivity.this.shoppinglistProductMappingsToShow.get(
							ShoppinglistActivity.this.shoppinglistProductMappingsToShow
									.indexOf(clickedMapping)).setChecked(GlobalValues.YES);
					ShoppinglistActivity.this.datasource
							.markShoppinglistProductMappingAsChecked(clickedMapping.getId());
				} else if (clickedMapping.isChecked() == GlobalValues.YES) {

					ShoppinglistActivity.this.shoppinglistProductMappingsToShow.get(
							ShoppinglistActivity.this.shoppinglistProductMappingsToShow
									.indexOf(clickedMapping)).setChecked(GlobalValues.NO);
					ShoppinglistActivity.this.datasource
							.markShoppinglistProductMappingAsUnchecked(clickedMapping.getId());
				}

				ShoppinglistActivity.this.shoppinglistMappingAdapter.notifyDataSetChanged();

				// update the process
				ShoppinglistActivity.this.setProcessTextInAlphabeticallyView();

				// show historybutton?
				ShoppinglistActivity.this.setVisibilityOfHistoryButton();
			}

		});
	}

	/**
	 * because this activity is the "Home" of the app, but we have two different
	 * viewTypes, here are the actions to perform when the viewtype = store
	 */
	public void actionsToPerformInStoreViewType() {

		// show the stores in the view
		this.storesToShowInOverview = this.datasource.getStoresForOverview();

		this.storeListAdapter = new StoreAdapter(this, this.storesToShowInOverview);

		this.listStore = (ListView) this.findViewById(R.id.listViewStore);
		this.listStore.setAdapter(this.storeListAdapter);

		// show historybutton?
		this.setVisibilityOfHistoryButton();

		// handle clicks on addToHistory button
		this.buttonAddToHistoryStoreView = (Button) this
				.findViewById(R.id.buttonAddToHistoryStoreOverview);
		this.buttonAddToHistoryStoreView.setOnClickListener(new View.OnClickListener() {

			public void onClick(final View v) {
				final AlertDialog.Builder alertBox = new AlertDialog.Builder(
						ShoppinglistActivity.this.context);
				alertBox.setMessage(ShoppinglistActivity.this
						.getString(R.string.msg_really_add_shoppinglist_to_history));
				alertBox.setPositiveButton(ShoppinglistActivity.this.getString(R.string.msg_yes),
						new OnClickListener() {

							public void onClick(final DialogInterface dialog, final int which) {
								ShoppinglistActivity.this.datasource.addAllToHistory();
								ShoppinglistActivity.this.datasource
										.deleteAllShoppinglistProductMappings();
								ShoppinglistActivity.this.datasource.createNewShoppinglist();
								ShoppinglistActivity.this.refreshLayout();
							}
						});

				alertBox.setNegativeButton(ShoppinglistActivity.this.getString(R.string.msg_no),
						new OnClickListener() {

							public void onClick(final DialogInterface dialog, final int which) {
								// do nothing here
							}
						});

				alertBox.show();
			}

		});

		// handle long clicks on the list items
		this.listStore.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(final AdapterView<?> arg0, final View v,
					final int position, final long id) {

				// show popup menu
				final PopupMenu popup = new PopupMenu(ShoppinglistActivity.this.context, v);
				final MenuInflater inflater = popup.getMenuInflater();
				inflater.inflate(R.menu.popupmenu_store_overview, popup.getMenu());
				popup.show();

				// handle clicks on the popup-buttons
				popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {

					public boolean onMenuItemClick(final MenuItem item) {

						switch (item.getItemId()) {
						case R.id.popupDeleteStoreEntries:
							// delete from mapping
							final Store storeToDeleteProductsFrom = ShoppinglistActivity.this.storeListAdapter
									.getItem(position);
							ShoppinglistActivity.this.datasource
									.deleteProductsFromStoreList(storeToDeleteProductsFrom.getId());
							ShoppinglistActivity.this.storeListAdapter
									.remove(storeToDeleteProductsFrom);

							return true;
						default:
							return false;
						}
					}

				});

				return false;
			}

		});

		// handle "normal" clicks on the list items
		this.listStore.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(final AdapterView<?> arg0, final View v, final int position,
					final long id) {

				final Store clickedStore = ShoppinglistActivity.this.storeListAdapter
						.getItem(position);

				// call another Activity to show the products of the clicked
				// store
				final Intent intent = new Intent(v.getContext(), StoreProductsActivity.class);
				intent.putExtra(DBConstants.COL_STORE_ID, clickedStore.getId());
				intent.putExtra(DBConstants.COL_STORE_NAME, clickedStore.getName());
				ShoppinglistActivity.this.startActivityForResult(intent, 0);
			}

		});
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.context = super.getContext();
		this.datasource = super.getDatasource();
		this.refreshLayout();
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

		// ViewHistory - Actionbar
		case R.id.actionbarShowHistory:
			// switch to the UserConfigurationActivity
			final Intent intentHistoryOverview = new Intent(this, ShowHistoryOverviewActivity.class);
			this.startActivityForResult(intentHistoryOverview, 0);
			break;

		// deleteShoppinglistMappingsButton - Actionbar
		case R.id.actionbarDeleteShoppinglist:
			final AlertDialog.Builder alertBox = new AlertDialog.Builder(this);
			alertBox.setMessage(this.getString(R.string.msg_really_delete_shoppinglist));
			alertBox.setPositiveButton(this.getString(R.string.msg_yes), new OnClickListener() {

				public void onClick(final DialogInterface dialog, final int which) {
					ShoppinglistActivity.this.datasource.deleteAllShoppinglistProductMappings();
					ShoppinglistActivity.this.datasource.createNewShoppinglist();
					ShoppinglistActivity.this.refreshLayout();
				}
			});

			alertBox.setNegativeButton(this.getString(R.string.msg_no), new OnClickListener() {

				public void onClick(final DialogInterface dialog, final int which) {
					// do nothing here
				}
			});

			alertBox.show();

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

	/**
	 * refreshes the whole layout (incl. data)
	 */
	private void refreshLayout() {
		this.setViewType();

		if (this.viewType == ConfigurationConstants.ALPHABETICALLY_VIEW) {

			this.setContentView(R.layout.overview_alphabet);
			this.actionsToPerformInAlphabeticallyViewType();

			// update the process
			this.setProcessTextInAlphabeticallyView();

		} else if (this.viewType == ConfigurationConstants.STORE_VIEW) {

			this.setContentView(R.layout.overview_store);
			this.actionsToPerformInStoreViewType();

		}
	}

	/**
	 * sets the textView with the actual process in alphabetically View
	 */
	private void setProcessTextInAlphabeticallyView() {
		// update the title with the actual status
		final int allMappingsCount = this.shoppinglistProductMappingsToShow.size();
		int checkedMappingsCount = 0;

		for (final ShoppinglistProductMapping mapping : this.shoppinglistProductMappingsToShow) {
			if (mapping.isChecked() == GlobalValues.YES) {
				checkedMappingsCount++;
			}
		}

		final int colorToShow = ProcessColorHelper.getColorForProcess(checkedMappingsCount,
				allMappingsCount);

		this.labelProcessAlphabetically = (TextView) this
				.findViewById(R.id.labelAlphabeticallyOverviewStatus);
		this.labelProcessAlphabetically.setText("( " + checkedMappingsCount + " / "
				+ allMappingsCount + " )");
		this.labelProcessAlphabetically.setTextColor(colorToShow);
	}

	/**
	 * sets the viewType
	 */
	private void setViewType() {
		this.viewType = this.datasource.getUserConfigurationViewType();
	}

	/**
	 * sets the visibility of buttonAddToHistory TRUE when all items are checked
	 */
	private void setVisibilityOfHistoryButton() {
		// update the title with the actual status
		int allMappingsCount = 0;
		int checkedMappingsCount = 0;
		this.setViewType();

		if (this.viewType == ConfigurationConstants.STORE_VIEW) {
			for (final Store store : this.storesToShowInOverview) {
				allMappingsCount = allMappingsCount + store.getCountProducts();
				checkedMappingsCount = checkedMappingsCount + store.getAlreadyCheckedProducts();
			}

			this.buttonAddToHistoryStoreView = (Button) this
					.findViewById(R.id.buttonAddToHistoryStoreOverview);

			if ((allMappingsCount > 0) && (checkedMappingsCount == allMappingsCount)) {
				this.buttonAddToHistoryStoreView.setVisibility(View.VISIBLE);
			} else {
				this.buttonAddToHistoryStoreView.setVisibility(View.INVISIBLE);
			}

		} else if (this.viewType == ConfigurationConstants.ALPHABETICALLY_VIEW) {
			allMappingsCount = this.shoppinglistProductMappingsToShow.size();
			for (final ShoppinglistProductMapping mapping : this.shoppinglistProductMappingsToShow) {
				if (mapping.isChecked() == GlobalValues.YES) {
					checkedMappingsCount++;
				}
			}

			this.buttonAddToHistoryAlphabeticallyView = (Button) this
					.findViewById(R.id.buttonAddToHistoryAlphabetOverview);

			if ((allMappingsCount > 0) && (checkedMappingsCount == allMappingsCount)) {
				this.buttonAddToHistoryAlphabeticallyView.setVisibility(View.VISIBLE);
			} else {
				this.buttonAddToHistoryAlphabeticallyView.setVisibility(View.INVISIBLE);
			}
		}

	}
}