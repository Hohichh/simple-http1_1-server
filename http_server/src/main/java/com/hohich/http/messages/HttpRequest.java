package com.hohich.http.messages;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private String method;
    private String uri;
    private final Map<String, String> headers;
    private String body;

    public HttpRequest(String request){
        headers = new HashMap<String, String>();
        parse(request);
    }

    private void parse(String request){
        String[] lines = request.split("\r\n");
        //start line parsing to get method and uri to resourse
        String startLine = request.substring(0, request.indexOf("\r\n"));
        String[] tokens = startLine.split(" ");
        if(tokens.length == 3){
            method = tokens[0];
            uri = tokens[1];
        } else{
            throw new IllegalArgumentException("Invalid request line");
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

    public String getRequest() {
        StringBuilder response = new StringBuilder();
        response.append(method).append(" ").append(uri).append(" HTTP/1.1\r\n");

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            response.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }

        response.append("\r\n");

        if (body != null && !body.isEmpty()) {
            response.append(body);
        }

        return response.toString();
    }

    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }
}
