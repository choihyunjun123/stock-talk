package stock.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Table(name = "stocks")
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Stocks {
    @Id
    @Column(name = "id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "ceo", nullable = false)
    private String ceo;

    @Column(name = "place", nullable = false)
    private String place;

    @Column(name = "market_type", nullable = false)
    private int marketType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "status", nullable = false)
    private boolean status;
}
