package com.zegocloud.uikit.call_plugin.voip;

import android.content.Intent;
import android.os.Bundle;
import android.telecom.Connection;
import android.telecom.ConnectionRequest;
import android.telecom.ConnectionService;
import android.telecom.PhoneAccountHandle;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.zegocloud.uikit.call_plugin.Defines;
import com.zegocloud.uikit.call_plugin.notification.PluginNotification;

public class VoipConnectionService extends ConnectionService {
    private static final String TAG = "VoipConnectionService";

    public static VoipConnection currentConnection = null;

    @Override
    public Connection onCreateIncomingConnection(
            PhoneAccountHandle connectionManagerPhoneAccount,
            ConnectionRequest request) {
        Log.d(TAG, "onCreateIncomingConnection");

        VoipConnection connection = new VoipConnection(getApplicationContext());
        connection.setRinging();
        currentConnection = connection;

        Bundle extras = request.getExtras();

        // Post the notification for the heads-up banner and notification shade buttons.
        new PluginNotification().showCallNotification(
                getApplicationContext(),
                extras.getString(Defines.FLUTTER_PARAM_TITLE, ""),
                extras.getString(Defines.FLUTTER_PARAM_CONTENT, ""),
                extras.getString(Defines.FLUTTER_PARAM_ACCEPT_BUTTON_TEXT, Defines.DEFAULT_ACCEPT_TEXT),
                extras.getString(Defines.FLUTTER_PARAM_REJECT_BUTTON_TEXT, Defines.DEFAULT_REJECT_TEXT),
                extras.getString(Defines.FLUTTER_PARAM_CHANNEL_ID, Defines.DEFAULT_CHANNEL_ID),
                extras.getString(Defines.FLUTTER_PARAM_SOUND_SOURCE, ""),
                extras.getString(Defines.FLUTTER_PARAM_ICON_SOURCE, ""),
                extras.getString(Defines.FLUTTER_PARAM_ID, "1"),
                extras.getBoolean(Defines.FLUTTER_PARAM_VIBRATE, Defines.DEFAULT_VIBRATE),
                extras.getBoolean(Defines.FLUTTER_PARAM_IS_VIDEO, Defines.DEFAULT_IS_VIDEO)
        );

        // Directly start the incoming call Activity. The Telecom system grants a temporary
        // background activity start token when onCreateIncomingConnection() fires, so this
        // works on the lock screen without USE_FULL_SCREEN_INTENT being user-granted.
        // ZegoCallIncomingActivity is singleInstance so a second launch (from setFullScreenIntent)
        // is a no-op.
        try {
            Intent activityIntent = new Intent(getApplicationContext(), ZegoCallIncomingActivity.class);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            activityIntent.putExtras(extras);
            getApplicationContext().startActivity(activityIntent);
        } catch (Exception e) {
            Log.w(TAG, "startActivity for incoming call failed: " + e.getMessage());
        }

        return connection;
    }

    @Override
    public void onCreateIncomingConnectionFailed(
            PhoneAccountHandle connectionManagerPhoneAccount,
            ConnectionRequest request) {
        Log.w(TAG, "onCreateIncomingConnectionFailed — already in call or account error");
        LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(new Intent(Defines.ACTION_CALL_NOTIFICATION_CANCEL));
    }
}
