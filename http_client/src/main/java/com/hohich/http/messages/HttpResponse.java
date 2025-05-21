package com.hohich.http.messages;

import java.util.*;

public class HttpResponse {
    private int code;
    private String status;
    private String body;
    private final Map<String, String> headers;

    public HttpResponse(int code, String status, String body) {
        this.code = code;
        this.status = status;
        this.body = body;
        this.headers = new HashMap<>();
    }

    public HttpResponse(String response){
        this.headers = new HashMap<>();
        parse(response);
    }

    private void parse(String response){
        String[] lines = response.split("\r\n");
        //start line parsing to get status and code to resourse
        String startLine = response.substring(0, response.indexOf("\r\n"));
        String[] tokens = startLine.split(" ");
        if(tokens.length >= 3){
            status = startLine.substring(tokens[0].length() + tokens[1].length() + 1).trim();
            code = Integer.parseInt(tokens[1]);
        } else{
            throw new IllegalArgumentException("Invalid response line");
        }
        //headers parsing
        StringBuilder bodyBuilder = new StringBuilder();
        boolean headersEnd = false;
        for(int i = 1; i < lines.length; i++){
            if(lines[i].isEmpty()){
                headersEnd = true;
            }
            if(headersEnd){
                bodyBuilder.append(lines[i]);
            } else {
                String headerName = lines[i].split(": ")[0];
                String headerValue = lines[i].split(": ")[1];
                headers.put(headerName, headerValue);
            }
        }
        //body parsing
        body = bodyBuilder.toString().trim();
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public String getResponse() {
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 ").append(code).append(" ").append(status).append("\r\n");

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

