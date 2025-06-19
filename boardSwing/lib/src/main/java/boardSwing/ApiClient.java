// api/ApiClient.java
package com.example.client.api;

import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

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
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream(),
                StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            for (String line; (line = br.readLine()) != null; ) sb.append(line);
            return sb.toString();
        }
    }
}
