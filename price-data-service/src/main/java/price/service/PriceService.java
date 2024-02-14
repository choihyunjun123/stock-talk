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
import price.dto.StockInformationRequest;
import price.dto.StockPriceRequest;
import price.repository.PriceRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PriceService {

    private final PriceRepository priceRepository;
    private final PriceClient priceClient;

    private static final String INITIAL_CRAWL_DATA_COUNT = "10";
    private static final String SCHEDULED_CRAWL_DATA_COUNT = "1";

    private static final int DATA_INDEX_DATE = 0;
    private static final int DATA_INDEX_OPEN = 1;
    private static final int DATA_INDEX_HIGH = 2;
    private static final int DATA_INDEX_LOW = 3;
    private static final int DATA_INDEX_CLOSE = 4;
    private static final int DATA_INDEX_VOLUME = 5;

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
        String countParam = initialCrawl ? INITIAL_CRAWL_DATA_COUNT : SCHEDULED_CRAWL_DATA_COUNT;
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
            price.setDate(LocalDate.parse(data[DATA_INDEX_DATE], formatter));
        } else {
            price = priceRepository.findByStockCodeAndDate(stockCode, LocalDate.parse(data[DATA_INDEX_DATE], formatter))
                    .orElse(new Price());
        }
        price.setOpen(Long.valueOf(data[DATA_INDEX_OPEN]));
        price.setHigh(Long.valueOf(data[DATA_INDEX_HIGH]));
        price.setLow(Long.valueOf(data[DATA_INDEX_LOW]));
        price.setClose(Long.valueOf(data[DATA_INDEX_CLOSE]));
        price.setVolume(Long.valueOf(data[DATA_INDEX_VOLUME]));
        price.setCreatedAt(LocalDateTime.now());
        priceRepository.save(price);
    }

    public StockPriceRequest stockInformation(StockInformationRequest stockInformationRequest) {
        String code = stockInformationRequest.getCode();
        LocalDate date = LocalDate.parse(stockInformationRequest.getDate(), DateTimeFormatter.ofPattern("yyyyMMdd"));
        StockPriceRequest stockPriceRequest = priceClient.getStockInformation(code);
        priceRepository.findByStockCodeAndDate(code, date).ifPresent(price -> {
            stockPriceRequest.setDate(price.getDate());
            stockPriceRequest.setOpen(price.getOpen());
            stockPriceRequest.setHigh(price.getHigh());
            stockPriceRequest.setLow(price.getLow());
            stockPriceRequest.setClose(price.getClose());
            stockPriceRequest.setVolume(price.getVolume());
        });
        return stockPriceRequest;
    }
}
