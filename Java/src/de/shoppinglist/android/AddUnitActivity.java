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
import de.shoppinglist.android.bean.Unit;
import de.shoppinglist.android.datasource.ShoppinglistDataSource;

public class AddUnitActivity extends AbstractShoppinglistActivity {

	private Button buttonAddUnit;

	private Context context;

	private ShoppinglistDataSource datasource;

	private EditText editTextUnitName;

	private List<Integer> editTextIds = new LinkedList<Integer>(
			Arrays.asList(R.id.editTextNameAddUnit));

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.datasource = super.getDatasource();
		this.context = super.getContext();

		this.setContentView(R.layout.add_or_edit_unit);

		this.editTextUnitName = (EditText) this.findViewById(R.id.editTextNameAddUnit);
		this.editTextUnitName
				.addTextChangedListener(super.getTextWatcher(R.id.editTextNameAddUnit));

		this.buttonAddUnit = (Button) this.findViewById(R.id.buttonConfirmAddUnit);
		this.buttonAddUnit.setOnClickListener(new OnClickListener() {

			public void onClick(final View v) {
				if (AddUnitActivity.super.setErrorOnEmptyEditTexts(editTextIds)) {

					// check whether there is already an unit with this name
					final Unit alreadyExistingUnit = AddUnitActivity.this.datasource
							.getUnitByName(AddUnitActivity.this.editTextUnitName.getText()
									.toString());

					if (alreadyExistingUnit == null) {
						// save new unit, when there is no unit with this name
						AddUnitActivity.this.datasource
								.saveUnit(AddUnitActivity.this.editTextUnitName.getText()
										.toString());
						AddUnitActivity.this.finish();

					} else {
						Toast.makeText(AddUnitActivity.this.context,
								AddUnitActivity.this.getString(R.string.msg_unit_already_exists),
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
			Intent intent = new Intent(this, ManageUnitsActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;

		default:
			break;
		}
		return false;
	}
}
