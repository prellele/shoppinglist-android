package de.shoppinglist.android.googledrive;

import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;

import de.shoppinglist.android.AbstractShoppinglistActivity;
import de.shoppinglist.android.R;

public class GoogleOAuthActivity extends AbstractShoppinglistActivity {

	/**
	 * need a counter for the onPageFinished-method, otherwise this is called
	 * multiple times
	 */
	private int counter = 0;

	private boolean isErrorOccured = false;

	private SharedPreferences prefs;

	private Context context;

	private CredentialStore credentialStore;

	@Override
	public void finish() {
		super.finish();

		if (this.isErrorOccured) {
			Toast.makeText(this.context, this.getString(R.string.msg_no_permission_google_oauth),
					Toast.LENGTH_SHORT).show();
		} else {
			GoogleOAuthActivity.this.startActivity(new Intent(GoogleOAuthActivity.this,
					DriveApiActivity.class));
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.context = super.getContext();
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);

		if (this.credentialStore == null) {
			this.credentialStore = new SharedPreferencesCredentialStore(prefs);
		}

		if (android.os.Build.VERSION.SDK_INT > 9) {
			final StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		// only do the OAuth-Key-Dance when there's no token saved.
		if (this.credentialStore.read().getAccessToken() == null
				|| this.credentialStore.read().getAccessToken().isEmpty()) {

			final WebView webview = new WebView(this);
			webview.getSettings().setJavaScriptEnabled(true);
			webview.setVisibility(View.VISIBLE);
			this.setContentView(webview);
			final String authorizationUrl = new GoogleAuthorizationCodeRequestUrl(
					OAuth2ClientCredentials.CLIENT_ID, OAuth2ClientCredentials.REDIRECT_URI,
					OAuth2ClientCredentials.SCOPE).build();

			/* WebViewClient must be set BEFORE calling loadUrl! */
			webview.setWebViewClient(new WebViewClient() {

				private String extractCodeFromUrl(final String url) {
					return url.substring(OAuth2ClientCredentials.REDIRECT_URI.length() + 7,
							url.length());
				}

				@Override
				public void onPageFinished(final WebView view, final String url) {

					if (url.startsWith(OAuth2ClientCredentials.REDIRECT_URI)
							&& (GoogleOAuthActivity.this.counter == 0)) {
						try {

							if (url.indexOf("code=") != -1) {

								final String code = this.extractCodeFromUrl(url);

								final GoogleTokenResponse googleTokenResponse = new GoogleAuthorizationCodeTokenRequest(
										new NetHttpTransport(), new JacksonFactory(),
										OAuth2ClientCredentials.CLIENT_ID,
										OAuth2ClientCredentials.CLIENT_SECRET, code,
										OAuth2ClientCredentials.REDIRECT_URI).execute();

								credentialStore = new SharedPreferencesCredentialStore(
										GoogleOAuthActivity.this.prefs);
								credentialStore.write(googleTokenResponse);
								view.setVisibility(View.INVISIBLE);
								GoogleOAuthActivity.this
										.setContentView(R.layout.user_configuration);

								GoogleOAuthActivity.this.counter++;

								finish();

							} else if (url.indexOf("error=") != -1) {
								view.setVisibility(View.INVISIBLE);
								credentialStore.clearCredentials();
								isErrorOccured = true;
								finish();
							}

						} catch (final NetworkOnMainThreadException nomte) {
							nomte.printStackTrace();
						} catch (final IOException e) {
							e.printStackTrace();
						}

					}
				}
			});

			webview.loadUrl(authorizationUrl);
		} else {
			GoogleTokenResponse googleRefreshTokenResponse = null;
			try {
				googleRefreshTokenResponse = new GoogleRefreshTokenRequest(new NetHttpTransport(),
						new JacksonFactory(), credentialStore.read().getRefreshToken(),
						OAuth2ClientCredentials.CLIENT_ID, OAuth2ClientCredentials.CLIENT_SECRET)
						.execute();
			} catch (IOException e) {

				e.printStackTrace();
			}

			credentialStore = new SharedPreferencesCredentialStore(GoogleOAuthActivity.this.prefs);
			credentialStore.refreshAccessTokenAndExpiresIn(
					googleRefreshTokenResponse.getAccessToken(),
					googleRefreshTokenResponse.getExpiresInSeconds());

			finish();
		}
	}
}