// src/ui/StableTableModel.java
package pl.agh.lab.ui;

import pl.agh.lab.model.Stable;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class StableTableModel extends AbstractTableModel {

    private final String[] columns = {"Name", "Current load", "Max capacity", "Total value (PLN)"};
    private List<Stable> stables = new ArrayList<>();

    public void setStables(List<Stable> stables) {
        this.stables = new ArrayList<>(stables);
        fireTableDataChanged();
    }

    public Stable getStableAt(int row) {
        if (row < 0 || row >= stables.size()) {
            return null;
        }
        return stables.get(row);
    }

    @Override
    public int getRowCount() {
        return stables.size();
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
        Stable s = stables.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> s.getStableName();
            case 1 -> s.getHorseList().size();
            case 2 -> s.getMaxCapacity();
            case 3 -> String.format("%.2f", s.totalValue());
            default -> null;
        };
    }
}
