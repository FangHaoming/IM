package com.hrl.chaui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aliyun.apsaravideo.sophon.bean.RTCAuthInfo;
import com.aliyun.apsaravideo.sophon.videocall.VideoCallActivity;
import com.aliyun.rtc.voicecall.bean.AliUserInfoResponse;
import com.aliyun.rtc.voicecall.ui.AliRtcChatActivity;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.hrl.chaui.adapter.ChatAdapter;
import com.hrl.chaui.bean.MsgType;
import com.hrl.chaui.bean.User;
import com.hrl.chaui.dao.imp.MessageDaoImp;
import com.hrl.chaui.util.LogUtil;
import com.hrl.chaui.bean.Message;
import com.hrl.chaui.R;
import com.hrl.chaui.bean.AudioMsgBody;
import com.hrl.chaui.bean.FileMsgBody;
import com.hrl.chaui.bean.ImageMsgBody;
import com.hrl.chaui.bean.MsgSendStatus;
import com.hrl.chaui.bean.TextMsgBody;
import com.hrl.chaui.bean.VideoMsgBody;
import com.hrl.chaui.util.ChatUiHelper;
import com.hrl.chaui.util.FileUtils;
import com.hrl.chaui.util.MqttByAli;
import com.hrl.chaui.util.MqttService;
import com.hrl.chaui.util.PictureFileUtil;
import com.hrl.chaui.util.RTCHelper;
import com.hrl.chaui.util.value;
import com.hrl.chaui.widget.MediaManager;
import com.hrl.chaui.widget.RecordButton;
import com.hrl.chaui.widget.StateButton;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.entity.LocalMedia;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.hrl.chaui.util.Constant.*;

/**
 * ???????????????????????????ChatActivity?????????????????????????????????
 * ???1???SharedPreferences?????? key=???user_id????????????
 * ???2???Intent???????????????????????????User??????????????????????????????????????? id???name ??????????????????
 */
public class ChatActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.llContent)
    LinearLayout mLlContent;
    @BindView(R.id.rv_chat_list)
    RecyclerView mRvChat;
    @BindView(R.id.et_content)
    EditText mEtContent;
    @BindView(R.id.bottom_layout)
    RelativeLayout mRlBottomLayout;//??????,??????????????????
    @BindView(R.id.ivAdd)
    ImageView mIvAdd;
    @BindView(R.id.ivEmo)
    ImageView mIvEmo;
    @BindView(R.id.btn_send)
    StateButton mBtnSend;//????????????
    @BindView(R.id.ivAudio)
    ImageView mIvAudio;//????????????
    @BindView(R.id.btnAudio)
    RecordButton mBtnAudio;//????????????
    @BindView(R.id.rlEmotion)
    LinearLayout mLlEmotion;//????????????
    @BindView(R.id.llAdd)
    LinearLayout mLlAdd;//????????????
    @BindView(R.id.swipe_chat)
    SwipeRefreshLayout mSwipeRefresh;//????????????

    private ChatAdapter mAdapter;
    public static final int REQUEST_CODE_IMAGE = 0000;
    public static final int REQUEST_CODE_VEDIO = 1111;
    public static final int REQUEST_CODE_FILE = 2222;

    private String targetClientID =  null;
    private String userClientID = null;
    private User targetUser = null;


    private final String TAG = "chatTest";
    private MqttByAli mqtt = null;

    private MessageReceiver messageReceiver = null; // ??????message arrive ??????
    private MessageDaoImp messageDaoImp = MessageDaoImp.getInstance();

    // ???Service????????????
    private MqttServiceConnection connection = null;

    // ??????????????????Handler
    NoOnlineHandler noOnlineHandler = null;

    // ?????????Item?????????
    int itemPos = -1;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

//        // ????????????????????????
        SharedPreferences userId=getSharedPreferences("data_userID",MODE_PRIVATE); //??????ID??????
        SharedPreferences sp=getSharedPreferences("data_"+userId.getInt("user_id",-1),MODE_PRIVATE); //??????ID????????????????????????
        userClientID = "GID_test@@@" + sp.getInt("user_id", -1);
        Log.e(TAG, "chatActivity onCreate()" +  "  userClientID:" + userClientID);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.white));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);



        // ????????????
        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.RECORD_AUDIO);
        permissions.add(Manifest.permission.CAMERA);
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.INTERNET);

        for (String s : permissions) { // ????????????
            if (ContextCompat.checkSelfPermission(this, s) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{s}, 1);
            }
        }


        // Service???????????????????????????????????????startService ??? bindService ?????? Service?????????
        Intent mqttServiceIntent = new Intent(this, MqttService.class);
        startService(mqttServiceIntent);
        connection = new MqttServiceConnection();
        // ??????MqttService ?????? MqttByAli??????
        bindService(mqttServiceIntent, connection, Context.BIND_AUTO_CREATE);


        // ???????????? MessageReceiver ???????????????
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MqttService.MESSAGEARRIVEACTION);
        messageReceiver = new MessageReceiver();
        registerReceiver(messageReceiver, intentFilter);

        // ???????????????????????????Handler
        noOnlineHandler = new NoOnlineHandler(this);

        initContent();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode) {
            case 1: {
                if (grantResults.length!=0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, permissions[0] + " denied", Toast.LENGTH_SHORT).show();
                    this.finish();
                }
                break;
            }
        }
    }

    @Override
    public void onResume() {

        if (mAdapter.getItemCount() != 0) {
            // ???ChatActivity????????????????????????????????????????????????
            super.onResume();
            return ;
        }

        // ???????????????????????????
        Intent intent = getIntent();
        targetUser = (User) intent.getSerializableExtra("targetUser");
        Log.e(TAG, "ChatActivity onResume()" + "  targetUser:" + targetUser.getUser_name());

        //  ????????????
        targetClientID = "GID_test@@@" + targetUser.getUser_id();
        TextView textView =(TextView) findViewById(R.id.common_toolbar_title);
        if(targetUser.getUser_note()!=null){
            textView.setText(targetUser.getUser_note());
        }else{
            textView.setText(targetUser.getUser_name());
        }


        // ???????????????????????????
        try {
            int uncheckedNums =  messageDaoImp.queryUncheckMessageNums(this, userClientID, targetClientID);
            uncheckedNums = uncheckedNums <= 10 ? 10 : uncheckedNums; // ??????10???
            List<Message> messageList =  messageDaoImp.queryMessage(this, userClientID, targetClientID, uncheckedNums);

            // ???????????????????????????????????????
            String[] uuids = new String[messageList.size()];
            for (int i = 0; i < messageList.size(); i++) {
                uuids[i] = messageList.get(i).getUuid();
            }
            messageDaoImp.checkMessage(this, uuids);

            // ??????????????????????????????????????????????????????
            Collections.reverse(messageList);
            // ?????????????????????
            mAdapter.addData(messageList);
            // ????????????????????????
            mRvChat.scrollToPosition(mAdapter.getItemCount()-1);

        } catch (IOException e) {
            Log.e(TAG,"messageDao query ???????????????");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            Log.e(TAG,"messageDao query ???????????????");
            e.printStackTrace();
        }

        super.onResume();
    }


    @Override
    protected void onDestroy() {
        Log.e(TAG, "ChatActivity Destory!!");

        //?????? ???????????????Receiver
        unregisterReceiver(messageReceiver);

        // ??????
        unbindService(connection);
        super.onDestroy();
    }

    private ImageView ivAudio;

    protected void initContent() {
        ButterKnife.bind(this);
        mAdapter = new ChatAdapter(this, new ArrayList<Message>(), "ChatActivity");
        LinearLayoutManager mLinearLayout = new LinearLayoutManager(this);
        mRvChat.setLayoutManager(mLinearLayout);
        mRvChat.setAdapter(mAdapter);
        mSwipeRefresh.setOnRefreshListener(this);
        initChatUi();
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initChatUi() {
        //mBtnAudio
        final ChatUiHelper mUiHelper = ChatUiHelper.with(this);
        mUiHelper.bindContentLayout(mLlContent)
                .bindttToSendButton(mBtnSend)
                .bindEditText(mEtContent)
                .bindBottomLayout(mRlBottomLayout)
                .bindEmojiLayout(mLlEmotion)
                .bindAddLayout(mLlAdd)
                .bindToAddButton(mIvAdd)
                .bindToEmojiButton(mIvEmo)
                .bindAudioBtn(mBtnAudio)
                .bindAudioIv(mIvAudio)
                .bindEmojiData();

        //??????????????????,??????????????????
        mRvChat.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    mRvChat.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mAdapter.getItemCount() > 0) {
                                mRvChat.smoothScrollToPosition(mAdapter.getItemCount() - 1);
                            }
                        }
                    });
                }
            }
        });

        //??????????????????????????????
        mRvChat.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mUiHelper.hideBottomLayout(false);
                mUiHelper.hideSoftInput();
                mEtContent.clearFocus();
                mIvEmo.setImageResource(R.mipmap.ic_emoji);
                return false;
            }
        });

        // ?????????????????????????????????
        ((RecordButton) mBtnAudio).setOnFinishedRecordListener(new RecordButton.OnFinishedRecordListener() {
            @Override
            public void onFinishedRecord(String audioPath, int time) {
                LogUtil.d("??????????????????");
                File file = new File(audioPath);
                if (file.exists()) {
                    sendAudioMessage(audioPath, time);
                }
            }
        });



        // ???????????????????????????
        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() { // RecyclerView Item ????????????
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) { // ?????????????????????

                // ???????????????Item
                Message msg = mAdapter.getItem(position);
                // ????????? ItemShowActivity
                Intent intent = new Intent(ChatActivity.this, ItemShowActivity.class);
                switch(view.getId()) {
                    case R.id.rlAudio: {
                        // Audio Item???????????????
                        // ????????????
                        final boolean isSend = mAdapter.getItem(position).getSenderId().equals(userClientID);
                        if (ivAudio != null) {
                            if (isSend) {
                                ivAudio.setBackgroundResource(R.mipmap.audio_animation_list_right_3);
                            } else {
                                ivAudio.setBackgroundResource(R.mipmap.audio_animation_list_left_3);
                            }
                            ivAudio = null;
                            MediaManager.reset();
                        } else {
                            ivAudio = view.findViewById(R.id.ivAudio);
                            MediaManager.reset();
                            if (isSend) {
                                ivAudio.setBackgroundResource(R.drawable.audio_animation_right_list);
                            } else {
                                ivAudio.setBackgroundResource(R.drawable.audio_animation_left_list);
                            }
                            AnimationDrawable drawable = (AnimationDrawable) ivAudio.getBackground();
                            drawable.start();
                            MediaManager.playSound(ChatActivity.this, ((AudioMsgBody) mAdapter.getData().get(position).getBody()).getLocalPath(), new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    if (isSend) {
                                        ivAudio.setBackgroundResource(R.mipmap.audio_animation_list_right_3);
                                    } else {
                                        ivAudio.setBackgroundResource(R.mipmap.audio_animation_list_left_3);
                                    }

                                    MediaManager.release();
                                }
                            });
                        }
                        break;
                    }
                    case R.id.chat_item_content_text: {
                        // ????????????item ????????????????????????
                        TextMsgBody textMsgBody = (TextMsgBody) msg.getBody();
                        intent.putExtra("msgType", "text");
                        intent.putExtra("textMsg", textMsgBody.getMessage());
                        startActivity(intent);
                        break;
                    }
                    case R.id.bivPic: {
                        // ????????????item???????????????
                        ImageMsgBody imageMsgBody = (ImageMsgBody) msg.getBody();
                        intent.putExtra("msgType", "image");
                        intent.putExtra("imagePath", imageMsgBody.getThumbUrl());
                        startActivity(intent);
                        break;
                    }
                    case R.id.ivPlay: {
                        // ?????????????????????
                        VideoMsgBody videoMsgBody = (VideoMsgBody) msg.getBody();
                        String videoPath = videoMsgBody.getLocalPath();
                        intent.putExtra("msgType", "video");
                        intent.putExtra("videoPath", videoPath);
                        startActivity(intent);
                        break;
                    }
                    case R.id.rc_msg_iv_file_type_image: {
                        // ?????????????????????
                        FileMsgBody fileMsgBody = (FileMsgBody) msg.getBody();
                        String filePath = fileMsgBody.getLocalPath();
                        intent.putExtra("msgType", "file");
                        intent.putExtra("filePath", filePath);
                        startActivity(intent);
                        break;
                    }
                }
            }
        });


        // title bar ???????????????
        ImageView titleBack = (ImageView) findViewById(R.id.title_back);
        titleBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatActivity.this.finish();
            }
        });
        // title bar ????????????????????????????????????????????????????????????
        ImageView menu = (ImageView) findViewById(R.id.chat_info);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this,UserInfoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("from","chat");
                bundle.putString("who","friend");
                bundle.putString("friend_from","chat");
                bundle.putString("friend_note",targetUser.getUser_note());
                bundle.putInt("contact_id",targetUser.getUser_id()); // ?????????????????????ID
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }


    // ?????????????????????????????????
    @Override
    public void onRefresh() {
        //????????????????????????????????????

        // ??????????????????item
        if (mAdapter.getItemCount() > 0) {
            // ?????????????????????
            Message topItem = mAdapter.getItem(0);
            long sendTime = topItem.getSentTime();
            try {
                List<Message> historyMessage = messageDaoImp.queryMessage(this, userClientID, targetClientID, 10, sendTime);
                if (historyMessage.size() == 0) {
                    Toast.makeText(this, "??????????????????", Toast.LENGTH_SHORT).show();
                } else {
                    Collections.reverse(historyMessage);
                    // ????????????????????????
                    mAdapter.addData(0, historyMessage);
                    mRvChat.scrollToPosition(historyMessage.size()-1);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        } else {
            // ????????????????????????
            try {
                List<Message> historyMessage = messageDaoImp.queryMessage(this, userClientID, targetClientID, 10);
                if (historyMessage.size() == 0) {
                    Toast.makeText(this, "??????????????????", Toast.LENGTH_SHORT).show();
                } else {
                    Collections.reverse(historyMessage);
                    // ????????????????????????
                    mAdapter.addData(0, historyMessage);
                    mRvChat.scrollToPosition(historyMessage.size()-1);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        mSwipeRefresh.setRefreshing(false);
    }


    // ?????? ??????????????????????????????????????????????????????????????????????????????????????? ???????????????????????????
    @OnClick({R.id.btn_send, R.id.rlPhoto, R.id.rlVideo, R.id.rlLocation, R.id.rlFile, R.id.rlPhone, R.id.rlVideoCall})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_send:
                sendTextMsg(mEtContent.getText().toString());
                mEtContent.setText("");
                break;
            case R.id.rlPhoto:
                PictureFileUtil.openGalleryPic(ChatActivity.this, REQUEST_CODE_IMAGE);
                break;
            case R.id.rlVideo:
                PictureFileUtil.openGalleryAudio(ChatActivity.this, REQUEST_CODE_VEDIO);
                break;
            case R.id.rlFile:
                PictureFileUtil.openFile(ChatActivity.this, REQUEST_CODE_FILE);
                break;
            case R.id.rlLocation:
                break;
            case R.id.rlPhone: {
                // ????????????
                Intent voiceCallIntent = new Intent(this, AliRtcChatActivity.class);
                String channelID = RTCHelper.getChannelID(userClientID, targetClientID);
                AliUserInfoResponse.AliUserInfo aliUserInfo = RTCHelper.getAliUserInfo(channelID, userClientID);
                voiceCallIntent.putExtra("channel", channelID);
                voiceCallIntent.putExtra("rtcAuthInfo", aliUserInfo);
                voiceCallIntent.putExtra("user2Name", targetUser.getUser_name());
                startActivity(voiceCallIntent);
                voiceCallIntent.putExtra("user2Name", targetUser.getUser_name());

                new Thread(()->{

                    try {
                        boolean isOnline = MqttByAli.checkIsOnline(targetClientID);
                        if (isOnline) {
                            mqtt.sendP2PVoiceCallRequest(targetClientID);
                            startActivity(voiceCallIntent);
                        } else {
                            android.os.Message msg =  new android.os.Message();
                            msg.what = NOTONLINE;
                            noOnlineHandler.sendMessage(msg);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        android.os.Message msg =  new android.os.Message();
                        msg.what = CHECKONLINEERR;
                        noOnlineHandler.sendMessage(msg);
                    }

                }).start();

                break;
            }
            case R.id.rlVideoCall: {
                Intent videoCallIntent = new Intent(this, VideoCallActivity.class);
                String channelID = RTCHelper.getNumsChannelID(userClientID, targetClientID);
                SharedPreferences sp = getSharedPreferences("data", Context.MODE_PRIVATE);
                String userName = sp.getString("user_name", "unknown");
                RTCAuthInfo info = RTCHelper.getVideoCallRTCAuthInfo(channelID, userClientID);
                videoCallIntent.putExtra("channel", channelID);
                videoCallIntent.putExtra("username", userName);
                videoCallIntent.putExtra("rtcAuthInfo", info);

                new Thread(()->{
                    try {
                        boolean isOnline = MqttByAli.checkIsOnline(targetClientID);
                        if (isOnline) {
                            mqtt.sendP2PVideoCallRequest(targetClientID);
                            startActivity(videoCallIntent);
                        } else {
                            android.os.Message msg =  new android.os.Message();
                            msg.what = NOTONLINE;
                            noOnlineHandler.sendMessage(msg);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        android.os.Message msg =  new android.os.Message();
                        msg.what = CHECKONLINEERR;
                        noOnlineHandler.sendMessage(msg);
                    }
                }).start();
                break;
            }
        }
    }


    // ??????onViewClicked??????PictureFileUtil???????????????(???????????????????????????????????????Activity???????????????)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_FILE:
                    String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
                    LogUtil.d("????????????????????????:" + filePath);
                    sendFileMessage(userClientID, targetClientID, filePath);
                    break;
                case REQUEST_CODE_IMAGE:
                    // ????????????????????????
                    List<LocalMedia> selectListPic = PictureSelector.obtainMultipleResult(data);
                    for (LocalMedia media : selectListPic) {
                        LogUtil.d("????????????????????????:" + media.getPath());
                        sendImageMessage(media);
                    }
                    break;
                case REQUEST_CODE_VEDIO:
                    // ????????????????????????
                    List<LocalMedia> selectListVideo = PictureSelector.obtainMultipleResult(data);
                    for (LocalMedia media : selectListVideo) {
                        LogUtil.d("????????????????????????:" + media.getPath());
                        sendVedioMessage(media);
                    }
                    break;
            }
        }
    }


    //????????????
    private void sendTextMsg(String hello)  {
        final Message mMessgae = getBaseSendMessage(MsgType.TEXT);
        TextMsgBody mTextMsgBody = new TextMsgBody();
        mTextMsgBody.setMessage(hello);
        mMessgae.setBody(mTextMsgBody);
        //????????????
        mAdapter.addData(mMessgae);
        //?????????????????????
        updateMsg(mMessgae);

        // ???message???????????????
        try {
            messageDaoImp.insertMessage(this, mMessgae);
        } catch (IOException e) {
            Log.e(TAG, "text message insert err, message:" + mMessgae);
            e.printStackTrace();
        }


        // ??????mqtt??????
        Log.e(TAG, "sendTextMsg:" + hello + " targerClientID:" + targetClientID);
        mqtt.sendTextP2P(hello, targetClientID);

    }

    //????????????
    private void sendImageMessage(final LocalMedia media) {
        final Message mMessgae = getBaseSendMessage(MsgType.IMAGE);
        ImageMsgBody mImageMsgBody = new ImageMsgBody();
        mImageMsgBody.setThumbUrl(media.getCompressPath()); // ???????????????
        mImageMsgBody.setLocalPath(media.getPath()); // ????????????
        mMessgae.setBody(mImageMsgBody);
        //????????????
        mAdapter.addData(mMessgae);
        //?????????????????????
        updateMsg(mMessgae);

        // ???message???????????????
        try {
            messageDaoImp.insertMessage(this, mMessgae);
        } catch (IOException e) {
            Log.e(TAG, "image message insert err, message:" + mMessgae);
            e.printStackTrace();
        }

        // ?????? mqtt ??????????????????
        File file = new File(media.getPath());
        mqtt.sendImageP2P(file, targetClientID);

    }

    //????????????
    private void sendVedioMessage(final LocalMedia media) {
        final Message mMessgae = getBaseSendMessage(MsgType.VIDEO);
        //?????????????????????
        String videoPath = media.getPath();
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(videoPath);
        Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime();
        String imgname = System.currentTimeMillis() + ".jpg";
//        String urlpath = Environment.getExternalStorageDirectory() + "/" + imgname;
        String urlpath = value.imgLocalPath + "/" + imgname;

        File f = new File(urlpath);
        try {
            if (f.exists()) {
                f.delete();
            }
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            LogUtil.d("????????????????????????????????????" + e.toString());
            e.printStackTrace();
        }
        VideoMsgBody mImageMsgBody = new VideoMsgBody();
        mImageMsgBody.setExtra(urlpath);
        mImageMsgBody.setLocalPath(videoPath);
        mMessgae.setBody(mImageMsgBody);
        //????????????
        mAdapter.addData(mMessgae);

        //??????item
        updateMsg(mMessgae);

        // ???message???????????????
        try {
            messageDaoImp.insertMessage(this, mMessgae);
        } catch (IOException e) {
            Log.e(TAG, "text message insert err, message:" + mMessgae);
            e.printStackTrace();
        }

        // mqtt ??????
        File file = new File(videoPath);
        mqtt.sendVideoP2P(file, targetClientID);

    }

    //????????????
    private void sendFileMessage(String from, String to, final String path) {
        final Message mMessgae = getBaseSendMessage(MsgType.FILE);
        FileMsgBody mFileMsgBody = new FileMsgBody();
        mFileMsgBody.setLocalPath(path);
        mFileMsgBody.setDisplayName(FileUtils.getFileName(path));
        mFileMsgBody.setSize(FileUtils.getFileLength(path));
        mMessgae.setBody(mFileMsgBody);
        //????????????
        mAdapter.addData(mMessgae);
        // ??????
        updateMsg(mMessgae);

        // ???message???????????????
        try {
            messageDaoImp.insertMessage(this, mMessgae);
        } catch (IOException e) {
            Log.e(TAG, "text message insert err, message:" + mMessgae);
            e.printStackTrace();
        }

        File file = new File(path);
        mqtt.sendFileP2P(file, targetClientID);

    }

    //????????????
    private void sendAudioMessage(final String path, int time) {
        final Message mMessgae = getBaseSendMessage(MsgType.AUDIO);
        AudioMsgBody mFileMsgBody = new AudioMsgBody();
        mFileMsgBody.setLocalPath(path);
        mFileMsgBody.setDuration(time);
        mMessgae.setBody(mFileMsgBody);
        //????????????
        mAdapter.addData(mMessgae);
        //???????????????????????????
        updateMsg(mMessgae);

        // ???message???????????????
        try {
            messageDaoImp.insertMessage(this, mMessgae);
        } catch (IOException e) {
            Log.e(TAG, "text message insert err, message:" + mMessgae);
            e.printStackTrace();
        }

        File file = new File(path);
        mqtt.sendAudioP2P(file, targetClientID, time);
    }


    // ???????????????Message???????????????
    private Message getBaseSendMessage(MsgType msgType) {
        Message mMessgae = new Message();
        mMessgae.setUuid(UUID.randomUUID() + "");
        mMessgae.setSenderId(userClientID);
        mMessgae.setTargetId(targetClientID);
        mMessgae.setSentTime(System.currentTimeMillis());
        mMessgae.setSentStatus(MsgSendStatus.SENDING);
        mMessgae.setMsgType(msgType);
        mMessgae.setCheck(true);
        mMessgae.setMsgId(null);
        mMessgae.setGroup(false);
        return mMessgae;
    }


    private Message getBaseReceiveMessage(MsgType msgType) {
        Message mMessgae = new Message();
        mMessgae.setUuid(UUID.randomUUID() + "");
        mMessgae.setSenderId(targetClientID);
        mMessgae.setTargetId(userClientID);
        mMessgae.setSentTime(System.currentTimeMillis());
        mMessgae.setSentStatus(MsgSendStatus.SENDING);
        mMessgae.setMsgType(msgType);
        return mMessgae;
    }


    private void updateMsg(final Message mMessgae) { // ???????????????
        mRvChat.scrollToPosition(mAdapter.getItemCount() - 1);

        int position = 0;
        mMessgae.setSentStatus(MsgSendStatus.SENT);
        //?????????????????????
        for (int i = 0; i < mAdapter.getData().size(); i++) {
            Message mAdapterMessage = mAdapter.getData().get(i);
            if (mMessgae.getUuid().equals(mAdapterMessage.getUuid())) {
                position = i;
            }
        }
        mAdapter.notifyItemChanged(position);
    }


    private class MqttServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MqttService.LocalBinder localBinder = (MqttService.LocalBinder) service;
            mqtt = localBinder.getService().getMqtt();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }


    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // ??????mqttService???????????????
            Message message = (Message) intent.getSerializableExtra("message");

            // ?????????????????????????????? ??? ????????????????????????????????????????????????
            if(message.getSenderId().equals(targetClientID) || message.getTargetId().equals(targetClientID)) {
                mAdapter.addData(message);
                mRvChat.scrollToPosition(mAdapter.getItemCount()-1); // ?????????????????????
                // ????????????????????????????????????????????????
                messageDaoImp = MessageDaoImp.getInstance();
                messageDaoImp.checkMessage(ChatActivity.this, message.getUuid());
            }
        }
    }


    public String getTargetClientID() {
        return targetClientID;
    }

    public String getUserClientID() {
        return userClientID;
    }

    private static class NoOnlineHandler extends Handler {
        private final WeakReference<ChatActivity> mTarget;

        private NoOnlineHandler(ChatActivity activity) {
            mTarget = new WeakReference<ChatActivity>(activity);
        }


        @Override
        public void handleMessage(@NonNull android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case NOTONLINE:
                    Toast.makeText(mTarget.get(), "???????????????", Toast.LENGTH_SHORT).show();
                    break;
                case CHECKONLINEERR:
                    Toast.makeText(mTarget.get(), "?????????????????????????????????",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

}
