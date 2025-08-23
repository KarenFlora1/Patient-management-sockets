package common.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Request implements Serializable {
    private String action;
    private Map<String, Object> data = new HashMap<>();

    public Request(String action) {
        this.action = action;
    }

    public String getAction() { return action; }
    public Map<String, Object> getData() { return data; }

    public void addParam(String key, Object value) {
        data.put(key, value);
    }

    public Object getParam(String key) {
        return data.get(key);
    }
}
