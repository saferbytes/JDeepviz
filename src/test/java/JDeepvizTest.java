import deepviz.Result;
import org.json.JSONArray;
import deepviz.intel.Intel;
import java.io.IOException;
import deepviz.sandbox.Sandbox;
import com.mashape.unirest.http.exceptions.UnirestException;


public class JDeepvizTest {

    private static final String API_KEY = "0000000000000000000000000000000000000000000000000000000000000000";

    public static void main(String [] args) throws IOException, UnirestException {
        Result result;

        Sandbox sbx = new Sandbox();

        // Download sample
        result = sbx.downloadSample("a6ca3b8c79e1b7e2a6ef046b0702aeb2", ".", JDeepvizTest.API_KEY);
        System.out.println(result);

        // Upload sample
        result = sbx.uploadSample("./a6ca3b8c79e1b7e2a6ef046b0702aeb2", JDeepvizTest.API_KEY);
        System.out.println(result);

        // Upload folder
        sbx.uploadFolder(".", JDeepvizTest.API_KEY);

        // Sample result
        result = sbx.sampleResult("a6ca3b8c79e1b7e2a6ef046b0702aeb2", JDeepvizTest.API_KEY);
        System.out.println(result);
        System.out.println(result.getData().get("classification"));

        // Sample report
        JSONArray filters = new JSONArray();
        filters.put("classification");
        System.out.println(filters.toString());
        result = sbx.sampleReport("a6ca3b8c79e1b7e2a6ef046b0702aeb2", JDeepvizTest.API_KEY, filters);
        System.out.println(result);
        System.out.println(result.getData());

        JSONArray md5_list = new JSONArray();
        md5_list.put("a6ca3b8c79e1b7e2a6ef046b0702aeb2");
        md5_list.put("34781d4f8654f9547cc205061221aea5");
        md5_list.put("a8c5c0d39753c97e1ffdfc6b17423dd6");

        result = sbx.bulkDownloadRequest(md5_list, JDeepvizTest.API_KEY);
        System.out.println(result);

        // Download bulk request
        result = sbx.bulkDownloadRetrieve("30", ".", JDeepvizTest.API_KEY);
        System.out.println(result);

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

        Intel intel = new Intel();

        result = intel.ipInfo(JDeepvizTest.API_KEY, "7d", false);
        System.out.println(result);

        JSONArray ip = new JSONArray();
        ip.put("8.8.8.8");
        result = intel.ipInfo(JDeepvizTest.API_KEY, ip, false);
        System.out.println(result);
    }

}
