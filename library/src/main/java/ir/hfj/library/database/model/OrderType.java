package ir.hfj.library.database.model;


public enum OrderType
{
    ASC("ASC"),
    DESC("DESC");

    private String value;
    OrderType(String v)
    {
        value = v;
    }

    @Override
    public String toString()
    {
        return value;
    }
}
