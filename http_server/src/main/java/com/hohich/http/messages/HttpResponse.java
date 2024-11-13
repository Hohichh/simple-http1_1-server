package com.hohich.http.messages;

import java.util.*;

public class HttpResponse {
    private int code;
    private String status;
    private String body;
    private Map<String, String> headers;

    public HttpResponse(int code, String status, String body) {
        this.code = code;
        this.status = status;
        this.body = body;
        this.headers = new HashMap<>();
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public String getResponse() {
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 ").append(code).append(" ").append(status).append("\r\n")
                .append("Content-Type: text/html\r\n");

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            response.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }

        response.append("\r\n");

        if (body != null && !body.isEmpty()) {
            response.append(body);
        }

        return response.toString();
    }

    public int getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}

