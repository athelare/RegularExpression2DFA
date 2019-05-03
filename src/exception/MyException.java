package exception;

public class MyException extends Exception{
    private String message;
    public MyException(String s){
        this.message=s;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
