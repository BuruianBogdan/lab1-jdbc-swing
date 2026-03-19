package ui;

import dao.CategorieDAO;
import dao.ProdusDAO;
import model.Categorie;
import model.Produs;

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

    private CategorieDAO categorieDAO = new CategorieDAO();
    private ProdusDAO produsDAO = new ProdusDAO();

    public MainFrame() {
        setTitle("Magazin JDBC");
        setSize(1200, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        loadCategorii();
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

        JPanel panelButoane = new JPanel(new GridLayout(1, 4, 10, 10));
        panelButoane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        btnAdauga = new JButton("adauga");
        btnModifica = new JButton("modifica");
        btnSterge = new JButton("sterge");
        btnRefresh = new JButton("refresh");

        panelButoane.add(btnAdauga);
        panelButoane.add(btnModifica);
        panelButoane.add(btnSterge);
        panelButoane.add(btnRefresh);

        panelJos.add(panelCampuri, BorderLayout.NORTH);
        panelJos.add(panelButoane, BorderLayout.SOUTH);

        add(panelJos, BorderLayout.SOUTH);

        tableCategorii.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tableCategorii.getSelectedRow();
                if (row >= 0) {
                    int categorieId = (int) modelCategorii.getValueAt(row, 0);
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
    }

    private void loadCategorii() {
        modelCategorii.setRowCount(0);

        List<Categorie> categorii = categorieDAO.getAllCategorii();
        for (Categorie c : categorii) {
            modelCategorii.addRow(new Object[]{c.getId(), c.getNume()});
        }

        if (tableCategorii.getRowCount() > 0) {
            tableCategorii.setRowSelectionInterval(0, 0);
        }
    }

    private void loadProduse(int categorieId) {
        modelProduse.setRowCount(0);

        List<Produs> produse = produsDAO.getProduseByCategorieId(categorieId);
        for (Produs p : produse) {
            modelProduse.addRow(new Object[]{
                    p.getId(),
                    p.getNume(),
                    p.getPret(),
                    p.getStoc()
            });
        }
    }

    private Integer getSelectedCategorieId() {
        int row = tableCategorii.getSelectedRow();
        if (row < 0) {
            return null;
        }
        return (int) modelCategorii.getValueAt(row, 0);
    }

    private Integer getSelectedProdusId() {
        int row = tableProduse.getSelectedRow();
        if (row < 0) {
            return null;
        }
        return (int) modelProduse.getValueAt(row, 0);
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

            if (nume.isEmpty()) {
                JOptionPane.showMessageDialog(this, "numele nu poate fi gol", "eroare", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (pret <= 0) {
                JOptionPane.showMessageDialog(this, "pretul trebuie sa fie mai mare ca 0", "eroare", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (stoc < 0) {
                JOptionPane.showMessageDialog(this, "stocul nu poate fi negativ", "eroare", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Produs produs = new Produs(0, nume, pret, stoc, categorieId);
            produsDAO.addProdus(produs);

            loadProduse(categorieId);
            clearFields();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "pretul si stocul trebuie sa fie numere valide", "eroare", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "eroare baza de date", JOptionPane.ERROR_MESSAGE);
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

            if (nume.isEmpty()) {
                JOptionPane.showMessageDialog(this, "numele nu poate fi gol", "eroare", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (pret <= 0) {
                JOptionPane.showMessageDialog(this, "pretul trebuie sa fie mai mare ca 0", "eroare", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (stoc < 0) {
                JOptionPane.showMessageDialog(this, "stocul nu poate fi negativ", "eroare", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Produs produs = new Produs(produsId, nume, pret, stoc, categorieId);
            produsDAO.updateProdus(produs);

            loadProduse(categorieId);
            clearFields();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "pretul si stocul trebuie sa fie numere valide", "eroare", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "eroare baza de date", JOptionPane.ERROR_MESSAGE);
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
                produsDAO.deleteProdus(produsId);
                loadProduse(categorieId);
                clearFields();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "eroare baza de date", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshProduse() {
        Integer categorieId = getSelectedCategorieId();
        if (categorieId != null) {
            loadProduse(categorieId);
        }
    }

    private void clearFields() {
        txtNume.setText("");
        txtPret.setText("");
        txtStoc.setText("");
        tableProduse.clearSelection();
    }
}