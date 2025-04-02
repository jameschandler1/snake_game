package snake_game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.Random;

public class SnakeGame extends JPanel implements ActionListener {
    private final int TILE_SIZE = 18;
    private final int WIDTH = 800;
    private final int HEIGHT = 600;
    private final int TOTAL_TILES = (WIDTH * HEIGHT) / (TILE_SIZE * TILE_SIZE);
    private static final String HIGH_SCORE_FILE = "highscore.txt";
    
    private final int[] x = new int[TOTAL_TILES];
    private final int[] y = new int[TOTAL_TILES];
    
    private int bodyParts = 5;
    private int foodX, foodY;
    private char direction = 'R';
    private boolean running = false;
    private Timer movementTimer;
    private Timer scoreTimer;
    private int score = 0;
    private int highScore;
    
    public SnakeGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(new KeyHandler());
        loadHighScore();
        startGame();
    }
    
    private void startGame() {
        score = 0;
        placeFood();
        running = true;
        movementTimer = new Timer(95, this);
        scoreTimer = new Timer(1000, e -> {
            if (running) {
                score += 1;
                repaint();
            }
        });
        movementTimer.start();
        scoreTimer.start();
    }
    
    private void placeFood() {
        Random random = new Random();
        foodX = random.nextInt(WIDTH / TILE_SIZE) * TILE_SIZE;
        foodY = random.nextInt(HEIGHT / TILE_SIZE) * TILE_SIZE;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }
    
    private void draw(Graphics g) {
        if (running) {
            g.setColor(Color.YELLOW);
            g.fillOval(foodX, foodY, TILE_SIZE, TILE_SIZE);
            
            for (int i = 0; i < bodyParts; i++) {
                g.setColor(i == 0 ? Color.RED : Color.GREEN);
                g.fillRect(x[i], y[i], TILE_SIZE, TILE_SIZE);
            }
            
            g.setColor(Color.WHITE);
            g.setFont(new Font("Roboto", Font.BOLD, 20));
            g.drawString("Score: " + score, 10, 20);
            g.drawString("High Score: " + highScore, 10, 40);
        } else {
            gameOver(g);
        }
    }
    
    private void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }
        
        switch (direction) {
            case 'U' -> y[0] -= TILE_SIZE;
            case 'D' -> y[0] += TILE_SIZE;
            case 'L' -> x[0] -= TILE_SIZE;
            case 'R' -> x[0] += TILE_SIZE;
        }
    }
    
    private void checkFood() {
        if (x[0] == foodX && y[0] == foodY) {
            bodyParts++;
            score += 100;
            placeFood();
        }
    }
    
    private void checkCollision() {
        for (int i = bodyParts; i > 0; i--) {
            if (x[0] == x[i] && y[0] == y[i]) {
                running = false;
            }
        }
        if (x[0] < 0 || x[0] >= WIDTH || y[0] < 0 || y[0] >= HEIGHT) {
            running = false;
        }
        if (!running) {
            movementTimer.stop();
            scoreTimer.stop();
            saveHighScore();
        }
    }
    
    private void gameOver(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Roboto", Font.BOLD, 40));
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString("Game Over", (WIDTH - metrics.stringWidth("Game Over")) / 2, HEIGHT / 2);
        g.setFont(new Font("Roboto", Font.BOLD, 20));
        g.drawString("Final Score: " + score, (WIDTH - metrics.stringWidth("Final Score: " + score)) / 2, HEIGHT / 2 + 40);
        g.drawString("High Score: " + highScore, (WIDTH - metrics.stringWidth("High Score: " + highScore)) / 2, HEIGHT / 2 + 70);
        
        int response = JOptionPane.showConfirmDialog(this, "Play again?", "Game Over", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            resetGame();
        } else {
            System.exit(0);
        }
    }
    
    private void resetGame() {
        bodyParts = 5;
        direction = 'R';
        score = 0;
        running = true;
        x[0] = 25;
        y[0] = 50;
        placeFood();
        movementTimer.start();
        scoreTimer.start();
        repaint();
    }
    
    private void loadHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader(HIGH_SCORE_FILE))) {
            highScore = Integer.parseInt(reader.readLine());
        } catch (IOException | NumberFormatException e) {
            highScore = 0;
        }
    }
    
    private void saveHighScore() {
        if (score > highScore) {
            highScore = score;
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGH_SCORE_FILE))) {
                writer.write(String.valueOf(highScore));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkFood();
            checkCollision();
        }
        repaint();
    }
    
    private class KeyHandler extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT -> { if (direction != 'R') direction = 'L'; }
                case KeyEvent.VK_RIGHT -> { if (direction != 'L') direction = 'R'; }
                case KeyEvent.VK_UP -> { if (direction != 'D') direction = 'U'; }
                case KeyEvent.VK_DOWN -> { if (direction != 'U') direction = 'D'; }
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Snake Game");
            SnakeGame game = new SnakeGame();
            frame.add(game);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
