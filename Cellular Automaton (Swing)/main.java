import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CellularAutomaton extends JPanel implements ActionListener {
    private static final int CELL_SIZE = 10; // Size of each cell in pixels
    private static final int DEFAULT_ROWS = 60;
    private static final int DEFAULT_COLS = 80;
    private static final int TIMER_DELAY = 100; // milliseconds between generations

    private int rows;
    private int cols;
    private boolean[][] cells;
    private boolean[][] nextGen;
    private Timer timer;

    public CellularAutomaton(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.cells = new boolean[rows][cols];
        this.nextGen = new boolean[rows][cols];

        setPreferredSize(new Dimension(cols * CELL_SIZE, rows * CELL_SIZE));
        setBackground(Color.WHITE);

        // Random initial state
        randomizeCells();

        // Timer for simulation steps
        timer = new Timer(TIMER_DELAY, this);

        // Mouse listener to toggle cell state on click
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int col = e.getX() / CELL_SIZE;
                int row = e.getY() / CELL_SIZE;
                if (row >= 0 && row < rows && col >= 0 && col < cols) {
                    cells[row][col] = !cells[row][col];
                    repaint();
                }
            }
        });
    }

    // Set random initial configuration
    private void randomizeCells() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                cells[r][c] = Math.random() < 0.2;
            }
        }
        repaint();
    }

    // Compute next generation
    private void nextGeneration() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int neighbors = countNeighbors(r, c);
                if (cells[r][c]) {
                    // Any live cell with two or three live neighbors survives.
                    nextGen[r][c] = (neighbors == 2 || neighbors == 3);
                } else {
                    // Any dead cell with three live neighbors becomes a live cell.
                    nextGen[r][c] = (neighbors == 3);
                }
            }
        }

        // Swap buffers
        boolean[][] temp = cells;
        cells = nextGen;
        nextGen = temp;
        repaint();
    }

    // Count live neighbors of cell at (r, c)
    private int countNeighbors(int r, int c) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int rr = r + dr;
                int cc = c + dc;
                if (rr >= 0 && rr < rows && cc >= 0 && cc < cols && cells[rr][cc]) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.GRAY);

        // Draw grid
        for (int r = 0; r <= rows; r++) {
            g.drawLine(0, r * CELL_SIZE, cols * CELL_SIZE, r * CELL_SIZE);
        }
        for (int c = 0; c <= cols; c++) {
            g.drawLine(c * CELL_SIZE, 0, c * CELL_SIZE, rows * CELL_SIZE);
        }

        // Draw live cells
        g.setColor(Color.BLACK);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c]) {
                    g.fillRect(c * CELL_SIZE + 1, r * CELL_SIZE + 1, CELL_SIZE - 1, CELL_SIZE - 1);
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        nextGeneration();
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Cellular Automaton - Conway's Game of Life");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel controlPanel = new JPanel();

        JButton startBtn = new JButton("Start");
        JButton stopBtn = new JButton("Stop");
        JButton randomizeBtn = new JButton("Randomize");
        JLabel speedLabel = new JLabel("Speed(ms):");
        JTextField speedField = new JTextField(Integer.toString(TIMER_DELAY), 4);

        startBtn.addActionListener(e -> timer.start());
        stopBtn.addActionListener(e -> timer.stop());
        randomizeBtn.addActionListener(e -> {
            timer.stop();
            randomizeCells();
        });
        speedField.addActionListener(e -> {
            try {
                int delay = Integer.parseInt(speedField.getText());
                if (delay > 0) {
                    timer.setDelay(delay);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid speed value");
            }
        });

        controlPanel.add(startBtn);
        controlPanel.add(stopBtn);
        controlPanel.add(randomizeBtn);
        controlPanel.add(speedLabel);
        controlPanel.add(speedField);

        frame.add(this, BorderLayout.CENTER);
        frame.add(controlPanel, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CellularAutomaton automaton = new CellularAutomaton(DEFAULT_ROWS, DEFAULT_COLS);
            automaton.createAndShowGUI();
        });
    }
}
