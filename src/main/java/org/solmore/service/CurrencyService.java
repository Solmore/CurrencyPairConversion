package org.solmore.service;


import org.solmore.domain.CurrencyPair;
import org.solmore.domain.Ticket;

import java.math.BigDecimal;


public interface CurrencyService {

    void validationPair();

    void createPair(CurrencyPair pair);

    CurrencyPair getPair(Ticket baseTicket, Ticket convertTicket, BigDecimal baseAmount);

}
