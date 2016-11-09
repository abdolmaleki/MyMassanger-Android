package com.example.myapplication.factory;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;

import com.example.myapplication.R;
import com.example.myapplication.activity.ChatActivity;
import com.example.myapplication.application.Constant;
import com.example.myapplication.connection.socket.dto.ChatDto;
import com.example.myapplication.database.Db;
import com.example.myapplication.database.model.ContactModel;
import com.example.myapplication.entity.ChatContentType;
import com.example.myapplication.entity.ChatTextContent;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import ir.hfj.library.application.AppConfig;
import ir.hfj.library.service.INotificationManageable;
import ir.hfj.library.util.Helper;


public class Notifier
{


    private static Notification buildNotification(Notification.Builder builder)
    {
        if (Build.VERSION.SDK_INT >= 16)
        {
            return builder.build();
        }
        else
        {
             return builder.getNotification();
        }
    }

    public static void sendNotifyChat(INotificationManageable nm, ChatDto dto)
    {
        Context context = nm.getNotificationManagerContext();

        String notificationMessage = "";
        String tickerMessage = "";

        int unReadMessageCount = Db.Chat.selectUnreadHistoryCount(dto.senderUserId);

        ContactModel chatUser = Db.Contact.selectByGuid(dto.senderUserId);
        String chatUserName = chatUser.firstName + " " + chatUser.lastName;
        //  DisplayImageOptions displayImageOptions =AppConfig.createNotificationDisplayImageOptions();
        Bitmap chatUserImage = ImageLoader.getInstance().loadImageSync(chatUser.imageUrl, new ImageSize(96, 96), AppConfig.createNotificationDisplayImageOptions());

        if (unReadMessageCount > 1)
        {
            notificationMessage = unReadMessageCount + " پیام جدید";
            tickerMessage = unReadMessageCount + " پیام جدید";
        }

        else if (unReadMessageCount == 1)
        {
            if (dto.contentType == ChatContentType.Text)
            {
                notificationMessage = ((ChatTextContent) dto.content).text + "";
                tickerMessage = chatUserName + ": " + ((ChatTextContent) dto.content).text + "";

            }
            else if (dto.contentType == ChatContentType.Image)
            {
                notificationMessage = "عکس";
                tickerMessage = chatUserName + ": " + "عکس";
            }
        }

        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(Constant.Param.KEY_CHAT_CONTACT_GUID, chatUser.getGuid());
        PendingIntent pIntent = PendingIntent.getActivity(context, Helper.generateUniqueNumber(), intent, 0);
        Notification.Builder notificationBuilder = new Notification.Builder(context)
                .setContentTitle(chatUserName)
                .setContentText(notificationMessage)
                .setTicker(tickerMessage)
                .setContentIntent(pIntent)
                .setLargeIcon(chatUserImage)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true);

        Notification notification = buildNotification(notificationBuilder);


        nm.getNotificationManager().notify(chatUser.getId() + "", 0, notification);
    }
}
