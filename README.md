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
- change snake head color
- restart button
- mute/unmute toggle

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

<img width="550" alt="Screenshot 2025-04-04 at 11 39 45 PM" src="https://github.com/user-attachments/assets/6d740cc3-c376-4b00-b859-bc07f0c5daac" />

# gameplay

<img width="550" alt="Screenshot 2025-04-04 at 11 40 16 PM" src="https://github.com/user-attachments/assets/6afc65b1-131c-4d64-a730-80755e861df0" />
## = = = = = = = = = =
<img width="550" alt="Screenshot 2025-04-04 at 11 40 51 PM" src="https://github.com/user-attachments/assets/7189e42f-a464-41ae-94a0-d533d24614fd" />
## = = = = = = = = = =

# game over

<img width="550" alt="Screenshot 2025-04-04 at 11 41 50 PM" src="https://github.com/user-attachments/assets/120bed03-0771-4bdd-bb57-1c52124017dc" />

