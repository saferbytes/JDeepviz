package deepviz;


public class Result {
    private String status;
    private String msg;

    public Result(String status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus(){
        return this.status;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg(){
        return this.msg;
    }

    public String toString() {
        return "Result(status='" + this.status + "', msg='" + this.msg + "')";
    }
}
