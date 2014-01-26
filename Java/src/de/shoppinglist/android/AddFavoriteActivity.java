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
import de.shoppinglist.android.bean.Favorite;
import de.shoppinglist.android.datasource.ShoppinglistDataSource;

public class AddFavoriteActivity extends AbstractShoppinglistActivity {

	private Button buttonAddFavorite;

	private Context context;

	private ShoppinglistDataSource datasource;

	private EditText editTextFavoriteName;

	private List<Integer> editTextIds = new LinkedList<Integer>(
			Arrays.asList(R.id.editTextNameAddFavoritelist));

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.context = super.getContext();
		this.datasource = super.getDatasource();

		this.setContentView(R.layout.add_or_edit_favorite);

		this.editTextFavoriteName = (EditText) this.findViewById(R.id.editTextNameAddFavoritelist);
		this.editTextFavoriteName.addTextChangedListener(super
				.getTextWatcher(R.id.editTextNameAddFavoritelist));

		this.buttonAddFavorite = (Button) this.findViewById(R.id.buttonConfirmAddFavoritelist);
		this.buttonAddFavorite.setOnClickListener(new OnClickListener() {

			public void onClick(final View v) {
				if (AddFavoriteActivity.super.setErrorOnEmptyEditTexts(editTextIds)) {
					// check whether there is already a favoritelist with this
					// name
					final Favorite alreadyExistingFavorite = AddFavoriteActivity.this.datasource
							.getFavoriteByName(AddFavoriteActivity.this.editTextFavoriteName
									.getText().toString());
					if (alreadyExistingFavorite == null) {
						// save new favorite, when there is no favorite with
						// this
						// name
						AddFavoriteActivity.this.datasource
								.saveFavorite(AddFavoriteActivity.this.editTextFavoriteName
										.getText().toString());
						AddFavoriteActivity.this.finish();

					} else {
						Toast.makeText(
								AddFavoriteActivity.this.context,
								AddFavoriteActivity.this
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
