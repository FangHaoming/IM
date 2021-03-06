package com.hrl.chaui.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hrl.chaui.R;
import com.hrl.chaui.activity.UserInfoActivity;
import com.hrl.chaui.bean.User;
import com.hrl.chaui.util.Is;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import static com.hrl.chaui.MyApplication.modifyUser;

public class GroupMemberAdapter extends BaseAdapter {
    String[] mname;
    int[] icon;
    List<User> users;
    Context context;
    String group_name;

    public GroupMemberAdapter(Context context, List<User> users) {
        super();
        this.users=users;
        this.context=context;
    }
    public void setGroup_name(String name){this.group_name=name;}

    //设置条目的总数(一般以集合的长度,因为集合长度可变)
    @Override
    public int getCount() {
        return users==null?0:users.size();
    }
    //返回指定下标对应的数据对象
    @Override
    public Object getItem(int position) {
        return users.get(position);
    }
    //返回每个条目的ID
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//      优化
        viewholder holder =null;//else访问到同一个
        if(convertView==null){//如果显示的条目等于空是我再给他赋值,
//          不然会一直赋值导致内存溢出
            holder =new viewholder();//新建一个存放数据的
            //加载一个hen布局赋值到convertView,Item的视图对象
            convertView=View.inflate(context, R.layout.item_group_member, null);
//          convertView.findViewById//必须明写
            holder.img=(CircleImageView) convertView.findViewById(R.id.img);
//          hen里面的image和text
            holder.name=(TextView) convertView.findViewById(R.id.name);
            holder.content=convertView.findViewById(R.id.content);
//          有了布局再设置里面的控件.(holder设置控件)
            convertView.setTag(holder);
            holder.content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent;
                    intent = new Intent(context, UserInfoActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("contact_id", users.get(position).getUser_id());
                    bundle.putString("from","group");
                    if(Is.isFriendByPhone(users.get(position).getUser_phone())){
                        bundle.putString("who","friend");
                        bundle.putString("friend_from","group");
                        bundle.putInt("contact_id",users.get(position).getUser_id());
                        bundle.putString("friend_note",users.get(position).getUser_note());
                    }
                    else{
                        if(users.get(position).getUser_id().equals(modifyUser.getUser_id())){
                            bundle.putString("who","me");
                        }
                        else{
                            bundle.putString("who","stranger");
                            bundle.putString("user_phone",users.get(position).getUser_phone());
                        }
                    }
                    bundle.putString("nickname",users.get(position).getNickname());
                    bundle.putString("group_name",group_name);
                    intent.putExtras(bundle);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            });
        }else{
            holder= (viewholder) convertView.getTag();
        }
        //ViewUtils.setGridViewItemWith(convertView,parent,50,10,10,false);
//      给控件赋值
        Glide.with(context).load(context.getString(R.string.app_prefix_img)+users.get(position).getUser_img()).into(holder.img);
        holder.name.setText(users.get(position).getUser_name());
        return convertView;
    }
    public static class viewholder{//数据包
        CircleImageView img;
        TextView name;
        View content;
    }
}
