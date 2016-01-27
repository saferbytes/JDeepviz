package deepviz.intel.input;

import java.util.List;


public class DomainInfoInput {
    private java.util.List<String> domains;
    private boolean history = false;
    private List<String> filters;
    private String timeDelta;

    public List<String> getDomains() {
        return domains;
    }

    public void setDomains(List<String> domains) {
        this.domains = domains;
    }

    public List<String> getFilters() {
        return filters;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }

    public String getTimeDelta() {
        return timeDelta;
    }

    public void setTimeDelta(String timeDelta) {
        this.timeDelta = timeDelta;
    }

    public boolean isHistoryEnabled() {
        return history;
    }

    public void setHistory(boolean history) {
        this.history = history;
    }
}
