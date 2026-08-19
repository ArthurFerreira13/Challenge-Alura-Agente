/**
 * Módulo Core — domínio compartilhado da aplicação.
 * <p>
 * Contém as entidades de domínio ({@code Documento}, {@code DocumentoChunk})
 * e seus respectivos repositórios, utilizados por outros módulos da aplicação
 * (ex: {@code ingestion}, {@code retrieval}).
 * <p>
 * Declarado como {@code OPEN} pois representa um "shared kernel": seus
 * subpacotes ({@code domain}, {@code repository}) são expostos livremente
 * para os demais módulos.
 */
@org.springframework.modulith.ApplicationModule(
        type = org.springframework.modulith.ApplicationModule.Type.OPEN
)
package br.com.agenterag.core;