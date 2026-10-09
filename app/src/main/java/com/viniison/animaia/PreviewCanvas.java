package com.viniison.animaia;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.view.View;

public final class PreviewCanvas extends View {
    private final Paint background=new Paint();
    private final AnimationRenderer renderer=new AnimationRenderer();
    private final SpriteStore store;
    private StoryPlan story=StoryPlan.offline("Um herói encontra um rival misterioso",12);
    private boolean portrait=false;
    private long started=SystemClock.uptimeMillis();

    public PreviewCanvas(Context context,SpriteStore sprites) {
        super(context);store=sprites;
        setLayerType(View.LAYER_TYPE_SOFTWARE,null);
    }

    public void setStory(StoryPlan plan) {
        if(plan!=null)story=plan;
        started=SystemClock.uptimeMillis();
        invalidate();
    }

    public void setPortrait(boolean value) {
        portrait=value;invalidate();
    }

    public void restart() { started=SystemClock.uptimeMillis();invalidate(); }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w=getWidth(),h=getHeight();
        if(w<=0||h<=0)return;
        background.setColor(0xff080b19);
        canvas.drawRoundRect(0,0,w,h,20,20,background);
        float availableW=w-18,availableH=h-18;
        float ratio=portrait?9f/16f:16f/9f;
        float videoW=Math.min(availableW,availableH*ratio);
        float videoH=videoW/ratio;
        float left=(w-videoW)/2,top=(h-videoH)/2;
        int baseW=portrait?432:768,baseH=portrait?768:432;
        canvas.save();
        canvas.translate(left,top);
        canvas.scale(videoW/baseW,videoH/baseH);
        double time=((SystemClock.uptimeMillis()-started)/1000.0)%story.durationSeconds;
        renderer.render(canvas,baseW,baseH,time,story,store);
        canvas.restore();
        if(getWindowToken()!=null)postInvalidateDelayed(60);
    }
}
