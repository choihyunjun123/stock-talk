package price.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChartRequest {
    private String code;
    private String period;
    private String startDate;
}
