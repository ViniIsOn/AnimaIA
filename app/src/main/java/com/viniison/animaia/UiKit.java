package com.viniison.animaia;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Gravity;

final class UiKit {
    static final int BG=0xff0c1024, CARD=0xff181f3a, FG=0xfff3f3ff;
    static final int MUTED=0xffa7b2d3, VIOLET=0xff8565e9, CYAN=0xff5ddaca;

    static int dp(Context c,float size) {
        return (int)(size*c.getResources().getDisplayMetrics().density+.5f);
    }

    static GradientDrawable shape(int color,int radius,Context ctx) {
        GradientDrawable d=new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(ctx,radius));
        return d;
    }

    static TextView label(Context ctx,String text,int sp,boolean bold) {
        TextView out=new TextView(ctx);
        out.setText(text);out.setTextColor(FG);out.setTextSize(sp);
        if(bold)out.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        return out;
    }

    static TextView muted(Context ctx,String text,int sp) {
        TextView out=label(ctx,text,sp,false);
        out.setTextColor(MUTED);
        out.setLineSpacing(dp(ctx,3),1f);
        return out;
    }

    static Button button(Context ctx,String title,boolean primary) {
        Button b=new Button(ctx);
        b.setText(title);
        b.setAllCaps(false);
        b.setTextSize(13);
        b.setTextColor(primary?Color.WHITE:FG);
        b.setBackground(shape(primary?VIOLET:CARD,14,ctx));
        b.setMinHeight(dp(ctx,46));b.setMinimumHeight(dp(ctx,46));
        b.setElevation(dp(ctx,2));
        return b;
    }

    static EditText input(Context ctx,String hint) {
        EditText input=new EditText(ctx);
        input.setSingleLine(true);
        input.setTextColor(FG);
        input.setHintTextColor(0xff7f8bab);
        input.setTextSize(14);
        input.setHint(hint);
        input.setPadding(dp(ctx,14),dp(ctx,12),dp(ctx,14),dp(ctx,12));
        input.setBackground(shape(CARD,14,ctx));
        return input;
    }

    static LinearLayout vertical(Context ctx) {
        LinearLayout layout=new LinearLayout(ctx);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    static LinearLayout horizontal(Context ctx) {
        LinearLayout layout=new LinearLayout(ctx);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        return layout;
    }

    static void add(LinearLayout box,View item,int topMargin) {
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin=dp(box.getContext(),topMargin);
        box.addView(item,lp);
    }

    static void addWeighted(LinearLayout row,View item,float weight,int gap) {
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,
            LinearLayout.LayoutParams.WRAP_CONTENT,weight);
        lp.setMargins(dp(row.getContext(),gap),0,0,0);
        row.addView(item,lp);
    }
}
