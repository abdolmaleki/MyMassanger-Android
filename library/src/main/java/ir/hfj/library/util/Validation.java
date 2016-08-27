package ir.hfj.library.util;

public class Validation
{
	public static boolean phoneNumber(String phone)
	{
		try
		{
			if (phone == null || phone.length() != 11 || phone.charAt(0) != '0')
			{
				throw new Exception();
			}
			long phoneLong = (long) Long.parseLong(phone);

			if (String.valueOf(phoneLong).length() != 10)
			{
				throw new Exception();
			}

			return true;
		}
		catch (Exception ex)
		{
		}

		return false;

	}

	public static boolean simple(String text)
	{
		
		return (text != null && text.length() > 0);

	}

}
