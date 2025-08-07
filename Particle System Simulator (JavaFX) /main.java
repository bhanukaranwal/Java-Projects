import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class ParticleSystemSimulator extends Application {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private static final int PARTICLE_COUNT = 200;
    private static final double GRAVITY = 0.5;
    private static final double FRICTION = 0.98;

    private final Random random = new Random();

    private List<Particle> particles = new ArrayList<>();

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Initialize particles
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particles.add(createParticle());
        }

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateParticles();
                render(gc);
            }
        };
        timer.start();

        stage.setTitle("Particle System Simulator (JavaFX)");
        stage.setScene(new Scene(new javafx.scene.Group(canvas)));
        stage.show();
    }

    private Particle createParticle() {
        double x = WIDTH / 2 + random.nextDouble() * 100 - 50;
        double y = HEIGHT / 2 + random.nextDouble() * 100 - 50;
        double vx = random.nextDouble() * 4 - 2;
        double vy = random.nextDouble() * -6 - 2;
        double radius = random.nextDouble() * 4 + 2;
        Color color = Color.hsb(random.nextDouble() * 360, 0.8, 0.9, 1.0);
        return new Particle(x, y, vx, vy, radius, color);
    }

    private void updateParticles() {
        Iterator<Particle> iter = particles.iterator();
        while (iter.hasNext()) {
            Particle p = iter.next();

            // Apply gravity
            p.vy += GRAVITY;

            // Move particle
            p.x += p.vx;
            p.y += p.vy;

            // Bounce off floor
            if (p.y + p.radius > HEIGHT) {
                p.y = HEIGHT - p.radius;
                p.vy *= -0.6;  // lose some energy on bounce
                p.vx *= FRICTION;
            }

            // Bounce off walls
            if (p.x - p.radius < 0) {
                p.x = p.radius;
                p.vx *= -0.7;
            } else if (p.x + p.radius > WIDTH) {
                p.x = WIDTH - p.radius;
                p.vx *= -0.7;
            }

            // Slow down horizontal velocity slowly
            p.vx *= FRICTION;

            // Fade out
            p.life -= 0.01;
            if (p.life <= 0) {
                // Respawn particle
                iter.remove();
                particles.add(createParticle());
            }
        }
    }

    private void render(GraphicsContext gc) {
        // Clear screen with semi-transparent black for trail effect
        gc.setFill(Color.rgb(0, 0, 0, 0.2));
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw particles
        for (Particle p : particles) {
            gc.setFill(p.color.deriveColor(0, 1, 1, Math.max(p.life, 0)));
            gc.fillOval(p.x - p.radius, p.y - p.radius, p.radius * 2, p.radius * 2);
        }
    }

    private static class Particle {
        double x, y;
        double vx, vy;
        double radius;
        Color color;
        double life = 1.0;

        Particle(double x, double y, double vx, double vy, double radius, Color color) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.radius = radius;
            this.color = color;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
