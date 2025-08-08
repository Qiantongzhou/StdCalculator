import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class StdGUITest {

    private StdGui frame;

    @BeforeEach
    void setUp() throws Exception {
        // Skip these tests on headless CI (e.g., GitHub Actions without xvfb)
        assumeFalse(GraphicsEnvironment.isHeadless(), "Headless environment: skipping Swing UI tests.");

        SwingUtilities.invokeAndWait(() -> {
            frame = new StdGui();
            // keep it invisible to avoid popping a window during tests
            frame.setVisible(false);
        });
    }

    @AfterEach
    void tearDown() throws Exception {
        if (frame != null) {
            SwingUtilities.invokeAndWait(() -> frame.dispose());
        }
    }


    private static <T> T get(Object target, String fieldName, Class<T> type) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return type.cast(f.get(target));
        } catch (Exception e) {
            throw new AssertionError("Failed to access field: " + fieldName, e);
        }
    }

    private static void flushEDT() throws Exception {
        SwingUtilities.invokeAndWait(() -> {});
    }

    @Test
    void calculate_validInput_updatesResults() throws Exception {
        JTextArea inputArea = get(frame, "inputArea", JTextArea.class);
        JButton calcBtn      = get(frame, "calcBtn", JButton.class);
        JLabel sigmaLabel    = get(frame, "sigmaLabel", JLabel.class);
        JLabel meanLabel     = get(frame, "meanLabel", JLabel.class);
        JLabel varLabel      = get(frame, "varLabel", JLabel.class);
        JLabel statusBar     = get(frame, "statusBar", JLabel.class);

        SwingUtilities.invokeAndWait(() -> {
            inputArea.setText("1 2 3 4 5");
            calcBtn.setEnabled(true);
            calcBtn.doClick();
        });
        flushEDT();

        // Expect: mean=3, variance(pop)=2, stddev=sqrt(2) ~ 1.414213562 => formatted to 6 dp
        assertEquals("1.414214", sigmaLabel.getText());
        assertEquals("3.000000",  meanLabel.getText());
        assertEquals("2.000000",  varLabel.getText());
        assertTrue(statusBar.getText().startsWith("Calculated σ for n = 5"));
    }

    @Test
    void calculate_invalidToken_showsErrorAndNoResults() throws Exception {
        JTextArea inputArea = get(frame, "inputArea", JTextArea.class);
        JButton calcBtn      = get(frame, "calcBtn", JButton.class);
        JLabel sigmaLabel    = get(frame, "sigmaLabel", JLabel.class);
        JLabel statusBar     = get(frame, "statusBar", JLabel.class);

        SwingUtilities.invokeAndWait(() -> {
            inputArea.setText("1, 2, x, 4");
            calcBtn.setEnabled(true);
            calcBtn.doClick();
        });
        flushEDT();

        assertTrue(statusBar.getText().toLowerCase().contains("invalid token"));
        assertEquals("—", sigmaLabel.getText(), "σ label should stay unset when input is invalid.");
    }

    @Test
    void clearButton_resetsFields() throws Exception {
        JTextArea inputArea = get(frame, "inputArea", JTextArea.class);
        JButton clearBtn     = get(frame, "clearBtn", JButton.class);
        JLabel sigmaLabel    = get(frame, "sigmaLabel", JLabel.class);
        JLabel meanLabel     = get(frame, "meanLabel", JLabel.class);
        JLabel varLabel      = get(frame, "varLabel", JLabel.class);

        SwingUtilities.invokeAndWait(() -> {
            inputArea.setText("10 10 10");
            clearBtn.doClick();
        });
        flushEDT();

        assertEquals("", inputArea.getText());
        assertEquals("—", sigmaLabel.getText());
        assertEquals("—", meanLabel.getText());
        assertEquals("—", varLabel.getText());
    }
}
