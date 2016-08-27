package ir.hfj.library.database.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;

import java.util.UUID;

public abstract class BaseModel extends Model
{
    public static final String __id = "id";
    public static final String __guid = "guid";

    @Column(name = "guid", unique = true)
    private UUID guid;

    public BaseModel()
    {

    }

    public BaseModel(UUID guid)
    {
        this.guid = guid;
    }

    public UUID getGuid()
    {
        return guid;
    }


}
