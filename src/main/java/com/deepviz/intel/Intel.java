package com.deepviz.intel;

import com.deepviz.intel.input.AdvancedSearchInput;
import com.deepviz.utils.DeepvizResultStatus;
import com.deepviz.utils.Result;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;


public class Intel {

    public static final String URL_INTEL_SEARCH            = "https://api.deepviz.com/intel/search";
    public static final String URL_INTEL_DOWNLOAD_REPORT   = "https://api.deepviz.com/intel/report";
    public static final String URL_INTEL_IP                = "https://api.deepviz.com/intel/network/ip";
    public static final String URL_INTEL_DOMAIN            = "https://api.deepviz.com/intel/network/domain";
    public static final String URL_INTEL_SEARCH_ADVANCED   = "https://api.deepviz.com/intel/search/advanced";

    public Result ipInfo(String api_key, String ip, List<String> filters) {
        if (api_key == null || api_key.equals("")) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "API key cannot be null or empty String");
        }

        if (ip == null || ip.equals("")) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "Parameters missing or invalid. You must specify an IP");
        }

        HttpResponse response;

        try {
            String body;
            if (filters == null || filters.isEmpty()) {
                body = "{\"api_key\":\"" + api_key + "\", \"ip\": \"" + ip + "\"}";
            } else {
                JSONArray json_filters = new JSONArray();
                for (String filter : filters) {
                    json_filters.put(filter);
                }

                body = "{\"api_key\":\"" + api_key + "\", \"ip\": \"" + ip + "\", \"output_filters\": " + json_filters.toString() + "}";
            }

            response = Unirest.post(Intel.URL_INTEL_IP)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .asJson();
        } catch (UnirestException e) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_NETWORK_ERROR, "Error while connecting to Deepviz: " + e.getMessage());
        }

        JsonNode response_json;
        try {
            response_json = new JsonNode(response.getBody().toString());
        } catch (Exception e) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INTERNAL_ERROR, "Error loading Deepviz response");
        }

        if (response.getStatus() == 200) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_SUCCESS, response_json.getObject().get("data").toString());
        } else {
            String errMsg = response_json.getObject().get("errmsg").toString();
            DeepvizResultStatus code;
            if (response.getStatus() >= 500) {
                code = DeepvizResultStatus.DEEPVIZ_STATUS_SERVER_ERROR;
            } else {
                code = DeepvizResultStatus.DEEPVIZ_STATUS_CLIENT_ERROR;
            }
            return new Result(code, String.valueOf(response.getStatus()) + " - " + errMsg);
        }
    }

    public Result domainInfo(String api_key, String domain, List<String> filters) {
        if (api_key == null || api_key.equals("")) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "API key cannot be null");
        }

        if (domain == null || domain.equals("")) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "Parameters missing or invalid. You must specify a domain");
        }

        HttpResponse response = null;
        try {
            String body;
            if (filters == null || filters.isEmpty()) {
                body = "{\"api_key\":\"" + api_key + "\", \"domain\": \"" + domain + "\"}";
            } else {
                JSONArray json_filters = new JSONArray();
                for (String filter : filters) {
                    json_filters.put(filter);
                }

                body = "{\"api_key\":\"" + api_key + "\", \"domain\": \"" + domain + "\", \"output_filters\": " + json_filters.toString() + "}";
            }

            response = Unirest.post(Intel.URL_INTEL_DOMAIN)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .asJson();
        } catch (UnirestException e) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_NETWORK_ERROR, "Error while connecting to Deepviz: " + e.getMessage());
        }

        JsonNode response_json;
        try {
            response_json = new JsonNode(response.getBody().toString());
        } catch (Exception e) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INTERNAL_ERROR, "Error loading Deepviz response");
        }

        if (response.getStatus() == 200) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_SUCCESS, response_json.getObject().get("data").toString());
        } else {
            String errMsg = response_json.getObject().get("errmsg").toString();
            DeepvizResultStatus code;
            if (response.getStatus() >= 500) {
                code = DeepvizResultStatus.DEEPVIZ_STATUS_SERVER_ERROR;
            } else {
                code = DeepvizResultStatus.DEEPVIZ_STATUS_CLIENT_ERROR;
            }
            return new Result(code, String.valueOf(response.getStatus()) + " - " + errMsg);
        }
    }

    public Result search(String api_key, String search_string) {
        return this.search(api_key, search_string, null, null);
    }

    public Result search(String api_key, String search_string, int start_offset, int elements) {
        return this.search(api_key, search_string, Integer.getInteger(String.valueOf(start_offset)), Integer.getInteger(String.valueOf(elements)));
    }

    private Result search(String api_key, String search_string, Integer start_offset, Integer elements) {
        if (api_key == null || api_key.equals("")) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "API key cannot be null or empty String");
        }

        if (search_string == null || search_string.equals("")) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "String to be searched cannot be null or empty");
        }

        String body;
        if (start_offset == null && elements == null) {
            body = "{\"api_key\":\"" + api_key + "\", \"string\":\"" + search_string + "\"}";
        } else {
            JSONArray result_set = new JSONArray();
            result_set.put("start=" + start_offset.toString());
            result_set.put("rows=" + elements.toString());
            body = "{\"result_set\":" + result_set.toString() + ", \"api_key\":\"" + api_key + "\", \"string\":\"" + search_string + "\"}";
        }

        HttpResponse response;
        try {
            response = Unirest.post(Intel.URL_INTEL_SEARCH)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .asJson();
        } catch (UnirestException e) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_NETWORK_ERROR, "Error while connecting to Deepviz: " + e.getMessage());
        }

        JsonNode response_json;
        try {
            response_json = new JsonNode(response.getBody().toString());
        } catch (Exception e) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INTERNAL_ERROR, "Error loading Deepviz response");
        }

        if (response.getStatus() == 200) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_SUCCESS, response_json.getObject().get("data").toString());
        } else {
            String errMsg = response_json.getObject().get("errmsg").toString();
            DeepvizResultStatus code;
            if (response.getStatus() >= 500) {
                code = DeepvizResultStatus.DEEPVIZ_STATUS_SERVER_ERROR;
            } else {
                code = DeepvizResultStatus.DEEPVIZ_STATUS_CLIENT_ERROR;
            }
            return new Result(code, String.valueOf(response.getStatus()) + " - " + errMsg);
        }
    }

    public Result advancedSearch(String api_key, AdvancedSearchInput input) {
        if (api_key == null || api_key.equals("")) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "API key cannot be null or empty String");
        }

        JSONObject jsonInput = new JSONObject();

        jsonInput.put("api_key", api_key);

        if (input.getCreatedFiles() != null && ! input.getCreatedFiles().isEmpty()) {
            JSONArray jsonArray = new JSONArray();
            for (String s : input.getCreatedFiles()) {
                jsonArray.put(s);
            }
            jsonInput.put("created_files", jsonArray);
        }

        String neverSample = "false";
        if (input.isNeverSeen()) {
            neverSample = "true";
        }
        jsonInput.put("never_seen", neverSample);

        if (input.getResultSet() != null && ! input.getResultSet().isEmpty()) {
            JSONArray jsonArray = new JSONArray();
            for (String s : input.getResultSet()) {
                jsonArray.put(s);
            }
            jsonInput.put("result_set", jsonArray);
        }

        if (input.getSimHash() != null && ! input.getSimHash().isEmpty()) {
            JSONArray jsonArray = new JSONArray();
            for (String s : input.getSimHash()) {
                jsonArray.put(s);
            }
            jsonInput.put("sim_hash", jsonArray);
        }

        if (input.getImpHash() != null && ! input.getImpHash().isEmpty()) {
            JSONArray jsonArray = new JSONArray();
            for (String s : input.getImpHash()) {
                jsonArray.put(s);
            }
            jsonInput.put("imp_hash", jsonArray);
        }

        if (input.getStrings() != null && ! input.getStrings().isEmpty()) {
            JSONArray jsonArray = new JSONArray();
            for (String s : input.getStrings()) {
                jsonArray.put(s);
            }
            jsonInput.put("strings", jsonArray);
        }

        if (input.getCountry() != null && ! input.getCountry().isEmpty()) {
            JSONArray jsonArray = new JSONArray();
            for (String s : input.getCountry()) {
                jsonArray.put(s);
            }
            jsonInput.put("country", jsonArray);
        }

        if (input.getClassification() != null && ! input.getClassification().equals("")) {
            jsonInput.put("classification", input.getClassification());
        }

        if (input.getDomain() != null && ! input.getDomain().isEmpty()) {
            JSONArray jsonArray = new JSONArray();
            for (String s : input.getDomain()) {
                jsonArray.put(s);
            }
            jsonInput.put("domain", jsonArray);
        }

        if (input.getRules() != null && ! input.getRules().isEmpty()) {
            JSONArray jsonArray = new JSONArray();
            for (String s : input.getRules()) {
                jsonArray.put(s);
            }
            jsonInput.put("rules", jsonArray);
        }

        if (input.getTimeDelta() != null && ! input.getTimeDelta().equals("")) {
            jsonInput.put("time_delta", input.getTimeDelta());
        }

        if (input.getAsn() != null && ! input.getAsn().isEmpty()) {
            JSONArray jsonArray = new JSONArray();
            for (String s : input.getAsn()) {
                jsonArray.put(s);
            }
            jsonInput.put("asn", jsonArray);
        }

        if (input.getUrl() != null && ! input.getUrl().isEmpty()) {
            JSONArray jsonArray = new JSONArray();
            for (String s : input.getUrl()) {
                jsonArray.put(s);
            }
            jsonInput.put("url", jsonArray);
        }

        if (input.getIp() != null && ! input.getIp().isEmpty()) {
            JSONArray jsonArray = new JSONArray();
            for (String s : input.getIp()) {
                jsonArray.put(s);
            }
            jsonInput.put("ip", jsonArray);
        }

        if (input.getIpRange() != null && ! input.getIpRange().equals("")) {
            jsonInput.put("ip_range", input.getIpRange());
        }

        HttpResponse response;
        try {
            response = Unirest.post(Intel.URL_INTEL_SEARCH_ADVANCED)
                    .header("Content-Type", "application/json")
                    .body(jsonInput.toString())
                    .asJson();
        } catch (UnirestException e) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_NETWORK_ERROR, "Error while connecting to Deepviz: " + e.getMessage());
        }

        JsonNode response_json;
        try {
            response_json = new JsonNode(response.getBody().toString());
        } catch (Exception e) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INTERNAL_ERROR, "Error loading Deepviz response");
        }

        if (response.getStatus() == 200) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_SUCCESS, response_json.getObject().get("data").toString());
        } else {
            String errMsg = response_json.getObject().get("errmsg").toString();
            DeepvizResultStatus code;
            if (response.getStatus() >= 500) {
                code = DeepvizResultStatus.DEEPVIZ_STATUS_SERVER_ERROR;
            } else {
                code = DeepvizResultStatus.DEEPVIZ_STATUS_CLIENT_ERROR;
            }
            return new Result(code, String.valueOf(response.getStatus()) + " - " + errMsg);
        }
    }

    public Result sampleResult(String api_key, String md5) {
        List<String> filters = new ArrayList<String>();
        filters.add("classification");

        return this.sampleInfo(api_key, md5, filters);
    }

    public Result sampleInfo(String api_key, String md5, List<String> filters) {
        if (md5 == null || md5.equals("")) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INTERNAL_ERROR, "MD5 cannot be null or empty String");
        }

        if (api_key == null || api_key.equals("")) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "API key cannot be null or empty String");
        }

        String body;
        if (filters == null || filters.isEmpty()) {
            body = "{\"api_key\":\"" + api_key + "\", \"md5\":\"" + md5 + "\"}";
        } else {
            if (filters.size() > 10) {
                return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "Parameter 'output_filters' takes at most 10 values (" + String.valueOf(filters.size()) + " given).");
            }

            JSONArray json_filters = new JSONArray();
            for (String filter : filters) {
                json_filters.put(filter);
            }
            body = "{\"api_key\":\"" + api_key + "\", \"md5\":\"" + md5 + "\", \"output_filters\": " + json_filters.toString() + "}";
        }

        HttpResponse response;
        try {
            response = Unirest.post(Intel.URL_INTEL_DOWNLOAD_REPORT)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .asJson();
        } catch (UnirestException e) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_NETWORK_ERROR, "Error while connecting to Deepviz: " + e.getMessage());
        }

        if (response.getStatus() == 428) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_PROCESSING, "Analysis is running");
        } else {
            JsonNode response_json;
            try {
                response_json = new JsonNode(response.getBody().toString());
            } catch (Exception e) {
                return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INTERNAL_ERROR, "Error loading Deepviz response");
            }

            if (response.getStatus() == 200) {
                return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_SUCCESS, response_json.getObject().get("data").toString());
            } else {
                String errMsg = response_json.getObject().get("errmsg").toString();
                DeepvizResultStatus code;
                if (response.getStatus() >= 500) {
                    code = DeepvizResultStatus.DEEPVIZ_STATUS_SERVER_ERROR;
                } else {
                    code = DeepvizResultStatus.DEEPVIZ_STATUS_CLIENT_ERROR;
                }
                return new Result(code, String.valueOf(response.getStatus()) + " - " + errMsg);
            }
        }
    }
}
