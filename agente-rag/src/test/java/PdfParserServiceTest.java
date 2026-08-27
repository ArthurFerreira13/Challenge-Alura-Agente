import br.com.agenterag.domain.internal.Questao;
import br.com.agenterag.domain.service.PdfParserService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class PdfParserServiceTest {

    private final PdfParserService parser = new PdfParserService();

    @Test
    void deveExtrairQuestoesDeTextoLimpo() {
        String textoSimulado = """
                IX EXAME DE ORDEM UNIFICADO – TIPO 01 – BRANCA
                Questão 1
                Um advogado é contratado por uma sociedade empresária para atuar em uma demanda judicial.
                A) A primeira alternativa apresentada aqui para fins de teste.
                B) A segunda alternativa apresentada aqui para fins de teste.
                C) A terceira alternativa apresentada aqui para fins de teste.
                D) A quarta alternativa apresentada aqui para fins de teste.
                2
                IX EXAME DE ORDEM UNIFICADO – TIPO 01 – BRANCA
                Questão 2
                Os irmãos, Matilde, advogada, e Frederico, consultor de empresas, decidiram firmar sociedade.
                A) É vedada a divulgação dos serviços advocatícios em conjunto com qualquer outra atividade.
                B) A publicidade conjunta dos serviços advocatícios é permitida apenas por meio digital.
                C) Matilde e Frederico podem atuar conjuntamente, pois as atividades se complementam.
                D) A divulgação conjunta é permitida, desde que os materiais sejam sóbrios e não induzam ao erro.
                """;

        // Simula o PDF via um utilitário que gera bytes a partir de texto puro
        // (na prática, você vai chamar parser.extrairQuestoes(pdfBytesReal) direto)
        List<Questao> questoes = extrairDeTextoSimulado(textoSimulado);

        assertThat(questoes).hasSize(2);
        assertThat(questoes.get(0).getNumeroQuestao()).isEqualTo(1);
        assertThat(questoes.get(0).getAlternativas()).hasSize(4);
        assertThat(questoes.get(1).getEnunciado()).contains("Matilde");
    }

    // Método auxiliar só pra esse teste — chama a lógica interna sem precisar de PDF real.
    // Ideal: refatorar extrairQuestoes pra separar "extrair texto do PDF" de "parsear texto",
    // assim dá pra testar o parsing sem precisar gerar PDF nenhum.
    private List<Questao> extrairDeTextoSimulado(String texto) {
        // Ver observação abaixo sobre refatoração
        return null;
    }
}
