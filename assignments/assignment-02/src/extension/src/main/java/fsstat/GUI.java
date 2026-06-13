package fsstat;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

/**
 * - La callback onUpdate arriva dall'event-loop di Vert.x
 * - Gli aggiornamenti alla GUI DEVONO passare per SwingUtilities.invokeLater()
 *   perché Swing è single-threaded sul proprio thread EDT.
 */
public class GUI extends JFrame {

    private static final long DEFAULT_MAX_FS = 1_000_000L;
    private static final int DEFAULT_NB = 8;
    private static final Set<String> EXCLUDED = Set.of("logs");

    private final FSStatLibInteractive lib = new FSStatLibInteractive();

    private final JTextField dirField;
    private final JButton startButton;
    private final JButton stopButton;
    private final JTextArea reportArea;
    private final JLabel statusLabel;

    public GUI() {
        super("FSStatLib — Interactive Scanner");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);

        // Directory input + bottoni
        JPanel topPanel = new JPanel(new BorderLayout(8, 0));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        dirField = new JTextField("");
        startButton = new JButton("Start");
        stopButton  = new JButton("Stop");
        stopButton.setEnabled(false);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        topPanel.add(new JLabel("Directory: "), BorderLayout.WEST);
        topPanel.add(dirField, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        // Area report
        reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(reportArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        statusLabel = new JLabel("Insert a directory path...");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 10, 6, 10));

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        startButton.addActionListener(e -> startScan());
        stopButton.addActionListener(e -> stopScan());
    }

    private void startScan() {
        String dir = dirField.getText().trim();
        if (dir.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Insert a directory path.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        reportArea.setText("");
        statusLabel.setText("Scanning " + dir + "...");

        lib.getFSReport(dir, DEFAULT_MAX_FS, DEFAULT_NB, EXCLUDED,
                        // onUpdate: chiamata dall'event-loop ad ogni file trovato.
                        // SwingUtilities.invokeLater sposta l'aggiornamento sull'EDT di Swing.
                        partialReport -> SwingUtilities.invokeLater(() -> {
                            reportArea.setText(partialReport.toString());
                            statusLabel.setText("Scanning... files found: " + partialReport.getTotalFiles());
                        })
                )
                .onSuccess(ignored -> SwingUtilities.invokeLater(() -> {
                    FSReportExt last = lib.getLiveReport(); // restituisce lo snapshot
                    reportArea.setText(last.toString());
                    String msg = lib.isStopped()
                            ? "Scan stopped. Partial result: " + last.getTotalFiles() + " files."
                            : "Scan complete. Total files: "   + last.getTotalFiles();
                    statusLabel.setText(msg);
                    startButton.setEnabled(true);
                    stopButton.setEnabled(false);
                }))
                .onFailure(err -> SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Error: " + err.getMessage());
                    startButton.setEnabled(true);
                    stopButton.setEnabled(false);
                }));
    }

    private void stopScan() {
        lib.stop(); // scrive stopped=true sull'AtomicBoolean, visibile all'event-loop
        statusLabel.setText("Stopping...");
        stopButton.setEnabled(false);
    }

    public static void main(String[] args) {
        // Tutta la creazione della GUI avviene sull'EDT di Swing
        SwingUtilities.invokeLater(() -> new GUI().setVisible(true));
    }
}