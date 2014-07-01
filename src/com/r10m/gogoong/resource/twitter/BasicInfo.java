package com.r10m.gogoong.resource.twitter;

import twitter4j.Twitter;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
/** twitter 연동을 위한 기본정보 */
public class BasicInfo {
	public static final String TWIT_API_KEY = "i5fGLf8rGw48DlSYb2CPOlDqO";
	public static final String TWIT_CONSUMER_KEY = "i5fGLf8rGw48DlSYb2CPOlDqO";
	public static final String TWIT_CONSUMER_SECRET = "wqkCat5KlYigpQrmmkeB3joBS2L7lwWHNrJCTSVtt5EwjsWXG1";
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
