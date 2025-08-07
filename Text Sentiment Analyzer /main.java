import java.util.*;

public class TextSentimentAnalyzer {
    // Simple sentiment lexicon (word -> score)
    private static final Map<String, Integer> SENTIMENT_WORDS = new HashMap<>();

    static {
        // Positive words
        SENTIMENT_WORDS.put("happy", 2);
        SENTIMENT_WORDS.put("joy", 3);
        SENTIMENT_WORDS.put("love", 3);
        SENTIMENT_WORDS.put("excellent", 4);
        SENTIMENT_WORDS.put("good", 2);
        SENTIMENT_WORDS.put("great", 3);
        SENTIMENT_WORDS.put("fantastic", 4);
        SENTIMENT_WORDS.put("positive", 2);
        SENTIMENT_WORDS.put("wonderful", 4);
        SENTIMENT_WORDS.put("like", 1);

        // Negative words
        SENTIMENT_WORDS.put("sad", -2);
        SENTIMENT_WORDS.put("bad", -2);
        SENTIMENT_WORDS.put("terrible", -4);
        SENTIMENT_WORDS.put("hate", -3);
        SENTIMENT_WORDS.put("awful", -4);
        SENTIMENT_WORDS.put("horrible", -4);
        SENTIMENT_WORDS.put("negative", -2);
        SENTIMENT_WORDS.put("angry", -3);
        SENTIMENT_WORDS.put("dislike", -2);
        SENTIMENT_WORDS.put("pain", -3);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Text Sentiment Analyzer ===");
        System.out.println("Enter some text (type 'exit' to quit):");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) {
                break;
            }

            int score = analyzeSentiment(input);
            String sentiment = interpretScore(score);

            System.out.printf("Sentiment score: %d (%s)%n", score, sentiment);
        }

        System.out.println("Goodbye!");
        scanner.close();
    }

    private static int analyzeSentiment(String text) {
        String[] words = text.toLowerCase().split("\\W+");
        int totalScore = 0;
        int matchedWords = 0;

        for (String word : words) {
            if (SENTIMENT_WORDS.containsKey(word)) {
                totalScore += SENTIMENT_WORDS.get(word);
                matchedWords++;
            }
        }

        // Normalize score by number of matched words to avoid bias towards longer input
        if (matchedWords > 0) {
            return totalScore / matchedWords;
        } else {
            return 0;
        }
    }

    private static String interpretScore(int score) {
        if (score > 1) {
            return "Positive";
        } else if (score < -1) {
            return "Negative";
        } else if (score == 0) {
            return "Neutral";
        } else {
            return "Slightly " + (score > 0 ? "Positive" : "Negative");
        }
    }
}
