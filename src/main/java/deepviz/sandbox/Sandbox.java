package deepviz.sandbox;


import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;

import deepviz.ResultSuccess;
import deepviz.ResultError;
import deepviz.Result;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.HttpEntity;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.*;


public class Sandbox {
    public static final String URL_UPLOAD_SAMPLE   = "https://api.deepviz.com/sandbox/submit";
    public static final String URL_DOWNLOAD_REPORT = "https://api.deepviz.com/general/report";
    public static final String URL_DOWNLOAD_SAMPLE = "https://api.deepviz.com/sandbox/sample";
    public static final String URL_REQUEST_BULK    = "https://api.deepviz.com/sandbox/sample/bulk/request";
    public static final String URL_DOWNLOAD_BULK   = "https://api.deepviz.com/sandbox/sample/bulk/retrieve";

    public Result uploadSample(String path, String api_key) {
        if (path == null) {
            return new ResultError("Parameters 'path' cannot be null");
        }

        if (api_key == null) {
            return new ResultError("Parameters 'api_key' cannot be null");
        }

        File f = new File(path);
        if (! f.exists()) {
            return new ResultError("File does not exists");
        }

        if (f.isDirectory()) {
            return new ResultError("Parameters 'path' is a directory instead of a file");
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
                    String data = String.valueOf(body_json.getObject().get("data"));
                    return new ResultSuccess(String.valueOf(statusCode) + " - " + data, null);
                } else {
                    String errMsg = String.valueOf(body_json.getObject().get("errmsg"));
                    return new ResultError(String.valueOf(statusCode) + " - " + errMsg);
                }
            }catch(Exception e){
                return new ResultError("Error while connecting to Deepviz: " + e.getMessage());
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

    public Result uploadFolder(String path, String api_key) {
        if (path == null) {
            return new ResultError("Parameters 'path' cannot be null");
        }

        if (api_key == null) {
            return new ResultError("Parameters 'api_key' cannot be null");
        }

        File f = new File(path);
        if (f.isDirectory()) {
            return new ResultError("Parameters 'path' is a file instead of a directory");
        }

        if (! f.exists()) {
            return new ResultError("Folder does not exists");
        }

        File[] files = f.listFiles();
        if (files.length > 0) {
            for (File file : files) {
                if (file.isFile()) {
                    Result result = this.uploadSample(file.getPath(), api_key);
                    if (result.getStatus().equals("error")) {
                        result.setMsg("Unable to upload file '" + file.getPath() + "': " + result.getMsg());
                        return result;
                    }
                }
            }

            return new ResultSuccess("Every file in folder has been uploaded", null);
        } else {
            return new ResultError("Empty folder");
        }
    }

    public Result downloadSample(String md5, String path, String api_key) {
        if (md5 == null) {
            return new ResultError("Parameters 'md5' cannot be null");
        }

        if (path == null) {
            return new ResultError("Parameters 'path' cannot be null");
        }

        if (api_key == null) {
            return new ResultError("Parameters 'api_key' cannot be null");
        }

        File f = new File(path);
        if (f.exists() && f.isFile()) {
            return new ResultError("Parameters 'path': file already exists");
        } else if (! f.exists()) {
            f.mkdirs();
        }

        HttpResponse response;
        try {
            response = Unirest.post(Sandbox.URL_DOWNLOAD_SAMPLE)
                    .header("Content-Type", "application/json")
                    .body("{\"api_key\":\"" + api_key + "\", \"md5\":\"" + md5 + "\", }")
                    .asString();
        } catch (UnirestException e) {
            return new ResultError("Error while connecting to Deepviz: " + e.getMessage());
        }

        if (response.getStatus() == 200) {
            InputStream in = response.getRawBody();
            File file = new File(path, md5);
            try {
                file.createNewFile();
            } catch (Exception e) {
                return new ResultError("Cannot create file '" + file.getAbsolutePath() + "': " + e.getMessage());
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
                return new ResultError("Error writing file: " + e.getMessage());
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

            return new ResultSuccess(String.valueOf(response.getStatus()), null);
        } else {
            JsonNode response_json = new JsonNode(response.getBody().toString());
            String errMsg = String.valueOf(response_json.getObject().get("errmsg"));
            return new ResultError(String.valueOf(response.getStatus()) + " - " + errMsg);
        }
    }

    public Result sampleResult(String md5, String api_key) {
        if (md5 == null) {
            return new ResultError("Parameters 'md5' cannot be null");
        }

        if (api_key == null) {
            return new ResultError("Parameters 'api_key' cannot be null");
        }

        HttpResponse response;
        try {
            response = Unirest.post(Sandbox.URL_DOWNLOAD_REPORT)
                    .header("Content-Type", "application/json")
                    .body("{\"api_key\":\"" + api_key + "\", \"md5\":\"" + md5 + "\", \"output_filters\": [\"classification\"]}")
                    .asJson();
        } catch (UnirestException e) {
            return new ResultError("Error while connecting to Deepviz: " + e.getMessage());
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

    public Result sampleReport(String md5, String api_key, JSONArray filters) {
        if (md5 == null) {
            return new ResultError("Parameters 'md5' cannot be null");
        }

        if (api_key == null) {
            return new ResultError("Parameters 'api_key' cannot be null");
        }

        String body;
        if (filters == null || filters.length() == 0) {
            body = "{\"api_key\":\"" + api_key + "\", \"md5\":\"" + md5 + "\"}";
        } else {
            body = "{\"api_key\":\"" + api_key + "\", \"md5\":\"" + md5 + "\", \"output_filters\": " + filters.toString() + "}";
        }

        HttpResponse response;
        try {
            response = Unirest.post(Sandbox.URL_DOWNLOAD_REPORT)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .asJson();
        } catch (UnirestException e) {
            return new ResultError("Error while connecting to Deepviz: " + e.getMessage());
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

    public Result bulkDownloadRequest(JSONArray md5_list, String api_key) {
        if (api_key == null) {
            return new ResultError("Parameters 'api_key' cannot be null");
        }

        if (md5_list == null || md5_list.length() == 0) {
            return new ResultError("MD5 list empty or invalid.");
        }

        HttpResponse response;
        try {
            response = Unirest.post(Sandbox.URL_REQUEST_BULK)
                    .header("Content-Type", "application/json")
                    .body("{\"api_key\":\"" + api_key + "\", \"hashes\": " + md5_list.toString() + "}")
                    .asJson();
        } catch (UnirestException e) {
            return new ResultError("Error while connecting to Deepviz: " + e.getMessage());
        }

        if (response.getStatus() == 200) {
            JsonNode response_json = new JsonNode(response.getBody().toString());
            return new ResultSuccess("ID request: " + String.valueOf(response_json.getObject().get("id_request")), null);
        } else {
            JsonNode response_json = new JsonNode(response.getBody().toString());
            String errMsg = String.valueOf(response_json.getObject().get("errmsg"));
            return new ResultError(String.valueOf(response.getStatus()) + " - " + errMsg);
        }
    }

    public Result bulkDownloadRetrieve(String id_request, String path, String api_key) {
        if (id_request == null) {
            return new ResultError("Parameters 'id_request' cannot be null");
        }

        if (path == null) {
            return new ResultError("Parameters 'path' cannot be null");
        }

        if (api_key == null) {
            return new ResultError("Parameters 'api_key' cannot be null");
        }

        File f = new File(path);
        if (f.exists() && f.isFile()) {
            return new ResultError("Parameters 'path': file already exists");
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
            return new ResultError("Error while connecting to Deepviz: " + e.getMessage());
        }

        if (response.getStatus() == 200) {
            InputStream in = response.getRawBody();
            File file = new File(path, "bulk_download_" + id_request + ".zip");

            try {
                file.createNewFile();
            } catch (IOException e) {
                return new ResultError("Cannot create file '" + file.getAbsolutePath() + "': " + e.getMessage());
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
                return new ResultError("Error writing file: " + e.getMessage());
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

            return new ResultSuccess(String.valueOf(response.getStatus()), null);
        } else {
            JsonNode response_json = new JsonNode(response.getBody().toString());
            String errMsg = String.valueOf(response_json.getObject().get("errmsg"));
            return new ResultError(String.valueOf(response.getStatus()) + " - " + errMsg);
        }
    }
}