package com.viniison.animaia;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Locale;

public final class StoryPlan {
    public final ArrayList<Scene> scenes = new ArrayList<>();
    public final int durationSeconds;
    public final String title;
    public final boolean generatedByAi;

    public static final class Scene {
        public final String background;
        public final String action;
        public final String caption;
        public Scene(String background, String action, String caption) {
            this.background = background;
            this.action = action;
            this.caption = caption;
        }
    }

    private StoryPlan(int seconds, String title, boolean ai) {
        durationSeconds = Math.max(8, Math.min(24, seconds));
        this.title = limit(title, 46);
        generatedByAi = ai;
    }

    public Scene at(double seconds) {
        int i = (int) Math.floor(Math.max(0, seconds) / durationSeconds * scenes.size());
        return scenes.get(Math.max(0, Math.min(scenes.size() - 1, i)));
    }

    public double sceneProgress(double seconds) {
        double length = durationSeconds / (double) scenes.size();
        double elapsed = Math.max(0, seconds) % length;
        return Math.min(1, elapsed / length);
    }

    public static StoryPlan fromAiJson(String raw, int desiredSeconds) throws Exception {
        int start = raw.indexOf('{'), end = raw.lastIndexOf('}');
        if (start < 0 || end <= start) throw new IllegalArgumentException("A IA não enviou um roteiro JSON válido.");
        JSONObject root = new JSONObject(raw.substring(start, end + 1));
        StoryPlan result = new StoryPlan(desiredSeconds, root.optString("title", "Minha animação"), true);
        JSONArray arr = root.getJSONArray("scenes");
        for (int i = 0; i < arr.length() && i < 6; i++) {
            JSONObject obj = arr.getJSONObject(i);
            String background = allowed(obj.optString("background", "city"),
                    "city", "sunset", "forest", "space", "night", "beach");
            String action = allowed(obj.optString("action", "run"),
                    "run", "jump", "chase", "fly", "dance", "talk", "battle", "idle");
            result.scenes.add(new Scene(background, action,
                    limit(obj.optString("caption", "..."), 110)));
        }
        if (result.scenes.size() < 2) throw new IllegalArgumentException("A IA enviou cenas insuficientes.");
        return result;
    }

    private static String allowed(String text, String... options) {
        for (String option : options) if (option.equalsIgnoreCase(text)) return option;
        return options[0];
    }

    private static String limit(String text, int max) {
        if (text == null) return "";
        text = text.trim().replace('\n', ' ').replace('\r', ' ');
        return text.length() > max ? text.substring(0, max) : text;
    }

    public static StoryPlan offline(String prompt, int seconds) {
        String input = prompt == null ? "" : prompt.trim();
        if (input.isEmpty()) input = "Uma aventura inesperada";
        String lower = input.toLowerCase(Locale.ROOT);
        String action = "run";
        if (lower.contains("pula") || lower.contains("salta")) action = "jump";
        else if (lower.contains("voa") || lower.contains("voando")) action = "fly";
        else if (lower.contains("danç")) action = "dance";
        else if (lower.contains("luta") || lower.contains("batalha")) action = "battle";
        else if (lower.contains("conversa") || lower.contains("fala")) action = "talk";
        else if (lower.contains("persegue") || lower.contains("foge")) action = "chase";

        String bg = lower.contains("espaço") || lower.contains("lua") ? "space" :
                lower.contains("praia") || lower.contains("mar") ? "beach" :
                lower.contains("floresta") || lower.contains("natureza") ? "forest" :
                lower.contains("noite") ? "night" : "city";
        StoryPlan result = new StoryPlan(seconds, "Sua aventura animada", false);
        result.scenes.add(new Scene(bg, "idle", limit("Era uma vez... " + input, 100)));
        result.scenes.add(new Scene(bg, action, "De repente, tudo começou a mudar!"));
        result.scenes.add(new Scene("sunset", "chase", "A história ficou ainda mais emocionante..."));
        result.scenes.add(new Scene("night", "dance", "Continua no próximo episódio?"));
        return result;
    }
}
