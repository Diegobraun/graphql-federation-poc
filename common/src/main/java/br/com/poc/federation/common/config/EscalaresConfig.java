package br.com.poc.federation.common.config;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.function.Function;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

@Configuration
public class EscalaresConfig {

    public static final GraphQLScalarType DECIMAL = GraphQLScalarType.newScalar()
            .name("Decimal")
            .description("Valor decimal exato, serializado como número JSON")
            .coercing(new DecimalCoercing())
            .build();

    public static final GraphQLScalarType DATE = GraphQLScalarType.newScalar()
            .name("Date")
            .description("Data ISO-8601 (yyyy-MM-dd)")
            .coercing(new TextoCoercing<>(LocalDate.class, LocalDate::parse))
            .build();

    public static final GraphQLScalarType DATE_TIME = GraphQLScalarType.newScalar()
            .name("DateTime")
            .description("Data e hora ISO-8601 com offset")
            .coercing(new TextoCoercing<>(OffsetDateTime.class, OffsetDateTime::parse))
            .build();

    @Bean
    public RuntimeWiringConfigurer escalaresWiring() {
        return wiring -> wiring.scalar(DECIMAL).scalar(DATE).scalar(DATE_TIME);
    }

    static final class DecimalCoercing implements Coercing<BigDecimal, BigDecimal> {

        @Override
        public BigDecimal serialize(Object valor, GraphQLContext contexto, Locale locale) {
            if (valor instanceof BigDecimal decimal) {
                return decimal;
            }
            if (valor instanceof Number numero) {
                return new BigDecimal(numero.toString());
            }
            throw new CoercingSerializeException("Decimal inválido: " + valor);
        }

        @Override
        public BigDecimal parseValue(Object entrada, GraphQLContext contexto, Locale locale) {
            try {
                return new BigDecimal(entrada.toString());
            } catch (NumberFormatException e) {
                throw new CoercingParseValueException("Decimal inválido: " + entrada);
            }
        }

        @Override
        public BigDecimal parseLiteral(Value<?> entrada, CoercedVariables variaveis, GraphQLContext contexto, Locale locale) {
            return switch (entrada) {
                case FloatValue f -> f.getValue();
                case IntValue i -> new BigDecimal(i.getValue());
                case StringValue s -> parseValue(s.getValue(), contexto, locale);
                default -> throw new CoercingParseLiteralException("Decimal inválido: " + entrada);
            };
        }
    }

    static final class TextoCoercing<T> implements Coercing<T, String> {

        private final Class<T> tipo;
        private final Function<String, T> parser;

        TextoCoercing(Class<T> tipo, Function<String, T> parser) {
            this.tipo = tipo;
            this.parser = parser;
        }

        @Override
        public String serialize(Object valor, GraphQLContext contexto, Locale locale) {
            if (tipo.isInstance(valor)) {
                return valor.toString();
            }
            throw new CoercingSerializeException("Esperado " + tipo.getSimpleName() + ", recebido " + valor);
        }

        @Override
        public T parseValue(Object entrada, GraphQLContext contexto, Locale locale) {
            try {
                return parser.apply(entrada.toString());
            } catch (DateTimeParseException e) {
                throw new CoercingParseValueException("Formato inválido para " + tipo.getSimpleName() + ": " + entrada);
            }
        }

        @Override
        public T parseLiteral(Value<?> entrada, CoercedVariables variaveis, GraphQLContext contexto, Locale locale) {
            if (entrada instanceof StringValue s) {
                return parseValue(s.getValue(), contexto, locale);
            }
            throw new CoercingParseLiteralException("Esperado texto para " + tipo.getSimpleName());
        }
    }
}
