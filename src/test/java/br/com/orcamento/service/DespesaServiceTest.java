package br.com.orcamento.service;

import br.com.orcamento.exception.NegocioException;
import br.com.orcamento.model.Despesa;
import br.com.orcamento.repository.DespesaRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DespesaServiceTest {

    private final DespesaRepository repositorio = mock(DespesaRepository.class);
    private final DespesaService servico = new DespesaService(repositorio);

    @Test
    void registrarDeveSalvarComCategoriaSemAcentoEValorComDuasCasas() {
        when(repositorio.save(any(Despesa.class))).thenAnswer(i -> i.getArgument(0));

        Despesa salva = servico.registrar("  Almoço ", " Alimentação ", new BigDecimal("25.5"));

        assertEquals("Almoço", salva.getDescricao());
        assertEquals("alimentacao", salva.getCategoria());
        assertEquals(new BigDecimal("25.50"), salva.getValor());
        verify(repositorio).save(any(Despesa.class));
    }

    @Test
    void categoriaVaziaDeveVirarOutros() {
        when(repositorio.save(any(Despesa.class))).thenAnswer(i -> i.getArgument(0));

        Despesa salva = servico.registrar("presente", "  ", new BigDecimal("10"));

        assertEquals("outros", salva.getCategoria());
    }

    @Test
    void valorZeroOuNegativoDeveDarErroENaoSalvar() {
        assertThrows(NegocioException.class, () -> servico.registrar("almoço", "alimentação", BigDecimal.ZERO));
        assertThrows(NegocioException.class, () -> servico.registrar("almoço", "alimentação", new BigDecimal("-5")));
        assertThrows(NegocioException.class, () -> servico.registrar("almoço", "alimentação", null));
        verify(repositorio, never()).save(any(Despesa.class));
    }

    @Test
    void descricaoVaziaDeveDarErro() {
        assertThrows(NegocioException.class, () -> servico.registrar("  ", "lazer", new BigDecimal("10")));
        verify(repositorio, never()).save(any(Despesa.class));
    }

    @Test
    void totalSemDespesasDeveSerZero() {
        when(repositorio.somarTudo()).thenReturn(null);
        when(repositorio.somarPorCategoria("lazer")).thenReturn(null);

        assertEquals(0, servico.totalGeral().compareTo(BigDecimal.ZERO));
        assertEquals(0, servico.totalPorCategoria("Lazer").compareTo(BigDecimal.ZERO));
    }

    @Test
    void totalPorCategoriaDeveIgnorarAcentoECaixa() {
        when(repositorio.somarPorCategoria("alimentacao")).thenReturn(new BigDecimal("80.00"));

        assertEquals(new BigDecimal("80.00"), servico.totalPorCategoria("Alimentação"));
    }
}
