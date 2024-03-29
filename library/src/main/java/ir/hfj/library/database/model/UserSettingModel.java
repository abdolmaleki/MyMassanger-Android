package ir.hfj.library.database.model;


import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.lang.reflect.Field;

@Table(name = "userSetting")
public class UserSettingModel extends Model
{

    @Column(name = "token", notNull = true)
    public String token;

    @Column(name = "name", notNull = true)
    public String name;

    @Column(name = "family", notNull = true)
    public String family;

    @Column(name = "key", notNull = true)
    public String key;

    @Column(name = "expired", notNull = true)
    public boolean expired;

    @Column(name = "phoneNumber", notNull = true)
    public String phoneNumber;

    @Column(name = "imageUrl")
    public String imageUrl;

    public final void setId(Long id)
    {
        try
        {
            Field field = Model.class.getDeclaredField("mId");
            field.setAccessible(true);
            field.set(this, id);
        }
        catch (Throwable ignore)
        {
        }
    }

}
