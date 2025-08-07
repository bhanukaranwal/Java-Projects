import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class FractalTreeGenerator extends JPanel {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int START_X = WIDTH / 2;
    private static final int START_Y = HEIGHT - 50;
    private static final double DEFAULT_ANGLE = Math.toRadians(25);
    private static final int DEFAULT_DEPTH = 10;
    private static final int DEFAULT_LENGTH = 120;

    private double branchAngle = DEFAULT_ANGLE;
    private int depth = DEFAULT_DEPTH;

    public FractalTreeGenerator() {
        JFrame frame = new JFrame("Fractal Tree Generator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WIDTH, HEIGHT);
        frame.setLocationRelativeTo(null);

        // Create sliders to adjust angle and depth
        JSlider angleSlider = new JSlider(0, 90, (int)Math.toDegrees(branchAngle));
        angleSlider.setMajorTickSpacing(15);
        angleSlider.setPaintTicks(true);
        angleSlider.setPaintLabels(true);
        angleSlider.setBorder(BorderFactory.createTitledBorder("Branch Angle (degrees)"));

        JSlider depthSlider = new JSlider(1, 15, depth);
        depthSlider.setMajorTickSpacing(1);
        depthSlider.setPaintTicks(true);
        depthSlider.setPaintLabels(true);
        depthSlider.setBorder(BorderFactory.createTitledBorder("Recursion Depth"));

        angleSlider.addChangeListener(e -> {
            branchAngle = Math.toRadians(angleSlider.getValue());
            repaint();
        });

        depthSlider.addChangeListener(e -> {
            depth = depthSlider.getValue();
            repaint();
        });

        JPanel controls = new JPanel();
        controls.setLayout(new GridLayout(2, 1));
        controls.add(angleSlider);
        controls.add(depthSlider);

        frame.add(this, BorderLayout.CENTER);
        frame.add(controls, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawFractalTree(g, START_X, START_Y, -Math.PI / 2, depth, DEFAULT_LENGTH);
    }

    private void drawFractalTree(Graphics g, int x1, int y1, double angle, int depth, int length) {
        if (depth == 0) return;

        int x2 = x1 + (int) (Math.cos(angle) * length);
        int y2 = y1 + (int) (Math.sin(angle) * length);

        g.drawLine(x1, y1, x2, y2);

        drawFractalTree(g, x2, y2, angle - branchAngle, depth - 1, (int)(length * 0.7));
        drawFractalTree(g, x2, y2, angle + branchAngle, depth - 1, (int)(length * 0.7));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FractalTreeGenerator::new);
    }
}
