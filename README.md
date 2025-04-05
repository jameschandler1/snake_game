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
- mute/unmute toggle sfx

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

# menus

<img width="650" alt="pause menu" src="https://github.com/user-attachments/assets/3f1af0a4-c8e6-4fdd-a002-f5699740ceaf" />


<img width="650" alt="main menu" src="https://github.com/user-attachments/assets/3af8b8ae-b24a-4312-bec4-8ec5cacc8348" />


 
# gameplay


<img width="650" alt="gameplay 1" src="https://github.com/user-attachments/assets/c520d98a-539a-4eee-b4cb-129d6b02c0ea" />


# game over

<img width="650" alt="game-over" src="https://github.com/user-attachments/assets/e900c3b7-411b-4313-8d43-242e2f4d1a6c" />


