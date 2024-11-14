package com.hohich.http.server;

import com.hohich.http.messages.*;

import java.net.*;
import java.io.*;

public class HttpServer {
    private final static String CONTENT_PATH = "D:\\Study\\Univer\\3_kurs\\AIPOS\\lab5\\http1-1_server\\hosted_files";

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(
                8080, 50, InetAddress.getByName("localhost"))) {
            //listening
            while (true) {
                try {
                    Socket client = server.accept();
                    //handling client
                    new Thread(() -> handleClient(client)).start();
                    System.out.println("Client connected: " + client.getInetAddress().getHostAddress());
                } catch (IOException e) {
                    System.out.println("Error accepting connection: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.out.println("Could not start server: " + e.getMessage());
        } finally {
            System.out.println("Server stopped.");
        }
    }

    private static void handleClient(Socket cs) {
        try (cs;
             InputStream in = cs.getInputStream();
             OutputStream out = cs.getOutputStream()) {
            // client request handling
            System.out.println("Handling client: " + cs.getInetAddress());

            byte[] buf = new byte[1024 * 64];
            int bytesRead = in.read(buf);

            if(bytesRead == -1){
                System.out.println("No data read");
                return;
            }

            String request = new String(buf, 0, bytesRead);
            HttpRequest req = new HttpRequest(request);

            switch (req.getMethod()) {
                case "GET":
                    handleGetRequest(req, out);
                    break;
                case "POST":
                    handlePostRequest(req, out);
                    break;
                case "OPTIONS":
                    handleOptionsRequest(out);
                    break;
                default:
                    HttpResponse resp = new HttpResponse(405, "Method Not Allowed","<html>\n" +
                            "<head><title>405 Method Not Allowed</title></head>\n" +
                            "<body>\n" +
                            "<h1>405 Method Not Allowed</h1>\n" +
                            "<p>The request method is not allowed for the specified resource.</p>\n" +
                            "</body>\n" +
                            "</html>");
                    out.write(resp.getResponse().getBytes());
                    out.flush();
                    break;
            }

        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        }
    }

    private static void handleGetRequest(HttpRequest req,
                                         OutputStream out) throws IOException {
        System.out.println("handling GET");
        String uriPath = parseUriToPath(req.getUri());
        File file = new File(CONTENT_PATH + uriPath);

        if (!file.exists()) {
            HttpResponse content_access_err = new HttpResponse(404, "Not Found",
                    "<html>\n" +
                            "<head><title>404 Not Found</title></head>\n" +
                            "<body>\n" +
                            "<h1>404 Not Found</h1>\n" +
                            "<p>The requested file was not found on this server.</p>\n" +
                            "</body>\n" +
                            "</html>");
            content_access_err.addHeader("Content-Type", "text/html");
            content_access_err.addHeader("Content-Length", String.valueOf(
                    content_access_err.getBody().length()));

            out.write(content_access_err.getResponse().getBytes());
            out.flush();
            return;
        }
        //define mime-type
        String contentType = defineContentType(uriPath);

        HttpResponse response = new HttpResponse(200, "OK", "");
        response.addHeader("Content-Type", contentType);
        response.addHeader("Content-Length", String.valueOf(file.length()));

        out.write(response.getResponse().getBytes());
        out.flush();

        //send request body
        try(FileInputStream fis = new FileInputStream(file)) {
            byte[] buf = new byte[8192];
            int bytesRead;
            while(-1 != (bytesRead = fis.read(buf))) {
                out.write(buf, 0, bytesRead);
            }
            out.flush();
        } catch(IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
            HttpResponse inErrResponse = new HttpResponse(500, "Internal Server Error",
                    "<html>\n" +
                    "<head><title>500 Internal Server Error</title></head>\n" +
                    "<body>\n" +
                    "<h1>500 Internal Server Error</h1>\n" +
                    "<p>Unable to get content</p>\n" +
                    "</body>\n" +
                    "</html>");

            out.write(inErrResponse.getResponse().getBytes());
            out.flush();
        }
    }

    private static void handlePostRequest(HttpRequest req, OutputStream out) throws IOException {
        System.out.println("handling POST");
        String postedContent = req.getBody();
        System.out.println("Received posted content: " + postedContent);

        String respBody = "{\"message\": \"Data received\"}";
        HttpResponse resp = new HttpResponse(200, "OK", respBody);
        resp.addHeader("Content-Type", "text/html");
        resp.addHeader("Content-Length", String.valueOf(respBody.length()));
        out.write(resp.getResponse().getBytes());
        out.flush();
    }

    private static void handleOptionsRequest(
            OutputStream out) throws IOException {
        System.out.println("handling OPTIONS");
        HttpResponse optionsResponse = new HttpResponse(200, "OK", "");
        optionsResponse.addHeader("Allow", "GET, POST, OPTIONS");
        optionsResponse.addHeader("Content-Length", "0");

        out.write(optionsResponse.getResponse().getBytes());
        out.flush();
    }

    private static String parseUriToPath(String uri) {
        if(uri.startsWith("http://")){
            int pathStartIndex = uri.indexOf("/", uri.indexOf("://") + 3);
            return uri.substring(pathStartIndex).replace("/", "\\");
        }
        return uri.replace("/", "\\");
    }

    private static String defineContentType(String uriPath){
        if (uriPath.endsWith(".html")) return "text/html";
        if (uriPath.endsWith(".css")) return "text/css";
        if (uriPath.endsWith(".js")) return "application/javascript";
        if (uriPath.endsWith(".png")) return "image/png";
        if (uriPath.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }

}