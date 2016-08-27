package ir.hfj.library.connection.restapi.jto;


import ir.hfj.library.application.AppConfig;

public abstract class AuthenticationJto
{


	public abstract static class Login
	{

		public final static class Post extends PostJto
		{
			public String connectionId;
			private int role = AppConfig.ROLE;

			public Post(String connectionId)
			{
				this.connectionId = connectionId;
			}
		}

		public final static class PostBack extends PostBackJto
		{

			public boolean isSuccessful;
			public boolean isErrorSocket;
			public String errorMessage;
		}
	}

	//====================================================================================================



}
