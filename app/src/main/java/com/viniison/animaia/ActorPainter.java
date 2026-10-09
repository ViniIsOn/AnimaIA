package com.viniison.animaia;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

final class ActorPainter {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

    void draw(Canvas c,int w,int h,String action,double s,double t,SpriteStore store) {
        float ground=h*.795f;
        float x1=w*.27f,x2=w*.72f, y1=ground,y2=ground;
        float scale=Math.min(w*(w<h?.27f:.16f),h*.37f);
        boolean flip1=false,flip2=true;
        float rot1=0,rot2=0;
        float bounce=(float)Math.sin(t*15)*scale*.027f;
        switch(action) {
            case "run":
                x1=(float)(w*(.14+.65*s));y1+=bounce;
                x2=w*.76f;y2+=bounce*.7f;break;
            case "chase":
                x1=(float)(w*(.10+.53*s));x2=(float)(w*(.42+.45*s));
                y1+=bounce;y2-=bounce;break;
            case "jump":
                x1=(float)(w*(.16+.52*s));y1-=(float)(h*.34*Math.sin(Math.PI*s));
                y2+=bounce;break;
            case "fly":
                x1=(float)(w*(.11+.70*s));y1=h*.38f+(float)(Math.sin(s*Math.PI*4)*h*.05);
                x2=w*.72f;y2=h*.62f;rot1=-15;break;
            case "dance":
                rot1=(float)(Math.sin(s*Math.PI*8)*16);
                rot2=(float)(Math.sin(s*Math.PI*8+Math.PI)*14);
                y1-=Math.abs(Math.sin(s*Math.PI*8))*h*.06;
                y2-=Math.abs(Math.cos(s*Math.PI*8))*h*.06;break;
            case "talk":
                y1+=Math.sin(t*6)*scale*.025;
                y2+=Math.sin(t*5)*scale*.018;break;
            case "battle":
                x1=(float)(w*(.24+.15*Math.sin(s*Math.PI)));
                x2=(float)(w*(.75-.16*Math.sin(s*Math.PI)));
                rot1=(float)(Math.sin(s*12)*8);
                rot2=(float)(-Math.sin(s*12)*8);
                if(s>.35 && s<.6) sparkle(c,w*.51f,h*.52f,w*.07f,s);
                break;
            default:
                y1+=Math.sin(t*3)*scale*.012;
                y2+=Math.sin(t*2)*scale*.014;
        }
        sprite(c,store.get(0),x1,y1,scale,flip1,rot1,0);
        sprite(c,store.get(1),x2,y2,scale*.92f,flip2,rot2,1);
        if(store.get(2)!=null) {
            sprite(c,store.get(2),w*.50f,ground+(float)Math.sin(t*3)*scale*.018f,
                scale*.68f,false,0,2);
        }
    }

    private void sparkle(Canvas c,float x,float y,float r,double s) {
        p.setColor(0xffffed69);
        for(int i=0;i<9;i++) {
            double a=Math.PI*2*i/9+s*2;
            c.drawLine(x+(float)Math.cos(a)*r*.38f,y+(float)Math.sin(a)*r*.38f,
                x+(float)Math.cos(a)*r,y+(float)Math.sin(a)*r,p);
        }
        p.setColor(0xffffb86b);
        c.drawCircle(x,y,r*.34f,p);
    }

    private void sprite(Canvas c,Bitmap image,float cx,float foot,float size,boolean flip,
                        float rotation,int who) {
        p.setColor(0x77090d22);
        c.drawOval(cx-size*.36f,foot-size*.03f,cx+size*.36f,foot+size*.05f,p);
        c.save();
        c.translate(cx,foot-size*.55f);
        c.rotate(rotation);
        if(flip)c.scale(-1,1);
        if(image!=null&&!image.isRecycled()) {
            float ratio=(float)image.getWidth()/Math.max(1,image.getHeight());
            float bw=ratio>1?size:ratio*size;
            float bh=ratio>1?size/ratio:size;
            p.setColor(Color.WHITE);
            p.setFilterBitmap(false);
            c.drawBitmap(image,null,new RectF(-bw/2,-bh/2,bw/2,bh/2),p);
        } else pixelHero(c,size,who);
        c.restore();
    }

    private void pixelHero(Canvas c,float size,int who) {
        float u=size/14f;
        c.save();c.translate(-7*u,-7*u);
        int primary=who==0?0xff7c6af6:0xfff3a44e;
        int secondary=who==0?0xff4ddac5:0xfffe738c;
        rect(c,3,3,11,12,u,primary);
        rect(c,3,0,11,7,u,secondary);
        rect(c,2,1,12,4,u,0xff1b2348);
        rect(c,4,4,6,6,u,Color.WHITE);
        rect(c,8,4,10,6,u,Color.WHITE);
        rect(c,5,5,6,6,u,0xff141b32);
        rect(c,9,5,10,6,u,0xff141b32);
        rect(c,1,8,3,12,u,secondary);
        rect(c,11,8,13,12,u,secondary);
        rect(c,4,12,6,14,u,0xff1b2348);
        rect(c,8,12,10,14,u,0xff1b2348);
        c.restore();
    }

    private void rect(Canvas c,int x,int y,int x2,int y2,float u,int color) {
        p.setColor(color);c.drawRect(x*u,y*u,x2*u,y2*u,p);
    }
}
