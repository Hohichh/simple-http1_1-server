package com.hohich.http.client;

import com.hohich.http.messages.*;

import java.io.*;
import java.net.*;

public class HttpClient {
    public static void main(String[] args) {
        boolean isValidInput = inputCheck(args);
        if (!isValidInput) return;

        if(args[0].equals("--help") || args[0].equals("--h")){
            showAllCommands();
            return;
        }

        HttpRequest request = handleRequest(args);
        if (request == null) return;

        String hostname = "localhost";
        int port = 8080;

        try (Socket sc = new Socket(hostname, port);
             InputStream in = sc.getInputStream();
             OutputStream out = sc.getOutputStream()) {

            System.out.println(request.getRequest() + "\n");
            out.write(request.getRequest().getBytes());
            out.flush();

            ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int bytesRead;

            while((bytesRead = in.read(buf)) != -1) {
                responseBuffer.write(buf, 0, bytesRead);
            }

            byte[] responseBytes = responseBuffer.toByteArray();

            String respHeaders = new String(responseBytes);
            int headersEndInd = respHeaders.indexOf("\r\n\r\n");

            if(headersEndInd == -1) {
                System.out.println("body consists no content");
                return;
            }

            respHeaders = respHeaders.substring(0, headersEndInd);
            byte[] body = new byte[responseBytes.length - (headersEndInd + 4)];
            System.arraycopy(responseBytes, headersEndInd + 4, body, 0, body.length);

            System.out.println(respHeaders);

            String contentType = null;
            for(String line : respHeaders.split("\r\n")) {
                if(line.toLowerCase().startsWith("content-type:")) {
                    contentType = line.substring(line.indexOf(":") + 1).trim();
                    break;
                }
            }

            if(contentType == null){
                System.out.println("Body consists no content");
                return;
            }

            String fileExtension = defineFileExtension(contentType);

            String fileName = "resp" + fileExtension;
            try(FileOutputStream fos = new FileOutputStream(fileName)){
                fos.write(body);
            } catch(IOException e){
                System.out.printf(e.getMessage());
            }

            System.out.println("Response body saved to: " + fileName);

        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }

    public static HttpRequest configFileHandling(String configFile) {
        String configRequestStr = fileReadHandling(configFile);
        if (configRequestStr == null) return null;
        return new HttpRequest(configRequestStr);
    }

    public static HttpRequest consoleHandling(String[] args) {
        String method = "";
        String headers = "";
        String body = "";
        String uri = "";

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-X":
                    if (i + 1 < args.length && !args[i + 1].startsWith("-")) method = args[++i];
                    else {
                        System.out.println("Error: Missing value for -X flag.");
                        return null;
                    }
                    break;
                case "-H":
                    if (i + 1 < args.length && !args[i + 1].startsWith("-")) headers += args[++i] + "\r\n";
                    else {
                    System.out.println("Error: Missing value for -H flag.");
                    return null;
                }
                    break;
                case "-d":
                    if (i + 1 < args.length && !args[i + 1].startsWith("-")) body = args[++i];
                    else {
                        System.out.println("Error: Missing value for -d flag.");
                        return null;
                    }
                    break;
                case "-F":
                    if (i + 1 < args.length && !args[i + 1].startsWith("-")) body = fileReadHandling(args[++i]);
                    else {
                        System.out.println("Error: Missing value for -F flag.");
                        return null;
                    }
                default:
                    if (!args[i].startsWith("-")) {
                        uri = args[i];
                    } else{
                        System.out.println("Syntax error: unknown flag " + args[i]);
                        return null;
                    }
                    break;
            }
        }

        if (uri.isEmpty() && method.equals("GET")) {
            System.out.println("Error: URI is required.");
            return null;
        }

        StringBuilder requestStr = new StringBuilder();
        requestStr.append(method).append(" ").append(uri).append(" HTTP/1.1\r\n"); // Start line
        if (!headers.isEmpty()) {
            requestStr.append(headers); // Headers
        }
        requestStr.append("\r\n");
        assert body != null;
        if (!body.isEmpty()) {
            requestStr.append(body);
        }

        try{
            return new HttpRequest(requestStr.toString());
        }catch(Exception e){
            System.out.println("Bad request: " + e.getMessage());
            return null;
        }
    }

    private static String fileReadHandling(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            System.out.println("Error: Missing file name.");
            return null;
        }

        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("File does not exist: " + fileName);
            return null;
        }

        String data = null;
        try(FileInputStream fis = new FileInputStream(file)){
            byte[] buf = fis.readAllBytes();
            data = new String(buf, "UTF-8");
        }catch(IOException e){
            System.out.println("Error reading config file: " + e);
        }

        return data;
    }

    private static String defineFileExtension(String mime){
        if(mime.substring(mime.indexOf("/")+1).equals("html")) return ".html";
        if(mime.substring(mime.indexOf("/")+1).equals("png")) return ".png";
        if(mime.substring(mime.indexOf("/")+1).equals("jpg")) return ".jpg";
        if(mime.substring(mime.indexOf("/")+1).equals("gif")) return ".gif";
        if(mime.substring(mime.indexOf("/")+1).equals("svg+xml")) return ".svg";
        if(mime.substring(mime.indexOf("/")+1).equals("css")) return ".css";
        if(mime.substring(mime.indexOf("/")+1).equals("javascript")) return ".js";
        return ".";
    }

    private static boolean inputCheck(String[] args){
        if(args.length < 1) {
            System.out.println("No args has provided");
            return false;
        }
        boolean consoleHandling = false;
        boolean fileHandling = false;
        for (int i = 0; i < args.length; i++) {
            if(args[i].startsWith("-") && args[i].charAt(1) != '-') consoleHandling = true;
            if(args[i].equals("--config") || args[i].equals("--c")) {
                fileHandling = true;

                if (i + 1 >= args.length || args[i + 1].startsWith("-")) {
                    System.out.println("Syntax error: --config requires a file path.");
                    return false;
                }
            }
            if(consoleHandling && fileHandling){
                System.out.println("Syntax error: use either --config or other flags");
                return false;
            }
            if (args[i].startsWith("--") && !(args[i].equals("--config") || args[i].equals("--help")
                    || args[i].equals("--h") || args[i].equals("--c"))) {
                System.out.println("Syntax error: unknown flag " + args[i]);
                return false;
            }
        }

        return true;
    }

    private static HttpRequest handleRequest(String[] args){
        HttpRequest request = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--config") || args[i].equals("--c")) {
                request = configFileHandling(args[i+1]);
                return request;
            }
        }
        request = consoleHandling(args);

        return request;
    }

    private static void showAllCommands() {
        System.out.println("Command list:");
        System.out.println();

        System.out.println("--help             Display this help message.");
        System.out.println("--config <file>    Use a configuration file for the request.");
        System.out.println();

        System.out.println("-X <method>        Specify the HTTP method (e.g., GET, POST).");
        System.out.println("-H <header>        Add a request header (e.g., -H \"Content-Type: application/json\").");
        System.out.println("-d <data>          Provide data for the request body (e.g., -d \"key=value\").");
        System.out.println("-F <file>          Read data for the request body from a file.");
        System.out.println();

        System.out.println("Examples:");
        System.out.println("  1. Using a configuration file:");
        System.out.println("     java .\\client\\HttpClient --config .\\configs\\request.txt");
        System.out.println();
        System.out.println("  2. Manual request setup:");
        System.out.println("     java .\\client\\HttpClient -X POST -H \"Content-Type: application/json\" -d \"{\\\"name\\\":\\\"John\\\"}\"");
        System.out.println();
        System.out.println("  3. Reading body data from a file:");
        System.out.println("     java .\\client\\HttpClient -X POST -H \"Content-Type: application/json\" -F body.json");
        System.out.println();
    }


}