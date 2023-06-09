package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {

            HttpRequest request = new HttpRequest(in);
            String url = getDefaultPath(request.getPath());

//            DataOutputStream dos = new DataOutputStream(out);

            HttpResponse response = new HttpResponse(out);
            if(url.startsWith("/user/create")) {
                User user = new User(
                        request.getParameter("userId"),
                        request.getParameter("password"),
                        request.getParameter("name"),
                        request.getParameter("email")
                );
                log.debug("User = {}", user);

                DataBase.addUser(user);

                response.sendRedirect("/index.html");
            } else if(url.equals("/user/login")) {
                User user = DataBase.findUserById(request.getParameter("userId"));
                if (!request.isLogin("Cookie")) {
                    responseResource(out, "/user/login_failed.html");
                    return;
                }

                if(user.getPassword().equals(request.getParameter("password"))) {
                    log.debug("사용자 로그인 성공 : {}", user.getUserId());
//                    response302LoginSuccessHeader(dos);
                } else {
//                    responseResource(dos, "/user/login_failed.html");
                }
            } else if(url.equals("/user/list")) {

                if(!request.getHeader("Cookie").equals(false)) {
                    responseResource(out, "user/login.html");
                    return;
                }
                else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("<li>");
                    for (User user : DataBase.findAll()) {
                        log.debug("user : {}", user.getEmail());
                        sb.append("     <ul>" + user.getEmail() + "</ul>");
                    }
                    sb.append("</li>");
                    byte[] body = sb.toString().getBytes();
//                    response200Header(dos, body.length);
//                    responseBody(dos, body);
                }
            } else if(url.endsWith(".css")) {
                byte[] body = getAllBytes(url);
//                response200HeaderForCSS(dos, body.length);
//                responseBody(dos, body);
            } else {
                byte[] body = getAllBytes(url);
//                response200Header(dos, body.length);
//                responseBody(dos, body);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private byte[] getAllBytes(String url) throws IOException {
        return Files.readAllBytes(new File("webapp" + url).toPath());
    }




    private String getDefaultPath(String path) {
        if (path.equals("/")) {
            return "/index.html";
        }
        return path;
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
