package com.hrl.chaui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MineFragment extends Fragment {
    TextView name;
    TextView sign;
    TextView phone;
    CircleImageView img;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    SQLiteDatabase db;
    public final int Modify = 1;
    public final int ResetPwd = 2;
    Bundle bundle;
    Boolean isImgChange;
    String img_uri;

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
        sp= Objects.requireNonNull(getContext()).getSharedPreferences("data", Context.MODE_PRIVATE);
        editor=sp.edit();
        isImgChange=false;
        img_uri="";

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(v.getId()){
                    case R.id.modify:
                        Intent intent=new Intent(getActivity(), ModifyActivity.class);
                        startActivityForResult(intent, Modify);
                        break;
                    case R.id.modify_pwd:
                        Intent intent2=new Intent(getActivity(), ResetPwdActivity.class);
                        startActivityForResult(intent2,ResetPwd);
                        break;
                    case R.id.change:
                        Intent intent3=new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent3);
                        Objects.requireNonNull(getActivity()).finish();
                        break;
                    case R.id.user_img:
                        imgMax(((BitmapDrawable)img.getDrawable()).getBitmap());
                    default:
                        break;
                }
            }
        };

        modify.setOnClickListener(listener);
        modify_pwd.setOnClickListener(listener);
        change.setOnClickListener(listener);
        img.setOnClickListener(listener);



        return root;
    }


    @Override
    public void onResume() {
        super.onResume();
        name.setText(sp.getString("user_name",""));
        //Glide.with(Objects.requireNonNull(getContext())).load(getContext().getString(R.string.app_prefix_img)+sp.getString("user_img","")).into(img);
        sign.setText("个性签名: "+sp.getString("user_sign",""));
        phone.setText("手机号: "+sp.getString("user_phone",""));

        Log.i("isImgChang me resu",""+isImgChange);
        if (!sp.getString("user_img", "").equals("") && !isImgChange) {
            Glide.with(this).load(getString(R.string.app_prefix_img) + sp.getString("user_img", "")).into(img);
        } else if (!Objects.equals(img_uri, "") && isImgChange) {
            Log.i("isImgChange in Modify",""+isImgChange);
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(img_uri));
                img.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
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
/*
    @Override
    public void onFragmentResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==Modify) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    Bundle bundle = data.getExtras();
                    Log.i("json in Mine",bundle.getString("json"));
                }
            }
        }
    }

 */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Modify) {
            if (resultCode == Activity.RESULT_OK) {
                //Log.i("json in Main",data.getStringExtra("json"));
                //MineFragment.onFragmentResult(requestCode,resultCode,data);
                bundle = data.getExtras();
                isImgChange=bundle.getBoolean("isImgChange");
                img_uri=bundle.getString("img_uri");
                Log.i("json in Mine",bundle.getString("json"));
                Log.i("isImg in Result",""+bundle.getBoolean("isImgChange"));
            }
        }
        if(requestCode==ResetPwd){
            if(resultCode== Activity.RESULT_OK){
                System.out.println("*********bundle "+data);
                if(data!=null){
                    Bundle bundle = data.getExtras();
                    System.out.println("*********bundle "+bundle.getBoolean("isModigy"));
                    boolean isModify = bundle.getBoolean("isModify");
                    if (isModify) {

                    }
                }
            }
        }


    }
    /**
     * 点击查看大图
     */
    public void imgMax(Bitmap bitmap) {

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View imgEntryView = inflater.inflate(R.layout.dialog_photo_entry, null);
        // 加载自定义的布局文件
        final AlertDialog dialog = new AlertDialog.Builder(getContext()).create();
        ImageView img = (ImageView) imgEntryView.findViewById(R.id.large_image);
        img.setImageBitmap(bitmap);
        // 这个是加载网络图片的，可以是自己的图片设置方法
        // imageDownloader.download(imageBmList.get(0),img);
        dialog.setView(imgEntryView); // 自定义dialog
        dialog.show();
        // 点击布局文件（也可以理解为点击大图）后关闭dialog，这里的dialog不需要按钮
        imgEntryView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View paramView) {
                dialog.cancel();
            }
        });
    }
}
