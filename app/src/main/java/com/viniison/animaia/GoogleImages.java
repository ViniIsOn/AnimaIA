package com.viniison.animaia;

import org.json.JSONArray;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public final class GoogleImages {
    public static final class Result {
        public final String title, imageUrl, thumbnailUrl, source;
        Result(String title, String imageUrl, String thumbnailUrl, String source) {
            this.title=title; this.imageUrl=imageUrl; this.thumbnailUrl=thumbnailUrl; this.source=source;
        }
    }

    public static ArrayList<Result> search(String key, String engineId, String query) throws Exception {
        if (key.isEmpty() || engineId.isEmpty())
            throw new IllegalStateException("Configure sua chave da API e o ID do mecanismo de pesquisa.");
        String url = "https://www.googleapis.com/customsearch/v1?searchType=image&safe=active&num=8"
                + "&key=" + URLEncoder.encode(key, "UTF-8")
                + "&cx=" + URLEncoder.encode(engineId, "UTF-8")
                + "&q=" + URLEncoder.encode(query + " pixel art sprite png", "UTF-8");
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        try {
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(25000);
            int code=connection.getResponseCode();
            String body=AiPlanner.read(code<400?connection.getInputStream():connection.getErrorStream(), 1024*1024);
            JSONObject root=new JSONObject(body);
            if (code>=400) {
                JSONObject error=root.optJSONObject("error");
                throw new IllegalStateException(error == null ? "Erro na busca Google: "+code : error.optString("message"));
            }
            ArrayList<Result> found = new ArrayList<>();
            JSONArray arr=root.optJSONArray("items");
            if (arr==null) return found;
            for (int i=0;i<arr.length();i++) {
                JSONObject item=arr.getJSONObject(i);
                JSONObject image=item.optJSONObject("image");
                String link=item.optString("link","");
                if (!link.startsWith("https://")) continue;
                found.add(new Result(item.optString("title","Sprite"),
                    link, image==null?link:image.optString("thumbnailLink",link),
                    item.optString("displayLink","Fonte não informada")));
            }
            return found;
        } finally { connection.disconnect(); }
    }
}
