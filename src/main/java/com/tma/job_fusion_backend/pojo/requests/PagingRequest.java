package com.tma.job_fusion_backend.pojo.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tma.job_fusion_backend.commons.FieldConstant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagingRequest<T> {

    private String sortField;

    private T filters;

    private Sort.Direction sortBy;
    private int page;
    private int size;

    public Pageable toPageable() {
        int adjustedPage = page > 0 ? page - 1 : 0;
        int adjustedSize = size > 0 ? size : 10;

        Sort.Direction direction = sortBy != null ? sortBy : Sort.Direction.ASC;
        if (!StringUtils.hasText(sortField)) {
            return PageRequest.of(adjustedPage, adjustedSize, Sort.by(Sort.Direction.DESC, FieldConstant.UPDATED_AT));
        }
        return PageRequest.of(adjustedPage, adjustedSize, Sort.by(direction, sortField));
    }
}
