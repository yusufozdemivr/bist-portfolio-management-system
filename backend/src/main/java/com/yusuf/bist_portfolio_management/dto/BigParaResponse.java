package com.yusuf.bist_portfolio_management.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BigParaResponse {

    private String code;
    private Data data;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        private HisseYuzeysel hisseYuzeysel;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HisseYuzeysel {
        private String sembol;
        private BigDecimal kapanis;
        private BigDecimal alis;
        private BigDecimal satis;
        private BigDecimal yuksek;
        private BigDecimal dusuk;
        private BigDecimal tavan;
        private BigDecimal taban;
        private BigDecimal yuzdedegisim;
        private Long hacimlot;
        private BigDecimal hacimtl;
    }
}