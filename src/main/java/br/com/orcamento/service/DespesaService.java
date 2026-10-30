package br.com.orcamento.service;

import br.com.orcamento.exception.NegocioException;
import br.com.orcamento.model.Despesa;
import br.com.orcamento.repository.DespesaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/**
 * Regras de negocio do orcamento. Nao sabe nada de IA nem de HTTP:
 * quem chama (tools ou controller) so usa estes metodos.
 */
@Service
public class DespesaService {

    private static final String CATEGORIA_PADRAO = "outros";

    private final DespesaRepository repositorio;

    public DespesaService(DespesaRepository repositorio) {
        this.repositorio = repositorio;
    }

    public Despesa registrar(String descricao, String categoria, BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new NegocioException("Valor invalido: precisa ser maior que zero");
        }
        if (descricao == null || descricao.isBlank()) {
            throw new NegocioException("Descricao invalida: informe o que foi pago");
        }
        Despesa despesa = new Despesa(
                descricao.trim(),
                normalizar(categoria),
                valor.setScale(2, RoundingMode.HALF_UP),
                LocalDate.now());
        return repositorio.save(despesa);
    }

    public BigDecimal totalGeral() {
        return zeroSeNulo(repositorio.somarTudo());
    }

    public BigDecimal totalPorCategoria(String categoria) {
        return zeroSeNulo(repositorio.somarPorCategoria(normalizar(categoria)));
    }

    public List<Despesa> ultimas() {
        return repositorio.findTop5ByOrderByDataDescIdDesc();
    }

    public List<Despesa> todas() {
        return repositorio.findAll();
    }

    /**
     * Deixa a categoria em minusculas e sem acento, para "Alimentação"
     * e "alimentacao" contarem como a mesma categoria.
     */
    private String normalizar(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            return CATEGORIA_PADRAO;
        }
        String semAcento = Normalizer.normalize(categoria.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(Locale.ROOT);
    }

    private BigDecimal zeroSeNulo(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO.setScale(2) : valor;
    }
}
