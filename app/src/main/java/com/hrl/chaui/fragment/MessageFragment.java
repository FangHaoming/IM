package com.hrl.chaui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hrl.chaui.R;
import com.hrl.chaui.adapter.MessageAdapter;
import com.hrl.chaui.bean.MessageEntity;
import com.hrl.chaui.util.MyDBHelper;
import com.hrl.chaui.util.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MessageFragment extends Fragment {
    RecyclerView recyclerView;
    List<MessageEntity> list;
    MessageAdapter adapter;
    MyDBHelper dbHelper;
    SharedPreferences rev;
    SharedPreferences.Editor editor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root=inflater.inflate(R.layout.layout_message,container,false);
        rev = Objects.requireNonNull(getContext()).getSharedPreferences("data", Context.MODE_PRIVATE);
        editor = rev.edit();
        http.sendByPost(getContext(),rev.getInt("user_id",0));

        recyclerView=root.findViewById(R.id.recyclerView);
        list=new ArrayList<>();

        JSONObject json= JSON.parseObject(rev.getString("message",""));
        //int size= json.getInteger("size");
        int size=0;
        for(int i=0;i<size;i++){
            JSONObject j= (JSONObject) json.get(i+"");
            assert j != null;
            list.add(new MessageEntity(j.getString("name"),j.getString("url_img"),j.getString("time"),j.getString("message")));
        }
        //final String INSERT_DATA="insert into QQ_Login(QQname,QQpwd,QQimg)values(?,?,?)";

        adapter=new MessageAdapter(getContext(),list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        return root;
    }
}
