package com.viniison.animaia;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;

final class YuvEncoder {
    static final class Choice {
        final String name;
        final int format;
        Choice(String n,int f){ name=n; format=f; }
    }

    static Choice choose() {
        Choice alternative=null;
        for(MediaCodecInfo codec:new MediaCodecList(MediaCodecList.REGULAR_CODECS).getCodecInfos()) {
            if(!codec.isEncoder()) continue;
            boolean supports=false;
            for(String type:codec.getSupportedTypes())
                if(MediaFormat.MIMETYPE_VIDEO_AVC.equalsIgnoreCase(type)) supports=true;
            if(!supports) continue;
            try {
                int[] formats=codec.getCapabilitiesForType(MediaFormat.MIMETYPE_VIDEO_AVC).colorFormats;
                for(int color:formats) {
                    if(color==MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar ||
                       color==MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar) {
                        Choice found=new Choice(codec.getName(),color);
                        if(codec.isHardwareAccelerated()) return found;
                        if(alternative==null) alternative=found;
                    }
                }
            } catch(Exception ignored) { }
        }
        return alternative;
    }

    private static int y(int r,int g,int b) { return clamp(((66*r+129*g+25*b+128)>>8)+16); }
    private static int u(int r,int g,int b) { return clamp(((-38*r-74*g+112*b+128)>>8)+128); }
    private static int v(int r,int g,int b) { return clamp(((112*r-94*g-18*b+128)>>8)+128); }
    private static int clamp(int val) { return Math.max(0,Math.min(255,val)); }

    static void convert(int[] rgb,byte[] out,int w,int h,int format) {
        int ySize=w*h,uvIndex=ySize,vIndex=ySize+ySize/4;
        boolean semiplanar=format==MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
        for(int row=0;row<h;row+=2) for(int col=0;col<w;col+=2) {
            int rsum=0,gsum=0,bsum=0;
            for(int dy=0;dy<2;dy++) for(int dx=0;dx<2;dx++) {
                int pos=(row+dy)*w+col+dx;
                int c=rgb[pos],r=(c>>16)&255,g=(c>>8)&255,b=c&255;
                out[pos]=(byte)y(r,g,b);
                rsum+=r;gsum+=g;bsum+=b;
            }
            byte U=(byte)u(rsum/4,gsum/4,bsum/4), V=(byte)v(rsum/4,gsum/4,bsum/4);
            if(semiplanar) {
                out[uvIndex++]=U;out[uvIndex++]=V;
            } else {
                out[uvIndex++]=U;out[vIndex++]=V;
            }
        }
    }
}
