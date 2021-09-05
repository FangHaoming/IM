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
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
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



    /**
     * 该方法用于插入消息Message
     * @param context : 所在的context
     * @param message : 需要插入的message
     * @throws IOException  : 序列化失败时，会抛出该异常
     */
    public void insertMessage(Context context, Message message) throws IOException {
        MessageDBHelper messageDBHelper = new MessageDBHelper(context, DATABASENAME, MessageDBHelper.CURVERSION);
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
        insertValues.put("isGroup", message.isGroup()? 1:0);

        db.insert(MESSAGETABLENAME, null, insertValues);

        Log.e("Database", "message 插入成功: " + message);
        db.close();
    }

    /**
     * 该方法用于查找messageNums个 通信双方为clientID1、clientID2的消息
     * @param context
     * @param clientID1
     * @param clientID2
     * @param messageNums 最多返回的Message个数。
     * @return 符合条件的消息列表
     * @throws IOException : MsgBody反序列化失败时会返回该错误
     * @throws ClassNotFoundException ： MsgBody反序列化失败时会返回该错误
     */
    public List<Message> queryMessage(Context context, String clientID1, String clientID2, int messageNums) throws IOException, ClassNotFoundException {
        return queryMessage(context, clientID1, clientID2, messageNums, NOTIMELIMIT);
    }


    public List<Message> queryMessage(Context context, String groupID, int messageNums) throws IOException, ClassNotFoundException {
        MessageDBHelper messageDBHelper = new MessageDBHelper(context, DATABASENAME, MessageDBHelper.CURVERSION);
        SQLiteDatabase db = messageDBHelper.getWritableDatabase();
        ArrayList<Message> messageArrayList = new ArrayList<>();

        String selection = "(targetId == '" + groupID + "')";

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
                Message message = getMessageFromCursor(cursor);
                messageArrayList.add(message);
            } while(cursor.moveToNext());
        }

        db.close();

        return messageArrayList;
    }

    public List<Message> queryMessage(Context context, String groupID, int messageNums, long sendTime) throws IOException, ClassNotFoundException {
        MessageDBHelper messageDBHelper = new MessageDBHelper(context, DATABASENAME, MessageDBHelper.CURVERSION);
        SQLiteDatabase db = messageDBHelper.getWritableDatabase();
        ArrayList<Message> messageArrayList = new ArrayList<>();

        String selection = "(targetId == '" + groupID + "') AND sentTime <" + sendTime;

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
                Message message = getMessageFromCursor(cursor);
                messageArrayList.add(message);
            } while(cursor.moveToNext());
        }

        db.close();

        return messageArrayList;
    }

    /**
     *  该方法返回 通信双方为clientID1、clientID2、且发送时间小于sendTime的消息。
     * @param context
     * @param clientID1
     * @param clientID2
     * @param messageNums
     * @param sendTime ：返回的消息的发送时间，都应小于sendtime
     * @return 符合条件的消息列表
     * @throws IOException  反序列化失败时会返回该异常
     * @throws ClassNotFoundException 反序列化失败时会返回该异常
     */
    public List<Message> queryMessage(Context context, String clientID1, String clientID2, int messageNums, long sendTime) throws IOException, ClassNotFoundException {
        MessageDBHelper messageDBHelper = new MessageDBHelper(context, DATABASENAME, MessageDBHelper.CURVERSION);
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
                Message message = getMessageFromCursor(cursor);
                messageArrayList.add(message);
            } while(cursor.moveToNext());
        }

        db.close();

        return messageArrayList;
    }

    /**
     * 返回所有的消息
     * @param context
     * @return 所有消息
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public List<Message> queryAllMessage(Context context) throws IOException, ClassNotFoundException {
        MessageDBHelper messageDBHelper = new MessageDBHelper(context, DATABASENAME, MessageDBHelper.CURVERSION);
        SQLiteDatabase db = messageDBHelper.getWritableDatabase();
        ArrayList<Message> messageArrayList = new ArrayList<>();
        Cursor cursor = db.query(MESSAGETABLENAME,null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Message message = getMessageFromCursor(cursor);
                messageArrayList.add(message);
            } while(cursor.moveToNext());
        }
        db.close();
        return messageArrayList;
    }


    /**
     * 返回 不同通话中最新的消息。（A->B 和 B->A 属于一个通话）
     * @param context
     * @param userID : 用户的ID
     * @return
     */
    public List<Message> queryLatestDifMessage(Context context, String userID) throws IOException, ClassNotFoundException {
        MessageDBHelper messageDBHelper = new MessageDBHelper(context, DATABASENAME, MessageDBHelper.CURVERSION);
        SQLiteDatabase db = messageDBHelper.getWritableDatabase();
        ArrayList<Message> messageArrayList = new ArrayList<>();


        HashSet<String> clientIDs = new HashSet<>();
        HashSet<String> groupIDs = new HashSet<>();


        // 获取记录中出了userID外所有不同的通话ID
        Cursor cursorSender = db.query(MESSAGETABLENAME,
                 new String[]{"senderId"},
                "isGroup == 0",
                null,
                "senderId",
                null,
                null,
                null);

        Cursor cursorTarget = db.query(MESSAGETABLENAME,
                 new String[] {"targetId"},
                "isGroup == 0",
                null,
                "targetId",
                null,
                null,
                null);

        // 获取所有不同的群聊号
        Cursor cursorGroup = db.query(MESSAGETABLENAME,
                new String[]{"targetId"},
                "isGroup == 1",
                null,
                "targetId",
                null,
                null);

        if (cursorSender.moveToFirst()) {
            do {
                String id = cursorSender.getString(0);
                if (id.equals(userID)) continue;
                clientIDs.add(id);
            } while(cursorSender.moveToNext());
        }
        if (cursorTarget.moveToFirst()) {
            do {
                String id = cursorTarget.getString(0);
                if (id.equals(userID)) continue;
                clientIDs.add(id);
            } while(cursorTarget.moveToNext());
        }
        if (cursorGroup.moveToFirst()) {
            do {
                String id = cursorGroup.getString(0);
                groupIDs.add(id);
            } while (cursorGroup.moveToNext());
        }

        // 查找上面ID 与userID 的对话中，最新的消息。
        for (String clientID : clientIDs) {
            String selection = "(senderId == '" + clientID + "' or targetId == '" + clientID + "') and isGroup == 0";
            Cursor cursorP2P = db.query(MESSAGETABLENAME,
                                    null,
                                    selection,
                                    null,
                                    null,
                                    null,
                                    "sentTime DESC",
                                    "1");
            if (cursorP2P.moveToFirst()) {
                Message message = getMessageFromCursor(cursorP2P);
                messageArrayList.add(message);
            }
        }

        for (String group : groupIDs) {
            String selection = "(targetId == '" + group + "' ) and isGroup == 1";
            Cursor cursorGroupChat = db.query(MESSAGETABLENAME,
                                        null,
                                        selection,
                                        null,
                                        null,
                                        null,
                                        "sentTime DESC",
                                        "1");

            if (cursorGroupChat.moveToFirst()) {
                Message message = getMessageFromCursor(cursorGroupChat);
                messageArrayList.add(message);
            }
        }


        // 较新的放前面
        Collections.sort(messageArrayList, new Comparator<Message>() {
            @Override
            public int compare(Message o1, Message o2) {
                if (o1.getSentTime() > o2.getSentTime()) return -1;
                else if (o1.getSentTime() < o2.getSentTime()) return 1;
                else return 0;
            }
        });

        db.close();
        return messageArrayList;
    }


    /**
     *  查找所有通信双方为clientID1、clientID2,且 没有查看的消息。（即checkStatus=0的消息）
     * @param context
     * @param clientID1
     * @param clientID2
     * @return 符合条件的消息列表
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public List<Message> queryAllUncheckMessage(Context context, String clientID1, String clientID2) throws IOException, ClassNotFoundException {
        MessageDBHelper messageDBHelper = new MessageDBHelper(context, DATABASENAME, MessageDBHelper.CURVERSION);
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
                Message message = getMessageFromCursor(cursor);
                messageArrayList.add(message);
            } while(cursor.moveToNext());
        }

        db.close();
        return messageArrayList;
    }


    /**
     * 返回通信双方为clientID1、clientID2的没有查看的消息数量
     * @param context
     * @param clientID1
     * @param clientID2
     * @return 某个对话中没有查看的消息数量。
     */
    public int queryUncheckMessageNums(Context context, String clientID1, String clientID2) {
        MessageDBHelper messageDBHelper = new MessageDBHelper(context, DATABASENAME, MessageDBHelper.CURVERSION);
        SQLiteDatabase db = messageDBHelper.getWritableDatabase();


        String selection = "checkStatus == 0" + " AND ((senderId == '" + clientID1  + "' AND targetId == '" + clientID2 + "' )"
                + "Or ( senderId == '" + clientID2 + "' AND targetId == '" + clientID1 + "' ))"  ;

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

    public int queryUncheckMessageNums(Context context, String groupID) {
        MessageDBHelper messageDBHelper = new MessageDBHelper(context, DATABASENAME, MessageDBHelper.CURVERSION);
        SQLiteDatabase db = messageDBHelper.getWritableDatabase();


        String selection = "checkStatus == 0 AND" + "(targetId == '" + groupID + "')";

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

    /**
     * 返回 消息目标ID = targetID 的 所有未查询消息。
     * @param context
     * @param targetID
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public List<Message> queryAllUncheckedMessage(Context context, String targetID) throws IOException, ClassNotFoundException {
        MessageDBHelper messageDBHelper = new MessageDBHelper(context, DATABASENAME, MessageDBHelper.CURVERSION);
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
                Message message = getMessageFromCursor(cursor);
                messageArrayList.add(message);
            } while(cursor.moveToNext());
        }

        db.close();
        return messageArrayList;
    }



    /**
     *  将 以uuid为主键的消息状态设置为已查看
     * @param context
     * @param uuid
     */
    public void checkMessage(Context context, String uuid) {
        MessageDBHelper messageDBHelper = new MessageDBHelper(context, DATABASENAME, MessageDBHelper.CURVERSION);
        SQLiteDatabase db = messageDBHelper.getWritableDatabase();

        ContentValues updateValues = new ContentValues();
        updateValues.put("checkStatus", 1);
        db.update(MESSAGETABLENAME,updateValues,"uuid=='" + uuid + "'", null);
        db.close();
    }

    // 更新多个message的状态

    /**
     * 将 数据库中 消息uuid在 uuids 中的消息状态设置为已查看
     * @param context
     * @param uuids
     */
    public void checkMessage(Context context, String[] uuids) {
        MessageDBHelper messageDBHelper = new MessageDBHelper(context, DATABASENAME, MessageDBHelper.CURVERSION);
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


    private Message getMessageFromCursor(Cursor cursor) throws IOException, ClassNotFoundException {
        Message message = new Message();
        message.setUuid(cursor.getString(cursor.getColumnIndex("uuid")));
        message.setMsgId(cursor.getString(cursor.getColumnIndex("msgId")));
        message.setMsgType(MsgType.values()[cursor.getInt(cursor.getColumnIndex("msgType"))]);
        message.setSenderId(cursor.getString(cursor.getColumnIndex("senderId")));
        message.setTargetId(cursor.getString(cursor.getColumnIndex("targetId")));
        message.setSentTime(cursor.getLong(cursor.getColumnIndex("sentTime")));
        message.setSentStatus(MsgSendStatus.values()[cursor.getInt(cursor.getColumnIndex("sentStatus"))]);
        message.setCheck(cursor.getInt(cursor.getColumnIndex("checkStatus")) == 1);
        message.setGroup(cursor.getInt(cursor.getColumnIndex("isGroup")) == 1);
        String msgBodySerialization = cursor.getString(cursor.getColumnIndex("msgBody"));
        MsgBody msgBody = deserializeMsgBody(msgBodySerialization);
        message.setBody(msgBody);
        return message;
    }


}
