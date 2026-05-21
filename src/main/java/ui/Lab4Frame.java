package ui;

import dto.PageResult;
import entity.Employee;
import repository.Lab4EmployeeRepository;
import service.Lab4PerformanceService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class Lab4Frame extends JFrame {

    private final Lab4PerformanceService performanceService = new Lab4PerformanceService();
    private final Lab4EmployeeRepository employeeRepository = new Lab4EmployeeRepository();

    private final JTextArea logArea = new JTextArea();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"id", "nume", "email", "salariu"},
            0
    );
    private final JTable employeeTable = new JTable(tableModel);
    private final JComboBox<Integer> pageSizeCombo = new JComboBox<>(new Integer[]{10, 25, 50, 100});

    private final JLabel pageLabel = new JLabel("pagina 1");
    private final JLabel totalLabel = new JLabel("total 0");

    private int currentPage = 0;
    private Long currentCursor = 0L;

    public Lab4Frame() {
        setTitle("Lab 4 - optimizare performanta baza de date");
        setSize(1350, 850);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        loadOffsetPage(0);
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new GridLayout(2, 4, 8, 8));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        JButton btnPrepareData = new JButton("genereaza 10000 date");
        JButton btnNPlusOne = new JButton("demo n+1");
        JButton btnJoinFetch = new JButton("rezolva join fetch");
        JButton btnNoIndex = new JButton("benchmark fara index");
        JButton btnWithIndex = new JButton("benchmark cu index");
        JButton btnPagination = new JButton("benchmark paginare");
        JButton btnCache = new JButton("demo cache");
        JButton btnBulk = new JButton("bulk + prepared");

        topPanel.add(btnPrepareData);
        topPanel.add(btnNPlusOne);
        topPanel.add(btnJoinFetch);
        topPanel.add(btnNoIndex);
        topPanel.add(btnWithIndex);
        topPanel.add(btnPagination);
        topPanel.add(btnCache);
        topPanel.add(btnBulk);

        add(topPanel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(employeeTable),
                new JScrollPane(logArea)
        );

        splitPane.setDividerLocation(330);
        add(splitPane, BorderLayout.CENTER);

        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton btnPrev = new JButton("anterior offset");
        JButton btnNext = new JButton("urmator offset");
        JButton btnCursorNext = new JButton("urmator cursor");
        JButton btnClear = new JButton("clear log");

        bottomPanel.add(new JLabel("dimensiune pagina:"));
        bottomPanel.add(pageSizeCombo);
        bottomPanel.add(btnPrev);
        bottomPanel.add(btnNext);
        bottomPanel.add(btnCursorNext);
        bottomPanel.add(pageLabel);
        bottomPanel.add(totalLabel);
        bottomPanel.add(btnClear);

        add(bottomPanel, BorderLayout.SOUTH);

        btnPrepareData.addActionListener(e -> runAsync(() -> {
            performanceService.prepareData(this::appendLog);
            loadOffsetPage(0);
        }));

        btnNPlusOne.addActionListener(e -> runAsync(() ->
                performanceService.demonstrateNPlusOne(this::appendLog)
        ));

        btnJoinFetch.addActionListener(e -> runAsync(() ->
                performanceService.solveNPlusOneWithJoinFetch(this::appendLog)
        ));

        btnNoIndex.addActionListener(e -> runAsync(() ->
                performanceService.runIndexBenchmarkWithoutIndexes(this::appendLog)
        ));

        btnWithIndex.addActionListener(e -> runAsync(() ->
                performanceService.runIndexBenchmarkWithIndexes(this::appendLog)
        ));

        btnPagination.addActionListener(e -> runAsync(() ->
                performanceService.runPaginationBenchmark(this::appendLog)
        ));

        btnCache.addActionListener(e -> runAsync(() ->
                performanceService.demonstrateCache(this::appendLog)
        ));

        btnBulk.addActionListener(e -> runAsync(() -> {
            performanceService.runBulkUpdateBenchmark(this::appendLog);
            performanceService.runPreparedStatementBenchmark(this::appendLog);
        }));

        btnPrev.addActionListener(e -> loadOffsetPage(Math.max(0, currentPage - 1)));
        btnNext.addActionListener(e -> loadOffsetPage(currentPage + 1));
        btnCursorNext.addActionListener(e -> loadCursorPage(currentCursor));
        btnClear.addActionListener(e -> logArea.setText(""));

        pageSizeCombo.addActionListener(e -> loadOffsetPage(0));
    }

    private int getPageSize() {
        return (Integer) pageSizeCombo.getSelectedItem();
    }

    private void loadOffsetPage(int pageNumber) {
        try {
            PageResult<Employee> page = employeeRepository.getEmployeesPageOffset(pageNumber, getPageSize());

            currentPage = page.getPageNumber();
            currentCursor = page.getLastId() == null ? 0L : page.getLastId();

            fillTable(page);

            pageLabel.setText("pagina " + (currentPage + 1) + " / " + page.getTotalPages());
            totalLabel.setText("total " + page.getTotalElements());
        } catch (Exception e) {
            appendLog("eroare la paginare offset: " + e.getMessage());
        }
    }

    private void loadCursorPage(Long lastId) {
        try {
            PageResult<Employee> page = employeeRepository.getEmployeesPageCursor(lastId, getPageSize());

            currentCursor = page.getLastId() == null ? 0L : page.getLastId();

            fillTable(page);

            pageLabel.setText("cursor lastId=" + currentCursor);
            totalLabel.setText("total " + page.getTotalElements());
        } catch (Exception e) {
            appendLog("eroare la paginare cursor: " + e.getMessage());
        }
    }

    private void fillTable(PageResult<Employee> page) {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);

            for (Employee employee : page.getContent()) {
                tableModel.addRow(new Object[]{
                        employee.getId(),
                        employee.getName(),
                        employee.getEmail(),
                        employee.getSalary()
                });
            }
        });
    }

    private void runAsync(Runnable runnable) {
        new Thread(() -> {
            try {
                runnable.run();
                appendLog("");
            } catch (Exception e) {
                appendLog("eroare: " + e.getMessage());

                if (e.getCause() != null) {
                    appendLog("cauza: " + e.getCause().getMessage());
                }
            }
        }).start();
    }

    private void appendLog(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }
}