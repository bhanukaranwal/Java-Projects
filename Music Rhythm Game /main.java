import java.util.*;
import java.util.concurrent.*;

public class MusicRhythmGame {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Random random = new Random();

    // Define the keys player must press (e.g., A, S, D, F)
    private static final char[] KEYS = {'a', 's', 'd', 'f'};

    // Beat pattern length
    private static final int PATTERN_LENGTH = 20;

    // Interval between beats (in milliseconds)
    private static final int BEAT_INTERVAL = 1000;

    // Allowed timing window (ms) for correct hit
    private static final int HIT_WINDOW = 300;

    private char[] beatPattern = new char[PATTERN_LENGTH];

    // To track when current beat started
    private volatile long beatStartTime = 0;

    // To store user input time and key
    private volatile Character lastKeyPressed = null;
    private volatile long lastKeyPressTime = 0;

    private int score = 0;

    public static void main(String[] args) {
        new MusicRhythmGame().start();
    }

    public void start() {
        System.out.println("=== Music Rhythm Game ===");
        System.out.println("Press the correct key when the beat appears!");
        System.out.println("Keys: a, s, d, f");
        System.out.println("Press Enter to start...");
        scanner.nextLine();

        generateBeatPattern();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

        Runnable beatTask = new Runnable() {
            int beatIndex = 0;

            @Override
            public void run() {
                if (beatIndex >= PATTERN_LENGTH) {
                    System.out.println("\nGame Over! Final score: " + score);
                    scheduler.shutdownNow();
                    return;
                }
                char currentBeatKey = beatPattern[beatIndex];
                System.out.println("\nBeat " + (beatIndex + 1) + ": Press '" + currentBeatKey + "'");
                beatStartTime = System.currentTimeMillis();
                lastKeyPressed = null;
                lastKeyPressTime = 0;

                // Wait a little less than beat interval to check input and update score
                scheduler.schedule(() -> checkHit(currentBeatKey), HIT_WINDOW, TimeUnit.MILLISECONDS);
                beatIndex++;
            }
        };

        Runnable inputTask = () -> {
            while (!scheduler.isShutdown()) {
                String input = scanner.nextLine().trim().toLowerCase();
                if (!input.isEmpty()) {
                    char key = input.charAt(0);
                    long now = System.currentTimeMillis();
                    lastKeyPressed = key;
                    lastKeyPressTime = now;
                }
            }
        };

        scheduler.scheduleAtFixedRate(beatTask, 0, BEAT_INTERVAL, TimeUnit.MILLISECONDS);
        // Run input task on a separate thread to read asynchronously
        new Thread(inputTask).start();
    }

    private void generateBeatPattern() {
        for (int i = 0; i < PATTERN_LENGTH; i++) {
            beatPattern[i] = KEYS[random.nextInt(KEYS.length)];
        }
    }

    private void checkHit(char expectedKey) {
        if (lastKeyPressed == null) {
            System.out.println("Miss! No key pressed.");
        } else {
            long delta = Math.abs(lastKeyPressTime - beatStartTime);
            if (lastKeyPressed == expectedKey && delta <= HIT_WINDOW) {
                score++;
                System.out.println("Hit! Score: " + score);
            } else {
                System.out.println("Miss! Wrong key or timing.");
            }
        }
    }
}
