package price.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import price.domain.Price;
import price.dto.ChartRequest;
import price.dto.StockInformationRequest;
import price.dto.StockPriceRequest;
import price.service.PriceService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/price")
public class PriceController {

    private final PriceService priceService;

    public PriceController(PriceService priceService) {
        this.priceService = priceService;
    }

    private ResponseEntity<String> buildResponse(boolean success, String message) {
        String responseMessage = "{\"message\": \"" + message + "\"}";
        return success ? ResponseEntity.ok(responseMessage) : ResponseEntity.badRequest().body(responseMessage);
    }

    @GetMapping("/save-price-data")
    private ResponseEntity<String> savePriceData() throws IOException {
        boolean success = priceService.crawl();
        return buildResponse(success, success ? "성공" : "실패");
    }

    @GetMapping("/stock-information")
    private ResponseEntity<?> stockInformation(StockInformationRequest stockInformationRequest) {
        StockPriceRequest stockData = priceService.stockInformation(stockInformationRequest);
        if (stockData == null) {
            return buildResponse(false, "없음");
        } else {
            return ResponseEntity.ok(stockData);
        }
    }

    @GetMapping("/chart-data")
    private ResponseEntity<?> findByType(@ModelAttribute ChartRequest chartRequest) {
        Price foundStock = priceService.findAll(chartRequest);
        if (foundStock == null) {
            return buildResponse(false, "없음");
        } else {
            return ResponseEntity.ok(foundStock);
        }
    }
}
