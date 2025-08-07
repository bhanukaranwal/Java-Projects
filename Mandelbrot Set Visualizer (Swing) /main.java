import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class MandelbrotSetVisualizer extends JPanel {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;
    private static final int MAX_ITER = 200;
    private static final double DEFAULT_ZOOM = 0.009;
    private double zoom = DEFAULT_ZOOM;
    private double centerX = -0.6;
    private double centerY = 0.0;

    private BufferedImage image;
    private JFrame frame = new JFrame("Mandelbrot Set Visualizer");

    public MandelbrotSetVisualizer() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));

        // Controls
        JPanel controlPanel = new JPanel();
        JButton zoomIn = new JButton("Zoom In");
        JButton zoomOut = new JButton("Zoom Out");
        JButton recenter = new JButton("Recenter");
        JButton colorShift = new JButton("Color Shift");

        zoomIn.addActionListener(e -> { zoom *= 0.6; repaint(); });
        zoomOut.addActionListener(e -> { zoom /= 0.6; repaint(); });
        recenter.addActionListener(e -> { zoom = DEFAULT_ZOOM; centerX = -0.6; centerY = 0.0; repaint(); });
        colorShift.addActionListener(e -> { repaint(); });

        controlPanel.add(zoomIn);
        controlPanel.add(zoomOut);
        controlPanel.add(recenter);
        controlPanel.add(colorShift);

        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

        // Mouse movement for recentering
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Recenter on mouse click
                double dx = (e.getX() - WIDTH / 2.0) / (double)WIDTH;
                double dy = (e.getY() - HEIGHT / 2.0) / (double)HEIGHT;
                centerX += dx * zoom * 2;
                centerY += dy * zoom * 2;
                repaint();
            }
        });

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(this, BorderLayout.CENTER);
        frame.add(controlPanel, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Render the Mandelbrot set
        int width = getWidth();
        int height = getHeight();
        double wx = -2.0 / zoom;
        double wy = -2.0 / zoom;
        double ww = 4.0 / zoom;
        double wh = 4.0 / zoom;

        double x0 = centerX - 2.0 / zoom;
        double y0 = centerY - 2.0 / zoom;
        double x1 = centerX + 2.0 / zoom;
        double y1 = centerY + 2.0 / zoom;

        int[] colors = new int[MAX_ITER];
        for (int i = 0; i < MAX_ITER; i++) {
            float t = (float)i / (float)MAX_ITER;
            colors[i] = Color.HSBtoRGB(t * 10.0f % 1.0f, 0.8f, 1.0f);
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double zx = x0 + (x1 - x0) * x / width;
                double zy = y0 + (y1 - y0) * y / height;
                double cx = zx;
                double cy = zy;
                int iter = 0;

                double zx2 = zx * zx;
                double zy2 = zy * zy;
                double zzx, zzy;

                while (zx2 + zy2 < 4.0 && iter < MAX_ITER) {
                    zzx = zx2 - zy2 + cx;
                    zzy = 2 * zx * zy + cy;
                    zx = zzx;
                    zy = zzy;
                    zx2 = zx * zx;
                    zy2 = zy * zy;
                    iter++;
                }

                if (iter < MAX_ITER) {
                    image.setRGB(x, y, colors[iter]);
                } else {
                    image.setRGB(x, y, 0x000000);
                }
            }
        }

        g2d.drawImage(image, 0, 0, this);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MandelbrotSetVisualizer::new);
    }
}
