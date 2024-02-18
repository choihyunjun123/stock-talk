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

@Service
public class StockCrawlingService {

    private static final int DATA_INDEX_DATE = 0;
    private static final int DATA_INDEX_OPEN = 1;
    private static final int DATA_INDEX_HIGH = 2;
    private static final int DATA_INDEX_LOW = 3;
    private static final int DATA_INDEX_CLOSE = 4;
    private static final int DATA_INDEX_VOLUME = 5;

    private final PriceRepository priceRepository;
    private final PriceClient priceClient;

    public StockCrawlingService(PriceRepository priceRepository, PriceClient priceClient) {
        this.priceRepository = priceRepository;
        this.priceClient = priceClient;
    }

    public List<Price> crawlStocks() {
        List<Price> priceList = new ArrayList<>();
        List<CodesResponse> codesList = priceClient.getCodesFromStocks();
        for (CodesResponse code : codesList) {
            priceList.add(processCode(code.getCode()));
        }
        return priceList;
    }

    private Price processCode(String code) {
        String url = String.format("https://fchart.stock.naver.com/sise.nhn?symbol=%s&timeframe=day&count=1&requestType=0", code);
        try {
            Document document = Jsoup.connect(url).get();
            Elements elements = document.getElementsByTag("item");
            if (!elements.isEmpty()) {
                Element element = elements.first();
                return savePrice(code, element);
            }
        } catch (IOException e) {
            System.err.println("Failed to fetch or save price for stock code: " + code + ". Error: " + e.getMessage());
        }
        return null;
    }

    private Price savePrice(String stockCode, Element item) {
        String[] data = item.attr("data").split("\\|");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate date = LocalDate.parse(data[DATA_INDEX_DATE], formatter);
        Price price = new Price();
        setPriceData(stockCode, price, data);
        price.setCreatedAt(LocalDateTime.now());
        Optional<Price> optionalBeforePrice = findFirstExistingPriceBeforeDate(stockCode, date);
        optionalBeforePrice.ifPresent(beforePrice -> calculateAndSetPriceChange(price, data[DATA_INDEX_CLOSE], beforePrice));
        return price;
    }

    private void calculateAndSetPriceChange(Price price, String closeData, Price beforePrice) {
        double closeValue = Double.parseDouble(closeData);
        double priceChange = (closeValue - beforePrice.getClose()) / beforePrice.getClose() * 100;
        price.setPriceChange(priceChange);
    }

    private Optional<Price> findFirstExistingPriceBeforeDate(String stockCode, LocalDate date) {
        for (int i = 1; i < 10; i++) {
            Optional<Price> existingPrice = priceRepository.findByStockCodeAndDate(stockCode, date.minusDays(i));
            if (existingPrice.isPresent()) {
                return existingPrice;
            }
        }
        return Optional.empty();
    }

    private void setPriceData(String stockCode, Price price, String[] data) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        price.setStockCode(stockCode);
        price.setDate(LocalDate.parse(data[DATA_INDEX_DATE], formatter));
        price.setOpen(Long.parseLong(data[DATA_INDEX_OPEN]));
        price.setHigh(Long.parseLong(data[DATA_INDEX_HIGH]));
        price.setLow(Long.parseLong(data[DATA_INDEX_LOW]));
        price.setClose(Long.parseLong(data[DATA_INDEX_CLOSE]));
        price.setVolume(Long.parseLong(data[DATA_INDEX_VOLUME]));
        price.setTotalTrade(Long.parseLong(data[DATA_INDEX_CLOSE]) * Long.parseLong(data[DATA_INDEX_VOLUME]));
    }
}
