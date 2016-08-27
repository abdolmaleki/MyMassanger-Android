package ir.hfj.library.util;

public interface IRequestTaxiDialog
{

    int TAG_VOICE = 1;
    int TAG_ADDRESS = 2;
    int TAG_ADDRESS_HISTORY = 3;

    void setResult(String message);

    interface Callback
    {
        void onAcceptDialog(IRequestTaxiDialog dialog, int tag, Object result);
    }

}