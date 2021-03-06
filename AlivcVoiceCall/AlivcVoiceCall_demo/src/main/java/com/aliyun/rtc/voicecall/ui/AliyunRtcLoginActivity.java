package com.aliyun.rtc.voicecall.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alivc.rtc.AliRtcAuthInfo;
import com.alivc.rtc.AliRtcEngine;
import com.alivc.rtc.AliRtcEngineEventListener;
import com.alivc.rtc.AliRtcEngineImpl;
import com.aliyun.rtc.voicecall.R;
import com.aliyun.rtc.voicecall.bean.AliUserInfoResponse;
import com.aliyun.rtc.voicecall.bean.AliUserInfoResponse.AliUserInfo;
import com.aliyun.rtc.voicecall.bean.ChannelUserInfoResponse;
import com.aliyun.rtc.voicecall.constant.Constant;
import com.aliyun.rtc.voicecall.network.OkHttpCientManager;
import com.aliyun.rtc.voicecall.network.OkhttpClient;
import com.aliyun.rtc.voicecall.utils.DoubleClickUtil;
import com.aliyun.rtc.voicecall.utils.PermissionUtil;
import com.aliyun.rtc.voicecall.utils.RegexUtil;
import com.aliyun.rtc.voicecall.utils.ToastUtils;
import com.aliyun.rtc.voicecall.utils.UIHandlerUtil;
import com.aliyun.rtc.voicecall.view.LoadingDrawable;
import com.aliyun.svideo.common.utils.NetUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;


public class AliyunRtcLoginActivity extends AppCompatActivity implements View.OnClickListener, PermissionUtil.PermissionGrantedListener, OkhttpClient.HttpCallBack {

    private static final String TAG = AliyunRtcLoginActivity.class.getSimpleName();
    private AliRtcEngineImpl mEngine;
    private EditText mEtChannelId;
    private String[] permissions = new String[] {
        PermissionUtil.PERMISSION_CAMERA,
        PermissionUtil.PERMISSION_WRITE_EXTERNAL_STORAGE,
        PermissionUtil.PERMISSION_RECORD_AUDIO,
        PermissionUtil.PERMISSION_READ_EXTERNAL_STORAGE
    };
    private AliUserInfo rtcAuthInfo;
    private String userid;
    private TextInputLayout mTextInputLayoutChannelId;
    private TextView mTvConfirm;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alivc_voicecall_activity_rtc_login);
        //????????????
        PermissionUtil.requestPermissions(AliyunRtcLoginActivity.this, permissions, PermissionUtil.PERMISSION_REQUEST_CODE, AliyunRtcLoginActivity.this);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //?????????rtc??????
        initAlivcRtcEngine();
        clearChannelIdForEdit();
    }

    private void clearChannelIdForEdit() {
        if (mEtChannelId != null) {
            mEtChannelId.setText("");
        }
    }

    private void initAlivcRtcEngine() {
        if (mEngine == null) {
            mEngine = AliRtcEngine.getInstance(getApplicationContext());
            mEngine.setRtcEngineEventListener(mEventListener);
        }
    }

    private void initView() {
        mTextInputLayoutChannelId = findViewById(R.id.alivc_voicecall_textinputlayout_channelid);
        mEtChannelId = findViewById(R.id.alivc_voicecall_edittext_channelid);
        mTvConfirm = findViewById(R.id.alivc_voicecall_tv_begin_speak);
        RelativeLayout rlContent = findViewById(R.id.alivc_voicecall_rl_login_layout_content);

        rlContent.setOnClickListener(this);
        mTvConfirm.setOnClickListener(this);
        mTvConfirm.setEnabled(false);
        mEtChannelId.addTextChangedListener(new ChannelIdTextWatcher());
        mEtChannelId.setOnClickListener(this);
    }

    private void setEditTextErrorMsg(int p) {
        mTextInputLayoutChannelId.setErrorEnabled(true);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.alivc_voicecall_tv_begin_speak) {
            if (DoubleClickUtil.isDoubleClick(v, 500)) {
                ToastUtils.showInCenter(AliyunRtcLoginActivity.this, getString(R.string.alivc_voicecall_string_hint_double_click));
                return;
            }
            String roomNum = getChannelId();
            if (TextUtils.isEmpty(roomNum)) {
                ToastUtils.showInCenter(AliyunRtcLoginActivity.this, getString(R.string.alivc_voicecall_string_room_num_empty));
                return;
            }

            //?????????????????????
            if (RegexUtil.regexStr(RegexUtil.CHANNELID_REGEX, getChannelId())) {
                ToastUtils.showInCenter(AliyunRtcLoginActivity.this, getString(R.string.alivc_voicecall_string_text_channel_id_type_error));
                mEtChannelId.setBackgroundResource(R.drawable.alivc_voice_call_red_line_edit_bg_shape);
                return;
            }

            if (!NetUtils.isNetworkConnected(AliyunRtcLoginActivity.this)) {
                ToastUtils.showInCenter(AliyunRtcLoginActivity.this, getString(R.string.alivc_voicecall_string_network_conn_error));
                return;
            }
            getTokenByNet();

        } else if (v.getId() == R.id.alivc_voicecall_rl_login_layout_content) {
            if (mEtChannelId != null && mEtChannelId.hasFocus()) {
                hideSoftInput();
            }
        } else if (v.getId() == R.id.alivc_voicecall_edittext_channelid) {
            mEtChannelId.setBackgroundResource(R.drawable.alivc_voice_call_black_line_edit_bg_shape);
        }
    }

    // ???????????????
    private void hideSoftInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    }

    /**
     * ??????rtc????????????token??????
     */
    private void getTokenByNet() {
        showLoadingView();
        String url = Constant.getUserLoginUrl();
        Map<String, String> params = createTokenParams();
        OkHttpCientManager.getInstance().doGet(url, params, this);
    }


    private void getTokenByDefault() {

    }

    public static String createToken(
            String appId, String appKey, String channelId, String userId,
            String nonce, Long timestamp
    ) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(appId.getBytes());
        digest.update(appKey.getBytes());
        digest.update(channelId.getBytes());
        digest.update(userId.getBytes());
        digest.update(nonce.getBytes());
        digest.update(Long.toString(timestamp).getBytes());

        String token = DatatypeConverter.printHexBinary(digest.digest()).toLowerCase();
        return token;
    }


    /**
     * ????????????????????????loading??????
     */
    private void showLoadingView() {
        mTvConfirm.setText(R.string.alivc_voicecall_string_loading_join_channel);
        LoadingDrawable loadingDrawable = new LoadingDrawable();
        int width = mTvConfirm.getWidth();
        int height = mTvConfirm.getHeight();
        int left = width / 2 - height;
        int right = width / 2;
        loadingDrawable.setBounds(left, 0, right, height);
        mTvConfirm.setCompoundDrawables(loadingDrawable, null, null, null);
    }

    private void hideLoadingView() {
        UIHandlerUtil.getInstance().postRunnable(new Runnable() {
            @Override
            public void run() {
                mTvConfirm.setText(R.string.alivc_voicecall_string_btn_start_voice_call);
                mTvConfirm.setCompoundDrawables(null, null, null, null);
            }
        });
    }

    /*String token, String channelId*/
    private Map<String, String> createTokenParams() {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.NEW_TOKEN_PARAMS_KEY_CHANNELID, getChannelId());
        return params;
    }

    /*String token, String channelId*/
    private Map<String, String> createChannelUserInfoParams() {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.NEW_TOKEN_PARAMS_KEY_CHANNELID, getChannelId());
        return params;
    }

    /**
     * ???????????????
     *
     * @return ?????????
     */
    private String getChannelId() {
        return mEtChannelId == null ? "" : mEtChannelId.getText().toString();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mEngine != null) {
            mEngine.destroy();
            mEngine = null;
        }
    }

    /**
     * ??????????????????
     */
    @Override
    public void onPermissionGranted() {

    }

    /**
     * ??????????????????
     */
    @Override
    public void onPermissionCancel() {
        ToastUtils.showInCenter(AliyunRtcLoginActivity.this, getString(R.string.alirtc_permission));
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionUtil.PERMISSION_REQUEST_CODE) {
            PermissionUtil.requestPermissionsResult(AliyunRtcLoginActivity.this, PermissionUtil.PERMISSION_REQUEST_CODE, permissions, grantResults, AliyunRtcLoginActivity.this);
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void toChatActivity() {
        Intent intent = new Intent(AliyunRtcLoginActivity.this, AliRtcChatActivity.class);
        Bundle b = new Bundle();
        //?????????
        String channel = getChannelId();
        b.putString("channel", channel);
        b.putSerializable("rtcAuthInfo", rtcAuthInfo);
        intent.putExtras(b);
        startActivity(intent);
        overridePendingTransition(R.anim.alivc_voice_call_login_fade_in, R.anim.alivc_voice_call_login_fade_out);
    }

    /**
     * ??????RTC?????????TOKEN??????
     */
    @Override
        public void onSuccess(String result) {
        try {
            AliUserInfoResponse aliUserInfoResponse = new Gson().fromJson(result, AliUserInfoResponse.class);
            if (aliUserInfoResponse != null) {
                rtcAuthInfo = aliUserInfoResponse.getAliUserInfo();
                userid = rtcAuthInfo != null ? rtcAuthInfo.getUserid() : "";
            }
            //??????????????????????????????????????????
            checkChannelUserNum();
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * ????????????????????????
     *
     * @param errorMsg ????????????
     */
    @Override
    public void onFaild(String errorMsg) {
        showToastInCenter(String.format(getString(R.string.alivc_voicecall_string_join_channel_error), errorMsg));
        hideLoadingView();
    }

    /**
     * ???????????????????????????????????????????????????
     */
    private void checkChannelUserNum() {
        String url = Constant.getChannelUserInfo();
        Map<String, String> params = createChannelUserInfoParams();
        OkHttpCientManager.getInstance().doGet(url, params, new OkhttpClient.HttpCallBack() {
            @Override
            public void onSuccess(String result) {
                try {
                    ChannelUserInfoResponse channelUserInfoResponse = new Gson().fromJson(result, ChannelUserInfoResponse.class);
                    if (channelUserInfoResponse != null && channelUserInfoResponse.getData() != null) {
                        int totalUserNum = channelUserInfoResponse.getData().getCommTotalNum();
                        if (totalUserNum < Constant.ALIVC_VOICE_CALL_MAX_CHANNEL_USER_NUM) {
                            //?????????
                            joinChannel(getChannelId());
                        } else {
                            hideLoadingView();
                            showJoinChannelFaild();
                        }
                    } else {
                        hideLoadingView();
                        showToastInCenter(getString(R.string.alivc_voicecall_string_hint_join_room_faild));
                    }
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    hideLoadingView();
                    showToastInCenter(String.format(getString(R.string.alivc_voicecall_string_join_channel_error), getString(R.string.alivc_voicecall_string_data_parse_error)));
                }
            }

            @Override
            public void onFaild(String errorMsg) {
                hideLoadingView();
                showToastInCenter(String.format(getString(R.string.alivc_voicecall_string_join_channel_error), errorMsg));
            }
        });
    }

    private void showJoinChannelFaild() {
        UIHandlerUtil.getInstance().postRunnable(new Runnable() {
            @Override
            public void run() {
                showToastInCenter(getString(R.string.alivc_voicecall_string_channel_num_too_more));
                clearChannelIdForEdit();
            }
        });
    }

    private void joinChannel(String channel) {
        List<String> gslb = rtcAuthInfo.getGslb();
        AliRtcAuthInfo userInfo = new AliRtcAuthInfo();
        userInfo.setConferenceId(channel);//??????ID
        userInfo.setAppid(rtcAuthInfo.getAppid());/* ??????ID */
        userInfo.setNonce(rtcAuthInfo.getNonce());/* ????????? */
        userInfo.setTimestamp(rtcAuthInfo.getTimestamp());/* ?????????*/
        userInfo.setUserId(userid);/* ??????ID */
        userInfo.setGslb(gslb.toArray(new String[0]));/* GSLB??????*/
        userInfo.setToken(rtcAuthInfo.getToken());/*????????????Token*/
        if (mEngine != null) {
            AliUserInfo.TurnBean turn = rtcAuthInfo.getTurn();
            mEngine.joinChannel(userInfo, turn == null ? getString(R.string.alivc_voicecall_string_me) : turn.getUsername());/* ?????????????????? */
        }
    }

    /**
     * ????????????????????????(???????????????????????????)
     */
    private AliRtcEngineEventListener mEventListener = new AliRtcEngineEventListener() {

        /**
         * ?????????????????????
         * @param i ?????????
         */
        @Override
        public void onJoinChannelResult(int i) {
            Log.i(TAG, "onJoinChannelResult: " + i);
            if (i != 0) {//??????????????????
                showToastInCenter(getString(R.string.alivc_voicecall_string_hint_join_room_faild));
            } else {
                UIHandlerUtil.getInstance().postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        toChatActivity();
                        hideLoadingView();
                    }
                });
            }
        }

        /**
         * ?????????????????????
         * @param i ?????????
         */
        @Override
        public void onLeaveChannelResult(int i) {
        }

        /**
         * ???????????????
         * @param i ?????????
         * @param s publishId
         */
        @Override
        public void onPublishResult(int i, String s) {

        }

        /**
         * ???????????????????????????
         * @param i ?????????
         */
        @Override
        public void onUnpublishResult(int i) {

        }

        /**
         * ?????????????????????
         * @param s userid
         * @param i ?????????
         * @param aliRtcVideoTrack ?????????track
         * @param aliRtcAudioTrack ?????????track
         */
        @Override
        public void onSubscribeResult(String s, int i, AliRtcEngine.AliRtcVideoTrack aliRtcVideoTrack,
                                      AliRtcEngine.AliRtcAudioTrack aliRtcAudioTrack) {
        }

        /**
         * ???????????????
         * @param i ?????????
         * @param s userid
         */
        @Override
        public void onUnsubscribeResult(int i, String s) {
        }

        /**
         * ???????????????????????????
         * @param aliRtcNetworkQuality1 ??????????????????
         * @param aliRtcNetworkQuality  ??????????????????
         * @param s  String  ??????ID
         */
        @Override
        public void onNetworkQualityChanged(String s, AliRtcEngine.AliRtcNetworkQuality aliRtcNetworkQuality, AliRtcEngine.AliRtcNetworkQuality aliRtcNetworkQuality1) {

        }

        /**
         * ?????????????????????
         * @param i ?????????
         */
        @Override
        public void onOccurWarning(int i) {

        }

        /**
         * ?????????????????????
         * @param error ?????????
         */
        @Override
        public void onOccurError(int error) {
        }

        /**
         * ????????????????????????
         */
        @Override
        public void onPerformanceLow() {

        }

        /**
         * ????????????????????????
         */
        @Override
        public void onPermormanceRecovery() {

        }

        /**
         * ????????????
         */
        @Override
        public void onConnectionLost() {
        }

        /**
         * ??????????????????
         */
        @Override
        public void onTryToReconnect() {
        }

        /**
         * ???????????????
         */
        @Override
        public void onConnectionRecovery() {
        }
    };

    /**
     * ????????????????????????????????????
     */
    private class ChannelIdTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (TextUtils.isEmpty(s)) {
                if (mTvConfirm.isEnabled()) {
                    mTvConfirm.setEnabled(false);
                }
                mEtChannelId.setBackgroundResource(R.drawable.alivc_voice_call_black_line_edit_bg_shape);
                return;
            }
            //??????????????????????????????
            if (!mTvConfirm.isEnabled()) {
                mTvConfirm.setEnabled(true);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    public void showToastInCenter(final String msg) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            ToastUtils.showInCenter(AliyunRtcLoginActivity.this, msg);
        } else {
            UIHandlerUtil.getInstance().postRunnable(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showInCenter(AliyunRtcLoginActivity.this, msg);
                }
            });
        }
    }
}
