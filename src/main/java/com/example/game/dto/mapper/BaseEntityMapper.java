package com.example.game.dto.mapper;


import com.example.game.db.model.BaseEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

public interface BaseEntityMapper<E extends BaseEntity, D> extends Converter<E, D> {

	D convert(E entity);

	List<D> convert(List<E> entities);

	default Page<D> convert(Page<E> page) {
		return new PageImpl<>(
			convert(page.getContent()),
			page.getPageable(),
			page.getTotalElements()
		);
	}
}
