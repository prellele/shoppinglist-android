package de.shoppinglist.android;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import de.shoppinglist.android.adapter.StoreAdapter;
import de.shoppinglist.android.adapter.UnitAdapter;
import de.shoppinglist.android.bean.FavoriteProductMapping;
import de.shoppinglist.android.bean.Product;
import de.shoppinglist.android.bean.Store;
import de.shoppinglist.android.bean.Unit;
import de.shoppinglist.android.constant.DBConstants;
import de.shoppinglist.android.datasource.ShoppinglistDataSource;

public class AddProductToFavoriteActivity extends AbstractShoppinglistActivity {

	private Button buttonConfirmAddProduct;

	private ShoppinglistDataSource datasource;

	private EditText editTextProductName;

	private EditText editTextQuantity;

	private List<Integer> editTextIds = new LinkedList<Integer>(Arrays.asList(
			R.id.editTextQuantityAddProduct, R.id.editTextProductNameAutocomplete));

	private Spinner spinnerStores;

	private Spinner spinnerUnits;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.datasource = super.getDatasource();

		this.setContentView(R.layout.add_or_edit_product);

		// set title to match the activity
		final TextView titleView = (TextView) this.findViewById(R.id.titleEditOrAddProduct);
		titleView.setText(R.string.title_add_new_product_to_favorite);

		// get extra-values of intent
		final int selectedFavoriteId = this.getIntent()
				.getIntExtra(DBConstants.COL_FAVORITE_ID, -1);

		final List<Unit> units = super.getDatasource().getAllUnits();
		final List<Store> stores = super.getDatasource().getAllStores();

		this.spinnerUnits = (Spinner) this.findViewById(R.id.spinnerUnitAddProduct);
		final ArrayAdapter<Unit> spinnerUnitAdapter = new UnitAdapter(this, units);
		this.spinnerUnits.setAdapter(spinnerUnitAdapter);

		this.spinnerStores = (Spinner) this.findViewById(R.id.spinnerStoreAddProduct);
		final ArrayAdapter<Store> spinnerStoreAdapter = new StoreAdapter(this, stores);
		this.spinnerStores.setAdapter(spinnerStoreAdapter);

		this.editTextProductName = (EditText) this
				.findViewById(R.id.editTextProductNameAutocomplete);
		this.editTextProductName.addTextChangedListener(super
				.getTextWatcher(R.id.editTextProductNameAutocomplete));

		this.editTextQuantity = (EditText) this.findViewById(R.id.editTextQuantityAddProduct);
		this.editTextQuantity.addTextChangedListener(super
				.getTextWatcher(R.id.editTextQuantityAddProduct));

		this.buttonConfirmAddProduct = (Button) this.findViewById(R.id.buttonConfirmAddProduct);
		this.buttonConfirmAddProduct.setOnClickListener(new OnClickListener() {

			public void onClick(final View v) {
				if (AddProductToFavoriteActivity.super.setErrorOnEmptyEditTexts(editTextIds)) {

					final Store selectedStore = (Store) AddProductToFavoriteActivity.this.spinnerStores
							.getSelectedItem();
					final Unit selectedUnit = (Unit) AddProductToFavoriteActivity.this.spinnerUnits
							.getSelectedItem();
					final String productName = AddProductToFavoriteActivity.this.editTextProductName
							.getText().toString();
					final String quantity = AddProductToFavoriteActivity.this.editTextQuantity
							.getText().toString();

					Product product = AddProductToFavoriteActivity.this.datasource
							.getProductByNameAndUnit(productName, selectedUnit.getId());
					if (product == null) {
						AddProductToFavoriteActivity.this.datasource.saveProduct(productName,
								selectedUnit.getId());
						product = AddProductToFavoriteActivity.this.datasource
								.getProductByNameAndUnit(productName, selectedUnit.getId());
					}

					final FavoriteProductMapping alreadyExistingMapping = AddProductToFavoriteActivity.super
							.getDatasource().checkWhetherFavoriteProductMappingExists(
									selectedFavoriteId, selectedStore.getId(), product.getId());

					if (alreadyExistingMapping != null) {
						// JA: update quantity
						final double quantityToUpdate = Double.valueOf(alreadyExistingMapping
								.getQuantity()) + Double.valueOf(quantity);
						AddProductToFavoriteActivity.this.datasource.updateFavoriteProductMapping(
								alreadyExistingMapping.getId(), alreadyExistingMapping.getStore()
										.getId(), alreadyExistingMapping.getProduct().getId(),
								String.valueOf(quantityToUpdate));
					} else {
						// NEIN: insert new / save
						AddProductToFavoriteActivity.this.datasource.saveFavoriteProductMapping(
								selectedFavoriteId, selectedStore.getId(), product.getId(),
								quantity);
					}

					AddProductToFavoriteActivity.this.finish();
				}
			}

		});
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {

		case android.R.id.home:
			Intent intent = new Intent(this, ManageFavoritesActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;

		default:
			break;
		}
		return false;
	}
}
