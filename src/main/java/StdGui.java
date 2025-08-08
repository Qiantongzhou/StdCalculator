//version 3.10
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;


public final class StdGui extends JFrame {

    // --- UI fields ------------------------------------------------------------
    private final JTextArea inputArea   = new JTextArea(6, 32);
    private final JLabel    statusBar   = new JLabel("Ready.");
    private final JButton   calcBtn     = new JButton("Calculate σ");
    private final JButton   clearBtn    = new JButton("Clear");
    private final JButton   copyBtn     = new JButton("Copy Results");
    private final JLabel    sigmaLabel  = new JLabel("—", SwingConstants.LEFT);
    private final JLabel    meanLabel   = new JLabel("—");
    private final JLabel    varLabel    = new JLabel("—");
    private final JTextArea stepsArea   = new JTextArea(12, 40);

    // Colors chosen for contrast (meets WCAG AA with default LAF)
    private static final Color ACCENT = new Color(21, 101, 192);
    private static final Color OK     = new Color(46, 125, 50);
    private static final Color ERR    = new Color(198, 40, 40);

    public StdGui() {
        super("σ  Standard Deviation Calculator — D3 F8");

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(880, 560));
        setLocationByPlatform(true);

        setContentPane(buildRoot());
        wireActions();
        applyAccessibility();

        // Keyboard shortcuts (discoverable)
        getRootPane().setDefaultButton(calcBtn); // Enter triggers Calculate in focused context
        registerAccelerators();

        pack();
    }

    // --- UI building ----------------------------------------------------------
    private JPanel buildRoot() {
        final JPanel root = new JPanel(new BorderLayout(0, 0));
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildBody(),   BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);
        return root;
    }

    private JComponent buildHeader() {
        final JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, ACCENT.darker(), 0, getHeight(), ACCENT));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel title = new JLabel("Standard Deviation Calculator (σ)");
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        JLabel subtitle = new JLabel("Enter integers separated by spaces, commas, or newlines.");
        subtitle.setForeground(new Color(255, 255, 255, 215));
        subtitle.setFont(subtitle.getFont().deriveFont(13f));

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(title);
        texts.add(Box.createVerticalStrut(4));
        texts.add(subtitle);

        header.add(texts, BorderLayout.WEST);
        return header;
    }

    private JComponent buildBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setBorder(new EmptyBorder(16, 16, 16, 16));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.fill = GridBagConstraints.BOTH;

        // Left: INPUT
        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.setBorder(titled("Input"));

        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        inputArea.setToolTipText("Example: 1, 2, 3 4 5");
        JScrollPane inputScroll = new JScrollPane(inputArea);
        inputScroll.setBorder(new CompoundBorder(new LineBorder(new Color(0,0,0,60),1,true),
                new EmptyBorder(8,8,8,8)));
        left.add(inputScroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        calcBtn.setMnemonic(KeyEvent.VK_C); // Alt+C
        actions.add(calcBtn);
        actions.add(clearBtn);
        left.add(actions, BorderLayout.SOUTH);

        // Right Top: RESULTS
        JPanel rightTop = new JPanel(new GridBagLayout());
        rightTop.setBorder(titled("Results"));

        GridBagConstraints r = new GridBagConstraints();
        r.insets = new Insets(6, 6, 6, 6);
        r.fill = GridBagConstraints.HORIZONTAL;
        r.gridx = 0; r.gridy = 0; r.weightx = 0;
        JLabel sigmaText = new JLabel("σ");
        sigmaText.setFont(sigmaText.getFont().deriveFont(Font.BOLD, 18f));
        rightTop.add(sigmaText, r);

        r.gridx = 1; r.weightx = 1;
        sigmaLabel.setFont(sigmaLabel.getFont().deriveFont(Font.BOLD, 18f));
        rightTop.add(sigmaLabel, r);

        r.gridy = 1; r.gridx = 0; r.weightx = 0;
        rightTop.add(new JLabel("Mean (μ):"), r);
        r.gridx = 1; r.weightx = 1; rightTop.add(meanLabel, r);

        r.gridy = 2; r.gridx = 0; r.weightx = 0;
        rightTop.add(new JLabel("Variance (σ²):"), r);
        r.gridx = 1; r.weightx = 1; rightTop.add(varLabel, r);

        r.gridy = 3; r.gridx = 0; r.gridwidth = 2; r.weightx = 1;
        rightTop.add(copyBtn, r);

        // Right Bottom: STEPS
        JPanel rightBottom = new JPanel(new BorderLayout(8, 8));
        rightBottom.setBorder(titled("Steps (calculation details)"));
        stepsArea.setEditable(false);
        stepsArea.setLineWrap(true);
        stepsArea.setWrapStyleWord(true);
        stepsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane stepsScroll = new JScrollPane(stepsArea);
        stepsScroll.setBorder(new CompoundBorder(new LineBorder(new Color(0,0,0,40),1,true),
                new EmptyBorder(8,8,8,8)));
        rightBottom.add(stepsScroll, BorderLayout.CENTER);

        // Place panels
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0.46; gc.weighty = 1;
        body.add(left, gc);

        JPanel right = new JPanel(new GridBagLayout());
        GridBagConstraints rgc = new GridBagConstraints();
        rgc.insets = new Insets(0, 0, 8, 0);
        rgc.fill = GridBagConstraints.BOTH;
        rgc.gridx = 0; rgc.gridy = 0; rgc.weightx = 1; rgc.weighty = 0.35;
        right.add(rightTop, rgc);
        rgc.gridy = 1; rgc.weighty = 0.65;
        right.add(rightBottom, rgc);

        gc.gridx = 1; gc.gridy = 0; gc.weightx = 0.54; gc.weighty = 1;
        body.add(right, gc);

        return body;
    }

    private JComponent buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, new Color(0,0,0,40)),
                new EmptyBorder(8, 12, 8, 12)));
        statusBar.setForeground(new Color(20,20,20));
        bar.add(statusBar, BorderLayout.WEST);
        return bar;
    }

    private TitledBorder titled(String title) {
        TitledBorder tb = BorderFactory.createTitledBorder(title);
        tb.setTitleFont(tb.getTitleFont().deriveFont(Font.BOLD, 13f));
        return tb;
    }

    // --- Behavior -------------------------------------------------------------
    private void wireActions() {
        calcBtn.addActionListener(this::onCalculate);
        clearBtn.addActionListener(e -> {
            inputArea.setText("");
            setStatus("Cleared.", OK);
            showResults(null);
            inputArea.requestFocusInWindow();
        });
        copyBtn.addActionListener(e -> copyResults());
        inputArea.getDocument().addDocumentListener(simpleChange(() -> {
            // Live enable/disable calculate, gentle validation cue
            String txt = inputArea.getText().trim();
            calcBtn.setEnabled(!txt.isEmpty());
            if (!txt.isEmpty() && !looksValid(txt)) {
                setStatus("Hint: Only integers, separated by spaces/commas/newlines.", new Color(180,120,0));
            } else if (txt.isEmpty()) {
                setStatus("Ready.", new Color(20,20,20));
            } else {
                setStatus("Press Alt+C or Ctrl+Enter to calculate.", new Color(20,20,20));
            }
        }));
        calcBtn.setEnabled(false);
    }

    private void registerAccelerators() {
        InputMap im = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getRootPane().getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK), "calc");
        am.put("calc", new AbstractAction() { public void actionPerformed(ActionEvent e) { calcBtn.doClick(); }});
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK), "clear");
        am.put("clear", new AbstractAction() { public void actionPerformed(ActionEvent e) { clearBtn.doClick(); }});
    }

    private void applyAccessibility() {
        inputArea.getAccessibleContext().setAccessibleName("Numbers input");
        inputArea.getAccessibleContext().setAccessibleDescription("Enter integers separated by spaces, commas, or newlines.");
        calcBtn.getAccessibleContext().setAccessibleDescription("Computes the standard deviation.");
        clearBtn.getAccessibleContext().setAccessibleDescription("Clears the input.");
        copyBtn.getAccessibleContext().setAccessibleDescription("Copies the numeric results and steps to the clipboard.");
        sigmaLabel.getAccessibleContext().setAccessibleName("Standard deviation result");
        meanLabel.getAccessibleContext().setAccessibleName("Mean result");
        varLabel.getAccessibleContext().setAccessibleName("Variance result");
        stepsArea.getAccessibleContext().setAccessibleName("Calculation steps");
        stepsArea.getAccessibleContext().setAccessibleDescription("Detailed steps of the calculation.");
    }

    private void onCalculate(ActionEvent e) {
        String raw = inputArea.getText().trim();
        if (raw.isEmpty()) {
            showError("Please enter at least one integer.");
            return;
        }

        List<Long> values = new ArrayList<>();
        int idx = 0;
        for (String tok : raw.split("[,\\s]+")) {
            if (tok.isEmpty()) continue;
            if (!tok.matches("-?\\d+")) {
                showError("Invalid token at position " + (idx + 1) + ": \"" + tok + "\". Use only integers.");
                return;
            }
            try {
                values.add(Long.parseLong(tok));
            } catch (NumberFormatException ex) {
                showError("Number too large: \"" + tok + "\".");
                return;
            }
            idx++;
        }
        if (values.isEmpty()) {
            showError("No valid integers found.");
            return;
        }

        double[] xs = new double[values.size()];
        for (int i = 0; i < xs.length; i++) xs[i] = values.get(i);

        double mean = Stats.mean(xs);
        double var  = Stats.variancePopulation(xs, mean);
        double std  = Stats.sqrt(var);

        showResults(new double[]{std, mean, var});
        buildSteps(xs, mean, var, std);
        setStatus("Calculated σ for n = " + xs.length + ".", OK);
    }

    private void showResults(double[] tripleOrNull) {
        if (tripleOrNull == null) {
            sigmaLabel.setText("—");
            meanLabel.setText("—");
            varLabel.setText("—");
            stepsArea.setText("");
            return;
        }
        sigmaLabel.setText(format(tripleOrNull[0]));
        meanLabel.setText(format(tripleOrNull[1]));
        varLabel.setText(format(tripleOrNull[2]));
    }

    private void buildSteps(double[] xs, double mean, double var, double std) {
        StringBuilder sb = new StringBuilder();
        sb.append("Given n = ").append(xs.length).append("\n");
        sb.append("Values: ");
        for (int i = 0; i < xs.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append((long) xs[i]);
        }
        sb.append("\n\nμ = (1/n) · Σ x_i = ").append(format(mean)).append("\n");
        sb.append("σ² = (1/n) · Σ (x_i − μ)² = ").append(format(var)).append("\n");
        sb.append("σ = √(σ²) = ").append(format(std)).append("\n");
        stepsArea.setText(sb.toString());
        stepsArea.setCaretPosition(0);
    }

    private void copyResults() {
        String text = "σ = " + sigmaLabel.getText()
                + "\nμ = " + meanLabel.getText()
                + "\nσ² = " + varLabel.getText()
                + "\n\n" + stepsArea.getText();
        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new StringSelection(text), null);
        setStatus("Copied results to clipboard.", OK);
    }

    private void showError(String msg) {
        setStatus(msg, ERR);
        UIManager.getLookAndFeel().provideErrorFeedback(this);
    }

    private void setStatus(String msg, Color color) {
        statusBar.setText(msg);
        statusBar.setForeground(color);
    }

    private boolean looksValid(String txt) {
        // Lightweight hinting before strict parse
        return txt.matches("\\s*-?\\d+(?:[\\s,]+-?\\d+)*\\s*");
    }

    private String format(double v) {
        // Fixed 6 dp for readability without locale surprises
        return String.format(java.util.Locale.US, "%.6f", v);
    }

    private static javax.swing.event.DocumentListener simpleChange(Runnable r) {
        return new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
        };
    }

    // --- From-scratch friendly stats utilities --------------------------------
    private static final class Stats {
        static double mean(double[] xs) {
            double s = 0.0;
            for (double x : xs) s += x;
            return s / xs.length;
        }
        static double variancePopulation(double[] xs, double mean) {
            double s = 0.0;
            for (double x : xs) {
                double d = x - mean;
                s += d * d;
            }
            return s / xs.length; // population variance
        }
        // Newton–Raphson sqrt without using Math.sqrt
        static double sqrt(double v) {
            if (v < 0) throw new IllegalArgumentException("sqrt of negative");
            if (v == 0) return 0;
            double x = v >= 1 ? v : 1.0;  // initial guess
            for (int i = 0; i < 40; i++) {
                x = 0.5 * (x + v / x);
            }
            return x;
        }
    }

    // --- Bootstrapping ---------------------------------------------------------
    public static void main(String[] args) {
        setLookAndFeel();
        SwingUtilities.invokeLater(() -> new StdGui().setVisible(true));
    }

    private static void setLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignore) { }
    }
}
