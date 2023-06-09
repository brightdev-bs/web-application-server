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

    private final InputStream in;
    private String method;
    private String path;
    private Map<String, String> headerMap = new HashMap<>();
    private Map<String, String> parameterMap = new HashMap<>();


    public HttpRequest(InputStream in) throws IOException {
        this.in = in;
        BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String line = br.readLine();

        if (line == null) return;

        getRequestLineInfo(line);

        getHeaderInfo(br);

        getParameterInfo(br);
    }

    private void getRequestLineInfo(String line) {
        String[] requestLine = line.split(" ");
        method = requestLine[0];
        path = requestLine[1];
    }

    private void getHeaderInfo(BufferedReader br) throws IOException {
        String line;
        line = br.readLine();
        while(line != null && !line.equals("")) {
            log.debug(line);
            String[] tokens = line.split(": ");
            headerMap.put(tokens[0], tokens[1]);
            line = br.readLine();
        }
    }

    private void getParameterInfo(BufferedReader br) throws IOException {
        // post와 get만 있다고 가정했다.
        if(method.equals("POST")) {
            if(headerMap.containsKey("Content-Length")) {
                String body = IOUtils.readData(br, Integer.parseInt(headerMap.get("Content-Length")));
                parameterMap = HttpRequestUtils.parseQueryString(body);
            }
        } else {
            if(path.indexOf("?") != -1) {
                String queryString = path.substring(path.indexOf("?") + 1);
                parameterMap = HttpRequestUtils.parseQueryString(queryString);
            }
        }
    }

    public String getHeader(String name) {
        return headerMap.get(name);
    }

    public String getParameter(String key) {
        return parameterMap.get(key);
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        if(path.indexOf("?") == -1) return path;
        else return path.substring(0, path.indexOf("?"));
    }
}
