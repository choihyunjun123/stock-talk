package price.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import price.service.PriceService;

import java.io.IOException;

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
    private ResponseEntity<String> test() throws IOException {
        boolean success = priceService.crawl();
        return buildResponse(success, success ? "성공" : "실패");
    }

}
