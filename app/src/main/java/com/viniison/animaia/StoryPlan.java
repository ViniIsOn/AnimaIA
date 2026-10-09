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

    /**
     * Roteiro local baseado em palavras-chave e variações determinísticas.
     * Não confundir com texto gerado por modelo de IA.
     */
    public static StoryPlan offline(String prompt,int seconds) {
        String input=prompt==null?"":prompt.trim();
        if(input.isEmpty()) input="Uma aventura inesperada";
        String lower=input.toLowerCase(Locale.ROOT);
        int variety=Math.floorMod(input.hashCode(),4);
        String action="run";
        if(contains(lower,"pula","salta","plataforma")) action="jump";
        else if(contains(lower,"voa","voando","avião","pássaro")) action="fly";
        else if(contains(lower,"danç","música","festa")) action="dance";
        else if(contains(lower,"luta","batalha","boss","chefe")) action="battle";
        else if(contains(lower,"conversa","fala","encontra","descobre")) action="talk";
        else if(contains(lower,"persegue","foge","caça","corrida")) action="chase";
        String bg=contains(lower,"espaço","lua","planeta","galáxia")?"space":
                  contains(lower,"praia","mar","oceano","areia")?"beach":
                  contains(lower,"floresta","natureza","selva","árvore")?"forest":
                  contains(lower,"noite","escuro","assombra")?"night":
                  contains(lower,"sol","deserto","entardecer")?"sunset":"city";
        String[] openings={
            "Capítulo 1: "+input,
            "Tudo começou quando... "+input,
            "Uma nova aventura: "+input,
            "Hoje aconteceu algo inesperado: "+input
        };
        String[] twists={
            "Mas um desafio inesperado surge!",
            "Um rival aparece no pior momento!",
            "O caminho muda de repente!",
            "É hora de enfrentar um obstáculo!"
        };
        String[] endings={
            "Uma surpresa muda o final. Continua?",
            "Missão concluída... por enquanto!",
            "Essa aventura ainda não terminou!",
            "Fim do episódio. O que vem depois?"
        };
        StoryPlan story=new StoryPlan(seconds,"Sua história pixel art",false);
        story.scenes.add(new Scene(bg,"idle",limit(openings[variety],96)));
        story.scenes.add(new Scene(bg,action,limit("A aventura começa: "+input,96)));
        if(variety==1){
            story.scenes.add(new Scene("sunset","jump",twists[variety]));
            story.scenes.add(new Scene(bg,"battle","Chegou a hora de reagir!"));
        } else if(variety==2) {
            story.scenes.add(new Scene("forest","fly",twists[variety]));
            story.scenes.add(new Scene(bg,"chase","A busca continua em alta velocidade!"));
        } else if(variety==3){
            story.scenes.add(new Scene("night","chase",twists[variety]));
            story.scenes.add(new Scene("sunset","talk","Uma descoberta inesperada muda tudo!"));
        } else{
            story.scenes.add(new Scene(bg,"chase",twists[variety]));
            story.scenes.add(new Scene("sunset","jump","O herói encontra uma saída!"));
        }
        story.scenes.add(new Scene("night",variety==1?"dance":"talk",endings[variety]));
        return story;
    }

    private static boolean contains(String text,String... keys){
        for(String key:keys)if(text.contains(key))return true;
        return false;
    }
}
