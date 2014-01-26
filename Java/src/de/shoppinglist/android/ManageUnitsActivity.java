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
import de.shoppinglist.android.adapter.UnitAdapter;
import de.shoppinglist.android.bean.Unit;
import de.shoppinglist.android.constant.DBConstants;
import de.shoppinglist.android.datasource.ShoppinglistDataSource;

public class ManageUnitsActivity extends AbstractShoppinglistActivity {

	private List<Unit> allUnits;

	private Context context;

	private ShoppinglistDataSource datasource;

	private ListView listUnits;

	private ArrayAdapter<Unit> unitListAdapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.datasource = super.getDatasource();
		this.context = super.getContext();

		this.setContentView(R.layout.manage_units);

		this.allUnits = this.datasource.getAllUnits();

		this.unitListAdapter = new UnitAdapter(this, this.allUnits);

		this.listUnits = (ListView) this.findViewById(R.id.listViewManageUnits);
		this.listUnits.setAdapter(this.unitListAdapter);

		// handle long clicks on the stores
		this.listUnits.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(final AdapterView<?> arg0, final View v,
					final int position, final long id) {
				final PopupMenu popup = new PopupMenu(ManageUnitsActivity.this.context, v);
				final MenuInflater inflater = popup.getMenuInflater();
				inflater.inflate(R.menu.popupmenu_manage_units, popup.getMenu());
				popup.show();
				// handle clicks on the popup-buttons
				popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {

					public boolean onMenuItemClick(final MenuItem item) {
						final Unit selectedUnit = ManageUnitsActivity.this.unitListAdapter
								.getItem(position);

						switch (item.getItemId()) {

						case R.id.popupEditUnit:
							// switch to the AddStoreActivity
							final Intent intentEditStore = new Intent(
									ManageUnitsActivity.this.context, EditUnitActivity.class);

							// put the store attributes in here, so we
							// can show it in the edit-layout
							intentEditStore.putExtra(DBConstants.COL_UNIT_ID, selectedUnit.getId());
							intentEditStore.putExtra(DBConstants.COL_UNIT_NAME,
									selectedUnit.getName());

							ManageUnitsActivity.this.startActivityForResult(intentEditStore, 0);
							break;

						case R.id.popupDeleteUnit:
							// prüfen ob store in Benutzung
							// (fav_mapping, shop_mapping)
							// wenn in Benutzung, Toast. mit nachricht
							// wenn nicht, löschen
							if (ManageUnitsActivity.this.datasource
									.checkWhetherUnitIsNotInUse(selectedUnit.getId())) {

								ManageUnitsActivity.this.datasource.deleteUnit(selectedUnit.getId());

								ManageUnitsActivity.this.unitListAdapter.remove(selectedUnit);

							} else {
								Toast.makeText(
										ManageUnitsActivity.this.context,
										ManageUnitsActivity.this
												.getString(R.string.msg_unit_in_use_cant_delete),
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
		inflater.inflate(R.menu.actionbar_menu_manage_units, menu);
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

		case R.id.actionbarAddUnit:
			// switch to the AddStoreActivity
			final Intent intentAddUnit = new Intent(this, AddUnitActivity.class);
			this.startActivityForResult(intentAddUnit, 0);
			break;

		default:
			break;
		}
		return false;
	}

	@Override
	public void onResume() {
		super.onResume();

		this.allUnits = this.datasource.getAllUnits();
		this.unitListAdapter = new UnitAdapter(this, this.allUnits);
		this.listUnits.setAdapter(this.unitListAdapter);
	}

}
