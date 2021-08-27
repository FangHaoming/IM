package com.hrl.chaui.dao.imp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.hrl.chaui.bean.Message;
import com.hrl.chaui.bean.MsgBody;
import com.hrl.chaui.bean.MsgSendStatus;
import com.hrl.chaui.bean.MsgType;
import com.hrl.chaui.dao.MessageDao;
import com.hrl.chaui.util.MessageDBHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class MessageDaoImp implements MessageDao {

    private static volatile MessageDaoImp messageDaoImp;
    private static final String DATABASENAME = "message.db";
    private static final String MESSAGETABLENAME = "Message";
    private static final int NOTIMELIMIT = -1;

    private MessageDaoImp(){}


    // 使用单例模式
    public static MessageDaoImp getInstance() {
        if (messageDaoImp == null) {
            synchronized (MessageDaoImp.class) {
                if (messageDaoImp == null) {
                    messageDaoImp = new MessageDaoImp();
                }
            }
        }
        return messageDaoImp;
    }


    // 插入Message
    public void insertMessage(Context context, Message message) throws IOException {
        MessageDBHelper messageDBHelper = new MessageDBHelper(context, DATABASENAME, 1);
        SQLiteDatabase db = messageDBHelper.getWritableDatabase();
        MsgBody msgBody = message.getBody();

        String msgBodySerialization = serializeMsgBody(msgBody);

        ContentValues insertValues = new ContentValues();
        insertValues.put("uuid", message.getUuid());
        insertValues.put("msgType", message.getMsgType().ordinal());
        insertValues.put("senderId", message.getSenderId());
        insertValues.put("targetId", message.getTargetId());
        insertValues.put("sentTime", message.getSentTime());
        insertValues.put("sentStatus", message.getSentStatus().ordinal());
        insertValues.put("checkStatus", message.isCheck() ? 1:0 );
        insertValues.put("msgBody", msgBodySerialization);

        db.insert(MESSAGETABLENAME, null, insertValues);

        Log.e("Database", "message 插入成功: " + message);
        db.close();
    }

    // 查询 和某两个ClientID有关 的 Message (按照发送时间排序)。 messageNums是需要查询的数量
    public List<Message> queryMessage(Context context, String clientID1, String clientID2, int messageNums) throws IOException, ClassNotFoundException {
        return queryMessage(context, clientID1, clientID2, messageNums, NOTIMELIMIT);
    }


    // 查询 和某两个ClientID有关 的 在 sendTime 之前的 Message (按照发送时间排序)。 messageNums是需要查询的数量，
    public List<Message> queryMessage(Context context, String clientID1, String clientID2, int messageNums, long sendTime) throws IOException, ClassNotFoundException {
        MessageDBHelper messageDBHelper = new MessageDBHelper(context, DATABASENAME, 1);
        SQLiteDatabase db = messageDBHelper.getWritableDatabase();
        ArrayList<Message> messageArrayList = new ArrayList<>();

        String timeLimit = "";
        if (sendTime != NOTIMELIMIT) {
            // 有sendTime限制
            timeLimit = " AND sentTime <" + sendTime ;
        }

        String selection = "((senderId == '" + clientID1  + "' AND targetId == '" + clientID2 + "')"
                + "Or ( senderId == '" + clientID2 + "' AND targetId == '" + clientID1 + "' ))" + timeLimit;



        Cursor cursor = db.query(MESSAGETABLENAME,
                                    null,
                                    selection,
                                    null,
                                    null,
                                    null,
                                    "sentTime DESC", // 时间从大到小
                                    String.valueOf(messageNums)); // 只取messageNums个


        if (cursor.moveToFirst()) {
            do {
                Message message = new Message();
                message.setUuid(cursor.getString(cursor.getColumnIndex("uuid")));
                message.setMsgId(cursor.getString(cursor.getColumnIndex("msgId")));
                message.setMsgType(MsgType.values()[cursor.getInt(cursor.getColumnIndex("msgType"))]);
                message.setSenderId(cursor.getString(cursor.getColumnIndex("senderId")));
                message.setTargetId(cursor.getString(cursor.getColumnIndex("targetId")));
                message.setSentTime(cursor.getLong(cursor.getColumnIndex("sentTime")));
                message.setSentStatus(MsgSendStatus.values()[cursor.getInt(cursor.getColumnIndex("sentStatus"))]);
                message.setCheck(cursor.getInt(cursor.getColumnIndex("checkStatus")) == 1);
                String msgBodySerialization = cursor.getString(cursor.getColumnIndex("msgBody"));
                MsgBody msgBody = deserializeMsgBody(msgBodySerialization);
                message.setBody(msgBody);
                messageArrayList.add(message);
            } while(cursor.moveToNext());
        }

        db.close();

        return messageArrayList;
    }

    // 查询 和两个ClientID有关的 所有未被查看Message
    public List<Message> queryAllUncheckMessage(Context context, String clientID1, String clientID2) throws IOException, ClassNotFoundException {
        MessageDBHelper messageDBHelper = new MessageDBHelper(context, DATABASENAME, 1);
        SQLiteDatabase db = messageDBHelper.getWritableDatabase();

        ArrayList<Message> messageArrayList = new ArrayList<>();

        String selection = "((senderId == '" + clientID1  + "' AND targetId == '" + clientID2 + "')"
                + "Or ( senderId == '" + clientID2 + "' AND targetId == '" + clientID1 + "' )) AND ( checkStatus == " + 0  + ")" ;

        Cursor cursor = db.query(MESSAGETABLENAME,
                null,
                selection,
                null,
                null,
                null,
                "sentTime DESC" ); // 时间从大到小


        if (cursor.moveToFirst()) {
            do {
                Message message = new Message();
                message.setUuid(cursor.getString(cursor.getColumnIndex("uuid")));
                message.setMsgId(cursor.getString(cursor.getColumnIndex("msgId")));
                message.setMsgType(MsgType.values()[cursor.getInt(cursor.getColumnIndex("msgType"))]);
                message.setSenderId(cursor.getString(cursor.getColumnIndex("senderId")));
                message.setTargetId(cursor.getString(cursor.getColumnIndex("targetId")));
                message.setSentTime(cursor.getLong(cursor.getColumnIndex("sentTime")));
                message.setSentStatus(MsgSendStatus.values()[cursor.getInt(cursor.getColumnIndex("sentStatus"))]);
                message.setCheck(cursor.getInt(cursor.getColumnIndex("checkStatus")) == 1);
                String msgBodySerialization = cursor.getString(cursor.getColumnIndex("msgBody"));
                MsgBody msgBody = deserializeMsgBody(msgBodySerialization);
                message.setBody(msgBody);
                messageArrayList.add(message);
            } while(cursor.moveToNext());
        }

        db.close();
        return messageArrayList;
    }

    // 查询 通信双方未被查看的Message数量
    public int queryUncheckMessageNums(Context context, String clientID1, String clientID2) {
        MessageDBHelper messageDBHelper = new MessageDBHelper(context, DATABASENAME, 1);
        SQLiteDatabase db = messageDBHelper.getWritableDatabase();

        ArrayList<Message> messageArrayList = new ArrayList<>();

        String selection = "(senderId == '" + clientID1  + "' AND targetId == '" + clientID2 + "' )"
                + "Or ( senderId == '" + clientID2 + "' AND targetId == '" + clientID1 + "' )"  ;

        String[] columns = {"COUNT(*)"};

        Cursor cursor = db.query(MESSAGETABLENAME,
                columns,
                selection,
                null,
                null,
                null,
                null);

        cursor.moveToFirst();
        db.close();
        return cursor.getInt(0);
    }

    // 查询某个用户收到的所有未查看(check)Message
    @RequiresApi(api = Build.VERSION_CODES.O)
    public List<Message> queryAllUncheckedMessage(Context context, String targetID) throws IOException, ClassNotFoundException {
        MessageDBHelper messageDBHelper = new MessageDBHelper(context, DATABASENAME, 1);
        SQLiteDatabase db = messageDBHelper.getWritableDatabase();

        ArrayList<Message> messageArrayList = new ArrayList<>();

        String selection = "targetId == '" + targetID + "' AND" + "checkStatus == " + 0 ;


        Cursor cursor = db.query(MESSAGETABLENAME,
                null,
                selection,
                null,
                null,
                null,
                "senderId, sentTime ASC"); // 时间从小到大


        if (cursor.moveToFirst()) {
            do {
                Message message = new Message();
                message.setUuid(cursor.getString(cursor.getColumnIndex("uuid")));
                message.setMsgId(cursor.getString(cursor.getColumnIndex("msgId")));
                message.setMsgType(MsgType.values()[cursor.getInt(cursor.getColumnIndex("msgType"))]);
                message.setSenderId(cursor.getString(cursor.getColumnIndex("senderId")));
                message.setTargetId(cursor.getString(cursor.getColumnIndex("targetId")));
                message.setSentTime(cursor.getLong(cursor.getColumnIndex("sentTime")));
                message.setSentStatus(MsgSendStatus.values()[cursor.getInt(cursor.getColumnIndex("sentStatus"))]);
                message.setCheck(cursor.getInt(cursor.getColumnIndex("checkStatus")) == 1);
                String msgBodySerialization = cursor.getString(cursor.getColumnIndex("msgBody"));
                MsgBody msgBody = deserializeMsgBody(msgBodySerialization);
                message.setBody(msgBody);
                messageArrayList.add(message);
            } while(cursor.moveToNext());
        }

        db.close();
        return messageArrayList;
    }



    // 将某个信息的状态改为check
    public void checkMessage(Context context, String uuid) {
        MessageDBHelper messageDBHelper = new MessageDBHelper(context, DATABASENAME, 1);
        SQLiteDatabase db = messageDBHelper.getWritableDatabase();

        ContentValues updateValues = new ContentValues();
        updateValues.put("checkStatus", 1);
        db.update(MESSAGETABLENAME,updateValues,"uuid=='" + uuid + "'", null);
        db.close();
    }

    // 更新多个message的状态
    public void checkMessage(Context context, String[] uuids) {
        MessageDBHelper messageDBHelper = new MessageDBHelper(context, DATABASENAME, 1);
        SQLiteDatabase db = messageDBHelper.getWritableDatabase();

        ContentValues updateValues = new ContentValues();
        updateValues.put("checkStatus", 1);

        StringBuffer whereCluse = new StringBuffer();
        if (uuids.length > 0)
            whereCluse.append("uuid == '" + uuids[0]+"'");
        for (int i = 1; i < uuids.length; i++) {
            whereCluse.append(" or uuid == '" + uuids[i] + "' ");
        }
        db.update(MESSAGETABLENAME, updateValues, whereCluse.toString(), null);
        db.close();
    }


    // 序列化MsgBody
    private String serializeMsgBody(MsgBody msgBody) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(msgBody);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        String res = Base64.getEncoder().encodeToString(bytes);
        objectOutputStream.close();
        byteArrayOutputStream.close();
        return res;
    }

    // 反序列化MsgBody
    public MsgBody deserializeMsgBody(String str) throws IOException, ClassNotFoundException {
        byte[] bytes = Base64.getDecoder().decode(str);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        MsgBody msgBody = (MsgBody) objectInputStream.readObject();
        objectInputStream.close();
        byteArrayInputStream.close();
        return msgBody;
    }



}
