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
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.

            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line = br.readLine();

            if(line == null) {
                return;
            }

            String[] tokens = line.split(" ");
            String url = tokens[1];
            log.debug("url = {}", url);

            int contentLength = 0;
            Collection<User> users = new ArrayList<>();
            while(!line.equals("")) {
                line = br.readLine();
                log.debug("line = {}", line);
                if(line.contains("Content-Length")) {
                    contentLength = getContentLength(line);
                } else if(line.contains("Set-Cookie")) {
                    Map<String, String> cookies = HttpRequestUtils.parseCookies(line.split(" ")[0]);
                    boolean flag = Boolean.valueOf(cookies.get("Set-Cookie"));
                    log.debug("flag = {}", flag);
                    if(flag) {
                        users = DataBase.findAll();
                    }
                }
            }

            DataOutputStream dos = new DataOutputStream(out);
            if(url.startsWith("/user/create")) {
                String queryString = IOUtils.readData(br, contentLength);
                log.debug("Content-Length = {}", queryString);

                Map<String, String> params = HttpRequestUtils.parseQueryString(queryString);

                User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
                log.debug("User = {}", user);

                DataBase.addUser(user);

                response302Header(dos, "/index.html");
            } else if(url.equals("/user/login")) {
                String queryString = IOUtils.readData(br, contentLength);
                Map<String, String> params = HttpRequestUtils.parseQueryString(queryString);

                User user = DataBase.findUserById(params.get("userId"));
                if(user == null) {
                    log.debug("사용자 없음.");
                    responseResource(out, "/user/login_failed.html");
                    return;
                }

                if(user.getPassword().equals(params.get("password"))) {
                    log.debug("사용자 로그인 성공 : {}", user.getUserId());
                    response302LoginSuccessHeader(dos);
                } else {
                    responseResource(dos, "/user/login_failed.html");
                }
            } else if(url.startsWith("/user/list")) {

                if(users.isEmpty()) {
                    String body = "사용자 없음";
                    response200Header(dos, body.length());
                    responseBody(dos, body.getBytes());
                }
                else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("<li>");
                    for (User user : users) {
                        sb.append("     <ul>" + user.getEmail() + "</ul>");
                    }
                    sb.append("</li>");
                    byte[] body = sb.toString().getBytes();
                    response200Header(dos, body.length);
                    responseBody(dos, body);
                }
            }else {
                byte[] body = Files.readAllBytes(new File("webapp" + url).toPath());
                response200Header(dos, body.length);
                responseBody(dos, body);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
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

    private void response302Header(DataOutputStream dos, String location) {
        try {
            dos.writeBytes("HTTP/1.1 302 FOUND \r\n");
            dos.writeBytes("Location: " + location + "\r\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseResource(OutputStream out, String url) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        byte[] body = Files.readAllBytes(new File("webapp" + url).toPath());
        response200Header(dos, body.length);
        responseBody(dos, body);
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
}
