package com.deepviz.intel.input;

import java.util.List;

public class IpInfoInput {
    private String ip;
    private List<String> filters;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }


    public List<String> getFilters() {
        return filters;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }
}
