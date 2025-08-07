import java.util.*;

public class SimpleCompiler {
    // Token types
    private enum TokenType {NUMBER, PLUS, MINUS, MUL, DIV, LPAREN, RPAREN, EOF}

    // Token class
    private static class Token {
        TokenType type;
        String value;

        Token(TokenType t, String v) {
            type = t;
            value = v;
        }

        public String toString() {
            return String.format("Token(%s, %s)", type, value);
        }
    }

    // Lexer: converts input string into tokens
    private static class Lexer {
        private final String text;
        private int pos = 0;
        private char currentChar;

        Lexer(String text) {
            this.text = text;
            currentChar = pos < text.length() ? text.charAt(pos) : '\0';
        }

        private void advance() {
            pos++;
            currentChar = pos < text.length() ? text.charAt(pos) : '\0';
        }

        private void skipWhitespace() {
            while (currentChar != '\0' && Character.isWhitespace(currentChar)) {
                advance();
            }
        }

        private String integer() {
            StringBuilder sb = new StringBuilder();
            while (currentChar != '\0' && Character.isDigit(currentChar)) {
                sb.append(currentChar);
                advance();
            }
            return sb.toString();
        }

        Token getNextToken() {
            while (currentChar != '\0') {
                if (Character.isWhitespace(currentChar)) {
                    skipWhitespace();
                    continue;
                }

                if (Character.isDigit(currentChar)) {
                    return new Token(TokenType.NUMBER, integer());
                }

                switch (currentChar) {
                    case '+':
                        advance();
                        return new Token(TokenType.PLUS, "+");
                    case '-':
                        advance();
                        return new Token(TokenType.MINUS, "-");
                    case '*':
                        advance();
                        return new Token(TokenType.MUL, "*");
                    case '/':
                        advance();
                        return new Token(TokenType.DIV, "/");
                    case '(':
                        advance();
                        return new Token(TokenType.LPAREN, "(");
                    case ')':
                        advance();
                        return new Token(TokenType.RPAREN, ")");
                    default:
                        throw new RuntimeException("Unknown character: " + currentChar);
                }
            }
            return new Token(TokenType.EOF, "");
        }
    }

    // Parser and compiler: parses tokens into bytecode instructions
    private static class Parser {
        private final Lexer lexer;
        private Token currentToken;

        // Instruction set
        enum OpCode {PUSH, ADD, SUB, MUL, DIV}

        // Bytecode instructions (opcode + optional operand)
        static class Instruction {
            OpCode op;
            int operand;  // Only for PUSH. For others, operand ignored.

            Instruction(OpCode op) {
                this.op = op;
            }

            Instruction(OpCode op, int operand) {
                this.op = op;
                this.operand = operand;
            }

            public String toString() {
                if (op == OpCode.PUSH) return op + " " + operand;
                else return op.toString();
            }
        }

        private final List<Instruction> instructions = new ArrayList<>();

        Parser(Lexer lexer) {
            this.lexer = lexer;
            currentToken = lexer.getNextToken();
        }

        private void eat(TokenType type) {
            if (currentToken.type == type) {
                currentToken = lexer.getNextToken();
            } else {
                throw new RuntimeException("Unexpected token: " + currentToken + ", expected: " + type);
            }
        }

        // Grammar:
        // expr   : term ((PLUS|MINUS) term)*
        // term   : factor ((MUL|DIV) factor)*
        // factor : NUMBER | LPAREN expr RPAREN

        public List<Instruction> parse() {
            expr();
            if (currentToken.type != TokenType.EOF) {
                throw new RuntimeException("Unexpected token at end: " + currentToken);
            }
            return instructions;
        }

        private void expr() {
            term();
            while (currentToken.type == TokenType.PLUS || currentToken.type == TokenType.MINUS) {
                Token token = currentToken;
                if (token.type == TokenType.PLUS) {
                    eat(TokenType.PLUS);
                    term();
                    instructions.add(new Instruction(OpCode.ADD));
                } else {
                    eat(TokenType.MINUS);
                    term();
                    instructions.add(new Instruction(OpCode.SUB));
                }
            }
        }

        private void term() {
            factor();
            while (currentToken.type == TokenType.MUL || currentToken.type == TokenType.DIV) {
                Token token = currentToken;
                if (token.type == TokenType.MUL) {
                    eat(TokenType.MUL);
                    factor();
                    instructions.add(new Instruction(OpCode.MUL));
                } else {
                    eat(TokenType.DIV);
                    factor();
                    instructions.add(new Instruction(OpCode.DIV));
                }
            }
        }

        private void factor() {
            if (currentToken.type == TokenType.NUMBER) {
                int value = Integer.parseInt(currentToken.value);
                eat(TokenType.NUMBER);
                instructions.add(new Instruction(OpCode.PUSH, value));
            } else if (currentToken.type == TokenType.LPAREN) {
                eat(TokenType.LPAREN);
                expr();
                eat(TokenType.RPAREN);
            } else {
                throw new RuntimeException("Unexpected token in factor: " + currentToken);
            }
        }
    }

    // Interpreter: executes the compiled bytecode instructions
    private static class Interpreter {
        private final List<Parser.Instruction> instructions;
        private final Stack<Integer> stack = new Stack<>();

        Interpreter(List<Parser.Instruction> instructions) {
            this.instructions = instructions;
        }

        int execute() {
            for (Parser.Instruction inst : instructions) {
                switch (inst.op) {
                    case PUSH:
                        stack.push(inst.operand);
                        break;
                    case ADD: {
                        int b = stack.pop();
                        int a = stack.pop();
                        stack.push(a + b);
                        break;
                    }
                    case SUB: {
                        int b = stack.pop();
                        int a = stack.pop();
                        stack.push(a - b);
                        break;
                    }
                    case MUL: {
                        int b = stack.pop();
                        int a = stack.pop();
                        stack.push(a * b);
                        break;
                    }
                    case DIV: {
                        int b = stack.pop();
                        int a = stack.pop();
                        if (b == 0) throw new RuntimeException("Division by zero");
                        stack.push(a / b);
                        break;
                    }
                }
            }
            if (stack.size() != 1) {
                throw new RuntimeException("Stack error at end: " + stack);
            }
            return stack.pop();
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Simple Compiler - Arithmetic Expression Compiler and Interpreter");
        System.out.println("Enter an arithmetic expression (supports +, -, *, /, and parentheses).");
        System.out.println("Type 'exit' to quit.");

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            if (line.equalsIgnoreCase("exit")) break;
            if (line.isEmpty()) continue;
            try {
                Lexer lexer = new Lexer(line);
                Parser parser = new Parser(lexer);
                List<Parser.Instruction> bytecode = parser.parse();

                System.out.println("Bytecode:");
                for (Parser.Instruction inst : bytecode) {
                    System.out.println("  " + inst);
                }

                Interpreter interpreter = new Interpreter(bytecode);
                int result = interpreter.execute();
                System.out.println("Result: " + result);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        System.out.println("Goodbye!");
    }
}
