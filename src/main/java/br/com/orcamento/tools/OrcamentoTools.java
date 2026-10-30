package br.com.orcamento.tools;

import br.com.orcamento.exception.NegocioException;
import br.com.orcamento.model.Despesa;
import br.com.orcamento.service.DespesaService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * TOOL CALLING: cada metodo com @Tool e uma funcao real da aplicacao que a IA
 * pode pedir para executar. A "description" e o que a IA le para decidir
 * quando usar cada uma. Aqui as tools so chamam o DespesaService.
 */
@Component
public class OrcamentoTools {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private final DespesaService despesas;

    public OrcamentoTools(DespesaService despesas) {
        this.despesas = despesas;
    }

    @Tool(description = "Registra uma nova despesa no orcamento. Use quando a pessoa disser que gastou ou pagou algo.")
    public String registrarDespesa(
            @ToolParam(description = "O que foi comprado ou pago, por exemplo: almoco") String descricao,
            @ToolParam(description = "Categoria da despesa, por exemplo: alimentacao, transporte, lazer") String categoria,
            @ToolParam(description = "Valor em reais, por exemplo: 25.50") double valor) {
        try {
            Despesa salva = despesas.registrar(descricao, categoria, BigDecimal.valueOf(valor));
            return "Despesa registrada: " + salva.getDescricao()
                    + " (" + salva.getCategoria() + ") no valor de " + reais(salva.getValor());
        } catch (NegocioException e) {
            return "ERRO: " + e.getMessage();
        }
    }

    @Tool(description = "Consulta quanto ja foi gasto em uma categoria, por exemplo: alimentacao.")
    public String consultarTotalPorCategoria(
            @ToolParam(description = "Nome da categoria") String categoria) {
        return "Total gasto na categoria " + categoria + ": " + reais(despesas.totalPorCategoria(categoria));
    }

    @Tool(description = "Consulta quanto foi gasto no total, somando todas as categorias.")
    public String consultarTotalGeral() {
        return "Total geral gasto: " + reais(despesas.totalGeral());
    }

    @Tool(description = "Lista as 5 despesas mais recentes.")
    public String listarUltimasDespesas() {
        List<Despesa> lista = despesas.ultimas();
        if (lista.isEmpty()) {
            return "Nenhuma despesa registrada ainda.";
        }
        return lista.stream()
                .map(d -> d.getDescricao() + " (" + d.getCategoria() + "): "
                        + reais(d.getValor()) + " em " + d.getData())
                .collect(Collectors.joining("; "));
    }

    private String reais(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(valor);
    }
}
