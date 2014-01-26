package de.shoppinglist.android.googledrive;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files.Insert;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import de.shoppinglist.android.AbstractShoppinglistActivity;
import de.shoppinglist.android.R;
import de.shoppinglist.android.UserConfigurationActivity;
import de.shoppinglist.android.adapter.GoogleDriveFileAdapter;
import de.shoppinglist.android.bean.Product;
import de.shoppinglist.android.bean.ShoppinglistProductMapping;
import de.shoppinglist.android.bean.Store;
import de.shoppinglist.android.bean.Unit;
import de.shoppinglist.android.constant.GlobalValues;
import de.shoppinglist.android.datasource.ShoppinglistDataSource;

public class DriveApiActivity extends AbstractShoppinglistActivity {

	private Button buttonExport;

	private Button buttonImport;

	private Context context;

	private CredentialStore credentialStore;

	private ShoppinglistDataSource datasource;

	private EditText editTextDocumentNameExport;

	private final List<Integer> editTextIds = new LinkedList<Integer>(
			Arrays.asList(R.id.editTextGoogleDriveExportFileName));

	private String fileName;

	private final String filePath = "/data/data/de.shopplinglist.android/";

	private String fileTitle;

	private PopupWindow popupWindow;

	private SharedPreferences prefs;

	private Drive service;

	private List<ShoppinglistProductMapping> shoppinglistProductMappings;

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {

		case android.R.id.home:
			final Intent intent = new Intent(this, UserConfigurationActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
			break;

		default:
			break;
		}
		return false;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.google_drive_export_import);

		this.datasource = super.getDatasource();
		this.context = super.getContext();

		final HttpTransport httpTransport = new NetHttpTransport();
		final JsonFactory jsonFactory = new JacksonFactory();
		this.service = new Drive(httpTransport, jsonFactory, null);

		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);

		if (this.credentialStore == null) {
			this.credentialStore = new SharedPreferencesCredentialStore(this.prefs);
		}

		this.editTextDocumentNameExport = (EditText) this
				.findViewById(R.id.editTextGoogleDriveExportFileName);
		this.editTextDocumentNameExport.addTextChangedListener(super
				.getTextWatcher(R.id.editTextGoogleDriveExportFileName));

		this.buttonExport = (Button) this.findViewById(R.id.buttonConfirmExportToGoogleDrive);
		this.buttonExport.setOnClickListener(new OnClickListener() {

			public void onClick(final View v) {
				if (DriveApiActivity.super
						.setErrorOnEmptyEditTexts(DriveApiActivity.this.editTextIds)) {
					DriveApiActivity.this.exportShoppinglistToDrive();
					Toast.makeText(DriveApiActivity.this.context,
							DriveApiActivity.this.getString(R.string.msg_google_export_successful),
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		this.buttonImport = (Button) this.findViewById(R.id.buttonConfirmImportFromGoogleDrive);
		this.buttonImport.setOnClickListener(new OnClickListener() {

			public void onClick(final View v) {
				DriveApiActivity.this.importShoppinglistFromDrive();
			}
		});

	}

	private void exportShoppinglistToDrive() {
		this.fileTitle = this.editTextDocumentNameExport.getText().toString().trim();
		this.fileName = this.filePath + this.fileTitle + ".txt";

		this.shoppinglistProductMappings = this.datasource.getProductsOnShoppingList(-1);

		if (!this.shoppinglistProductMappings.isEmpty()) {

			try {
				// create local file to export it and write the shoppinglist in
				// it.
				final OutputStream fout = new FileOutputStream(this.fileName);
				final OutputStream bout = new BufferedOutputStream(fout);
				final OutputStreamWriter out = new OutputStreamWriter(bout, "8859_1");
				for (final ShoppinglistProductMapping shoppinglistProductMapping : this.shoppinglistProductMappings) {
					if (shoppinglistProductMapping.isChecked() == GlobalValues.NO) {

						out.write(shoppinglistProductMapping.toString() + " @"
								+ shoppinglistProductMapping.getStore().getName() + "\n");
					}
				}

				out.close();

				// prepare the file for export
				final java.io.File fileContent = new java.io.File(this.fileName);

				final FileContent mediaContent = new FileContent(GlobalValues.TEXT_PLAIN_MIMETYPE,
						fileContent);

				final File file = new File();
				file.setEditable(GlobalValues.YES_BOOL);
				file.setTitle(this.fileTitle);
				file.setDescription(this.getString(R.string.msg_google_export_file_description));
				file.setMimeType(GlobalValues.TEXT_PLAIN_MIMETYPE);

				// do the insert / export
				final Insert request = this.service.files().insert(file, mediaContent);
				request.setOauthToken(this.credentialStore.read().getAccessToken());
				request.execute();

				// delete the locally created file
				// fileContent.delete();

			} catch (final IOException e1) {
				e1.printStackTrace();
			}
		} else {
			Toast.makeText(DriveApiActivity.this.context,
					this.getString(R.string.msg_google_export_empty_shoppinglist),
					Toast.LENGTH_SHORT).show();
		}
	}

	private void importShoppinglistFromDrive() {
		try {

			// get the file list, so the user can chose the file to import
			final Drive.Files.List request = this.service.files().list();
			request.setOauthToken(this.credentialStore.read().getAccessToken());
			final FileList fileList = request.execute();

			// show the file-list-popup for user's choice.
			final LayoutInflater inflater = (LayoutInflater) this.getBaseContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View popupView = inflater.inflate(R.layout.popup_import_google_file, null);
			this.popupWindow = new PopupWindow(popupView,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			this.popupWindow.setBackgroundDrawable(new BitmapDrawable());

			final Button buttonAbortImport = (Button) popupView
					.findViewById(R.id.buttonDismissGoogleImportPopup);
			buttonAbortImport.setOnClickListener(new OnClickListener() {

				public void onClick(final View v) {
					DriveApiActivity.this.popupWindow.dismiss();
				}
			});

			final ListView listViewDriveFiles = (ListView) popupView
					.findViewById(R.id.listViewGoogleDriveFilesImport);
			final GoogleDriveFileAdapter importFileAdapter = new GoogleDriveFileAdapter(
					this.context, fileList.getItems());
			listViewDriveFiles.setAdapter(importFileAdapter);

			listViewDriveFiles.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(final AdapterView<?> arg0, final View view,
						final int position, final long id) {

					final File selectedFile = importFileAdapter.getItem(position);
					DriveApiActivity.this.fileTitle = selectedFile.getTitle();

					// download the file, read it, put the mappings in
					// DB
					try {
						// download the selectedFile
						if ((selectedFile != null) && (selectedFile.getDownloadUrl() != null)) {

							final GenericUrl url = new GenericUrl(selectedFile.getDownloadUrl());
							final HttpRequest request = DriveApiActivity.this.service
									.getRequestFactory().buildGetRequest(url);
							final HttpHeaders headers = new HttpHeaders();
							headers.setAccept(GlobalValues.TEXT_PLAIN_MIMETYPE);
							headers.set("Accept-Charset", "ISO-8859-1");
							headers.setAuthorization("OAuth "
									+ DriveApiActivity.this.credentialStore.read().getAccessToken());
							request.setHeaders(headers);

							final HttpResponse response = request.execute();
							final InputStream inputStream = response.getContent();
							int readChar = 0;
							final StringBuffer fileStringBuffer = new StringBuffer();
							while ((readChar = inputStream.read()) != -1) {
								fileStringBuffer.append((char) readChar);
							}

							final String[] shoppinglistProductMappings = fileStringBuffer
									.toString().split("\\n");

							boolean isShoppinglistFile = true;
							for (final String mapping : shoppinglistProductMappings) {
								if (!mapping.matches("^\\d*\\s.*\\s.*\\@.*$")) {
									isShoppinglistFile = false;
									break;
								}
							}

							if (isShoppinglistFile) {
								String quantity = "";
								String unitName = "";
								String productName = "";
								String storeName = "";
								// add the mappings to DB
								for (String mapping : shoppinglistProductMappings) {
									// get quantity
									quantity = mapping.substring(0, mapping.indexOf(" "));
									// cut the quantity out of this string
									mapping = mapping.substring(quantity.length() + 1,
											mapping.length());

									// get unit
									unitName = mapping.substring(0, mapping.indexOf(" "));
									// cut the unit out of this string
									mapping = mapping.substring(unitName.length() + 1,
											mapping.length());

									// get productName
									productName = mapping.substring(0, mapping.indexOf("@") - 1);
									// cut the product out of this string
									mapping = mapping.substring(productName.length() + 1,
											mapping.length());

									// get storeName
									storeName = mapping.substring(mapping.indexOf("@") + 1);

									// check which of the elements in this
									// mapping
									// must be added / saved to the db and
									// which
									// ones could only be updated.
									Unit unit = DriveApiActivity.this.datasource
											.getUnitByName(unitName);
									if (unit == null) {
										DriveApiActivity.this.datasource.saveUnit(unitName);
										unit = DriveApiActivity.this.datasource
												.getUnitByName(unitName);
									}

									Product product = DriveApiActivity.this.datasource
											.getProductByNameAndUnit(productName, unit.getId());
									if (product == null) {
										DriveApiActivity.this.datasource.saveProduct(productName,
												unit.getId());
										product = DriveApiActivity.this.datasource
												.getProductByNameAndUnit(productName, unit.getId());
									}

									Store store = DriveApiActivity.this.datasource
											.getStoreByName(storeName);
									if (store == null) {
										DriveApiActivity.this.datasource.saveStore(storeName);
										store = DriveApiActivity.this.datasource
												.getStoreByName(storeName);
									}

									final ShoppinglistProductMapping alreadyExistingMapping = DriveApiActivity.this.datasource
											.checkWhetherShoppinglistProductMappingExists(
													store.getId(), product.getId());
									if (alreadyExistingMapping != null) {
										// JA: update quantity
										final double quantityToUpdate = Double
												.valueOf(alreadyExistingMapping.getQuantity())
												+ Double.valueOf(quantity);
										DriveApiActivity.this.datasource.updateShoppinglistProductMapping(
												alreadyExistingMapping.getId(),
												alreadyExistingMapping.getStore().getId(),
												alreadyExistingMapping.getProduct().getId(),
												String.valueOf(quantityToUpdate));
									} else {
										// NEIN: insert new / save
										DriveApiActivity.this.datasource
												.saveShoppingListProductMapping(store.getId(),
														product.getId(), quantity, GlobalValues.NO);
									}

									Toast.makeText(
											DriveApiActivity.this.context,
											DriveApiActivity.this
													.getString(R.string.msg_google_import_successful),
											Toast.LENGTH_SHORT).show();
								}
							} else {
								Toast.makeText(
										DriveApiActivity.this.context,
										DriveApiActivity.this
												.getString(R.string.msg_google_import_not_compatible),
										Toast.LENGTH_SHORT).show();
							}

						} else {
							Toast.makeText(
									DriveApiActivity.this.context,
									DriveApiActivity.this
											.getString(R.string.msg_google_import_not_compatible),
									Toast.LENGTH_SHORT).show();
						}

					} catch (final IOException e) {
						e.printStackTrace();
					}
					DriveApiActivity.this.popupWindow.dismiss();
				}
			});

			this.popupWindow.setFocusable(GlobalValues.YES_BOOL);
			this.popupWindow.showAtLocation(
					this.findViewById(R.id.buttonConfirmImportFromGoogleDrive), Gravity.CENTER, 0,
					0);

		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

}
