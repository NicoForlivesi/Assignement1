package fsstat;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

/**
 * GUI minimale con:
 * - Campo per inserire la directory
 * - Pulsante Start per avviare la scansione
 * - Pulsante Stop per interromperla
 * - TextArea che si aggiorna dinamicamente con il report parziale
 *
 * Attenzione ai thread:
 * - La callback onUpdate arriva dal thread dell'event-loop di Vert.x
 * - Gli aggiornamenti alla GUI DEVONO passare per SwingUtilities.invokeLater()
 *   altrimenti violiamo il modello single-thread di Swing
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
        stopButton = new JButton("Stop");
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

        // Listener sui pulsanti
        startButton.addActionListener(e -> startScan());
        stopButton.addActionListener(e -> stopScan()); // Bindo il pulsante all'atomicBoolean "stopped"
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

        lib.getFSReport(dir, DEFAULT_MAX_FS, DEFAULT_NB, EXCLUDED, partialReport -> {
                    // Questa callback arriva dall'event-loop di Vert.x, NON dal thread Swing.
                    // SwingUtilities.invokeLater garantisce che l'aggiornamento alla GUI
                    // avvenga sul thread EDT (Event Dispatch Thread) di Swing.
                    SwingUtilities.invokeLater(() -> {
                        reportArea.setText(partialReport.toString());
                        statusLabel.setText("Scanning... files found so far: " + partialReport.getTotalFiles());
                    });
                })
                .onSuccess(finalReport -> SwingUtilities.invokeLater(() -> {
                    reportArea.setText(finalReport.toString());
                    String msg = lib.isStopped()
                            ? "Scan stopped. Partial result: " + finalReport.getTotalFiles() + " files."
                            : "Scan complete. Total files: " + finalReport.getTotalFiles();
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
        lib.stop();
        statusLabel.setText("Stopping...");
        stopButton.setEnabled(false);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GUI().setVisible(true));
    }
}