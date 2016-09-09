package com.example.myapplication.connection.restapi;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.myapplication.connection.restapi.jto.FileJto;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import ir.hfj.library.R;
import ir.hfj.library.application.AppConfig;
import ir.hfj.library.connection.restapi.jto.PostBackJto;
import ir.hfj.library.exception.SamimException;
import ir.hfj.library.receiver.SamimAction;
import ir.hfj.library.util.Helper;
import ir.hfj.library.util.Validation;


public final class FileUploadApi
{

    private DefaultHttpClient mHttpClient = null;
    private boolean isCanceled = false;

    private String mToken = null;
    private String mKey = null;
    private Context mContext;
    private boolean mBroadcastUnAuthorization = true;
    private long totalSize;

    public FileUploadApi(Context context, String token, String key)
    {
        mContext = context;
        mToken = token;
        mKey = key;
    }

    public void setBroadcastUnAuthorization(boolean val)
    {
        mBroadcastUnAuthorization = val;
    }

    public static interface ProgressListener
    {

        void transferred(long num);
    }
    private static class AndroidMultiPartEntity extends MultipartEntity
    {

        private final ProgressListener listener;

        public AndroidMultiPartEntity(final ProgressListener listener)
        {
            super();
            this.listener = listener;
        }

        public AndroidMultiPartEntity(final HttpMultipartMode mode,
                                      final ProgressListener listener)
        {
            super(mode);
            this.listener = listener;
        }

        public AndroidMultiPartEntity(HttpMultipartMode mode, final String boundary,
                                      final Charset charset, final ProgressListener listener)
        {
            super(mode, boundary, charset);
            this.listener = listener;
        }

        @Override
        public void writeTo(final OutputStream outstream) throws IOException
        {
            super.writeTo(new CountingOutputStream(outstream, this.listener));
        }


        public static class CountingOutputStream extends FilterOutputStream
        {

            private final ProgressListener listener;
            private long transferred;

            public CountingOutputStream(final OutputStream out,
                                        final ProgressListener listener)
            {
                super(out);
                this.listener = listener;
                this.transferred = 0;
            }

            public void write(byte[] b, int off, int len) throws IOException
            {
                out.write(b, off, len);
                this.transferred += len;
                this.listener.transferred(this.transferred);
            }

            public void write(int b) throws IOException
            {
                out.write(b);
                this.transferred++;
                this.listener.transferred(this.transferred);
            }
        }



    }

    public FileJto.PostBack uploadFile(FileJto.Post jto, OnProgressListener listener) throws SamimException
    {

        HttpResponseParser response = executePostRequest(jto, AppConfig.RestApiAction.MediaUpload, listener);

        FileJto.PostBack postBackDto;

        try
        {

            postBackDto = PostBackJto.toObject(response.content, response.code, FileJto.PostBack.class);

            if (postBackDto == null)
            {
                throw new Exception();
            }

        }
        catch (Exception e)
        {
            if (AppConfig.DEBUG)
            {
                Log.e(AppConfig.LOG_TAG, e.getMessage());
            }
            throw new SamimException(mContext.getString(R.string.samim_ws_message_error_format));
        }

        return postBackDto;

    }


    private HttpResponseParser executePostRequest(FileJto.Post jto, String method, final OnProgressListener listener) throws SamimException
    {

        try
        {

            if (!Validation.simple(mToken) || !Validation.simple(mKey))
            {
                //send broadcast not logined
                Intent intent = new Intent();
                intent.setAction(SamimAction.ACTIVATION_NULL);
                mContext.sendBroadcast(intent);

                throw new SamimException(mContext.getString(R.string.samim_ws_message_error_logindata));
            }

            mHttpClient = new DefaultHttpClient();

            HttpPost httpPost = new HttpPost(AppConfig.NETWORK_HOST_WS + "/" + method);

            httpPost.addHeader(AppConfig.HttpHeaders.Authorization, mToken);
            httpPost.addHeader(AppConfig.HttpHeaders.Version, AppConfig.getAppVersion() + "");
            httpPost.addHeader(AppConfig.HttpHeaders.Encryption, "False");
            httpPost.addHeader("Extension", jto.extension);
            //httpPost.addHeader(AppConfig.HttpHeaders.Encryption, encryption ? "True" : "False");


            //------------------------

            AndroidMultiPartEntity en = new AndroidMultiPartEntity(
                    new ProgressListener()
                    {
                        int oldPercent = Integer.MIN_VALUE;

                        @Override
                        public void transferred(long num)
                        {
                            if (listener != null)
                            {
                                int percent = (int) ((num / (float) totalSize) * 100);

                                if(oldPercent != percent)
                                {
                                    oldPercent = percent;
                                    listener.OnProgress(percent);
                                }
                            }
                        }
                    });

            File sourceFile = new File(jto.path);

            // Adding file data to http body
            en.addPart("image", new FileBody(sourceFile));

            // Extra parameters if you want to pass to server
            //en.addPart("extension", new StringBody(jto.extension));

            totalSize = en.getContentLength();

            httpPost.setEntity(en);

            //------------------------

            if (isCanceled)
            {
                mHttpClient = null;
                throw new SamimException(mContext.getString(R.string.samim_ws_message_cancel_operation));
            }

            HttpResponse response = mHttpClient.execute(httpPost);

            mHttpClient = null;

            if (response != null)
            {
                int status = response.getStatusLine().getStatusCode();

                HttpResponseParser responseParser = new HttpResponseParser();
                responseParser.code = status;

                //include [status == HttpStatus.SC_FORBIDDEN] when encryption all message(request)
                if (status == HttpStatus.SC_OK || status == HttpStatus.SC_INTERNAL_SERVER_ERROR || status == HttpStatus.SC_UNAUTHORIZED || status == HttpStatus.SC_BAD_REQUEST || status == HttpStatus.SC_FORBIDDEN || status == HttpStatus.SC_SERVICE_UNAVAILABLE)
                {

                    if (mBroadcastUnAuthorization && status == HttpStatus.SC_UNAUTHORIZED)
                    {
                        //ActivationRestApi api = new ActivationRestApi(mContext);

                        //if (api.verifyKey(mKey, mToken))
                        //{
                        //    throw new SamimException(mContext.getString(R.string.samim_ws_message_error_verifykey_conflict));
                        //}

                        Intent intent = new Intent();
                        intent.setAction(SamimAction.ACTIVATION_EXPIRED);
                        mContext.sendBroadcast(intent);
                    }

                    try
                    {
                        boolean serverEncryption = false;
                        Header[] headers;
                        headers = response.getHeaders(AppConfig.HttpHeaders.Encryption);
                        if (headers != null && headers.length > 0)
                        {
                            serverEncryption = headers[0].getValue().equalsIgnoreCase("True");
                        }

                        HttpEntity entity = response.getEntity();

                        //if (serverEncryption)
                        //{
                        //    responseParser.content = Helper.correctJson(Encryption.decryptAes256Base64(EntityUtils.toString(entity, postCharset), mKey));
                        //    return responseParser;
                        //}
                        //else
                        //{
                        responseParser.content = Helper.correctJson(EntityUtils.toString(entity, "UTF-8"));
                        return responseParser;
                        //}

                    }
                    catch (Exception e)
                    {
                        if (AppConfig.DEBUG)
                        {
                            Log.e(AppConfig.LOG_TAG, e.getMessage());
                        }
                        throw new SamimException(mContext.getString(R.string.samim_ws_message_error_format));
                    }

                }
                else if (status == HttpStatus.SC_NOT_FOUND)
                {
                    throw new SamimException(mContext.getString(R.string.samim_ws_message_error_notfound));
                }
                else
                {
                    throw new SamimException(mContext.getString(R.string.samim_ws_message_error_httpcode));
                }

            }
            else
            {
                throw new SamimException(mContext.getString(R.string.samim_ws_message_error_null_data));
            }

        }
        catch (SamimException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new SamimException(mContext.getString(R.string.samim_ws_message_error_unknow));
        }

    }

    public interface OnProgressListener
    {
        void OnProgress(int percent);
    }

    private static class HttpResponseParser
    {

        public int code;
        public String content;
    }

    public void cancel()
    {
        isCanceled = true;
        if (mHttpClient != null)
        {
            mHttpClient.getConnectionManager().shutdown();
        }
    }

}
