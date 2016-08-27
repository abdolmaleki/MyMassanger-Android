package ir.hfj.library.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;

public class VideoHelper
{

//    public static Bitmap createThumbnailAtTime(String videofilePath, int timeInSeconds)
//    {
//        MediaMetadataRetriever mMMR = new MediaMetadataRetriever();
//        mMMR.setDataSource(videofilePath);
//        //api time unit is microseconds
//        return mMMR.getFrameAtTime(timeInSeconds * 1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
//    }

    public static Bitmap createThumbnailAtTime(String videofilePath)
    {
        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(videofilePath, MediaStore.Video.Thumbnails.MINI_KIND);
        return thumb;

    }


    public static String getPath(Context context, Uri contentUri)
    {
        Cursor cursor = null;
        try
        {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
    }

    public static boolean openVideo(Context ctx, String path)
    {
        File file = new File(path);
        Uri uri = Uri.fromFile(file);
        Intent fileOpen = new Intent(Intent.ACTION_VIEW);
        fileOpen.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        fileOpen.setDataAndType(uri, "video/*");
        try
        {
            ctx.startActivity(fileOpen);
            return true;
        }
        catch (ActivityNotFoundException e)
        {

            return false;
        }
    }

}
