import java.util.*;

public class MiniInterpreter {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Map<String, Integer> variables = new HashMap<>();

    public static void main(String[] args) {
        System.out.println("Mini Interpreter");
        System.out.println("Type 'exit' to quit.");
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Goodbye!");
                break;
            }
            try {
                if (input.contains("=")) {
                    handleAssignment(input);
                } else {
                    int result = evaluateExpression(input);
                    System.out.println(result);
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void handleAssignment(String line) {
        String[] parts = line.split("=", 2);
        if (parts.length != 2) throw new RuntimeException("Invalid assignment");
        String varName = parts[0].trim();
        if (!varName.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            throw new RuntimeException("Invalid variable name");
        }
        int value = evaluateExpression(parts[1].trim());
        variables.put(varName, value);
        System.out.println(varName + " = " + value);
    }

    private static int evaluateExpression(String expr) {
        // Use a simple recursive descent parser for arithmetic expressions with +, -, *, /
        return new Parser(expr).parseExpression();
    }

    private static class Parser {
        private final String input;
        private int pos = -1, ch;

        Parser(String input) {
            this.input = input;
            nextChar();
        }

        void nextChar() {
            ch = (++pos < input.length()) ? input.charAt(pos) : -1;
        }

        boolean eat(int charToEat) {
            while (ch == ' ') nextChar();
            if (ch == charToEat) {
                nextChar();
                return true;
            }
            return false;
        }

        int parseExpression() {
            int x = parseTerm();
            for (;;) {
                if (eat('+')) x += parseTerm();
                else if (eat('-')) x -= parseTerm();
                else return x;
            }
        }

        int parseTerm() {
            int x = parseFactor();
            for (;;) {
                if (eat('*')) x *= parseFactor();
                else if (eat('/')) x /= parseFactor();
                else return x;
            }
        }

        int parseFactor() {
            if (eat('+')) return parseFactor(); // unary plus
            if (eat('-')) return -parseFactor(); // unary minus

            int x;
            int startPos = this.pos;
            if (eat('(')) { // parentheses
                x = parseExpression();
                if (!eat(')')) throw new RuntimeException("Missing ')'");
            } else if ((ch >= '0' && ch <= '9')) { // numbers
                while (ch >= '0' && ch <= '9') nextChar();
                x = Integer.parseInt(input.substring(startPos, this.pos));
            } else if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch == '_') { // variables
                while (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9' || ch == '_') nextChar();
                String varName = input.substring(startPos, this.pos);
                if (variables.containsKey(varName)) {
                    x = variables.get(varName);
                } else {
                    throw new RuntimeException("Unknown variable: " + varName);
                }
            } else {
                throw new RuntimeException("Unexpected: " + (char) ch);
            }

            return x;
        }
    }
}
