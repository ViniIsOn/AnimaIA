package com.viniison.animaia;

import android.app.Activity;
import android.app.AlertDialog;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;

final class SceneEditor {
    private static final String[] BACKGROUNDS={"Cidade","Pôr do sol","Floresta","Espaço","Noite","Praia"};
    private static final String[] BACKGROUND_VALUES={"city","sunset","forest","space","night","beach"};
    private static final String[] ACTIONS={"Correr","Pular","Perseguir","Voar","Dançar","Conversar","Batalha","Parado"};
    private static final String[] ACTION_VALUES={"run","jump","chase","fly","dance","talk","battle","idle"};

    static void show(Activity activity, StoryPlan story, Runnable onSave) {
        LinearLayout box=UiKit.vertical(activity);
        box.setPadding(UiKit.dp(activity,15),UiKit.dp(activity,10),
                       UiKit.dp(activity,15),UiKit.dp(activity,12));
        UiKit.add(box,UiKit.label(activity,"🎞 Editar cenas",20,true),0);
        UiKit.add(box,UiKit.muted(activity,
                "Toque em uma cena para escolher movimento, cenário e legenda. "
                +"A prévia e a exportação usarão suas alterações.",12),8);
        LinearLayout list=UiKit.vertical(activity);
        ScrollView scroll=new ScrollView(activity);
        scroll.addView(list);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,UiKit.dp(activity,380));
        lp.topMargin=UiKit.dp(activity,8);
        box.addView(scroll,lp);
        AlertDialog dialog=new AlertDialog.Builder(activity).setView(box)
                .setPositiveButton("Pronto",(d,w)->onSave.run()).create();
        box.setBackgroundColor(UiKit.BG);
        dialog.show();
        populate(activity,list,story,onSave);
    }

    private static void populate(Activity activity, LinearLayout list,
                                 StoryPlan story, Runnable onSave) {
        list.removeAllViews();
        for(int i=0;i<story.scenes.size();i++){
            final int index=i;
            StoryPlan.Scene scene=story.scenes.get(index);
            Button row=UiKit.button(activity,(i+1)+". "+scene.caption,false);
            row.setSingleLine(false);
            row.setMaxLines(3);
            UiKit.add(list,row,7);
            row.setOnClickListener(v -> edit(activity,story,index,
                    () -> {populate(activity,list,story,onSave);onSave.run();}));
        }
        if(story.scenes.size()<6){
            Button add=UiKit.button(activity,"＋ Adicionar cena",true);
            UiKit.add(list,add,10);
            add.setOnClickListener(v -> {
                story.scenes.add(new StoryPlan.Scene("city","run","Uma nova cena começa!"));
                populate(activity,list,story,onSave);onSave.run();
            });
        }
    }

    private static void edit(Activity activity, StoryPlan story,int index,Runnable refresh) {
        StoryPlan.Scene current=story.scenes.get(index);
        LinearLayout layout=UiKit.vertical(activity);
        layout.setPadding(UiKit.dp(activity,18),UiKit.dp(activity,10),
                UiKit.dp(activity,18),UiKit.dp(activity,10));

        UiKit.add(layout,UiKit.label(activity,"Legenda",14,true),0);
        EditText caption=UiKit.input(activity,"Texto da cena");
        caption.setText(current.caption);
        caption.setSingleLine(false);
        caption.setMinLines(2);
        UiKit.add(layout,caption,7);

        UiKit.add(layout,UiKit.label(activity,"Cenário",14,true),15);
        Spinner background=spinner(activity,BACKGROUNDS);
        background.setSelection(indexOf(BACKGROUND_VALUES,current.background));
        UiKit.add(layout,background,6);

        UiKit.add(layout,UiKit.label(activity,"Ação",14,true),15);
        Spinner action=spinner(activity,ACTIONS);
        action.setSelection(indexOf(ACTION_VALUES,current.action));
        UiKit.add(layout,action,6);

        AlertDialog.Builder builder=new AlertDialog.Builder(activity)
            .setTitle("Cena "+(index+1))
            .setView(layout)
            .setPositiveButton("Salvar",(d,w)-> {
                String text=caption.getText().toString().trim();
                if(text.isEmpty())text="...";
                if(text.length()>110)text=text.substring(0,110);
                story.scenes.set(index,new StoryPlan.Scene(
                    BACKGROUND_VALUES[background.getSelectedItemPosition()],
                    ACTION_VALUES[action.getSelectedItemPosition()],text));
                refresh.run();
            })
            .setNegativeButton("Cancelar",(d,w)->{});
        if(story.scenes.size()>2) {
            builder.setNeutralButton("Excluir cena",(d,w)-> {
                story.scenes.remove(index);
                refresh.run();
            });
        }
        builder.show();
    }

    private static int indexOf(String[] values,String what) {
        for(int i=0;i<values.length;i++)if(values[i].equals(what))return i;
        return 0;
    }

    private static Spinner spinner(Activity activity,String[] names) {
        Spinner spinner=new Spinner(activity);
        ArrayAdapter<String> adapter=new ArrayAdapter<>(
                activity,android.R.layout.simple_spinner_dropdown_item,names);
        spinner.setAdapter(adapter);
        return spinner;
    }
}
