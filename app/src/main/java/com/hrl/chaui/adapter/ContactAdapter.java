package com.hrl.chaui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hrl.chaui.R;
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
        final User user = mDatas.get(position);
        ((ViewHolder)holder).name.setText(user.getName());
        ((ViewHolder)holder).img.setImageResource(R.drawable.default0);
        if(user.getName().equals("新的朋友")){
            ((ViewHolder)holder).img.setImageResource(R.drawable.friend);
            ((ViewHolder)holder).content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext.getApplicationContext(), "新的朋友",Toast.LENGTH_SHORT).show();
                }
            });
        }
        else if(user.getName().equals("群聊")){
            ((ViewHolder)holder).img.setImageResource(R.drawable.group);
            ((ViewHolder)holder).content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext.getApplicationContext(), "群聊",Toast.LENGTH_SHORT).show();
                }
            });
        }
        else{
            //Glide.with(mContext).load(mContext.getString(R.string.app_prefix_img)+mDatas.get(position).getImg()).into(holder.img);
            ((ViewHolder)holder).content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext, "pos:" + position, Toast.LENGTH_SHORT).show();
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
