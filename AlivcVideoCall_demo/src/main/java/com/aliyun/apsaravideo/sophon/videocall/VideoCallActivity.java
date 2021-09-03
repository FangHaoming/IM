package com.aliyun.apsaravideo.sophon.videocall;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alirtc.beacontowner.R;
import com.aliyun.apsaravideo.sophon.bean.RTCAuthInfo;
import com.aliyun.apsaravideo.sophon.login.VideoCallJoinActivity;
import com.aliyun.apsaravideo.sophon.utils.PermissionUtils;
import com.aliyun.apsaravideo.sophon.utils.StringUtil;

public class VideoCallActivity extends AppCompatActivity implements View.OnClickListener {

    AlivcVideoCallView alivcVideoCallView;
    String displayName;
    String channel;
    private RTCAuthInfo mRtcAuthInfo;
    private TextView mTitleTv;
    private TextView mCopyTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aliyun_video_call_main);

        // 获取权限
        checkHadPermissions(mGrant, 1000);

        getIntentData();
        mTitleTv =findViewById(R.id.tv_title);
        mCopyTv =findViewById(R.id.tv_copy);
        alivcVideoCallView = findViewById(R.id.alivc_videocall_view);
        mCopyTv.setOnClickListener(this);
        alivcVideoCallView.setAlivcVideoCallNotifyListner(new AlivcVideoCallView.AlivcVideoCallNotifyListner() {
            @Override
            public void onLeaveChannel() {
                finish();
            }
        });

        mTitleTv.setText(String.format(getResources().getString(R.string.str_channel_code),channel));
        mTitleTv.setVisibility(View.GONE);

        alivcVideoCallView.auth(displayName, channel, mRtcAuthInfo);
    }

    private void getIntentData() {
        if (getIntent().getExtras() != null) {
            displayName = getIntent().getExtras().getString("username");
            channel = getIntent().getExtras().getString("channel");
            mRtcAuthInfo = (RTCAuthInfo) getIntent().getExtras().getSerializable("rtcAuthInfo");
        }
    }

    @Override
    protected void onDestroy() {
        if (alivcVideoCallView != null) {
            alivcVideoCallView.leave();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_copy) {
            StringUtil.clipChannelId(channel, VideoCallActivity.this);
        }
    }

    private PermissionUtils.PermissionGrant mGrant = new PermissionUtils.PermissionGrant() {
        @Override
        public void onPermissionGranted(int requestCode) {
            try {
                Toast.makeText(VideoCallActivity.this, "已获取权限", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onPermissionCancel() {
            try {
                Toast.makeText(VideoCallActivity.this, "未获取权限", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public void checkHadPermissions(PermissionUtils.PermissionGrant grant, int delay) {
        this.mGrant = grant;
        requestPermission();
    }

    private void requestPermission(){
        PermissionUtils.requestMultiPermissions(this,
                new String[]{
                        PermissionUtils.PERMISSION_CAMERA,
                        PermissionUtils.PERMISSION_WRITE_EXTERNAL_STORAGE,
                        PermissionUtils.PERMISSION_RECORD_AUDIO,
                        PermissionUtils.PERMISSION_READ_EXTERNAL_STORAGE}, mGrant);
    }

}
