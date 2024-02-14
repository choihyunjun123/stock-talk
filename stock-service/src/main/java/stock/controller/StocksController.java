package stock.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import stock.domain.Stocks;
import stock.dto.CodeRequest;
import stock.dto.NameRequest;
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

    private ResponseEntity<?> buildResponse(boolean success, String message) {
        String responseMessage = "{\"message\": \"" + message + "\"}";
        return success ? ResponseEntity.ok(responseMessage) : ResponseEntity.badRequest().body(responseMessage);
    }

    @PostMapping("/excel-upload")
    public ResponseEntity<?> uploadExcelFile(@RequestParam("file") MultipartFile file, @RequestParam("type") int type) throws IOException {
        boolean success = stocksService.uploadExcelFile(file, type);
        return buildResponse(success, success ? "성공" : "실패");
    }

    @PostMapping("/find-name")
    public ResponseEntity<?> findName(@RequestBody NameRequest nameRequest) {
        List<Stocks> foundStock = stocksService.findName(nameRequest);
        if (foundStock.isEmpty()) {
            return buildResponse(false, "없음");
        } else {
            return ResponseEntity.ok(foundStock);
        }
    }

    @PostMapping("/find-code")
    public ResponseEntity<?> findCode(@RequestBody CodeRequest codeRequest) {
        List<Stocks> foundStock = stocksService.findCode(codeRequest);
        if (foundStock.isEmpty()) {
            return buildResponse(false, "없음");
        } else {
            return ResponseEntity.ok(foundStock);
        }
    }

    @GetMapping("/codes/module")
    public List<StockCodeProjection> findAllStockCodes() {
        return stocksService.findAllStockCodes();
    }
}
