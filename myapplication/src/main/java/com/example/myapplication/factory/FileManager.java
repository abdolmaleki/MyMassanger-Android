package com.example.myapplication.factory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.example.myapplication.R;
import com.example.myapplication.application.AppSamimConfig;
import com.example.myapplication.entity.ChatContentType;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import ir.hfj.library.application.AppConfig;
import ir.hfj.library.exception.SamimException;


public final class FileManager
{

    public static String getDirectory(ChatContentType type) throws Exception
    {
        String path = getRootDirectory() + "/" + type.name;

        return checkOrCreateDirectory(path);
    }

    public static String getTempDirectory() throws Exception
    {
        String path = getRootDirectory() + "/Temp";

        return checkOrCreateDirectory(path);
    }

    private static String checkOrCreateDirectory(String path) throws Exception
    {
        File folder = new File(path);

        if (!folder.exists())
        {
            if (!folder.mkdir())
            {
                throw new Exception();
            }
        }

        return path;
    }

    private static String getRootDirectory()
    {
        return Environment.getExternalStorageDirectory().getPath() + "/" + AppSamimConfig.DIRECTORY_MEDIA;
    }

    public static byte[] read(Context context, String path) throws SamimException
    {
        try
        {
            File file = new File(path);
            int size = (int) file.length();
            byte[] bytes = new byte[size];

            BufferedInputStream buf;

            buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);

            buf.close();

            if (bytes.length <= 0)
            {
                throw new SamimException(context.getString(R.string.samim_message_file_read_cant));
            }

            return bytes;
        }
        catch (FileNotFoundException e)
        {
            throw new SamimException(context.getString(R.string.samim_message_file_read_not_found));
        }
        catch (Exception e)
        {
            throw new SamimException(context.getString(R.string.samim_message_file_read_ex));
        }


    }

    public static String getMediaUrl(String token)
    {
        return AppConfig.NETWORK_HOST_WS + "/" + AppConfig.RestApiAction.MediaDownload + "/" + token;
    }

    public static String getThumbnailUrl(String token)
    {
        return AppConfig.NETWORK_HOST_WS + "/" + AppConfig.RestApiAction.MediaDownload + "/" + token + "/?thumbnail=true";
    }

    public static boolean exist(String path)
    {
        File file = new File(path);
        return file.exists();
    }

    public static Bitmap loadImage(String path)
    {
        return BitmapFactory.decodeFile(path);
    }

}
