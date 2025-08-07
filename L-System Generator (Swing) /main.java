import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.util.HashMap;
import java.util.Map;

public class LSystemGenerator extends JPanel {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private String axiom = "F";
    private Map<Character, String> rules = new HashMap<>();
    private String currentString;

    private double angle = Math.toRadians(25);
    private int iterations = 5;
    private double length = 5.0;

    public LSystemGenerator() {
        // Setup L-System rules for a simple fractal plant
        rules.put('F', "FF+[+F-F-F]-[-F+F+F]");

        // Generate the string after specified iterations
        currentString = generateString(axiom, iterations);

        setPreferredSize(new Dimension(WIDTH, HEIGHT));
    }

    private String generateString(String input, int iterations) {
        String output = input;
        for (int i = 0; i < iterations; i++) {
            StringBuilder newString = new StringBuilder();
            for (char c : output.toCharArray()) {
                String replacement = rules.getOrDefault(c, String.valueOf(c));
                newString.append(replacement);
            }
            output = newString.toString();
        }
        return output;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Enable better rendering
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.GREEN.darker());
        g2d.setStroke(new BasicStroke(1));

        // Start drawing from bottom-center
        double startX = WIDTH / 2.0;
        double startY = HEIGHT - 50;

        // Use a stack for position and angle saving
        TurtleState state = new TurtleState(startX, startY, -Math.PI / 2); // Pointing up

        java.util.Stack<TurtleState> stack = new java.util.Stack<>();

        for (char c : currentString.toCharArray()) {
            switch (c) {
                case 'F': {
                    // Move forward and draw a line
                    double newX = state.x + length * Math.cos(state.angle);
                    double newY = state.y + length * Math.sin(state.angle);
                    g2d.draw(new Line2D.Double(state.x, state.y, newX, newY));
                    state.x = newX;
                    state.y = newY;
                } break;
                case '+': {
                    // Turn right by angle
                    state.angle += angle;
                } break;
                case '-': {
                    // Turn left by angle
                    state.angle -= angle;
                } break;
                case '[': {
                    // Push current state
                    stack.push(new TurtleState(state.x, state.y, state.angle));
                } break;
                case ']': {
                    // Pop state
                    if (!stack.isEmpty()) {
                        state = stack.pop();
                    }
                } break;
                // Ignore other characters
            }
        }

        g2d.dispose();
    }

    private static class TurtleState {
        double x, y;
        double angle;

        TurtleState(double x, double y, double angle) {
            this.x = x;
            this.y = y;
            this.angle = angle;
        }
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("L-System Generator (Fractal Plant)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Controls panel for user to adjust iterations and angle
        JPanel controls = new JPanel();

        JLabel iterationLabel = new JLabel("Iterations:");
        JTextField iterationField = new JTextField(Integer.toString(iterations), 3);

        JLabel angleLabel = new JLabel("Angle (degrees):");
        JTextField angleField = new JTextField(Double.toString(Math.toDegrees(angle)), 3);

        JButton generateButton = new JButton("Generate");

        generateButton.addActionListener(e -> {
            try {
                int iter = Integer.parseInt(iterationField.getText());
                double ang = Math.toRadians(Double.parseDouble(angleField.getText()));

                if (iter < 0 || iter > 10) {
                    JOptionPane.showMessageDialog(frame, "Iterations must be between 0 and 10.");
                    return;
                }

                iterations = iter;
                angle = ang;

                currentString = generateString(axiom, iterations);
                repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid input for iterations or angle.");
            }
        });

        controls.add(iterationLabel);
        controls.add(iterationField);
        controls.add(angleLabel);
        controls.add(angleField);
        controls.add(generateButton);

        frame.setLayout(new java.awt.BorderLayout());
        frame.add(this, java.awt.BorderLayout.CENTER);
        frame.add(controls, java.awt.BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LSystemGenerator generator = new LSystemGenerator();
            generator.createAndShowGUI();
        });
    }
}
