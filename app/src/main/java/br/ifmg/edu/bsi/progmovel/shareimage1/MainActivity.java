package br.ifmg.edu.bsi.progmovel.shareimage1;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;
import java.io.BufferedOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Activity que cria uma imagem com um texto e imagem de fundo.
 */
public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private MemeCreator memeCreator;
    private final ActivityResultLauncher<Intent> startNovoTexto = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = result.getData();
                    if (intent != null) {
                        String novoTexto = intent.getStringExtra(NovoTextoActivity.EXTRA_NOVO_TEXTO);
                        String novaCor = intent.getStringExtra(NovoTextoActivity.EXTRA_NOVA_COR);
                        // Questão 1: novo tamanho da fonte da tela de edição.
                        float novoTamanho = intent.getFloatExtra(NovoTextoActivity.EXTRA_NOVO_TAMANHO, 64f);

                        // Questão 2: Verificar texto de cima ou de baixo.
                        int alvoEdicao = intent.getIntExtra(NovoTextoActivity.EXTRA_ALVO_EDICAO, NovoTextoActivity.ALVO_INFERIOR);

                        if (novaCor == null) {
                            Toast.makeText(MainActivity.this, "Cor desconhecida. Usando preto no lugar.", Toast.LENGTH_SHORT).show();
                            novaCor = "BLACK";
                        }

                        // Questão 2: Atualizo o MemeCreator .
                        if (alvoEdicao == NovoTextoActivity.ALVO_SUPERIOR) {
                            memeCreator.setTextoSuperior(novoTexto);
                            memeCreator.setCorTextoSuperior(Color.parseColor(novaCor.toUpperCase()));
                            memeCreator.setTamanhoTextoSuperior(novoTamanho);
                        } else {
                            memeCreator.setTexto(novoTexto);
                            memeCreator.setCorTexto(Color.parseColor(novaCor.toUpperCase()));
                            // Questão 1:  novo tamanho no criador de meme.
                            memeCreator.setTamanhoTexto(novoTamanho);
                        }

                        mostrarImagem();
                    }
                }
            });

    private final ActivityResultLauncher<PickVisualMediaRequest> startImagemFundo = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(),
            result -> {
                if (result == null) {
                    return;
                }
                try (ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(result, "r")) {
                    Bitmap imagemFundo = MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver(), result);
                    memeCreator.setFundo(imagemFundo);

                    // descobrir se é preciso rotacionar a imagem
                    FileDescriptor fd = pfd.getFileDescriptor();
                    ExifInterface exif = new ExifInterface(fd);
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                        memeCreator.rotacionarFundo(90);
                    }

                    mostrarImagem();
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            });

    private ActivityResultLauncher<String> startWriteStoragePermission = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
            result -> {
                if (!result) {
                    Toast.makeText(MainActivity.this, "Sem permissão de acesso a armazenamento do celular.", Toast.LENGTH_SHORT).show();
                } else {
                    compartilhar(null);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);

        Bitmap imagemFundo = BitmapFactory.decodeResource(getResources(), R.drawable.fry_meme);

        memeCreator = new MemeCreator("Olá Android!", Color.WHITE, imagemFundo, getResources().getDisplayMetrics());
        mostrarImagem();
    }

    public void iniciarMudarTexto(View v) {
        Intent intent = new Intent(this, NovoTextoActivity.class);
        intent.putExtra(NovoTextoActivity.EXTRA_TEXTO_ATUAL, memeCreator.getTexto());
        intent.putExtra(NovoTextoActivity.EXTRA_COR_ATUAL, converterCor(memeCreator.getCorTexto()));
        // Questão 1: tamanho da fonte atual para a tela de edição
        intent.putExtra(NovoTextoActivity.EXTRA_TAMANHO_ATUAL, memeCreator.getTamanhoTexto());
        // Questão 2:  activity que vai editar o texto de baixo.
        intent.putExtra(NovoTextoActivity.EXTRA_ALVO_EDICAO, NovoTextoActivity.ALVO_INFERIOR);
        startNovoTexto.launch(intent);
    }

    // Questão 2:  botão de editar o texto de cima.
    public void iniciarMudarTextoSuperior(View v) {
        Intent intent = new Intent(this, NovoTextoActivity.class);
        intent.putExtra(NovoTextoActivity.EXTRA_TEXTO_ATUAL, memeCreator.getTextoSuperior());
        intent.putExtra(NovoTextoActivity.EXTRA_COR_ATUAL, converterCor(memeCreator.getCorTextoSuperior()));
        intent.putExtra(NovoTextoActivity.EXTRA_TAMANHO_ATUAL, memeCreator.getTamanhoTextoSuperior());
        // Questão 2: Falo para a activity que ela vai editar o texto de cima.
        intent.putExtra(NovoTextoActivity.EXTRA_ALVO_EDICAO, NovoTextoActivity.ALVO_SUPERIOR);
        startNovoTexto.launch(intent);
    }

    public String converterCor(int cor) {
        switch (cor) {
            case Color.BLACK: return "BLACK";
            case Color.WHITE: return "WHITE";
            case Color.BLUE: return "BLUE";
            case Color.GREEN: return "GREEN";
            case Color.RED: return "RED";
            case Color.YELLOW: return "YELLOW";
        }
        return "BLACK";
    }

    public void iniciarMudarFundo(View v) {
        startImagemFundo.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    public void compartilhar(View v) {
        compartilharImagem(memeCreator.getImagem());
    }

    public void mostrarImagem() {
        imageView.setImageBitmap(memeCreator.getImagem());
    }

    public void compartilharImagem(Bitmap bitmap) {

        // ... (código de compartilhar não muda)
        Uri contentUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            int write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (PackageManager.PERMISSION_GRANTED != write) {
                startWriteStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                return;
            }
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, "shareimage1file");
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        Uri imageUri = getContentResolver().insert(contentUri, values);

        try (
                ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(imageUri, "w");
                FileOutputStream fos = new FileOutputStream(pfd.getFileDescriptor())
        ) {
            BufferedOutputStream bytes = new BufferedOutputStream(fos);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao gravar imagem:\n" + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/*");
        share.putExtra(Intent.EXTRA_TITLE, "Seu meme fabuloso");
        share.putExtra(Intent.EXTRA_STREAM, imageUri);
        startActivity(Intent.createChooser(share, "Compartilhar Imagem"));
    }
}