package de.shoppinglist.android;

import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import de.shoppinglist.android.adapter.HistoryOverviewAdapter;
import de.shoppinglist.android.bean.Shoppinglist;
import de.shoppinglist.android.constant.DBConstants;
import de.shoppinglist.android.datasource.ShoppinglistDataSource;
import de.shoppinglist.android.helper.GMTToLocalTimeConverter;

public class ShowHistoryOverviewActivity extends AbstractShoppinglistActivity {

	private Context context;

	private ShoppinglistDataSource datasource;

	private HistoryOverviewAdapter historyAdapter;

	private List<Shoppinglist> historyShoppinglists;

	private ListView listViewHistory;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.datasource = super.getDatasource();
		this.context = super.getContext();

		this.setContentView(R.layout.history_overview);

		this.historyShoppinglists = this.datasource.getHistoryShoppinglists();

		this.historyAdapter = new HistoryOverviewAdapter(this.context, this.historyShoppinglists);

		this.listViewHistory = (ListView) this.findViewById(R.id.listViewHistoryOverview);
		this.listViewHistory.setAdapter(this.historyAdapter);

		// handle clicks on the historyItems
		this.listViewHistory.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(final AdapterView<?> arg0, final View v, final int position,
					final long id) {

				// call another Activity to show the details of the clicked
				// historyItem
				final Shoppinglist clickedHistoryShoppinglist = ShowHistoryOverviewActivity.this.historyAdapter
						.getItem(position);

				final Intent intent = new Intent(v.getContext(), ShowHistoryShoppinglist.class);
				intent.putExtra(DBConstants.COL_SHOPPINGLIST_ID, clickedHistoryShoppinglist.getId());

				if (clickedHistoryShoppinglist.getCreatedTime() != null) {
					final Date clickedShoppinglistCreatedTime = GMTToLocalTimeConverter
							.convert(clickedHistoryShoppinglist.getCreatedTime());
					intent.putExtra(DBConstants.COL_SHOPPINGLIST_CREATED_TIME,
							clickedShoppinglistCreatedTime.toLocaleString());
				}
				if (clickedHistoryShoppinglist.getFinishedTime() != null) {
					final Date clickedShoppinglistFinishedTime = GMTToLocalTimeConverter
							.convert(clickedHistoryShoppinglist.getFinishedTime());
					intent.putExtra(DBConstants.COL_SHOPPINGLIST_FINISHED_TIME,
							clickedShoppinglistFinishedTime.toLocaleString());
				}
				ShowHistoryOverviewActivity.this.startActivityForResult(intent, 0);
			}

		});
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		final MenuInflater inflater = this.getMenuInflater();
		inflater.inflate(R.menu.actionbar_menu_history, menu);
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

		// AddProductbutton - Actionbar
		case R.id.actionbarDeleteHistory:
			final AlertDialog.Builder alertBox = new AlertDialog.Builder(this);
			alertBox.setMessage(this.getString(R.string.msg_really_delete_history));
			alertBox.setPositiveButton(this.getString(R.string.msg_yes), new OnClickListener() {

				public void onClick(final DialogInterface dialog, final int which) {
					ShowHistoryOverviewActivity.this.datasource.deleteHistory();

					ShowHistoryOverviewActivity.this.historyAdapter.clear();
				}
			});

			alertBox.setNegativeButton(this.getString(R.string.msg_no), new OnClickListener() {

				public void onClick(final DialogInterface dialog, final int which) {
					// do nothing here
				}
			});

			alertBox.show();

			break;
		default:
			break;
		}

		return false;
	}

	@Override
	public void onResume() {
		super.onResume();

		this.historyShoppinglists = this.datasource.getHistoryShoppinglists();
		this.historyAdapter = new HistoryOverviewAdapter(this.context, this.historyShoppinglists);

		this.listViewHistory = (ListView) this.findViewById(R.id.listViewHistoryOverview);
		this.listViewHistory.setAdapter(this.historyAdapter);

	}

}
