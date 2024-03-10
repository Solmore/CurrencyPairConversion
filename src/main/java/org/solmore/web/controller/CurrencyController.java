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
            try {
                logger.info("Get all currency");
                ctx.status(200).json(Ticket.values());
            } catch (Exception e) {
                logger.error("Error processing request: " + e.getMessage());
                ctx.status(500).json("An error occurred while processing the request");
            }
        });

        app.post("/convert", ctx -> {
            try {
                logger.info("Get convert of currency");
                CurrencyRequestPairDto dto = ctx.bodyAsClass(CurrencyRequestPairDto.class);
                CurrencyPair pair = service.getPair(dto.getBase_currency(), dto.getConvert_currency(), dto.getBase_amount());
                System.out.println(pair);
                ctx.status(200).json(mapper.toDto(pair));
            } catch (Exception e) {
                logger.error("Error converting currency: " + e.getMessage());
                ctx.status(500).json("An error occurred while converting currency");
            }
        });

        app.get("/invalidate", ctx -> {
            try {
                logger.info("Validated pair of currency");
                service.validationPair();
                ctx.status(200);
            } catch (Exception e) {
                logger.error("Error validating currency pair: " + e.getMessage());
                ctx.status(500).json("An error occurred while validating currency pair");
            }
        });

        app.post("/set", ctx -> {
            try {
                logger.info("Set currency pairs");
                CurrencyPairDto dto = ctx.bodyAsClass(CurrencyPairDto.class);
                CurrencyPair pair = mapper.toEntity(dto);
                service.createPair(pair);
                CurrencyPair reversePair = service.getPair(pair.getId().getConvertCurrency(), pair.getId().getBaseCurrency(), pair.getId().getBaseAmount());
                var response = Map.of(
                        "direct", mapper.toDto(pair),
                        "reverse", mapper.toDto(reversePair)
                );
                ctx.status(200).json(response);
            } catch (Exception e) {
                logger.error("Error setting currency pair: " + e.getMessage());
                ctx.status(500).json("An error occurred while setting currency pair");
            }
        });
    }
}
