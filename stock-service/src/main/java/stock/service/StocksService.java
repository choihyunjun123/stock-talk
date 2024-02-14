package stock.service;

import jakarta.persistence.EntityNotFoundException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import stock.domain.Stocks;
import stock.dto.*;
import stock.repository.StocksRepository;
import stock.repository.projection.StockCodeProjection;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StocksService {

    private final StocksRepository stocksRepository;

    private static final int COLUMN_INDEX_NAME = 0;
    private static final int COLUMN_INDEX_CODE = 1;
    private static final int COLUMN_INDEX_CEO = 6;
    private static final int COLUMN_INDEX_PLACE = 8;

    @Autowired
    public StocksService(StocksRepository stocksRepository) {
        this.stocksRepository = stocksRepository;
    }

    @Transactional
    // 엑셀 파일 데이터 저장
    public boolean uploadExcelFile(MultipartFile file, int type) throws IOException {
        InputStream in = file.getInputStream();
        Workbook workbook = new XSSFWorkbook(in);
        processSheet(workbook.getSheetAt(0), type);
        return true;
    }

    // 시작 페이지
    private void processSheet(Sheet sheet, int type) {
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;
            parseRow(row, type);
        }
    }

    // 열 별 데이터 저장
    private void parseRow(Row row, int type) {
        Stocks stocks = new Stocks();
        stocks.setCreatedAt(LocalDateTime.now());
        stocks.setMarketType(type);
        stocks.setStatus(true);
        for (Cell cell : row) {
            switch (cell.getColumnIndex()) {
                case COLUMN_INDEX_NAME:
                    stocks.setName(cell.getStringCellValue());
                    break;
                case COLUMN_INDEX_CODE:
                    stocks.setCode(cell.getStringCellValue());
                    break;
                case COLUMN_INDEX_CEO:
                    stocks.setCeo(cell.getStringCellValue());
                    break;
                case COLUMN_INDEX_PLACE:
                    stocks.setPlace(cell.getStringCellValue());
                    break;
                default:
                    break;
            }
        }
        stocksRepository.save(stocks);
    }

    // 상장코드 리스트
    public List<StockCodeProjection> findAllStockCodes() {
        return stocksRepository.findAllProjectedByStatus(true);
    }

    // 이름 검색
    public List<Stocks> findName(NameRequest nameRequest) {
        return stocksRepository.findByNameContaining(nameRequest.getName());
    }

    // 코드 검색
    public List<Stocks> findCode(CodeRequest codeRequest) {
        return stocksRepository.findByCodeContaining(codeRequest.getCode());
    }

    // 시장 종류 검색
    public Page<Stocks> findType(TypeRequest typeRequest, Pageable pageable) {
        return stocksRepository.findByMarketType(typeRequest.getMarketType(), pageable);
    }

    // 상장 리스트
    public List<Stocks> findAllStocks(SortRequest sortRequest) {
        Sort sort = "asc".equalsIgnoreCase(sortRequest.getMethod()) ?
                Sort.by(sortRequest.getField()).ascending() :
                Sort.by(sortRequest.getField()).descending();
        return stocksRepository.findAllByStatus(sort, true);
    }

    public StockPriceRequest findStockInformation(String code) {
        return stocksRepository.findByCode(code).stream()
                .findFirst()
                .map(information -> {
                    StockPriceRequest stockPriceRequest = new StockPriceRequest();
                    stockPriceRequest.setCode(information.getCode());
                    stockPriceRequest.setName(information.getName());
                    stockPriceRequest.setCeo(information.getCeo());
                    stockPriceRequest.setPlace(information.getPlace());
                    stockPriceRequest.setMarketType(information.getMarketType());
                    stockPriceRequest.setStatus(information.isStatus());
                    return stockPriceRequest;
                })
                .orElseThrow(() -> new EntityNotFoundException("Stock information not found for code: " + code));
    }
}
