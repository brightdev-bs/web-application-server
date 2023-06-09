package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);
    private String method;
    private String path;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> params = new HashMap<>();


    public HttpRequest(InputStream in) {

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line = br.readLine();

            if (line == null) return;

            processRequestLine(line);

            line = br.readLine();
            while(line != null && !line.equals("")) {
                log.debug(line);
                String[] tokens = line.split(": ");
                headers.put(tokens[0], tokens[1]);
                line = br.readLine();
            }

            if("POST".equals(method)) {
                String body = IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
                params = HttpRequestUtils.parseQueryString(body);
            }
        } catch(IOException e) {
            log.error(e.getMessage());
        }
    }

    private void processRequestLine(String line) {
        log.debug("request line = {}", line);
        String[] requestLine = line.split(" ");
        method = requestLine[0];

        if("POST".equals(method)) {
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

    public String getHeader(String name) {
        return headers.get(name);
    }

    public String getParameter(String key) {
        return params.get(key);
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        if(path.indexOf("?") == -1) return path;
        else return path.substring(0, path.indexOf("?"));
    }
}
