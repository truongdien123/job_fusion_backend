package com.tma.job_fusion_backend.mappers;

import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

public interface EntityMapper<REQ, RES, E> {

    @BeanMapping(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    E toEntity(REQ req);

    @BeanMapping(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    List<E> toEntity(List<REQ> reqs);

    RES toResponse(E e);

    List<RES> toResponse(List<E> e);

    @BeanMapping(
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    void updateEntity(REQ req, @MappingTarget E e);
}
