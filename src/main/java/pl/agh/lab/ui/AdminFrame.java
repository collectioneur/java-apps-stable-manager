// src/ui/AdminFrame.java
package pl.agh.lab.ui;

import pl.agh.lab.model.*;
import pl.agh.lab.service.HorseOperationException;
import pl.agh.lab.service.StableOperationException;
import pl.agh.lab.service.StableService;
import pl.agh.lab.service.ValidationException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.util.List;

public class AdminFrame extends JFrame {

    private final StableService service;

    private StableTableModel stableTableModel;
    private HorseTableModel horseTableModel;

    private JTable stableTable;
    private JTable horseTable;

    private JTextField filterField;
    private JComboBox<Object> stateComboBox;

    private JButton addStableButton;
    private JButton removeStableButton;
    private JButton sortStableButton;

    private JButton addHorseButton;
    private JButton removeHorseButton;

    public AdminFrame(StableService service) {
        this.service = service;
        initUI();
        reloadStables(false);
    }

    private void initUI() {
        setTitle("Stable Manager – Admin");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(960, 600);
        setLocationRelativeTo(null);


        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 247, 250));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        setContentPane(root);

        JPanel topBar = new JPanel();
        topBar.setOpaque(false);
        topBar.setLayout(new BoxLayout(topBar, BoxLayout.X_AXIS));

        addStableButton = new JButton("Add stable");
        removeStableButton = new JButton("Remove stable");
        sortStableButton = new JButton("Sort stables by load");

        topBar.add(addStableButton);
        topBar.add(Box.createHorizontalStrut(6));
        topBar.add(removeStableButton);
        topBar.add(Box.createHorizontalStrut(6));
        topBar.add(sortStableButton);
        topBar.add(Box.createHorizontalGlue());

        root.add(topBar, BorderLayout.NORTH);

        stableTableModel = new StableTableModel();
        horseTableModel = new HorseTableModel();

        stableTable = new JTable(stableTableModel);
        horseTable = new JTable(horseTableModel);

        configureTable(stableTable);
        configureTable(horseTable);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                wrapInCard("Stables", new JScrollPane(stableTable)),
                wrapInCard("Horses", createHorsePanel())
        );
        splitPane.setResizeWeight(0.35);
        splitPane.setBorder(null);

        root.add(splitPane, BorderLayout.CENTER);

        stableTable.getSelectionModel().addListSelectionListener(this::onStableSelectionChanged);
        addStableButton.addActionListener(e -> onAddStable());
        removeStableButton.addActionListener(e -> onRemoveStable());
        sortStableButton.addActionListener(e -> onSortStables());

        addHorseButton.addActionListener(e -> onAddHorse());
        removeHorseButton.addActionListener(e -> onRemoveHorse());

        filterField.addActionListener(e -> refreshHorsesWithFilters());
        stateComboBox.addActionListener(e -> refreshHorsesWithFilters());
    }

    private void configureTable(JTable table) {
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private JPanel wrapInCard(String title, JComponent content) {
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(true);
        card.setBackground(new Color(255, 255, 255, 235));
        card.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel label = new JLabel(title);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(label, BorderLayout.WEST);

        card.add(header, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);

        return card;
    }

    private JComponent createHorsePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));

        filterField = new JTextField();
        filterField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        filterField.putClientProperty("JComponent.sizeVariant", "small");
        filterField.setToolTipText("Filter by name / breed (press Enter)");

        stateComboBox = new JComboBox<>();
        stateComboBox.addItem("All states");
        for (HorseCondition c : HorseCondition.values()) {
            stateComboBox.addItem(c);
        }

        addHorseButton = new JButton("Add horse");
        removeHorseButton = new JButton("Remove horse");

        top.add(new JLabel("Filter: "));
        top.add(Box.createHorizontalStrut(4));
        top.add(filterField);
        top.add(Box.createHorizontalStrut(8));
        top.add(new JLabel("State: "));
        top.add(Box.createHorizontalStrut(4));
        top.add(stateComboBox);
        top.add(Box.createHorizontalStrut(16));
        top.add(addHorseButton);
        top.add(Box.createHorizontalStrut(4));
        top.add(removeHorseButton);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(horseTable), BorderLayout.CENTER);

        return panel;
    }


    private void onStableSelectionChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        refreshHorsesWithFilters();
    }

    private void onAddStable() {
        String name = JOptionPane.showInputDialog(this, "Stable name:", "Add stable",
                JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.isBlank()) return;

        String capacityStr = JOptionPane.showInputDialog(this, "Max capacity:", "Add stable",
                JOptionPane.PLAIN_MESSAGE);
        if (capacityStr == null || capacityStr.isBlank()) return;

        try {
            int capacity = Integer.parseInt(capacityStr.trim());
            service.addStable(name.trim(), capacity);
            reloadStables(false);
        } catch (NumberFormatException ex) {
            showError("Capacity must be an integer");
        } catch (ValidationException ex) {
            showError(ex.getMessage());
        }
    }

    private void onRemoveStable() {
        Stable stable = getSelectedStable();
        if (stable == null) {
            showError("No stable selected");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Remove stable '" + stable.getStableName() + "'?",
                "Confirm remove",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            service.removeStable(stable);
            reloadStables(false);
            horseTableModel.setHorses(List.of());
        } catch (StableOperationException ex) {
            showError(ex.getMessage());
        }
    }

    private void onSortStables() {
        List<Stable> sorted = service.sortStablesByCurrentLoad();
        stableTableModel.setStables(sorted);
    }

    private void onAddHorse() {
        Stable stable = getSelectedStable();
        if (stable == null) {
            showError("Select a stable first");
            return;
        }

        JTextField nameField = new JTextField();
        JTextField breedField = new JTextField();
        JComboBox<HorseCondition> statusBox = new JComboBox<>(HorseCondition.values());
        JTextField ageField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField weightField = new JTextField();
        JTextField heightField = new JTextField();
        JTextField chipField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 2, 4, 4));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Breed:"));
        panel.add(breedField);
        panel.add(new JLabel("Type (enum name):"));
        JTextField typeField = new JTextField("GORACOKRWISTY");
        panel.add(typeField);
        panel.add(new JLabel("Status:"));
        panel.add(statusBox);
        panel.add(new JLabel("Age:"));
        panel.add(ageField);
        panel.add(new JLabel("Price:"));
        panel.add(priceField);
        panel.add(new JLabel("Weight (kg):"));
        panel.add(weightField);
        panel.add(new JLabel("Height (cm):"));
        panel.add(heightField);
        panel.add(new JLabel("Microchip id:"));
        panel.add(chipField);

        int res = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Add horse",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (res != JOptionPane.OK_OPTION) return;

        try {
            String name = nameField.getText();
            String breed = breedField.getText();
            String typeStr = typeField.getText();
            HorseCondition status = (HorseCondition) statusBox.getSelectedItem();
            int age = Integer.parseInt(ageField.getText().trim());
            double price = Double.parseDouble(priceField.getText().trim());
            double weight = Double.parseDouble(weightField.getText().trim());
            double height = Double.parseDouble(heightField.getText().trim());
            String chip = chipField.getText();

            var type = Enum.valueOf(pl.agh.lab.model.HorseType.class, typeStr.trim());

            service.addHorse(
                    stable,
                    name,
                    breed,
                    type,
                    status,
                    age,
                    price,
                    weight,
                    height,
                    chip,
                    new java.util.Date()
            );
            refreshHorsesWithFilters();
            reloadStables(false);
        } catch (NumberFormatException ex) {
            showError("Numeric fields (age, price, weight, height) must be valid numbers");
        } catch (IllegalArgumentException ex) {
            showError("Invalid horse type enum name");
        } catch (ValidationException | StableOperationException | HorseOperationException ex) {
            showError(ex.getMessage());
        }
    }

    private void onRemoveHorse() {
        Stable stable = getSelectedStable();
        if (stable == null) {
            showError("Select a stable first");
            return;
        }
        Horse horse = getSelectedHorse();
        if (horse == null) {
            showError("No horse selected");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Remove horse '" + horse.getName() + "'?",
                "Confirm remove",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            service.removeHorse(stable, horse);
            refreshHorsesWithFilters();
            reloadStables(false);
        } catch (StableOperationException | HorseOperationException ex) {
            showError(ex.getMessage());
        }
    }

    private void reloadStables(boolean keepSelection) {
        int selectedRow = stableTable.getSelectedRow();
        Stable previouslySelected = null;
        if (keepSelection && selectedRow >= 0) {
            previouslySelected = stableTableModel.getStableAt(selectedRow);
        }

        List<Stable> all = service.getAllStables();
        stableTableModel.setStables(all);

        if (keepSelection && previouslySelected != null) {
            for (int i = 0; i < all.size(); i++) {
                if (all.get(i).getStableName().equals(previouslySelected.getStableName())) {
                    stableTable.setRowSelectionInterval(i, i);
                    break;
                }
            }
        } else if (!all.isEmpty()) {
            stableTable.setRowSelectionInterval(0, 0);
        }
        refreshHorsesWithFilters();
    }

    private void refreshHorsesWithFilters() {
        Stable stable = getSelectedStable();
        if (stable == null) {
            horseTableModel.setHorses(List.of());
            return;
        }

        String text = filterField.getText();
        Object selectedState = stateComboBox.getSelectedItem();
        HorseCondition condition = (selectedState instanceof HorseCondition)
                ? (HorseCondition) selectedState
                : null;

        try {
            List<Horse> horses = service.filterHorses(stable, text, condition);
            var ratingStats = service.getHorseRatingStatsForStable(stable);

            horseTableModel.setHorsesWithStats(horses, ratingStats);

        } catch (StableOperationException ex) {
            showError(ex.getMessage());
        }
    }

    private Stable getSelectedStable() {
        int row = stableTable.getSelectedRow();
        return stableTableModel.getStableAt(row);
    }

    private Horse getSelectedHorse() {
        int row = horseTable.getSelectedRow();
        return horseTableModel.getHorseAt(row);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
