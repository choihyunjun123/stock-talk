package price.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import price.dto.CodesResponse;

import java.util.List;

@FeignClient(name = "price", url = "http://host.docker.internal:8081")
public interface  PriceClient {

    @GetMapping("/stocks/codes")
    List<CodesResponse> getCodesFromStocks();
}
