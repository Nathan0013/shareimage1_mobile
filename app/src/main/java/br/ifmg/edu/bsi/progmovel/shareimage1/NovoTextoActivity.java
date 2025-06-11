package br.ifmg.edu.bsi.progmovel.shareimage1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class NovoTextoActivity extends AppCompatActivity {

    public static String EXTRA_TEXTO_ATUAL = "br.ifmg.edu.bsi.progmovel.shareimage1.texto_atual";
    public static String EXTRA_COR_ATUAL = "br.ifmg.edu.bsi.progmovel.shareimage1.cor_atual";
    // Questão 1: Criei uma chave para enviar o tamanho atual da fonte para esta tela.
    public static String EXTRA_TAMANHO_ATUAL = "br.ifmg.edu.bsi.progmovel.shareimage1.tamanho_atual";

    public static String EXTRA_NOVO_TEXTO = "br.ifmg.edu.bsi.progmovel.shareimage1.novo_texto";
    public static String EXTRA_NOVA_COR = "br.ifmg.edu.bsi.progmovel.shareimage1.nova_cor";
    // Questão 1: Criei uma chave para enviar o novo tamanho de volta à MainActivity.
    public static String EXTRA_NOVO_TAMANHO = "br.ifmg.edu.bsi.progmovel.shareimage1.novo_tamanho";

    private EditText etTexto;
    private EditText etCor;
    private EditText etTamanhoFonte;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novo_texto);

        etTexto = findViewById(R.id.etTexto);
        etCor = findViewById(R.id.etCor);
        etTamanhoFonte = findViewById(R.id.etTamanhoFonte);

        Intent intent = getIntent();
        String textoAtual = intent.getStringExtra(EXTRA_TEXTO_ATUAL);
        String corAtual = intent.getStringExtra(EXTRA_COR_ATUAL);
        // Questão 1: Recebo o tamanho atual da fonte para que o campo já comece com o valor certo.
        float tamanhoAtual = intent.getFloatExtra(EXTRA_TAMANHO_ATUAL, 64f);

        etTexto.setText(textoAtual);
        etCor.setText(corAtual);
        etTamanhoFonte.setText(String.valueOf(tamanhoAtual));
    }

    public void enviarNovoTexto(View v) {
        String novoTexto = etTexto.getText().toString();
        String novaCor = etCor.getText().toString();
        String novoTamanhoStr = etTamanhoFonte.getText().toString();

        float novoTamanho;

        // Questão 1: Leio o valor do campo de texto e o converto para float.
        // O try-catch é para evitar que o app quebre se o campo estiver vazio.
        try {
            novoTamanho = Float.parseFloat(novoTamanhoStr);
        } catch (NumberFormatException e) {
            novoTamanho = 64f; // Valor padrão se o campo estiver inválido
            Toast.makeText(this, "Tamanho inválido. Usando valor padrão.", Toast.LENGTH_SHORT).show();
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_NOVO_TEXTO, novoTexto);
        intent.putExtra(EXTRA_NOVA_COR, novaCor);
        // Questão 1: Envio o novo tamanho de volta para a MainActivity.
        intent.putExtra(EXTRA_NOVO_TAMANHO, novoTamanho);

        setResult(RESULT_OK, intent);
        finish();
    }
}