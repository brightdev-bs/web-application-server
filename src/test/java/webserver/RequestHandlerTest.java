package webserver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import util.HttpRequestUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RequestHandlerTest {

    @DisplayName("BufferedReader 테스트")
    @Test
    public void BufferdReaderTest() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("filetest.txt"));
        String s = br.readLine();
        assertEquals("file test", s);

        s = br.readLine();
        assertEquals("I want to check if BufferedReader collect all the data in this file", s);
    }

    @DisplayName("split 테스트")
    @Test
    public void splitTest() {
        String text = "GET /index.html HTTP/1.1";
        String[] tokens = text.split(" ");
        assertEquals(tokens[1], "/index.html");
    }

    @DisplayName("쿼리 스트링 파싱 테스트 (회원가입)")
    @Test
    public void parseQueryString() {
        String queryString = "/user/create?userId=user&password=password&name=kim&email=kim@naver.com";
        Map<String, String> map = new HashMap<>();
        if(queryString.contains("?")) {
            String info = queryString.substring(queryString.indexOf("?") + 1);
            map = HttpRequestUtils.parseQueryString(info);
        }

    }


}