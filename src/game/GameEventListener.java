package game;

public interface GameEventListener {
    void onWin();

    void onGameOver();

    void onExitToMenu();
}
