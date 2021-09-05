package com.hrl.chaui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.hrl.chaui.R;
import com.hrl.chaui.activity.ChatActivity;
import com.hrl.chaui.activity.GroupChatActivity;
import com.hrl.chaui.adapter.MessageAdapter;
import com.hrl.chaui.bean.Message;
import com.hrl.chaui.bean.User;
import com.hrl.chaui.dao.imp.MessageDaoImp;
import com.hrl.chaui.util.MqttService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.hrl.chaui.MyApplication.contactData;
import static com.hrl.chaui.MyApplication.groupData;

public class MessageFragment extends Fragment {

    // 布局
    View root;

    // 显示消息的recyclerview
    RecyclerView recyclerView;

    // 下拉刷新
    SwipeRefreshLayout mSwipeRefresh;

    // 工具栏的标题
    TextView toolBarTitle;

    // 适配器
    MessageAdapter mAdapter;

    // message arrive Receiver
    MessageReceiver messageReceiver;

    // 用户ID
    String userClientID;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.layout_message,container,false);
        // 获取用户id
        SharedPreferences sharedPreferences = Objects.requireNonNull(getContext()).getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean("isImgChange",false);
        editor.apply();
        int user_id = sharedPreferences.getInt("user_id", -1);
        userClientID = "GID_test@@@" + user_id;


        // 动态注册 Receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MqttService.MESSAGEARRIVEACTION);
        messageReceiver = new MessageReceiver();
        getContext().registerReceiver(messageReceiver, intentFilter);

        initMessageUI();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        MessageDaoImp messageDaoImp = MessageDaoImp.getInstance();
        List<Message> list = null;
        try {
            list = messageDaoImp.queryLatestDifMessage(getContext(),userClientID);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (list != null) {
            mAdapter.setNewData(list);
        } else {
            Toast.makeText(this.getContext(), "没有消息",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unregisterReceiver(messageReceiver);
    }

    private void initMessageUI() {
        // 获取布局中的元素
        recyclerView = root.findViewById(R.id.message_recyclerview);
        mSwipeRefresh = root.findViewById(R.id.swipe_message);

        // 设置适配器
        mAdapter = new MessageAdapter(this.getContext(), new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mAdapter);

        // 设置点击监听器(跳转到相应的聊天界面)
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                Message message = (Message)adapter.getItem(position);

                if (message.isGroup()) {
                    // 群聊信息
                    String targetID = message.getTargetId();
                    int groupID = Integer.valueOf(targetID);
                    for (User group : groupData) {
                        if (group.getId() == groupID) {
                            Intent groupChatIntent = new Intent(MessageFragment.this.getContext(), GroupChatActivity.class);
                            groupChatIntent.putExtra("targetUser", group);
                            startActivity(groupChatIntent);
                            break;
                        }
                    }
                } else {
                    // 私聊信息
                    String targetID = message.getSenderId().equals(userClientID) ? message.getTargetId() : message.getSenderId();

                    int target_id = Integer.valueOf(targetID.split("@@@")[1]);

                    User targetUser = new User();
                    targetUser.setId(target_id);
                    for (User user : contactData) {
                        if (user.getId() != null && user.getId() == target_id) {
                            targetUser.setName(user.getName());
                            targetUser.setNote(user.getNote());
                            targetUser.setImg(user.getImg());
                            targetUser.setSign(user.getSign());
                            targetUser.setGender(user.getGender());
                            targetUser.setType(user.getType());
                            targetUser.setTop(user.isTop());
                            targetUser.setPhone(user.getPhone());
                            targetUser.setBaseIndexPinyin(user.getBaseIndexPinyin());
                            targetUser.setBaseIndexTag(user.getBaseIndexTag());
                            break;
                        }
                    }

                    Intent chatIntent = new Intent(MessageFragment.this.getContext(), ChatActivity.class);
                    chatIntent.putExtra("targetUser", targetUser);
                    startActivity(chatIntent);
                }


            }
        });

        // 设置SwipeRefreshLayout 监听器 （下拉刷新）
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                MessageDaoImp messageDaoImp = MessageDaoImp.getInstance();
                List<Message> list = null;
                try {
                    list = messageDaoImp.queryLatestDifMessage(getContext(),userClientID);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                if (list != null) {
                    mAdapter.setNewData(list);
                }

                mSwipeRefresh.setRefreshing(false);
            }
        });

    }


    class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Message message = (Message) intent.getSerializableExtra("message");

            List<Message> messageList = mAdapter.getData();
            int pos = -1 ;

            if (!message.isGroup()) {
                // 私聊消息的更新
                for (int i = 0; i < messageList.size(); i++) {
                    Message localMessage = messageList.get(i);
                    if (localMessage.getSenderId().equals(message.getSenderId())
                            || localMessage.getTargetId().equals(message.getSenderId())) {
                        pos = i;
                        break;
                    }
                }
            } else {
                // 群聊消息的更新
                for (int i = 0; i < messageList.size(); i++) {
                    Message localMessage = messageList.get(i);
                    if (localMessage.getTargetId().equals(message.getTargetId())) {
                        pos = i;
                        break;
                    }
                }
            }

            if(pos != -1) {
                // 如果已存在消息则将该消息删除
                messageList.remove(pos);
            }


            messageList.add(0, message);
            mAdapter.setNewData(messageList);

        }
    }
}
