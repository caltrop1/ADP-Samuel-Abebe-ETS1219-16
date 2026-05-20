import java.util.ArrayList;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {
    // Game data
    private ArrayList<Player> players = new ArrayList<>();
    private Deck deck;

    private int currentPlayerIndex = 0;
    private int pot = 0;
    private boolean cardsDealt = false;

    private final int startingChips = 100;
    private final int anteAmount = 5;
    private final int raiseAmount = 10;

    private Label titleLabel;
    private Label messageLabel;
    private Label potLabel;
    private Label currentPlayerLabel;
    private Spinner<Integer> playerCountSpinner;

    private HBox cardBox;
    private VBox playerListBox;
    private ArrayList<ToggleButton> cardButtons = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        // Build the main window layout.
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        root.setTop(createTopArea());
        root.setCenter(createCenterArea());
        root.setRight(createRightArea());
        root.setBottom(createBottomArea());

        startNewGame();

        Scene scene = new Scene(root, 850, 500);
        stage.setTitle("Five-Card Draw Poker");
        stage.setScene(scene);
        stage.show();
    }

    private VBox createTopArea() {
        titleLabel = new Label("FIVE-CARD DRAW POKER");
        titleLabel.setFont(Font.font(28));
        titleLabel.setStyle("-fx-font-weight: bold;");

        messageLabel = new Label("Click Deal Cards to begin.");
        potLabel = new Label("Pot: 0");
        currentPlayerLabel = new Label();

        VBox topBox = new VBox(8, titleLabel, messageLabel, potLabel, currentPlayerLabel);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(0, 0, 15, 0));
        return topBox;
    }

    private VBox createCenterArea() {
        cardBox = new HBox(12);
        cardBox.setAlignment(Pos.CENTER);

        Label hintLabel = new Label("Select cards, then click Draw Selected Cards.");

        VBox centerBox = new VBox(15, cardBox, hintLabel);
        centerBox.setAlignment(Pos.CENTER);
        return centerBox;
    }

    private VBox createRightArea() {
        Label playersTitle = new Label("Players");
        playersTitle.setFont(Font.font(18));
        playersTitle.setStyle("-fx-font-weight: bold;");

        playerListBox = new VBox(10);

        VBox rightBox = new VBox(12, playersTitle, playerListBox);
        rightBox.setPadding(new Insets(10));
        rightBox.setPrefWidth(230);
        rightBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1;");
        return rightBox;
    }

    private HBox createBottomArea() {
        playerCountSpinner = new Spinner<>(2, 4, 2);
        playerCountSpinner.setPrefWidth(70);

        Button newGameButton = new Button("New Game");
        Button dealButton = new Button("Deal Cards");
        Button drawButton = new Button("Draw Selected Cards");
        Button raiseButton = new Button("Raise");
        Button checkButton = new Button("Check Hand");
        Button nextButton = new Button("Next Player");

        newGameButton.setOnAction(event -> startNewGame());
        dealButton.setOnAction(event -> dealCards());
        drawButton.setOnAction(event -> drawSelectedCards());
        raiseButton.setOnAction(event -> raise());
        checkButton.setOnAction(event -> checkHand());
        nextButton.setOnAction(event -> nextPlayer());

        Label countLabel = new Label("Players:");

        HBox bottomBox = new HBox(
                10,
                countLabel,
                playerCountSpinner,
                newGameButton,
                dealButton,
                drawButton,
                raiseButton,
                checkButton,
                nextButton);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(15, 0, 0, 0));
        return bottomBox;
    }

    private void startNewGame() {
        // Create simple players with starting chips.
        players.clear();
        int playerCount = playerCountSpinner == null ? 2 : playerCountSpinner.getValue();

        for (int i = 1; i <= playerCount; i++) {
            players.add(new Player("Player " + i, startingChips));
        }

        deck = new Deck();
        deck.shuffle();
        currentPlayerIndex = 0;
        pot = 0;
        cardsDealt = false;

        messageLabel.setText("New game ready. Click Deal Cards.");
        updateScreen();
    }

    private void dealCards() {
        // Start one round: shuffle, collect antes, and deal five cards.
        deck = new Deck();
        deck.shuffle();
        pot = 0;
        currentPlayerIndex = 0;
        cardsDealt = true;

        for (Player player : players) {
            player.clearHand();
            player.setFolded(false);
            player.setHandType("Not checked");

            player.removeChips(anteAmount);
            pot += anteAmount;

            for (int i = 0; i < 5; i++) {
                player.addCard(deck.dealCard());
            }
        }

        messageLabel.setText("Cards dealt. Each player paid a " + anteAmount + " chip ante.");
        updateScreen();
    }

    private void raise() {
        if (!cardsDealt) {
            messageLabel.setText("Deal cards first.");
            return;
        }

        Player player = getCurrentPlayer();

        if (player.getChips() < raiseAmount) {
            messageLabel.setText(player.getName() + " does not have enough chips to raise.");
            return;
        }

        player.removeChips(raiseAmount);
        pot += raiseAmount;
        messageLabel.setText(player.getName() + " raises " + raiseAmount + " chips.");
        updateScreen();
    }

    private void drawSelectedCards() {
        if (!cardsDealt) {
            messageLabel.setText("Deal cards first.");
            return;
        }

        Player player = getCurrentPlayer();
        ArrayList<Card> hand = player.getHand();
        int cardsDrawn = 0;

        for (int i = cardButtons.size() - 1; i >= 0; i--) {
            if (cardButtons.get(i).isSelected()) {
                hand.remove(i);
                cardsDrawn++;
            }
        }

        for (int i = 0; i < cardsDrawn; i++) {
            hand.add(deck.dealCard());
        }

        player.setHandType("Not checked");
        messageLabel.setText(player.getName() + " drew " + cardsDrawn + " card(s).");
        updateScreen();
    }

    private void checkHand() {
        if (!cardsDealt) {
            messageLabel.setText("Deal cards first.");
            return;
        }

        Player player = getCurrentPlayer();
        String handType = HandEvaluator.getHandType(player.getHand());
        player.setHandType(handType);
        messageLabel.setText(player.getName() + " has: " + handType);
        updateScreen();
    }

    private void nextPlayer() {
        if (!cardsDealt) {
            messageLabel.setText("Deal cards first.");
            return;
        }

        if (currentPlayerIndex < players.size() - 1) {
            currentPlayerIndex++;
            messageLabel.setText(getCurrentPlayer().getName() + "'s turn.");
        } else {
            finishRound();
        }

        updateScreen();
    }

    private void finishRound() {
        // Pick the best hand. Split pots are intentionally not used.
        Player winner = players.get(0);
        int bestScore = HandEvaluator.getHandStrength(winner.getHand());

        for (Player player : players) {
            int score = HandEvaluator.getHandStrength(player.getHand());
            String handType = HandEvaluator.getHandType(player.getHand());
            player.setHandType(handType);

            if (score > bestScore) {
                winner = player;
                bestScore = score;
            }
        }

        winner.addChips(pot);
        messageLabel.setText(winner.getName() + " wins the pot with " + winner.getHandType() + "!");
        showWinnerMessage(winner);

        pot = 0;
        cardsDealt = false;
    }

    private void showWinnerMessage(Player winner) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Round Complete");
        alert.setHeaderText(winner.getName() + " wins!");
        alert.setContentText("Winning hand: " + winner.getHandType() + "\nPot awarded: " + pot + " chips");
        alert.showAndWait();
    }

    private Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    private void updateScreen() {
        updateCards();
        updatePlayerList();
        potLabel.setText("Pot: " + pot);

        if (!players.isEmpty()) {
            currentPlayerLabel.setText("Current Player: " + getCurrentPlayer().getName());
        }
    }

    private void updateCards() {
        cardBox.getChildren().clear();
        cardButtons.clear();

        if (!cardsDealt) {
            Label waitingLabel = new Label("[ no cards dealt ]");
            waitingLabel.setFont(Font.font(20));
            cardBox.getChildren().add(waitingLabel);
            return;
        }

        for (Card card : getCurrentPlayer().getHand()) {
            ToggleButton cardButton = new ToggleButton(card.toString());
            cardButton.setFont(Font.font(20));
            cardButton.setMinSize(90, 70);
            cardButton.setStyle("-fx-background-color: white; -fx-border-color: black;");
            cardButtons.add(cardButton);
            cardBox.getChildren().add(cardButton);
        }
    }

    private void updatePlayerList() {
        playerListBox.getChildren().clear();

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            Label label = new Label(
                    player.getName()
                            + "\nChips: " + player.getChips()
                            + "\nHand: " + player.getHandType());

            if (i == currentPlayerIndex && cardsDealt) {
                label.setStyle("-fx-font-weight: bold; -fx-background-color: lightyellow;");
            }

            label.setPadding(new Insets(6));
            playerListBox.getChildren().add(label);
        }
    }
}
