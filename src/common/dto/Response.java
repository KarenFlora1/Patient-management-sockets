package common.dto;

import java.io.Serializable;

public class Response implements Serializable {
    private boolean success;
    private String message;
    private Object payload;

    public Response(boolean success, String message, Object payload) {
        this.success = success;
        this.message = message;
        this.payload = payload;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Object getPayload() { return payload; }
}

