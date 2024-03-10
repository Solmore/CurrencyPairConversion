package org.solmore.web.controller;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmore.domain.CurrencyPair;
import org.solmore.domain.Ticket;
import org.solmore.service.CurrencyService;
import org.solmore.web.dto.CurrencyPairDto;
import org.solmore.web.dto.CurrencyRequestPairDto;
import org.solmore.web.mapper.CurrencyPairDtoMapper;

import java.util.Map;


public class CurrencyController {
    private final CurrencyService service;
    private final CurrencyPairDtoMapper mapper;
    private final Logger logger = LoggerFactory.getLogger(CurrencyController.class);

    public CurrencyController(CurrencyService service, CurrencyPairDtoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    public void initRoutes(Javalin app) {
        app.get("/tickets", ctx -> {
            logger.info("Get all currency");
            ctx.status(200).json(Ticket.values());
        });

        app.post("/convert", ctx -> {
            logger.info("Get convert of currency");
            CurrencyRequestPairDto dto = ctx.bodyAsClass(CurrencyRequestPairDto.class);
            CurrencyPair pair = service.getPair(dto.getBase_currency(), dto.getConvert_currency(), dto.getBase_amount());
            System.out.println(pair);
            ctx.status(200).json(mapper.toDto(pair));
        });

        app.get("/invalidate", ctx -> {
            logger.info("Validated pair of currency");
            service.validationPair();
            ctx.status(200);
        });

        app.post("/set", ctx -> {
            logger.info("Set currency pairs");
            CurrencyPairDto dto = ctx.bodyAsClass(CurrencyPairDto.class);
            CurrencyPair pair = mapper.toEntity(dto);
            service.createPair(pair);
            CurrencyPair reversePair = service.getPair(pair.getId().getConvertCurrency(),pair.getId().getBaseCurrency(),pair.getId().getBaseAmount());
            var response = Map.of(
                    "direct", mapper.toDto(pair),
                    "reverse", mapper.toDto(reversePair)
            );
            ctx.status(200).json(response);
        });
    }
}
