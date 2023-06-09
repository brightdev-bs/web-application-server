package webserver;

import java.io.*;
import java.net.HttpCookie;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
//        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
//                connection.getPort());
//
//        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
//
//            HttpRequest request = new HttpRequest(in);
//            String url = request.getPath();
//
////            DataOutputStream dos = new DataOutputStream(out);
//
//            HttpResponse response = new HttpResponse(out);
//            if(url.startsWith("/user/create")) {
//                Map<String, String> params = getRequestBody(
//                        new BufferedReader(new InputStreamReader(request.getInputStream())),
//                        Integer.parseInt(request.getHeader("Content-Length"))
//                );
//
//                User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
//                log.debug("User = {}", user);
//
//                DataBase.addUser(user);
//
//                response.sendRedirect("/index.html");
//            } else if(url.equals("/user/login")) {
//                Map<String, String> params = getRequestBody(
//                        new BufferedReader(new InputStreamReader(request.getInputStream())),
//                        Integer.parseInt(request.getHeader("Content-Length"))
//                );
//
//                User user = DataBase.findUserById(params.get("userId"));
//                if(user == null) {
//                    responseResource(out, "/user/login_failed.html");
//                    return;
//                }
//
//                if(user.getPassword().equals(params.get("password"))) {
//                    log.debug("사용자 로그인 성공 : {}", user.getUserId());
//                    response302LoginSuccessHeader(dos);
//                } else {
//                    responseResource(dos, "/user/login_failed.html");
//                }
//            } else if(url.equals("/user/list")) {
//
//                if(!request.getHeader("Cookie").equals(false)) {
//                    responseResource(out, "user/login.html");
//                    return;
//                }
//                else {
//                    StringBuilder sb = new StringBuilder();
//                    sb.append("<li>");
//                    for (User user : DataBase.findAll()) {
//                        log.debug("user : {}", user.getEmail());
//                        sb.append("     <ul>" + user.getEmail() + "</ul>");
//                    }
//                    sb.append("</li>");
//                    byte[] body = sb.toString().getBytes();
//                    response200Header(dos, body.length);
//                    responseBody(dos, body);
//                }
//            } else if(url.endsWith(".css")) {
//                byte[] body = getAllBytes(url);
//                response200HeaderForCSS(dos, body.length);
//                responseBody(dos, body);
//            } else {
//                byte[] body = getAllBytes(url);
//                response200Header(dos, body.length);
//                responseBody(dos, body);
//            }
//        } catch (IOException e) {
//            log.error(e.getMessage());
//        }
    }
    private Map<String, String> getRequestBody(BufferedReader br, int contentLength) throws IOException {
        String queryString = IOUtils.readData(br, contentLength);
        return HttpRequestUtils.parseQueryString(queryString);
    }

    private byte[] getAllBytes(String url) throws IOException {
        return Files.readAllBytes(new File("webapp" + url).toPath());
    }


    private boolean isLogin(String line) {
        String[] headerToken = line.split(":");
        Map<String, String> cookies = HttpRequestUtils.parseCookies(headerToken[1].trim());
        String value = cookies.get("logined");
        if (value == null) {
            return false;
        }

        return Boolean.parseBoolean(value);
    }

    private int getContentLength(String line) {
        return Integer.parseInt(line.split(" ")[1]);
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200HeaderForCSS(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String location) {
        try {
            dos.writeBytes("HTTP/1.1 302 FOUND \r\n");
            dos.writeBytes("Location: " + location + "\r\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void response302LoginSuccessHeader(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 302 FOUND \r\n");
            dos.writeBytes("Set-Cookie: logined=true \r\n");
            dos.writeBytes("Location: /index.html \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void responseResource(OutputStream out, String url) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        byte[] body = getAllBytes(url);
        response200Header(dos, body.length);
        responseBody(dos, body);
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
