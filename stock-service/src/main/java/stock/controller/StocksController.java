package stock.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import stock.service.StocksService;

@RestController
@RequestMapping("/stocks")
public class StocksController {

    private final StocksService stocksService;

    public StocksController(StocksService stocksService) {
        this.stocksService = stocksService;
    }

    @PostMapping("/upload")
    public void uploadFile(@RequestParam("file")MultipartFile file, @RequestParam("type") int type) {
        stocksService.uploadExcelFile(file, type);
    }
}
