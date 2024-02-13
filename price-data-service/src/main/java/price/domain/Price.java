package price.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Table(name = "price")
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Price {
    @Id
    @Column(name = "id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_code", nullable = false)
    private String stockCode;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "open", nullable = false)
    private Long open;

    @Column(name = "high", nullable = false)
    private Long high;

    @Column(name = "low", nullable = false)
    private Long low;

    @Column(name = "close", nullable = false)
    private Long close;

    @Column(name = "volume")
    private Long volume;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
