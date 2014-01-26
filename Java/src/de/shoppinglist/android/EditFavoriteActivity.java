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
import de.shoppinglist.android.bean.Favorite;
import de.shoppinglist.android.constant.DBConstants;
import de.shoppinglist.android.datasource.ShoppinglistDataSource;

public class EditFavoriteActivity extends AbstractShoppinglistActivity {

	private Button buttonEditFavorite;

	private Context context;

	private ShoppinglistDataSource datasource;

	private EditText editTextFavoriteName;

	private List<Integer> editTextIds = new LinkedList<Integer>(
			Arrays.asList(R.id.editTextNameAddFavoritelist));

	private TextView textViewTitle;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.datasource = super.getDatasource();
		this.context = super.getContext();

		this.setContentView(R.layout.add_or_edit_favorite);

		// get values of calling activity
		final int selectedFavoriteId = this.getIntent()
				.getIntExtra(DBConstants.COL_FAVORITE_ID, -1);
		final String selectedFavoriteName = this.getIntent().getStringExtra(
				DBConstants.COL_FAVORITE_NAME);

		// set the title to match the activity
		this.textViewTitle = (TextView) this.findViewById(R.id.titleAddFavorite);
		this.textViewTitle.setText(R.string.title_edit_name_favoritelist);

		this.editTextFavoriteName = (EditText) this.findViewById(R.id.editTextNameAddFavoritelist);
		this.editTextFavoriteName.setText(selectedFavoriteName);
		this.editTextFavoriteName.addTextChangedListener(super
				.getTextWatcher(R.id.editTextNameAddFavoritelist));

		this.buttonEditFavorite = (Button) this.findViewById(R.id.buttonConfirmAddFavoritelist);
		this.buttonEditFavorite.setText(R.string.button_text_save);

		this.buttonEditFavorite.setOnClickListener(new OnClickListener() {

			public void onClick(final View v) {
				if (EditFavoriteActivity.super.setErrorOnEmptyEditTexts(editTextIds)) {

					// check whether there is already a favoritelist with this
					// name
					final Favorite alreadyExistingFavorite = EditFavoriteActivity.this.datasource
							.getFavoriteByName(EditFavoriteActivity.this.editTextFavoriteName
									.getText().toString());

					if (alreadyExistingFavorite == null) {

						final Favorite favoriteToUpdate = new Favorite();
						favoriteToUpdate.setId(selectedFavoriteId);
						favoriteToUpdate.setName(EditFavoriteActivity.this.editTextFavoriteName
								.getText().toString());

						EditFavoriteActivity.this.datasource.updateFavorite(favoriteToUpdate);
						EditFavoriteActivity.this.finish();

					} else {
						Toast.makeText(
								EditFavoriteActivity.this.context,
								EditFavoriteActivity.this
										.getString(R.string.msg_favorite_already_exists),
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
