package com.hrl.chaui.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hrl.chaui.R;
import com.hrl.chaui.activity.UserInfoActivity;
import com.hrl.chaui.bean.User;

import java.util.List;


public class GroupMemberAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected Context mContext;
    protected List<User> mDatas;
    protected LayoutInflater mInflater;

    public GroupMemberAdapter(Context mContext, List<User> mDatas) {
        this.mContext = mContext;
        this.mDatas = mDatas;
        mInflater = LayoutInflater.from(mContext);
    }

    public List<User> getDatas() {
        return mDatas;
    }

    public GroupMemberAdapter setDatas(List<User> datas) {
        mDatas = datas;
        return this;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.item_group_member, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull  RecyclerView.ViewHolder holder, int position) {
        User user = mDatas.get(position);
        ((ViewHolder) holder).name.setText(user.getName());
        //((ViewHolder)holder).img.setImageResource(user.);
        Glide.with(mContext).load(mContext.getString(R.string.app_prefix_img) + mDatas.get(position).getImg()).into(((ViewHolder) holder).img);
        ((ViewHolder) holder).content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(mContext, UserInfoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("contact_id", user.getId());
                intent.putExtras(bundle);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
                mContext.startActivity(intent);
                //((Activity) mContext).overridePendingTransition(R.anim.slide_left_in, 0);


            }
        });
    }

    @Override
    public int getItemCount() {
        return mDatas != null ? mDatas.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView img;
        View content;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            img = (ImageView) itemView.findViewById(R.id.img);
            content = itemView.findViewById(R.id.content);
        }
    }
}
