package price.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class StockPriceRequest {
    private String code;
    private String name;
    private String ceo;
    private String place;
    private int marketType;
    private boolean status;
    private LocalDate Date;
    private Long open;
    private Long high;
    private Long low;
    private Long close;
    private Long volume;
}
