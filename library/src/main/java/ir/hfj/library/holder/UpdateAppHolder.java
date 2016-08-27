package ir.hfj.library.holder;


import java.io.Serializable;

public class UpdateAppHolder implements Serializable
{
    public int state;
    public int progress;
    public String message;
    public String newVersion;
    public String currentVersion;
}
