package com.zegocloud.uikit.call_plugin.voip;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.zegocloud.uikit.call_plugin.Defines;
import com.zegocloud.uikit.call_plugin.R;

public class ZegoCallIncomingActivity extends Activity {
    private static final String TAG = "ZegoCallIncomingActivity";

    private BroadcastReceiver callEndedReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setShowWhenLocked(true);
        setTurnScreenOn(true);

        setContentView(R.layout.activity_incoming_call);

        Intent intent = getIntent();
        String title = intent.getStringExtra(Defines.FLUTTER_PARAM_TITLE);
        String content = intent.getStringExtra(Defines.FLUTTER_PARAM_CONTENT);
        boolean isVideo = intent.getBooleanExtra(Defines.FLUTTER_PARAM_IS_VIDEO, false);
        String acceptText = intent.getStringExtra(Defines.FLUTTER_PARAM_ACCEPT_BUTTON_TEXT);
        String rejectText = intent.getStringExtra(Defines.FLUTTER_PARAM_REJECT_BUTTON_TEXT);
        String avatarUrl = intent.getStringExtra(Defines.FLUTTER_PARAM_CALLER_AVATAR_URL);

        TextView tvTitle = findViewById(R.id.tvCallerName);
        TextView tvContent = findViewById(R.id.tvCallType);
        TextView tvAccept = findViewById(R.id.tvAcceptBtn);
        TextView tvReject = findViewById(R.id.tvRejectBtn);
        ImageView ivAccept = findViewById(R.id.ivAcceptIcon);
        ImageView ivAvatar = findViewById(R.id.imageView);
        ImageView ivAppLogo = findViewById(R.id.ivAppLogo);

        if (tvTitle != null && title != null) tvTitle.setText(title);
        if (tvContent != null && content != null) tvContent.setText(content);
        if (tvAccept != null && acceptText != null) tvAccept.setText(acceptText);
        if (tvReject != null && rejectText != null) tvReject.setText(rejectText);
        if (ivAccept != null) {
            ivAccept.setImageResource(isVideo ? R.drawable.ic_video_accept : R.drawable.ic_audio_accept);
        }

        if (ivAvatar != null && avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .transform(new CircleCrop())
                    .placeholder(R.drawable.ic_person_placeholder)
                    .error(R.drawable.ic_person_placeholder)
                    .into(ivAvatar);
        }

        // Load the app's logo from the host app; try a custom override first,
        // then fall back to the launcher icons that every app ships.
        if (ivAppLogo != null) {
            int logoResId = getResources().getIdentifier("ic_app_logo", "drawable", getPackageName());
            if (logoResId == 0) {
                logoResId = getResources().getIdentifier("ic_launcher_round", "mipmap", getPackageName());
            }
            if (logoResId == 0) {
                logoResId = getResources().getIdentifier("ic_launcher", "mipmap", getPackageName());
            }
            if (logoResId != 0) {
                Glide.with(this)
                        .load(logoResId)
                        .transform(new CircleCrop())
                        .into(ivAppLogo);
                ivAppLogo.setVisibility(View.VISIBLE);
            }
        }

        LinearLayout llAccept = findViewById(R.id.llAcceptBtn);
        LinearLayout llReject = findViewById(R.id.llRejectBtn);

        if (llAccept != null) {
            llAccept.setOnClickListener(v -> {
                Log.d(TAG, "accept tapped");
                LocalBroadcastManager.getInstance(this)
                        .sendBroadcast(new Intent(Defines.ACTION_CALL_NOTIFICATION_ACCEPT));
                finish();
            });
        }

        if (llReject != null) {
            llReject.setOnClickListener(v -> {
                Log.d(TAG, "reject tapped");
                LocalBroadcastManager.getInstance(this)
                        .sendBroadcast(new Intent(Defines.ACTION_CALL_NOTIFICATION_REJECT));
                finish();
            });
        }

        callEndedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "call ended broadcast received, finishing");
                finish();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(
                callEndedReceiver,
                new IntentFilter(Defines.ACTION_VOIP_CALL_ENDED)
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (callEndedReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(callEndedReceiver);
        }
    }
}
