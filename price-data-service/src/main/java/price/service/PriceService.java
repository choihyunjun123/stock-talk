package price.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final PriceRepository priceRepository;
    private final PriceClient priceClient;

    @Autowired
    public PriceService(PriceRepository priceRepository, PriceClient priceClient) {
        this.priceRepository = priceRepository;
        this.priceClient = priceClient;
    }

    public boolean crawl() throws IOException {
        List<CodesResponse> codes = priceClient.getCodesFromStocks();
        codes.forEach(this::processCode);
        return true;
    }

    private void processCode(CodesResponse code) {
        String url = String.format("https://fchart.stock.naver.com/sise.nhn?symbol=%s&timeframe=day&count=10&requestType=0", code.getCode());
        try {
            Document document = Jsoup.connect(url).get();
            Elements elements = document.getElementsByTag("item");
            elements.forEach(element -> savePrice(code.getCode(), element));
        } catch (IOException e) {
            System.err.println("Failed to fetch or save price for stock code: " + code.getCode() + ". Error: " + e.getMessage());
        }
    }

    private void savePrice(String stockCode, Element item) {
        String[] data = item.attr("data").split("\\|");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        Price price = new Price();
        price.setStockCode(stockCode);
        price.setDate(LocalDate.parse(data[0], formatter));
        price.setOpen(Long.valueOf(data[1]));
        price.setHigh(Long.valueOf(data[2]));
        price.setLow(Long.valueOf(data[3]));
        price.setClose(Long.valueOf(data[4]));
        price.setVolume(Long.valueOf(data[5]));
        price.setCreatedAt(LocalDateTime.now());
        priceRepository.save(price);
    }
}
