import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimplePhysicsEngine extends Application {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final double GRAVITY = 500; // pixels per second squared
    private static final double RESTITUTION = 0.75; // bounce damping
    private static final double FRICTION = 0.99; // horizontal velocity damping

    private final List<Ball> balls = new ArrayList<>();
    private final Random random = new Random();

    private long lastTime = 0;

    private static class Ball {
        double x, y;
        double vx, vy;
        double radius;
        Color color;
        double mass;

        Ball(double x, double y, double vx, double vy, double radius, Color color) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.radius = radius;
            this.color = color;
            this.mass = radius * radius * radius;  // mass proportional to volume (cube of radius)
        }
    }

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Create some balls with random positions, velocities, and colors
        for (int i = 0; i < 15; i++) {
            double radius = random.nextDouble() * 20 + 15;
            double x = radius + random.nextDouble() * (WIDTH - 2 * radius);
            double y = radius + random.nextDouble() * (HEIGHT / 2);
            double vx = random.nextDouble() * 200 - 100;
            double vy = random.nextDouble() * 50 - 25;
            Color color = Color.color(random.nextDouble(), random.nextDouble(), random.nextDouble());
            balls.add(new Ball(x, y, vx, vy, radius, color));
        }

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }
                double deltaTime = (now - lastTime) / 1_000_000_000.0; // seconds
                lastTime = now;

                updatePhysics(deltaTime);
                render(gc);
            }
        };
        timer.start();

        stage.setTitle("Simple Physics Engine - Bouncing Balls");
        stage.setScene(new Scene(new javafx.scene.Group(canvas)));
        stage.show();
    }

    private void updatePhysics(double dt) {
        // Update velocities and positions with gravity
        for (Ball ball : balls) {
            ball.vy += GRAVITY * dt;
            ball.x += ball.vx * dt;
            ball.y += ball.vy * dt;

            // Bounce off floor
            if (ball.y + ball.radius > HEIGHT) {
                ball.y = HEIGHT - ball.radius;
                ball.vy = -ball.vy * RESTITUTION;
                ball.vx *= FRICTION; // friction on bounce

                // If velocity is very small, stop bouncing
                if (Math.abs(ball.vy) < 1) ball.vy = 0;
            }

            // Bounce off ceiling
            if (ball.y - ball.radius < 0) {
                ball.y = ball.radius;
                ball.vy = -ball.vy * RESTITUTION;
            }

            // Bounce off walls
            if (ball.x - ball.radius < 0) {
                ball.x = ball.radius;
                ball.vx = -ball.vx * RESTITUTION;
            }
            if (ball.x + ball.radius > WIDTH) {
                ball.x = WIDTH - ball.radius;
                ball.vx = -ball.vx * RESTITUTION;
            }
        }

        // Handle ball-to-ball collisions
        for (int i = 0; i < balls.size(); i++) {
            Ball b1 = balls.get(i);
            for (int j = i + 1; j < balls.size(); j++) {
                Ball b2 = balls.get(j);
                resolveCollision(b1, b2);
            }
        }
    }

    // Elastic collision response between two balls
    private void resolveCollision(Ball b1, Ball b2) {
        double dx = b2.x - b1.x;
        double dy = b2.y - b1.y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        double minDist = b1.radius + b2.radius;

        if (dist < minDist && dist > 0) {
            // Normalize vector between balls
            double nx = dx / dist;
            double ny = dy / dist;

            // Calculate relative velocity in normal direction
            double dvx = b1.vx - b2.vx;
            double dvy = b1.vy - b2.vy;
            double relVel = dvx * nx + dvy * ny;

            if (relVel > 0) return; // balls moving apart

            // Calculate impulse scalar
            double e = RESTITUTION;
            double j = -(1 + e) * relVel;
            j /= (1 / b1.mass) + (1 / b2.mass);

            // Apply impulse to each ball’s velocity
            double impulseX = j * nx;
            double impulseY = j * ny;
            b1.vx += impulseX / b1.mass;
            b1.vy += impulseY / b1.mass;
            b2.vx -= impulseX / b2.mass;
            b2.vy -= impulseY / b2.mass;

            // Positional correction to avoid sinking
            double penetration = minDist - dist;
            double correctionPercent = 0.5; // usually 20-80%
            double correctionX = nx * penetration * correctionPercent;
            double correctionY = ny * penetration * correctionPercent;

            b1.x -= correctionX;
            b1.y -= correctionY;
            b2.x += correctionX;
            b2.y += correctionY;
        }
    }

    private void render(GraphicsContext gc) {
        // Clear background
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw each ball
        for (Ball ball : balls) {
            gc.setFill(ball.color);
            gc.fillOval(ball.x - ball.radius, ball.y - ball.radius, ball.radius * 2, ball.radius * 2);
            gc.setStroke(Color.BLACK);
            gc.strokeOval(ball.x - ball.radius, ball.y - ball.radius, ball.radius * 2, ball.radius * 2);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
