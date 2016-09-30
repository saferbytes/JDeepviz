import com.deepviz.intel.input.AdvancedSearchInput;
import com.deepviz.intel.input.DomainInfoInput;
import com.deepviz.utils.DeepvizResultStatus;
import com.deepviz.intel.input.IpInfoInput;
import com.mashape.unirest.http.JsonNode;
import com.deepviz.sandbox.Sandbox;
import com.deepviz.utils.Result;
import com.deepviz.intel.Intel;

import java.util.ArrayList;
import java.util.List;


public class JDeepvizTest {

    private static final String API_KEY = "804268c4e9c22da6f0b4188dc101b439ba04fc4b265419c8321acf5cb52f5e88";

    public static void main(String [] args) {
        Result result;

        Sandbox sbx = new Sandbox();

        // Download sample
        result = sbx.downloadSample(JDeepvizTest.API_KEY, "a6ca3b8c79e1b7e2a6ef046b0702aeb2", ".");
        System.out.println(result);

        // Upload sample
        result = sbx.uploadSample(JDeepvizTest.API_KEY, "./a6ca3b8c79e1b7e2a6ef046b0702aeb2");
        System.out.println(result);

        // Upload folder
        result = sbx.uploadFolder(JDeepvizTest.API_KEY, "folder_to_upload");
        System.out.println(result);

        // Sample report
        result = sbx.sampleReport(JDeepvizTest.API_KEY, "a6ca3b8c79e1b7e2a6ef046b0702aeb2");
        System.out.println(result);

        // Send bulk request and retrieve the archive
        List<String> md5_list = new ArrayList<String>();
        md5_list.add("a6ca3b8c79e1b7e2a6ef046b0702aeb2");
        md5_list.add("34781d4f8654f9547cc205061221aea5");
        md5_list.add("a8c5c0d39753c97e1ffdfc6b17423dd6");
        result = sbx.bulkDownloadRequest(JDeepvizTest.API_KEY, md5_list);
        System.out.println(result);

        JsonNode message_json = new JsonNode(result.getMsg());
        do {
            result = sbx.bulkDownloadRetrieve(JDeepvizTest.API_KEY, String.valueOf(message_json.getObject().get("id_request")), ".");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (result.getStatus() == DeepvizResultStatus.DEEPVIZ_STATUS_PROCESSING);
        System.out.println(result);

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

        Intel intel = new Intel();
        IpInfoInput ipInfoInput = new IpInfoInput();
        DomainInfoInput domainInfoinput = new DomainInfoInput();

        // Sample result
        result = intel.sampleResult(JDeepvizTest.API_KEY, "a6ca3b8c79e1b7e2a6ef046b0702aeb2");
        System.out.println(result);

        // Sample info
        List<String> filters = new ArrayList<String>();
        filters.add("info");
        filters.add("hash");
        filters.add("classification");
        result = intel.sampleInfo(JDeepvizTest.API_KEY, "a6ca3b8c79e1b7e2a6ef046b0702aeb2", filters);
        System.out.println(result);

        // To retrieve intel data about one or more IPs:
        ipInfoInput.setIp("8.8.8.8");
        result = intel.ipInfo(JDeepvizTest.API_KEY, ipInfoInput);
        System.out.println(result);

        // To retrieve intel data about IPs contacted in the last 7 days:
        ipInfoInput = new IpInfoInput();
        ipInfoInput.setIp("8.8.8.8");
        filters = new ArrayList<String>();
        filters.add("generic_info");
        ipInfoInput.setFilters(filters);
        result = intel.ipInfo(JDeepvizTest.API_KEY, ipInfoInput);
        System.out.println(result);

        // To retrieve intel data about one or more domains:
        domainInfoinput.setDomain("google.com");
        result = intel.domainInfo(JDeepvizTest.API_KEY, domainInfoinput);
        System.out.println(result);

        //To retrieve newly registered domains in the last 7 days:
        domainInfoinput = new DomainInfoInput();
        domainInfoinput.setDomain("google.com");
        filters = new ArrayList<String>();
        filters.add("generic_info");
        domainInfoinput.setFilters(filters);
        result = intel.domainInfo(JDeepvizTest.API_KEY, domainInfoinput);
        System.out.println(result);

        // To run generic search based on strings
        // (find all IPs, domains, samples related to the searched keyword):
        //result = intel.search(JDeepvizTest.API_KEY, "test", 0, 2);
        result = intel.search(JDeepvizTest.API_KEY, "test");
        System.out.println(result);

        // To run advanced search based on parameters
        // (find all MD5 samples connecting to a domain and determined as malicious):
        List<String> domains2 = new ArrayList<String>();
        domains2.add("justfacebook.net");
        AdvancedSearchInput input = new AdvancedSearchInput();
        input.setDomain(domains2);
        input.setClassification("M");
        result = intel.advancedSearch(JDeepvizTest.API_KEY, input);
        System.out.println(result);
    }
}