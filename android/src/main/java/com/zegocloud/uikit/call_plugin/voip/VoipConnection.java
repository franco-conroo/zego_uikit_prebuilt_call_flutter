package com.zegocloud.uikit.call_plugin.voip;

import android.content.Context;
import android.content.Intent;
import android.telecom.Connection;
import android.telecom.DisconnectCause;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.zegocloud.uikit.call_plugin.Defines;

public class VoipConnection extends Connection {
    private static final String TAG = "VoipConnection";
    private final Context context;

    public VoipConnection(Context context) {
        this.context = context;
    }

    @Override
    public void onAnswer() {
        Log.d(TAG, "onAnswer");
        setActive();
        sendLocalBroadcast(Defines.ACTION_CALL_NOTIFICATION_ACCEPT);
    }

    @Override
    public void onReject() {
        Log.d(TAG, "onReject");
        setDisconnected(new DisconnectCause(DisconnectCause.REJECTED));
        destroy();
        sendLocalBroadcast(Defines.ACTION_CALL_NOTIFICATION_REJECT);
    }

    @Override
    public void onDisconnect() {
        Log.d(TAG, "onDisconnect");
        setDisconnected(new DisconnectCause(DisconnectCause.LOCAL));
        destroy();
        sendLocalBroadcast(Defines.ACTION_CALL_NOTIFICATION_CANCEL);
    }

    private void sendLocalBroadcast(String action) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(action));
    }
}
