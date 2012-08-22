package de.shoppinglist.android.googledrive;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;

public interface CredentialStore {
	void clearCredentials();

	GoogleTokenResponse read();

	void refreshAccessTokenAndExpiresIn(String accessToken, Long expiresIn);

	void write(GoogleTokenResponse response);
}
