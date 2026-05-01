package ui;

import service.BatchPerformanceService;
import service.TransactionDemoService;

import javax.swing.*;
import java.awt.*;

public class Lab2Frame extends JFrame {

    private final JTextArea logArea = new JTextArea();
    private final TransactionDemoService transactionDemoService = new TransactionDemoService();
    private final BatchPerformanceService batchPerformanceService = new BatchPerformanceService();

    public Lab2Frame() {
        setTitle("lab 2 - tranzactii si niveluri de izolare");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel(new GridLayout(3, 3, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton btnReset = new JButton("reset data");
        JButton btnDirtyRead = new JButton("dirty read");
        JButton btnNonRepeatable = new JButton("non-repeatable read");
        JButton btnPhantom = new JButton("phantom read");
        JButton btnLostUpdate = new JButton("lost update");
        JButton btnDeadlock = new JButton("deadlock");
        JButton btnBatch = new JButton("batch performance");
        JButton btnClear = new JButton("clear log");

        buttonPanel.add(btnReset);
        buttonPanel.add(btnDirtyRead);
        buttonPanel.add(btnNonRepeatable);
        buttonPanel.add(btnPhantom);
        buttonPanel.add(btnLostUpdate);
        buttonPanel.add(btnDeadlock);
        buttonPanel.add(btnBatch);
        buttonPanel.add(btnClear);

        add(buttonPanel, BorderLayout.NORTH);

        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        btnReset.addActionListener(e -> runAsync(() -> transactionDemoService.resetDemoData(this::appendLog)));
        btnDirtyRead.addActionListener(e -> runAsync(() -> transactionDemoService.runDirtyRead(this::appendLog)));
        btnNonRepeatable.addActionListener(e -> runAsync(() -> transactionDemoService.runNonRepeatableRead(this::appendLog)));
        btnPhantom.addActionListener(e -> runAsync(() -> transactionDemoService.runPhantomRead(this::appendLog)));
        btnLostUpdate.addActionListener(e -> runAsync(() -> transactionDemoService.runLostUpdate(this::appendLog)));
        btnDeadlock.addActionListener(e -> runAsync(() -> transactionDemoService.runDeadlock(this::appendLog)));
        btnBatch.addActionListener(e -> runAsync(() -> batchPerformanceService.runBatchComparison(this::appendLog)));
        btnClear.addActionListener(e -> logArea.setText(""));
    }

    private void runAsync(Runnable runnable) {
        new Thread(() -> {
            try {
                runnable.run();
                appendLog("");
            } catch (Exception e) {
                appendLog("eroare: " + e.getMessage());
            }
        }).start();
    }

    private void appendLog(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }
}