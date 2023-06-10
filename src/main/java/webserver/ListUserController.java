package webserver;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ListUserController implements Controller {

    private static final Logger log = LoggerFactory.getLogger(ListUserController.class);

    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
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
    }
}
