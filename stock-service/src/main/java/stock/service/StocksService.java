package stock.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import stock.domain.Stocks;
import stock.repository.StocksRepository;

import java.io.InputStream;
import java.time.LocalDateTime;

@Service
public class StocksService {

    private final StocksRepository stocksRepository;

    @Autowired
    public StocksService(StocksRepository stocksRepository) {
        this.stocksRepository = stocksRepository;
    }

    // 엑셀 파일 데이터 저장
    public void uploadExcelFile(MultipartFile file, int type) {
        try (InputStream in = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(in)) {
            processSheet(workbook.getSheetAt(0), type);
        } catch (Exception e) {
            System.err.println("Error reading excel file: " + e.getMessage());
        }
    }

    // 시작 페이지
    private void processSheet(Sheet sheet, int type) {
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // 헤더 라인 스킵
            parseRow(row, type);
        }
    }

    // 열 별 데이터 저장
    private void parseRow(Row row, int type) {
        Stocks stocks = new Stocks();
        stocks.setCreatedAt(LocalDateTime.now());
        stocks.setMarketType(type);
        for (Cell cell : row) {
            switch (cell.getColumnIndex()) {
                case 0:
                    stocks.setName(cell.getStringCellValue());
                    break;
                case 1:
                    stocks.setCode(cell.getStringCellValue());
                    break;
                case 6:
                    stocks.setCeo(cell.getStringCellValue());
                    break;
                case 8:
                    stocks.setPlace(cell.getStringCellValue());
                    break;
                default:
                    break;
            }
        }
        stocksRepository.save(stocks);
    }
}
