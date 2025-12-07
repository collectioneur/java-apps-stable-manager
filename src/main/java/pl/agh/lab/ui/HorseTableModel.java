package pl.agh.lab.ui;

import pl.agh.lab.model.Horse;
import pl.agh.lab.service.HorseRatingStat;

import javax.swing.table.AbstractTableModel;
import java.util.*;

public class HorseTableModel extends AbstractTableModel {

    private final String[] columns = {
            "Name", "Breed", "Type", "Status",
            "Age", "Price (PLN)", "Weight (kg)", "Height (cm)", "#Ratings", "Avg rating"
    };

    private List<Horse> horses = new ArrayList<>();
    private Map<String, HorseRatingStat> ratingStatsByName = new HashMap<>();

    public void setHorses(List<Horse> horses) {
        this.horses = new ArrayList<>(horses);
        fireTableDataChanged();
    }

    public void setHorsesWithStats(List<Horse> horses, List<HorseRatingStat> stats) {
        this.horses = new ArrayList<>(horses);
        this.ratingStatsByName = new HashMap<>();
        for (HorseRatingStat s : stats) {
            ratingStatsByName.put(s.getHorseName().toLowerCase(Locale.ROOT), s);
        }
        fireTableDataChanged();
    }


    public Horse getHorseAt(int row) {
        if (row < 0 || row >= horses.size()) {
            return null;
        }
        return horses.get(row);
    }

    @Override
    public int getRowCount() {
        return horses.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Horse h = horses.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> h.getName();
            case 1 -> h.getBreed();
            case 2 -> h.getType();
            case 3 -> h.getStatus();
            case 4 -> h.getAge();
            case 5 -> String.format("%.2f", h.getPrice());
            case 6 -> String.format("%.1f", h.getWeightKg());
            case 7 -> String.format("%.1f", h.getHeightCm());
            case 8 -> {
                HorseRatingStat stat = ratingStatsByName.get(
                        h.getName().toLowerCase(Locale.ROOT)
                );
                yield stat != null ? stat.getCount() : 0L;
            }
            case 9 -> {
                HorseRatingStat stat = ratingStatsByName.get(
                        h.getName().toLowerCase(Locale.ROOT)
                );
                yield stat != null
                        ? String.format(Locale.ROOT, "%.2f", stat.getAverage())
                        : "-";
            }
            default -> null;
        };
    }

}
