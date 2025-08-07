import java.util.Random;
import java.util.Scanner;

public class NeuralNetworkSimulator {
    private static final Random random = new Random();

    // Network parameters
    private static final int INPUT_NEURONS = 2;
    private static final int HIDDEN_NEURONS = 2;
    private static final int OUTPUT_NEURONS = 1;
    private static final double LEARNING_RATE = 0.5;

    // Weights
    private double[][] weightsInputHidden = new double[INPUT_NEURONS][HIDDEN_NEURONS];
    private double[] weightsHiddenOutput = new double[HIDDEN_NEURONS];

    // Biases
    private double[] biasHidden = new double[HIDDEN_NEURONS];
    private double biasOutput;

    public NeuralNetworkSimulator() {
        initializeWeights();
    }

    private void initializeWeights() {
        for (int i = 0; i < INPUT_NEURONS; i++) {
            for (int j = 0; j < HIDDEN_NEURONS; j++) {
                weightsInputHidden[i][j] = random.nextDouble() * 2 - 1; // -1 to 1
            }
        }
        for (int j = 0; j < HIDDEN_NEURONS; j++) {
            weightsHiddenOutput[j] = random.nextDouble() * 2 - 1;
            biasHidden[j] = random.nextDouble() * 2 - 1;
        }
        biasOutput = random.nextDouble() * 2 - 1;
    }

    // Activation function (sigmoid) and its derivative
    private double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }

    private double sigmoidDerivative(double x) {
        return x * (1 - x);
    }

    // Forward propagation
    private double[] feedForward(double[] inputs) {
        double[] hiddenLayerOutputs = new double[HIDDEN_NEURONS];

        for (int j = 0; j < HIDDEN_NEURONS; j++) {
            double activation = biasHidden[j];
            for (int i = 0; i < INPUT_NEURONS; i++) {
                activation += inputs[i] * weightsInputHidden[i][j];
            }
            hiddenLayerOutputs[j] = sigmoid(activation);
        }

        double outputActivation = biasOutput;
        for (int j = 0; j < HIDDEN_NEURONS; j++) {
            outputActivation += hiddenLayerOutputs[j] * weightsHiddenOutput[j];
        }
        double output = sigmoid(outputActivation);

        return new double[] {output, hiddenLayerOutputs[0], hiddenLayerOutputs[1]};
    }

    // Training with backpropagation
    private void train(double[] inputs, double targetOutput) {
        double[] forwardResult = feedForward(inputs);
        double output = forwardResult[0];
        double[] hiddenOutputs = {forwardResult[1], forwardResult[2]};

        // Calculate output error
        double outputError = targetOutput - output;
        double outputDelta = outputError * sigmoidDerivative(output);

        // Calculate hidden layer errors and deltas
        double[] hiddenErrors = new double[HIDDEN_NEURONS];
        double[] hiddenDeltas = new double[HIDDEN_NEURONS];

        for (int j = 0; j < HIDDEN_NEURONS; j++) {
            hiddenErrors[j] = outputDelta * weightsHiddenOutput[j];
            hiddenDeltas[j] = hiddenErrors[j] * sigmoidDerivative(hiddenOutputs[j]);
        }

        // Update weights Hidden -> Output
        for (int j = 0; j < HIDDEN_NEURONS; j++) {
            weightsHiddenOutput[j] += LEARNING_RATE * outputDelta * hiddenOutputs[j];
        }
        biasOutput += LEARNING_RATE * outputDelta;

        // Update weights Input -> Hidden
        for (int i = 0; i < INPUT_NEURONS; i++) {
            for (int j = 0; j < HIDDEN_NEURONS; j++) {
                weightsInputHidden[i][j] += LEARNING_RATE * hiddenDeltas[j] * inputs[i];
            }
        }
        for (int j = 0; j < HIDDEN_NEURONS; j++) {
            biasHidden[j] += LEARNING_RATE * hiddenDeltas[j];
        }
    }

    private void printWeights() {
        System.out.println("Weights Input-Hidden:");
        for (int i = 0; i < INPUT_NEURONS; i++) {
            for (int j = 0; j < HIDDEN_NEURONS; j++) {
                System.out.printf("% .4f ", weightsInputHidden[i][j]);
            }
            System.out.println();
        }
        System.out.println("Weights Hidden-Output:");
        for (int j = 0; j < HIDDEN_NEURONS; j++) {
            System.out.printf("% .4f ", weightsHiddenOutput[j]);
        }
        System.out.println();
        System.out.println();
    }

    public static void main(String[] args) {
        NeuralNetworkSimulator nn = new NeuralNetworkSimulator();
        Scanner scanner = new Scanner(System.in);

        // XOR training data: inputs and expected outputs
        double[][] trainingInputs = {
            {0, 0},
            {0, 1},
            {1, 0},
            {1, 1}
        };
        double[] expectedOutputs = {0, 1, 1, 0};

        System.out.println("Training Neural Network to learn XOR:");
        // Train for some iterations
        for (int epoch = 0; epoch < 10000; epoch++) {
            for (int i = 0; i < trainingInputs.length; i++) {
                nn.train(trainingInputs[i], expectedOutputs[i]);
            }
        }

        System.out.println("Training completed.\n");

        // Test phase
        while (true) {
            System.out.println("Enter two binary inputs (0 or 1), separated by space, or 'exit' to quit:");
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            if (line.equalsIgnoreCase("exit")) {
                break;
            }
            String[] parts = line.split("\\s+");
            if (parts.length != 2) {
                System.out.println("Please enter exactly two inputs.");
                continue;
            }
            try {
                double input1 = Double.parseDouble(parts[0]);
                double input2 = Double.parseDouble(parts[1]);
                if ((input1 != 0 && input1 != 1) || (input2 != 0 && input2 != 1)) {
                    System.out.println("Inputs must be 0 or 1.");
                    continue;
                }
                double[] outputAndHidden = nn.feedForward(new double[] {input1, input2});
                double outputVal = outputAndHidden[0];
                System.out.printf("Output (approx): %.4f (rounded: %d)%n", outputVal, outputVal >= 0.5 ? 1 : 0);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Enter numbers 0 or 1.");
            }
        }

        scanner.close();
        System.out.println("Exiting Neural Network Simulator.");
    }
}
