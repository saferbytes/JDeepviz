package deepviz.intel;

import com.mashape.unirest.http.JsonNode;
import deepviz.ResultError;
import deepviz.Result;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import deepviz.ResultSuccess;
import org.json.JSONObject;
import org.json.JSONArray;


public class Intel {

    public static final String URL_INTEL_SEARCH            = "https://api.deepviz.com/intel/search";
    public static final String URL_INTEL_IP                = "https://api.deepviz.com/intel/network/ip";
    public static final String URL_INTEL_DOMAIN            = "https://api.deepviz.com/intel/network/domain";
    public static final String URL_INTEL_SEARCH_ADVANCED   = "https://api.deepviz.com/intel/search/advanced";


    public Result ipInfo(String api_key, String time_delta, boolean history) {
        if (api_key == null) {
            return new ResultError("Parameters 'api_key' cannot be null");
        }

        if (time_delta == null) {
            return new ResultError("Parameters 'time_delta' cannot be null");
        }

        String strHistory = "false";
        if (history) {
            strHistory = "true";
        }

        return this.ipInfo(api_key, null, time_delta, strHistory);
    }

    public Result ipInfo(String api_key, JSONArray ip, boolean history) {
        if (api_key == null) {
            return new ResultError("Parameters 'api_key' cannot be null");
        }

        if (ip == null || ip.length() == 0) {
            return new ResultError("Parameters 'ip' cannot be null or empty JSONArray");
        }

        String strHistory = "false";
        if (history) {
            strHistory = "true";
        }

        return this.ipInfo(api_key, ip, null, strHistory);
    }

    private Result ipInfo(String api_key, JSONArray ip, String time_delta, String history) {
        HttpResponse response;

        if (ip != null) {
            try {
                response = Unirest.post(Intel.URL_INTEL_IP)
                        .header("Content-Type", "application/json")
                        .body("{\"api_key\":\"" + api_key + "\", \"history\":\"" + history + "\", \"ip\": " + ip.toString() + "}")
                        .asJson();
            } catch (UnirestException e) {
                return new ResultError("Error while connecting to Deepviz: " + e.getMessage());
            }
        } else if (time_delta != null) {
            try {
                response = Unirest.post(Intel.URL_INTEL_IP)
                        .header("Content-Type", "application/json")
                        .body("{\"api_key\":\"" + api_key + "\", \"history\":\"" + history + "\", \"time_delta\": \"" + time_delta + "\"}")
                        .asJson();
            } catch (UnirestException e) {
                return new ResultError("Error while connecting to Deepviz: " + e.getMessage());
            }
        } else {
            return new ResultError("Unexpected error");
        }

        if (response.getStatus() == 200) {
            JsonNode response_json = new JsonNode(response.getBody().toString());
            return new ResultSuccess(String.valueOf(response.getStatus()), (JSONObject) response_json.getObject().get("data"));
        } else {
            JsonNode response_json = new JsonNode(response.getBody().toString());
            String errMsg = String.valueOf(response_json.getObject().get("errmsg"));
            return new ResultError(String.valueOf(response.getStatus()) + " - " + errMsg);
        }
    }
}
