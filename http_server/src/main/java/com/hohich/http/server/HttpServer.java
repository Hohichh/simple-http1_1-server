package com.hohich.http.server;

import com.hohich.http.messages.*;

import java.net.*;
import java.io.*;
import java.util.logging.*;

public class HttpServer {
    private static final String CONTENT_PATH = "D:\\Study\\Univer\\3_kurs\\AIPOS\\lab5\\http1-1_server\\hosted_files";
    private static final Logger logger = Logger.getLogger(HttpServer.class.getName());

    public static void main(String[] args) {
        loggerConfigure();
        logger.info("Initializing server...");
        try (ServerSocket server = new ServerSocket(
                8080, 50, InetAddress.getByName("localhost"))) {

            logger.info("Server listening on port 8080");
            while (true) {
                try {
                    Socket client = server.accept();
                    new Thread(() -> handleClient(client)).start();
                    logger.info("Client connected:" + client.getInetAddress() + ":" + client.getPort());
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error while accepting client connection", e);
                }
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while listening on port 8080", e);
        } finally {
            logger.info("Shutting down server...");
        }
    }

    private static void handleClient(Socket cs) {
        try (cs;
             InputStream in = cs.getInputStream();
             OutputStream out = cs.getOutputStream()) {
            // client request handling
            logger.info("Handling client " + cs.getInetAddress() + ":" + cs.getPort());

            byte[] buf = new byte[1024 * 64];
            int bytesRead = in.read(buf);

            if(bytesRead == -1){
                logger.warning("No data read");
                return;
            }

            String request = new String(buf, 0, bytesRead);
            HttpRequest req = new HttpRequest(request);

            switch (req.getMethod()) {
                case "GET":
                    logger.finer("Handling GET request for " + req.getUri());
                    logger.finest("Client request: " + req.getRequest());
                    handleGetRequest(req, out);
                    break;
                case "POST":
                    logger.finer("Handling POST request");
                    logger.finest("Client request: " + req.getRequest());
                    handlePostRequest(req, out);
                    break;
                case "OPTIONS":
                    logger.finer("Handling POST request");
                    logger.finest("Client request: " + req.getRequest());
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
                    logger.warning("Unsupported HTTP method: " + req.getMethod());
                    logger.finest("Server response " + resp.getResponse());
                    break;
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while handling client connection: ", e);
        }
    }

    private static void handleGetRequest(HttpRequest req,
                                         OutputStream out) throws IOException {

        String uriPath = parseUriToPath(req.getUri());
        File file = new File(CONTENT_PATH + uriPath);

        if (!file.exists()) {
            logger.warning("File does not exist: " + file.getAbsolutePath());
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

            logger.finest("Server response: " + content_access_err.getResponse());
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
        logger.finest("Server response: " + response.getResponse());

        //send request body
        try(FileInputStream fis = new FileInputStream(file)) {
            byte[] buf = new byte[8192];
            int bytesRead;
            while(-1 != (bytesRead = fis.read(buf))) {
                out.write(buf, 0, bytesRead);
            }
            out.flush();
        } catch(IOException e) {

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
            logger.log(Level.SEVERE, "Error while reading file: " + file.getAbsolutePath(), e);
            logger.finest("Server response: " + inErrResponse.getResponse());
        }
    }

    private static void handlePostRequest(HttpRequest req, OutputStream out) throws IOException {

        String postedContent = req.getBody();

        String respBody = "{\"message\": \"Data received\"}";
        HttpResponse resp = new HttpResponse(200, "OK", respBody);
        resp.addHeader("Content-Type", "text/html");
        resp.addHeader("Content-Length", String.valueOf(respBody.length()));
        out.write(resp.getResponse().getBytes());
        out.flush();
        logger.finest("Server response: " + resp.getResponse());
    }

    private static void handleOptionsRequest(
            OutputStream out) throws IOException {

        HttpResponse optionsResponse = new HttpResponse(200, "OK", "");
        optionsResponse.addHeader("Allow", "GET, POST, OPTIONS");
        optionsResponse.addHeader("Content-Length", "0");

        out.write(optionsResponse.getResponse().getBytes());
        out.flush();
        logger.finest("Server response: " + optionsResponse.getResponse());
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


    private static void loggerConfigure(){
        try{
            logger.setLevel(Level.ALL);

            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.INFO);
            consoleHandler.setFormatter(new SimpleFormatter());

            FileHandler fileHandler = new FileHandler("app.log", true);
            fileHandler.setLevel(Level.FINEST);
            fileHandler.setFormatter(new SimpleFormatter());

            logger.addHandler(consoleHandler);
            logger.addHandler(fileHandler);
        } catch(IOException e){
            logger.log(Level.SEVERE, "Error initializing logger", e);
        }

    }

}