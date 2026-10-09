package com.viniison.animaia;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class MainActivity extends Activity {
    private static final int PICK_SPRITE=91;
    private SharedPreferences prefs;
    private SpriteStore sprites;
    private PreviewCanvas preview;
    private StoryPlan story;
    private EditText prompt,geminiKey,googleKey,googleCx;
    private Button generate,export,importSprite,searchSprite,share;
    private final Button[] slots=new Button[3];
    private TextView status;
    private boolean portrait=false,busy=false;
    private int seconds=12,selectedSlot=0;
    private Uri video;

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        getWindow().setStatusBarColor(UiKit.BG);
        getWindow().setNavigationBarColor(UiKit.BG);
        prefs=getSharedPreferences("animaia",MODE_PRIVATE);
        sprites=new SpriteStore(this);
        story=StoryPlan.offline("Uma aventura pixel art com reviravolta",seconds);
        setup();
    }

    private void setup() {
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(UiKit.BG);
        LinearLayout root=UiKit.vertical(this);
        root.setPadding(UiKit.dp(this,18),UiKit.dp(this,18),
            UiKit.dp(this,18),UiKit.dp(this,28));
        scroll.addView(root);
        setContentView(scroll);
        UiKit.add(root,UiKit.label(this,"✦ AnimaIA",30,true),0);
        UiKit.add(root,UiKit.muted(this,"Estúdio de histórias animadas • Pixel art 2D",13),4);
        UiKit.add(root,UiKit.label(this,"PRÉVIA AO VIVO",12,true),24);
        preview=new PreviewCanvas(this,sprites);
        preview.setStory(story);
        LinearLayout.LayoutParams previewLp=new LinearLayout.LayoutParams(-1,UiKit.dp(this,258));
        previewLp.topMargin=UiKit.dp(this,9);
        root.addView(preview,previewLp);

        LinearLayout aspect=UiKit.horizontal(this);
        Button wide=UiKit.button(this,"▰ 16:9  •  YouTube",true);
        Button tall=UiKit.button(this,"▯ 9:16  •  Shorts",false);
        UiKit.addWeighted(aspect,wide,1,0);
        UiKit.addWeighted(aspect,tall,1,8);
        UiKit.add(root,aspect,12);
        wide.setOnClickListener(v -> chooseRatio(false,wide,tall));
        tall.setOnClickListener(v -> chooseRatio(true,wide,tall));

        UiKit.add(root,UiKit.label(this,"1. Sua ideia",19,true),23);
        UiKit.add(root,UiKit.muted(this,"Descreva personagens, cenário e o que acontece. "
                +"Com uma chave Gemini, a IA cria as cenas; sem chave, você testa o modo de demonstração.",12),7);
        prompt=UiKit.input(this,"Ex.: Um herói encontra uma arara mágica na floresta e aprende a voar...");
        prompt.setSingleLine(false);prompt.setMinLines(3);
        prompt.setGravity(Gravity.TOP|Gravity.START);
        prompt.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE
                |InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        prompt.setText(prefs.getString("last_prompt","Um herói encontra um rival e começa uma corrida insana"));
        UiKit.add(root,prompt,10);

        LinearLayout durationRow=UiKit.horizontal(this);
        String[] lengths={"8 s","12 s","18 s","24 s"};
        for(int i=0;i<lengths.length;i++) {
            final int n=new int[]{8,12,18,24}[i];
            Button button=UiKit.button(this,lengths[i],i==1);
            UiKit.addWeighted(durationRow,button,1,i==0?0:6);
            button.setOnClickListener(v -> {
                seconds=n;
                for(int j=0;j<durationRow.getChildCount();j++)
                    durationRow.getChildAt(j).setBackground(UiKit.shape(
                        durationRow.getChildAt(j)==button?UiKit.VIOLET:UiKit.CARD,14,this));
                status.setText("Duração escolhida: "+seconds+" segundos. Gere o roteiro novamente.");
            });
        }
        UiKit.add(root,durationRow,10);
        generate=UiKit.button(this,"✨ Criar roteiro e animação",true);
        UiKit.add(root,generate,14);
        generate.setOnClickListener(v -> generateStory());

        UiKit.add(root,UiKit.label(this,"2. Personagens e sprites",19,true),24);
        UiKit.add(root,UiKit.muted(this,"Troque os personagens de teste por PNGs transparentes. "
                +"São três camadas: herói, rival e amigo.",12),6);
        LinearLayout slotRow=UiKit.horizontal(this);
        for(int i=0;i<3;i++) {
            final int slot=i;
            slots[i]=UiKit.button(this,SpriteStore.SLOTS[i],i==0);
            UiKit.addWeighted(slotRow,slots[i],1,i==0?0:6);
            slots[i].setOnClickListener(v -> setSlot(slot));
        }
        UiKit.add(root,slotRow,12);
        LinearLayout spriteActions=UiKit.horizontal(this);
        importSprite=UiKit.button(this,"🖼 Importar PNG",false);
        searchSprite=UiKit.button(this,"🔎 Google Imagens",false);
        UiKit.addWeighted(spriteActions,importSprite,1,0);
        UiKit.addWeighted(spriteActions,searchSprite,1,8);
        UiKit.add(root,spriteActions,10);
        importSprite.setOnClickListener(v -> openGallery());
        searchSprite.setOnClickListener(v -> GoogleImagePicker.show(
                this,prefs,sprites,selectedSlot,detail -> {
                    status.setText(detail);preview.invalidate();
                }));
        Button reset=UiKit.button(this,"Restaurar personagem selecionado",false);
        UiKit.add(root,reset,7);
        reset.setOnClickListener(v -> {
            sprites.clear(selectedSlot);
            preview.invalidate();
            status.setText("Personagem de teste restaurado: "+SpriteStore.SLOTS[selectedSlot]);
        });

        UiKit.add(root,UiKit.label(this,"3. Exportar seu vídeo",19,true),24);
        UiKit.add(root,UiKit.muted(this,"Vídeo MP4 H.264 • 768×432 ou 432×768 • 18 fps. "
            +"A v0.1 exporta sem áudio. O arquivo aparece na pasta Filmes/AnimaIA.",12),6);
        export=UiKit.button(this,"🎬 Renderizar MP4",true);
        UiKit.add(root,export,12);
        export.setOnClickListener(v -> exportVideo());
        share=UiKit.button(this,"↗ Compartilhar último vídeo",false);
        share.setVisibility(View.GONE);
        UiKit.add(root,share,9);
        share.setOnClickListener(v -> shareVideo());

        status=UiKit.muted(this,"Pronto. Escolha sprites, gere cenas e exporte!",13);
        status.setTextColor(UiKit.CYAN);
        UiKit.add(root,status,17);

        Button settings=UiKit.button(this,"⚙ Configurar IA e busca Google",false);
        UiKit.add(root,settings,22);
        LinearLayout panel=settingsPanel();
        panel.setVisibility(View.GONE);
        UiKit.add(root,panel,8);
        settings.setOnClickListener(v -> panel.setVisibility(
                panel.getVisibility()==View.VISIBLE?View.GONE:View.VISIBLE));
    }

    private LinearLayout settingsPanel() {
        LinearLayout panel=UiKit.vertical(this);
        panel.setPadding(UiKit.dp(this,13),UiKit.dp(this,10),
                         UiKit.dp(this,13),UiKit.dp(this,12));
        panel.setBackground(UiKit.shape(UiKit.CARD,14,this));
        UiKit.add(panel,UiKit.muted(this,"As chaves são suas. Não as coloque no repositório público; "
                +"elas ficam nas preferências deste aparelho.",12),0);
        geminiKey=UiKit.input(this,"Chave Gemini API");
        geminiKey.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
        geminiKey.setText(prefs.getString("gemini_key",""));
        UiKit.add(panel,geminiKey,10);
        googleKey=UiKit.input(this,"Google Custom Search API key");
        googleKey.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
        googleKey.setText(prefs.getString("google_key",""));
        UiKit.add(panel,googleKey,9);
        googleCx=UiKit.input(this,"Google Programmable Search Engine ID (cx)");
        googleCx.setText(prefs.getString("google_cx",""));
        UiKit.add(panel,googleCx,9);
        Button save=UiKit.button(this,"Salvar configurações",true);
        UiKit.add(panel,save,10);
        save.setOnClickListener(v -> {
            prefs.edit().putString("gemini_key",geminiKey.getText().toString().trim())
                .putString("google_key",googleKey.getText().toString().trim())
                .putString("google_cx",googleCx.getText().toString().trim()).apply();
            status.setText("Configurações salvas neste celular.");
        });
        return panel;
    }

    private void chooseRatio(boolean mode,Button wide,Button tall) {
        portrait=mode;preview.setPortrait(portrait);
        wide.setBackground(UiKit.shape(mode?UiKit.CARD:UiKit.VIOLET,14,this));
        tall.setBackground(UiKit.shape(mode?UiKit.VIOLET:UiKit.CARD,14,this));
        status.setText(mode?"Proporção 9:16 para Shorts/Reels.":"Proporção 16:9 para YouTube.");
    }

    private void setSlot(int index) {
        selectedSlot=index;
        for(int i=0;i<3;i++)
            slots[i].setBackground(UiKit.shape(i==index?UiKit.VIOLET:UiKit.CARD,14,this));
        status.setText("Personagem selecionado: "+SpriteStore.SLOTS[index]);
    }

    private void openGallery() {
        Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent,PICK_SPRITE);
    }

    @Override protected void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);
        if(request!=PICK_SPRITE||result!=RESULT_OK||data==null||data.getData()==null)return;
        Uri uri=data.getData();
        setBusy(true);
        new Thread(() -> {
            try {
                sprites.importUri(selectedSlot,uri);
                runOnUiThread(() -> {
                    setBusy(false);preview.invalidate();
                    status.setText("Imagem importada para "+SpriteStore.SLOTS[selectedSlot]+".");
                });
            }catch(Exception e) {
                runOnUiThread(() -> {setBusy(false);status.setText("Erro no PNG: "+e.getMessage());});
            }
        }).start();
    }

    private void generateStory() {
        if(busy)return;
        final String idea=prompt.getText().toString().trim();
        if(idea.isEmpty()){status.setText("Escreva sua ideia primeiro.");return;}
        prefs.edit().putString("last_prompt",idea).apply();
        String key=prefs.getString("gemini_key","");
        int secs=seconds;
        setBusy(true);
        status.setText(key.isEmpty()?"Criando animação demonstrativa..."
            :"A IA está escrevendo e organizando as cenas...");
        new Thread(() -> {
            try {
                StoryPlan result=key.isEmpty()?StoryPlan.offline(idea,secs)
                    :AiPlanner.generate(key,idea,secs);
                runOnUiThread(() -> {
                    story=result;
                    preview.setStory(story);
                    setBusy(false);
                    status.setText((result.generatedByAi?"Roteiro Gemini":"Demonstração local")
                        +" pronto! "+result.scenes.size()+" cenas, "+secs+" s.");
                });
            }catch(Exception e){
                runOnUiThread(() -> {
                    setBusy(false);
                    status.setText("Não foi possível gerar: "+e.getMessage());
                });
            }
        }).start();
    }

    private void exportVideo() {
        if(busy||story==null)return;
        StoryPlan project=story;
        boolean vertical=portrait;
        setBusy(true);
        video=null;share.setVisibility(View.GONE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        status.setText("Renderizando... 0%. Mantenha o aplicativo aberto.");
        new Thread(() -> {
            try {
                Uri ready=Mp4Exporter.render(getApplicationContext(),project,sprites,vertical,
                    percent -> runOnUiThread(() ->
                        status.setText("Renderizando... "+percent+"%. Mantenha o aplicativo aberto.")));
                runOnUiThread(() -> {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    video=ready;setBusy(false);
                    share.setVisibility(View.VISIBLE);
                    status.setText("✅ MP4 pronto em Filmes/AnimaIA! Você já pode compartilhar.");
                });
            }catch(Exception e){
                runOnUiThread(() -> {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    setBusy(false);
                    status.setText("Falha ao exportar: "+e.getMessage());
                });
            }
        }).start();
    }

    private void shareVideo() {
        if(video==null)return;
        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.setType("video/mp4");
        intent.putExtra(Intent.EXTRA_STREAM,video);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent,"Compartilhar animação"));
    }

    private void setBusy(boolean value) {
        busy=value;
        generate.setEnabled(!busy);
        export.setEnabled(!busy);
        importSprite.setEnabled(!busy);
        searchSprite.setEnabled(!busy);
    }
}
