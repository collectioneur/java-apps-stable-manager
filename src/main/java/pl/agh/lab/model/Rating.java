package pl.agh.lab.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

@Entity
@Table(name = "ratings")
public class Rating implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rating_value", nullable = false)
    private int value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "horse_id", nullable = false)
    private Horse horse;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date ratingDate;

    @Column(nullable = false)
    private String description;

    protected Rating() {}

    public Rating(int value, Horse horse, Date ratingDate, String description) {
        if (value < 0 || value > 5) {
            throw new IllegalArgumentException("Rating value must be between 0 and 5");
        }
        Objects.requireNonNull(horse, "Horse is required");
        Objects.requireNonNull(ratingDate, "Date is required");

        this.value = value;
        this.horse = horse;
        this.ratingDate = new Date(ratingDate.getTime());
        this.description = (description == null ? "" : description);
    }

    public Horse getHorse() {
        return horse;
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT,
                "Rating{id=%d, value=%d, horse=%s, date=%s, desc='%s'}",
                id, value,
                horse != null ? horse.getName() : "null",
                ratingDate,
                description
        );
    }
}
