package price.batch;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import price.client.PriceClient;
import price.domain.Price;
import price.dto.CodesResponse;
import price.repository.PriceRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class StockCrawlingService {

    private final PriceClient priceClient;

    public StockCrawlingService(PriceClient priceClient) {
        this.priceClient = priceClient;
    }

    public List<String> crawlStocks() {
        List<CodesResponse> codesList = priceClient.getCodesFromStocks();
        List<String> result = new ArrayList<>();
        for (CodesResponse codesResponse : codesList) {
            result.add(codesResponse.getCode());
        }
        return result;
    }

    public String[] processCode(String code) {
        String url = String.format("https://fchart.stock.naver.com/sise.nhn?symbol=%s&timeframe=day&count=1&requestType=0", code);
        try {
            Document document = Jsoup.connect(url).get();
            Elements elements = document.getElementsByTag("item");
            if (!elements.isEmpty()) {
                Element element = elements.first();
                assert element != null;
                return element.attr("data").split("\\|");
            }
        } catch (IOException e) {
            System.err.println("Failed to fetch or save price for stock code: " + code + ". Error: " + e.getMessage());
        }
        return null;
    }
}
