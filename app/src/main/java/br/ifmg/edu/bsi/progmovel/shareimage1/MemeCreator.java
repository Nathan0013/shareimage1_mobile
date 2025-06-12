package br.ifmg.edu.bsi.progmovel.shareimage1;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.DisplayMetrics;

public class MemeCreator {
    private String texto;
    private int corTexto;
    private float tamanhoTexto;
    private String textoSuperior;
    private int corTextoSuperior;
    private float tamanhoTextoSuperior;

    // Questão 4: Variáveis para guardar a posição de cada texto.
    private float textoSuperiorX, textoSuperiorY;
    private float textoX, textoY;

    private Bitmap fundo;
    private DisplayMetrics displayMetrics;
    private Bitmap meme;
    private boolean dirty;

    public MemeCreator(String texto, int corTexto, Bitmap fundo, DisplayMetrics displayMetrics) {
        this.texto = texto;
        this.corTexto = corTexto;
        this.tamanhoTexto = 64f;
        this.textoSuperior = "";
        this.corTextoSuperior = Color.WHITE;
        this.tamanhoTextoSuperior = 64f;
        this.fundo = fundo;
        this.displayMetrics = displayMetrics;
        this.meme = criarImagem();
        this.dirty = false;
    }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; dirty = true; }
    public int getCorTexto() { return corTexto; }
    public void setCorTexto(int corTexto) { this.corTexto = corTexto; dirty = true; }
    public float getTamanhoTexto() { return this.tamanhoTexto; }
    public void setTamanhoTexto(float tamanhoTexto) { this.tamanhoTexto = tamanhoTexto; dirty = true; }
    public String getTextoSuperior() { return textoSuperior; }
    public void setTextoSuperior(String texto) { this.textoSuperior = texto; dirty = true; }
    public int getCorTextoSuperior() { return corTextoSuperior; }
    public void setCorTextoSuperior(int corTexto) { this.corTextoSuperior = corTexto; dirty = true; }
    public float getTamanhoTextoSuperior() { return this.tamanhoTextoSuperior; }
    public void setTamanhoTextoSuperior(float tamanhoTexto) { this.tamanhoTextoSuperior = tamanhoTexto; dirty = true; }

    // Questão 4: Métodos para definir a nova posição dos textos.
    public void setPosicaoTexto(float x, float y) {
        this.textoX = x;
        this.textoY = y;
        this.dirty = true;
    }

    public void setPosicaoTextoSuperior(float x, float y) {
        this.textoSuperiorX = x;
        this.textoSuperiorY = y;
        this.dirty = true;
    }

    public Bitmap getFundo() { return fundo; }
    public void setFundo(Bitmap fundo) { this.fundo = fundo; dirty = true; }

    public void rotacionarFundo(float graus) {
        Matrix matrix = new Matrix();
        matrix.postRotate(graus);
        fundo = Bitmap.createBitmap(fundo, 0, 0, fundo.getWidth(), fundo.getHeight(), matrix, true);
        dirty = true;
    }

    public Bitmap getImagem() {
        if (dirty) {
            meme = criarImagem();
            dirty = false;
        }
        return meme;
    }

    protected Bitmap criarImagem() {
        float heightFactor = (float) fundo.getHeight() / fundo.getWidth();
        int width = displayMetrics.widthPixels;
        int height = (int) (width * heightFactor);

        if (height > displayMetrics.heightPixels * 0.6) {
            height = (int) (displayMetrics.heightPixels * 0.6);
            width = (int) (height * (1 / heightFactor));
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Questão 4: Define as posições iniciais se ainda não foram definidas.
        if (textoSuperiorX == 0 && textoSuperiorY == 0) {
            textoSuperiorX = width / 2.f;
            textoSuperiorY = height * 0.15f;
        }
        if (textoX == 0 && textoY == 0) {
            textoX = width / 2.f;
            textoY = height * 0.9f;
        }

        Paint paint = new Paint();
        Bitmap scaledFundo = Bitmap.createScaledBitmap(fundo, width, height, true);
        canvas.drawBitmap(scaledFundo, 0, 0, new Paint());

        paint.setAntiAlias(true);
        paint.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        paint.setTextAlign(Paint.Align.CENTER);

        if (textoSuperior != null && !textoSuperior.isEmpty()) {
            paint.setColor(corTextoSuperior);
            paint.setTextSize(this.tamanhoTextoSuperior);
            canvas.drawText(textoSuperior, textoSuperiorX, textoSuperiorY, paint);
        }

        if (texto != null && !texto.isEmpty()) {
            paint.setColor(corTexto);
            paint.setTextSize(this.tamanhoTexto);
            canvas.drawText(texto, textoX, textoY, paint);
        }
        return bitmap;
    }
}