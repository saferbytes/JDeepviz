package deepviz;


import org.json.JSONObject;

public class Result {
    private String status;
    private JSONObject data;
    private String msg;

    public Result(String status, String msg, JSONObject data) {
        this.status = status;
        this.data = data;
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

    public void setData(JSONObject data) {
        this.data = data;
    }

    public JSONObject getData(){
        return this.data;
    }

    public String toString() {
        if (this.data != null) {
            return "Result(status='" + this.status + "', msg='" + this.msg + "', data='" + this.data.toString() + "')";
        } else {
            return "Result(status='" + this.status + "', msg='" + this.msg + "')";
        }
    }
}
