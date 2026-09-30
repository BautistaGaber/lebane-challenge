package com.lebane.backend.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(

        @JsonProperty("contenido")
        List<T> content,

        @JsonProperty("pagina")
        int page,

        @JsonProperty("cantidad")
        int size,

        @JsonProperty("totalElementos")
        long totalElements,

        @JsonProperty("totalPaginas")
        int totalPages,

        @JsonProperty("primera")
        boolean first,

        @JsonProperty("ultima")
        boolean last

) {
    public static <T> PageResponse<T> from(Page<?> page, List<T> content) {
        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
