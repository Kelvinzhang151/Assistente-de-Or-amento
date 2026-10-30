package br.com.orcamento.controller;

import br.com.orcamento.exception.NegocioException;
import br.com.orcamento.service.AssistenteService;
import br.com.orcamento.service.VozService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Porta de entrada da API do assistente. So recebe e devolve dados:
 * quem faz o trabalho sao os servicos.
 */
@RestController
@RequestMapping("/assistente")
public class AssistenteController {

    public record MensagemRequest(String mensagem) {
    }

    public record RespostaTexto(String resposta) {
    }

    public record TranscricaoResponse(String texto) {
    }

    private final AssistenteService assistente;
    private final VozService voz;

    public AssistenteController(AssistenteService assistente, VozService voz) {
        this.assistente = assistente;
        this.voz = voz;
    }

    /** Pergunta em texto, resposta em texto. */
    @PostMapping("/texto")
    public RespostaTexto texto(@RequestBody MensagemRequest requisicao) {
        if (requisicao == null || requisicao.mensagem() == null || requisicao.mensagem().isBlank()) {
            throw new NegocioException("Mensagem vazia");
        }
        return new RespostaTexto(assistente.responder(requisicao.mensagem()));
    }

    /** So transcreve o audio (bom para testar o microfone). */
    @PostMapping(value = "/transcrever", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TranscricaoResponse transcrever(@RequestParam("audio") MultipartFile audio) throws IOException {
        validar(audio);
        return new TranscricaoResponse(voz.transcrever(audio.getBytes(), audio.getOriginalFilename()));
    }

    /** Fluxo completo: audio -> texto -> IA com tools -> texto -> audio (MP3). */
    @PostMapping(value = "/voz", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "audio/mpeg")
    public ResponseEntity<byte[]> voz(@RequestParam("audio") MultipartFile audio) throws IOException {
        validar(audio);
        String pergunta = voz.transcrever(audio.getBytes(), audio.getOriginalFilename());
        String resposta = assistente.responder(pergunta);
        byte[] mp3 = voz.sintetizar(resposta);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(mp3);
    }

    private void validar(MultipartFile audio) {
        if (audio == null || audio.isEmpty()) {
            throw new NegocioException("Audio vazio");
        }
    }
}
