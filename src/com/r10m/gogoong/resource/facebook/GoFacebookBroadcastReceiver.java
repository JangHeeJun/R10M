package com.r10m.gogoong.resource.facebook;

import android.os.Bundle;
import android.util.Log;

import com.facebook.FacebookBroadcastReceiver;
/** faceBook imagePosting시 필요 */
public class GoFacebookBroadcastReceiver extends FacebookBroadcastReceiver {

	@Override
    protected void onSuccessfulAppCall(String appCallId, String action, Bundle extras) {
        // A real app could update UI or notify the user that their photo was uploaded.
        Log.d("HelloFacebook", String.format("Photo uploaded by call " + appCallId + " succeeded."));
    }

    @Override
    protected void onFailedAppCall(String appCallId, String action, Bundle extras) {
        // A real app could update UI or notify the user that their photo was not uploaded.
        Log.d("HelloFacebook", String.format("Photo uploaded by call " + appCallId + " failed."));
    }
}
