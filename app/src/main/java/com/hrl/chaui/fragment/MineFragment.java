package com.hrl.chaui.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.hrl.chaui.R;
import com.hrl.chaui.activity.LoginActivity;
import com.hrl.chaui.activity.ModifyActivity;
import com.hrl.chaui.activity.ResetPwdActivity;
import com.hrl.chaui.util.MyDBHelper;

import java.util.Objects;

public class MineFragment extends Fragment {
    TextView name;
    TextView sign;
    TextView phone;
    ImageView img;
    SharedPreferences sp;
    SQLiteDatabase db;
    @Nullable
    @Override
    public View onCreateView(@NonNull  LayoutInflater inflater, @Nullable  ViewGroup container, @Nullable  Bundle savedInstanceState) {
        View root=inflater.inflate(R.layout.layout_mine,container,false);
        img=root.findViewById(R.id.user_img);
        name=root.findViewById(R.id.user_name);
        sign=root.findViewById(R.id.user_sign);
        phone=root.findViewById(R.id.user_phone);
        TextView modify=root.findViewById(R.id.modify);
        TextView modify_pwd=root.findViewById(R.id.modify_pwd);
        TextView change=root.findViewById(R.id.change);
        TextView delete=root.findViewById(R.id.delete);
        sp= Objects.requireNonNull(getContext()).getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sp.edit();
        MyDBHelper dbHelper=new MyDBHelper(getContext(),"DB",null,1);
        db=dbHelper.getWritableDatabase();


        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(v.getId()){
                    case R.id.modify:
                        Intent intent=new Intent(getContext(), ModifyActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.modify_pwd:
                        Intent intent2=new Intent(getContext(), ResetPwdActivity.class);
                        startActivity(intent2);
                        break;
                    case R.id.change:
                        Intent intent3=new Intent(getContext(), LoginActivity.class);
                        startActivity(intent3);
                        Objects.requireNonNull(getActivity()).finish();
                        break;
                    case R.id.delete:
                        showDialog();
                        break;
                    default:
                        break;
                }
            }
        };

        modify.setOnClickListener(listener);
        modify_pwd.setOnClickListener(listener);
        change.setOnClickListener(listener);
        delete.setOnClickListener(listener);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        name.setText(sp.getString("user_name",""));
        Glide.with(Objects.requireNonNull(getContext())).load(getContext().getString(R.string.app_prefix_img)+sp.getString("user_img","")).into(img);
        sign.setText("个性签名: "+sp.getString("user_sign",""));
        phone.setText("手机号: "+sp.getString("user_phone",""));
    }

    private void showDialog(){
        final AlertDialog.Builder dialog=new AlertDialog.Builder(getContext());
        dialog.setMessage("确定要注销账号吗？该过程不可逆");
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //db.delete("QQ_Login","QQname=? AND QQpwd=?",new String[]{sp.getString("QQname",""),sp.getString("QQpwd","")});
                Intent intent4=new Intent(getContext(), LoginActivity.class);
                startActivity(intent4);
                Objects.requireNonNull(getActivity()).finish();
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.show();
    }


}
