package pl.agh.lab.model;

import jakarta.persistence.*;
import java.util.*;
import java.util.Comparator;
import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "horses")
public class Horse implements Comparable<Horse>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String breed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HorseType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HorseCondition status;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private double weightKg;

    @Column(nullable = false)
    private double heightCm;

    @Column
    private String microchipId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date acquisitionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stable_id")
    @JsonIgnore
    private Stable stable;


    protected Horse() {
    }

    public Horse(String name,
                 String breed,
                 HorseType type,
                 HorseCondition status,
                 int age,
                 double price,
                 double weightKg,
                 double heightCm,
                 String microchipId,
                 Date acquisitionDate) {

        if (name == null || name.isBlank()) throw new IllegalArgumentException("name is required");
        if (breed == null || breed.isBlank()) throw new IllegalArgumentException("breed is required");
        if (type == null) throw new IllegalArgumentException("type is required");
        if (status == null) throw new IllegalArgumentException("status is required");
        if (age < 0) throw new IllegalArgumentException("age must be >= 0");
        if (price < 0) throw new IllegalArgumentException("price must be >= 0");
        if (weightKg <= 0) throw new IllegalArgumentException("weight must be > 0");
        if (heightCm <= 0) throw new IllegalArgumentException("height must be > 0");

        this.name = name;
        this.breed = breed;
        this.type = type;
        this.status = status;
        this.age = age;
        this.price = price;
        this.weightKg = weightKg;
        this.heightCm = heightCm;
        this.microchipId = microchipId == null ? "" : microchipId;
        this.acquisitionDate = acquisitionDate == null ? new Date() : new Date(acquisitionDate.getTime());
    }

    public Long getId() { return id; }

    public String getName() { return name; }
    public String getBreed() { return breed; }
    public HorseType getType() { return type; }
    public HorseCondition getStatus() { return status; }
    public int getAge() { return age; }
    public double getPrice() { return price; }
    public double getWeightKg() { return weightKg; }
    public double getHeightCm() { return heightCm; }
    public String getMicrochipId() { return microchipId; }
    public Date getAcquisitionDate() { return acquisitionDate; }

    public Stable getStable() { return stable; }
    public void setStable(Stable stable) { this.stable = stable; }

    public void setStatus(HorseCondition status) {
        this.status = Objects.requireNonNull(status);
    }

    public void changeWeight(double delta) {
        double w = this.weightKg + delta;
        if (w <= 0) throw new IllegalArgumentException("weight must be > 0");
        this.weightKg = w;
    }

    @Override
    public int compareTo(Horse o) {
        return Comparator
                .comparing(Horse::getName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Horse::getBreed, String.CASE_INSENSITIVE_ORDER)
                .thenComparingInt(Horse::getAge)
                .compare(this, o);
    }

    public static final Comparator<Horse> BY_NAME =
            Comparator.comparing(Horse::getName, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(Horse::getBreed, String.CASE_INSENSITIVE_ORDER)
                    .thenComparingInt(Horse::getAge);

    public static final Comparator<Horse> BY_PRICE_ASC =
            Comparator.comparingDouble(Horse::getPrice)
                    .thenComparing(Horse::getName, String.CASE_INSENSITIVE_ORDER);


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Horse)) return false;
        Horse h = (Horse) o;
        return name.equalsIgnoreCase(h.name)
                && breed.equalsIgnoreCase(h.breed)
                && age == h.age;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase(Locale.ROOT), breed.toLowerCase(Locale.ROOT), age);
    }

    @Override
    public String toString() {
        return String.format("%s (%s, %d years) [%.2f PLN, %s]", name, breed, age, price, status);
    }
}
