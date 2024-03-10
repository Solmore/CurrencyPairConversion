package org.solmore.web.dto;

import lombok.Getter;
import lombok.Setter;
import org.solmore.domain.Ticket;

import java.math.BigDecimal;

@Getter
@Setter
public class CurrencyRequestPairDto {

    private Ticket base_currency;

    private Ticket convert_currency;

    private BigDecimal base_amount;

}
