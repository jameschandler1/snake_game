package snake_game;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Random;

public class SnakeGame extends JPanel implements ActionListener {
    private final int TILE_SIZE = 20;
    private final int WIDTH = 600;
    private final int HEIGHT = 600;
    private final int TOTAL_TILES = (WIDTH * HEIGHT) / (TILE_SIZE * TILE_SIZE);
    private static final String HIGH_SCORE_FILE = "highscore.txt";

    private final int[] x = new int[TOTAL_TILES];
    private final int[] y = new int[TOTAL_TILES];

    private int bodyParts = 5;
    private int foodX, foodY;
    private char direction = 'R';
    private boolean running = false;
    private boolean paused = false;
    private boolean gameStarted = false;
    private int difficulty = 5;
    private Timer movementTimer;
    private Timer scoreTimer;
    private int score = 0;
    private int highScore;
    private Image backgroundImage;
    private Clip backgroundMusic;

    private final Font retroFont = new Font("Courier New", Font.BOLD, 22);
    private boolean soundMuted = false;
    private float volume = 1.0f;

    private JSlider volumeSlider;
    private JButton muteButton;
    private JSlider difficultySlider;

    private int foodColorState = 0;
    private final Color[] foodColors = {Color.RED, Color.ORANGE, Color.MAGENTA, Color.PINK, Color.YELLOW};
    private Timer foodColorTimer;

    private JButton restartButton;

    public SnakeGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        setLayout(null);
        addKeyListener(new KeyHandler());
        loadHighScore();
        loadBackground();
        setupAudioControls();
        startBackgroundMusic();
        setupFoodColorTimer();
        showStartMenu();
    }

    private void setupAudioControls() {
        volumeSlider = new JSlider(0, 100, (int) (volume * 100));
        volumeSlider.setBounds(WIDTH - 160, 10, 100, 30);
        volumeSlider.setToolTipText("Volume (dB)");
        volumeSlider.setFocusable(false);
        volumeSlider.addChangeListener(e -> {
            volume = volumeSlider.getValue() / 100f;
        });
        add(volumeSlider);

        muteButton = new JButton("Mute");
        muteButton.setBounds(WIDTH - 60, 10, 50, 30);
        muteButton.setFont(new Font("Courier New", Font.PLAIN, 10));
        muteButton.setFocusable(false);
        muteButton.addActionListener(e -> {
            soundMuted = !soundMuted;
            muteButton.setText(soundMuted ? "Unmute" : "Mute");
        });
        add(muteButton);

        difficultySlider = new JSlider(1, 11, difficulty);
        difficultySlider.setBounds(10, HEIGHT - 40, 200, 30);
        difficultySlider.setToolTipText("Difficulty (1 - 11)");
        difficultySlider.setFocusable(false);
        difficultySlider.setPaintTicks(true);
        difficultySlider.setPaintLabels(true);
        difficultySlider.setMajorTickSpacing(1);
        difficultySlider.setSnapToTicks(true);
        difficultySlider.addChangeListener(e -> {
            difficulty = difficultySlider.getValue();
            if (movementTimer != null) {
                movementTimer.setDelay(200 - (difficulty * 10));
            }
        });
        add(difficultySlider);

        restartButton = new JButton("Restart");
        restartButton.setBounds((WIDTH - 100) / 2, HEIGHT - 40, 100, 30);
        restartButton.setFont(new Font("Courier New", Font.PLAIN, 12));
        restartButton.setFocusable(false);
        restartButton.addActionListener(e -> {
            resetGame();
        });
        restartButton.setVisible(false);
        add(restartButton);
    }

    private void setupFoodColorTimer() {
        foodColorTimer = new Timer(150, e -> {
            foodColorState = (foodColorState + 1) % foodColors.length;
            repaint();
        });
        foodColorTimer.start();
    }

    private void loadBackground() {
        try {
            backgroundImage = new ImageIcon("retro_bg.png").getImage();
        } catch (Exception e) {
            backgroundImage = null;
        }
    }

    private void startBackgroundMusic() {
        if (soundMuted) return;
        try {
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(new File("background.wav"));
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioInput);
            FloatControl gainControl = (FloatControl) backgroundMusic.getControl(FloatControl.Type.MASTER_GAIN);
            if (volume > 0f) {
                gainControl.setValue(20f * (float) Math.log10(volume));
            } else {
                gainControl.setValue(gainControl.getMinimum());
            }
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showStartMenu() {
    JDialog menuDialog = new JDialog();
    menuDialog.setUndecorated(true);
    menuDialog.setSize(WIDTH, HEIGHT);
    menuDialog.setLocationRelativeTo(null);
    menuDialog.setModal(true);

    JPanel menuPanel = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, WIDTH, HEIGHT, this);
            } else {
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, WIDTH, HEIGHT);
            }
        }
    };
    menuPanel.setLayout(null);
    menuPanel.setBackground(Color.BLACK);

    JLabel titleLabel = new JLabel("S N A K E  G A M E", SwingConstants.CENTER);
    titleLabel.setFont(retroFont.deriveFont(Font.BOLD, 42f));
    titleLabel.setForeground(Color.GREEN);
    titleLabel.setBounds(0, 40, WIDTH, 60);
    menuPanel.add(titleLabel);

    JTextArea infoArea = new JTextArea(
        "➤ Use ← ↑ ↓ → to move\n" +
        "➤ Press 'P' to pause\n" +
        "➤ Choose difficulty (1–11)\n\n" +
        "▶ Press ENTER to Start"
    );
    infoArea.setFont(retroFont.deriveFont(20f));
    infoArea.setForeground(Color.CYAN);
    infoArea.setBackground(new Color(0, 0, 0, 180));
    infoArea.setOpaque(true);
    infoArea.setEditable(false);
    infoArea.setFocusable(false);
    infoArea.setHighlighter(null);
    infoArea.setBounds(WIDTH / 2 - 220, HEIGHT / 2 - 100, 440, 180);
    menuPanel.add(infoArea);

    menuDialog.setContentPane(menuPanel);

    // Key listener to start the game
    menuDialog.addKeyListener(new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                menuDialog.dispose();
            }
        }
    });

    // Ensure key listener works
    menuDialog.setFocusable(true);
    menuDialog.requestFocusInWindow();

    menuDialog.setVisible(true);
    gameStarted = true;
    startGame();
}


    private void startGame() {
        score = 0;
        placeFood();
        running = true;
        paused = false;
        x[0] = 0;
        y[0] = 0;
        movementTimer = new Timer(200 - (difficulty * 10), this);
        scoreTimer = new Timer(1000, e -> {
            if (running && !paused) {
                score += 1;
                repaint();
            }
        });
        movementTimer.start();
        scoreTimer.start();
        volumeSlider.setVisible(true);
        muteButton.setVisible(true);
        difficultySlider.setVisible(true);
        restartButton.setVisible(false);
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
        if (!gameStarted) return;
        draw(g);
    }

    private void draw(Graphics g) {
        if (paused) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Courier New", Font.BOLD, 30));
            g.drawString("== PAUSED ==", WIDTH / 2 - 100, HEIGHT / 2);
            g.setFont(new Font("Courier New", Font.PLAIN, 18));
            g.drawString("Press ENTER or P to continue", WIDTH / 2 - 140, HEIGHT / 2 + 30);
        }

        if (running) {
            g.setColor(foodColors[foodColorState]);
            g.fillOval(foodX, foodY, TILE_SIZE, TILE_SIZE);

            for (int i = 0; i < bodyParts; i++) {
                g.setColor(i == 0 ? Color.GREEN : Color.LIGHT_GRAY);
                g.fillRect(x[i], y[i], TILE_SIZE, TILE_SIZE);
            }

            g.setColor(Color.CYAN);
            g.setFont(retroFont);
            g.drawString("SCORE: " + score, 10, 25);
            g.drawString("HIGH SCORE: " + highScore, 10, 50);

            volumeSlider.setVisible(true);
            muteButton.setVisible(true);
            difficultySlider.setVisible(true);
            restartButton.setVisible(false);
        } else {
            volumeSlider.setVisible(false);
            muteButton.setVisible(false);
            difficultySlider.setVisible(false);
            restartButton.setVisible(true);
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
            score += 10;
            placeFood();
            playSound("eat.wav");
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
    }

    private void resetHighScore() {
        highScore = 0;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGH_SCORE_FILE))) {
            writer.write("0");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetGame() {
        bodyParts = 5;
        direction = 'R';
        score = 0;
        running = true;
        paused = false;
        x[0] = 0;
        y[0] = 0;
        placeFood();
        movementTimer.setDelay(200 - (difficulty * 10));
        movementTimer.start();
        scoreTimer.start();
        volumeSlider.setVisible(true);
        muteButton.setVisible(true);
        difficultySlider.setVisible(true);
        restartButton.setVisible(false);
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
            File soundFile = new File(fileName);
            if (!soundFile.exists()) {
                System.err.println("Sound file not found: " + fileName);
                return;
            }

            AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInput);

            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                if (volume > 0f) {
                    gainControl.setValue(20f * (float) Math.log10(volume));
                } else {
                    gainControl.setValue(gainControl.getMinimum());
                }
            }

            clip.start();
            System.out.println("Playing sound: " + fileName);
        } catch (UnsupportedAudioFileException e) {
            System.err.println("Unsupported audio format: " + fileName);
        } catch (IOException e) {
            System.err.println("I/O error while playing sound: " + fileName);
        } catch (LineUnavailableException e) {
            System.err.println("Audio line unavailable for: " + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running && !paused) {
            move();
            checkFood();
            checkCollision();
        }
        repaint();
    }

    private class KeyHandler extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            requestFocusInWindow();
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT -> {
                    if (direction != 'R' && !paused) direction = 'L';
                }
                case KeyEvent.VK_RIGHT -> {
                    if (direction != 'L' && !paused) direction = 'R';
                }
                case KeyEvent.VK_UP -> {
                    if (direction != 'D' && !paused) direction = 'U';
                }
                case KeyEvent.VK_DOWN -> {
                    if (direction != 'U' && !paused) direction = 'D';
                }
                case KeyEvent.VK_P, KeyEvent.VK_ENTER -> {
                    if (gameStarted) {
                        paused = !paused;
                        repaint();
                    }
                }
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
