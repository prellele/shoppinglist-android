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

public class EditFavoriteProductMappingActivity extends AbstractShoppinglistActivity {

	private Button buttonConfirmEditFavoriteProductMapping;

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
		titleView.setText(R.string.title_edit_product);

		final List<Unit> units = this.datasource.getAllUnits();
		final List<Store> stores = this.datasource.getAllStores();

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

		// set the values of the calling activity (clicked mapping)

		// quantity (EditText)
		final String clickedMappingQuantity = this.getIntent().getStringExtra(
				DBConstants.COL_FAVORITE_PRODUCT_MAPPING_QUANTITY);
		this.editTextQuantity.setText(clickedMappingQuantity);

		// unit (Spinner)
		final int clickedMappingUnitId = this.getIntent().getIntExtra(DBConstants.COL_UNIT_ID, -1);
		for (final Unit unit : units) {
			if (unit.getId() == clickedMappingUnitId) {
				this.spinnerUnits.setSelection(spinnerUnitAdapter.getPosition(unit));
			}
		}

		// productName and Id (EditText)
		final String clickedMappingProductName = this.getIntent().getStringExtra(
				DBConstants.COL_PRODUCT_NAME);
		final int clickedMappingProductId = this.getIntent().getIntExtra(
				DBConstants.COL_PRODUCT_ID, -1);
		this.editTextProductName.setText(clickedMappingProductName);

		// Store (Spinner)
		final int clickedMappingStoreId = this.getIntent()
				.getIntExtra(DBConstants.COL_STORE_ID, -1);

		for (final Store store : stores) {
			if (store.getId() == clickedMappingStoreId) {
				this.spinnerStores.setSelection(spinnerStoreAdapter.getPosition(store));
			}
		}

		// FavoriteProductMappingId
		final int clickedMappingId = this.getIntent().getIntExtra(
				DBConstants.COL_FAVORITE_PRODUCT_MAPPING_ID, -1);

		// FavoriteId
		final int clickedMappingFavoriteId = this.getIntent().getIntExtra(
				DBConstants.COL_FAVORITE_ID, -1);

		this.buttonConfirmEditFavoriteProductMapping = (Button) this
				.findViewById(R.id.buttonConfirmAddProduct);
		this.buttonConfirmEditFavoriteProductMapping.setText(R.string.button_text_save);

		this.buttonConfirmEditFavoriteProductMapping.setOnClickListener(new OnClickListener() {

			public void onClick(final View v) {
				if (EditFavoriteProductMappingActivity.super.setErrorOnEmptyEditTexts(editTextIds)) {

					// prüfen, ob Produkt in Benutzung, wenn nicht updaten,
					// sonst
					// neu anlegen
					//
					final String quantity = EditFavoriteProductMappingActivity.this.editTextQuantity
							.getText().toString();
					final Unit selectedUnit = (Unit) EditFavoriteProductMappingActivity.this.spinnerUnits
							.getSelectedItem();
					final String productName = EditFavoriteProductMappingActivity.this.editTextProductName
							.getText().toString();
					final Store selectedStore = (Store) EditFavoriteProductMappingActivity.this.spinnerStores
							.getSelectedItem();

					if (!clickedMappingProductName.equals(productName)
							|| (clickedMappingUnitId != selectedUnit.getId())) {
						// product has changed - check whether the product
						// already
						// exist

						// delete old mapping
						EditFavoriteProductMappingActivity.this.datasource
								.deleteFavoriteProductMapping(clickedMappingId);

						final Product alreadyExistingProduct = EditFavoriteProductMappingActivity.this.datasource
								.getProductByNameAndUnit(productName, selectedUnit.getId());

						// delete "old" product, when it's not in use
						if (EditFavoriteProductMappingActivity.this.datasource
								.checkWhetherProductIsNotInUse(clickedMappingProductId)) {
							EditFavoriteProductMappingActivity.this.datasource
									.deleteProduct(clickedMappingProductId);
						}

						if (alreadyExistingProduct != null) {
							// new Product exist - check whether there is a
							// mapping
							// for this product

							final FavoriteProductMapping alreadyExistingMapping = EditFavoriteProductMappingActivity.super
									.getDatasource().checkWhetherFavoriteProductMappingExists(
											clickedMappingFavoriteId, selectedStore.getId(),
											alreadyExistingProduct.getId());

							if (alreadyExistingMapping != null) {
								// already existing mapping - update
								// quantity
								// (old + new)
								final double newQuantity = Double.valueOf(alreadyExistingMapping
										.getQuantity()) + Double.valueOf(quantity);
								EditFavoriteProductMappingActivity.this.datasource
										.updateFavoriteProductMapping(
												alreadyExistingMapping.getId(),
												alreadyExistingMapping.getStore().getId(),
												alreadyExistingProduct.getId(),
												String.valueOf(newQuantity));

							} else {
								// already existing mapping NOT exist -
								// insert new
								// mapping
								EditFavoriteProductMappingActivity.this.datasource
										.saveFavoriteProductMapping(clickedMappingFavoriteId,
												selectedStore.getId(),
												alreadyExistingProduct.getId(), quantity);
							}

						} else {
							// new Product not exist
							EditFavoriteProductMappingActivity.this.datasource.saveProduct(
									productName, selectedUnit.getId());
							final Product newProduct = EditFavoriteProductMappingActivity.this.datasource
									.getProductByNameAndUnit(productName, selectedUnit.getId());

							EditFavoriteProductMappingActivity.this.datasource
									.saveFavoriteProductMapping(clickedMappingFavoriteId,
											selectedStore.getId(), newProduct.getId(), quantity);

						}

					} else {
						// product has not changed - check whether there is
						// an
						// existing mapping (pro_id + sto_id)
						final FavoriteProductMapping alreadyExistingMapping = EditFavoriteProductMappingActivity.super
								.getDatasource().checkWhetherFavoriteProductMappingExists(
										clickedMappingFavoriteId, selectedStore.getId(),
										clickedMappingProductId);

						if (alreadyExistingMapping != null) {
							// already existing mapping - update quantity
							// (old + new)

							if (clickedMappingStoreId != alreadyExistingMapping.getStore().getId()) {
								// delete old mapping
								EditFavoriteProductMappingActivity.this.datasource
										.deleteFavoriteProductMapping(clickedMappingId);

							}

							final double newQuantity = Double.valueOf(alreadyExistingMapping
									.getQuantity()) + Double.valueOf(quantity);
							EditFavoriteProductMappingActivity.this.datasource
									.updateFavoriteProductMapping(alreadyExistingMapping.getId(),
											alreadyExistingMapping.getStore().getId(),
											clickedMappingProductId, String.valueOf(newQuantity));

						} else {
							// already existing mapping NOT exist - insert
							// new mapping and delete old mapping
							EditFavoriteProductMappingActivity.this.datasource
									.deleteFavoriteProductMapping(clickedMappingId);

							EditFavoriteProductMappingActivity.this.datasource
									.saveFavoriteProductMapping(clickedMappingFavoriteId,
											selectedStore.getId(), clickedMappingProductId,
											quantity);
						}
					}

					EditFavoriteProductMappingActivity.this.finish();
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
