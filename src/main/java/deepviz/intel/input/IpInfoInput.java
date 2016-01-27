package deepviz.intel.input;


import java.security.InvalidParameterException;
import java.util.List;

public class IpInfoInput {
    private boolean history = false;
    private List<String> ips;
    private String timeDelta;

    public List<String> getIps() {
        return ips;
    }

    public void setIps(List<String> ips) {
        if (this.timeDelta == null) {
            this.ips = ips;
        } else {
            throw new InvalidParameterException("You cannot specify TimeDelta and a list of IPs at the same time");
        }
    }

    public String getTimeDelta() {
        return timeDelta;
    }

    public void setTimeDelta(String timeDelta) {
        if (this.ips == null) {
            this.timeDelta = timeDelta;
        } else {
            throw new InvalidParameterException("You cannot specify TimeDelta and a list of IPs at the same time");
        }
    }

    public boolean isHistoryEnabled() {
        return history;
    }

    public void setHistory(boolean history) {
        this.history = history;
    }
}
