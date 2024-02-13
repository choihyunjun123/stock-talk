package stock.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import stock.repository.projection.StockCodeProjection;
import stock.service.StocksService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/stocks")
public class StocksController {

    private final StocksService stocksService;

    public StocksController(StocksService stocksService) {
        this.stocksService = stocksService;
    }

    private ResponseEntity<String> buildResponse(boolean success, String message) {
        String responseMessage = "{\"message\": \"" + message + "\"}";
        return success ? ResponseEntity.ok(responseMessage) : ResponseEntity.badRequest().body(responseMessage);
    }

    @PostMapping("/excel-upload")
    public ResponseEntity<String> uploadExcelFile(@RequestParam("file") MultipartFile file, @RequestParam("type") int type) throws IOException {
        boolean success = stocksService.uploadExcelFile(file, type);
        return buildResponse(success, success ? "성공" : "실패");
    }

    @GetMapping("/codes")
    public List<StockCodeProjection> findAllStockCodes() {
        return stocksService.findAllStockCodes();
    }
}
