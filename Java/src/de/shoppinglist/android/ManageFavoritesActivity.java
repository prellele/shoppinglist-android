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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import de.shoppinglist.android.adapter.FavoriteAdapter;
import de.shoppinglist.android.bean.Favorite;
import de.shoppinglist.android.bean.FavoriteProductMapping;
import de.shoppinglist.android.bean.ShoppinglistProductMapping;
import de.shoppinglist.android.constant.DBConstants;
import de.shoppinglist.android.constant.GlobalValues;
import de.shoppinglist.android.datasource.ShoppinglistDataSource;

public class ManageFavoritesActivity extends AbstractShoppinglistActivity {

	private List<Favorite> allFavorites;

	private Context context;

	private ShoppinglistDataSource datasource;

	private ArrayAdapter<Favorite> favoriteListAdapter;

	private ListView listFavorites;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.manage_favorites);

		this.datasource = super.getDatasource();
		this.context = super.getContext();

		this.allFavorites = this.datasource.getAllFavorites();

		this.favoriteListAdapter = new FavoriteAdapter(this, this.allFavorites);

		this.listFavorites = (ListView) this.findViewById(R.id.listViewManageFavorites);
		this.listFavorites.setAdapter(this.favoriteListAdapter);

		this.listFavorites.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(final AdapterView<?> arg0, final View v, final int position,
					final long id) {

				final Favorite selectedFavorite = ManageFavoritesActivity.this.favoriteListAdapter
						.getItem(position);

				// switch to activity
				// EditFavoriteProductlist
				final Intent intentEditFavoriteProductList = new Intent(
						ManageFavoritesActivity.this.getApplicationContext(),
						EditFavoriteProductListActivity.class);

				intentEditFavoriteProductList.putExtra(DBConstants.COL_FAVORITE_ID,
						selectedFavorite.getId());
				intentEditFavoriteProductList.putExtra(DBConstants.COL_FAVORITE_NAME,
						selectedFavorite.getName());

				ManageFavoritesActivity.this.startActivityForResult(intentEditFavoriteProductList,
						0);
			}

		});

		this.listFavorites.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(final AdapterView<?> arg0, final View v,
					final int position, final long id) {
				// show popup menu
				final PopupMenu popup = new PopupMenu(ManageFavoritesActivity.this
						.getApplicationContext(), v);
				final MenuInflater inflater = popup.getMenuInflater();
				inflater.inflate(R.menu.popupmenu_manage_favorites, popup.getMenu());
				popup.show();

				// handle clicks on the popup-buttons
				popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {

					public boolean onMenuItemClick(final MenuItem item) {

						final Favorite selectedFavorite = ManageFavoritesActivity.this.favoriteListAdapter
								.getItem(position);

						switch (item.getItemId()) {

						case R.id.popupAddFavoritelistToShoppinglist:
							// add favoriteProductMappings to
							// shoppinglistProductMappings

							final List<FavoriteProductMapping> favoriteProductMappings = ManageFavoritesActivity.super
									.getDatasource().getFavoriteProductMappingsByFavoriteId(
											selectedFavorite.getId());

							// for each favoriteProductMapping check
							// whether there is already an existing
							// shoppinglistProductMapping
							for (final FavoriteProductMapping favoriteProductMapping : favoriteProductMappings) {
								final ShoppinglistProductMapping shoppinglistProductMapping = ManageFavoritesActivity.super
										.getDatasource()
										.checkWhetherShoppinglistProductMappingExists(
												favoriteProductMapping.getStore().getId(),
												favoriteProductMapping.getProduct().getId());

								if (shoppinglistProductMapping != null) {
									// update the quantity
									final Double quantityToUpdate = (Double
											.valueOf(favoriteProductMapping.getQuantity()) + (Double
											.valueOf(shoppinglistProductMapping.getQuantity())));
									ManageFavoritesActivity.this.datasource.updateShoppinglistProductMapping(
											shoppinglistProductMapping.getId(),
											shoppinglistProductMapping.getStore().getId(),
											shoppinglistProductMapping.getProduct().getId(),
											String.valueOf(quantityToUpdate));

								} else {
									// insert new mapping
									ManageFavoritesActivity.this.datasource
											.saveShoppingListProductMapping(favoriteProductMapping
													.getStore().getId(), favoriteProductMapping
													.getProduct().getId(), favoriteProductMapping
													.getQuantity(), GlobalValues.NO);

								}
							}

							return true;

						case R.id.popupEditFavoriteName:
							// switch to activity EditFavoriteActivity
							final Intent intentEditFavoriteName = new Intent(
									ManageFavoritesActivity.this.getApplicationContext(),
									EditFavoriteActivity.class);

							intentEditFavoriteName.putExtra(DBConstants.COL_FAVORITE_ID,
									selectedFavorite.getId());
							intentEditFavoriteName.putExtra(DBConstants.COL_FAVORITE_NAME,
									selectedFavorite.getName());

							ManageFavoritesActivity.this.startActivityForResult(
									intentEditFavoriteName, 0);
							return true;

						case R.id.popupDeleteFavorite:
							// delete from mapping
							final AlertDialog.Builder alertBox = new AlertDialog.Builder(
									ManageFavoritesActivity.this.context);
							alertBox.setMessage(ManageFavoritesActivity.this
									.getString(R.string.msg_really_delete_favoritelist));
							alertBox.setPositiveButton(
									ManageFavoritesActivity.this.getString(R.string.msg_yes),
									new OnClickListener() {

										public void onClick(final DialogInterface dialog,
												final int which) {

											// delete it
											ManageFavoritesActivity.this.datasource
													.deleteFavoriteAndItsMappings(selectedFavorite
															.getId());
											ManageFavoritesActivity.this.favoriteListAdapter
													.remove(selectedFavorite);

										}
									});

							alertBox.setNegativeButton(
									ManageFavoritesActivity.this.getString(R.string.msg_no),
									new OnClickListener() {

										public void onClick(final DialogInterface dialog,
												final int which) {
											// do nothing here
										}
									});

							alertBox.show();

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
		inflater.inflate(R.menu.actionbar_menu_manage_favorites, menu);
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

		case R.id.actionbarAddFavorite:
			// switch to the AddFavoriteActivity
			final Intent intentAddFavorite = new Intent(this, AddFavoriteActivity.class);
			this.startActivityForResult(intentAddFavorite, 0);
			break;

		default:
			break;
		}
		return false;
	}

	@Override
	public void onResume() {
		super.onResume();

		this.allFavorites = this.datasource.getAllFavorites();
		this.favoriteListAdapter = new FavoriteAdapter(this, this.allFavorites);
		this.listFavorites.setAdapter(this.favoriteListAdapter);
	}
}
