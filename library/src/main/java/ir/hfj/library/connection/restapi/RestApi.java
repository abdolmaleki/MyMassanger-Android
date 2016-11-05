package ir.hfj.library.connection.restapi;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import ir.hfj.library.R;
import ir.hfj.library.application.AppConfig;
import ir.hfj.library.connection.restapi.jto.AuthenticationJto;
import ir.hfj.library.connection.restapi.jto.PostBackJto;
import ir.hfj.library.connection.restapi.jto.PostJto;
import ir.hfj.library.connection.restapi.jto.UtilityJto;
import ir.hfj.library.exception.MyMessangerException;
import ir.hfj.library.receiver.SamimAction;
import ir.hfj.library.util.Encryption;
import ir.hfj.library.util.Helper;
import ir.hfj.library.util.Validation;


public final class RestApi
{

    private DefaultHttpClient mHttpClient = null;
    private boolean isCanceled = false;

    private String mToken = null;
    private String mKey = null;
    private Context mContext;
    private boolean mBroadcastUnAuthorization = true;

    public RestApi(Context context, String token, String key)
    {
        mContext = context;
        mToken = token;
        mKey = key;
    }

    public void setBroadcastUnAuthorization(boolean val)
    {
        mBroadcastUnAuthorization = val;
    }


    public AuthenticationJto.Login.PostBack login(AuthenticationJto.Login.Post jto) throws MyMessangerException
    {

        try
        {
            jto.connectionId = Encryption.encryptAes256Base64(jto.connectionId, mKey);
        }
        catch (Exception e)
        {
            throw new MyMessangerException(mContext.getString(R.string.messanger_ws_message_error_logindata));
        }

        HttpResponseParser response = executePostRequest(jto, AppConfig.RestApiAction.AccountLogin, false);

        AuthenticationJto.Login.PostBack postBackDto;

        try
        {

            postBackDto = PostBackJto.toObject(response.content, response.code, AuthenticationJto.Login.PostBack.class);

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
            throw new MyMessangerException(mContext.getString(R.string.messanger_ws_message_error_format));
        }

        return postBackDto;
    }

    public UtilityJto.UpdateAppJto.PostBack updateApp(UtilityJto.UpdateAppJto.Post jto) throws MyMessangerException
    {


        HttpResponseParser response = executePostRequest(jto, AppConfig.RestApiAction.UtilityUpdateApp, false);

        UtilityJto.UpdateAppJto.PostBack postBackDto;

        try
        {

            postBackDto = PostBackJto.toObject(response.content, response.code, UtilityJto.UpdateAppJto.PostBack.class);

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
            throw new MyMessangerException(mContext.getString(R.string.messanger_ws_message_error_format));
        }

        return postBackDto;
    }

    private HttpResponseParser executePostRequest(PostJto dto, String method, boolean encryption) throws MyMessangerException
    {
        return executePostRequest(dto, method, encryption, "UTF-8");
    }

    private HttpResponseParser executePostRequest(PostJto dto, String method, boolean encryption, String postCharset) throws MyMessangerException
    {

        try
        {

            if (!Validation.simple(mToken) || !Validation.simple(mKey))
            {
                //send broadcast not logined
                Intent intent = new Intent();
                intent.setAction(SamimAction.ACTIVATION_NULL);
                mContext.sendBroadcast(intent);

                throw new MyMessangerException(mContext.getString(R.string.messanger_ws_message_error_logindata));
            }

            mHttpClient = new DefaultHttpClient();

            HttpPost httpPost = new HttpPost(AppConfig.NETWORK_HOST_WS + "/" + method);

            httpPost.addHeader(AppConfig.HttpHeaders.Authorization, mToken);
            httpPost.addHeader(AppConfig.HttpHeaders.Version, AppConfig.getAppVersion() + "");
            httpPost.addHeader(AppConfig.HttpHeaders.Encryption, encryption ? "True" : "False");

            StringEntity se;
            if (encryption)
            {
                String con = "=" + Encryption.encryptAes256Base64(dto.toJson(), mKey);
                se = new StringEntity(con, postCharset);
                se.setContentType("application/x-www-form-urlencoded");
            }
            else
            {
                se = new StringEntity(dto.toJson(), postCharset);
                se.setContentType("application/json;charset=" + postCharset);
            }

            //////se.setContentType("application/json;charset=UTF-8");
            //////se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));

            httpPost.setEntity(se);

            if (isCanceled)
            {
                mHttpClient = null;
                throw new MyMessangerException(mContext.getString(R.string.messanger_ws_message_cancel_operation));
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
                        //
                        //if (api.verifyKey(mKey, mToken))
                        //{
                        //    throw new MyMessangerException(mContext.getString(R.string.samim_ws_message_error_verifykey_conflict));
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

                        if (serverEncryption)
                        {
                            responseParser.content = Helper.correctJson(Encryption.decryptAes256Base64(EntityUtils.toString(entity, postCharset), mKey));
                            return responseParser;
                        }
                        else
                        {
                            responseParser.content = Helper.correctJson(EntityUtils.toString(entity, postCharset));
                            return responseParser;
                        }

                    }
                    catch (Exception e)
                    {
                        if (AppConfig.DEBUG)
                        {
                            Log.e(AppConfig.LOG_TAG, e.getMessage());
                        }
                        throw new MyMessangerException(mContext.getString(R.string.messanger_ws_message_error_format));
                    }

                }
                else if (status == HttpStatus.SC_NOT_FOUND)
                {
                    throw new MyMessangerException(mContext.getString(R.string.messanger_ws_message_error_notfound));
                }
                else
                {
                    throw new MyMessangerException(mContext.getString(R.string.messanger_ws_message_error_httpcode));
                }

            }
            else
            {
                throw new MyMessangerException(mContext.getString(R.string.messanger_ws_message_error_null_data));
            }

        }
        catch (MyMessangerException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new MyMessangerException(mContext.getString(R.string.messanger_ws_message_error_unknow));
        }

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
