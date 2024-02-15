package price.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import price.client.PriceClient;
import price.domain.Price;
import price.dto.ChartRequest;
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
    private static final int PERIOD_1M = 1;
    private static final int PERIOD_1Y = 12;
    private static final int PERIOD_3Y = 36;
    private static final int PERIOD_5Y = 60;

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
        price.setTotalTrade(Long.parseLong(data[DATA_INDEX_CLOSE]) * Long.parseLong(data[DATA_INDEX_VOLUME]));
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
            stockPriceRequest.setTotalTrade(price.getTotalTrade());
        });
        return stockPriceRequest;
    }

    public List<Price> findAll(ChartRequest chartRequest) {
        LocalDate end = LocalDate.parse(chartRequest.getStartDate(), DateTimeFormatter.ofPattern("yyyyMMdd"));
        int monthsAgo = 0;
        switch (chartRequest.getPeriod()) {
            case "1m":
                monthsAgo = PERIOD_1M;
                break;
            case "1y":
                monthsAgo = PERIOD_1Y;
                break;
            case "3y":
                monthsAgo = PERIOD_3Y;
                break;
            case "5y":
                monthsAgo = PERIOD_5Y;
                break;
            default:
                break;
        }
        LocalDate start = end.minusMonths(monthsAgo);
        return priceRepository.findAllByStockCodeAndDateBetween(chartRequest.getCode(), start, end);
    }

    public List<Price> totalAmountRanking(String date, String method) {
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"));
        Sort sort = "asc".equalsIgnoreCase(method) ?
                Sort.by("totalTrade").ascending() :
                Sort.by("totalTrade").descending();
        return priceRepository.findAllByDate(sort, localDate);
    }
}
