# JDeepviz
JDeepviz is a Java wrapper for deepviz.com REST APIs

# Install

JDeepviz is hosted by Maven central repository

```xml
<dependencies>
    <dependency>
        <groupId>com.mashape.unirest</groupId>
        <artifactId>unirest-java</artifactId>
        <version>1.4.7</version>
    </dependency>
</dependencies>
```

# Usage
To use Deepviz API sdk you will need an API key you can get by
subscribing the service free at https://account.deepviz.com/register/

# Sandbox SDK API

To upload a sample:

```java
import deepviz.Result;
import deepviz.sandbox.Sandbox;
...
Sandbox sbx = new Sandbox();
Result result = sbx.uploadSample("path\\to\\file.exe", "my-api-key")
System.out.println(result);
```

To upload a folder:

```java
import deepviz.sandbox.Sandbox;
...
Sandbox sbx = new Sandbox();
sbx.uploadFolder("path\\to\\file.exe", "my-api-key")
```

To download a sample:

```java
import deepviz.Result;
import deepviz.sandbox.Sandbox;
...
Sandbox sbx = new Sandbox();
Result result = sbx.downloadSample("md5-to-download", "dest-path", "my-api-key")
System.out.println(result);
```

To download the archive af a bulk download request:

```java
import deepviz.Result;
import deepviz.sandbox.Sandbox;
...
Sandbox sbx = new Sandbox();
Result result = sbx.bulkDownloadRetrieve("id-request", "dest-path", "my-api-key");
System.out.println(result);
```

To send a bulk download request:

```java
import deepviz.Result;
import deepviz.sandbox.Sandbox;
...
JSONArray md5_list = new JSONArray();
md5_list.put("a6ca3b8c79e1b7e2a6ef046b0702aeb2");
md5_list.put("34781d4f8654f9547cc205061221aea5");
md5_list.put("a8c5c0d39753c97e1ffdfc6b17423dd6");

Result result =  sbx.bulkDownloadRequest(md5_list, "my-api-key")
System.out.println(result);
```

To retrieve scan result of a specific MD5

```java
import deepviz.Result;
import deepviz.sandbox.Sandbox;
...
Result result = sbx.sampleResult("MD5-hash", "my-api-key");
System.out.print('STATUS: ');
System.out.println(result.getData().get("classification").get("result");
System.out.print('ACCURACY: ');
System.out.println(result.getData().get("classification").get("accuracy");
```

To retrieve full scan report for a specific MD5

```java
import deepviz.Result;
import org.json.JSONArray;
import deepviz.sandbox.Sandbox;
...
JSONArray filters = null;
Result result = sbx.sampleReport("MD5-hash", "my-api-key", filters);
System.out.println(result.getData());
```

To retrieve only specific parts of the report of a specific MD5 scan

```java
import deepviz.Result;
import org.json.JSONArray;
import deepviz.sandbox.Sandbox;
...
JSONArray filters = new JSONArray();
filters.put("classification");
filters.put("rules");
Result result = sbx.sampleReport("MD5-hash", "my-api-key", filters);

# List of the optional filters - they can be combined together
# "network_ip",
# "network_ip_tcp",
# "network_ip_udp",
# "rules",
# "classification",
# "created_process",
# "hook_user_mode",
# "strings",
# "created_files",
# "hash",
# "info",
# "code_injection"

System.out.println(result.getData());
```

# Threat Intelligence SDK API

To retrieve intel data about one or more IPs:

```java
import deepviz.Result;
import org.json.JSONArray;
import deepviz.intel.Intel;
...
Intel intel = new Intel();
JSONArray ip_list = new JSONArray();
ip_list.put("8.8.8.8");
result = intel.ipInfo("my-api-key", ip_list, false);
System.out.println(result);
```

To retrieve intel data about IPs contacted in the last 7 days:

```java
import deepviz.Result;
import deepviz.intel.Intel;
...
Intel intel = new Intel();
result = intel.ipInfo("my-api-key", "7d", false);
System.out.println(result);
```