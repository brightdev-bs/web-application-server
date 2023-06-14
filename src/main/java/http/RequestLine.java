package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

import java.util.HashMap;
import java.util.Map;

public class RequestLine {

    private static final Logger log = LoggerFactory.getLogger(RequestLine.class);

    private HttpMethod method;
    private String path;
    private Map<String, String> params = new HashMap<>();

    public RequestLine(String line) {
        log.debug("request line = {}", line);
        String[] requestLine = line.split(" ");
        if(requestLine.length != 3) {
            throw new IllegalArgumentException(line + "이 형식에 맞지 않습니다.");
        }

        method = HttpMethod.valueOf(requestLine[0]);
        if(method.isPost()) {
            path = requestLine[1];
            return;
        }

        int index = requestLine[1].indexOf("?");
        if (index == -1) {
            path = requestLine[1];
        } else {
            path = requestLine[1].substring(0, index);
            params = HttpRequestUtils.parseQueryString(requestLine[1].substring(index + 1));
        }
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getParams() {
        return params;
    }
}
