package deepviz.intel;

import deepviz.intel.input.AdvancedSearchInput;
import deepviz.intel.input.DomainInfoInput;
import deepviz.utils.DeepvizResultStatus;
import deepviz.utils.Result;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import deepviz.intel.input.IpInfoInput;
import org.json.JSONArray;
import org.json.JSONObject;


public class Intel {

    public static final String URL_INTEL_SEARCH            = "https://api.deepviz.com/intel/search";
    public static final String URL_INTEL_IP                = "https://api.deepviz.com/intel/network/ip";
    public static final String URL_INTEL_DOMAIN            = "https://api.deepviz.com/intel/network/domain";
    public static final String URL_INTEL_SEARCH_ADVANCED   = "https://api.deepviz.com/intel/search/advanced";

    public Result ipInfo(String api_key, IpInfoInput input) {
        if (api_key == null || api_key.equals("")) {
                return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "API key cannot be null or empty String");
        }

        if ((input.getIps() == null || input.getIps().isEmpty()) && (input.getTimeDelta() == null || input.getTimeDelta().equals(""))) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "Parameters missing or invalid");
        }

        String strHistory = "false";
        if (input.isHistoryEnabled()) {
            strHistory = "true";
        }

        HttpResponse response;

        if (input.getIps() != null && ! input.getIps().isEmpty()) {
            try {
                JSONArray json_ips = new JSONArray();
                for (String filter : input.getIps()) {
                    json_ips.put(filter);
                }

                response = Unirest.post(Intel.URL_INTEL_IP)
                        .header("Content-Type", "application/json")
                        .body("{\"api_key\":\"" + api_key + "\", \"history\":\"" + strHistory + "\", \"ip\": " + json_ips.toString() + "}")
                        .asJson();
            } catch (UnirestException e) {
                return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_NETWORK_ERROR, "Error while connecting to Deepviz: " + e.getMessage());
            }
        } else if (input.getTimeDelta() != null) {
            try {
                response = Unirest.post(Intel.URL_INTEL_IP)
                        .header("Content-Type", "application/json")
                        .body("{\"api_key\":\"" + api_key + "\", \"history\":\"" + strHistory + "\", \"time_delta\": \"" + input.getTimeDelta() + "\"}")
                        .asJson();
            } catch (UnirestException e) {
                return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_NETWORK_ERROR, "Error while connecting to Deepviz: " + e.getMessage());
            }
        } else {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INTERNAL_ERROR, "Unexpected error");
        }

        if (response.getStatus() == 200) {
            JsonNode response_json = new JsonNode(response.getBody().toString());
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_SUCCESS, response_json.getObject().get("data").toString());
        } else {
            JsonNode response_json = new JsonNode(response.getBody().toString());
            String errMsg = response_json.getObject().get("errmsg").toString();
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INTERNAL_ERROR, String.valueOf(response.getStatus()) + " - " + errMsg);
        }
    }

    public Result domainInfo(String api_key, DomainInfoInput input) {
        if (api_key == null || api_key.equals("")) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "API key cannot be null");
        }

        HttpResponse response;
        if (input.getDomains() != null && ! input.getDomains().isEmpty()) {
            JSONArray json_domains = new JSONArray();
            for (String domain : input.getDomains()) {
                json_domains.put(domain);
            }

            try {
                String body;
                if (input.getFilters() == null || input.getFilters().isEmpty()) {
                    body = "{\"api_key\":\"" + api_key + "\", \"history\":\"" + input.isHistoryEnabled() + "\", \"domain\": " + json_domains.toString() + "}";
                } else {
                    JSONArray json_filters = new JSONArray();
                    for (String filter : input.getFilters()) {
                        json_filters.put(filter);
                    }

                    body = "{\"api_key\":\"" + api_key + "\", \"history\":\"" + input.isHistoryEnabled() + "\", \"domain\": " + json_domains.toString() + ", \"output_filters\": " + json_filters.toString() + "}";
                }

                response = Unirest.post(Intel.URL_INTEL_DOMAIN)
                        .header("Content-Type", "application/json")
                        .body(body)
                        .asJson();
            } catch (UnirestException e) {
                return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_NETWORK_ERROR, "Error while connecting to Deepviz: " + e.getMessage());
            }
        } else if (input.getTimeDelta() != null && ! input.getTimeDelta().equals("")) {
            try {
                String body;
                if (input.getFilters() == null || input.getFilters().isEmpty()) {
                    body = "{\"api_key\":\"" + api_key + "\", \"history\":\"" + input.isHistoryEnabled() + "\", \"time_delta\": \"" + input.getTimeDelta() + "\"}";
                } else {
                    JSONArray json_filters = new JSONArray();
                    for (String filter : input.getFilters()) {
                        json_filters.put(filter);
                    }

                    body = "{\"api_key\":\"" + api_key + "\", \"history\":\"" + input.isHistoryEnabled() + "\", \"time_delta\": \"" + input.getTimeDelta() + "\", \"output_filters\": " + json_filters.toString() + "}";
                }

                response = Unirest.post(Intel.URL_INTEL_DOMAIN)
                        .header("Content-Type", "application/json")
                        .body(body)
                        .asJson();
            } catch (UnirestException e) {
                return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_NETWORK_ERROR, "Error while connecting to Deepviz: " + e.getMessage());
            }
        } else {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "Parameters missing or invalid");
        }

        if (response.getStatus() == 200) {
            JsonNode response_json = new JsonNode(response.getBody().toString());
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_SUCCESS, response_json.getObject().get("data").toString());
        } else {
            JsonNode response_json = new JsonNode(response.getBody().toString());
            String errMsg = response_json.getObject().get("errmsg").toString();
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INTERNAL_ERROR, String.valueOf(response.getStatus()) + " - " + errMsg);
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

        if (response.getStatus() == 200) {
            JsonNode response_json = new JsonNode(response.getBody().toString());
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_SUCCESS, response_json.getObject().get("data").toString());
        } else {
            JsonNode response_json = new JsonNode(response.getBody().toString());
            String errMsg = response_json.getObject().get("errmsg").toString();
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INTERNAL_ERROR, String.valueOf(response.getStatus()) + " - " + errMsg);
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

        if (response.getStatus() == 200) {
            JsonNode response_json = new JsonNode(response.getBody().toString());
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_SUCCESS, response_json.getObject().get("data").toString());
        } else {
            JsonNode response_json = new JsonNode(response.getBody().toString());
            String errMsg = response_json.getObject().get("errmsg").toString();
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INTERNAL_ERROR, String.valueOf(response.getStatus()) + " - " + errMsg);
        }
    }
}
