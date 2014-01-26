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
import android.widget.TextView;
import android.widget.Toast;
import de.shoppinglist.android.bean.Unit;
import de.shoppinglist.android.constant.DBConstants;
import de.shoppinglist.android.datasource.ShoppinglistDataSource;

public class EditUnitActivity extends AbstractShoppinglistActivity {

	private Button buttonConfirmEdit;

	private Context context;

	private ShoppinglistDataSource datasource;

	private EditText editTextUnitName;

	private List<Integer> editTextIds = new LinkedList<Integer>(
			Arrays.asList(R.id.editTextNameAddUnit));

	private TextView textViewTitle;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.datasource = super.getDatasource();
		this.context = super.getContext();

		this.setContentView(R.layout.add_or_edit_unit);

		// get values of calling activity
		final int selectedUnitId = this.getIntent().getIntExtra(DBConstants.COL_UNIT_ID, -1);
		final String selectedUnitName = this.getIntent().getStringExtra(DBConstants.COL_UNIT_NAME);

		// set the title to match activity
		this.textViewTitle = (TextView) this.findViewById(R.id.titleAddUnit);
		this.textViewTitle.setText(R.string.title_edit_unit);

		this.editTextUnitName = (EditText) this.findViewById(R.id.editTextNameAddUnit);
		this.editTextUnitName.setText(selectedUnitName);
		this.editTextUnitName
				.addTextChangedListener(super.getTextWatcher(R.id.editTextNameAddUnit));

		this.buttonConfirmEdit = (Button) this.findViewById(R.id.buttonConfirmAddUnit);
		this.buttonConfirmEdit.setText(R.string.button_text_save);
		this.buttonConfirmEdit.setOnClickListener(new OnClickListener() {

			public void onClick(final View v) {
				if (EditUnitActivity.super.setErrorOnEmptyEditTexts(editTextIds)) {

					// check whether there is an unit with this name already
					final Unit alreadyExistingUnit = EditUnitActivity.this.datasource
							.getUnitByName(EditUnitActivity.this.editTextUnitName.getText()
									.toString());

					if (alreadyExistingUnit == null) {

						final Unit unitToUpdate = new Unit();
						unitToUpdate.setId(selectedUnitId);
						unitToUpdate.setName(EditUnitActivity.this.editTextUnitName.getText()
								.toString());

						EditUnitActivity.this.datasource.updateUnit(unitToUpdate);
						EditUnitActivity.this.finish();

					} else {
						Toast.makeText(EditUnitActivity.this.context,
								EditUnitActivity.this.getString(R.string.msg_unit_already_exists),
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
