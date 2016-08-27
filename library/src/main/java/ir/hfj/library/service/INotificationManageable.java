package ir.hfj.library.service;


import android.app.NotificationManager;
import android.content.Context;

public interface INotificationManageable
{
    NotificationManager getNotificationManager();

    Context getNotificationManagerContext();
}
