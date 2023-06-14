package controller;

import http.HttpMethod;
import http.HttpRequest;
import http.HttpResponse;

import java.io.IOException;

// 만약 같은 URL로 다양한 Http Method가 사용된다면 이를 상속하여 사용하자.
public class AbstractController implements Controller {

    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        HttpMethod method = request.getMethod();

        if (method.isPost()) {
            doPost(request, response);
        } else {
            doGet(request, response);
        }
    }

    protected void doPost(HttpRequest request, HttpResponse response) {

    }

    protected void doGet(HttpRequest request, HttpResponse response) {

    }
}
