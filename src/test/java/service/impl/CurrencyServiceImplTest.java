package service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.solmore.domain.CurrencyPair;
import org.solmore.domain.CurrencyPairId;
import org.solmore.domain.Ticket;
import org.solmore.domain.exception.CurrencyPairNotFoundException;
import org.solmore.repository.CurrencyRepository;
import org.solmore.service.CurrencyServiceImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;

public class CurrencyServiceImplTest {

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    private CurrencyServiceImpl currencyService;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        currencyService = new CurrencyServiceImpl(currencyRepository, redisTemplate);
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void getPairReturnsCorrectPair() {
        Ticket baseTicket = Ticket.USD;
        Ticket convertTicket = Ticket.CAD;
        BigDecimal baseAmount = BigDecimal.ONE;
        CurrencyPair expectedPair = new CurrencyPair();
        CurrencyPairId id = new CurrencyPairId(
                baseTicket,
                convertTicket,
                BigDecimal.ONE
        );
        expectedPair.setId(id);
        expectedPair.setConvertAmount(BigDecimal.valueOf(0.85));
        Mockito.when(currencyRepository.findByBaseCurrencyAndConvertCurrencyAndBaseAmount(
                baseTicket.name(), convertTicket.name(), baseAmount))
                .thenReturn(expectedPair);
        Mockito.when(redisTemplate.opsForValue().get(Mockito.anyString())).thenReturn(null);
        CurrencyPair resultPair = currencyService.getPair(baseTicket, convertTicket, baseAmount);

        Assertions.assertEquals(expectedPair, resultPair);
        Mockito.verify(currencyRepository, Mockito.times(1))
                .findByBaseCurrencyAndConvertCurrencyAndBaseAmount(baseTicket.name(), convertTicket.name(), baseAmount);
    }

    @Test
    void getPairReturnsNullPair() {
        Ticket baseTicket = Ticket.USD;
        Ticket convertTicket = Ticket.CAD;
        BigDecimal baseAmount = BigDecimal.ONE;

        Mockito.when(currencyRepository.findByBaseCurrencyAndConvertCurrencyAndBaseAmount(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(null);

        Assertions.assertThrows(CurrencyPairNotFoundException.class, () -> currencyService.getPair(baseTicket, convertTicket, baseAmount));

        Mockito.verify(currencyRepository, Mockito.times(2)).findByBaseCurrencyAndConvertCurrencyAndBaseAmount(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void testCreatePair() {
        Ticket baseCurrency = Ticket.USD;
        Ticket convertCurrency = Ticket.CAD;
        BigDecimal baseAmount = BigDecimal.ONE;
        BigDecimal convertAmount = BigDecimal.valueOf(0.85);
        Mockito.when(currencyRepository.findByBaseCurrencyAndConvertCurrencyAndBaseAmount(
                        Mockito.anyString(), Mockito.anyString(), Mockito.any(BigDecimal.class)))
                .thenReturn(null);
        CurrencyPair currencyPair = new CurrencyPair();
        CurrencyPairId id = new CurrencyPairId(
                baseCurrency,
                convertCurrency,
                baseAmount
        );
        currencyPair.setId(id);
        currencyPair.setConvertAmount(convertAmount);
        currencyService.createPair(currencyPair);
        Mockito.verify(currencyRepository, Mockito.times(2)).saveAndFlush(Mockito.any(CurrencyPair.class));

    }
}
