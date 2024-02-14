package stock.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import stock.domain.Stocks;
import stock.dto.CodeRequest;
import stock.dto.NameRequest;
import stock.dto.SortRequest;
import stock.dto.TypeRequest;
import stock.repository.projection.StockCodeProjection;
import stock.service.StocksService;


import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
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

    @GetMapping("/find-name")
    public ResponseEntity<?> findName(@ModelAttribute NameRequest nameRequest) {
        List<Stocks> foundStock = stocksService.findName(nameRequest);
        return handleRequest(foundStock);
    }

    @GetMapping("/find-code")
    public ResponseEntity<?> findCode(@ModelAttribute CodeRequest codeRequest) {
        List<Stocks> foundStock = stocksService.findCode(codeRequest);
        return handleRequest(foundStock);
    }

    @GetMapping("/find-type")
    public ResponseEntity<?> findType(@ModelAttribute TypeRequest typeRequest, @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Stocks> foundStock = stocksService.findType(typeRequest, pageable);
        if (foundStock.isEmpty()) {
            return buildResponse(false, "없음");
        } else {
            return ResponseEntity.ok(foundStock);
        }
    }

    public ResponseEntity<?> handleRequest(List<Stocks> stocksList) {
        if (stocksList.isEmpty()) {
            return buildResponse(false, "없음");
        } else {
            return ResponseEntity.ok(stocksList);
        }
    }

    @GetMapping("/find-all")
    public ResponseEntity<?> sortAscend(@ModelAttribute SortRequest sortRequest) {
        List<Stocks> foundStock = stocksService.findAllStocks(sortRequest);
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
