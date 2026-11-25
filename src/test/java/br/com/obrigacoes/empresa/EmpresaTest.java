package br.com.obrigacoes.empresa;

import br.com.obrigacoes.shared.domain.Situacao;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmpresaTest {

    @Test
    void deveNascerAtivaComDadosNormalizados() {
        Empresa empresa = new Empresa("  Contábil Alfa LTDA ", " ", "11.222.333/0001-81", " Contato@Alfa.com.br ", "");

        assertThat(empresa.getSituacao()).isEqualTo(Situacao.ATIVO);
        assertThat(empresa.getRazaoSocial()).isEqualTo("Contábil Alfa LTDA");
        assertThat(empresa.getNomeFantasia()).isNull();
        assertThat(empresa.getCnpj()).isEqualTo("11222333000181");
        assertThat(empresa.getEmail()).isEqualTo("contato@alfa.com.br");
        assertThat(empresa.getTelefone()).isNull();
    }

    @Test
    void deveAlternarSituacaoPreservandoOsDados() {
        Empresa empresa = new Empresa("Contábil Alfa LTDA", null, "11222333000181", null, null);

        empresa.inativar();
        assertThat(empresa.isAtiva()).isFalse();
        assertThat(empresa.getRazaoSocial()).isEqualTo("Contábil Alfa LTDA");

        empresa.ativar();
        assertThat(empresa.isAtiva()).isTrue();
    }

    @Test
    void entidadesNaoPersistidasSoSaoIguaisASiMesmas() {
        Empresa uma = new Empresa("Contábil Alfa LTDA", null, "11222333000181", null, null);
        Empresa outra = new Empresa("Contábil Alfa LTDA", null, "11222333000181", null, null);

        assertThat(uma).isEqualTo(uma).isNotEqualTo(outra);
        assertThat(uma.hashCode()).isEqualTo(outra.hashCode());
    }
}
