package org.solmore.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class CurrencyAPIDto {

    private String status;

    private String message;

    private Map<String,String> data;
}
