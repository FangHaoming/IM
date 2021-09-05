package com.hrl.chaui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hrl.chaui.R;
import com.hrl.chaui.adapter.ContactAdapter;
import com.hrl.chaui.bean.User;
import com.mcxtzhang.indexlib.IndexBar.widget.IndexBar;
import com.mcxtzhang.indexlib.suspension.SuspensionDecoration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.hrl.chaui.MyApplication.contactData;
import static com.hrl.chaui.MyApplication.groupData;

public class ContactFragment extends Fragment {
    private RecyclerView mRv;
    private LinearLayoutManager mManager;
    private SuspensionDecoration mDecoration;
    private ContactAdapter mAdapter;
    public SharedPreferences recv;
    public SharedPreferences.Editor editor;
    public TextView mTvSideBarHint;
    public IndexBar mIndexBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root=inflater.inflate(R.layout.layout_contact,container,false);
        recv = Objects.requireNonNull(getContext()).getSharedPreferences("data", Context.MODE_PRIVATE);
        editor = recv.edit();
        editor.putBoolean("isImgChange",false);
        editor.apply();
        //使用indexBar
        mRv = (RecyclerView) root.findViewById(R.id.rv);
        mRv.setLayoutManager(mManager = new LinearLayoutManager(getContext()));
        mTvSideBarHint = (TextView) root.findViewById(R.id.tvSideBarHint);//HintTextView
        mIndexBar = (IndexBar) root.findViewById(R.id.indexBar);//IndexBar
        sendByPost(recv.getInt("user_id",0));


        return root;
    }
    private void sendByPost(Integer user_id) {
        JSONObject json = new JSONObject();
        json.put("user_id", user_id);
        String path = getContext().getResources().getString(R.string.request_local)+"/userContacts";
        OkHttpClient client = new OkHttpClient();
        final FormBody formBody = new FormBody.Builder()
                .add("json", json.toJSONString())
                .build();
        Request request = new Request.Builder()
                .url(path)
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Looper.prepare();
                Toast.makeText(getContext(), "服务器连接失败", Toast.LENGTH_SHORT).show();
                Looper.loop();
                e.printStackTrace();
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                String info = response.body().string();
                JSONObject json = JSON.parseObject(info);
                JSONArray contacts = json.getJSONArray("contacts");
                System.out.println("*********contact" + json.toJSONString());
                contactData = new ArrayList<>();
                groupData = new ArrayList<>();
                contactData.add((User) new User("新的朋友","-1").setTop(true).setBaseIndexTag("↑"));
                contactData.add((User) new User("群聊","-1").setTop(true).setBaseIndexTag("↑"));
                for (int i = 0; i < contacts.size(); i++) {
                    JSONObject obj = contacts.getJSONObject(i);
                    if (obj.getInteger("type") == 0) {
                        contactData.add(new User(obj.getInteger("contact_id"), obj.getString("contact_img"), obj.getString("contact_name"),obj.getString("user_phone"), obj.getInteger("type")));
                    } else {
                        groupData.add(new User(obj.getInteger("contact_id"), obj.getString("contact_img"), obj.getString("contact_name"),obj.getString("user_phone"), obj.getInteger("type")));

                    }
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter = new ContactAdapter(getContext(), contactData);
                                mRv.setAdapter(mAdapter);
                                mRv.addItemDecoration(mDecoration = new SuspensionDecoration(getContext(), contactData));
                                mRv.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));


                                mIndexBar.setmPressedShowTextView(mTvSideBarHint)//设置HintTextView
                                        .setNeedRealIndex(true)//设置需要真实的索引
                                        .setmLayoutManager(mManager);//设置RecyclerView的LayoutManager
                                mAdapter.setDatas(contactData);
                                mAdapter.notifyDataSetChanged();

                                mIndexBar.setmSourceDatas(contactData)//设置数据
                                        .invalidate();
                                mDecoration.setmDatas(contactData);
                            }
                        });
                    }
                }).start();

            }
        });
    }
}
