package com.hrl.chaui.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.hrl.chaui.R;
import com.hrl.chaui.util.GlideUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class ItemShowActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_show);

        String msgType = getIntent().getStringExtra("msgType");

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.itemshowLinearLayout); // layout

        // 设置控件可见性
        ArrayMap<String, View> viewArrayMap = new ArrayMap<>();
        TextView textView =  (TextView) findViewById(R.id.textItemshow);
        ImageView imageView = (ImageView) findViewById(R.id.imageItemshow);
        VideoView videoView = (VideoView) findViewById(R.id.videoItemshow);
        viewArrayMap.put("text", textView);
        viewArrayMap.put("image", imageView);
        viewArrayMap.put("video", videoView);
        setVisibility(viewArrayMap, msgType);

        // 显示对应的内容
        switch (msgType) {
            case "text":{
                String textMsg = getIntent().getStringExtra("textMsg");
                textView.setText(textMsg);
                break;
            }
            case "image": {
                String imagePath = getIntent().getStringExtra("imagePath");
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                imageView.setImageBitmap(bitmap);
                break;
            }
            case "video": {
                String videoPath = getIntent().getStringExtra("videoPath");
                videoView.setVideoPath(videoPath);
                MediaController mediaController = new MediaController(this);
                videoView.setMediaController(mediaController);
                videoView.requestFocus();
                break;
            }
            case "file": {
                String filePath = getIntent().getStringExtra("filePath");
                Toast.makeText(this, "filePath:" + filePath, Toast.LENGTH_LONG).show();
            }
        }


    }

    private void setVisibility(Map<String, View> viewMap, String ... vis) {
        // 除了vis中的view可见，其他都要隐藏
        for (String key : viewMap.keySet()) {
            View view = viewMap.get(key);
            view.setVisibility(View.GONE);
            for (String target : vis) {
                if (key.equals(target)) {
                    view.setVisibility(View.VISIBLE);
                    break;
                }
            }
        }
    }


}