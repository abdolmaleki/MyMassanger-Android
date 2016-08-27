package ir.hfj.library.util;

import android.util.Log;

import ir.hfj.library.application.AppConfig;

public class Encryption
{

	public static String encryptAes256Base64(String plainText, String yourKey)
	{

		try
		{
			CryptLib _crypt = new CryptLib();
			String key = CryptLib.SHA256(yourKey, 32); //32 bytes = 256 bit
			String iv = "a01rb147jg36d9i1";//CryptLib.generateRandomIV(16); //16 bytes = 128 bit

			return Base64.encodeBytes(_crypt.encrypt(plainText, key, iv).getBytes("UTF-8"));

		}
		catch (Exception e)
		{
			if (AppConfig.DEBUG)
			{
				Log.e(AppConfig.LOG_TAG, "encryptAes256 error: " + e.getMessage());
			}
		}

		return "";
		//return "[" + key + "]" + text + "[" + key + "]";
	}

	public static String decryptAes256Base64(String encryptText, String yourKey)
	{

		try
		{
			CryptLib _crypt = new CryptLib();
			String key = CryptLib.SHA256(yourKey, 32); //32 bytes = 256 bit
			String iv = "a01rb147jg36d9i1";//CryptLib.generateRandomIV(16); //16 bytes = 128 bit

			return _crypt.decrypt(new String(Base64.decode(encryptText)), key, iv);
		}
		catch (Exception e)
		{
			if (AppConfig.DEBUG)
			{
				Log.e(AppConfig.LOG_TAG, "decryptAes256 error: " + e.getMessage());
			}
		}

		return "";
		//return text.replace("[" + key + "]", "");
	}
}
