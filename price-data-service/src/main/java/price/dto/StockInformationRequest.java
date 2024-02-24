package price.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockInformationRequest {
    private String code;
    private String date;
}
