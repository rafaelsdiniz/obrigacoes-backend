package br.com.obrigacoes.shared.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.YearMonth;

@Converter
public class YearMonthConverter implements AttributeConverter<YearMonth, String> {

    @Override
    public String convertToDatabaseColumn(YearMonth valor) {
        return valor == null ? null : valor.toString();
    }

    @Override
    public YearMonth convertToEntityAttribute(String valor) {
        return valor == null ? null : YearMonth.parse(valor);
    }
}
