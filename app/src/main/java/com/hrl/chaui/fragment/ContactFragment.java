package com.hrl.chaui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hrl.chaui.R;
import com.hrl.chaui.adapter.ContacAdapter;
import com.hrl.chaui.bean.User;
import com.mcxtzhang.indexlib.IndexBar.widget.IndexBar;
import com.mcxtzhang.indexlib.suspension.SuspensionDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ContactFragment extends Fragment {
    private RecyclerView mRv;
    private LinearLayoutManager mManager;
    private List<User> mDatas=new ArrayList<>();
    private SuspensionDecoration mDecoration;
    private ContacAdapter mAdapter;

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
        //使用indexBar
        mRv = (RecyclerView) root.findViewById(R.id.rv);
        mRv.setLayoutManager(mManager = new LinearLayoutManager(getContext()));
        TextView mTvSideBarHint = (TextView) root.findViewById(R.id.tvSideBarHint);//HintTextView
        IndexBar mIndexBar = (IndexBar) root.findViewById(R.id.indexBar);//IndexBar
        mAdapter = new ContacAdapter(getContext(), mDatas);
        mRv.setAdapter(mAdapter);
        mRv.addItemDecoration(mDecoration = new SuspensionDecoration(getContext(), mDatas));
        //如果add两个，那么按照先后顺序，依次渲染。
        mRv.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        mDatas.add((User) new User("新的朋友").setTop(true).setBaseIndexTag("↑"));
        mDatas.add((User) new User("群聊").setTop(true).setBaseIndexTag("↑"));
        mDatas.add((User) new User("标签").setTop(true).setBaseIndexTag("↑"));
        mDatas.add((User) new User("公众号").setTop(true).setBaseIndexTag("↑"));
        String[] data=getContext().getResources().getStringArray(R.array.provinces);
        for (int i = 0; i < data.length; i++) {
            User user = new User();
            user.setName(data[i]);//设置城市名称
            mDatas.add(user);
        }
        mAdapter.setDatas(mDatas);
        mAdapter.notifyDataSetChanged();

        mIndexBar.setmSourceDatas(mDatas)//设置数据
                .invalidate();
        mDecoration.setmDatas(mDatas);
        mIndexBar.setmPressedShowTextView(mTvSideBarHint)//设置HintTextView
                .setNeedRealIndex(true)//设置需要真实的索引
                .setmLayoutManager(mManager)//设置RecyclerView的LayoutManager
                .setmSourceDatas(mDatas);//设置数据源
        return root;
    }
}
