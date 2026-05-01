package ui;

import entity.Categorie;
import entity.Produs;
import service.CategorieService;
import service.PerformanceTestService;
import service.ProdusService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MainFrame extends JFrame {

    private JTable tableCategorii;
    private JTable tableProduse;

    private DefaultTableModel modelCategorii;
    private DefaultTableModel modelProduse;

    private JTextField txtNume;
    private JTextField txtPret;
    private JTextField txtStoc;

    private JButton btnAdauga;
    private JButton btnModifica;
    private JButton btnSterge;
    private JButton btnRefresh;
    private JButton btnTestPerformanta;
    private JButton btnTestLeak;
    private JButton btnEagerLoad;

    private final CategorieService categorieService = new CategorieService();
    private final ProdusService produsService = new ProdusService();
    private final PerformanceTestService performanceTestService = new PerformanceTestService();

    public MainFrame() {
        setTitle("Magazin ORM - Hibernate + HikariCP");
        setSize(1300, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        incarcaCategoriiInitial();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        modelCategorii = new DefaultTableModel(new String[]{"ID", "Nume"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        modelProduse = new DefaultTableModel(new String[]{"ID", "Nume", "Pret", "Stoc"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableCategorii = new JTable(modelCategorii);
        tableProduse = new JTable(modelProduse);

        JScrollPane scrollCategorii = new JScrollPane(tableCategorii);
        JScrollPane scrollProduse = new JScrollPane(tableProduse);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollCategorii, scrollProduse);
        splitPane.setDividerLocation(350);
        add(splitPane, BorderLayout.CENTER);

        JPanel panelJos = new JPanel(new BorderLayout());

        JPanel panelCampuri = new JPanel(new GridLayout(1, 6, 10, 10));
        panelCampuri.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panelCampuri.add(new JLabel("nume"));
        txtNume = new JTextField();
        panelCampuri.add(txtNume);

        panelCampuri.add(new JLabel("pret"));
        txtPret = new JTextField();
        panelCampuri.add(txtPret);

        panelCampuri.add(new JLabel("stoc"));
        txtStoc = new JTextField();
        panelCampuri.add(txtStoc);

        JPanel panelButoane = new JPanel(new GridLayout(2, 4, 10, 10));
        panelButoane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        btnAdauga = new JButton("adauga");
        btnModifica = new JButton("modifica");
        btnSterge = new JButton("sterge");
        btnRefresh = new JButton("refresh");
        btnTestPerformanta = new JButton("test performanta");
        btnTestLeak = new JButton("test leak");
        btnEagerLoad = new JButton("eager load");
        JButton btnAdaugaCategorieDemo = new JButton("adauga categorie demo");

        panelButoane.add(btnAdauga);
        panelButoane.add(btnModifica);
        panelButoane.add(btnSterge);
        panelButoane.add(btnRefresh);
        panelButoane.add(btnTestPerformanta);
        panelButoane.add(btnTestLeak);
        panelButoane.add(btnEagerLoad);
        panelButoane.add(btnAdaugaCategorieDemo);

        panelJos.add(panelCampuri, BorderLayout.NORTH);
        panelJos.add(panelButoane, BorderLayout.SOUTH);

        add(panelJos, BorderLayout.SOUTH);

        tableCategorii.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Integer categorieId = getSelectedCategorieId();
                if (categorieId != null) {
                    loadProduse(categorieId);
                    clearFields();
                }
            }
        });

        tableProduse.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tableProduse.getSelectedRow();
                if (row >= 0) {
                    txtNume.setText(modelProduse.getValueAt(row, 1).toString());
                    txtPret.setText(modelProduse.getValueAt(row, 2).toString());
                    txtStoc.setText(modelProduse.getValueAt(row, 3).toString());
                }
            }
        });

        btnAdauga.addActionListener(e -> adaugaProdus());
        btnModifica.addActionListener(e -> modificaProdus());
        btnSterge.addActionListener(e -> stergeProdus());
        btnRefresh.addActionListener(e -> refreshProduse());

        btnTestPerformanta.addActionListener(e -> ruleazaTestPerformanta());
        btnTestLeak.addActionListener(e -> ruleazaTestLeak());
        btnEagerLoad.addActionListener(e -> ruleazaDemoEagerLoad());

        btnAdaugaCategorieDemo.addActionListener(e -> {
            try {
                String nume = JOptionPane.showInputDialog(this, "numele categoriei:");
                if (nume != null && !nume.trim().isEmpty()) {
                    categorieService.adaugaCategorie(nume.trim());
                    loadCategorii();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "eroare", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void incarcaCategoriiInitial() {
        List<Categorie> categorii = categorieService.getAllCategorii();

        if (categorii.isEmpty()) {
            categorieService.adaugaCategorie("lactate");
            categorieService.adaugaCategorie("dulciuri");
            categorieService.adaugaCategorie("bauturi");
        }

        loadCategorii();
    }

    private void loadCategorii() {
        modelCategorii.setRowCount(0);

        List<Categorie> categorii = categorieService.getAllCategorii();
        for (Categorie categorie : categorii) {
            modelCategorii.addRow(new Object[]{categorie.getId(), categorie.getNume()});
        }

        if (tableCategorii.getRowCount() > 0) {
            tableCategorii.setRowSelectionInterval(0, 0);
        }
    }

    private void loadProduse(int categorieId) {
        modelProduse.setRowCount(0);

        List<Produs> produse = produsService.getProduseByCategorieId(categorieId);
        for (Produs produs : produse) {
            modelProduse.addRow(new Object[]{
                    produs.getId(),
                    produs.getNume(),
                    produs.getPret(),
                    produs.getStoc()
            });
        }
    }

    private Integer getSelectedCategorieId() {
        int row = tableCategorii.getSelectedRow();
        if (row < 0) {
            return null;
        }
        return (Integer) modelCategorii.getValueAt(row, 0);
    }

    private Integer getSelectedProdusId() {
        int row = tableProduse.getSelectedRow();
        if (row < 0) {
            return null;
        }
        return (Integer) modelProduse.getValueAt(row, 0);
    }

    private void adaugaProdus() {
        Integer categorieId = getSelectedCategorieId();
        if (categorieId == null) {
            JOptionPane.showMessageDialog(this, "selecteaza o categorie", "eroare", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String nume = txtNume.getText().trim();
            double pret = Double.parseDouble(txtPret.getText().trim());
            int stoc = Integer.parseInt(txtStoc.getText().trim());

            produsService.adaugaProdus(nume, pret, stoc, categorieId);

            loadProduse(categorieId);
            clearFields();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "pretul si stocul trebuie sa fie numere valide", "eroare", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "eroare", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modificaProdus() {
        Integer produsId = getSelectedProdusId();
        Integer categorieId = getSelectedCategorieId();

        if (produsId == null) {
            JOptionPane.showMessageDialog(this, "selecteaza un produs", "eroare", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String nume = txtNume.getText().trim();
            double pret = Double.parseDouble(txtPret.getText().trim());
            int stoc = Integer.parseInt(txtStoc.getText().trim());

            produsService.modificaProdus(produsId, nume, pret, stoc);

            loadProduse(categorieId);
            clearFields();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "pretul si stocul trebuie sa fie numere valide", "eroare", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "eroare", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void stergeProdus() {
        Integer produsId = getSelectedProdusId();
        Integer categorieId = getSelectedCategorieId();

        if (produsId == null) {
            JOptionPane.showMessageDialog(this, "selecteaza un produs", "eroare", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirmare = JOptionPane.showConfirmDialog(
                this,
                "esti sigur ca vrei sa stergi produsul?",
                "confirmare stergere",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmare == JOptionPane.YES_OPTION) {
            try {
                produsService.stergeProdus(produsId);
                loadProduse(categorieId);
                clearFields();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "eroare", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshProduse() {
        Integer categorieId = getSelectedCategorieId();
        if (categorieId != null) {
            loadProduse(categorieId);
        }
    }

    private void ruleazaTestPerformanta() {
        new Thread(() -> {
            StringBuilder rezultat = new StringBuilder();

            try {
                performanceTestService.ruleazaTestConexiuni(text -> rezultat.append(text).append("\n"));
                SwingUtilities.invokeLater(() ->
                        afiseazaText("rezultate test performanta", rezultat.toString())
                );
            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, e.getMessage(), "eroare", JOptionPane.ERROR_MESSAGE)
                );
            }
        }).start();
    }

    private void ruleazaTestLeak() {
        new Thread(() -> {
            StringBuilder rezultat = new StringBuilder();

            try {
                performanceTestService.demonstreazaConnectionLeak(text -> rezultat.append(text).append("\n"));
                SwingUtilities.invokeLater(() ->
                        afiseazaText("rezultate test leak", rezultat.toString())
                );
            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, e.getMessage(), "eroare", JOptionPane.ERROR_MESSAGE)
                );
            }
        }).start();
    }

    private void ruleazaDemoEagerLoad() {
        Integer categorieId = getSelectedCategorieId();
        if (categorieId == null) {
            JOptionPane.showMessageDialog(this, "selecteaza o categorie", "eroare", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Categorie categorie = categorieService.getCategorieWithProduse(categorieId);

            StringBuilder sb = new StringBuilder();
            sb.append("categorie: ").append(categorie.getNume()).append("\n");
            sb.append("numar produse incarcate eager: ").append(categorie.getProduse().size()).append("\n\n");

            for (Produs produs : categorie.getProduse()) {
                sb.append(" - ")
                        .append(produs.getNume())
                        .append(", pret=")
                        .append(produs.getPret())
                        .append(", stoc=")
                        .append(produs.getStoc())
                        .append("\n");
            }

            afiseazaText("demo eager loading", sb.toString());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "eroare", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void afiseazaText(String titlu, String text) {
        JTextArea textArea = new JTextArea(text);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(700, 400));

        JOptionPane.showMessageDialog(this, scrollPane, titlu, JOptionPane.INFORMATION_MESSAGE);
    }

    private void clearFields() {
        txtNume.setText("");
        txtPret.setText("");
        txtStoc.setText("");
        tableProduse.clearSelection();
    }
}