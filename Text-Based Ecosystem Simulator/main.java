import java.util.Random;
import java.util.Scanner;

public class TextBasedEcosystemSimulator {
    private static final int DAYS = 50; // Number of simulation steps
    private static final int INITIAL_PREY = 100;
    private static final int INITIAL_PREDATORS = 20;

    // Parameters controlling reproduction and death rates
    private static final double PREY_BIRTH_RATE = 0.1;
    private static final double PREDATOR_BIRTH_RATE = 0.05;
    private static final double PREDATOR_DEATH_RATE = 0.1;
    private static final double PREDATION_RATE = 0.01;  // Probability that a predator kills a prey per encounter

    private int preyPopulation;
    private int predatorPopulation;

    private Random random = new Random();

    public TextBasedEcosystemSimulator() {
        this.preyPopulation = INITIAL_PREY;
        this.predatorPopulation = INITIAL_PREDATORS;
    }

    public void simulate() {
        System.out.println("Starting ecosystem simulation...");
        System.out.printf("Day\tPrey\tPredators%n");
        for (int day = 0; day <= DAYS; day++) {
            printStatus(day);
            if (day < DAYS) {
                simulateDay();
                if (preyPopulation <= 0) {
                    System.out.println("Prey population has gone extinct.");
                    break;
                }
                if (predatorPopulation <= 0) {
                    System.out.println("Predator population has gone extinct.");
                    break;
                }
            }
        }
    }

    private void simulateDay() {
        // Prey reproduction
        int preyBirths = binomial(preyPopulation, PREY_BIRTH_RATE);

        // Predators kill prey
        int predationEvents = 0;
        int predatorHungry = predatorPopulation;
        int preyAvailable = preyPopulation;

        for (int i = 0; i < predatorPopulation; i++) {
            if (preyAvailable <= 0) break;
            if (random.nextDouble() < PREDATION_RATE) {
                predationEvents++;
                preyAvailable--;
            }
        }

        // Predator reproduction based on food consumed
        int predatorBirths = (int)(predationEvents * PREDATOR_BIRTH_RATE);

        // Predator deaths
        int predatorDeaths = binomial(predatorPopulation, PREDATOR_DEATH_RATE);

        // Update populations
        preyPopulation = Math.max(preyAvailable + preyBirths, 0);
        predatorPopulation = Math.max(predatorPopulation + predatorBirths - predatorDeaths, 0);
    }

    private void printStatus(int day) {
        System.out.printf("%3d\t%4d\t%9d%n", day, preyPopulation, predatorPopulation);
    }

    // Helper method to sample from binomial distribution
    private int binomial(int n, double p) {
        int count = 0;
        for (int i = 0; i < n; i++) {
            if (random.nextDouble() < p) count++;
        }
        return count;
    }

    public static void main(String[] args) {
        TextBasedEcosystemSimulator simulator = new TextBasedEcosystemSimulator();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Text-Based Ecosystem Simulator");
        System.out.print("Press Enter to start simulation...");
        scanner.nextLine();

        simulator.simulate();

        System.out.println("Simulation ended.");
        scanner.close();
    }
}
