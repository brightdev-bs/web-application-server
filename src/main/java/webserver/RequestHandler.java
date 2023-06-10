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
            HttpResponse response = new HttpResponse(out);
            String url = getDefaultPath(request.getPath());


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
                log.debug("user = {}", user);
                if(user != null) {
                    if(user.getPassword().equals(request.getParameter("password"))) {
                        log.debug("사용자 로그인 성공 : {}", user.getUserId());
                        response.addHeader("logined", "true");
                        response.sendRedirect("/index.html");
                    } else {
                        response.sendRedirect("/user/login_failed.html");
                    }
                } else {
                    response.sendRedirect("/user/login_failed.html");
                }
            } else if(url.equals("/user/list")) {

                if(!request.getHeader("Cookie").equals(false)) {
                    response.sendRedirect("/user/login.html");
                    return;
                }

                StringBuilder sb = new StringBuilder();
                sb.append("<li>");
                for (User user : DataBase.findAll()) {
                    log.debug("user : {}", user.getEmail());
                    sb.append("     <ul>" + user.getEmail() + "</ul>");
                }
                sb.append("</li>");
                response.forwardBody(sb.toString());
            } else {
                response.forward(url);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private String getDefaultPath(String path) {
        if (path.equals("/")) {
            return "/index.html";
        }
        return path;
    }
}
