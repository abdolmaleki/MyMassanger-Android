package ir.hfj.library.connection.socket.dto;


import java.io.Serializable;


public class BaseDto implements Serializable
{



    public long modelId = -1;
    public final long autoId = System.nanoTime();
    public long arg1;


    public static class Result<T> implements Serializable
    {
        public String baseMessage = "";
        public boolean isSuccessful = true;
        public boolean isException = false;

        public long autoIdResponse = -1;

        public long arg1Response;
        public T request;

        public boolean isValid()
        {
            return isSuccessful;
        }

    }
}
