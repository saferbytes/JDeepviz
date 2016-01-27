package deepviz.intel.input;

import java.util.List;


public class AdvancedSearchInput {
    private List<String> createdFiles;
    private boolean neverSeen = true;
    private List<String> resultSet;
    private String classification;
    private List<String> simHash;
    private List<String> impHash;
    private List<String> strings;
    private List<String> country;
    private List<String> domain;
    private List<String> rules;
    private String timeDelta;
    private List<String> asn;
    private List<String> url;
    private List<String> ip;
    private String ipRange;

    public List<String> getCreatedFiles() {
        return createdFiles;
    }

    public void setCreatedFiles(List<String> createdFiles) {
        this.createdFiles = createdFiles;
    }

    public List<String> getResultSet() {
        return resultSet;
    }

    public void setResultSet(List<String> resultSet) {
        this.resultSet = resultSet;
    }

    public List<String> getSimHash() {
        return simHash;
    }

    public void setSimHash(List<String> simHash) {
        this.simHash = simHash;
    }

    public List<String> getImpHash() {
        return impHash;
    }

    public void setImpHash(List<String> impHash) {
        this.impHash = impHash;
    }

    public List<String> getStrings() {
        return strings;
    }

    public void setStrings(List<String> strings) {
        this.strings = strings;
    }

    public List<String> getCountry() {
        return country;
    }

    public void setCountry(List<String> country) {
        this.country = country;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public List<String> getDomain() {
        return domain;
    }

    public void setDomain(List<String> domain) {
        this.domain = domain;
    }

    public boolean isNeverSeen() {
        return neverSeen;
    }

    public void setNeverSeen(boolean neverSeen) {
        this.neverSeen = neverSeen;
    }

    public List<String> getRules() {
        return rules;
    }

    public void setRules(List<String> rules) {
        this.rules = rules;
    }

    public String getTimeDelta() {
        return timeDelta;
    }

    public void setTimeDelta(String timeDelta) {
        this.timeDelta = timeDelta;
    }

    public List<String> getAsn() {
        return asn;
    }

    public void setAsn(List<String> asn) {
        this.asn = asn;
    }

    public List<String> getUrl() {
        return url;
    }

    public void setUrl(List<String> url) {
        this.url = url;
    }

    public List<String> getIp() {
        return ip;
    }

    public void setIp(List<String> ip) {
        this.ip = ip;
    }

    public String getIpRange() {
        return ipRange;
    }

    public void setIpRange(String ipRange) {
        this.ipRange = ipRange;
    }
}
