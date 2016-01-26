package deepviz;


import org.json.JSONObject;

public class ResultSuccess extends Result {

    public ResultSuccess(String msg, JSONObject data) {
        super("success", msg, data);
    }
}
