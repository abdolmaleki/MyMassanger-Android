package ir.hfj.library.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.provider.MediaStore;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageHelper
{

    public static String compress(String path, int quality)
    {
        Bitmap src = BitmapFactory.decodeFile(path);

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        src.compress(Bitmap.CompressFormat.JPEG, quality, os);

        byte[] byteArray = os.toByteArray();

        String encoded = Base64.encodeBytes(byteArray);

        return encoded;
    }
    public static Bitmap decode(String encoded)
    {
        byte[] decodedString;
        try
        {
            decodedString = Base64.decode(encoded);
        }
        catch (Exception e)
        {
            return null;
        }

        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        Paint paint = new Paint();
        Canvas canvas = new Canvas();
        canvas.drawBitmap(bitmap, 10, 10 + bitmap.getHeight() + 10, paint);

        return bitmap;
    }

    public static String getPath(Context context, Uri uri)
    {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    public static boolean isImageCached(String token)
    {
        // return MemoryCacheUtils.findCachedBitmapsForImageUri(token, ImageLoader.getInstance().getMemoryCache()).size() > 0;
        return DiskCacheUtils.findInCache(token, ImageLoader.getInstance().getDiskCache()) != null;
    }

    public static boolean saveCompressedImage(String originalImagePath, String destinationPath, int quality)
    {
        Bitmap bitmap = BitmapFactory.decodeFile(originalImagePath);

        File image = new File(destinationPath);
        boolean success = false;
        FileOutputStream outStream;

        try
        {
            outStream = new FileOutputStream(image);
            if (image.exists())
            {
                image.deleteOnExit();
            }

            bitmap = scale(bitmap);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outStream);


            outStream.flush();
            outStream.close();
            success = true;
        }

        catch (FileNotFoundException e)
        {

        }
        catch (IOException e)
        {

        }

        return success;
    }


    private static Bitmap scale(Bitmap bitmap)
    {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();


        if(w > h)
        {

            if(w > 1000)
            {
                h = 1000 * h / w;
                w = 1000;
            }

        }
        else
        {
            if(h > 1000)
            {
                w = 1000 * w / h;
                h = 1000;
            }
        }

        return Bitmap.createScaledBitmap(bitmap, w, h, false);
    }


}
