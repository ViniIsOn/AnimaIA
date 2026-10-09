package com.viniison.animaia;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;

public final class SpriteStore {
    public static final String[] SLOTS = { "Herói", "Rival", "Amigo" };
    private final Context context;
    private final SharedPreferences prefs;
    private final Bitmap[] sprites = new Bitmap[3];

    SpriteStore(Context context) {
        this.context = context.getApplicationContext();
        prefs = context.getSharedPreferences("animaia", Context.MODE_PRIVATE);
        for (int i=0;i<3;i++) {
            File file = new File(context.getFilesDir(), "sprite" + i + ".png");
            if (file.exists()) sprites[i] = BitmapFactory.decodeFile(file.getAbsolutePath());
        }
    }

    public Bitmap get(int slot) {
        return slot >= 0 && slot < sprites.length ? sprites[slot] : null;
    }

    public void clear(int slot) {
        if (slot < 0 || slot >= 3) return;
        File f = new File(context.getFilesDir(), "sprite" + slot + ".png");
        if (f.exists()) f.delete();
        sprites[slot] = null;
    }

    public void importUri(int slot, Uri uri) throws Exception {
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            if (in == null) throw new IllegalArgumentException("Não foi possível abrir a imagem.");
            loadAndSave(slot, in);
        }
    }

    public void importFromHttps(int slot, String url) throws Exception {
        if (!url.startsWith("https://")) throw new IllegalArgumentException("A imagem precisa usar HTTPS.");
        java.net.HttpURLConnection connection =
            (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
        try {
            connection.setConnectTimeout(12000);
            connection.setReadTimeout(20000);
            connection.setRequestProperty("User-Agent", "AnimaIA/0.1");
            if (connection.getResponseCode() != 200) throw new IllegalStateException("Imagem indisponível.");
            if (connection.getContentLengthLong() > 8 * 1024 * 1024)
                throw new IllegalStateException("Imagem maior que 8 MB.");
            try (InputStream in = connection.getInputStream()) {
                loadAndSave(slot, in);
            }
        } finally {
            connection.disconnect();
        }
    }

    private void loadAndSave(int slot, InputStream input) throws Exception {
        if (slot < 0 || slot >= 3) throw new IllegalArgumentException("Personagem inválido.");
        byte[] bytes = new byte[8 * 1024 * 1024];
        int used = 0, read;
        while ((read = input.read(bytes, used, bytes.length-used)) > 0) {
            used += read;
            if (used == bytes.length) throw new IllegalArgumentException("Imagem muito grande (máx. 8 MB).");
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, used, options);
        int sample = 1;
        while (options.outWidth / sample > 1000 || options.outHeight / sample > 1000) sample *= 2;
        options.inJustDecodeBounds = false;
        options.inSampleSize = sample;
        Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, used, options);
        if (image == null) throw new IllegalArgumentException("Formato de imagem não suportado.");
        Bitmap transformed = trimAndClean(image);
        if (transformed != image) image.recycle();
        File file = new File(context.getFilesDir(), "sprite" + slot + ".png");
        try (FileOutputStream out = new FileOutputStream(file)) {
            if (!transformed.compress(Bitmap.CompressFormat.PNG, 100, out))
                throw new IllegalStateException("Não foi possível salvar o sprite.");
        }
        if (sprites[slot] != null && sprites[slot] != transformed) sprites[slot].recycle();
        sprites[slot] = transformed;
    }

    private Bitmap trimAndClean(Bitmap original) {
        int w = original.getWidth(), h = original.getHeight();
        boolean whiteCorner = nearWhite(original.getPixel(0,0))
            && nearWhite(original.getPixel(w-1,0))
            && nearWhite(original.getPixel(0,h-1))
            && nearWhite(original.getPixel(w-1,h-1));
        int[] colors = new int[w*h];
        original.getPixels(colors, 0, w, 0, 0, w, h);
        if (whiteCorner) {
            for (int i=0;i<colors.length;i++) if (nearWhite(colors[i]))
                colors[i] = Color.TRANSPARENT;
        }
        int left=w, right=-1, top=h, bottom=-1;
        for (int y=0;y<h;y++) for (int x=0;x<w;x++) {
            if (Color.alpha(colors[y*w+x]) > 16) {
                if (x<left) left=x;
                if (x>right) right=x;
                if (y<top) top=y;
                if (y>bottom) bottom=y;
            }
        }
        if (right < left) return original;
        Bitmap out = Bitmap.createBitmap(right-left+1, bottom-top+1, Bitmap.Config.ARGB_8888);
        int[] cut = new int[(right-left+1)*(bottom-top+1)];
        for (int y=top;y<=bottom;y++) System.arraycopy(colors, y*w+left,
                cut, (y-top)*(right-left+1), right-left+1);
        out.setPixels(cut, 0, right-left+1, 0, 0, right-left+1, bottom-top+1);
        return out;
    }

    private boolean nearWhite(int c) {
        return Color.red(c)>246 && Color.green(c)>246 && Color.blue(c)>246;
    }
}
