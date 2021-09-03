package com.hrl.chaui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.hrl.chaui.R;
import com.hrl.chaui.fragment.MineFragment;
import com.hrl.chaui.util.AvatarStudio;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class ModifyActivity extends AppCompatActivity {

    View img_view,name_view,phone_view,sign_view,gender_view;
    TextView back_arrow,name,phone,sign,gender;
    Boolean isModify;
    CircleImageView img;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    Intent intent_Main;
    Bundle bundle_Main;
    Bundle bundle;
    JSONObject json;
    Boolean isImgChange;
    String img_uri;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_modify);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},1);
        }
        isModify=false;
        isImgChange=false;
        img_uri="";
        intent_Main = new Intent(ModifyActivity.this, MineFragment.class);
        bundle_Main = new Bundle();
        sp = getSharedPreferences("data", MODE_PRIVATE);
        editor = sp.edit();
        json= JSON.parseObject(sp.getString("json","{}"));
        img = findViewById(R.id.img);
        img_view = findViewById(R.id.img_view);
        name = findViewById(R.id.name);
        name_view = findViewById(R.id.name_view);
        phone = findViewById(R.id.phone);
        phone_view = findViewById(R.id.phone_view);
        gender = findViewById(R.id.gender);
        gender_view = findViewById(R.id.gender_view);
        sign = findViewById(R.id.sign);
        sign_view = findViewById(R.id.sign_view);
        back_arrow=findViewById(R.id.back_arrow);
        name.setText(sp.getString("user_name", ""));
        phone.setText(sp.getString("user_phone", ""));
        sign.setText(sp.getString("user_sign", ""));
        gender.setText(sp.getString("user_gender", ""));


        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                switch (v.getId()) {
                    case R.id.img_view:
                        modifyImg();
                        break;
                    case R.id.name_view:
                        intent = new Intent(ModifyActivity.this, ModifyNameActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.phone_view:
                        intent = new Intent(ModifyActivity.this, ModifyPhoneActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.gender_view:
                        intent = new Intent(ModifyActivity.this, ModifyGenderActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.sign_view:
                        intent = new Intent(ModifyActivity.this, ModifySignActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.back_arrow:
                        bundle_Main.putBoolean("isModify", isModify);
                        bundle_Main.putString("json",sp.getString("json","{}"));
                        System.out.println("********json in Modify "+json.toJSONString());
                        intent_Main.putExtras(bundle_Main);
                        editor.putString("json","{}");
                        editor.apply();
                        setResult(Activity.RESULT_OK, intent_Main);
                        finish();
                        break;

                }
            }
        };
        img_view.setOnClickListener(listener);
        name_view.setOnClickListener(listener);
        phone_view.setOnClickListener(listener);
        gender_view.setOnClickListener(listener);
        sign_view.setOnClickListener(listener);
        back_arrow.setOnClickListener(listener);


    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        bundle = intent.getExtras();
        if (bundle != null) {
            if (bundle.getString("user_name") != null) {
                name.setText(bundle.getString("user_name"));
                //bundle_Main.putString("user_name",bundle.getString("user_name"));
                json.put("user_name", bundle.getString("user_name"));
            }
            if (bundle.getString("user_phone") != null) {
                phone.setText(bundle.getString("user_phone"));
                json.put("user_phone", bundle.getString("user_phone"));
            }
            if (bundle.getString("user_gender") != null) {
                gender.setText(bundle.getString("user_gender"));
                json.put("user_gender", bundle.getString("user_gender"));
            }
            if (bundle.getString("user_sign") != null) {
                sign.setText(bundle.getString("user_sign"));
                json.put("user_sign", bundle.getString("user_sign"));
            }
            isModify = bundle.getBoolean("isModify");
            System.out.println("*********isModify in Modify onCreate " + isModify);
            editor.putString("json", json.toJSONString());
            editor.apply();
        }

        if (!sp.getString("user_img", "").equals("") && !isImgChange) {
            Glide.with(this).load(getString(R.string.app_prefix_img) + sp.getString("user_img", "")).into(img);
        } else if (!img_uri.equals("") && isImgChange) {
            System.out.println("*********isImgChange in Modify" + isImgChange);
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(img_uri));
                img.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            intent_Main = new Intent(ModifyActivity.this, MainActivity.class);
            bundle_Main.putBoolean("isModify",isModify);
            bundle_Main.putString("json",sp.getString("json","{}"));
            editor.putString("json","{}");
            editor.apply();
            Log.i("json in Modify ", json.toJSONString());
            intent_Main.putExtras(bundle_Main);
            setResult(Activity.RESULT_OK, intent_Main);
            finish();
        }
        return true;
    }
    private void modifyImg() {
        new AvatarStudio.Builder(ModifyActivity.this)
                .needCrop(true)
                .setTextColor(Color.BLACK)
                .dimEnabled(true)
                .setAspect(1, 1)
                .setOutput(250, 250)
                .setText("打开相机", "从相册中选取", "取消")
                .show(new AvatarStudio.CallBack() {
                    @Override
                    public void callback(final String uri) {
                        System.out.println("*********uri "+uri);
                        setAvataor(uri);
                    }
                });
    }

    private void setAvataor(final String uri) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    /*
                    InputStream inputStream=new FileInputStream(uri);
                    byte[] baos=inputStream2ByteArr(inputStream);
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(baos,0,baos.length);
                     */
                    final Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(uri));
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    byte[] img_data=out.toByteArray();
                    bundle_Main.putBoolean("isImgChange",true);
                    bundle_Main.putString("img_uri",uri);

                    isImgChange=true;
                    img_uri=uri;

                    json.put("img_data",img_data);
                    editor.putString("img_uri",uri);
                    editor.putBoolean("isImgChange",true);
                    editor.putString("json",json.toJSONString());
                    editor.apply();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            img.setImageBitmap(bitmap);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private byte[] inputStream2ByteArr(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int len = 0;
        while ((len = inputStream.read(buff)) != -1) {
            outputStream.write(buff, 0, len);
        }
        inputStream.close();
        outputStream.close();
        return outputStream.toByteArray();
    }
}
