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
import com.hrl.chaui.activity.GroupActivity;
import com.hrl.chaui.activity.GroupChatActivity;
import com.hrl.chaui.activity.NewFriendActivity;
import com.hrl.chaui.activity.UserInfoActivity;
import com.hrl.chaui.bean.User;

import java.util.List;


public class ContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected Context mContext;
    protected List<User> mDatas;
    protected LayoutInflater mInflater;

    public ContactAdapter(Context mContext, List<User> mDatas) {
        this.mContext = mContext;
        this.mDatas = mDatas;
        mInflater = LayoutInflater.from(mContext);
    }

    public List<User> getDatas() {
        return mDatas;
    }

    public ContactAdapter setDatas(List<User> datas) {
        mDatas = datas;
        return this;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.item_user, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull  RecyclerView.ViewHolder holder, int position) {
        User user = mDatas.get(position);
        ((ViewHolder) holder).name.setText(user.getUser_name());
        if (user.getUser_name().equals("新的朋友")) {
            ((ViewHolder) holder).img.setImageResource(R.drawable.friend);
            ((ViewHolder) holder).content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(mContext, NewFriendActivity.class);
                    mContext.startActivity(intent);
                }
            });
        } else if (user.getUser_name().equals("群聊")) {
            ((ViewHolder) holder).img.setImageResource(R.drawable.group);
            ((ViewHolder) holder).content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(mContext, GroupActivity.class);
                    mContext.startActivity(intent);
                }
            });
        } else {
            Glide.with(mContext).load(mContext.getString(R.string.app_prefix_img) + mDatas.get(position).getUser_img()).into(((ViewHolder) holder).img);
            ((ViewHolder) holder).content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent;
                    Bundle bundle = new Bundle();
                    if(user.getType()==0){
                        intent = new Intent(mContext, UserInfoActivity.class);
                        bundle.putString("from","contact");
                        bundle.putBoolean("isFriend", true);
                    }
                    else {
                        intent=new Intent(mContext, GroupChatActivity.class);
                        User groupUser = getDatas().get(position);
                        intent.putExtra("targetUser", groupUser);
                    }
                    bundle.putInt("contact_id", user.getUser_id()); // 存放的是群聊的ID 或者 用户的ID
                    intent.putExtras(bundle);
                    mContext.startActivity(intent);
                    //((Activity) mContext).overridePendingTransition(R.anim.slide_left_in, 0);


                }
            });
        }
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
