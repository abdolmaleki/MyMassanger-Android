package ir.hfj.library.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.List;

public class ContactUtil
{

    public static List<String> getContactsPhoneNumbers(Context ctx)
    {
        List<String> phoneNumbers = new ArrayList<>();

        ContentResolver cr = ctx.getContentResolver();

        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                              null, null, null, null);

        if (cur != null)
        {
            if (cur.getCount() > 0)
            {
                while (cur.moveToNext())
                {
                    String id = cur.getString(
                            cur.getColumnIndex(ContactsContract.Contacts._ID));

                    if (cur.getInt(cur.getColumnIndex(
                            ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0)
                    {
                        Cursor pCur = cr.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{id}, null);
                        if (pCur != null)
                        {
                            while (pCur.moveToNext())
                            {
                                String phoneNo = pCur.getString(pCur.getColumnIndex(
                                        ContactsContract.CommonDataKinds.Phone.NUMBER));
                                phoneNumbers.add(getStandardPhoneNumber(phoneNo));
                            }

                            pCur.close();
                        }
                    }
                }
            }
            cur.close();
        }
        return phoneNumbers;
    }


    private static String getStandardPhoneNumber(String phoneNumber)
    {
        String standardPhoneNumber = phoneNumber.replace(" ", "");
        standardPhoneNumber = standardPhoneNumber.replace("+98", "");
        if (standardPhoneNumber.charAt(0) == '0')
        {
            standardPhoneNumber = standardPhoneNumber.substring(1);
        }

        return standardPhoneNumber;
    }


}
