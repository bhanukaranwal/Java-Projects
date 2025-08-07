import java.util.*;

public class MarkovChainTextGenerator {
    private static final Random random = new Random();

    // The order of the Markov chain (length of key sequences)
    private static final int ORDER = 2;

    // Markov model: maps from a state (list of words) to list of possible next words
    private final Map<List<String>, List<String>> markovChain = new HashMap<>();

    /**
     * Builds the Markov chain model from the input text.
     * @param words Array of words from the input text.
     */
    private void buildMarkovChain(String[] words) {
        for (int i = 0; i <= words.length - ORDER; i++) {
            List<String> key = Arrays.asList(Arrays.copyOfRange(words, i, i + ORDER));
            String nextWord = (i + ORDER < words.length) ? words[i + ORDER] : null;
            markovChain.computeIfAbsent(key, k -> new ArrayList<>());

            if (nextWord != null) {
                markovChain.get(key).add(nextWord);
            }
        }
    }

    /**
     * Generates text of a specified number of words based on the Markov chain.
     * @param length Number of words to generate.
     * @return Generated text as a string.
     */
    private String generateText(int length) {
        if (markovChain.isEmpty()) {
            return "";
        }

        // Pick a random starting state
        List<List<String>> keys = new ArrayList<>(markovChain.keySet());
        List<String> currentKey = keys.get(random.nextInt(keys.size()));

        List<String> output = new ArrayList<>(currentKey);

        for (int i = 0; i < length - ORDER; i++) {
            List<String> possibleNextWords = markovChain.get(currentKey);
            if (possibleNextWords == null || possibleNextWords.isEmpty()) {
                break;
            }
            String nextWord = possibleNextWords.get(random.nextInt(possibleNextWords.size()));
            output.add(nextWord);

            // Move forward by one word
            currentKey = new ArrayList<>(currentKey);
            currentKey.remove(0);
            currentKey.add(nextWord);
        }

        // Join words into a single string
        return String.join(" ", output);
    }

    public static void main(String[] args) {
        MarkovChainTextGenerator generator = new MarkovChainTextGenerator();
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Markov Chain Text Generator ===");
        System.out.println("Enter a few lines of input text to build the model.");
        System.out.println("End your input with an empty line:");

        List<String> inputLines = new ArrayList<>();
        while (true) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) break;
            inputLines.add(line);
        }

        String inputText = String.join(" ", inputLines);
        // Split by whitespace, simple tokenizer - can be improved
        String[] words = inputText.split("\\s+");

        if (words.length < ORDER) {
            System.out.println("Not enough input text provided to build a model.");
            return;
        }

        generator.buildMarkovChain(words);

        System.out.print("How many words would you like to generate? ");
        int length = 0;
        try {
            length = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number, defaulting to 50 words.");
            length = 50;
        }

        if (length < ORDER) length = ORDER;

        String generatedText = generator.generateText(length);
        System.out.println("\nGenerated Text:");
        System.out.println(generatedText);
    }
}
