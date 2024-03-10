package org.solmore.repository;

import jakarta.transaction.Transactional;
import org.solmore.domain.CurrencyPair;
import org.solmore.domain.CurrencyPairId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface CurrencyRepository extends JpaRepository<CurrencyPair, CurrencyPairId> {

    @Query(value ="""
           SELECT *
           FROM currency_schema.currency_pairs
           WHERE base_currency = :baseCurrency
           AND convert_currency = :convertCurrency
           AND base_amount = :baseAmount
           """, nativeQuery = true)
    CurrencyPair findByBaseCurrencyAndConvertCurrencyAndBaseAmount(@Param("baseCurrency")
                                                                   String baseCurrency,
                                                                   @Param("convertCurrency")
                                                                   String convertCurrency,
                                                                   @Param("baseAmount")
                                                                   BigDecimal baseAmount);
    @Modifying
    @Transactional
    @Query(value = """
    UPDATE currency_schema.currency_pairs
             SET convert_amount = :convertAmount
             WHERE base_currency = :baseCurrency
             AND convert_currency = :convertCurrency
             AND base_amount = :baseAmount
             """, nativeQuery = true)
    void updateConvertAmountById(@Param("baseCurrency")
                                 String baseCurrency,
                                 @Param("convertCurrency")
                                 String convertCurrency,
                                 @Param("baseAmount")
                                 BigDecimal baseAmount,
                                 @Param("convertAmount")
                                 BigDecimal convertAmount);

}
