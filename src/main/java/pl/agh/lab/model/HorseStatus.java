package pl.agh.lab.model;

public enum HorseStatus {
    ZDROWY, CHORY, TRENING, KARENCJA, SPRZEDANY;

    static HorseStatus from(HorseCondition c) {
        return HorseStatus.valueOf(c.name());
    }

    HorseCondition toCondition() {
        return HorseCondition.valueOf(this.name());
    }
}
