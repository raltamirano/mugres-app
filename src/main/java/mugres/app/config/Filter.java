package mugres.app.config;

import java.util.HashMap;
import java.util.Map;

public class Filter {
    private String filter;
    private Map<String, Object> args = new HashMap<>();

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public Map<String, Object> getArgs() {
        return args;
    }

    public void setArgs(Map<String, Object> args) {
        this.args = args;
    }
}
