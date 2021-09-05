package com.hrl.chaui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hrl.chaui.R;
import com.hrl.chaui.bean.User;

import java.util.List;


public class NewFriendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected Context mContext;
    protected List<User> mDatas;
    protected LayoutInflater mInflater;

    public NewFriendAdapter(Context mContext, List<User> mDatas) {
        this.mContext = mContext;
        this.mDatas = mDatas;
        mInflater = LayoutInflater.from(mContext);
    }

    public List<User> getDatas() {
        return mDatas;
    }

    public NewFriendAdapter setDatas(List<User> datas) {
        mDatas = datas;
        return this;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.item_new_friend, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull  RecyclerView.ViewHolder holder, int position) {
        User user = mDatas.get(position);
        ((ViewHolder) holder).name.setText(user.getUser_name());
        //((ViewHolder)holder).img.setImageResource(user.);
        Glide.with(mContext).load(mContext.getString(R.string.app_prefix_img) + mDatas.get(position).getUser_img()).into(((ViewHolder) holder).img);
        ((ViewHolder) holder).btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ViewHolder) holder).btn.setBackgroundColor(mContext.getResources().getColor(R.color.white));
                ((ViewHolder) holder).btn.setTextColor(mContext.getResources().getColor(R.color.black));
                ((ViewHolder) holder).btn.setText("已添加");
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
        TextView btn;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            img = (ImageView) itemView.findViewById(R.id.img);
            btn = itemView.findViewById(R.id.btn);
        }
    }
}
