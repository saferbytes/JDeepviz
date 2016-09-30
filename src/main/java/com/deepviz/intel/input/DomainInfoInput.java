package com.deepviz.intel.input;

import java.util.List;


public class DomainInfoInput {
    private String domain;
    private List<String> filters;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public List<String> getFilters() {
        return filters;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }
}
