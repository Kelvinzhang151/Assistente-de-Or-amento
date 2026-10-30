package br.com.orcamento.controller;

import br.com.orcamento.model.Despesa;
import br.com.orcamento.service.DespesaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Consulta direta das despesas gravadas no banco (sem IA), para conferir se o assistente salvou certo.
 */
@RestController
@RequestMapping("/despesas")
public class DespesaController {

    public record DespesaResponse(Long id, String descricao, String categoria, BigDecimal valor, LocalDate data) {
        static DespesaResponse de(Despesa d) {
            return new DespesaResponse(d.getId(), d.getDescricao(), d.getCategoria(), d.getValor(), d.getData());
        }
    }

    private final DespesaService despesas;

    public DespesaController(DespesaService despesas) {
        this.despesas = despesas;
    }

    @GetMapping
    public List<DespesaResponse> listar() {
        return despesas.todas().stream().map(DespesaResponse::de).toList();
    }
}
