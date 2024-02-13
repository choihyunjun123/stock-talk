package price.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import price.client.PriceClient;
import price.domain.Price;
import price.dto.CodesResponse;
import price.repository.PriceRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PriceService {

    private final String initialCrawlDataCount  = "10";

    private final String scheduledCrawlDataCount  = "1";

    private final PriceRepository priceRepository;
    private final PriceClient priceClient;

    @Autowired
    public PriceService(PriceRepository priceRepository, PriceClient priceClient) {
        this.priceRepository = priceRepository;
        this.priceClient = priceClient;
    }

    public boolean crawl() throws IOException {
        updateStockPrices(true);
        return true;
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void scheduledCrawl() {
        updateStockPrices(false);
    }

    private void updateStockPrices(boolean initialCrawl) {
        List<CodesResponse> codes = priceClient.getCodesFromStocks();
        codes.forEach(code -> processCode(code, initialCrawl));
    }

    private void processCode(CodesResponse code, boolean initialCrawl) {
        String countParam = initialCrawl ? initialCrawlDataCount  : scheduledCrawlDataCount ;
        String url = String.format("https://fchart.stock.naver.com/sise.nhn?symbol=%s&timeframe=day&count=%s&requestType=0", code.getCode(), countParam);
        try {
            Document document = Jsoup.connect(url).get();
            Elements elements = document.getElementsByTag("item");
            elements.forEach(element -> savePrice(code.getCode(), element, initialCrawl));
        } catch (IOException e) {
            System.err.println("Failed to fetch or save price for stock code: " + code.getCode() + ". Error: " + e.getMessage());
        }
    }

    private void savePrice(String stockCode, Element item, boolean initialCrawl) {
        String[] data = item.attr("data").split("\\|");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        Price price;
        if (initialCrawl) {
            price = new Price();
            price.setStockCode(stockCode);
            price.setDate(LocalDate.parse(data[0], formatter));
        } else {
            price = priceRepository.findByStockCodeAndDate(stockCode, LocalDate.parse(data[0], formatter))
                    .orElse(new Price());
        }
        price.setOpen(Long.valueOf(data[1]));
        price.setHigh(Long.valueOf(data[2]));
        price.setLow(Long.valueOf(data[3]));
        price.setClose(Long.valueOf(data[4]));
        price.setVolume(Long.valueOf(data[5]));
        price.setCreatedAt(LocalDateTime.now());
        priceRepository.save(price);
    }
}
