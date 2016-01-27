package deepviz.utils;


public class Result {
    private String msg;
    private DeepvizResultStatus status;

    public Result(DeepvizResultStatus status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public void setStatus(DeepvizResultStatus status) {
        this.status = status;
    }

    public DeepvizResultStatus getStatus(){
        return this.status;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg(){
        return this.msg;
    }

    public String toString() {
        return "Result(DeepvizResultStatus='" + this.status + "', msg='" + this.msg + "')";
    }
}
