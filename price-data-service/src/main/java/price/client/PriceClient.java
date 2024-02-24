package price.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import price.dto.CodesResponse;
import price.dto.StockPriceRequest;

import java.util.List;

@FeignClient(name = "price", url = "http://localhost:8081")
public interface  PriceClient {

    @GetMapping("/stocks/codes/module")
    List<CodesResponse> getCodesFromStocks();

    @GetMapping("/stocks/find-stock-information/module")
    StockPriceRequest getStockInformation(@RequestParam("code") String code);
}
