package com.hohich.http.messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpResponse {
    private int code;
    private String status;
    private byte[] body;
    private final Map<String, String> headers;

    public HttpResponse(int code, String status, byte[] body) {
        this.code = code;
        this.status = status;
        this.body = body;
        this.headers = new HashMap<>();
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public byte[] getResponse() {
        ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
        StringBuilder responseHeaders = new StringBuilder();
        responseHeaders.append("HTTP/1.1 ").append(code).append(" ").append(status).append("\r\n");

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            responseHeaders.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }
        responseHeaders.append("\r\n");

        // Преобразуем заголовки в байты
        try {
            responseStream.write(responseHeaders.toString().getBytes(StandardCharsets.UTF_8));
            if (body != null) {
                responseStream.write(body);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseStream.toByteArray();
    }


    public int getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    public byte[] getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}

