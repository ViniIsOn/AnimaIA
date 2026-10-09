package com.viniison.animaia;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;

final class ScenePainter {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

    void draw(Canvas canvas, int w, int h, String theme, double t) {
        int top=0xff18254e, bottom=0xff6557b1;
        switch(theme) {
            case "space": top=0xff060b24;bottom=0xff243d78;break;
            case "sunset": top=0xfff65c7a;bottom=0xfffead71;break;
            case "forest": top=0xff123d57;bottom=0xff73c9a1;break;
            case "beach": top=0xff1a86bf;bottom=0xff9ce8ec;break;
            case "night": top=0xff060b23;bottom=0xff554191;break;
        }
        p.setShader(new LinearGradient(0,0,0,h,top,bottom,Shader.TileMode.CLAMP));
        canvas.drawRect(0,0,w,h,p);p.setShader(null);
        if(theme.equals("night")||theme.equals("space")) {
            for(int i=0;i<44;i++) {
                int x=(i*173+19)%w, y=(i*137+57)%(int)(h*.79);
                p.setColor(i%5==0?0xfffff7ca:0xffd9e5ff);
                float r=(float)(Math.min(w,h)*(.0015+(i%3)*.001));
                p.setAlpha(120+(int)(100*Math.abs(Math.sin(t*1.3+i))));
                canvas.drawCircle(x,y,r,p);
            }
            p.setAlpha(255);
        } else {
            p.setColor(theme.equals("sunset")?0xffffe5a5:0xfffff8c3);
            canvas.drawCircle(w*.76f,h*.23f,Math.min(w,h)*.09f,p);
            for(int i=0;i<4;i++) {
                float x=(float)((w*(i*.33+.10)+t*13)%(w*1.4)-w*.2);
                cloud(canvas,x,h*(.12f+i*.055f),w*.13f);
            }
        }
        if(theme.equals("space")) {
            p.setColor(0xff2a4782);
            canvas.drawCircle(w*.50f,h*.92f,Math.max(w,h)*.42f,p);
            p.setColor(0xff354f91);
            canvas.drawCircle(w*.47f,h*.93f,Math.max(w,h)*.39f,p);
        } else {
            p.setColor(theme.equals("forest")?0xff235e65:0xff445e9a);
            Path hills = new Path();
            hills.moveTo(0,h*.78f);
            for(int i=0;i<=6;i++) hills.lineTo((float)w*i/6,h*(.52f+((i%2==0)?.08f:.19f)));
            hills.lineTo(w,h);hills.lineTo(0,h);hills.close();canvas.drawPath(hills,p);
            if(theme.equals("forest")||theme.equals("beach")) {
                for(int i=0;i<6;i++) {
                    float x=w*(i+.12f)/6, bottomY=h*.82f;
                    p.setColor(theme.equals("forest")?0xff493c65:0xff845b59);
                    canvas.drawRect(x-w*.012f,bottomY-h*.22f,x+w*.012f,bottomY,p);
                    p.setColor(theme.equals("forest")?0xff2e9e7e:0xff48bf8f);
                    float rr=w*.048f;
                    canvas.drawCircle(x,bottomY-h*.24f,rr,p);
                    canvas.drawCircle(x-rr*.6f,bottomY-h*.215f,rr*.7f,p);
                    canvas.drawCircle(x+rr*.6f,bottomY-h*.215f,rr*.7f,p);
                }
            }
            p.setColor(theme.equals("beach")?0xffffdba4:0xff262c57);
            canvas.drawRect(0,h*.80f,w,h,p);
            p.setColor(theme.equals("beach")?0xffd39f73:0xff3b4776);
            for(int i=0;i<Math.max(8,w/32);i++) {
                float x=(float)(i*37 % w), y=(float)(h*(.82+(i%4)*.05));
                canvas.drawRect(x,y,x+w*.018f,y+h*.006f,p);
            }
        }
        p.setColor(0x28ffffff);
        for(int i=0;i<7;i++) {
            float x=(float)((w*(i*.17)-t*65)%w);
            if(x<0)x+=w;
            canvas.drawRoundRect(x,h*.86f,x+w*.03f,h*.865f,w*.002f,w*.002f,p);
        }
    }

    private void cloud(Canvas c,float x,float y,float r) {
        p.setColor(0x66ffffff);
        c.drawOval(x-r,y-r*.19f,x+r,y+r*.28f,p);
        c.drawCircle(x-r*.34f,y,r*.36f,p);
        c.drawCircle(x+r*.10f,y-r*.16f,r*.43f,p);
    }
}
