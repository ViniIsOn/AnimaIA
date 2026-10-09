package com.viniison.animaia;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

final class GoogleImagePicker {
    interface Imported { void onImported(String detail); }

    // Commons is a live in-app search; Google is opened in the browser.
    static void show(Activity activity, SharedPreferences prefs, SpriteStore store,
                     int slot, Imported callback) {
        LinearLayout box=UiKit.vertical(activity);
        box.setPadding(UiKit.dp(activity,16),UiKit.dp(activity,14),
                       UiKit.dp(activity,16),UiKit.dp(activity,14));
        box.setBackgroundColor(UiKit.BG);
        UiKit.add(box,UiKit.label(activity,"🔎 Encontrar imagens",20,true),0);
        UiKit.add(box,UiKit.muted(activity,
                "A busca interna usa Wikimedia Commons, sem chave. "
                +"Para franquias de jogos, use Google Imagens no navegador e importe "
                +"arquivos que você tenha autorização para utilizar.",12),8);
        EditText query=UiKit.input(activity,"Ex.: pixel art sprite transparent");
        UiKit.add(box,query,13);
        Button go=UiKit.button(activity,"🌍 Buscar grátis (Wikimedia Commons)",true);
        UiKit.add(box,go,9);
        Button browser=UiKit.button(activity,"🌐 Abrir Google Imagens",false);
        UiKit.add(box,browser,8);
        TextView note=UiKit.muted(activity,
                "Os resultados podem não ser sprites. Confira licença e atribuição antes de publicar.",12);
        UiKit.add(box,note,8);
        LinearLayout results=UiKit.vertical(activity);
        ScrollView scroller=new ScrollView(activity);
        scroller.addView(results);
        LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,UiKit.dp(activity,345));
        sp.topMargin=UiKit.dp(activity,10);box.addView(scroller,sp);
        AlertDialog dialog=new AlertDialog.Builder(activity)
                .setView(box).setNegativeButton("Fechar",(d,w)->{}).create();
        dialog.show();

        browser.setOnClickListener(v -> {
            try {
                String url="https://www.google.com/search?tbm=isch&safe=active&q="
                    +URLEncoder.encode(query.getText().toString()+" sprite png","UTF-8");
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } catch(Exception e){note.setText("Não foi possível abrir o navegador.");}
        });

        go.setOnClickListener(v -> {
            String q=query.getText().toString().trim();
            if(q.isEmpty()){note.setText("Digite o personagem ou objeto procurado.");return;}
            results.removeAllViews();go.setEnabled(false);
            note.setText("Buscando imagens na Wikimedia Commons...");
            new Thread(() -> {
                try {
                    java.util.ArrayList<CommonsImages.Result> list=CommonsImages.search(q);
                    activity.runOnUiThread(() -> {
                        go.setEnabled(true);
                        note.setText(list.isEmpty()
                            ?"Sem resultados compatíveis. Pesquise outro termo ou importe da galeria."
                            :list.size()+" imagens. Confira a licença antes de usar.");
                        for(CommonsImages.Result result:list)
                            addResult(activity,results,result,slot,store,note,dialog,callback);
                    });
                }catch(Exception e){
                    activity.runOnUiThread(() -> {
                        go.setEnabled(true);
                        note.setText("Falha na busca: "+e.getMessage());
                    });
                }
            }).start();
        });
    }

    private static void addResult(Activity activity,LinearLayout list,
                  CommonsImages.Result result,int slot,SpriteStore store,TextView status,
                  AlertDialog dialog, Imported callback) {
        LinearLayout row=UiKit.horizontal(activity);
        row.setPadding(0,UiKit.dp(activity,8),0,UiKit.dp(activity,8));
        ImageView thumb=new ImageView(activity);
        thumb.setScaleType(ImageView.ScaleType.FIT_CENTER);
        thumb.setBackground(UiKit.shape(UiKit.CARD,10,activity));
        row.addView(thumb,new LinearLayout.LayoutParams(
                UiKit.dp(activity,76),UiKit.dp(activity,76)));
        LinearLayout description=UiKit.vertical(activity);
        description.setPadding(UiKit.dp(activity,9),0,0,0);
        TextView name=UiKit.label(activity,result.title.replace("File:",""),12,true);
        name.setMaxLines(2);
        UiKit.add(description,name,0);
        UiKit.add(description,UiKit.muted(activity,"Licença: "+result.license,11),3);
        LinearLayout buttons=UiKit.horizontal(activity);
        Button choose=UiKit.button(activity,"Importar",true);
        Button source=UiKit.button(activity,"Fonte",false);
        UiKit.addWeighted(buttons,choose,1,0);
        UiKit.addWeighted(buttons,source,1,5);
        UiKit.add(description,buttons,4);
        row.addView(description,new LinearLayout.LayoutParams(0,-2,1));
        UiKit.add(list,row,3);

        source.setOnClickListener(v -> {
            if(result.pageUrl.startsWith("https://"))
                activity.startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(result.pageUrl)));
        });
        new Thread(() -> {
            HttpURLConnection connection=null;
            try{
                connection=(HttpURLConnection)new URL(result.previewUrl).openConnection();
                connection.setRequestProperty("User-Agent","AnimaIA/0.2 (https://github.com/ViniIsOn/AnimaIA)");
                connection.setConnectTimeout(6000);connection.setReadTimeout(9000);
                if(connection.getResponseCode()!=200)return;
                try(java.io.InputStream input=connection.getInputStream()){
                    Bitmap bitmap=BitmapFactory.decodeStream(input);
                    if(bitmap!=null)
                        activity.runOnUiThread(() -> {if(dialog.isShowing())thumb.setImageBitmap(bitmap);});
                }
            }catch(Exception ignored){}finally{if(connection!=null)connection.disconnect();}
        }).start();

        choose.setOnClickListener(v -> {
            choose.setEnabled(false);status.setText("Importando imagem...");
            new Thread(() -> {
                try{
                    store.importFromHttps(slot,result.imageUrl);
                    activity.runOnUiThread(() -> {
                        callback.onImported("Imagem importada em "+SpriteStore.SLOTS[slot]+
                                ". Verifique os créditos: "+result.license);
                        dialog.dismiss();
                    });
                }catch(Exception e){
                    activity.runOnUiThread(() -> {
                        choose.setEnabled(true);
                        status.setText("Falha na importação: "+e.getMessage());
                    });
                }
            }).start();
        });
    }
}
