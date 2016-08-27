package ir.hfj.library.connection.restapi.jto;


import ir.hfj.library.application.AppConfig;

public abstract class ActivationJto
{


	public abstract static class Phone
	{

		public final static class Post extends PostJto
		{

			public String phoneNumber;
			private int role = AppConfig.ROLE;

			public Post(String phoneNumber)
			{
				this.phoneNumber = phoneNumber;
			}

		}

		public final static class PostBack extends PostBackJto
		{

			public String activeToken;
		}
	}

	//==========================================================================================================

	public abstract static class Register
	{

		public final static class Post extends PostJto
		{

			public String username;
			public String password;
			public String name;
			public String family;
			public String car;

			public Post(String username, String password, String name, String family, String car)
			{
				this.username = username;
				this.password = password;
				this.name = name;
				this.family = family;
				this.car = car;
			}

		}

		public final static class PostBack extends PostBackJto
		{

			public boolean isSuccessful;
		}
	}

	//==========================================================================================================
	public abstract static class Verify
	{

		public final static class Post extends PostJto
		{

			public String username;
			public String password;
			public String secretMessage;
			public String serial;
			public String key;

			public Post(String username, String password, String secretMessage, String serial, String key)
			{
				this.username = username;
				this.password = password;
				this.secretMessage = secretMessage;
				this.serial = serial;
				this.key = key;
			}
		}


		public final static class PostBack extends PostBackJto
		{
			public String token;
			public String secretMessage;
			public String name;
			public String family;
		}
	}


}
