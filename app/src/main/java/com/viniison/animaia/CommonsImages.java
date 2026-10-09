package com.viniison.animaia;

import org.json.JSONObject;
import org.json.JSONArray;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Wikimedia Commons media search, no registration or API key required.
 * This is NOT Google Images. Each file has its own license and attribution rules.
 */
public final class CommonsImages {
    public static class Result {
        public final String title, imageUrl, previewUrl, license, pageUrl;
        Result(String title, String imageUrl, String previewUrl, String license, String pageUrl) {
            this.title=title;
            this.imageUrl=imageUrl;
            this.previewUrl=previewUrl;
            this.license=license;
            this.pageUrl=pageUrl;
        }
    }

    static ArrayList<Result> search(String phrase) throws Exception {
        if(phrase.trim().isEmpty())throw new IllegalArgumentException("Escreva o que procura.");
        String endpoint="https://commons.wikimedia.org/w/api.php?action=query&format=json"
            +"&generator=search&gsrnamespace=6&gsrlimit=12"
            +"&gsrsearch="+URLEncoder.encode(phrase.trim(),"UTF-8")
            +"&prop=imageinfo&iiprop=url%7Cextmetadata&iiurlwidth=180";
        HttpURLConnection con=(HttpURLConnection)new URL(endpoint).openConnection();
        try {
            con.setConnectTimeout(12000);con.setReadTimeout(20000);
            con.setRequestProperty("User-Agent","AnimaIA/0.2 (https://github.com/ViniIsOn/AnimaIA)");
            if(con.getResponseCode()!=200) throw new IllegalStateException("Erro Wikimedia: "+con.getResponseCode());
            JSONObject root=new JSONObject(AiPlanner.read(con.getInputStream(),1024*1024));
            JSONObject query=root.optJSONObject("query");
            JSONObject pages=query==null?null:query.optJSONObject("pages");
            ArrayList<Result> output=new ArrayList<>();
            if(pages==null)return output;
            Iterator<String> keys=pages.keys();
            while(keys.hasNext()){
                JSONObject item=pages.optJSONObject(keys.next());
                if(item==null)continue;
                JSONArray info=item.optJSONArray("imageinfo");
                if(info==null||info.length()==0)continue;
                JSONObject image=info.optJSONObject(0);
                if(image==null)continue;
                String url=image.optString("url","");
                String low=url.toLowerCase(java.util.Locale.ROOT);
                if(!url.startsWith("https://") ||
                    !(low.endsWith(".png")||low.endsWith(".jpg")||
                      low.endsWith(".jpeg")||low.endsWith(".webp")))continue;
                JSONObject meta=image.optJSONObject("extmetadata");
                JSONObject license=meta==null?null:meta.optJSONObject("LicenseShortName");
                String kind=license==null?"Ver licença na fonte":license.optString("value","Ver licença na fonte");
                String source=image.optString("descriptionurl","");
                output.add(new Result(item.optString("title","Imagem"),
                     url,image.optString("thumburl",url),kind,source));
            }
            return output;
        }finally{con.disconnect();}
    }
}
