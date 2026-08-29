package br.com.agenterag.domain.service;

import br.com.agenterag.domain.internal.Disciplina;

public final class DisciplinaMapper {

    private DisciplinaMapper() {}

    /**
     * Distribuição de disciplinas por questão, validada manualmente contra
     * o conteúdo real das 80 questões do XXXVII Exame de Ordem (26/02/2023).
     *
     * ⚠️ ATENÇÃO: a FGV publica a distribuição de disciplinas no edital de
     * CADA exame, e ela pode variar entre edições (quantidade de questões
     * por matéria, ordem dos blocos). Este mapeamento é específico para o
     * XXXVII Exame — ao processar outras edições em lote, os números podem
     * não corresponder à realidade daquela prova específica. Idealmente,
     * cada edição deveria ter seu próprio mapeamento validado, ou vir de
     * uma fonte estruturada (o gabarito às vezes indica a disciplina por
     * bloco de questões).
     */
    public static Disciplina paraQuestao(int numeroQuestao) {
        if (numeroQuestao <= 8)  return Disciplina.ETICA_ESTATUTO_ADVOCACIA;
        if (numeroQuestao <= 10) return Disciplina.FILOSOFIA_DO_DIREITO;
        if (numeroQuestao <= 17) return Disciplina.DIREITO_CONSTITUCIONAL;
        if (numeroQuestao <= 19) return Disciplina.DIREITOS_HUMANOS;
        if (numeroQuestao <= 21) return Disciplina.DIREITO_INTERNACIONAL;
        if (numeroQuestao <= 26) return Disciplina.DIREITO_TRIBUTARIO;
        if (numeroQuestao <= 32) return Disciplina.DIREITO_ADMINISTRATIVO;
        if (numeroQuestao <= 34) return Disciplina.DIREITO_AMBIENTAL;
        if (numeroQuestao <= 41) return Disciplina.DIREITO_CIVIL;
        if (numeroQuestao <= 43) return Disciplina.ECA;
        if (numeroQuestao <= 45) return Disciplina.DIREITO_DO_CONSUMIDOR;
        if (numeroQuestao <= 50) return Disciplina.DIREITO_EMPRESARIAL;
        if (numeroQuestao <= 57) return Disciplina.DIREITO_PROCESSUAL_CIVIL;
        if (numeroQuestao <= 63) return Disciplina.DIREITO_PENAL;
        if (numeroQuestao <= 69) return Disciplina.DIREITO_PROCESSUAL_PENAL;
        if (numeroQuestao <= 75) return Disciplina.DIREITO_DO_TRABALHO;
        if (numeroQuestao <= 80) return Disciplina.DIREITO_PROCESSUAL_DO_TRABALHO;
        return Disciplina.DESCONHECIDA;
    }
}