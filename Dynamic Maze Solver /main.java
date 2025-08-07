import java.util.*;

public class DynamicMazeSolver {
    private static final int WIDTH = 21;  // odd for proper maze
    private static final int HEIGHT = 21; // odd for proper maze

    private static final char WALL = '#';
    private static final char PATH = ' ';
    private static final char VISITED = '.';
    private static final char START = 'S';
    private static final char GOAL = 'G';
    private static final char ROUTE = '*';

    private static final int DELAY_MS = 50; // delay for animation

    private char[][] maze = new char[HEIGHT][WIDTH];
    private Node start, goal;

    private static class Node {
        int x, y;
        int g; // cost from start
        int h; // heuristic cost to goal
        int f; // g + h
        Node parent;

        Node(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Node)) return false;
            Node n = (Node) o;
            return x == n.x && y == n.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    public DynamicMazeSolver() {
        generateMaze();
        start = new Node(1, 1);
        goal = new Node(WIDTH - 2, HEIGHT - 2);
        maze[start.y][start.x] = START;
        maze[goal.y][goal.x] = GOAL;
    }

    // Maze generation using recursive backtracking
    private void generateMaze() {
        // Fill maze with walls
        for (int y = 0; y < HEIGHT; y++) {
            Arrays.fill(maze[y], WALL);
        }

        boolean[][] visited = new boolean[HEIGHT][WIDTH];
        carvePassage(1,1, visited);
    }

    private void carvePassage(int cx, int cy, boolean[][] visited) {
        visited[cy][cx] = true;
        maze[cy][cx] = PATH;

        int[] dx = {0, 0, -2, 2};
        int[] dy = {-2, 2, 0, 0};
        Integer[] dirs = {0, 1, 2, 3};
        Collections.shuffle(Arrays.asList(dirs));

        for (int dir : dirs) {
            int nx = cx + dx[dir];
            int ny = cy + dy[dir];

            if (ny > 0 && ny < HEIGHT-1 && nx > 0 && nx < WIDTH-1 && !visited[ny][nx]) {
                maze[cy + dy[dir]/2][cx + dx[dir]/2] = PATH; // carve wall between
                carvePassage(nx, ny, visited);
            }
        }
    }

    // A* Pathfinding Algorithm with animation
    public void solve() throws InterruptedException {
        PriorityQueue<Node> openList = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));
        Map<Node, Integer> costSoFar = new HashMap<>();
        Set<Node> closedSet = new HashSet<>();

        start.g = 0;
        start.h = heuristic(start, goal);
        start.f = start.g + start.h;
        openList.add(start);
        costSoFar.put(start, 0);

        while (!openList.isEmpty()) {
            Node current = openList.poll();

            if (current.equals(goal)) {
                reconstructPath(current);
                printMaze();
                System.out.println("Goal reached!");
                return;
            }

            closedSet.add(current);

            for (Node neighbor : neighbors(current)) {
                if (closedSet.contains(neighbor)) continue;

                int newCost = current.g + 1; // cost between nodes is 1

                boolean inOpen = costSoFar.containsKey(neighbor);

                if (!inOpen || newCost < costSoFar.get(neighbor)) {
                    neighbor.g = newCost;
                    neighbor.h = heuristic(neighbor, goal);
                    neighbor.f = neighbor.g + neighbor.h;
                    neighbor.parent = current;
                    costSoFar.put(neighbor, newCost);

                    if (!inOpen) {
                        openList.add(neighbor);
                    }
                }
            }

            // Mark current explored cell on maze (except start and goal)
            if (!current.equals(start) && !current.equals(goal)) {
                maze[current.y][current.x] = VISITED;
            }

            printMaze();
            Thread.sleep(DELAY_MS);
        }

        System.out.println("No path found.");
    }

    private List<Node> neighbors(Node node) {
        List<Node> result = new ArrayList<>();
        int[] dx = {0, 0, -1, 1};
        int[] dy = {-1, 1, 0, 0};

        for (int i = 0; i < 4; i++) {
            int nx = node.x + dx[i];
            int ny = node.y + dy[i];
            if (ny >= 0 && ny < HEIGHT && nx >= 0 && nx < WIDTH && maze[ny][nx] != WALL) {
                result.add(new Node(nx, ny));
            }
        }
        return result;
    }

    private int heuristic(Node a, Node b) {
        // Manhattan distance
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    private void reconstructPath(Node end) {
        Node current = end.parent;
        while (current != null && !current.equals(start)) {
            maze[current.y][current.x] = ROUTE;
            current = current.parent;
        }
    }

    private void printMaze() {
        clearConsole();
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                System.out.print(maze[y][x]);
            }
            System.out.println();
        }
    }

    private void clearConsole() {
        // ANSI escape codes to clear console and move cursor to top-left
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void main(String[] args) throws InterruptedException {
        DynamicMazeSolver solver = new DynamicMazeSolver();
        solver.printMaze();
        System.out.println("Press Enter to start solving...");
        new Scanner(System.in).nextLine();
        solver.solve();
        System.out.println("Press Enter to exit.");
        new Scanner(System.in).nextLine();
    }
}
