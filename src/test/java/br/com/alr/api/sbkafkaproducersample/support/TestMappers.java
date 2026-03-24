package br.com.alr.api.sbkafkaproducersample.support;

import br.com.alr.api.sbkafkaproducersample.adapter.in.web.ApiMapper;
import br.com.alr.api.sbkafkaproducersample.config.ModelMapperConfiguration;

public final class TestMappers {

  private TestMappers() {
  }

  public static ApiMapper apiMapper() {
    return new ApiMapper(new ModelMapperConfiguration().modelMapper());
  }
}
