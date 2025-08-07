import java.util.*;
import java.util.regex.*;

public class CodeObfuscator {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Random random = new Random();

    // Pattern to match Java variable names (simple version)
    // Matches variable names outside strings/comments roughly
    private static final Pattern VAR_PATTERN = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b");

    // Java keywords (won't rename these)
    private static final Set<String> JAVA_KEYWORDS = new HashSet<>(Arrays.asList(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
            "const", "continue", "default", "do", "double", "else", "enum", "extends", "final",
            "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int",
            "interface", "long", "native", "new", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
            "throw", "throws", "transient", "try", "void", "volatile", "while", "true", "false", "null"
    ));

    // Map of original variable names to obfuscated names
    private Map<String, String> obfuscatedNames = new HashMap<>();

    // Generate a random variable name
    private String generateRandomName() {
        int length = 6 + random.nextInt(5);
        StringBuilder sb = new StringBuilder();
        sb.append((char) ('a' + random.nextInt(26)));
        for (int i = 1; i < length; i++) {
            char c;
            int choice = random.nextInt(3);
            if (choice == 0) c = (char) ('a' + random.nextInt(26));
            else if (choice == 1) c = (char) ('A' + random.nextInt(26));
            else c = (char) ('0' + random.nextInt(10));
            sb.append(c);
        }
        return sb.toString();
    }

    // Obfuscate the code: rename variables and insert random spaces/comments
    private String obfuscateCode(String code) {
        // Step 1: Find all variable/identifier candidates
        Matcher matcher = VAR_PATTERN.matcher(code);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String varName = matcher.group(1);

            // Skip keywords and if already obfuscated
            if (JAVA_KEYWORDS.contains(varName)) {
                matcher.appendReplacement(sb, varName);
                continue;
            }

            String obfName = obfuscatedNames.get(varName);
            if (obfName == null) {
                obfName = generateRandomName();
                obfuscatedNames.put(varName, obfName);
            }

            matcher.appendReplacement(sb, obfName);
        }
        matcher.appendTail(sb);

        String obfuscated = sb.toString();

        // Step 2: Add random spaces and fake comments to obfuscate formatting
        obfuscated = insertNoise(obfuscated);

        return obfuscated;
    }

    // Inserts random spaces and fake comments in the code to confuse readers
    private String insertNoise(String code) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < code.length(); i++) {
            sb.append(code.charAt(i));

            // Randomly insert spaces
            if (random.nextDouble() < 0.05) {
                sb.append(" ");
            }

            // Randomly insert fake comment blocks
            if (random.nextDouble() < 0.01) {
                sb.append("/* ");
                sb.append(randomCommentText());
                sb.append(" */");
            }
        }

        return sb.toString();
    }

    // Generates random comment text (simple random words)
    private String randomCommentText() {
        String[] words = {"foo", "bar", "baz", "lorem", "ipsum", "dolor", "sit", "amet", "hack", "123"};
        int count = 2 + random.nextInt(3);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(words[random.nextInt(words.length)]);
            if (i < count - 1) sb.append(" ");
        }
        return sb.toString();
    }

    public void run() {
        System.out.println("=== Simple Java Code Obfuscator ===");
        System.out.println("Paste your complete Java code below. End input with a line containing only 'END':");

        StringBuilder inputBuilder = new StringBuilder();
        while (true) {
            String line = scanner.nextLine();
            if (line.equals("END")) break;
            inputBuilder.append(line).append("\n");
        }

        String inputCode = inputBuilder.toString();

        // Obfuscate
        String obfuscatedCode = obfuscateCode(inputCode);

        System.out.println("\n=== Obfuscated Code ===");
        System.out.println(obfuscatedCode);
    }

    public static void main(String[] args) {
        new CodeObfuscator().run();
    }
}
