import java.util.Scanner;

public class SimpleRaycaster {
    private static final int MAP_WIDTH = 16;
    private static final int MAP_HEIGHT = 16;

    private static final int SCREEN_WIDTH = 80;
    private static final int SCREEN_HEIGHT = 24;

    private static final double FOV = Math.PI / 4.0;  // 45 degrees
    private static final double DEPTH = 16.0; // max view distance

    private static final char[] SHADES = {' ', '.', '-', '+', '*', 'X', '#', '%', '@'};

    private static final String[] MAP = {
        "################",
        "#..............#",
        "#.......########",
        "#..............#",
        "#......##......#",
        "#......##......#",
        "#..............#",
        "###............#",
        "##.............#",
        "#..............#",
        "#......#########",
        "#..............#",
        "#..............#",
        "#......#########",
        "#..............#",
        "################"
    };

    private double playerX = 8.0;
    private double playerY = 8.0;
    private double playerAngle = 0.0;

    private Scanner scanner = new Scanner(System.in);

    public void run() {
        while (true) {
            renderFrame();

            System.out.println("Use keys: A/D - rotate, W/S - move, Q - quit");
            System.out.print("Move> ");
            String input = scanner.nextLine().toLowerCase();
            if (input.isEmpty()) continue;

            char command = input.charAt(0);
            if (command == 'q') {
                System.out.println("Exiting.");
                break;
            }
            handleInput(command);
        }
    }

    private void handleInput(char command) {
        final double moveSpeed = 0.5;
        final double rotSpeed = 0.1;

        switch (command) {
            case 'a':
                playerAngle -= rotSpeed;
                break;
            case 'd':
                playerAngle += rotSpeed;
                break;
            case 'w':
                double newX = playerX + Math.sin(playerAngle) * moveSpeed;
                double newY = playerY + Math.cos(playerAngle) * moveSpeed;
                if (isEmpty(newX, newY)) {
                    playerX = newX;
                    playerY = newY;
                }
                break;
            case 's':
                newX = playerX - Math.sin(playerAngle) * moveSpeed;
                newY = playerY - Math.cos(playerAngle) * moveSpeed;
                if (isEmpty(newX, newY)) {
                    playerX = newX;
                    playerY = newY;
                }
                break;
            default:
                System.out.println("Unknown command.");
        }
    }

    private boolean isEmpty(double x, double y) {
        if (x < 0 || x >= MAP_WIDTH || y < 0 || y >= MAP_HEIGHT) return false;
        return MAP[(int)y].charAt((int)x) == '.';
    }

    private void renderFrame() {
        StringBuilder screen = new StringBuilder();

        for (int y = 0; y < SCREEN_HEIGHT; y++) {
            for (int x = 0; x < SCREEN_WIDTH; x++) {
                if (y == SCREEN_HEIGHT / 2) {
                    // Render horizon line
                    screen.append('-');
                } else {
                    screen.append(' ');
                }
            }
            screen.append('\n');
        }

        // Render vertical slice per column (raycasting)
        char[] screenChars = screen.toString().toCharArray();

        for (int x = 0; x < SCREEN_WIDTH; x++) {
            // For each column, calculate the projected ray angle into world space
            double rayAngle = (playerAngle - FOV / 2.0) + ((double)x / (double)SCREEN_WIDTH) * FOV;

            double distanceToWall = 0.0;
            boolean hitWall = false;

            boolean boundary = false;

            double eyeX = Math.sin(rayAngle);
            double eyeY = Math.cos(rayAngle);

            while (!hitWall && distanceToWall < DEPTH) {
                distanceToWall += 0.1;

                int testX = (int)(playerX + eyeX * distanceToWall);
                int testY = (int)(playerY + eyeY * distanceToWall);

                // Test if ray is out of bounds
                if (testX < 0 || testX >= MAP_WIDTH || testY < 0 || testY >= MAP_HEIGHT) {
                    hitWall = true;
                    distanceToWall = DEPTH;
                } else {
                    if (MAP[testY].charAt(testX) == '#') {
                        hitWall = true;

                        // To highlight boundaries, check for closeness of multiple walls
                        java.util.List<java.awt.geom.Point2D.Double> p = new java.util.ArrayList<>();

                        for (int tx = 0; tx < 2; tx++) {
                            for (int ty = 0; ty < 2; ty++) {
                                double vx = testX + tx - playerX;
                                double vy = testY + ty - playerY;
                                double d = Math.sqrt(vx * vx + vy * vy);
                                double dot = (eyeX * vx / d) + (eyeY * vy / d);
                                p.add(new java.awt.geom.Point2D.Double(d, dot));
                            }
                        }

                        p.sort((a, b) -> {
                            if (a.x < b.x) return -1;
                            else if (a.x > b.x) return 1;
                            else return 0;
                        });

                        double bound = 0.01;
                        if (Math.acos(p.get(0).y) < bound) boundary = true;
                        if (Math.acos(p.get(1).y) < bound) boundary = true;
                    }
                }
            }

            int ceiling = (int)((SCREEN_HEIGHT / 2.0) - SCREEN_HEIGHT / distanceToWall);
            int floor = SCREEN_HEIGHT - ceiling;

            // Shade walls based on distance
            char wallShade;
            if (distanceToWall <= DEPTH / 4.0) wallShade = SHADES[8];
            else if (distanceToWall < DEPTH / 3.0) wallShade = SHADES[6];
            else if (distanceToWall < DEPTH / 2.0) wallShade = SHADES[4];
            else if (distanceToWall < DEPTH) wallShade = SHADES[2];
            else wallShade = SHADES[0];

            if (boundary) wallShade = ' '; // make boundaries blank for effect

            for (int y = 0; y < SCREEN_HEIGHT; y++) {
                int idx = y * (SCREEN_WIDTH + 1) + x;  // +1 for the newline char at line end

                if (y < ceiling) {
                    screenChars[idx] = ' '; // sky
                } else if (y > ceiling && y <= floor) {
                    screenChars[idx] = wallShade;
                } else {
                    // Floor shading
                    double b = 1.0 - ((double)(y - SCREEN_HEIGHT / 2) / (SCREEN_HEIGHT / 2));
                    char floorShade;
                    if (b < 0.25) floorShade = '#';
                    else if (b < 0.5) floorShade = 'x';
                    else if (b < 0.75) floorShade = '.';
                    else if (b < 0.9) floorShade = '-';
                    else floorShade = ' ';
                    screenChars[idx] = floorShade;
                }
            }
        }

        // Clear console (platform dependent)
        clearConsole();

        // Print screen buffer
        System.out.println(new String(screenChars));
        System.out.printf("X=%.2f Y=%.2f Angle=%.2f radians%n", playerX, playerY, playerAngle);
    }

    private void clearConsole() {
        // ANSI escape codes to clear screen and move cursor home
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void main(String[] args) {
        SimpleRaycaster raycaster = new SimpleRaycaster();
        raycaster.run();
    }
}
