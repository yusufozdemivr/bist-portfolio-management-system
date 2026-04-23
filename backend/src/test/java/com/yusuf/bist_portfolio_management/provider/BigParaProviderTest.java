package com.yusuf.bist_portfolio_management.provider;

import com.yusuf.bist_portfolio_management.dto.BigParaResponse;
import com.yusuf.bist_portfolio_management.dto.StockPriceData;
import com.yusuf.bist_portfolio_management.entity.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BigParaProviderTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private BigParaProvider bigParaProvider;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(bigParaProvider,
                "baseUrl",
                "https://bigpara.hurriyet.com.tr/api/v1/borsa/hisseyuzeysel");
        ReflectionTestUtils.setField(bigParaProvider,
                "requestDelayMs", 0L);
    }

    @Test
    void fetchPrices_WithValidResponse_MapsAllFieldsCorrectly() {
        Stock stock = Stock.builder()
                .id(UUID.randomUUID())
                .symbol("SISE")
                .companyName("Şişecam")
                .isBist100(true)
                .build();

        BigParaResponse response = buildBigParaResponse("SISE",
                new BigDecimal("47.40"), new BigDecimal("47.38"),
                new BigDecimal("47.40"), new BigDecimal("47.84"),
                new BigDecimal("46.56"), new BigDecimal("52.10"),
                new BigDecimal("42.66"), new BigDecimal("0.38"),
                35252373L, new BigDecimal("1661556101.0"));

        when(restTemplate.getForObject(anyString(),
                eq(BigParaResponse.class)))
                .thenReturn(response);

        List<StockPriceData> result = bigParaProvider
                .fetchPrices(List.of(stock));

        assertEquals(1, result.size());

        StockPriceData data = result.get(0);
        assertEquals("SISE", data.getSymbol());
        assertEquals(new BigDecimal("47.40"), data.getLastPrice());
        assertEquals(new BigDecimal("47.38"), data.getBidPrice());
        assertEquals(new BigDecimal("47.40"), data.getAskPrice());
        assertEquals(new BigDecimal("47.84"), data.getDayHigh());
        assertEquals(new BigDecimal("46.56"), data.getDayLow());
        assertEquals(new BigDecimal("52.10"), data.getDayLimitUp());
        assertEquals(new BigDecimal("42.66"), data.getDayLimitDown());
        assertEquals(new BigDecimal("0.38"), data.getChangePercentage());
        assertEquals(35252373L, data.getVolume());
        assertEquals(new BigDecimal("1661556101.0"), data.getTurnover());
    }

    @Test
    void fetchPrices_WhenInvalidCode_SkipsStock() {
        Stock stock = Stock.builder()
                .id(UUID.randomUUID())
                .symbol("INVALID")
                .companyName("Invalid Stock")
                .isBist100(true)
                .build();

        BigParaResponse response = new BigParaResponse();
        response.setCode("1");

        when(restTemplate.getForObject(anyString(),
                eq(BigParaResponse.class)))
                .thenReturn(response);

        List<StockPriceData> result = bigParaProvider
                .fetchPrices(List.of(stock));

        assertTrue(result.isEmpty());
    }

    @Test
    void fetchPrices_WhenApiFails_ContinuesWithNextStock() {
        Stock stock1 = Stock.builder()
                .id(UUID.randomUUID())
                .symbol("FAIL")
                .companyName("Fail Stock")
                .isBist100(true)
                .build();

        Stock stock2 = Stock.builder()
                .id(UUID.randomUUID())
                .symbol("SISE")
                .companyName("Şişecam")
                .isBist100(true)
                .build();

        BigParaResponse validResponse = buildBigParaResponse("SISE",
                new BigDecimal("47.40"), new BigDecimal("47.38"),
                new BigDecimal("47.40"), new BigDecimal("47.84"),
                new BigDecimal("46.56"), new BigDecimal("52.10"),
                new BigDecimal("42.66"), new BigDecimal("0.38"),
                35252373L, new BigDecimal("1661556101.0"));

        when(restTemplate.getForObject(
                eq("https://bigpara.hurriyet.com.tr/api/v1/borsa/hisseyuzeysel/FAIL"),
                eq(BigParaResponse.class)))
                .thenThrow(new RuntimeException("403 Forbidden"));

        when(restTemplate.getForObject(
                eq("https://bigpara.hurriyet.com.tr/api/v1/borsa/hisseyuzeysel/SISE"),
                eq(BigParaResponse.class)))
                .thenReturn(validResponse);

        List<StockPriceData> result = bigParaProvider
                .fetchPrices(List.of(stock1, stock2));

        assertEquals(1, result.size());
        assertEquals("SISE", result.get(0).getSymbol());
    }

    private BigParaResponse buildBigParaResponse(
            String sembol, BigDecimal kapanis, BigDecimal alis,
            BigDecimal satis, BigDecimal yuksek, BigDecimal dusuk,
            BigDecimal tavan, BigDecimal taban,
            BigDecimal yuzdedegisim, Long hacimlot,
            BigDecimal hacimtl) {

        BigParaResponse.HisseYuzeysel hisse =
                new BigParaResponse.HisseYuzeysel();
        hisse.setSembol(sembol);
        hisse.setKapanis(kapanis);
        hisse.setAlis(alis);
        hisse.setSatis(satis);
        hisse.setYuksek(yuksek);
        hisse.setDusuk(dusuk);
        hisse.setTavan(tavan);
        hisse.setTaban(taban);
        hisse.setYuzdedegisim(yuzdedegisim);
        hisse.setHacimlot(hacimlot);
        hisse.setHacimtl(hacimtl);

        BigParaResponse.Data data = new BigParaResponse.Data();
        data.setHisseYuzeysel(hisse);

        BigParaResponse response = new BigParaResponse();
        response.setCode("0");
        response.setData(data);
        return response;
    }
}