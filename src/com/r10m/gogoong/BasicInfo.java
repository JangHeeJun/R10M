package com.r10m.gogoong;

import twitter4j.Twitter;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class BasicInfo {
	public static final String TWIT_API_KEY = "3Of6DKbWKy5dI4hpFVB0PULnO";
	public static final String TWIT_CONSUMER_KEY = "3Of6DKbWKy5dI4hpFVB0PULnO";
	public static final String TWIT_CONSUMER_SECRET = "5l4fbhG9N3EUsM1FU82xVqdxk4tlcDUWwtX4UV1H0dXzAbawGC";
	public static final String TWIT_CALLBACK_URL = "https://twitter.com";

	public static final int REQ_CODE_TWIT_LOGIN = 1001;

	public static boolean TwitLogin = false;
	public static Twitter TwitInstance = null;
	public static AccessToken TwitAccessToken = null;
	public static RequestToken TwitRequestToken = null;

	public static String TWIT_KEY_TOKEN = "";
	public static String TWIT_KEY_TOKEN_SECRET = "";
	public static String TwitScreenName = "";
}
