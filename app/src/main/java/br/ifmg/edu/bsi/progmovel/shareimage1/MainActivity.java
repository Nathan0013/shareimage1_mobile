package br.ifmg.edu.bsi.progmovel.shareimage1;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
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

public class MainActivity extends AppCompatActivity {
    // ... (código existente, como imageView, memeCreator e os launchers, não muda)
    private ImageView imageView;
    private MemeCreator memeCreator;
    private final ActivityResultLauncher<Intent> startNovoTexto = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = result.getData();
                    if (intent != null) {
                        String novoTexto = intent.getStringExtra(NovoTextoActivity.EXTRA_NOVO_TEXTO);
                        String novaCor = intent.getStringExtra(NovoTextoActivity.EXTRA_NOVA_COR);
                        float novoTamanho = intent.getFloatExtra(NovoTextoActivity.EXTRA_NOVO_TAMANHO, 64f);
                        int alvoEdicao = intent.getIntExtra(NovoTextoActivity.EXTRA_ALVO_EDICAO, NovoTextoActivity.ALVO_INFERIOR);

                        if (novaCor == null || novaCor.isEmpty()) {
                            novaCor = "BLACK";
                        }

                        if (alvoEdicao == NovoTextoActivity.ALVO_SUPERIOR) {
                            memeCreator.setTextoSuperior(novoTexto);
                            memeCreator.setCorTextoSuperior(Color.parseColor(novaCor.toUpperCase()));
                            memeCreator.setTamanhoTextoSuperior(novoTamanho);
                        } else {
                            memeCreator.setTexto(novoTexto);
                            memeCreator.setCorTexto(Color.parseColor(novaCor.toUpperCase()));
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
        intent.putExtra(NovoTextoActivity.EXTRA_TAMANHO_ATUAL, memeCreator.getTamanhoTexto());
        intent.putExtra(NovoTextoActivity.EXTRA_ALVO_EDICAO, NovoTextoActivity.ALVO_INFERIOR);
        startNovoTexto.launch(intent);
    }

    public void iniciarMudarTextoSuperior(View v) {
        Intent intent = new Intent(this, NovoTextoActivity.class);
        intent.putExtra(NovoTextoActivity.EXTRA_TEXTO_ATUAL, memeCreator.getTextoSuperior());
        intent.putExtra(NovoTextoActivity.EXTRA_COR_ATUAL, converterCor(memeCreator.getCorTextoSuperior()));
        intent.putExtra(NovoTextoActivity.EXTRA_TAMANHO_ATUAL, memeCreator.getTamanhoTextoSuperior());
        intent.putExtra(NovoTextoActivity.EXTRA_ALVO_EDICAO, NovoTextoActivity.ALVO_SUPERIOR);
        startNovoTexto.launch(intent);
    }

    // Questão 3: novo botão de templates.
    public void iniciarEscolherTemplate(View v) {
        // Nomes dos templates que vão aparecer na lista para o usuário.
        final CharSequence[] nomesDosTemplates = {"Macaco Turn Down for What", "Meme Bebê", "Fry (Original)"};

        // Arquivos de imagem correspondentes na pasta 'drawable'.
        final int[] idsDosTemplates = {R.drawable.macaco, R.drawable.meme_bb, R.drawable.fry_meme};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Escolha um Template");
        builder.setItems(nomesDosTemplates, (dialog, which) -> {
            // 'which' é a posição do item que o usuário clicou.

            // Pego a imagem correspondente usando a posição.
            Bitmap novoFundo = BitmapFactory.decodeResource(getResources(), idsDosTemplates[which]);

            // Atualizo o fundo no nosso criador de meme.
            memeCreator.setFundo(novoFundo);

            // Mostro a imagem atualizada na tela.
            mostrarImagem();
        });
        builder.show();
    }

    public String converterCor(int cor) {
        // ... (método sem alteração)
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
        // ... (método sem alteração)
        startImagemFundo.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }
    public void compartilhar(View v) {
        // ... (método sem alteração)
        compartilharImagem(memeCreator.getImagem());
    }
    public void mostrarImagem() {
        // ... (método sem alteração)
        imageView.setImageBitmap(memeCreator.getImagem());
    }
    public void compartilharImagem(Bitmap bitmap) {
        // ... (método sem alteração)
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