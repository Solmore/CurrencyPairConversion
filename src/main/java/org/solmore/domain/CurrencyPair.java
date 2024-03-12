package org.solmore.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;


@Table(name = "currency_pairs")
@Entity
@Getter
@Setter
public class CurrencyPair implements Serializable {

    @EmbeddedId
    private CurrencyPairId id;

    @Column(name = "convert_amount", columnDefinition = "NUMERIC(30,6)")
    private BigDecimal convertAmount;

}

