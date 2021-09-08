package com.hrl.chaui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.hrl.chaui.R;
import com.hrl.chaui.activity.LoginActivity;
import com.hrl.chaui.activity.ModifyActivity;
import com.hrl.chaui.activity.ResetPwdActivity;
import com.hrl.chaui.util.AppManager;
import com.hrl.chaui.util.http;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;
import static com.hrl.chaui.MyApplication.modifyUser;

public class MineFragment extends Fragment {
    TextView name;
    TextView sign;
    TextView phone;
    CircleImageView img;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
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

        SharedPreferences userId=Objects.requireNonNull(getContext()).getSharedPreferences("data_userID",MODE_PRIVATE); //存用户登录ID
        sp=Objects.requireNonNull(getContext()).getSharedPreferences("data_"+userId.getInt("user_id",-1),MODE_PRIVATE); //根据ID获取用户数据文件
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
                        //TODO 重新登陆 结束掉mqttService  有待测试
                        Intent intent3=new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent3);
                        /*Service service= Objects.requireNonNull(getContext()).getSystemService(MqttService.class);
                        service.stopSelf();*/
                        AppManager.AppExit(getContext());
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
        //TODO 头像显示有点问题
        Log.i("isImgChang me resu",""+isImgChange);
        Log.i("user_img in Mine",sp.getString("user_img", ""));
        if (!sp.getString("user_img", "").equals("") && !isImgChange) {
            Glide.with(this).load(getString(R.string.app_prefix_img) +sp.getString("user_img", "")).into(img);
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
                http.sendByPostLogin(getContext(),modifyUser.getUser_phone(),modifyUser.getUser_pwd());
                bundle = data.getExtras();
                isImgChange=bundle.getBoolean("isImgChange");
                Log.i("isImg onResult",""+isImgChange);
                img_uri=bundle.getString("img_uri");
                if(bundle.getBoolean("isModify")||isImgChange){
                    sendByPost_upDate(bundle.getString("json"));
                }
            }
        }
        if(requestCode==ResetPwd){
            if(resultCode== Activity.RESULT_OK){
                System.out.println("*********bundle "+data);
                if(data!=null){
                    Bundle bundle = data.getExtras();
                    System.out.println("*********userUpdate  "+bundle.getString("json"));
                    boolean isModify = bundle.getBoolean("isModify");
                    if (isModify) {
                        String json=bundle.getString("json");
                        sendByPost_upDate(json);
                    }
                }
            }
        }


    }

    private void sendByPost_upDate(String json) {
        String path = getResources().getString(R.string.request_local)+"/userUpdate";
        OkHttpClient client = new OkHttpClient();
        final FormBody formBody = new FormBody.Builder()
                .add("json", json)
                .build();
        System.out.println("*********userUpdate "+json);
        Request request = new Request.Builder()
                .url(path)
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Looper.prepare();
                Toast.makeText(getContext(), "服务器连接失败", Toast.LENGTH_SHORT).show();
                Looper.loop();
                e.printStackTrace();
            }
            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                String info=response.body().string();
                JSONObject json= JSON.parseObject(info);
                System.out.println("*****userUpdate return "+info);
                if(json.getString("msg").equals("update success")){
                    editor.putString("user_name",modifyUser.getUser_name());
                    editor.putString("user_sign",modifyUser.getUser_sign());
                    editor.putString("user_gender",modifyUser.getUser_gender());
                    editor.putString("user_pwd",modifyUser.getUser_pwd());
                    editor.apply();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            name.setText(modifyUser.getUser_name());
                            sign.setText("个性签名: "+modifyUser.getUser_sign());
                            Glide.with(Objects.requireNonNull(getContext())).load(getString(R.string.app_prefix_img)+sp.getString("user_img","")).into(img);
                        }
                    });
                }
                else{
                    modifyUser.setUser_pwd(sp.getString("user_pwd",""));
                    Looper.prepare();
                    Toast.makeText(getContext(),"修改密码失败!",Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        });
    }

    /**
     * 点击查看大图     */
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
