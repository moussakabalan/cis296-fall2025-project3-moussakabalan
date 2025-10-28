package monopoly;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.List;

public class StartMenuController {
    @FXML private VBox playerNamesBox;
    @FXML private TextField player1Field;
    @FXML private TextField player2Field;
    @FXML private TextField player3Field;
    @FXML private TextField player4Field;
    @FXML private Button startGameButton;
    @FXML private Button loadGameButton;
    
    @FXML
    private void onStartGame() {
        List<String> playerNames = new ArrayList<>();

        if (!player1Field.getText().trim().isEmpty()) {
            playerNames.add(player1Field.getText().trim());
        }
        if (!player2Field.getText().trim().isEmpty()) {
            playerNames.add(player2Field.getText().trim());
        }
        if (!player3Field.getText().trim().isEmpty()) {
            playerNames.add(player3Field.getText().trim());
        }
        if (!player4Field.getText().trim().isEmpty()) {
            playerNames.add(player4Field.getText().trim());
        }

        if (playerNames.size() < 2) {
            showAlert("Need at least 2 players!");
            return;
        }
        
        GameEngine engine = new GameEngine();
        engine.newGame(playerNames);
        
        try {
            Main.showGameScreen(engine);
        } catch (Exception e) {
            showAlert("Error starting game: " + e.getMessage());
        }
    }

    @FXML
    private void onLoadGame() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Load Game");
        dialog.setHeaderText("Enter save file name (without .txt)");
        dialog.setContentText("File name:");
        
        dialog.showAndWait().ifPresent(fileName -> {
            GameEngine engine = new GameEngine();
            if (engine.loadGame(fileName)) {
                try {
                    Main.showGameScreen(engine);
                } catch (Exception e) {
                    showAlert("Error loading game: " + e.getMessage());
                }
            } else {
                showAlert("Failed to load game file: " + fileName);
            }
        });
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}


