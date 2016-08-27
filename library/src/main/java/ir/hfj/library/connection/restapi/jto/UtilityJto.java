package ir.hfj.library.connection.restapi.jto;


import ir.hfj.library.application.AppConfig;

public abstract class UtilityJto
{


	public abstract static class UpdateAppJto
	{

		public final static class Post extends PostJto
		{
			private int role = AppConfig.ROLE;
		}

		public final static class PostBack extends PostBackJto
		{
			public boolean isNeedUpdate;
			public String apkUrl;
			public String currentVersion;
			public String newVersion;
		}

	}

	//====================================================================================================



}
