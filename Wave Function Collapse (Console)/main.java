import java.util.*;

public class WaveFunctionCollapse {
    private static final int WIDTH = 20;
    private static final int HEIGHT = 20;

    // Define tile types and their constraints (which tiles can be neighbors on each side)
    private enum Tile {
        FLOOR('.'), WALL('#'), WATER('~');

        final char symbol;
        // For each side, allowed neighboring tiles
        Map<Direction, Set<Tile>> constraints = new EnumMap<>(Direction.class);

        Tile(char symbol) {
            this.symbol = symbol;
        }

        static {
            // Setup constraints for each tile's neighbors
            FLOOR.constraints.put(Direction.UP, Set.of(FLOOR, WALL));
            FLOOR.constraints.put(Direction.DOWN, Set.of(FLOOR, WALL));
            FLOOR.constraints.put(Direction.LEFT, Set.of(FLOOR, WALL));
            FLOOR.constraints.put(Direction.RIGHT, Set.of(FLOOR, WALL));

            WALL.constraints.put(Direction.UP, Set.of(FLOOR, WALL));
            WALL.constraints.put(Direction.DOWN, Set.of(FLOOR, WALL));
            WALL.constraints.put(Direction.LEFT, Set.of(WALL, WATER));
            WALL.constraints.put(Direction.RIGHT, Set.of(WALL, WATER));

            WATER.constraints.put(Direction.UP, Set.of(WATER, WALL));
            WATER.constraints.put(Direction.DOWN, Set.of(WATER, WALL));
            WATER.constraints.put(Direction.LEFT, Set.of(WATER, WALL));
            WATER.constraints.put(Direction.RIGHT, Set.of(WATER, WALL));
        }
    }

    private enum Direction {
        UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0);
        final int dx, dy;
        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }
    }

    // Each cell can have multiple possible tiles (superposition)
    private static class Cell {
        Set<Tile> possible = EnumSet.allOf(Tile.class);
        boolean collapsed = false;

        Tile collapse() {
            if (collapsed) return possible.iterator().next();
            // Randomly pick one tile from the possible
            Tile choice = new ArrayList<>(possible).get(new Random().nextInt(possible.size()));
            possible.clear();
            possible.add(choice);
            collapsed = true;
            return choice;
        }

        @Override
        public String toString() {
            if (collapsed) {
                return String.valueOf(possible.iterator().next().symbol);
            }
            else if (possible.size() == 0) {
                return "?"; // Contradiction
            } else {
                return " "; // unresolved
            }
        }
    }

    private Cell[][] grid = new Cell[HEIGHT][WIDTH];
    private Random random = new Random();

    public WaveFunctionCollapse() {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                grid[y][x] = new Cell();
            }
        }
    }

    public void run() {
        // Initialize by collapsing a random cell to start
        int startX = random.nextInt(WIDTH);
        int startY = random.nextInt(HEIGHT);
        grid[startY][startX].collapse();

        while (true) {
            Optional<Pos> cellToCollapse = findCellWithMinimumEntropy();
            if (cellToCollapse.isEmpty()) {
                // All collapsed or contradiction - finish
                break;
            }
            Pos pos = cellToCollapse.get();
            if (!grid[pos.y][pos.x].collapsed) {
                grid[pos.y][pos.x].collapse();
                propagateConstraints(pos.x, pos.y);
                printGrid();
                sleep(200);
            }
        }

        System.out.println("\nFinal pattern:");
        printGrid();
    }

    // Find cell with lowest entropy (fewest possibilities, but > 1), selecting randomly among those
    private Optional<Pos> findCellWithMinimumEntropy() {
        int minOptions = Integer.MAX_VALUE;
        List<Pos> candidates = new ArrayList<>();
        for (int y=0; y<HEIGHT; y++) {
            for (int x=0; x<WIDTH; x++) {
                Cell cell = grid[y][x];
                if (!cell.collapsed) {
                    int options = cell.possible.size();
                    if (options == 0) {
                        // Contradiction found, skip
                        continue;
                    }
                    if (options < minOptions) {
                        minOptions = options;
                        candidates.clear();
                        candidates.add(new Pos(x, y));
                    } else if (options == minOptions) {
                        candidates.add(new Pos(x, y));
                    }
                }
            }
        }
        if (candidates.isEmpty()) return Optional.empty();
        return Optional.of(candidates.get(random.nextInt(candidates.size())));
    }

    // Propagation: update possibilities of neighbor cells based on constraints
    private void propagateConstraints(int startX, int startY) {
        Queue<Pos> queue = new LinkedList<>();
        queue.add(new Pos(startX, startY));

        while (!queue.isEmpty()) {
            Pos current = queue.poll();
            Cell currentCell = grid[current.y][current.x];
            if (!currentCell.collapsed) continue;
            Tile currentTile = currentCell.possible.iterator().next();

            for (Direction dir : Direction.values()) {
                int nx = current.x + dir.dx;
                int ny = current.y + dir.dy;

                if (nx >= 0 && nx < WIDTH && ny >= 0 && ny < HEIGHT) {
                    Cell neighborCell = grid[ny][nx];
                    if (neighborCell.collapsed) continue;

                    int before = neighborCell.possible.size();
                    neighborCell.possible.removeIf(tile -> !currentTile.constraints.get(dir).contains(tile));
                    int after = neighborCell.possible.size();

                    if (after == 0) {
                        // Contradiction found: no possible tiles - reset neighbor (could improve)
                        // For simplicity, we won't fix here; just continue
                    }

                    if (after < before) {
                        queue.add(new Pos(nx, ny));
                    }
                }
            }
        }
    }

    private void printGrid() {
        clearConsole();
        for (int y=0; y<HEIGHT; y++) {
            for (int x=0; x<WIDTH; x++) {
                System.out.print(grid[y][x]);
            }
            System.out.println();
        }
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }

    private void clearConsole() {
        // ANSI escape code to clear console and home cursor
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    // Helper class for positions
    private static class Pos {
        int x, y;
        Pos(int x, int y) { this.x = x; this.y = y; }
    }

    public static void main(String[] args) {
        System.out.println("Wave Function Collapse - Tile Pattern Generation");
        System.out.println("Generating pattern...\nPress Ctrl+C to exit anytime.\n");
        WaveFunctionCollapse wfc = new WaveFunctionCollapse();
        wfc.run();
    }
}
