package ir.hfj.library.connection.restapi;


import android.content.Context;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import ir.hfj.library.R;
import ir.hfj.library.application.AppConfig;
import ir.hfj.library.exception.MyMessangerException;
import ir.hfj.library.util.Base64;

public class UploadRestApi
{

    private String mToken = null;
    private Context mContext;
    private long mTotalSize;

    public UploadRestApi(Context context, String token)
    {
        mContext = context;
        mToken = token;
    }

    public String uploadFile(byte[] file, String action, final OnUploadProgress progressListener) throws MyMessangerException
    {

        try
        {

            if(file == null || file.length <= 0)
            {
                throw new Exception();
            }

            DefaultHttpClient mHttpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(AppConfig.NETWORK_HOST_WS + "/Media/Upload");
            StringEntity se;
            se = new StringEntity("=" + Base64.encodeBytes(file));
            se.setContentType("application/x-www-form-urlencoded");
            httpPost.setEntity(se);
            HttpResponse response = mHttpClient.execute(httpPost);

            HttpEntity entity = response.getEntity();

            progressListener.OnUploadProgress(100, 50);

            return EntityUtils.toString(entity).replace("\"","");


        }
        catch (Exception e)
        {
            throw new MyMessangerException(mContext.getString(R.string.messanger_ws_message_error_unknow));
        }


    }

    /*public String uploadFileXXXX(byte[] file, String action, final OnUploadProgress progressListener) throws MyMessangerException
    {


        HttpClient httpclient = new DefaultHttpClient();
        //HttpPost httppost = new HttpPost(AppConfig.NETWORK_HOST_WS + "/Media/PutContent");
        HttpPost httppost = new HttpPost("http://192.168.13.3:5000/FileManagerService.svc/UploadFile?Path=" + UUID.randomUUID().toString());

        try
        {
            CustomMultiPartEntity entity = new CustomMultiPartEntity(new CustomMultiPartEntity.ProgressListener()
            {
                @Override
                public void transferred(long num)
                {
                    if (progressListener != null)
                    {
                        progressListener.OnUploadProgress(num, mTotalSize);
                    }
                }
            });

            ContentBody mimePart = new ByteArrayBody(file, "file");
            entity.addPart("file", mimePart);
            mTotalSize = entity.getContentLength();
            httppost.setEntity(entity);
            httppost.addHeader(AppConfig.HttpHeaders.Authorization, mToken);
            httppost.addHeader(AppConfig.HttpHeaders.Version, AppConfig.getAppVersion() + "");

            HttpResponse response = httpclient.execute(httppost);

            if (response != null)
            {
                int status = response.getStatusLine().getStatusCode();

                //include [status == HttpStatus.SC_FORBIDDEN] when encryption all message(request)
                if (status == HttpStatus.SC_OK)
                {
                    return EntityUtils.toString(response.getEntity());
                }
                else if (status == HttpStatus.SC_NOT_FOUND)
                {
                    throw new MyMessangerException(mContext.getString(R.string.samim_ws_message_error_notfound));
                }
                else if (status == HttpStatus.SC_INTERNAL_SERVER_ERROR)
                {
                    throw new MyMessangerException(mContext.getString(R.string.samim_ws_message_error_server_internal_error));
                }
                else
                {
                    throw new MyMessangerException(mContext.getString(R.string.samim_ws_message_error_httpcode));
                }
            }

            else
            {
                throw new MyMessangerException(mContext.getString(R.string.samim_ws_message_error_null_data));
            }

        }
        catch (IOException e)
        {
            throw new MyMessangerException(mContext.getString(R.string.samim_ws_message_error_unknow));
        }


    }*/

    public interface OnUploadProgress
    {

        void OnUploadProgress(long v, long total);
    }

}
