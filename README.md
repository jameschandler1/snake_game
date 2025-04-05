# Snake Game

A retro-themed, arcade-style Snake game built with Java Swing. Features include animated food, dynamic difficulty, sound effects, score tracking, and a stylish interface.

---

## Features

- Classic Snake gameplay
- Retro UI design with pixel font and themed background
- Volume and mute controls with dB-style slider
- Customizable difficulty (levels 1 to 11)
- Animated food colors
- Score system:
  - +10 per food eaten
  - +1 every second survived
- Persistent high score saved to `highscore.txt`
- Pause and resume functionality (`P` key)
- Start and game over menus with arcade style
- Keyboard and mouse input coordination

---

## Getting Started

### Requirements
- Java JDK 8 or newer
- Any Java IDE (e.g., IntelliJ, Eclipse) or command-line tools

### Directory Structure
```
snake_game/
├── SnakeGame.java
├── background.png
├── eat.wav
├── gameover.wav
├── music.wav
├── highscore.txt
```

### Running the Game
1. Place all files in the same directory.
2. Compile:
   ```sh
   javac snake_game/SnakeGame.java
   ```
3. Run:
   ```sh
   java snake_game.SnakeGame
   ```

---

## Controls

- Arrow keys: Move the snake
- P: Pause / Resume
- ENTER: Start the game from main menu
- Mouse: Adjust volume and difficulty, click buttons

---

## Configuration


- **Difficulty**: Use the slider at the bottom left (1 = slowest, 11 = fastest)
- **Volume**: Use the top-right slider. Mute/unmute via button.
- **High Score**: Stored in `highscore.txt`. Do not delete this file if you want to keep your record.

---

## Notes
- Sound files (`eat.wav`, `gameover.wav`, `music.wav`) must be valid WAV files.
- Ensure `background.png` is 600x600 or larger for best appearance.
- On some systems, audio may require permission to run properly.

---


Enjoy the retro challenge!

# main menu

<img width="750" alt="Screenshot 2025-04-04 at 8 28 35 PM" src="https://github.com/user-attachments/assets/e3c671ae-91b5-4b3a-9a1e-79ca627d68f8" />

# gameplay

<img width="750" alt="Screenshot 2025-04-04 at 8 29 27 PM" src="https://github.com/user-attachments/assets/b9d769d2-7a16-4309-ba47-d0b0403c3109" />

# game over

<img width="750" alt="Screenshot 2025-04-04 at 8 28 58 PM" src="https://github.com/user-attachments/assets/a9259be7-7609-4082-a948-55e526e89524" />


