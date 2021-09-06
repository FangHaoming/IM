package com.hrl.chaui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hrl.chaui.R;
import com.hrl.chaui.bean.User;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


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
        ((ViewHolder) holder).check.setText(user.getCheck());
        //((ViewHolder)holder).img.setImageResource(user.);
        Glide.with(mContext).load(mContext.getString(R.string.app_prefix_img) + mDatas.get(position).getUser_img()).into(((ViewHolder) holder).img);
        ((ViewHolder) holder).add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ViewHolder) holder).add.setBackgroundColor(mContext.getResources().getColor(R.color.white));
                ((ViewHolder) holder).add.setTextColor(mContext.getResources().getColor(R.color.black));
                ((ViewHolder) holder).add.setText("已添加");
                //TODO 在“新的朋友界面点击”添加“之后怎么处理
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDatas != null ? mDatas.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView check;
        CircleImageView img;
        TextView add;
        View content;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            check=itemView.findViewById(R.id.check);
            img = itemView.findViewById(R.id.img);
            add = itemView.findViewById(R.id.add);
            content=itemView.findViewById(R.id.content);
        }
    }
}
