package br.com.alr.api.sbkafkaproducersample.adapter.in.web.response;

import java.math.BigDecimal;

public record ProductResponse(Long id, String name, BigDecimal price) {
}
