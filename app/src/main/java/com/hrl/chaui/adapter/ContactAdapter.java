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
import com.hrl.chaui.bean.MessageEntity;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<MessageEntity> list;

    public ContactAdapter(Context mContext, List<MessageEntity> list) {
        this.mContext = mContext;
        this.list = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(mContext).inflate(R.layout.layout_messageadapter,parent,false);

        return new ListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull  RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof ListViewHolder){
            Glide.with(mContext).load(list.get(position).getUrl_img()).into(((ListViewHolder)holder).img);
            ((ListViewHolder)holder).name.setText(list.get(position).getName());
            ((ListViewHolder)holder).time.setText(list.get(position).getTime());
            ((ListViewHolder)holder).message.setText(list.get(position).getMessages());
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        ImageView img;
        TextView time;
        TextView message;
        public ListViewHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.name);
            img=itemView.findViewById(R.id.img);
            time=itemView.findViewById(R.id.time);
            message=itemView.findViewById(R.id.mes);
        }
    }
}
