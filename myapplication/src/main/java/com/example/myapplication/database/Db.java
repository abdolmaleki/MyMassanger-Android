package com.example.myapplication.database;

import android.database.Cursor;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.example.myapplication.database.model.ChatModel;
import com.example.myapplication.database.model.TeacherModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ir.hfj.library.application.AppConfig;
import ir.hfj.library.database.DbBase;


public abstract class Db extends DbBase
{



    public final static class Teacher
    {

        public static boolean insert(TeacherModel teacherModel)
        {

            try
            {
                teacherModel.save();

                return true;
            }
            catch (Exception e)
            {
                if (AppConfig.DEBUG)
                {
                    Log.e(AppConfig.LOG_TAG, "SamimDatabase > Insert [Teacher] problem: " + e.getMessage());
                }
            }

            return false;
        }

        public static List<TeacherModel> selectByStudentId(long studentId)
        {
            return new Select()
                    .from(TeacherModel.class)
                    .where(TeacherModel._studentId + "=?", studentId)
                    .execute();
        }

        public static TeacherModel selectByGuid(UUID teacherGuid)
        {
            return new Select()
                    .from(TeacherModel.class)
                    .where(TeacherModel.__guid + "=?", teacherGuid)
                    .executeSingle();
        }

        public static UUID selectById(long teacherId)
        {
            TeacherModel model = new Select()
                    .from(TeacherModel.class)
                    .where(TeacherModel.__id + "=?", teacherId)
                    .executeSingle();

            return model.getGuid();
        }

        public static boolean delete(UUID guid)
        {
            try
            {
                new Delete().from(TeacherModel.class).where(TeacherModel.__guid + "=?", guid).execute();
                return true;
            }
            catch (Exception e)
            {
                if (AppConfig.DEBUG)
                {
                    Log.e(AppConfig.LOG_TAG, "SamimDatabase > Delete [TeacherModel] problem: " + e.getMessage());
                }
            }
            return false;
        }
    }

    public final static class Chat
    {

        public static boolean insert(ChatModel chatModel)
        {

            try
            {
                chatModel.save();

                return true;
            }
            catch (Exception e)
            {
                if (AppConfig.DEBUG)
                {
                    Log.e(AppConfig.LOG_TAG, "SamimDatabase > Insert [Chat] problem" + e.getMessage());
                }
            }
            return false;
        }

        public static List<ChatModel> selectByUser(UUID userId)
        {
            return new Select()
                    .from(ChatModel.class)
                    .where(ChatModel._contactUserId + "=?", userId)
                    .orderBy(ChatModel._date + " ASC")
                    .execute();
        }


        public static ChatModel selectHistory(UUID userId)
        {
            return new Select()
                    .from(ChatModel.class)
                    .where(ChatModel._contactUserId + "=?", userId)
                    .orderBy(ChatModel._date + " DESC")
                    .executeSingle();
        }

        public static int selectUnreadHistoryCount(UUID userId)
        {
            return new Select()
                    .from(ChatModel.class)
                    .where(ChatModel._contactUserId + "=? AND " + ChatModel._state + "=?", userId, ChatModel.RECEIVER_STATE_RECEIVED)
                    .count();
        }

        public static List<UUID> selectUnReportedReadedMessage(UUID userId)
        {
            List<UUID> chatGuids = new ArrayList<>();

            try
            {
                Cursor c = ActiveAndroid.getDatabase().rawQuery("SELECT " + ChatModel.__guid + " FROM " + ChatModel.__table + " WHERE " + ChatModel._contactUserId + "='" + userId + "' AND " + ChatModel._state + "=" + ChatModel.RECEIVER_STATE_READ, null);

                if (c.getCount() > 0)
                {
                    c.moveToFirst();
                    do
                    {
                        UUID chatGuid = UUID.fromString(c.getString(c.getColumnIndex(ChatModel.__guid)));
                        chatGuids.add(chatGuid);
                    } while (c.moveToNext());
                }

                c.close();

            }
            catch (Exception e)
            {
                if (AppConfig.DEBUG)
                {
                    Log.e(AppConfig.LOG_TAG, "SamimDatabase > selectUnReportedReadedMessage [Chat] problem" + e.getMessage());
                }
            }

            return chatGuids;
        }

        public static List<UUID> selectUnReportedReadedUser()
        {
            List<UUID> userGuids = new ArrayList<>();

            try
            {
                Cursor c = ActiveAndroid.getDatabase().rawQuery("SELECT DISTINCT " + ChatModel._contactUserId + " FROM " + ChatModel.__table + " WHERE " + ChatModel._state + "=" + ChatModel.RECEIVER_STATE_READ, null);

                if (c.getCount() > 0)
                {
                    c.moveToFirst();
                    do
                    {
                        UUID chatGuid = UUID.fromString(c.getString(c.getColumnIndex(ChatModel._contactUserId)));
                        userGuids.add(chatGuid);
                    } while (c.moveToNext());
                }

                c.close();

            }
            catch (Exception e)
            {
                if (AppConfig.DEBUG)
                {
                    Log.e(AppConfig.LOG_TAG, "SamimDatabase > selectUnReportedReadedUser [Chat] problem" + e.getMessage());
                }
            }

            return userGuids;
        }

        public static ChatModel selectFirstNotSended()
        {
            return new Select()
                    .from(ChatModel.class)
                    .where(ChatModel._state + "=?", ChatModel.SENDER_STATE_SENDING)
                    .orderBy(ChatModel._date + " ASC")
                    .executeSingle();
        }

        public static List<ChatModel> selectAllNotSended()
        {
            return new Select()
                    .from(ChatModel.class)
                    .where(ChatModel._state + "=?", ChatModel.SENDER_STATE_SENDING)
                    .orderBy(ChatModel._date + " ASC")
                    .execute();
        }

        public static ChatModel select(UUID chatId)
        {
            return new Select()
                    .from(ChatModel.class)
                    .where(ChatModel.__guid + "=?", chatId)
                    .executeSingle();
        }

        public static boolean readAllUnReadMessage(UUID chatUserGuid)
        {

            try
            {
                ActiveAndroid.getDatabase().execSQL("UPDATE " + ChatModel.__table + " SET " + ChatModel._state + " = " + ChatModel.RECEIVER_STATE_READ + " WHERE " + ChatModel._contactUserId + " = '" + chatUserGuid + "' AND " + ChatModel._state + "=" + ChatModel.RECEIVER_STATE_RECEIVED);
                return true;
            }
            catch (Exception e)
            {
                if (AppConfig.DEBUG)
                {
                    Log.e(AppConfig.LOG_TAG, "SamimDatabase > readAllUnReadMessage [Chat] problem" + e.getMessage());
                }
            }
            return false;
        }


        public static boolean update(ChatModel model)
        {
            try
            {
                if (model != null)
                {
                    model.save();
                    return true;
                }
            }
            catch (Exception e)
            {
                if (AppConfig.DEBUG)
                {
                    Log.e(AppConfig.LOG_TAG, "SamimDatabase > Update [Chat] problem" + e.getMessage());
                }
            }
            return false;
        }

        public static boolean delete(ChatModel model)
        {
            try
            {
                if (model != null)
                {
                    model.delete();
                    return true;
                }
            }
            catch (Exception e)
            {
                if (AppConfig.DEBUG)
                {
                    Log.e(AppConfig.LOG_TAG, "SamimDatabase > Delete [Chat] problem" + e.getMessage());
                }
            }
            return false;
        }

        /*public static boolean update(UUID chatId, int txvState)
        {
            try
            {
                ChatModel model = new Select().from(ChatModel.class).where(ChatModel.__guid + "=?", chatId).executeSingle();
                if (model != null)
                {
                    long id = model.getId();
                    ChatModel chatModel = ChatModel.load(ChatModel.class, id);
                    chatModel.txvState = txvState;
                    chatModel.save();
                    return true;
                }

            }
            catch (Exception e)
            {
                if (AppConfig.DEBUG)
                {
                    Log.e(AppConfig.LOG_TAG, "SamimDatabase > Update [Chat] problem" + e.getMessage());
                }
            }
            return false;
        }*/

    }
}