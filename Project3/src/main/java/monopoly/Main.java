package monopoly;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        showStartMenu();
    }

    public static void showStartMenu() throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("start-menu.fxml"));
        Scene scene = new Scene(loader.load(), 600, 400);
        primaryStage.setTitle("Monopoly Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void showGameScreen(GameEngine engine) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("game-screen.fxml"));
        Scene scene = new Scene(loader.load(), 1200, 800);
        GameController controller = loader.getController();
        controller.setGameEngine(engine);
        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}


