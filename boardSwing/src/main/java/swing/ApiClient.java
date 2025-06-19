// api/ApiClient.java
package swing;

import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class ApiClient {
    private static final String BASE = "http://localhost:8080";
    private static final Gson gson = new Gson();

    public static String post(String path, Object body) throws IOException {
        HttpURLConnection conn = connect("POST", path);
        if (body != null) {
            String json = gson.toJson(body);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }
        }
        return read(conn);
    }

    public static String get(String path) throws IOException {
        return read(connect("GET", path));
    }

    /* ───────── 내부 ───────── */
    private static HttpURLConnection connect(String method, String path) throws IOException {
        URL url = new URL(BASE + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json");
        if ("POST".equals(method)) conn.setDoOutput(true);
        return conn;
    }

    private static String read(HttpURLConnection conn) throws IOException {
        int code = conn.getResponseCode();                       // 1) 상태 코드 확인

        /* 2) 본문 읽기 (정상/에러 스트림 구분) */
        InputStream is = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            for (String line; (line = br.readLine()) != null; ) sb.append(line);
        }
        String body = sb.toString();

        /* 3) 4xx·5xx → 예외로 변환 */
        if (code >= 400) {
            String msg = body.isBlank() ? "HTTP " + code : body;
            throw new IOException(msg);          // Swing 쪽 catch 블록으로 전달
        }
        return body;                             // 2xx → 정상 리턴
    }
    private static String request(String method, String path, Object body) throws IOException {
    	System.out.println("➡️ " + method + " " + BASE + path);
    	if (body != null) System.out.println("   body = " + gson.toJson(body));
    	
        URL url = new URL(BASE + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoInput(true);

        if (body != null) {           // JSON 바디 전송
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(gson.toJson(body).getBytes(StandardCharsets.UTF_8));
            }
        }

        int code = conn.getResponseCode();

        /* ★ 4xx·5xx 는 곧바로 예외로 던진다 → Swing 쪽 catch 로 전달 */
        InputStream is = (code >= 400) ? conn.getErrorStream() : conn.getInputStream();
        String resp;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            resp = br.lines().collect(Collectors.joining("\n"));
        }
        if (code >= 400)
            throw new IOException("HTTP "+code+" : "+resp);  // ← 토스트에 그대로 찍힘
        return resp;
    }


    
    public static String put   (String url, Object body) throws IOException { return request("PUT"   , url, body); }
    public static String delete(String url)               throws IOException { return request("DELETE", url, null); }
    
}
