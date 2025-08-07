import java.util.*;

public class TextBasedDungeonCrawler {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Random random = new Random();

    // Dungeon size
    private static final int WIDTH = 5;
    private static final int HEIGHT = 5;

    private static final char EMPTY = '.';
    private static final char PLAYER = '@';
    private static final char ENEMY = 'E';
    private static final char TREASURE = 'T';

    private static class Position {
        int x, y;
        Position(int x, int y) { this.x = x; this.y = y; }
        boolean equals(Position other) { return x == other.x && y == other.y; }
    }

    private static class Enemy {
        Position pos;
        int hp;
        Enemy(Position pos, int hp) {
            this.pos = pos;
            this.hp = hp;
        }
    }

    private char[][] dungeon = new char[HEIGHT][WIDTH];
    private Position playerPos;
    private int playerHp = 20;
    private int playerAttack = 5;
    private int playerGold = 0;

    private List<Enemy> enemies = new ArrayList<>();
    private Position treasurePos;

    public TextBasedDungeonCrawler() {
        generateDungeon();
    }

    private void generateDungeon() {
        // Initialize dungeon with empty tiles
        for (int i = 0; i < HEIGHT; i++) {
            Arrays.fill(dungeon[i], EMPTY);
        }

        // Place player at random location
        playerPos = randomEmptyPosition();
        dungeon[playerPos.y][playerPos.x] = PLAYER;

        // Place treasure at random location
        treasurePos = randomEmptyPosition();
        dungeon[treasurePos.y][treasurePos.x] = TREASURE;

        // Place enemies
        int enemyCount = 5;
        for (int i = 0; i < enemyCount; i++) {
            Position ePos = randomEmptyPosition();
            enemies.add(new Enemy(ePos, 10));
            dungeon[ePos.y][ePos.x] = ENEMY;
        }
    }

    private Position randomEmptyPosition() {
        int x, y;
        do {
            x = random.nextInt(WIDTH);
            y = random.nextInt(HEIGHT);
        } while (dungeon[y][x] != EMPTY);
        return new Position(x, y);
    }

    public void play() {
        System.out.println("Welcome to the Text-Based Dungeon Crawler!");
        System.out.println("Commands: w (up), s (down), a (left), d (right), q (quit)");
        System.out.println("Find the treasure (T) and avoid or defeat enemies (E).");
        System.out.println("You are represented by '@'.");
        System.out.println();

        while (true) {
            printDungeon();
            System.out.printf("HP: %d | Attack: %d | Gold: %d%n", playerHp, playerAttack, playerGold);
            System.out.print("Your move: ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.isEmpty()) continue;
            char move = input.charAt(0);
            if (move == 'q') {
                System.out.println("Quitting game. Goodbye!");
                break;
            }
            Position newPos = new Position(playerPos.x, playerPos.y);
            switch (move) {
                case 'w': newPos.y -= 1; break;
                case 's': newPos.y += 1; break;
                case 'a': newPos.x -= 1; break;
                case 'd': newPos.x += 1; break;
                default:
                    System.out.println("Invalid command!");
                    continue;
            }
            if (!isInsideDungeon(newPos)) {
                System.out.println("You hit a wall!");
                continue;
            }

            char tile = dungeon[newPos.y][newPos.x];
            if (tile == ENEMY) {
                Enemy enemy = enemyAt(newPos);
                if (enemy != null) {
                    battle(enemy);
                    if (playerHp <= 0) {
                        System.out.println("You died! Game Over.");
                        break;
                    }
                    if (enemy.hp <= 0) {
                        System.out.println("Enemy defeated!");
                        enemies.remove(enemy);
                        dungeon[newPos.y][newPos.x] = EMPTY;
                    }
                }
            } else if (tile == TREASURE) {
                System.out.println("Congratulations! You found the treasure and won!");
                break;
            }

            // Move player
            dungeon[playerPos.y][playerPos.x] = EMPTY;
            playerPos = newPos;
            dungeon[playerPos.y][playerPos.x] = PLAYER;

            // Enemies move randomly
            enemyTurn();

            if (playerHp <= 0) {
                System.out.println("You died! Game Over.");
                break;
            }
        }
    }

    private boolean isInsideDungeon(Position pos) {
        return pos.x >= 0 && pos.x < WIDTH && pos.y >= 0 && pos.y < HEIGHT;
    }

    private Enemy enemyAt(Position pos) {
        for (Enemy enemy : enemies) {
            if (enemy.pos.equals(pos)) return enemy;
        }
        return null;
    }

    private void battle(Enemy enemy) {
        System.out.println("You encountered an enemy!");
        while (enemy.hp > 0 && playerHp > 0) {
            System.out.printf("Enemy HP: %d | Your HP: %d%n", enemy.hp, playerHp);
            System.out.print("Attack (a) or Run (r)? ");
            String action = scanner.nextLine().trim().toLowerCase();
            if (action.isEmpty()) continue;
            char act = action.charAt(0);
            if (act == 'a') {
                int playerDamage = playerAttack + random.nextInt(3);
                enemy.hp -= playerDamage;
                System.out.printf("You hit the enemy for %d damage.%n", playerDamage);
                if (enemy.hp <= 0) break;

                int enemyDamage = 3 + random.nextInt(3);
                playerHp -= enemyDamage;
                System.out.printf("Enemy hits you for %d damage.%n", enemyDamage);
            } else if (act == 'r') {
                System.out.println("You ran away!");
                break;
            } else {
                System.out.println("Invalid action!");
            }
        }
    }

    private void enemyTurn() {
        for (Enemy enemy : new ArrayList<>(enemies)) {
            Position oldPos = enemy.pos;
            List<Position> possibleMoves = new ArrayList<>();
            for (int dx = -1; dx <= +1; dx++) {
                for (int dy = -1; dy <= +1; dy++) {
                    if (Math.abs(dx) + Math.abs(dy) != 1) continue; // only cardinal moves
                    Position np = new Position(oldPos.x + dx, oldPos.y + dy);
                    if (isInsideDungeon(np) && dungeon[np.y][np.x] == EMPTY && !np.equals(playerPos)) {
                        possibleMoves.add(np);
                    }
                }
            }
            if (!possibleMoves.isEmpty()) {
                Position newPos = possibleMoves.get(random.nextInt(possibleMoves.size()));
                dungeon[oldPos.y][oldPos.x] = EMPTY;
                enemy.pos = newPos;
                dungeon[newPos.y][newPos.x] = ENEMY;
            }
            // If enemy moves onto player position, battle occurs next player's turn
        }
    }

    private void printDungeon() {
        System.out.println();
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                System.out.print(dungeon[y][x] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    public static void main(String[] args) {
        TextBasedDungeonCrawler game = new TextBasedDungeonCrawler();
        game.play();
    }
}
