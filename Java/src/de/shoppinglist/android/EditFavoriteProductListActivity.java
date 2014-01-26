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
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import de.shoppinglist.android.adapter.FavoriteProductListAdapter;
import de.shoppinglist.android.bean.FavoriteProductMapping;
import de.shoppinglist.android.constant.DBConstants;
import de.shoppinglist.android.datasource.ShoppinglistDataSource;

public class EditFavoriteProductListActivity extends AbstractShoppinglistActivity {

	int selectedFavoriteId;

	private Context context;

	private ShoppinglistDataSource datasource;

	private FavoriteProductListAdapter favoriteProductListAdapter;

	private List<FavoriteProductMapping> favoriteProductMappings;

	private ListView listViewFavoriteProducts;

	private TextView titleTextView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.datasource = super.getDatasource();
		this.context = super.getContext();

		this.setContentView(R.layout.add_or_edit_favorite_product_list);

		// get values of calling activity
		this.selectedFavoriteId = this.getIntent().getIntExtra(DBConstants.COL_FAVORITE_ID, -1);
		final String selectedFavoriteName = this.getIntent().getStringExtra(
				DBConstants.COL_FAVORITE_NAME);

		this.titleTextView = (TextView) this.findViewById(R.id.titleAddProductToFavorite);
		this.titleTextView.setText(selectedFavoriteName);

		this.favoriteProductMappings = super.getDatasource()
				.getFavoriteProductMappingsByFavoriteId(this.selectedFavoriteId);

		this.favoriteProductListAdapter = new FavoriteProductListAdapter(this.context,
				this.favoriteProductMappings);

		this.listViewFavoriteProducts = (ListView) this.findViewById(R.id.listProductsInFavorite);
		this.listViewFavoriteProducts.setAdapter(this.favoriteProductListAdapter);

		this.listViewFavoriteProducts.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(final AdapterView<?> arg0, final View v,
					final int position, final long id) {
				// show popup menu
				final PopupMenu popup = new PopupMenu(EditFavoriteProductListActivity.this.context,
						v);
				final MenuInflater inflater = popup.getMenuInflater();
				inflater.inflate(R.menu.popupmenu_favorite_product_list, popup.getMenu());
				popup.show();

				// handle clicks on the popup-buttons
				popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {

					public boolean onMenuItemClick(final MenuItem item) {

						final FavoriteProductMapping selectedMapping = EditFavoriteProductListActivity.this.favoriteProductListAdapter
								.getItem(position);

						switch (item.getItemId()) {

						case R.id.popupEditFavoriteProductMapping:
							// switch to the
							// EditFavoriteProductMappingActivity
							final Intent intentEditFavoriteProductMapping = new Intent(
									EditFavoriteProductListActivity.this.context,
									EditFavoriteProductMappingActivity.class);

							// put the values of the mapping in the
							// intent, so they can used by the other
							// activity
							intentEditFavoriteProductMapping.putExtra(
									DBConstants.COL_FAVORITE_PRODUCT_MAPPING_ID,
									selectedMapping.getId());
							intentEditFavoriteProductMapping.putExtra(DBConstants.COL_FAVORITE_ID,
									selectedMapping.getFavorite().getId());
							intentEditFavoriteProductMapping.putExtra(
									DBConstants.COL_FAVORITE_PRODUCT_MAPPING_QUANTITY,
									selectedMapping.getQuantity());
							intentEditFavoriteProductMapping.putExtra(DBConstants.COL_UNIT_ID,
									selectedMapping.getProduct().getUnit().getId());
							intentEditFavoriteProductMapping.putExtra(DBConstants.COL_PRODUCT_NAME,
									selectedMapping.getProduct().getName());
							intentEditFavoriteProductMapping.putExtra(DBConstants.COL_PRODUCT_ID,
									selectedMapping.getProduct().getId());
							intentEditFavoriteProductMapping.putExtra(DBConstants.COL_STORE_ID,
									selectedMapping.getStore().getId());

							EditFavoriteProductListActivity.this.startActivityForResult(
									intentEditFavoriteProductMapping, 0);

							return true;

						case R.id.popupDeleteFavoriteProductMapping:
							// delete this mappping
							EditFavoriteProductListActivity.this.datasource
									.deleteFavoriteProductMapping(selectedMapping.getId());
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
		inflater.inflate(R.menu.actionbar_menu_edit_favorite_product_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {

		case android.R.id.home:
			Intent intent = new Intent(this, ManageFavoritesActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;

		case R.id.actionbarAddProductToFavorite:
			// switch to the AddFavoriteActivity
			final Intent intentAddProductToFavorite = new Intent(this,
					AddProductToFavoriteActivity.class);

			intentAddProductToFavorite.putExtra(DBConstants.COL_FAVORITE_ID,
					this.selectedFavoriteId);

			this.startActivityForResult(intentAddProductToFavorite, 0);
			break;

		default:
			break;
		}
		return false;
	}

	@Override
	public void onResume() {
		super.onResume();

		this.favoriteProductMappings = super.getDatasource()
				.getFavoriteProductMappingsByFavoriteId(this.selectedFavoriteId);
		this.favoriteProductListAdapter = new FavoriteProductListAdapter(this,
				this.favoriteProductMappings);
		this.listViewFavoriteProducts.setAdapter(this.favoriteProductListAdapter);
	}
}
