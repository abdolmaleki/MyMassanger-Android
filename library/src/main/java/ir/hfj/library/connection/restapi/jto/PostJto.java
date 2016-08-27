package ir.hfj.library.connection.restapi.jto;


import ir.hfj.library.application.AppConfig;

public abstract class PostJto extends BaseJto
{


	public String toJson()
	{
		return AppConfig.getGsonSetting().toJson(this, this.getClass());
	}

	/*public String toPostValue()
	{
		StringBuilder sb = new StringBuilder();

		for (Field field : getClass().getFields())
		{

			try
			{
				PreventPost haved = field.getAnnotation(PreventPost.class);
				if (haved == null)
				{
					sb.append("&" + field.getName() + "=" + field.get(this));
				}

			}
			catch (Exception e)
			{
			}
		}

		try
		{
			return URLEncoder.encode(sb.toString().substring(1), "utf-8");
		}
		catch (Exception e)
		{
			return "";
		}
	}*/
}
