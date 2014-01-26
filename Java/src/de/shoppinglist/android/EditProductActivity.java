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
import de.shoppinglist.android.bean.Product;
import de.shoppinglist.android.bean.ShoppinglistProductMapping;
import de.shoppinglist.android.bean.Store;
import de.shoppinglist.android.bean.Unit;
import de.shoppinglist.android.constant.DBConstants;
import de.shoppinglist.android.constant.GlobalValues;
import de.shoppinglist.android.datasource.ShoppinglistDataSource;

public class EditProductActivity extends AbstractShoppinglistActivity {

	private Button buttonConfirmEditProduct;

	private ShoppinglistDataSource datasource;

	private final List<Integer> editTextIds = new LinkedList<Integer>(Arrays.asList(
			R.id.editTextQuantityAddProduct, R.id.editTextProductNameAutocomplete));

	private EditText editTextProductName;

	private EditText editTextQuantity;

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
				DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_QUANTITY);
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

		// ShoppinglistProductMappingId
		final int clickedMappingId = this.getIntent().getIntExtra(
				DBConstants.COL_SHOPPINGLIST_PRODUCT_MAPPING_ID, -1);

		this.buttonConfirmEditProduct = (Button) this.findViewById(R.id.buttonConfirmAddProduct);
		this.buttonConfirmEditProduct.setText(R.string.button_text_save);
		this.buttonConfirmEditProduct.setOnClickListener(new OnClickListener() {

			public void onClick(final View v) {
				if (EditProductActivity.super
						.setErrorOnEmptyEditTexts(EditProductActivity.this.editTextIds)) {

					// prüfen, ob Produkt in Benutzung, wenn nicht updaten,
					// sonst
					// neu anlegen
					//
					final String quantity = EditProductActivity.this.editTextQuantity.getText()
							.toString();
					final Unit selectedUnit = (Unit) EditProductActivity.this.spinnerUnits
							.getSelectedItem();
					final String productName = EditProductActivity.this.editTextProductName
							.getText().toString();
					final Store selectedStore = (Store) EditProductActivity.this.spinnerStores
							.getSelectedItem();

					if (!clickedMappingProductName.equals(productName)
							|| (clickedMappingUnitId != selectedUnit.getId())) {
						// product has changed - check whether the product
						// already
						// exist

						// delete old mapping
						EditProductActivity.this.datasource
								.deleteShoppinglistProductMapping(clickedMappingId);

						final Product alreadyExistingProduct = EditProductActivity.this.datasource
								.getProductByNameAndUnit(productName, selectedUnit.getId());

						// delete "old" product, when it's not in use
						if (EditProductActivity.this.datasource
								.checkWhetherProductIsNotInUse(clickedMappingProductId)) {
							EditProductActivity.this.datasource
									.deleteProduct(clickedMappingProductId);
						}

						if (alreadyExistingProduct != null) {
							// new Product exist - check whether there is a
							// mapping
							// for this product

							final ShoppinglistProductMapping alreadyExistingMapping = EditProductActivity.this.datasource
									.checkWhetherShoppinglistProductMappingExists(
											selectedStore.getId(), alreadyExistingProduct.getId());

							if (alreadyExistingMapping != null) {
								// already existing mapping - update quantity
								final double newQuantity = Double.valueOf(alreadyExistingMapping
										.getQuantity()) + Double.valueOf(quantity);
								EditProductActivity.this.datasource
										.updateShoppinglistProductMapping(
												alreadyExistingMapping.getId(),
												alreadyExistingMapping.getStore().getId(),
												alreadyExistingProduct.getId(),
												String.valueOf(newQuantity));

							} else {
								// already existing mapping NOT exist - insert
								// new
								// mapping
								EditProductActivity.this.datasource.saveShoppingListProductMapping(
										selectedStore.getId(), alreadyExistingProduct.getId(),
										quantity, GlobalValues.NO);

							}

						} else {
							// new Product not exist
							EditProductActivity.this.datasource.saveProduct(productName,
									selectedUnit.getId());
							final Product newProduct = EditProductActivity.this.datasource
									.getProductByNameAndUnit(productName, selectedUnit.getId());

							EditProductActivity.this.datasource.saveShoppingListProductMapping(
									selectedStore.getId(), newProduct.getId(), quantity,
									GlobalValues.NO);

						}

					} else {
						// product has not changed - check whether there is an
						// existing mapping (pro_id + sto_id)
						final ShoppinglistProductMapping alreadyExistingMapping = EditProductActivity.this.datasource
								.checkWhetherShoppinglistProductMappingExists(
										selectedStore.getId(), clickedMappingProductId);

						if (alreadyExistingMapping != null) {
							// already existing mapping - update quantity
							// (old + new)´

							if (clickedMappingStoreId != alreadyExistingMapping.getStore().getId()) {
								// delete old mapping
								EditProductActivity.this.datasource
										.deleteShoppinglistProductMapping(clickedMappingId);

							}

							alreadyExistingMapping.setQuantity(quantity);
							final double newQuantity = Double.valueOf(alreadyExistingMapping
									.getQuantity());
							EditProductActivity.this.datasource.updateShoppinglistProductMapping(
									alreadyExistingMapping.getId(), alreadyExistingMapping
											.getStore().getId(), clickedMappingProductId, String
											.valueOf(newQuantity));

						} else {
							// already existing mapping NOT exist - insert new
							// mapping and delete old mapping
							EditProductActivity.this.datasource
									.deleteShoppinglistProductMapping(clickedMappingId);

							EditProductActivity.this.datasource.saveShoppingListProductMapping(
									selectedStore.getId(), clickedMappingProductId, quantity,
									GlobalValues.NO);
						}
					}

					EditProductActivity.this.finish();
				}
			}
		});

	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {

		case android.R.id.home:
			final Intent intent = new Intent(this, ShoppinglistActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
			break;

		default:
			break;
		}
		return false;
	}
}
