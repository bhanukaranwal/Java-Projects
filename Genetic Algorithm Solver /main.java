import java.util.*;

public class GeneticAlgorithmSolver {
    // Problem definition: items with weight and value
    static class Item {
        int weight;
        int value;
        Item(int w, int v) { weight = w; value = v; }
    }

    private static final Random random = new Random();

    // Define the items for the knapsack problem
    private static final Item[] ITEMS = {
        new Item(10, 60),
        new Item(20, 100),
        new Item(30, 120),
        new Item(5, 30),
        new Item(15, 70),
        new Item(25, 90),
        new Item(35, 150),
        new Item(45, 200),
        new Item(40, 160),
        new Item(50, 240)
    };

    private static final int KNAPSACK_CAPACITY = 100;

    // GA parameters
    private static final int POPULATION_SIZE = 100;
    private static final int TOURNAMENT_SIZE = 5;
    private static final double MUTATION_RATE = 0.05;
    private static final int MAX_GENERATIONS = 200;

    static class Individual {
        boolean[] chromosome;
        int fitness;

        Individual(int length) {
            chromosome = new boolean[length];
        }

        // Randomly initialize chromosome
        void randomize() {
            for (int i = 0; i < chromosome.length; i++) {
                chromosome[i] = random.nextBoolean();
            }
        }

        // Calculate fitness (total value, 0 if overweight)
        void calculateFitness() {
            int totalWeight = 0;
            int totalValue = 0;
            for (int i = 0; i < chromosome.length; i++) {
                if (chromosome[i]) {
                    totalWeight += ITEMS[i].weight;
                    totalValue += ITEMS[i].value;
                }
            }
            fitness = (totalWeight <= KNAPSACK_CAPACITY) ? totalValue : 0;
        }
    }

    // Genetic Algorithm core
    public static void main(String[] args) {
        List<Individual> population = new ArrayList<>();

        // Initialize population
        for (int i = 0; i < POPULATION_SIZE; i++) {
            Individual ind = new Individual(ITEMS.length);
            ind.randomize();
            ind.calculateFitness();
            population.add(ind);
        }

        Individual best = null;

        for (int generation = 0; generation < MAX_GENERATIONS; generation++) {
            List<Individual> newPopulation = new ArrayList<>();

            // Elitism: keep the best individual
            population.sort(Comparator.comparingInt(i -> -i.fitness));
            if (best == null || population.get(0).fitness > best.fitness) {
                best = population.get(0);
            }
            newPopulation.add(best);

            // Generate new individuals
            while (newPopulation.size() < POPULATION_SIZE) {
                Individual parent1 = tournamentSelection(population);
                Individual parent2 = tournamentSelection(population);
                Individual offspring = crossover(parent1, parent2);
                mutate(offspring);
                offspring.calculateFitness();
                newPopulation.add(offspring);
            }

            population = newPopulation;

            if (generation % 20 == 0 || generation == MAX_GENERATIONS - 1) {
                System.out.printf("Generation %d - Best fitness: %d%n", generation, best.fitness);
            }
        }

        // Display best solution found
        System.out.println("\nBest solution found:");
        System.out.printf("Value: %d\n", best.fitness);
        System.out.print("Items chosen (weight, value): ");
        int totalWeight = 0;
        for (int i = 0; i < best.chromosome.length; i++) {
            if (best.chromosome[i]) {
                System.out.printf("(%d, %d) ", ITEMS[i].weight, ITEMS[i].value);
                totalWeight += ITEMS[i].weight;
            }
        }
        System.out.printf("\nTotal Weight: %d\n", totalWeight);
    }

    private static Individual tournamentSelection(List<Individual> population) {
        List<Individual> tournament = new ArrayList<>();
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            tournament.add(population.get(random.nextInt(population.size())));
        }
        // Select individual with highest fitness
        return Collections.max(tournament, Comparator.comparingInt(i -> i.fitness));
    }

    private static Individual crossover(Individual parent1, Individual parent2) {
        Individual offspring = new Individual(parent1.chromosome.length);
        int crossoverPoint = random.nextInt(parent1.chromosome.length);
        for (int i = 0; i < parent1.chromosome.length; i++) {
            offspring.chromosome[i] = (i < crossoverPoint) ? parent1.chromosome[i] : parent2.chromosome[i];
        }
        return offspring;
    }

    private static void mutate(Individual individual) {
        for (int i = 0; i < individual.chromosome.length; i++) {
            if (random.nextDouble() < MUTATION_RATE) {
                individual.chromosome[i] = !individual.chromosome[i];
            }
        }
    }
}
