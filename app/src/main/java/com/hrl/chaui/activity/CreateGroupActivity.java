package com.hrl.chaui.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.hrl.chaui.R;
import com.hrl.chaui.bean.User;
import com.hrl.chaui.util.MqttByAli;
import com.hrl.chaui.util.MqttService;
import com.hrl.chaui.util.http;
import com.mcxtzhang.indexlib.IndexBar.widget.IndexBar;
import com.mcxtzhang.indexlib.suspension.SuspensionDecoration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.hrl.chaui.MyApplication.friendData;
import static com.hrl.chaui.MyApplication.modifyUser;

public class CreateGroupActivity extends AppCompatActivity {

    private TextView back_arrow,search_icon;
    private Button save;
    private RecyclerView mRv;
    private TextView mTvSideBarHint;
    private IndexBar mIndexBar;
    private SuspensionDecoration mDecoration;
    private LinearLayoutManager mManager;
    private CreateGroupAdapter mAdapter;
    private HorizontalScrollView hsv;
    private LinearLayout Avatar_container,parent;
    private EditText editText;
    private ArrayList<User> addList;
    private MqttByAli mqtt = null;
    private MqttServiceConnection connection = null;



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable  Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_create_group);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.top_bottom));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        back_arrow=findViewById(R.id.back_arrow);
        search_icon=findViewById(R.id.search_icon);
        save=findViewById(R.id.save);
        mRv=findViewById(R.id.rv);
        mRv.setLayoutManager(mManager = new LinearLayoutManager(this));
        mTvSideBarHint = findViewById(R.id.tvSideBarHint);
        mIndexBar = findViewById(R.id.indexBar);
        hsv=findViewById(R.id.hsv);
        Avatar_container=findViewById(R.id.Avatar_container);
        parent=findViewById(R.id.parent);
        editText=findViewById(R.id.editText);

        Intent mqttServiceIntent = new Intent(this, MqttService.class);
        startService(mqttServiceIntent);
        connection = new MqttServiceConnection();
        // 绑定MqttService 获取 MqttByAli对象
        bindService(mqttServiceIntent, connection, Context.BIND_AUTO_CREATE);

        addList=new ArrayList<>();
        mAdapter = new CreateGroupAdapter(this, friendData);
        mRv.setAdapter(mAdapter);
        mRv.addItemDecoration(mDecoration = new SuspensionDecoration(this, friendData));
        mRv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));


        mIndexBar.setmPressedShowTextView(mTvSideBarHint)//设置HintTextView
                .setNeedRealIndex(true)//设置需要真实的索引
                .setmLayoutManager(mManager);//设置RecyclerView的LayoutManager
        mAdapter.setDatas(friendData);
        mAdapter.notifyDataSetChanged();

        mIndexBar.setmSourceDatas(friendData)//设置数据
                .invalidate();
        mDecoration.setmDatas(friendData);

        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode==KeyEvent.KEYCODE_DEL){
                    int size=addList.size();
                    if(size>0) {
                        deleteImage(addList.get(addList.size() - 1));
                    }
                }
                return false;
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()>0){
                    List<User> user_temp=new ArrayList<>();
                    for(User user:friendData){
                        if(user.getUser_name().contains(editText.getText().toString().trim())){
                            user_temp.add(user);
                        }
                        mAdapter=new CreateGroupAdapter(CreateGroupActivity.this,user_temp);
                        mRv.setAdapter(mAdapter);
                        mIndexBar.setmSourceDatas(user_temp)//设置数据
                                .invalidate();
                        mDecoration.setmDatas(user_temp);
                    }
                }
                else{
                    mAdapter = new CreateGroupAdapter(CreateGroupActivity.this, friendData);
                    mRv.setAdapter(mAdapter);
                    mIndexBar.setmSourceDatas(friendData)//设置数据
                            .invalidate();
                    mDecoration.setmDatas(friendData);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //确定
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject json=new JSONObject();
                json.put("group_name","hello group");
                JSONArray jsonArray=new JSONArray();
                addList.add(modifyUser);
                for(User user:addList){
                    JSONObject temp=new JSONObject();
                    temp.put("user_id",user.getUser_id());
                    temp.put("nickname",null);
                    if(user.getUser_id().equals(modifyUser.getUser_id())){
                        temp.put("rank",0);
                    }else{
                        temp.put("rank",2);
                    }
                    jsonArray.add(temp);
                }
                json.put("members",jsonArray.toJSONString());
                sendByPost(json.toJSONString());
            }
        });
        back_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(CreateGroupActivity.this,GroupActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            Intent intent=new Intent(CreateGroupActivity.this,GroupActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
        return true;
    }

    private class MqttServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MqttService.LocalBinder localBinder = (MqttService.LocalBinder) service;
            mqtt = localBinder.getService().getMqtt();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    /**
     * @param user 要添加图标的user
     */
    private void showCheckImage(User user){
        addList.add(user);
        save.setText("确定("+addList.size()+")");
        if(addList.size()>0){
            if(search_icon.getVisibility()==View.VISIBLE){
                search_icon.setVisibility(View.GONE);
            }
        }
        CircleImageView img=new CircleImageView(CreateGroupActivity.this);
        LinearLayout.LayoutParams imgParams= new LinearLayout.LayoutParams(dip2px(CreateGroupActivity.this,35),dip2px(CreateGroupActivity.this,35));
        imgParams.leftMargin=dip2px(CreateGroupActivity.this,1);
        imgParams.rightMargin=dip2px(CreateGroupActivity.this,1);
        Glide.with(this).load(getResources().getString(R.string.app_prefix_img)+user.getUser_img()).into(img);
        img.setTag(user);  //设置id，方便删除
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteImage(user);
            }
        });
        Avatar_container.addView(img,imgParams);
        Avatar_container.post(new Runnable() {
            @Override
            public void run() {
                hsv.fullScroll(View.FOCUS_RIGHT);
            }
        });
        System.out.println("****"+Avatar_container.getWidth()+" "+editText.getWidth()+" "+parent.getWidth());
        if(editText.getWidth()<dip2px(CreateGroupActivity.this,100)){
            LinearLayout.LayoutParams linearParams= (LinearLayout.LayoutParams) hsv.getLayoutParams();
            linearParams.width=parent.getWidth()-dip2px(CreateGroupActivity.this,100);
            hsv.setLayoutParams(linearParams);
        }
    }

    /**
     * @param user 要删除图标的User
     */
    private void deleteImage(User user){
        CircleImageView img=Avatar_container.findViewWithTag(user);
        Avatar_container.removeView(img);
        addList.remove(user);
        friendData.get(friendData.indexOf(user)).setSelect(false);
        save.setText("确定("+addList.size()+")");
        if(addList.size()==0){
            if(search_icon.getVisibility()==View.GONE){
                search_icon.setVisibility(View.VISIBLE);
            }
        }
    }


    /**
     * 将dp转换成px值
     */
    public static int dip2px(Context context, float dip) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dip * scale + 0.5);
    }


    private class CreateGroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        protected Context mContext;
        protected List<User> users;
        protected LayoutInflater mInflater;

        public CreateGroupAdapter(Context mContext, List<User> users) {
            this.mContext = mContext;
            this.users = users;
            mInflater = LayoutInflater.from(mContext);
        }

        public List<User> getDatas() {
            return users;
        }

        public CreateGroupAdapter setDatas(List<User> datas) {
            users = datas;
            return this;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(mInflater.inflate(R.layout.item_add_to_group, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            User user = users.get(position);
            ((ViewHolder) holder).name.setText(user.getUser_name());
            Glide.with(mContext).load(mContext.getString(R.string.app_prefix_img) + user.getUser_img()).into(((ViewHolder) holder).img);
            if(user.isSelect()){
                ((ViewHolder) holder).check.setChecked(true);
            }else{
                ((ViewHolder) holder).check.setChecked(false);
            }
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.check:
                        case R.id.content:
                            if (user.isSelect()) {
                                user.setSelect(false);
                                ((ViewHolder) holder).check.setChecked(false);
                                deleteImage(user);
                            } else {
                                user.setSelect(true);
                                ((ViewHolder) holder).check.setChecked(true);
                                showCheckImage(user);
                            }
                    }
                }
            };
            ((ViewHolder) holder).content.setOnClickListener(listener);
            ((ViewHolder) holder).check.setOnClickListener(listener);
        }

        @Override
        public int getItemCount() {
            return users != null ? users.size() : 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView name;
            CircleImageView img;
            CheckBox check;
            View content;

            public ViewHolder(View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.name);
                img = itemView.findViewById(R.id.img);
                check = itemView.findViewById(R.id.check);
                content = itemView.findViewById(R.id.content);
            }
        }
    }
    private void sendByPost(String json) {
        String path = getResources().getString(R.string.request_local)+"/groupCreate";
        OkHttpClient client = new OkHttpClient();
        final FormBody formBody = new FormBody.Builder()
                .add("json", json)
                .build();
        System.out.println("*********json in groupCreate"+json);
        Request request = new Request.Builder()
                .url(path)
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Looper.prepare();
                Toast.makeText(CreateGroupActivity.this, "服务器连接失败", Toast.LENGTH_SHORT).show();
                Looper.loop();
                e.printStackTrace();
            }
            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                String info=response.body().string();
                JSONObject json= JSON.parseObject(info);
                if(json.getString("msg").equals("create success")){
                    http.sendByPost(CreateGroupActivity.this,modifyUser.getUser_id());
                    Looper.prepare();
                    Toast.makeText(CreateGroupActivity.this,"创建成功!",Toast.LENGTH_SHORT).show();
                    addList.remove(modifyUser);
                    for(User user:addList){
                        mqtt.sendP2PGroupInvite("GID_test@@@"+user.getUser_id(),user);
                    }
                    Intent intent=new Intent(CreateGroupActivity.this,GroupActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    Looper.loop();
                }
                else{
                    Looper.prepare();
                    Toast.makeText(CreateGroupActivity.this,"创建失败",Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        });
    }
}
