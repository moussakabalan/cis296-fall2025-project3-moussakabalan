package monopoly;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameController {
    @FXML private GridPane boardGrid;
    @FXML private VBox playerInfoBox;
    @FXML private TextArea messageArea;
    @FXML private Button rollDiceButton;
    @FXML private Button buyButton;
    @FXML private Button declineButton;
    @FXML private Button saveButton;
    @FXML private Button buildButton;
    @FXML private Button exitButton;
    @FXML private Label dice1Label;
    @FXML private Label dice2Label;
    
    private GameEngine engine;
    private Map<Integer, StackPane> spacePanes = new HashMap<>();
    
    public void setGameEngine(GameEngine engine) {
        this.engine = engine;
        initializeBoard();
        updateDisplay();
    }
    
    private void initializeBoard() {
        boardGrid.getChildren().clear();
        spacePanes.clear();
        
        Rules rules = engine.getRules();
        int size = rules.spaces.size();
        int side = (int) Math.ceil(Math.sqrt(size));
        
        for (int i = 0; i < size; i++) {
            Rules.BoardSpace space = rules.spaces.get(i);
            StackPane spacePane = createSpacePane(space, i);
            spacePanes.put(i, spacePane);
            
            int row = i / side;
            int col = i % side;
            boardGrid.add(spacePane, col, row);
        }
    }
    
    private StackPane createSpacePane(Rules.BoardSpace space, int index) {
        StackPane pane = new StackPane();
        pane.setPrefSize(140, 100);
        pane.setStyle("-fx-border-color: black; -fx-border-width: 2;");
        
        VBox content = new VBox(5);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(5));
        
        Label nameLabel = new Label(space.name);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(130);
        nameLabel.setAlignment(Pos.CENTER);
        
        String bgColor = getSpaceColor(space);
        pane.setStyle("-fx-border-color: black; -fx-border-width: 2; -fx-background-color: " + bgColor + ";");
        
        content.getChildren().add(nameLabel);
        
        if (space.type == Rules.SpaceType.PROP && space.price != null) {
            Label priceLabel = new Label("$" + space.price);
            priceLabel.setFont(Font.font("Arial", 10));
            content.getChildren().add(priceLabel);
            
            Property prop = engine.getBoard().getProperty(index);
            if (prop != null && prop.houses > 0) {
                Label houseLabel = new Label("Houses: " + prop.houses);
                houseLabel.setFont(Font.font("Arial", FontWeight.BOLD, 9));
                content.getChildren().add(houseLabel);
            }
            if (prop != null && prop.hasHotel) {
                Label hotelLabel = new Label("HOTEL");
                hotelLabel.setFont(Font.font("Arial", FontWeight.BOLD, 10));
                hotelLabel.setTextFill(Color.RED);
                content.getChildren().add(hotelLabel);
            }
        }
        
        HBox playerMarkers = new HBox(3);
        playerMarkers.setAlignment(Pos.CENTER);
        pane.setUserData(playerMarkers);
        content.getChildren().add(playerMarkers);
        
        pane.getChildren().add(content);
        return pane;
    }
    
    private String getSpaceColor(Rules.BoardSpace space) {
        if (space.type == Rules.SpaceType.GO) return "#90EE90";
        if (space.type == Rules.SpaceType.JAIL) return "#FFB6C1";
        if (space.type == Rules.SpaceType.CHANCE) return "#FFA500";
        if (space.type == Rules.SpaceType.COMMUNITY_CHEST) return "#87CEEB";
        if (space.type == Rules.SpaceType.FREE) return "#F0E68C";
        if (space.colorSet != null) {
            switch (space.colorSet) {
                case "Brown": return "#8B4513";
                case "LightBlue": return "#ADD8E6";
                case "Pink": return "#FFC0CB";
                case "Orange": return "#FFA500";
                case "Red": return "#FF6347";
                case "Yellow": return "#FFFF00";
                case "Green": return "#90EE90";
                case "Blue": return "#4169E1";
            }
        }
        return "#FFFFFF";
    }
    
    private void updateDisplay() {
        updatePlayerInfo();
        updatePlayerPositions();
        updateButtons();
        updateMessage();
        updateDice();
        updateBoardProperties();
    }
    
    private void updatePlayerInfo() {
        playerInfoBox.getChildren().clear();
        
        Label title = new Label("PLAYERS");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        playerInfoBox.getChildren().add(title);
        
        for (Player player : engine.getPlayers()) {
            VBox playerBox = new VBox(5);
            playerBox.setPadding(new Insets(10));
            playerBox.setStyle("-fx-border-color: " + (player == engine.getCurrentPlayer() ? "green" : "gray") + 
                              "; -fx-border-width: 2; -fx-background-color: #f0f0f0;");
            
            Label nameLabel = new Label(player.name + (player == engine.getCurrentPlayer() ? " (Current)" : ""));
            nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            
            Label cashLabel = new Label("Cash: $" + player.cash);
            Label posLabel = new Label("Position: " + engine.getBoard().getSpaceName(player.position));
            
            playerBox.getChildren().addAll(nameLabel, cashLabel, posLabel);
            
            if (player.inJail) {
                Label jailLabel = new Label("IN JAIL");
                jailLabel.setTextFill(Color.RED);
                jailLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                playerBox.getChildren().add(jailLabel);
            }
            
            if (player.isBankrupt) {
                Label bankruptLabel = new Label("BANKRUPT");
                bankruptLabel.setTextFill(Color.DARKRED);
                bankruptLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                playerBox.getChildren().add(bankruptLabel);
            }
            
            Label propsLabel = new Label("Properties: " + countPlayerProperties(player));
            playerBox.getChildren().add(propsLabel);
            
            playerInfoBox.getChildren().add(playerBox);
        }
    }
    
    private int countPlayerProperties(Player player) {
        int count = 0;
        for (Property prop : engine.getBoard().properties.values()) {
            if (prop.owner == player) count++;
        }
        return count;
    }
    
    private void updatePlayerPositions() {
        for (StackPane pane : spacePanes.values()) {
            HBox markers = (HBox) pane.getUserData();
            if (markers != null) markers.getChildren().clear();
        }
        
        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW};
        List<Player> players = engine.getPlayers();
        
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            StackPane pane = spacePanes.get(player.position);
            if (pane != null) {
                HBox markers = (HBox) pane.getUserData();
                if (markers != null) {
                    Circle marker = new Circle(6, colors[i % colors.length]);
                    marker.setStroke(Color.BLACK);
                    markers.getChildren().add(marker);
                }
            }
        }
    }
    
    private void updateBoardProperties() {
        for (int i = 0; i < engine.getRules().spaces.size(); i++) {
            Rules.BoardSpace space = engine.getRules().spaces.get(i);
            if (space.type == Rules.SpaceType.PROP) {
                StackPane pane = spacePanes.get(i);
                if (pane != null) {
                    pane.getChildren().clear();
                    VBox content = (VBox) createSpacePane(space, i).getChildren().get(0);
                    HBox markers = (HBox) pane.getUserData();
                    content.getChildren().add(markers);
                    pane.getChildren().add(content);
                }
            }
        }
    }
    
    private void updateButtons() {
        Player current = engine.getCurrentPlayer();
        boolean canPlay = !current.isBankrupt && !engine.isGameOver();
        
        rollDiceButton.setDisable(!canPlay || engine.isWaitingForPropertyDecision());
        buyButton.setDisable(!engine.isWaitingForPropertyDecision());
        declineButton.setDisable(!engine.isWaitingForPropertyDecision());
        buildButton.setDisable(current.isBankrupt);
    }
    
    private void updateMessage() {
        String msg = engine.getLastMessage();
        if (!msg.isEmpty()) {
            messageArea.appendText("\n" + msg);
        }
        
        if (engine.isGameOver()) {
            Player winner = engine.getWinner();
            if (winner != null) {
                messageArea.appendText("\n\n*** " + winner.name + " WINS! ***");
            }
        }
    }
    
    private void updateDice() {
        int[] dice = engine.getLastDiceRoll();
        dice1Label.setText(String.valueOf(dice[0]));
        dice2Label.setText(String.valueOf(dice[1]));
    }
    
    @FXML
    private void onRollDice() {
        engine.rollDice();
        updateDisplay();
    }
    
    @FXML
    private void onBuy() {
        engine.buyProperty();
        updateDisplay();
    }
    
    @FXML
    private void onDecline() {
        engine.declineProperty();
        updateDisplay();
    }
    
    @FXML
    private void onSave() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Save Game");
        dialog.setHeaderText("Enter save file name (without .txt)");
        dialog.setContentText("File name:");
        
        dialog.showAndWait().ifPresent(fileName -> {
            if (engine.saveGame(fileName)) {
                showAlert("Game saved successfully!", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Failed to save game!", Alert.AlertType.ERROR);
            }
        });
    }
    
    @FXML
    private void onBuild() {
        List<Property> ownedProps = getOwnedProperties(engine.getCurrentPlayer());
        
        if (ownedProps.isEmpty()) {
            showAlert("You don't own any properties!", Alert.AlertType.WARNING);
            return;
        }
        
        ChoiceDialog<String> dialog = new ChoiceDialog<>();
        dialog.setTitle("Build");
        dialog.setHeaderText("Select a property to build on:");
        
        for (Property prop : ownedProps) {
            String info = prop.name + " (Houses: " + prop.houses + 
                         (prop.hasHotel ? ", Hotel" : "") + ")";
            dialog.getItems().add(info);
        }
        
        dialog.showAndWait().ifPresent(selection -> {
            int index = dialog.getItems().indexOf(selection);
            Property prop = ownedProps.get(index);
            
            if (engine.canBuildHotel(prop)) {
                if (confirmBuild(prop, true)) {
                    engine.buildHotel(prop);
                    messageArea.appendText("\nBuilt hotel on " + prop.name);
                    updateDisplay();
                }
            } else if (engine.canBuildHouse(prop)) {
                if (confirmBuild(prop, false)) {
                    engine.buildHouse(prop);
                    messageArea.appendText("\nBuilt house on " + prop.name);
                    updateDisplay();
                }
            } else {
                showAlert("Cannot build on this property. Need to own all properties in color set.", Alert.AlertType.WARNING);
            }
        });
    }
    
    private boolean confirmBuild(Property prop, boolean isHotel) {
        String type = isHotel ? "hotel" : "house";
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Build");
        alert.setHeaderText("Build " + type + " on " + prop.name + "?");
        alert.setContentText("Cost: $" + prop.housePrice);
        return alert.showAndWait().get() == ButtonType.OK;
    }
    
    private List<Property> getOwnedProperties(Player player) {
        List<Property> owned = new java.util.ArrayList<>();
        for (Property prop : engine.getBoard().properties.values()) {
            if (prop.owner == player) {
                owned.add(prop);
            }
        }
        return owned;
    }
    
    @FXML
    private void onExit() {
        try {
            Main.showStartMenu();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type.toString());
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}


