package br.com.orcamento.service;

import br.com.orcamento.exception.NegocioException;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * Parte de voz: transforma audio em texto (transcricao) e texto em audio (sintese de voz).
 */
@Service
public class VozService {

    private final OpenAiAudioTranscriptionModel transcricao;
    private final OpenAiAudioSpeechModel fala;

    public VozService(OpenAiAudioTranscriptionModel transcricao, OpenAiAudioSpeechModel fala) {
        this.transcricao = transcricao;
        this.fala = fala;
    }

    /** Audio -> texto. */
    public String transcrever(byte[] audio, String nomeArquivo) {
        // A OpenAI usa a extensao do nome (.mp3, .m4a, .webm...) para saber o formato do audio
        String nome = (nomeArquivo == null || nomeArquivo.isBlank()) ? "audio.webm" : nomeArquivo;
        Resource recurso = new ByteArrayResource(audio) {
            @Override
            public String getFilename() {
                return nome;
            }
        };

        AudioTranscriptionResponse resposta = transcricao.call(new AudioTranscriptionPrompt(recurso));
        String texto = resposta.getResult().getOutput();
        if (texto == null || texto.isBlank()) {
            throw new NegocioException("Nao consegui entender o audio");
        }
        return texto;
    }

    /** Texto -> audio (MP3). */
    public byte[] sintetizar(String texto) {
        return fala.call(texto);
    }
}
