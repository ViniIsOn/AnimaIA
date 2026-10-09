package com.viniison.animaia;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
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

    static void show(Activity activity,SharedPreferences prefs,SpriteStore store,
                     int slot,Imported callback) {
        LinearLayout box=UiKit.vertical(activity);
        box.setPadding(UiKit.dp(activity,16),UiKit.dp(activity,14),
                       UiKit.dp(activity,16),UiKit.dp(activity,14));
        box.setBackgroundColor(UiKit.BG);
        UiKit.add(box,UiKit.label(activity,"🔎 Sprites no Google",20,true),0);
        UiKit.add(box,UiKit.muted(activity,"Escolha imagens que você tenha permissão de utilizar. "
                +"Prefira PNG com fundo transparente.",12),8);
        EditText query=UiKit.input(activity,"Ex.: pixel art sprite de dragão");
        UiKit.add(box,query,14);
        Button go=UiKit.button(activity,"Buscar imagens",true);
        UiKit.add(box,go,10);
        Button browser=UiKit.button(activity,"Abrir Google Imagens no navegador",false);
        UiKit.add(box,browser,8);
        TextView note=UiKit.muted(activity,"Busca integrada: requer Google Programmable Search "
                +"(API key + Search Engine ID nas configurações).",12);
        UiKit.add(box,note,8);
        LinearLayout results=UiKit.vertical(activity);
        ScrollView scroll=new ScrollView(activity);
        scroll.addView(results);
        LinearLayout.LayoutParams scrollParams=new LinearLayout.LayoutParams(-1,UiKit.dp(activity,370));
        scrollParams.topMargin=UiKit.dp(activity,10);
        box.addView(scroll,scrollParams);
        AlertDialog dialog=new AlertDialog.Builder(activity)
                .setView(box).setNegativeButton("Fechar",(d,which)->{}).create();
        dialog.show();
        browser.setOnClickListener(v -> {
            try {
                String url="https://www.google.com/search?tbm=isch&safe=active&q="
                    +URLEncoder.encode(query.getText().toString()+" sprite png","UTF-8");
                activity.startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));
            }catch(Exception e) {note.setText("Não foi possível abrir o navegador.");}
        });
        go.setOnClickListener(v -> {
            String q=query.getText().toString().trim();
            if(q.isEmpty()) {note.setText("Digite o personagem que deseja procurar.");return;}
            String key=prefs.getString("google_key","");
            String cx=prefs.getString("google_cx","");
            if(key.isEmpty()||cx.isEmpty()) {
                note.setText("Para ver resultados aqui, primeiro configure API key e Search Engine ID."
                    +" O botão abaixo abre a pesquisa normal no navegador.");
                return;
            }
            go.setEnabled(false);
            results.removeAllViews();
            note.setText("Pesquisando no Google...");
            new Thread(() -> {
                try {
                    java.util.ArrayList<GoogleImages.Result> images=GoogleImages.search(key,cx,q);
                    activity.runOnUiThread(() -> {
                        go.setEnabled(true);
                        note.setText(images.isEmpty()?"Nenhuma imagem encontrada. Tente outra busca."
                                    :images.size()+" resultados — toque em Usar sprite.");
                        for(GoogleImages.Result image:images)
                            addResult(activity,results,image,slot,store,note,dialog,callback);
                    });
                } catch(Exception error) {
                    activity.runOnUiThread(() -> {
                        go.setEnabled(true);
                        note.setText("Erro: "+error.getMessage());
                    });
                }
            }).start();
        });
    }

    private static void addResult(Activity activity,LinearLayout list,
                                  GoogleImages.Result result,int slot,SpriteStore store,
                                  TextView status,AlertDialog dialog,Imported callback) {
        LinearLayout row=UiKit.horizontal(activity);
        row.setPadding(UiKit.dp(activity,6),UiKit.dp(activity,12),
                       UiKit.dp(activity,6),UiKit.dp(activity,12));
        ImageView thumb=new ImageView(activity);
        thumb.setScaleType(ImageView.ScaleType.FIT_CENTER);
        thumb.setBackground(UiKit.shape(UiKit.CARD,10,activity));
        row.addView(thumb,new LinearLayout.LayoutParams(
                UiKit.dp(activity,74),UiKit.dp(activity,74)));
        LinearLayout text=UiKit.vertical(activity);
        text.setPadding(UiKit.dp(activity,10),0,0,0);
        TextView title=UiKit.label(activity,result.title,12,true);
        title.setMaxLines(2);UiKit.add(text,title,0);
        UiKit.add(text,UiKit.muted(activity,result.source,11),4);
        Button choose=UiKit.button(activity,"Usar sprite",true);
        UiKit.add(text,choose,6);
        row.addView(text,new LinearLayout.LayoutParams(0,-2,1));
        UiKit.add(list,row,4);
        new Thread(() -> {
            HttpURLConnection connection=null;
            try {
                if(!result.thumbnailUrl.startsWith("https://"))return;
                connection=(HttpURLConnection)new URL(result.thumbnailUrl).openConnection();
                connection.setConnectTimeout(7000);connection.setReadTimeout(8000);
                if(connection.getResponseCode()!=200)return;
                Bitmap bitmap=BitmapFactory.decodeStream(connection.getInputStream());
                if(bitmap!=null)activity.runOnUiThread(() -> thumb.setImageBitmap(bitmap));
            }catch(Exception ignored){}finally{if(connection!=null)connection.disconnect();}
        }).start();
        choose.setOnClickListener(v -> {
            choose.setEnabled(false);status.setText("Importando imagem...");
            new Thread(() -> {
                try {
                    store.importFromHttps(slot,result.imageUrl);
                    activity.runOnUiThread(() -> {
                        callback.onImported("Sprite salvo em "+SpriteStore.SLOTS[slot]+".");
                        dialog.dismiss();
                    });
                }catch(Exception e) {
                    activity.runOnUiThread(() -> {
                        choose.setEnabled(true);
                        status.setText("Falha ao baixar: "+e.getMessage()
                            +". Abra a fonte no navegador e importe o PNG manualmente.");
                    });
                }
            }).start();
        });
    }
}
