package ir.hfj.library.connection.restapi.jto;

import org.apache.http.HttpStatus;

import java.lang.reflect.Type;

import ir.hfj.library.application.AppConfig;


public class PostBackJto extends BaseJto
{
	public static final int RESULT_OK = HttpStatus.SC_OK;
	public static final int RESULT_NOT_SUPPORT_VERSION = HttpStatus.SC_FORBIDDEN;
	public static final int RESULT_UNAUTHORIZED = HttpStatus.SC_UNAUTHORIZED;
	public static final int RESULT_BAD_REQUEST = HttpStatus.SC_BAD_REQUEST;
	public static final int RESULT_BLOCKED = HttpStatus.SC_SERVICE_UNAVAILABLE;

	public int stateCode;
	public String subjectMessage;
	public String detailMessage;

	public static  <T> T toObject(String json, int stateCode, Type type)
	{

		//JsonReader reader = new JsonReader(new StringReader(json));
		//reader.setLenient(true);
		//PostBackJTO dto = gson.fromJson(reader, type);
		PostBackJto dto = AppConfig.getGsonSetting().fromJson(json, type);
		dto.stateCode = stateCode;
		return (T) dto;
	}
}
