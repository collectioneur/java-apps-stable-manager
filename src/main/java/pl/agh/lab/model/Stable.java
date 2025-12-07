package pl.agh.lab.model;

import jakarta.persistence.*;
import java.util.*;
import java.util.stream.Collectors;
import java.io.Serializable;

@Entity
@Table(name = "stables")
public class Stable implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stable_name", nullable = false, unique = true)
    private String stableName;



    @OneToMany(
            mappedBy = "stable",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    private List<Horse> horseList = new ArrayList<>();

    @Column(nullable = false)
    private int maxCapacity;

    protected Stable() {
    }

    public Stable(String stableName, int maxCapacity) {
        if (stableName == null || stableName.isBlank()) throw new IllegalArgumentException("stableName is required");
        if (maxCapacity <= 0) throw new IllegalArgumentException("maxCapacity must be > 0");
        this.stableName = stableName;
        this.maxCapacity = maxCapacity;
    }

    public Long getId() { return id; }


    public void addHorse(Horse horse) {
        Objects.requireNonNull(horse);
        boolean exists = horseList.stream().anyMatch(horse::equals);
        if (exists) {
            System.out.printf("Horse already exists: %s%n", horse);
            return;
        }
        if (horseList.size() >= maxCapacity) {
            System.err.printf("Stable '%s' is full (%d). Cannot add: %s%n",
                    stableName, maxCapacity, horse.getName());
            return;
        }
        horseList.add(horse);
        horse.setStable(this);
    }



    public boolean isEmpty() { return horseList.isEmpty(); }
    public List<Horse> getHorseList() { return Collections.unmodifiableList(horseList); }
    public int getMaxCapacity() { return maxCapacity; }
    public String getStableName() { return stableName; }

    public double totalValue() {
        return horseList.stream().mapToDouble(Horse::getPrice).sum();
    }
}
