package de.shoppinglist.android.googledrive;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;

public class SharedPreferencesCredentialStore implements CredentialStore {

	private static final String ACCESS_TOKEN = "access_token";
	private static final String EXPIRES_IN = "expires_in";
	private static final String REFRESH_TOKEN = "refresh_token";
	private static final String SCOPE = "scope";

	private final SharedPreferences prefs;

	public SharedPreferencesCredentialStore(SharedPreferences prefs) {
		this.prefs = prefs;
	}

	public void clearCredentials() {
		Editor editor = prefs.edit();
		editor.remove(ACCESS_TOKEN);
		editor.remove(EXPIRES_IN);
		editor.remove(REFRESH_TOKEN);
		editor.remove(SCOPE);
		editor.commit();
	}

	public GoogleTokenResponse read() {
		GoogleTokenResponse accessTokenResponse = new GoogleTokenResponse();
		accessTokenResponse.setAccessToken(prefs.getString(ACCESS_TOKEN, ""));
		accessTokenResponse.setExpiresInSeconds(prefs.getLong(EXPIRES_IN, 0));
		accessTokenResponse.setRefreshToken(prefs.getString(REFRESH_TOKEN, ""));
		accessTokenResponse.setScope(prefs.getString(SCOPE, ""));
		return accessTokenResponse;
	}

	public void refreshAccessTokenAndExpiresIn(String accessToken, Long expiresIn) {
		Editor editor = prefs.edit();
		editor.putString(ACCESS_TOKEN, accessToken);
		editor.putLong(EXPIRES_IN, expiresIn);
		editor.commit();
	}

	public void write(GoogleTokenResponse accessTokenResponse) {
		Editor editor = prefs.edit();
		editor.putString(ACCESS_TOKEN, accessTokenResponse.getAccessToken());
		editor.putLong(EXPIRES_IN, accessTokenResponse.getExpiresInSeconds());
		editor.putString(REFRESH_TOKEN, accessTokenResponse.getRefreshToken());
		editor.putString(SCOPE, accessTokenResponse.getScope());
		editor.commit();
	}
}
