package webserver;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LoginController implements Controller {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);
    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
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
    }
}
