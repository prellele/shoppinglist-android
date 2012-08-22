package de.shoppinglist.android.googledrive;

import java.util.LinkedList;
import java.util.List;

public class OAuth2ClientCredentials {

	public static final String CLIENT_ID = "1077853032636-scf1im7nkb2meknp2u8dvf82p70m564v.apps.googleusercontent.com";

	public static final String OAUTH_CALLBACK_URL = "http://localhost";

	public static final String CLIENT_SECRET = "wt40NVKX4hp3zzlSOkmk4bmX";

	public static final String REDIRECT_URI = "http://localhost";

	public static final String SINGLE_SCOPE = "https://www.googleapis.com/auth/drive.file";

	public static final List<String> SCOPE = new LinkedList<String>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			// add("https://www.googleapis.com/auth/drive.file");
			add("https://www.googleapis.com/auth/drive");
		}
	};

}
