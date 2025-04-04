package snake_game;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.FloatControl;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Random;

public class SnakeGame extends JPanel implements ActionListener {
    private final int TILE_SIZE = 20;
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
    private Image backgroundImage;

    private boolean soundMuted = false;
    private float volume = 1.0f;

    private final Font retroFont = new Font("Courier New", Font.BOLD, 22);
    private JSlider volumeSlider;
    private JButton muteButton;

    public SnakeGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(new KeyHandler());
        loadHighScore();
        loadBackground();
        addVolumeControls();
        startGame();
    }

    private void loadBackground() {
        try {
            backgroundImage = new ImageIcon(getClass().getResource("/retro_bg.png")).getImage();
        } catch (Exception e) {
            backgroundImage = null;
        }
    }

    private void addVolumeControls() {
        setLayout(null);

        muteButton = new JButton("Mute");
        muteButton.setBounds(WIDTH - 120, 10, 100, 30);
        muteButton.setFont(new Font("Courier New", Font.PLAIN, 14));
        muteButton.addActionListener(e -> {
            soundMuted = !soundMuted;
            muteButton.setText(soundMuted ? "Unmute" : "Mute");
        });
        add(muteButton);

        volumeSlider = new JSlider(0, 100, (int)(volume * 100));
        volumeSlider.setBounds(WIDTH - 160, 50, 140, 20);
        volumeSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                volume = volumeSlider.getValue() / 100f;
            }
        });
        add(volumeSlider);
    }

    private void startGame() {
        score = 0;
        placeFood();
        running = true;
        movementTimer = new Timer(100, this);
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
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, WIDTH, HEIGHT, this);
        }
        draw(g);
    }

    private void draw(Graphics g) {
        if (running) {
            g.setColor(Color.RED);
            g.fillOval(foodX, foodY, TILE_SIZE, TILE_SIZE);

            for (int i = 0; i < bodyParts; i++) {
                g.setColor(i == 0 ? Color.GREEN : Color.LIGHT_GRAY);
                g.fillRect(x[i], y[i], TILE_SIZE, TILE_SIZE);
            }

            g.setColor(Color.CYAN);
            g.setFont(retroFont);
            g.drawString("SCORE: " + score, 10, 25);
            g.drawString("HIGH SCORE: " + highScore, 10, 50);
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
            playSound("eat.wav");
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
            playSound("gameover.wav");
        }
    }

    private void gameOver(Graphics g) {
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Courier New", Font.BOLD, 36));
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString("== GAME OVER ==", (WIDTH - metrics.stringWidth("== GAME OVER ==")) / 2, HEIGHT / 2);

        g.setFont(new Font("Courier New", Font.PLAIN, 20));
        g.drawString("FINAL SCORE: " + score, (WIDTH - metrics.stringWidth("FINAL SCORE: " + score)) / 2, HEIGHT / 2 + 40);
        g.drawString("HIGH SCORE: " + highScore, (WIDTH - metrics.stringWidth("HIGH SCORE: " + highScore)) / 2, HEIGHT / 2 + 70);

        UIManager.put("OptionPane.messageFont", new Font("Courier New", Font.PLAIN, 18));
        UIManager.put("OptionPane.buttonFont", new Font("Courier New", Font.BOLD, 16));

        int response = JOptionPane.showConfirmDialog(this, "Wanna try again?", "== GAME OVER ==", JOptionPane.YES_NO_OPTION);
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
        x[0] = 0;
        y[0] = 0;
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

   private void playSound(String fileName) {
    if (soundMuted) return;
    try {
        AudioInputStream audioInput = AudioSystem.getAudioInputStream(
            getClass().getResource(fileName)
        );
			Clip clip = AudioSystem.getClip();
			clip.open(audioInput);
	
			FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        if (volume > 0f) {
				gainControl.setValue(20f * (float) Math.log10(volume));
			} else {
				gainControl.setValue(gainControl.getMinimum());
			}

			clip.start();
		} catch (Exception e) {
			e.printStackTrace();
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
