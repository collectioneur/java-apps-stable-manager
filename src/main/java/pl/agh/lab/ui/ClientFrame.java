package pl.agh.lab.ui;

import pl.agh.lab.model.*;
import pl.agh.lab.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.util.List;

public class ClientFrame extends JFrame {

    private final StableService service;

    private StableTableModel stableTableModel;
    private HorseTableModel horseTableModel;

    private JTable stableTable;
    private JTable horseTable;

    private JTextField filterField;
    private JComboBox<Object> stateComboBox;
    private JButton requestContactButton;

    private JButton rateHorseButton;

    public ClientFrame(StableService service) {
        this.service = service;
        initUI();
        reloadStables();
    }

    private void initUI() {
        setTitle("Stable Manager – Client");
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

        requestContactButton = new JButton("Request contact");
        topBar.add(requestContactButton);
        topBar.add(Box.createHorizontalGlue());

        rateHorseButton = new JButton("Rate horse");
        topBar.add(rateHorseButton);
        rateHorseButton.addActionListener(e -> onRateHorse());

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
        filterField.addActionListener(e -> refreshHorsesWithFilters());
        stateComboBox.addActionListener(e -> refreshHorsesWithFilters());
        requestContactButton.addActionListener(e -> onRequestContact());
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
        filterField.setToolTipText("Filter by name / breed (press Enter)");

        stateComboBox = new JComboBox<>();
        stateComboBox.addItem("All states");
        for (HorseCondition c : HorseCondition.values()) {
            stateComboBox.addItem(c);
        }

        top.add(new JLabel("Filter: "));
        top.add(Box.createHorizontalStrut(4));
        top.add(filterField);
        top.add(Box.createHorizontalStrut(8));
        top.add(new JLabel("State: "));
        top.add(Box.createHorizontalStrut(4));
        top.add(stateComboBox);
        top.add(Box.createHorizontalGlue());

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(horseTable), BorderLayout.CENTER);

        return panel;
    }

    private void onStableSelectionChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        refreshHorsesWithFilters();
    }

    private void onRequestContact() {
        Stable stable = getSelectedStable();
        Horse horse = getSelectedHorse();

        String target;
        if (horse != null) {
            target = "about horse '" + horse.getName() + "' in stable '" +
                    (stable != null ? stable.getStableName() : "?") + "'";
        } else if (stable != null) {
            target = "about stable '" + stable.getStableName() + "'";
        } else {
            target = "about available horses";
        }

        JOptionPane.showMessageDialog(
                this,
                "Your contact request has been sent to the administrator " + target + ".",
                "Request sent",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void onRateHorse() {
        Horse horse = getSelectedHorse();
        if (horse == null) {
            showError("Select a horse first");
            return;
        }

        JTextField valueField = new JTextField();
        JTextField descField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 2, 4, 4));
        panel.add(new JLabel("Rating (0-5):"));
        panel.add(valueField);
        panel.add(new JLabel("Description:"));
        panel.add(descField);

        int res = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Add rating for " + horse.getName(),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (res != JOptionPane.OK_OPTION) return;

        try {
            int value = Integer.parseInt(valueField.getText().trim());
            String desc = descField.getText();

            service.addRatingToHorse(horse.getId(), value, desc);

            refreshHorsesWithFilters();

        } catch (NumberFormatException ex) {
            showError("Rating must be an integer between 0 and 5");
        } catch (ValidationException | HorseOperationException ex) {
            showError(ex.getMessage());
        }
    }

    private void reloadStables() {
        List<Stable> all = service.getAllStables();
        stableTableModel.setStables(all);
        if (!all.isEmpty()) {
            stableTable.setRowSelectionInterval(0, 0);
        }
        refreshHorsesWithFilters();
    }

    private void refreshHorsesWithFilters() {
        Stable stable = getSelectedStable();
        if (stable == null) {
            horseTableModel.setHorsesWithStats(List.of(), List.of());
            return;
        }

        String text = filterField.getText();
        Object selectedState = stateComboBox.getSelectedItem();
        HorseCondition condition = (selectedState instanceof HorseCondition)
                ? (HorseCondition) selectedState
                : null;

        List<Horse> horses = service.filterHorses(stable, text, condition);

        var ratingStats = service.getHorseRatingStatsForStable(stable);

        horseTableModel.setHorsesWithStats(horses, ratingStats);

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
