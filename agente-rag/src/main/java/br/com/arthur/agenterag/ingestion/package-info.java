/**
 * Módulo de ingestão de documentos.
 * <p>
 * Responsável por extrair texto puro de arquivos em diferentes formatos
 * (PDF, DOCX e futuramente XLSX/PPTX/MD/CSV/JSON/HTML), preparando o conteúdo
 * para as etapas seguintes do pipeline RAG (chunking + embedding).
 * <p>
 * API pública deste módulo: {@link br.com.arthur.agenterag.ingestion.service.IngestionService}
 * e {@link br.com.arthur.agenterag.ingestion.dto.ExtracaoResponse}.
 * As implementações de {@link br.com.arthur.agenterag.ingestion.internal.DocumentExtractor}
 * são detalhe interno do módulo.
 */
@ApplicationModule(
        displayName = "Ingestão de Documentos",
        allowedDependencies = {
                "core"
        }
)
package br.com.arthur.agenterag.ingestion;
import org.springframework.modulith.ApplicationModule;