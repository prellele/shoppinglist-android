package de.shoppinglist.android;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import de.shoppinglist.android.bean.ShoppinglistProductMapping;
import de.shoppinglist.android.bean.Store;
import de.shoppinglist.android.constant.GlobalValues;
import de.shoppinglist.android.datasource.ShoppinglistDataSource;

public class SendEmailActivity extends AbstractShoppinglistActivity {

	private Context context;

	private ShoppinglistDataSource datasource;

	private List<ShoppinglistProductMapping> shoppinglistProductMappingsToSend;

	private List<Store> stores;

	private Button buttonSend;

	private EditText editTextRecipient;

	private List<Integer> editTextIds = new LinkedList<Integer>(
			Arrays.asList(R.id.editTextRecipient));

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.datasource = super.getDatasource();
		this.context = super.getContext();

		this.setContentView(R.layout.send_email);

		this.editTextRecipient = (EditText) this.findViewById(R.id.editTextRecipient);
		this.editTextRecipient.addTextChangedListener(super.getTextWatcher(R.id.editTextRecipient));

		this.buttonSend = (Button) this.findViewById(R.id.buttonSend);

		buttonSend.setOnClickListener(new OnClickListener() {

			public void onClick(final View v) {
				if (SendEmailActivity.super.setErrorOnEmptyEditTexts(editTextIds)) {

					SendEmailActivity.this.stores = SendEmailActivity.this.datasource
							.getStoresForOverview();
					String text = "";

					for (int i = 0; i < stores.size(); i++) {

						text = text + getString(R.string.export_email_at_which_store) + " "
								+ stores.get(i).getName() + ":\n";
						SendEmailActivity.this.shoppinglistProductMappingsToSend = SendEmailActivity.this.datasource
								.getProductsOnShoppingList(stores.get(i).getId());

						for (final ShoppinglistProductMapping mapping : SendEmailActivity.this.shoppinglistProductMappingsToSend) {
							if (mapping.isChecked() == GlobalValues.NO) {
								text = text + "- " + mapping.toString() + "\n";
							}
						}
						text = text + "\n\n";
					}

					if (Pattern.compile(GlobalValues.EMAIL_PATTERN)
							.matcher(editTextRecipient.getText().toString()).matches()) {

						final Intent emailIntent = new Intent(Intent.ACTION_SEND);

						emailIntent.setType("plain/text");
						emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { editTextRecipient
								.getText().toString() });
						emailIntent.putExtra(Intent.EXTRA_SUBJECT, SendEmailActivity.this
								.getString(R.string.export_email_current_shoppinglist));
						emailIntent.putExtra(Intent.EXTRA_TEXT, text);
						SendEmailActivity.this.startActivity(Intent.createChooser(emailIntent,
								SendEmailActivity.this.getString(R.string.export_email_send_via)));
					} else {
						final Toast invalidEmailAlert = Toast.makeText(
								SendEmailActivity.this.context, SendEmailActivity.this
										.getString(R.string.export_email_invalid_email),
								Toast.LENGTH_SHORT);
						invalidEmailAlert.show();
					}
				}
			}
		});

	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {

		case android.R.id.home:
			Intent intent = new Intent(this, UserConfigurationActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;

		default:
			break;
		}
		return false;
	}
}
