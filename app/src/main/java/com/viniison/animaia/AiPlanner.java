package com.viniison.animaia;

import org.json.JSONArray;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public final class AiPlanner {
    private AiPlanner() {}

    public static StoryPlan generate(String apiKey, String prompt, int seconds) throws Exception {
        JSONObject part = new JSONObject();
        part.put("text", "Você é um roteirista de animações pixel art 2D para celular. "
                + "Crie 4 a 6 cenas VISUAIS curtas e diferentes a partir desta ideia: " + prompt
                + ". Responda APENAS JSON válido, sem markdown. Estrutura exata: "
                + "{\"title\":\"Título\",\"scenes\":[{\"background\":\"city\",\"action\":\"run\",\"caption\":\"Legenda\"}]}. "
                + "Valores permitidos para background: city, sunset, forest, space, night, beach. "
                + "Valores permitidos para action: run, jump, chase, fly, dance, talk, battle, idle. "
                + "Cada legenda deve ter até 75 caracteres, em português brasileiro. "
                + "As cenas precisam ter começo, meio e fim criativos e fazer sentido entre si. "
                + "Você só planeja movimentos de sprites 2D, não gera imagens nem fala real.");

        JSONObject content = new JSONObject();
        content.put("parts", new JSONArray().put(part));
        JSONObject req = new JSONObject();
        req.put("contents", new JSONArray().put(content));
        JSONObject generation = new JSONObject();
        generation.put("responseMimeType", "application/json");
        generation.put("temperature", 0.85);
        generation.put("maxOutputTokens", 1600);
        req.put("generationConfig", generation);
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                + java.net.URLEncoder.encode(apiKey.trim(), "UTF-8");
        HttpURLConnection con = (HttpURLConnection) new URL(endpoint).openConnection();
        try {
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setConnectTimeout(16000);
            con.setReadTimeout(65000);
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            try (OutputStream stream = con.getOutputStream()) {
                stream.write(req.toString().getBytes(StandardCharsets.UTF_8));
            }
            int status = con.getResponseCode();
            String body = read(status < 400 ? con.getInputStream() : con.getErrorStream(), 1024 * 1024);
            if (status >= 400) {
                String message = new JSONObject(body).optJSONObject("error") == null ? body :
                    new JSONObject(body).getJSONObject("error").optString("message", body);
                throw new IllegalStateException("Gemini (" + status + "): " + message);
            }
            JSONObject response = new JSONObject(body);
            JSONArray candidates = response.getJSONArray("candidates");
            String answer = candidates.getJSONObject(0).getJSONObject("content")
                    .getJSONArray("parts").getJSONObject(0).getString("text");
            return StoryPlan.fromAiJson(answer, seconds);
        } finally {
            con.disconnect();
        }
    }

    static String read(InputStream input, int maxBytes) throws Exception {
        if (input == null) return "";
        try (InputStream stream = input; ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int count, total = 0;
            while ((count = stream.read(buf)) != -1) {
                total += count;
                if (total > maxBytes) throw new IllegalStateException("Resposta maior que o permitido.");
                result.write(buf, 0, count);
            }
            return result.toString("UTF-8");
        }
    }
}
