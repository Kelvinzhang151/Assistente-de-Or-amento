package br.com.orcamento.exception;

/**
 * Erro de regra de negocio (valor invalido, audio vazio, etc.).
 * A mensagem ja vem pronta para ser mostrada.
 */
public class NegocioException extends RuntimeException {

    public NegocioException(String mensagem) {
        super(mensagem);
    }
}
