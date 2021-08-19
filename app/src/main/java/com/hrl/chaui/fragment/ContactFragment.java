package com.hrl.chaui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hrl.chaui.R;
import com.hrl.chaui.bean.ContactEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ContactFragment extends Fragment {

    ExpandableListView expandableListView;
    List<ContactEntity> list;
    List<String> groupdata;
    //QQContactAdapter adapter;
    Map<String,List<ContactEntity>> map;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root=inflater.inflate(R.layout.layout_contact,container,false);
        SharedPreferences sharedPreferences = Objects.requireNonNull(getContext()).getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        expandableListView=root.findViewById(R.id.expanded_list);
        map=new HashMap<>();
        groupdata=new ArrayList<>();
        list=new ArrayList<>();
        groupdata.add("吴");
        groupdata.add("蜀");
        groupdata.add("魏");

        //adapter=new QQContactAdapter(groupdata,map,getContext());
        //expandableListView.setAdapter(adapter);

        return root;
    }
}
