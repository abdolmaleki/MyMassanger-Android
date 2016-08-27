package ir.hfj.library.holder;

import java.io.Serializable;

import ir.hfj.library.database.model.UserSettingModel;

public class LoginDataHolder implements Serializable
{
	public String phoneNumber;
	public String activeCode;
	public String token;
	public String activeToken;
	public String secretMessage;
	public String key;
	public String name;
	public String family;
	//public boolean gender;

	public UserSettingModel getUserSettingModel()
	{
		UserSettingModel model = new UserSettingModel();
		model.key = key;
		model.token = token;
		model.expired = false;
		model.name = name;
		model.family = family;

		return model;
	}

}
