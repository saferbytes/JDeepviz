package deepviz.sandbox;


import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;

import deepviz.utils.DeepvizResultStatus;
import deepviz.utils.Result;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.HttpEntity;
import org.json.JSONArray;
import java.util.List;
import java.io.*;



public class Sandbox {
    public static final String URL_UPLOAD_SAMPLE   = "https://api.deepviz.com/sandbox/submit";
    public static final String URL_DOWNLOAD_REPORT = "https://api.deepviz.com/general/report";
    public static final String URL_DOWNLOAD_SAMPLE = "https://api.deepviz.com/sandbox/sample";
    public static final String URL_REQUEST_BULK    = "https://api.deepviz.com/sandbox/sample/bulk/request";
    public static final String URL_DOWNLOAD_BULK   = "https://api.deepviz.com/sandbox/sample/bulk/retrieve";

    public Result uploadSample(String api_key, String path) {
        if (path == null || path.equals("")) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "File path cannot be null or empty String");
        }

        if (api_key == null || api_key.equals("")) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "API key cannot be null or empty String");
        }

        File f = new File(path);
        if (! f.exists()) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "File does not exists");
        }

        if (f.isDirectory()) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "Path is a directory instead of a file");
        }

        if(! f.canRead()) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "Cannot open file '" + f.getAbsolutePath() + "'");
        }

        CloseableHttpClient httpclient = HttpClients.createDefault();

        try {
            HttpPost httppost = new HttpPost(Sandbox.URL_UPLOAD_SAMPLE);
            FileBody fileBody = new FileBody(f);
            StringBody apiKeyStringBody = new StringBody(api_key, ContentType.TEXT_PLAIN);
            StringBody sourceStringBody = new StringBody("j_deepviz", ContentType.TEXT_PLAIN);

            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addPart("api_key", apiKeyStringBody)
                    .addPart("source", sourceStringBody)
                    .addPart("file", fileBody)
                    .build();

            httppost.setEntity(reqEntity);

            CloseableHttpResponse response = null;

            try {
                response = httpclient.execute(httppost);

                int statusCode = response.getStatusLine().getStatusCode();
                StringBuilder str_buffer = new StringBuilder();

                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
                    String line;
                    while ((line = br.readLine()) != null) {
                        str_buffer.append(line);
                    }
                }

                JsonNode body_json = new JsonNode(str_buffer.toString());
                if (statusCode == 200) {
                    return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_SUCCESS, body_json.getObject().get("data").toString());
                } else {
                    String errMsg = body_json.getObject().get("errmsg").toString();
                    DeepvizResultStatus code;
                    if (statusCode >= 500) {
                        code = DeepvizResultStatus.DEEPVIZ_STATUS_SERVER_ERROR;
                    } else {
                        code = DeepvizResultStatus.DEEPVIZ_STATUS_CLIENT_ERROR;
                    }
                    return new Result(code, String.valueOf(statusCode) + " - " + errMsg);
                }
            }catch(IOException e){
                return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_NETWORK_ERROR, "Error while connecting to Deepviz: " + e.getMessage());
            } finally {
                try {
                    if (response != null) {
                        response.close();
                    }
                } catch (Exception e) {}
            }
        } finally {
            try {
                httpclient.close();
            } catch (Exception e) {}
        }
    }

    public Result uploadFolder(String api_key, String path) {
        if (path == null || path.equals("")) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "Folder path cannot be null or empty String");
        }

        if (api_key == null || api_key.equals("")) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "API key cannot be null or empty String");
        }

        File f = new File(path);
        if (! f.exists()) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "Directory does not exists");
        } else {
            if (! f.isDirectory()) {
                return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "Path is a file instead of a directory");
            }
        }

        File[] files = f.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.isFile()) {
                    Result result = this.uploadSample(file.getPath(), api_key);
                    if (result.getStatus() != DeepvizResultStatus.DEEPVIZ_STATUS_SUCCESS) {
                        result.setMsg("Unable to upload file '" + file.getPath() + "': " + result.getMsg());
                        return result;
                    }
                }
            }

            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_SUCCESS, "Every file in folder has been uploaded");
        } else {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "Empty folder");
        }
    }

    public Result downloadSample(String api_key, String md5, String path) {
        if (md5 == null || md5.equals("")) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "MD5 cannot be null or empty String");
        }

        if (path == null || path.equals("")) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "Destination path cannot be null or empty String");
        }

        if (api_key == null || api_key.equals("")) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "API key cannot be null or empty String");
        }

        File f = new File(path);
        if (f.exists() && f.isFile()) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "Invalid destination folder");
        } else if (! f.exists()) {
            f.mkdirs();
        }

        File file = new File(path, md5);
        if (! f.canWrite()) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "Cannot create file '" + file.getAbsolutePath() + "'");
        }

        HttpResponse response;
        try {
            response = Unirest.post(Sandbox.URL_DOWNLOAD_SAMPLE)
                    .header("Content-Type", "application/json")
                    .body("{\"api_key\":\"" + api_key + "\", \"md5\":\"" + md5 + "\"}")
                    .asString();
        } catch (UnirestException e) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_NETWORK_ERROR, "Error while connecting to Deepviz: " + e.getMessage());
        }

        if (response.getStatus() == 200) {
            InputStream in = response.getRawBody();
            try {
                file.createNewFile();
            } catch (Exception e) {
                return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INTERNAL_ERROR, "Cannot create file '" + file.getAbsolutePath() + "': " + e.getMessage());
            }

            OutputStream out = null;
            try {
                out = new FileOutputStream(file);
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } catch (IOException e) {
                return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INTERNAL_ERROR, "Error writing file: " + e.getMessage());
            } finally {
                try {
                    in.close();
                } catch (Exception e) {

                }
                try {
                    out.close();
                } catch (Exception e) {

                }
            }

            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_SUCCESS, "Sample downloaded to '" + file.getAbsolutePath() + "'");
        } else {
            JsonNode response_json = new JsonNode(response.getBody().toString());
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
        if (md5 == null || md5.equals("")) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "MD5 cannot be null or empty String");
        }

        if (api_key == null || api_key.equals("")) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "API key cannot be null or empty String");
        }

        HttpResponse response;
        try {
            response = Unirest.post(Sandbox.URL_DOWNLOAD_REPORT)
                    .header("Content-Type", "application/json")
                    .body("{\"api_key\":\"" + api_key + "\", \"md5\":\"" + md5 + "\", \"output_filters\": [\"classification\"]}")
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
            DeepvizResultStatus code;
            if (response.getStatus() >= 500) {
                code = DeepvizResultStatus.DEEPVIZ_STATUS_SERVER_ERROR;
            } else {
                code = DeepvizResultStatus.DEEPVIZ_STATUS_CLIENT_ERROR;
            }
            return new Result(code, String.valueOf(response.getStatus()) + " - " + errMsg);
        }
    }

    public Result sampleReport(String api_key, String md5) {
        return this.sampleReport(api_key, md5, null);
    }

    public Result sampleReport(String api_key, String md5, List<String> filters) {
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
            JSONArray json_filters = new JSONArray();
            for (String filter : filters) {
                json_filters.put(filter);
            }
            body = "{\"api_key\":\"" + api_key + "\", \"md5\":\"" + md5 + "\", \"output_filters\": " + json_filters.toString() + "}";
        }

        HttpResponse response;
        try {
            response = Unirest.post(Sandbox.URL_DOWNLOAD_REPORT)
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
            DeepvizResultStatus code;
            if (response.getStatus() >= 500) {
                code = DeepvizResultStatus.DEEPVIZ_STATUS_SERVER_ERROR;
            } else {
                code = DeepvizResultStatus.DEEPVIZ_STATUS_CLIENT_ERROR;
            }
            return new Result(code, String.valueOf(response.getStatus()) + " - " + errMsg);
        }
    }

    public Result bulkDownloadRequest(String api_key, List<String> md5_list) {
        if (api_key == null || api_key.equals("")) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "API key cannot be null or empty String");
        }

        if (md5_list == null || md5_list.isEmpty()) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "MD5 list empty or invalid");
        }

        HttpResponse response;
        try {
            JSONArray json_md5 = new JSONArray();
            for (String filter : md5_list) {
                json_md5.put(filter);
            }

            response = Unirest.post(Sandbox.URL_REQUEST_BULK)
                    .header("Content-Type", "application/json")
                    .body("{\"api_key\":\"" + api_key + "\", \"hashes\": " + json_md5.toString() + "}")
                    .asJson();
        } catch (UnirestException e) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_NETWORK_ERROR, "Error while connecting to Deepviz: " + e.getMessage());
        }

        if (response.getStatus() == 200) {
            JsonNode response_json = new JsonNode(response.getBody().toString());
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_SUCCESS, "ID request: " + response_json.getObject().get("id_request").toString());
        } else {
            JsonNode response_json = new JsonNode(response.getBody().toString());
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

    public Result bulkDownloadRetrieve(String api_key, String id_request, String path) {
        if (id_request == null || id_request.equals("")) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "Request ID cannot be null or empty String");
        }

        if (path == null || path.equals("")) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "Destination path cannot be null or empty String");
        }

        if (api_key == null || api_key.equals("")) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "API key cannot be null or empty String");
        }

        File f = new File(path);
        if (f.exists() && f.isFile()) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INPUT_ERROR, "Invalid destination folder");
        } else if (! f.exists()) {
            f.mkdirs();
        }

        HttpResponse response;
        try {
            response = Unirest.post(Sandbox.URL_DOWNLOAD_BULK)
                    .header("Content-Type", "application/json")
                    .body("{\"api_key\":\"" + api_key + "\", \"id_request\":\"" + id_request + "\"}")
                    .asString();
        } catch (UnirestException e) {
            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_NETWORK_ERROR, "Error while connecting to Deepviz: " + e.getMessage());
        }

        if (response.getStatus() == 200) {
            InputStream in = response.getRawBody();
            File file = new File(path, "bulk_download_" + id_request + ".zip");

            try {
                file.createNewFile();
            } catch (IOException e) {
                return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INTERNAL_ERROR, "Cannot create file '" + file.getAbsolutePath() + "': " + e.getMessage());
            }

            OutputStream out = null;
            try {
                out = new FileOutputStream(file);

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } catch (IOException e) {
                return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_INTERNAL_ERROR, "Error writing file: " + e.getMessage());
            } finally {
                try {
                    in.close();
                } catch (Exception e) {

                }
                try {
                    out.close();
                } catch (Exception e) {

                }
            }

            return new Result(DeepvizResultStatus.DEEPVIZ_STATUS_SUCCESS, "Archive downloaded");
        } else {
            JsonNode response_json = new JsonNode(response.getBody().toString());
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