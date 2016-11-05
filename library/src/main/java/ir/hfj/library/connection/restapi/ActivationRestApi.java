package ir.hfj.library.connection.restapi;


import android.content.Context;
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
import ir.hfj.library.connection.restapi.jto.ActivationJto;
import ir.hfj.library.connection.restapi.jto.PostBackJto;
import ir.hfj.library.connection.restapi.jto.PostJto;
import ir.hfj.library.exception.MyMessangerException;
import ir.hfj.library.util.Encryption;
import ir.hfj.library.util.Helper;


public final class ActivationRestApi
{

	private final Context mContext;
	private DefaultHttpClient mHttpClient = null;
	private boolean isCanceled = false;

	public ActivationRestApi(Context context)
	{
		this.mContext = context;
	}

	public ActivationJto.Phone.PostBack activationPhone(ActivationJto.Phone.Post dto) throws MyMessangerException
	{

		HttpResponseParser response = executePostRequest(dto, AppConfig.RestApiAction.ActivationPhone, "", "", false);

		ActivationJto.Phone.PostBack postBackDto;

		try
		{

			postBackDto = PostBackJto.toObject(response.content, response.code, ActivationJto.Phone.PostBack.class);

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

	public ActivationJto.Verify.PostBack activationVerify(String token, ActivationJto.Verify.Post dto, String activeCode) throws MyMessangerException
	{

		HttpResponseParser response = executePostRequest(dto, AppConfig.RestApiAction.ActivationVerify, token, activeCode, true);

		ActivationJto.Verify.PostBack postBackDto;

		try
		{

			postBackDto = PostBackJto.toObject(response.content, response.code, ActivationJto.Verify.PostBack.class);

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

	public ActivationJto.Register.PostBack activationRegister(String token, ActivationJto.Register.Post dto, String activeCode) throws MyMessangerException
	{

		HttpResponseParser response = executePostRequest(dto, AppConfig.RestApiAction.ActivationRegister, token, activeCode, true);

		ActivationJto.Register.PostBack postBackDto;

		try
		{

			postBackDto = PostBackJto.toObject(response.content, response.code, ActivationJto.Register.PostBack.class);


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

	private HttpResponseParser executePostRequest(PostJto dto, String method, String token, String key, boolean encryption) throws MyMessangerException
	{

		try
		{

			mHttpClient = new DefaultHttpClient();

			HttpPost httpPost = new HttpPost(AppConfig.NETWORK_HOST_WS + "/" + method);

			httpPost.addHeader(AppConfig.HttpHeaders.Authorization, token);
			httpPost.addHeader(AppConfig.HttpHeaders.Version, AppConfig.getAppVersion());
			httpPost.addHeader(AppConfig.HttpHeaders.Encryption, encryption ? "True" : "False");

			StringEntity se;
			if (encryption)
			{
				String con = "=" + Encryption.encryptAes256Base64(dto.toJson(), key);
				se = new StringEntity(con, "utf-8");
				se.setContentType("application/x-www-form-urlencoded");
			}
			else
			{
				se = new StringEntity(dto.toJson(), "utf-8");
				se.setContentType("application/json;charset=UTF-8");
			}

			//se.setContentType("application/json;charset=UTF-8");
			//se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));

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
					try
					{
						boolean serverEncryption = false;
						Header[] headers = response.getHeaders(AppConfig.HttpHeaders.Encryption);
						if (headers != null && headers.length > 0)
						{
							serverEncryption = headers[0].getValue().equalsIgnoreCase("True");
						}

						HttpEntity entity = response.getEntity();

						if (serverEncryption)
						{
							responseParser.content = Helper.correctJson(Encryption.decryptAes256Base64(EntityUtils.toString(entity, "utf-8"), key));
							return responseParser;
						}
						else
						{
							responseParser.content = Helper.correctJson(EntityUtils.toString(entity, "utf-8"));
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
