package com.hohich.http.server;

import com.hohich.http.messages.*;

import java.net.*;
import java.io.*;

public class HttpServer{
    private final static String CONTENT_PATH = "D:\\Study\\Univer\\3_kurs\\AIPOS\\lab5\\http1-1_server\\hosted_files";

    public static void main(String[] args) {
        try(ServerSocket server = new ServerSocket(
                8080, 50, InetAddress.getByName("localhost"));) {
            //listening
            while(true){
                try{
                    Socket client = server.accept();
                    //handling client
                    new Thread(() -> handleClient(client)).start();
                    System.out.println("Client connected: " + client.getInetAddress().getHostAddress());
                } catch(IOException e){
                    System.out.println("Error accepting connection: " + e.getMessage());
                }
            }

        } catch(IOException e){
            System.out.println("Could not start server: " + e.getMessage());
        }finally{
            System.out.println("Server stopped.");
        }
    }

    private static void handleClient(Socket cs){
        try (cs;
            InputStream in = cs.getInputStream();
            OutputStream out = cs.getOutputStream();) {
            // client request handling
            System.out.println("Handling client: " + cs.getInetAddress());

            byte[] buf = new byte[1024*64];
            int bytesRead = in.read(buf);

            String request = new String(buf, 0, bytesRead);
            HttpRequest req = new HttpRequest(request);

            switch(req.getMethod()){
                case "GET":
                    handleGetRequest(req, cs, in, out);
                    break;
                case "POST":
                    // тут хз можно просто показывать html c содержимым тела запросы
                    break;
                case "OPTIONS":
                    //опции
                    break;
                default:
//                    HttpResponse resp = new HttpResponse(405, "Method Not Allowed","<html>\n" +
//                            "<head><title>405 Method Not Allowed</title></head>\n" +
//                            "<body>\n" +
//                            "<h1>405 Method Not Allowed</h1>\n" +
//                            "<p>The request method is not allowed for the specified resource.</p>\n" +
//                            "</body>\n" +
//                            "</html>");
//                    out.write(resp.getResponse().getBytes());
//                    out.flush();
//                    out.close();
//                    cs.close();
                    break;
            }

        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        }
    }

    private static void handleGetRequest(HttpRequest req, Socket cs,
                                         InputStream in, OutputStream out) throws IOException {
            String uriPath = parseUriToPath(req.getUri());
            File file = new File(CONTENT_PATH + uriPath);
            //файл не найден
            if(!file.exists()){
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

            StringBuilder contentBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    contentBuilder.append(line).append("\n");
                }
            } catch(IOException e){
                System.out.println("Error reading file: " + e.getMessage());
                HttpResponse internalErrResp = new HttpResponse(
                        500, "Internal Server Error", "<html>\n" +
                        "<head><title>500 Internal Server Error</title></head>\n" +
                        "<body>\n" +
                        "<h1>500 Internal Server Error</h1>\n" +
                        "<p>An error occurred while reading the requested file.</p>\n" +
                        "</body>\n" +
                        "</html>");
                internalErrResp.addHeader("Content-Type", "text/html");
                internalErrResp.addHeader("Content-Length", String.valueOf(
                        internalErrResp.getBody().length()));
                out.write(internalErrResp.getResponse().getBytes());
                out.flush();
                return;
            }

            String respBody = contentBuilder.toString();
            HttpResponse response = new HttpResponse(200, "OK", respBody);
            response.addHeader("Content-Type", "text/html");
            response.addHeader("Content-Length", String.valueOf(respBody.length()));

            out.write(response.getResponse().getBytes());
            out.flush();
    }

    private static void handlePostRequest(HttpRequest req, Socket cs,
                                          InputStream in, OutputStream out) throws IOException {

    }

    private static void handleOptionsRequest(HttpRequest req, Socket cs,
                                             InputStream in, OutputStream out){
        
    }

    private static String parseUriToPath(String uri){
        int ind1 = uri.indexOf("http://", 0);
        int ind2 = uri.indexOf("/", ind1);
        String subUri = uri.substring(0, ind2);
        return String.join("\\", uri.replace(subUri, "").split("/"));
    }

}