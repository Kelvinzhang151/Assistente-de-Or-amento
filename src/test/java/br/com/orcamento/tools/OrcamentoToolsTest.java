package br.com.orcamento.tools;

import br.com.orcamento.model.Despesa;
import br.com.orcamento.repository.DespesaRepository;
import br.com.orcamento.service.DespesaService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrcamentoToolsTest {

    private final DespesaRepository repositorio = mock(DespesaRepository.class);
    private final OrcamentoTools ferramentas = new OrcamentoTools(new DespesaService(repositorio));

    @Test
    void registrarDespesaDeveConfirmarComOValor() {
        when(repositorio.save(any(Despesa.class))).thenAnswer(i -> i.getArgument(0));

        String resposta = ferramentas.registrarDespesa("almoço", "alimentação", 25.5);

        assertTrue(resposta.startsWith("Despesa registrada"));
        assertTrue(resposta.contains("25,50"));
        verify(repositorio).save(any(Despesa.class));
    }

    @Test
    void valorInvalidoDeveDevolverMensagemDeErroParaAIA() {
        String resposta = ferramentas.registrarDespesa("almoço", "alimentação", 0);

        assertTrue(resposta.startsWith("ERRO"));
        verify(repositorio, never()).save(any(Despesa.class));
    }

    @Test
    void consultarTotalPorCategoriaDeveMostrarOTotal() {
        when(repositorio.somarPorCategoria("alimentacao")).thenReturn(new BigDecimal("80.00"));

        String resposta = ferramentas.consultarTotalPorCategoria("Alimentação");

        assertTrue(resposta.contains("80,00"));
    }

    @Test
    void consultarTotalGeralSemDespesasDeveMostrarZero() {
        when(repositorio.somarTudo()).thenReturn(null);

        assertTrue(ferramentas.consultarTotalGeral().contains("0,00"));
    }

    @Test
    void listarSemDespesasDeveAvisar() {
        when(repositorio.findTop5ByOrderByDataDescIdDesc()).thenReturn(List.of());

        assertEquals("Nenhuma despesa registrada ainda.", ferramentas.listarUltimasDespesas());
    }

    @Test
    void listarDeveMostrarDescricaoCategoriaEValor() {
        Despesa d = new Despesa("almoço", "alimentacao", new BigDecimal("25.50"), LocalDate.of(2026, 10, 30));
        when(repositorio.findTop5ByOrderByDataDescIdDesc()).thenReturn(List.of(d));

        String resposta = ferramentas.listarUltimasDespesas();

        assertTrue(resposta.contains("almoço"));
        assertTrue(resposta.contains("alimentacao"));
        assertTrue(resposta.contains("25,50"));
    }
}
