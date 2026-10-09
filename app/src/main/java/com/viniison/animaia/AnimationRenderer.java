package com.viniison.animaia;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

public final class AnimationRenderer {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final ScenePainter backgrounds=new ScenePainter();
    private final ActorPainter actors=new ActorPainter();

    public void render(Canvas c,int width,int height,double seconds,StoryPlan plan,SpriteStore store) {
        c.save();
        c.clipRect(0,0,width,height);
        double t=Math.max(0,Math.min(plan.durationSeconds-.0001,seconds));
        StoryPlan.Scene scene=plan.at(t);
        backgrounds.draw(c,width,height,scene.background,t);
        actors.draw(c,width,height,scene.action,plan.sceneProgress(t),t,store);
        overlay(c,width,height,plan,scene,t);
        c.restore();
    }

    private void overlay(Canvas c,int w,int h,StoryPlan plan,StoryPlan.Scene scene,double t) {
        float pad=w*.045f;
        p.setTypeface(Typeface.create("sans-serif-black",Typeface.BOLD));
        p.setTextSize(Math.max(13,Math.min(w,h)*.025f));
        p.setColor(0xccffffff);
        c.drawText("ANIMAIA  /  PIXEL STORIES",pad,h*.06f,p);
        float inset=w*.065f,barY=h*.945f;
        p.setColor(0x66232953);
        c.drawRoundRect(inset,barY,w-inset,barY+h*.012f,h*.006f,h*.006f,p);
        p.setColor(0xff67e8db);
        c.drawRoundRect(inset,barY,inset+(w-inset*2)*(float)(t/plan.durationSeconds),
            barY+h*.012f,h*.006f,h*.006f,p);
        p.setColor(0xe2070e29);
        float top=h*.815f,bot=h*.925f;
        c.drawRoundRect(pad,top,w-pad,bot,h*.025f,h*.025f,p);
        p.setColor(0xffc5b5ff);
        c.drawRoundRect(pad,top,pad+w*.010f,bot,h*.01f,h*.01f,p);
        p.setColor(Color.WHITE);
        p.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));
        float textSize=Math.max(15,Math.min(w,h)*(w<h?.041f:.035f));
        p.setTextSize(textSize);
        drawWrapped(c,scene.caption,pad+w*.035f,top+h*.043f,
            w-pad*2-w*.07f,textSize*1.23f,2);
    }

    private void drawWrapped(Canvas c,String text,float x,float y,float maxWidth,
                             float step,int maxLines) {
        String rest=text==null?"":text;
        for(int i=0;i<maxLines&&!rest.isEmpty();i++) {
            int count=p.breakText(rest,true,maxWidth,null);
            if(count<=0)break;
            if(count<rest.length()) {
                int space=rest.lastIndexOf(' ',count);
                if(space>0)count=space;
            }
            c.drawText(rest.substring(0,count).trim(),x,y+i*step,p);
            rest=rest.substring(count).trim();
        }
    }
}
