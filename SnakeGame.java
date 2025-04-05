	package snake_game;

	import javax.sound.sampled.*;
	import javax.swing.*;
	import java.awt.*;
	import java.awt.event.*;
	import java.io.*;
	import java.util.Random;


	// Main game class with static constants
	public class SnakeGame extends JPanel implements ActionListener {
		// Constants are now properly static
		private static final int TILE_SIZE = 12;
		private static final int WIDTH = 800;
		private static final int HEIGHT = 500;
		private static final int BORDER_SIZE = 60; // Border size for UI elements
		private static final int PLAY_AREA_WIDTH = WIDTH - (2 * BORDER_SIZE);
		private static final int PLAY_AREA_HEIGHT = HEIGHT - (2 * BORDER_SIZE);
		private static final int TOTAL_TILES = (PLAY_AREA_WIDTH * PLAY_AREA_HEIGHT) / (TILE_SIZE * TILE_SIZE);
		private static final String HIGH_SCORE_FILE = "highscore.txt";

		private final int[] x = new int[TOTAL_TILES];
		private final int[] y = new int[TOTAL_TILES];

		private int bodyParts = 5;
		private int foodX, foodY;
		private char direction = 'R';
		private boolean running = false;
		private boolean paused = false;
		private boolean gameStarted = false;
		private int difficulty = 6;
		private Timer movementTimer;
		private Timer scoreTimer;
		private int score = 0;
		private int highScore;
		private Image backgroundImage;
		private Clip backgroundMusic;

		private boolean soundMuted = false;
		private float volume = 1.0f;

		private int foodColorState = 0;
		private final Color[] foodColors = {Color.RED, Color.ORANGE, Color.PINK, Color.YELLOW};
		private Timer foodColorTimer;
		
		private Color snakeHeadColor = Color.GREEN;
		private Color snakeBodyColor = Color.LIGHT_GRAY;
		private boolean wallWrapEnabled = false;
		
		// UI components organized into a separate class
		private GameUI gameUI;
		
		// Side panels for UI controls
		private JPanel topPanel;
		private JPanel bottomPanel;
		private JPanel leftPanel;
		private JPanel rightPanel;
		private JPanel gamePanel; // The actual gameplay area
		
		// UI components organized into separate classes
	class GameUI {
		final Font retroFont = new Font("Courier New", Font.BOLD, 22);
		JSlider volumeSlider;
		JButton muteButton;
		JSlider difficultySlider;
		JButton restartButton;
		JComboBox<String> colorSelector;
		JCheckBox wallWrapCheckbox;
		
		// Add label references for score display
		JLabel scoreLabel;
		JLabel highScoreLabel;
		
		GameUI(SnakeGame game, int width, int height, float initialVolume, int initialDifficulty) {
			setupScoreLabels();
			setupAudioControls(game, width, height, initialVolume);
			setupGameControls(game, width, height, initialDifficulty);
		}
		
		private void setupScoreLabels() {
			scoreLabel = new JLabel("SCORE: 0");
			scoreLabel.setFont(retroFont);
			scoreLabel.setForeground(Color.CYAN);
			
			highScoreLabel = new JLabel("HIGH SCORE: 0");
			highScoreLabel.setFont(retroFont);
			highScoreLabel.setForeground(Color.CYAN);
		}
		
		private void setupAudioControls(SnakeGame game, int width, int height, float initialVolume) {
			volumeSlider = new JSlider(0, 100, (int) (initialVolume * 100));
			volumeSlider.setToolTipText("Volume (dB)");
			volumeSlider.setFocusable(false);
			volumeSlider.addChangeListener(e -> {
				game.setVolume(volumeSlider.getValue() / 100f);
			});
			
			muteButton = new JButton("Mute");
			muteButton.setFont(new Font("Courier New", Font.PLAIN, 10));
			muteButton.setFocusable(false);
			muteButton.addActionListener(e -> {
				game.toggleMute();
				muteButton.setText(game.isMuted() ? "Unmute" : "Mute");
			});
		}
		
		private void setupGameControls(SnakeGame game, int width, int height, int initialDifficulty) {
			difficultySlider = new JSlider(1, 11, initialDifficulty);
			difficultySlider.setToolTipText("Difficulty (1 - 11)");
			difficultySlider.setFocusable(false);
			difficultySlider.setPaintTicks(true);
			difficultySlider.setPaintLabels(true);
			difficultySlider.setMajorTickSpacing(1);
			difficultySlider.setSnapToTicks(true);
			difficultySlider.addChangeListener(e -> {
				game.setDifficulty(difficultySlider.getValue());
			});
			
			restartButton = new JButton("Restart");
			restartButton.setFont(new Font("Courier New", Font.PLAIN, 12));
			restartButton.setFocusable(false);
			restartButton.addActionListener(e -> {
				game.resetGame();
			});
			restartButton.setVisible(false);
			
			colorSelector = new JComboBox<>(new String[]{"Green", "Blue", "Purple", "White"});
			colorSelector.setFocusable(false);
			colorSelector.addActionListener(e -> {
				String selected = (String) colorSelector.getSelectedItem();
				switch (selected) {
					case "Green" -> game.setSnakeColors(Color.GREEN, Color.LIGHT_GRAY);
					case "Blue" -> game.setSnakeColors(Color.BLUE, new Color(173, 216, 230));
					case "Purple" -> game.setSnakeColors(new Color(138, 43, 226), new Color(216, 191, 216));
					case "White" -> game.setSnakeColors(Color.WHITE, Color.GRAY);
				}
			});
			
			wallWrapCheckbox = new JCheckBox("Wrap Walls");
			wallWrapCheckbox.setOpaque(false);
			wallWrapCheckbox.setFocusable(false);
			wallWrapCheckbox.setForeground(Color.WHITE);
			wallWrapCheckbox.addActionListener(e -> {
				game.setWallWrapEnabled(wallWrapCheckbox.isSelected());
			});
		}
		
		void updateScoreDisplay(int score, int highScore) {
			scoreLabel.setText("SCORE: " + score);
			highScoreLabel.setText("HIGH SCORE: " + highScore);
		}
		
		void updateMuteButtonText(boolean muted) {
			muteButton.setText(muted ? "Unmute" : "Mute");
		}
		
		void toggleControlsVisibility(boolean visible) {
			volumeSlider.setVisible(visible);
			muteButton.setVisible(visible);
			difficultySlider.setVisible(visible);
		}
		
		void setRestartButtonVisible(boolean visible) {
			restartButton.setVisible(visible);
		}
	}


		public SnakeGame() {
			setPreferredSize(new Dimension(WIDTH, HEIGHT));
			setFocusable(true);
			setLayout(new BorderLayout());
			addKeyListener(new KeyHandler());
			
			// Initialize UI components
			gameUI = new GameUI(this, WIDTH, HEIGHT, volume, difficulty);
			
			// Create border panels and gameplay area
			createPanels();
			
			loadHighScore();
			loadBackground();
			setupFoodColorTimer();
			startBackgroundMusic();
			showStartMenu();
			
			// Add window focus listener to auto-pause
			addWindowFocusListener();
			
			// Make parent window non-resizable if exists
			SwingUtilities.invokeLater(() -> {
				Window window = SwingUtilities.getWindowAncestor(this);
				if (window instanceof JFrame) {
					((JFrame) window).setResizable(false);
				}
			});
		}
		
		private void createPanels() {
			// Create a panel for the gameplay area
			gamePanel = new JPanel() {
				@Override
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					if (backgroundImage != null) {
						g.drawImage(backgroundImage, 0, 0, PLAY_AREA_WIDTH, PLAY_AREA_HEIGHT, this);
					}
					if (gameStarted) {
						drawGame(g);
					}
				}
			};
			gamePanel.setPreferredSize(new Dimension(PLAY_AREA_WIDTH, PLAY_AREA_HEIGHT));
			gamePanel.setBackground(Color.BLACK);
			gamePanel.setLayout(null);
			
			// Create border panels
			topPanel = new JPanel();
			topPanel.setPreferredSize(new Dimension(WIDTH, BORDER_SIZE));
			topPanel.setBackground(new Color(20, 20, 40));
			topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 5));
			
			bottomPanel = new JPanel();
			bottomPanel.setPreferredSize(new Dimension(WIDTH, BORDER_SIZE));
			bottomPanel.setBackground(new Color(20, 20, 40));
			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 5));
			
			leftPanel = new JPanel();
			leftPanel.setPreferredSize(new Dimension(BORDER_SIZE, HEIGHT));
			leftPanel.setBackground(new Color(20, 20, 40));
			
			rightPanel = new JPanel();
			rightPanel.setPreferredSize(new Dimension(BORDER_SIZE, HEIGHT));
			rightPanel.setBackground(new Color(20, 20, 40));
			
			// Add score labels to top panel
			topPanel.add(gameUI.scoreLabel);
			topPanel.add(gameUI.highScoreLabel);
			
			// Add audio controls to top panel
			JPanel audioPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			audioPanel.setOpaque(false);
			audioPanel.add(gameUI.volumeSlider);
			audioPanel.add(gameUI.muteButton);
			topPanel.add(Box.createHorizontalGlue());
			topPanel.add(audioPanel);
			
			// Add game controls to bottom panel
			bottomPanel.add(gameUI.difficultySlider);
			bottomPanel.add(gameUI.colorSelector);
			bottomPanel.add(gameUI.wallWrapCheckbox);
			bottomPanel.add(gameUI.restartButton);
			
			// Add panels to the main layout
			add(topPanel, BorderLayout.NORTH);
			add(bottomPanel, BorderLayout.SOUTH);
			add(leftPanel, BorderLayout.WEST);
			add(rightPanel, BorderLayout.EAST);
			add(gamePanel, BorderLayout.CENTER);
		}

		// Window focus listener to auto-pause when window loses focus
		private void addWindowFocusListener() {
			SwingUtilities.invokeLater(() -> {
				Window window = SwingUtilities.getWindowAncestor(this);
				if (window != null) {
					window.addWindowFocusListener(new WindowAdapter() {
						@Override
						public void windowLostFocus(WindowEvent e) {
							if (running && !paused) {
								paused = true;
								repaint();
							}
						}
					});
				}
			});
		}

		private void setupFoodColorTimer() {
			foodColorTimer = new Timer(150, e -> {
				foodColorState = (foodColorState + 1) % foodColors.length;
				gamePanel.repaint();
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
			
			// Using try-with-resources for proper resource management
			try (AudioInputStream audioInput = AudioSystem.getAudioInputStream(new File("background.wav"))) {
				backgroundMusic = AudioSystem.getClip();
				backgroundMusic.open(audioInput);
				updateVolumeControl(backgroundMusic);
				backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private void updateVolumeControl(Clip clip) {
			if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
				FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
				if (volume > 0f) {
					gainControl.setValue(20f * (float) Math.log10(volume));
				} else {
					gainControl.setValue(gainControl.getMinimum());
				}
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
			titleLabel.setFont(gameUI.retroFont.deriveFont(Font.BOLD, 42f));
			titleLabel.setForeground(Color.GREEN);
			titleLabel.setBounds(0, 40, WIDTH, 60);
			menuPanel.add(titleLabel);

			// Center the info text
			String infoText = "➤ Use ← ↑ ↓ → to move\n" +
							  "➤ Press 'P' to pause\n" +
							  "➤ Choose difficulty (1–11)\n\n" +
							  "▶ Press ENTER to Start";
							  
			JTextArea infoArea = new JTextArea(infoText);
			infoArea.setFont(gameUI.retroFont.deriveFont(20f));
			infoArea.setForeground(Color.CYAN);
			infoArea.setBackground(new Color(0, 0, 0, 180));
			infoArea.setOpaque(true);
			infoArea.setEditable(false);
			infoArea.setFocusable(false);
			infoArea.setHighlighter(null);
			
			// Calculate the width and height of the text area
			int infoWidth = 440;
			int infoHeight = 180;
			
			// Center it horizontally and position it vertically
			infoArea.setBounds((WIDTH - infoWidth) / 2, HEIGHT / 2 - 100, infoWidth, infoHeight);
			
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
			
			// Initialize snake position in the play area
			x[0] = BORDER_SIZE + TILE_SIZE;
			y[0] = BORDER_SIZE + TILE_SIZE;
			
			movementTimer = new Timer(200 - (difficulty * 10), this);
			scoreTimer = new Timer(1000, e -> {
				if (running && !paused) {
					score += 1;
					gameUI.updateScoreDisplay(score, highScore);
					gamePanel.repaint();
				}
			});
			movementTimer.start();
			scoreTimer.start();
			gameUI.toggleControlsVisibility(true);
			gameUI.setRestartButtonVisible(false);
			gameUI.updateScoreDisplay(score, highScore);
		}

		private void placeFood() {
			Random random = new Random();
			foodX = random.nextInt(PLAY_AREA_WIDTH / TILE_SIZE) * TILE_SIZE + BORDER_SIZE;
			foodY = random.nextInt(PLAY_AREA_HEIGHT / TILE_SIZE) * TILE_SIZE + BORDER_SIZE;
		}

		private void drawGame(Graphics g) {
			if (paused) {
				g.setColor(new Color(0, 0, 0, 150)); // translucent black overlay
				g.fillRect(0, 0, PLAY_AREA_WIDTH, PLAY_AREA_HEIGHT);
				g.setColor(Color.YELLOW);
				g.setFont(new Font("Courier New", Font.BOLD, 30));
				FontMetrics metrics = getFontMetrics(g.getFont());
				String pausedText = "== PAUSED ==";
				g.drawString(pausedText, (PLAY_AREA_WIDTH - metrics.stringWidth(pausedText)) / 2, PLAY_AREA_HEIGHT / 2);
				g.setFont(new Font("Courier New", Font.PLAIN, 18));
				String continueText = "Press ENTER or P to continue";
				g.drawString(continueText, (PLAY_AREA_WIDTH - metrics.stringWidth(continueText)) / 2, PLAY_AREA_HEIGHT / 2 + 30);
			}

			if (running) {
				g.setColor(foodColors[foodColorState]);
				g.fillOval(foodX - BORDER_SIZE, foodY - BORDER_SIZE, TILE_SIZE, TILE_SIZE);

				for (int i = 0; i < bodyParts; i++) {
					g.setColor(i == 0 ? snakeHeadColor : snakeBodyColor);
					g.fillRect(x[i] - BORDER_SIZE, y[i] - BORDER_SIZE, TILE_SIZE, TILE_SIZE);
				}
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
				score += 10;
				gameUI.updateScoreDisplay(score, highScore);
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
			
			// Calculate play area boundaries
			int minX = BORDER_SIZE;
			int maxX = WIDTH - BORDER_SIZE;
			int minY = BORDER_SIZE;
			int maxY = HEIGHT - BORDER_SIZE;
			
			if (wallWrapEnabled) {
				if (x[0] < minX) x[0] = maxX - TILE_SIZE;
				else if (x[0] >= maxX) x[0] = minX;
				if (y[0] < minY) y[0] = maxY - TILE_SIZE;
				else if (y[0] >= maxY) y[0] = minY;
			} else {
				if (x[0] < minX || x[0] >= maxX || y[0] < minY || y[0] >= maxY) {
					running = false;
				}
			}

			if (!running) {
				movementTimer.stop();
				scoreTimer.stop();
				saveHighScore();
				playSound("gameover.wav");
				gameUI.setRestartButtonVisible(true);
			}
		}

		private void gameOver(Graphics g) {
			g.setColor(Color.YELLOW);
			g.setFont(new Font("Courier New", Font.BOLD, 36));
			FontMetrics largeMetrics = getFontMetrics(g.getFont());
			String gameOverText = "== GAME OVER ==";
			g.drawString(gameOverText, (PLAY_AREA_WIDTH - largeMetrics.stringWidth(gameOverText)) / 2, PLAY_AREA_HEIGHT / 2);

			g.setFont(new Font("Courier New", Font.PLAIN, 20));
			FontMetrics smallMetrics = getFontMetrics(g.getFont());
			
			String finalScoreText = "FINAL SCORE: " + score;
			String highScoreText = "HIGH SCORE: " + highScore;
			
			g.drawString(finalScoreText, (PLAY_AREA_WIDTH - smallMetrics.stringWidth(finalScoreText)) / 2, PLAY_AREA_HEIGHT / 2 + 40);
			g.drawString(highScoreText, (PLAY_AREA_WIDTH - smallMetrics.stringWidth(highScoreText)) / 2, PLAY_AREA_HEIGHT / 2 + 70);
		}

		private void resetHighScore() {
			highScore = 0;
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGH_SCORE_FILE))) {
				writer.write("0");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		void resetGame() {
			bodyParts = 5;
			direction = 'R';
			score = 0;
			running = true;
			paused = false;
			
			// Reset snake position to starting point within play area
			x[0] = BORDER_SIZE + TILE_SIZE;
			y[0] = BORDER_SIZE + TILE_SIZE;
			
			placeFood();
			movementTimer.setDelay(200 - (difficulty * 10));
			movementTimer.start();
			scoreTimer.start();
			gameUI.toggleControlsVisibility(true);
			gameUI.setRestartButtonVisible(false);
			gameUI.updateScoreDisplay(score, highScore);
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

			// Using try-with-resources for proper resource management
			try {
				File soundFile = new File(fileName);
				if (!soundFile.exists()) {
					System.err.println("Sound file not found: " + fileName);
					return;
				}

				try (AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundFile)) {
					Clip clip = AudioSystem.getClip();
					clip.open(audioInput);
					updateVolumeControl(clip);
					clip.start();
				}
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
			gamePanel.repaint();
		}

		// KeyHandler that checks game state first before handling movement keys
		private class KeyHandler extends KeyAdapter {
			@Override
			public void keyPressed(KeyEvent e) {
				requestFocusInWindow();
				
				// Global controls like pause that work regardless of game state
				switch (e.getKeyCode()) {
					case KeyEvent.VK_P, KeyEvent.VK_ENTER -> {
						if (gameStarted) {
							paused = !paused;
							gamePanel.repaint();
						}
					}
					case KeyEvent.VK_M -> {
						toggleMute();
					}
				}
				
				// Movement controls only work when game is running and not paused
				if (running && !paused) {
					switch (e.getKeyCode()) {
						case KeyEvent.VK_LEFT -> {
							if (direction != 'R') direction = 'L';
						}
						case KeyEvent.VK_RIGHT -> {
							if (direction != 'L') direction = 'R';
						}
						case KeyEvent.VK_UP -> {
							if (direction != 'D') direction = 'U';
						}
						case KeyEvent.VK_DOWN -> {
							if (direction != 'U') direction = 'D';
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
		
		// Getter/setter methods for use by GameUI
		void setDifficulty(int difficulty) {
			this.difficulty = difficulty;
			if (movementTimer != null) {
				movementTimer.setDelay(200 - (difficulty * 10));
			}
		}
		
		void setVolume(float volume) {
			this.volume = volume;
			if (backgroundMusic != null && backgroundMusic.isOpen()) {
				updateVolumeControl(backgroundMusic);
			}
		}
		
		void toggleMute() {
			soundMuted = !soundMuted;
			gameUI.updateMuteButtonText(soundMuted);
		}
		
		boolean isMuted() {
			return soundMuted;
		}
		
		void setSnakeColors(Color headColor, Color bodyColor) {
			this.snakeHeadColor = headColor;
			this.snakeBodyColor = bodyColor;
		}
		
		void setWallWrapEnabled(boolean enabled) {
			this.wallWrapEnabled = enabled;
		}
	}
