// UI components organized into separate classes
class GameUI {
	
    final Font retroFont = new Font("Courier New", Font.BOLD, 22);
    JSlider volumeSlider;
    JButton muteButton;
    JSlider difficultySlider;
    JButton restartButton;
    JComboBox<String> colorSelector;
    JCheckBox wallWrapCheckbox;
    
    GameUI(SnakeGame game, int width, int height, float initialVolume, int initialDifficulty) {
        setupAudioControls(game, width, height, initialVolume);
        setupGameControls(game, width, height, initialDifficulty);
    }
    
    private void setupAudioControls(SnakeGame game, int width, int height, float initialVolume) {
        volumeSlider = new JSlider(0, 100, (int) (initialVolume * 100));
        volumeSlider.setBounds(width - 160, 10, 100, 30);
        volumeSlider.setToolTipText("Volume (dB)");
        volumeSlider.setFocusable(false);
        volumeSlider.addChangeListener(e -> {
            game.setVolume(volumeSlider.getValue() / 100f);
        });
        
        muteButton = new JButton("Mute");
        muteButton.setBounds(width - 60, 10, 50, 30);
        muteButton.setFont(new Font("Courier New", Font.PLAIN, 10));
        muteButton.setFocusable(false);
        muteButton.addActionListener(e -> {
            game.toggleMute();
            muteButton.setText(game.isMuted() ? "Unmute" : "Mute");
        });
    }
    
    private void setupGameControls(SnakeGame game, int width, int height, int initialDifficulty) {
        difficultySlider = new JSlider(1, 11, initialDifficulty);
        difficultySlider.setBounds(12, height - 60, 180, 30);
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
        restartButton.setBounds((width - 100) / 2, height - 40, 100, 30);
        restartButton.setFont(new Font("Courier New", Font.PLAIN, 12));
        restartButton.setFocusable(false);
        restartButton.addActionListener(e -> {
            game.resetGame();
        });
        restartButton.setVisible(false);
        
        colorSelector = new JComboBox<>(new String[]{"Green", "Blue", "Purple", "White"});
        colorSelector.setBounds(12, height - 100, 100, 25);
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
        wallWrapCheckbox.setBounds(130, height - 100, 120, 25);
        wallWrapCheckbox.setOpaque(false);
        wallWrapCheckbox.setFocusable(false);
        wallWrapCheckbox.setForeground(Color.WHITE);
        wallWrapCheckbox.addActionListener(e -> {
            game.setWallWrapEnabled(wallWrapCheckbox.isSelected());
        });
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
    
    void addComponentsToPanel(JPanel panel) {
        panel.add(volumeSlider);
        panel.add(muteButton);
        panel.add(difficultySlider);
        panel.add(restartButton);
        panel.add(colorSelector);
        panel.add(wallWrapCheckbox);
    }
}
