package br.com.orcamento.repository;

import br.com.orcamento.model.Despesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * Acesso ao banco. O Spring Data cria a implementacao sozinho.
 * Os somatorios devolvem null quando nao ha despesas (o servico trata isso).
 */
public interface DespesaRepository extends JpaRepository<Despesa, Long> {

    @Query("select sum(d.valor) from Despesa d")
    BigDecimal somarTudo();

    @Query("select sum(d.valor) from Despesa d where d.categoria = :categoria")
    BigDecimal somarPorCategoria(@Param("categoria") String categoria);

    List<Despesa> findTop5ByOrderByDataDescIdDesc();
}
