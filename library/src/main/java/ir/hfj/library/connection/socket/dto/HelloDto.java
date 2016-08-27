package ir.hfj.library.connection.socket.dto;

public class HelloDto extends BaseDto
{
    public String message;
    public int number;

    public static final class Result extends BaseDto.Result
    {
        public String message;
    }

}
