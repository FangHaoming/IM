package com.hrl.chaui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hrl.chaui.R;
import com.hrl.chaui.bean.User;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GridViewAdapter extends BaseAdapter {
    String[] mname;
    int[] icon;
    List<User> users;
    Context context;

    public GridViewAdapter(Context context,List<User> users) {
        super();
        this.users=users;
        this.context=context;
    }
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
//          有了布局再设置里面的控件.(holder设置控件)
            convertView.setTag(holder);
        }else{
            holder= (viewholder) convertView.getTag();
        }
//      给控件赋值
        Glide.with(context).load(context.getString(R.string.app_prefix_img)+users.get(position).getImg()).into(holder.img);
        holder.name.setText(users.get(position).getName());
        return convertView;
    }
    class viewholder{//数据包
        CircleImageView img;
        TextView name;
    }
}
