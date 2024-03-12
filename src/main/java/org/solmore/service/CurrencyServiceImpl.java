package org.solmore.service;

import com.google.gson.Gson;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmore.domain.CurrencyPair;
import org.solmore.domain.CurrencyPairId;
import org.solmore.domain.Ticket;
import org.solmore.domain.exception.CurrencyPairNotFoundException;
import org.solmore.repository.CurrencyRepository;
import org.solmore.web.dto.CurrencyAPIDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service("currencyService")
public class CurrencyServiceImpl implements CurrencyService{

    private final CurrencyRepository repository;


    @Value("${security.key}") String urlKey;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final Logger logger = LoggerFactory.getLogger(CurrencyServiceImpl.class);

    public CurrencyServiceImpl(CurrencyRepository repository, RedisTemplate<String, Object> redisTemplate) {
        this.repository = repository;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void validationPair() {
        logger.info("Start validation pair");
        List<String> pairTickets = Arrays.stream(Ticket.values())
                .flatMap(ticket -> Arrays.stream(Ticket.values())
                        .filter(otherTicker -> !otherTicker.equals(ticket))
                        .map(otherTicker -> ticket.name() + otherTicker.name()))
                .collect(Collectors.toList());
        OkHttpClient client = new OkHttpClient();
        Request request = buildRequest(String.join(",", pairTickets));
        processResponse(client, request);
        logger.info("Finish validation pair");
    }

    private Request buildRequest(String pairs) {
        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme("https")
                .host("currate.ru")
                .addPathSegment("api")
                .addPathSegment("")
                .addQueryParameter("get", "rates")
                .addQueryParameter("pairs", pairs)
                .addQueryParameter("key", urlKey)
                .build();
        return new Request.Builder().url(httpUrl).build();
    }

    private void processResponse(OkHttpClient client, Request request) {
        try (Response response = client.newCall(request).execute()) {
            CurrencyAPIDto dto = new Gson().fromJson(Objects.requireNonNull(response.body()).string(),
                    CurrencyAPIDto.class);
            List<CurrencyPair> currencyPairs = dto.getData()
                    .keySet()
                    .stream()
                    .map( key -> {
                        CurrencyPair currencyPair = new CurrencyPair();
                        CurrencyPairId currencyPairId = new CurrencyPairId(
                                Ticket.valueOf(key.substring(0, 3)),
                                Ticket.valueOf(key.substring(3)),
                                BigDecimal.ONE
                        );
                        currencyPair.setId(currencyPairId);
                        currencyPair.setConvertAmount(
                                BigDecimal.valueOf(Float.parseFloat(dto.getData().get(key))));
                        return currencyPair;
                    })
                    .collect(Collectors.toList());
            repository.saveAllAndFlush(currencyPairs);
        } catch (IOException e) {
            logger.error("Get issue: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }


    @Override
    public void createPair(CurrencyPair pair) {
        logger.info("Save currency pair");
        saveOrUpdatePair(pair);
        CurrencyPair reversePair = getCurrencyPair(pair);
        saveOrUpdatePair(reversePair);
        String key = pair.getId().getBaseCurrency().name() + pair.getId().getConvertCurrency().name()
                + pair.getId().getBaseAmount().toPlainString();
        redisTemplate.opsForValue().set(key, pair);
        logger.info("Successful finish currency pair");
    }

    @NotNull
    private CurrencyPair getCurrencyPair(CurrencyPair pair) {
        CurrencyPair reversePair = new CurrencyPair();
        CurrencyPairId currencyPairId = new CurrencyPairId(
                pair.getId().getConvertCurrency(),
                pair.getId().getBaseCurrency(),
                pair.getId().getBaseAmount()
        );
        reversePair.setId(currencyPairId);
        reversePair.setConvertAmount(
                pair.getId().getBaseAmount().divide(pair.getConvertAmount(), 15 ,RoundingMode.DOWN)
                        .multiply(reversePair.getId().getBaseAmount()));
        return reversePair;
    }

    public CurrencyPair getPair(Ticket baseTicket, Ticket convertTicket, BigDecimal baseAmount) {
        logger.info("Get currency pair");
        String cacheKey = baseTicket.name() + "_" + convertTicket.name() + "_" + baseAmount;

        CurrencyPair pair = (CurrencyPair) redisTemplate.opsForValue().get(cacheKey);
        logger.info("Get redisTemplate");
        if (pair != null) {
            logger.info("Currency pair found in cache");
            return pair;
        }
        pair = repository
                .findByBaseCurrencyAndConvertCurrencyAndBaseAmount(baseTicket.name(),
                                                                   convertTicket.name(),
                                                                   baseAmount);
        if (pair == null) {
            logger.info("No direct pair found, trying with base amount of BigDecimal.ONE");
            pair = repository.findByBaseCurrencyAndConvertCurrencyAndBaseAmount(baseTicket.name(),
                                                                                convertTicket.name(),
                                                                                BigDecimal.ONE);
            if (pair == null) {
                logger.error("Currency pair not found for " + baseTicket + " to "
                        + convertTicket + " with base amount " + baseAmount);
                throw new CurrencyPairNotFoundException("Currency pair not found for "
                        + baseTicket + " to " + convertTicket + " with base amount " + baseAmount);
            }
            CurrencyPair adjustedPair = adjustPairToOriginalBaseAmount(pair, baseAmount);
            createPair(adjustedPair);
            return adjustedPair;
        }
        redisTemplate.opsForValue().set(cacheKey, pair);
        logger.info("Successful get currency pair");
        return pair;
    }

    private CurrencyPair adjustPairToOriginalBaseAmount(CurrencyPair pair, BigDecimal baseAmount) {
        CurrencyPair adjustedPair = new CurrencyPair();
        CurrencyPairId id = new CurrencyPairId(pair.getId().getBaseCurrency(), 
                                               pair.getId().getConvertCurrency(),
                                               baseAmount);
        adjustedPair.setId(id);
        adjustedPair.setConvertAmount(baseAmount.multiply(pair.getConvertAmount()));
        return adjustedPair;
    }

    private void saveOrUpdatePair(CurrencyPair pair) {
        CurrencyPair pairRepository = repository.findByBaseCurrencyAndConvertCurrencyAndBaseAmount(
                pair.getId().getBaseCurrency().name(),
                pair.getId().getConvertCurrency().name(),
                pair.getId().getBaseAmount()
        );
        if(pairRepository != null) {
            CurrencyPairId id = pair.getId();
            repository.updateConvertAmountById(
                    id.getBaseCurrency().name(),
                    id.getConvertCurrency().name(),
                    id.getBaseAmount(),
                    pair.getConvertAmount());
        } else {
            repository.saveAndFlush(pair);
        }
        String key = pair.getId().getBaseCurrency().name() + pair.getId().getConvertCurrency().name()
                + pair.getId().getBaseAmount().toPlainString();
        redisTemplate.opsForValue().set(key, pair);
    }
}
