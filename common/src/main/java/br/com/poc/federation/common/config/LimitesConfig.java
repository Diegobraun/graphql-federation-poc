package br.com.poc.federation.common.config;

import graphql.analysis.MaxQueryComplexityInstrumentation;
import graphql.analysis.MaxQueryDepthInstrumentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LimitesConfig {

    @Bean
    public MaxQueryDepthInstrumentation limiteProfundidade(@Value("${poc.graphql.profundidade-maxima:12}") int profundidade) {
        return new MaxQueryDepthInstrumentation(profundidade);
    }

    @Bean
    public MaxQueryComplexityInstrumentation limiteComplexidade(@Value("${poc.graphql.complexidade-maxima:400}") int complexidade) {
        return new MaxQueryComplexityInstrumentation(complexidade);
    }
}
