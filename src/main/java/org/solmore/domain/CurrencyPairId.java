package org.solmore.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
public class CurrencyPairId implements Serializable {

    @Column(name = "base_currency")
    @Enumerated(EnumType.STRING)
    private Ticket baseCurrency;

    @Column(name = "convert_currency")
    @Enumerated(EnumType.STRING)
    private Ticket convertCurrency;

    @Column(name = "base_amount", columnDefinition = "NUMERIC(30,6)")
    private BigDecimal baseAmount;

    protected CurrencyPairId() {
    }

    public CurrencyPairId(Ticket baseCurrency, Ticket convertCurrency, BigDecimal baseAmount) {
        this.baseCurrency = baseCurrency;
        this.convertCurrency = convertCurrency;
        this.baseAmount = baseAmount;
    }
}
