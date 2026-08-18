/**
 * Módulo de ingestão de documentos.
 * <p>
 * Responsável por extrair texto puro de arquivos em diferentes formatos
 * (PDF, DOCX e futuramente XLSX/PPTX/MD/CSV/JSON/HTML), preparando o conteúdo
 * para as etapas seguintes do pipeline RAG (chunking + embedding).
 * <p>
 * API pública deste módulo: {@link br.com.arthur.agenterag.ingestion.IngestionService}
 * e {@link br.com.arthur.agenterag.ingestion.ExtracaoResponse}.
 * As implementações de {@link br.com.arthur.agenterag.ingestion.DocumentExtractor}
 * são detalhe interno do módulo.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Ingestão de Documentos"
)
package br.com.arthur.agenterag.ingestion;