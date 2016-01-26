package deepviz.sandbox;


import java.nio.file.FileSystemException;
import java.io.*;

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
import org.apache.http.client.ClientProtocolException;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.HttpEntity;
import org.json.JSONArray;
import org.json.JSONObject;


public class Sandbox {
    public final String URL_UPLOAD_SAMPLE   = "https://api.deepviz.com/sandbox/submit";
    public final String URL_DOWNLOAD_REPORT = "https://api.deepviz.com/general/report";
    public final String URL_DOWNLOAD_SAMPLE = "https://api.deepviz.com/sandbox/sample";

    public Result uploadSample(String path, String api_key) throws FileNotFoundException, FileSystemException {
        File f = new File(path);
        if (! f.exists()) {
            throw new FileNotFoundException();
        }

        if (f.isDirectory()) {
            throw new FileSystemException("Parameters 'path' is a directory instead of a file");
        }

        CloseableHttpClient httpclient = HttpClients.createDefault();

        try {
            HttpPost httppost = new HttpPost(this.URL_UPLOAD_SAMPLE);
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
            }catch(ClientProtocolException cpe){
                cpe.printStackTrace();
            }catch(IOException ioe){
                ioe.printStackTrace();
            } finally {
                try {
                    if (response != null) {
                        response.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public void uploadFolder(String path, String api_key) throws FileNotFoundException, FileSystemException {
        File f = new File(path);
        if (! f.exists()) {
            throw new FileNotFoundException();
        }

        if (f.isDirectory()) {
            throw new FileSystemException("Parameters 'path' is a file instead of a directory");
        }

        File[] files = f.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                System.out.println(this.uploadSample(file.getPath(), api_key));
            }
        }
    }

    public Result downloadSample(String md5, String path, String api_key) throws IOException {
        File f = new File(path);
        if (f.exists() && f.isFile()) {
            throw new FileSystemException("Parameters 'path': file already exists");
        } else if (! f.exists()) {
            f.mkdirs();
        }

        HttpResponse response;
        try {
            response = Unirest.post(this.URL_DOWNLOAD_SAMPLE)
                    .header("Content-Type", "application/json")
                    .body("{\"api_key\":\"" + api_key + "\", \"md5\":\"" + md5.toLowerCase() + "\", }")
                    .asString();
        } catch (UnirestException e) {
            return new ResultError("Error while connecting to Deepviz");
        }

        if (response.getStatus() == 200) {
            InputStream in = response.getRawBody();
            String filePath = new File(path, md5).toString();
            OutputStream out = new FileOutputStream(filePath);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();

            return new ResultSuccess(String.valueOf(response.getStatus()), null);
        } else {
            JsonNode response_json = new JsonNode(response.getBody().toString());
            String errMsg = String.valueOf(response_json.getObject().get("errmsg"));
            return new ResultError(String.valueOf(response.getStatus()) + " - " + errMsg);
        }
    }

    public Result sampleResult(String md5, String api_key) {
        HttpResponse response;
        try {
            response = Unirest.post(this.URL_DOWNLOAD_REPORT)
                    .header("Content-Type", "application/json")
                    .body("{\"api_key\":\"" + api_key + "\", \"md5\":\"" + md5.toLowerCase() + "\", \"output_filters\": [\"classification\"]}")
                    .asJson();
        } catch (UnirestException e) {
            return new ResultError("Error while connecting to Deepviz");
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
        String body;
        if (filters == null || filters.length() == 0) {
            body = "{\"api_key\":\"" + api_key + "\", \"md5\":\"" + md5.toLowerCase() + "\"}";
        } else {
            body = "{\"api_key\":\"" + api_key + "\", \"md5\":\"" + md5.toLowerCase() + "\", \"output_filters\": " + filters.toString() + "}";
        }

        HttpResponse response;
        try {
            response = Unirest.post(this.URL_DOWNLOAD_REPORT)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .asJson();
        } catch (UnirestException e) {
            return new ResultError("Error while connecting to Deepviz");
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
