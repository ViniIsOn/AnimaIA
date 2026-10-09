package com.viniison.animaia;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import java.nio.ByteBuffer;

public final class Mp4Exporter {
    public interface Progress { void onProgress(int percent); }
    private static final int FPS=18;

    public static Uri render(Context context,StoryPlan story,SpriteStore sprites,
                             boolean portrait,Progress progress) throws Exception {
        int width=portrait?432:768,height=portrait?768:432;
        ContentValues values=new ContentValues();
        values.put(MediaStore.Video.Media.DISPLAY_NAME,"AnimaIA_"+System.currentTimeMillis()+".mp4");
        values.put(MediaStore.Video.Media.MIME_TYPE,"video/mp4");
        values.put(MediaStore.Video.Media.RELATIVE_PATH,Environment.DIRECTORY_MOVIES+"/AnimaIA");
        values.put(MediaStore.Video.Media.IS_PENDING,1);
        Uri uri=context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,values);
        if(uri==null) throw new IllegalStateException("Falha ao criar o arquivo no dispositivo.");
        MediaCodec codec=null;
        MediaMuxer muxer=null;
        ParcelFileDescriptor fd=null;
        Bitmap frame=null;
        boolean started=false,ok=false;
        try {
            YuvEncoder.Choice choice=YuvEncoder.choose();
            if(choice==null) throw new IllegalStateException("Nenhum encoder H.264 YUV420 compatível encontrado.");
            MediaFormat format=MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,width,height);
            format.setInteger(MediaFormat.KEY_BIT_RATE,1600000);
            format.setInteger(MediaFormat.KEY_FRAME_RATE,FPS);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,2);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT,choice.format);
            codec=MediaCodec.createByCodecName(choice.name);
            codec.configure(format,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
            codec.start();
            fd=context.getContentResolver().openFileDescriptor(uri,"rw");
            if(fd==null) throw new IllegalStateException("Não foi possível abrir o vídeo.");
            muxer=new MediaMuxer(fd.getFileDescriptor(),MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            frame=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
            Canvas canvas=new Canvas(frame);
            AnimationRenderer renderer=new AnimationRenderer();
            int[] rgb=new int[width*height];
            byte[] yuv=new byte[width*height*3/2];
            int total=story.durationSeconds*FPS,count=0,track=-1;
            boolean endInput=false,endOutput=false;
            int stalled=0;
            MediaCodec.BufferInfo info=new MediaCodec.BufferInfo();
            while(!endOutput) {
                if(!endInput) {
                    int index=codec.dequeueInputBuffer(10000);
                    if(index>=0) {
                        if(count>=total) {
                            codec.queueInputBuffer(index,0,0,total*1000000L/FPS,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            endInput=true;
                        } else {
                            renderer.render(canvas,width,height,count/(double)FPS,story,sprites);
                            frame.getPixels(rgb,0,width,0,0,width,height);
                            YuvEncoder.convert(rgb,yuv,width,height,choice.format);
                            ByteBuffer in=codec.getInputBuffer(index);
                            if(in==null||in.capacity()<yuv.length)
                                throw new IllegalStateException("O buffer do encoder é pequeno demais.");
                            in.clear();in.put(yuv);
                            codec.queueInputBuffer(index,0,yuv.length,count*1000000L/FPS,0);
                            count++;
                            if(count%9==0) progress.onProgress(count*100/total);
                        }
                    }
                }
                int output=codec.dequeueOutputBuffer(info,10000);
                if(output==MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    if(started) throw new IllegalStateException("Formato inesperado do vídeo.");
                    track=muxer.addTrack(codec.getOutputFormat());
                    muxer.start();started=true;stalled=0;
                } else if(output>=0) {
                    stalled=0;
                    ByteBuffer data=codec.getOutputBuffer(output);
                    if((info.flags&MediaCodec.BUFFER_FLAG_CODEC_CONFIG)!=0) info.size=0;
                    if(info.size>0) {
                        if(!started||data==null) throw new IllegalStateException("O codec não iniciou.");
                        data.position(info.offset);data.limit(info.offset+info.size);
                        muxer.writeSampleData(track,data,info);
                    }
                    endOutput=(info.flags&MediaCodec.BUFFER_FLAG_END_OF_STREAM)!=0;
                    codec.releaseOutputBuffer(output,false);
                } else if(++stalled>10000) {
                    throw new IllegalStateException("Tempo limite do encoder excedido.");
                }
            }
            muxer.stop();started=false;
            values.clear();values.put(MediaStore.Video.Media.IS_PENDING,0);
            context.getContentResolver().update(uri,values,null,null);
            progress.onProgress(100);
            ok=true;
            return uri;
        } finally {
            if(frame!=null) frame.recycle();
            if(codec!=null) {
                try {codec.stop();}catch(Exception ignored){}
                codec.release();
            }
            if(muxer!=null) {
                if(started) try {muxer.stop();}catch(Exception ignored){}
                try {muxer.release();}catch(Exception ignored){}
            }
            if(fd!=null) try {fd.close();}catch(Exception ignored){}
            if(!ok) context.getContentResolver().delete(uri,null,null);
        }
    }
}
