package org.solmore.web.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.solmore.domain.CurrencyPair;
import org.solmore.web.dto.CurrencyPairDto;


import java.util.List;

@Mapper
public interface CurrencyPairDtoMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(source = "base_currency", target = "id.baseCurrency")
    @Mapping(source = "convert_currency", target = "id.convertCurrency")
    @Mapping(source = "base_amount", target = "id.baseAmount")
    @Mapping(source = "convert_amount", target = "convertAmount")
    CurrencyPair toEntity(CurrencyPairDto dto);

    //@Mapping(target = "id", ignore = true)
    @Mapping(source = "id.baseCurrency", target = "base_currency")
    @Mapping(source = "id.convertCurrency", target = "convert_currency")
    @Mapping(source = "id.baseAmount", target = "base_amount")
    @Mapping(source = "convertAmount", target = "convert_amount")
    CurrencyPairDto toDto(CurrencyPair entity);

}
