package com.hrl.chaui.adapter;

import android.content.Context;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.hrl.chaui.R;
import com.hrl.chaui.bean.User;
import com.hrl.chaui.util.http;

import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.hrl.chaui.MyApplication.friendRequest;
import static com.hrl.chaui.MyApplication.modifyUser;

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
                ((ViewHolder) holder).add.setTextColor(mContext.getResources().getColor(R.color.top_bottom));
                ((ViewHolder) holder).add.setText("已添加");
                sendByPost(mContext,position,modifyUser.getUser_id(),user.getUser_id(),user.getUser_note(), user.getNotice_rank());
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
    private void sendByPost(Context mContext, Integer position,Integer user_id,Integer friend_id,String friend_note,Integer notice_rank) {
        JSONObject json = new JSONObject();
        json.put("user_id", user_id);
        json.put("friend_id",friend_id);
        json.put("friend_note", friend_note);
        json.put("notice_rank", notice_rank);
        String path = mContext.getResources().getString(R.string.request_local)+"/addFriend";
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
                Toast.makeText(mContext, "服务器连接失败", Toast.LENGTH_SHORT).show();
                Looper.loop();
                e.printStackTrace();
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                String info = response.body().string();
                JSONObject json = JSON.parseObject(info);
                if(json.getString("msg").equals("add success")){
                    Looper.prepare();
                    Toast.makeText(mContext, "添加成功!", Toast.LENGTH_SHORT).show();
                    friendRequest.remove(position);
                    http.sendByPost(mContext,modifyUser.getUser_id()); //获取联系人
                    Looper.loop();
                }
                else if(json.getString("msg").equals("add failed")){
                    Looper.prepare();
                    Toast.makeText(mContext, "添加失败!", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }

            }
        });
    }
}
