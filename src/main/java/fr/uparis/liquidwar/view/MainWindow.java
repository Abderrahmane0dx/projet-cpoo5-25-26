package fr.uparis.liquidwar.view;

import fr.uparis.liquidwar.algorithm.AIController;
import fr.uparis.liquidwar.algorithm.GradientCalculator;
import fr.uparis.liquidwar.algorithm.ParticleMovement;
import fr.uparis.liquidwar.model.Board;
import fr.uparis.liquidwar.model.Gradient;
import fr.uparis.liquidwar.model.Particle;
import fr.uparis.liquidwar.model.Position;
import fr.uparis.liquidwar.model.Team;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main game window with controls and display.
 */
public class MainWindow {
    private static final int BOARD_WIDTH = 200;
    private static final int BOARD_HEIGHT = 150;
    private static final int PARTICLE_COUNT_PER_TEAM = 500;
    
    private Stage stage;
    private GamePanel gamePanel;
    private Board board;
    private List<Team> teams;
    private Map<Team, Gradient> gradients;
    
    private GradientCalculator gradientCalculator;
    private ParticleMovement particleMovement;
    private AIController aiController;
    
    private AnimationTimer gameLoop;
    private boolean running = false;
    
    private Label statusLabel;
    private Label fpsLabel;
    private long lastTime = 0;
    private int frameCount = 0;
    
    private AIController.Difficulty aiDifficulty = AIController.Difficulty.MEDIUM;
    
    /**
     * Creates the main game window with default difficulty.
     * 
     * @param stage JavaFX stage
     */
    public MainWindow(Stage stage) {
        this(stage, AIController.Difficulty.MEDIUM);
    }
    
    /**
     * Creates the main game window with specified AI difficulty.
     * 
     * @param stage JavaFX stage
     * @param difficulty AI difficulty level
     */
    public MainWindow(Stage stage, AIController.Difficulty difficulty) {
        this.stage = stage;
        this.aiDifficulty = difficulty;
        initializeGame();
        setupUI();
    }
    
    /**
     * Initializes the game state.
     */
    private void initializeGame() {
        // Create board
        board = new Board(BOARD_WIDTH, BOARD_HEIGHT);
        board.createBorderWalls();
        board.createRandomObstacles(0.05); // 5% obstacles
        
        // Create teams
        Team team1 = new Team(1, Color.RED, new Position(50, 75));
        Team team2 = new Team(2, Color.BLUE, new Position(150, 75));
        teams = List.of(team1, team2);
        
        // Create particles
        createParticles(team1, 30, 50, 60, 100);
        createParticles(team2, 140, 160, 60, 100);
        
        // Initialize algorithms
        gradientCalculator = new GradientCalculator(board);
        particleMovement = new ParticleMovement(board);
        gradients = new HashMap<>();
        
        // Initialize AI for team 2 (Blue team)
        aiController = new AIController(board, team2, team1, aiDifficulty);
        
        // Calculate initial gradients
        updateGradients();
    }
    
    /**
     * Creates particles for a team in a rectangular area.
     */
    private void createParticles(Team team, int x1, int x2, int y1, int y2) {
        int count = 0;
        while (count < PARTICLE_COUNT_PER_TEAM) {
            int x = x1 + (int)(Math.random() * (x2 - x1));
            int y = y1 + (int)(Math.random() * (y2 - y1));
            
            if (board.isFree(x, y)) {
                Particle p = new Particle(new Position(x, y), team);
                board.setParticle(p, x, y);
                team.addParticle(p);
                count++;
            }
        }
    }
    
    /**
     * Sets up the user interface.
     */
    private void setupUI() {
        BorderPane root = new BorderPane();
        
        // Create game panel
        gamePanel = new GamePanel(board);
        gamePanel.setGradients(gradients);
        
        // Mouse handler for moving cursor
        gamePanel.setOnMouseMoved(event -> {
            if (teams.size() > 0) {
                Position pos = gamePanel.screenToBoard(event.getX(), event.getY());
                if (board.isInBounds(pos)) {
                    teams.get(0).setTargetPosition(pos); // Player 1 controls with mouse
                }
            }
        });
        
        root.setCenter(gamePanel);
        
        // Control panel
        VBox controlPanel = createControlPanel();
        root.setRight(controlPanel);
        
        // Status bar
        HBox statusBar = createStatusBar();
        root.setBottom(statusBar);
        
        // Create scene
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Liquid War - CPOO Project");
        stage.setResizable(false);
        stage.show();
        
        // Setup game loop
        setupGameLoop();
    }
    
    /**
     * Creates the control panel.
     */
    private VBox createControlPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(200);
        
        Label title = new Label("Liquid War");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Button startButton = new Button("Start");
        startButton.setPrefWidth(180);
        startButton.setOnAction(e -> start());
        
        Button pauseButton = new Button("Pause");
        pauseButton.setPrefWidth(180);
        pauseButton.setOnAction(e -> pause());
        
        Button resetButton = new Button("Reset");
        resetButton.setPrefWidth(180);
        resetButton.setOnAction(e -> reset());
        
        // Menu button
        Button menuButton = new Button("⬅ Menu");
        menuButton.setPrefWidth(180);
        menuButton.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
        menuButton.setOnAction(e -> returnToMenu());
        
        // AI Difficulty selector
        Label aiLabel = new Label("AI Difficulty:");
        aiLabel.setStyle("-fx-font-weight: bold;");
        
        ComboBox<String> difficultyBox = new ComboBox<>();
        difficultyBox.getItems().addAll("Easy", "Medium", "Hard");
        difficultyBox.setValue("Medium");
        difficultyBox.setPrefWidth(180);
        difficultyBox.setOnAction(e -> {
            String selected = difficultyBox.getValue();
            AIController.Difficulty diff = switch (selected) {
                case "Easy" -> AIController.Difficulty.EASY;
                case "Hard" -> AIController.Difficulty.HARD;
                default -> AIController.Difficulty.MEDIUM;
            };
            aiController.setDifficulty(diff);
        });
        
        // AI Strategy selector
        Label strategyLabel = new Label("AI Strategy:");
        strategyLabel.setStyle("-fx-font-weight: bold;");
        
        ComboBox<String> strategyBox = new ComboBox<>();
        strategyBox.getItems().addAll("Balanced", "Aggressive", "Defensive", "Hunt");
        strategyBox.setValue("Balanced");
        strategyBox.setPrefWidth(180);
        strategyBox.setOnAction(e -> {
            String selected = strategyBox.getValue();
            AIController.Strategy strat = switch (selected) {
                case "Aggressive" -> AIController.Strategy.AGGRESSIVE;
                case "Defensive" -> AIController.Strategy.DEFENSIVE;
                case "Hunt" -> AIController.Strategy.HUNT;
                default -> AIController.Strategy.BALANCED;
            };
            aiController.setStrategy(strat);
        });
        
        statusLabel = new Label("Ready");
        statusLabel.setWrapText(true);
        
        // Team info
        Label team1Info = new Label("Red Team: " + teams.get(0).getParticleCount());
        team1Info.setTextFill(Color.RED);
        team1Info.setStyle("-fx-font-weight: bold;");
        
        Label team2Info = new Label("Blue Team (AI): " + teams.get(1).getParticleCount());
        team2Info.setTextFill(Color.BLUE);
        team2Info.setStyle("-fx-font-weight: bold;");
        
        panel.getChildren().addAll(
            title,
            startButton,
            pauseButton,
            resetButton,
            menuButton,
            new Label(""),
            aiLabel,
            difficultyBox,
            strategyLabel,
            strategyBox,
            new Label(""),
            team1Info,
            team2Info,
            new Label(""),
            statusLabel
        );
        
        return panel;
    }
    
    /**
     * Creates the status bar.
     */
    private HBox createStatusBar() {
        HBox statusBar = new HBox(20);
        statusBar.setPadding(new Insets(5));
        statusBar.setStyle("-fx-background-color: #333; -fx-text-fill: white;");
        
        fpsLabel = new Label("FPS: 0");
        fpsLabel.setTextFill(Color.WHITE);
        
        Label controlsLabel = new Label("Controls: Move mouse to control Red team | Blue team is AI controlled");
        controlsLabel.setTextFill(Color.WHITE);
        
        statusBar.getChildren().addAll(fpsLabel, controlsLabel);
        
        return statusBar;
    }
    
    /**
     * Sets up the game loop.
     */
    private void setupGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (running) {
                    update();
                    render();
                    updateFPS(now);
                }
            }
        };
    }
    
    /**
     * Updates the game state.
     */
    private void update() {
        // Update AI for team 2
        aiController.update();
        aiController.adaptStrategy(); // Adapt strategy based on game state
        
        // Update gradients
        updateGradients();
        
        // Move all particles
        for (Particle particle : board.getAllParticles()) {
            Gradient gradient = gradients.get(particle.getTeam());
            if (gradient != null) {
                particleMovement.moveParticle(particle, gradient);
            }
        }
        
        // Check win condition
        checkWinCondition();
    }
    
    /**
     * Updates gradients for all teams.
     */
    private void updateGradients() {
        for (Team team : teams) {
            Gradient gradient = gradientCalculator.calculate(team);
            gradients.put(team, gradient);
        }
    }
    
    /**
     * Renders the game.
     */
    private void render() {
        gamePanel.render();
    }
    
    /**
     * Updates FPS counter.
     */
    private void updateFPS(long now) {
        if (lastTime == 0) {
            lastTime = now;
            return;
        }
        
        frameCount++;
        long elapsed = now - lastTime;
        
        if (elapsed >= 1_000_000_000) { // 1 second
            int fps = (int) ((frameCount * 1_000_000_000.0) / elapsed);
            Platform.runLater(() -> fpsLabel.setText("FPS: " + fps));
            frameCount = 0;
            lastTime = now;
        }
    }
    
    /**
     * Checks if a team has won.
     */
    private void checkWinCondition() {
        int team1Count = teams.get(0).getParticleCount();
        int team2Count = teams.get(1).getParticleCount();
        
        if (team1Count == 0) {
            pause();
            Platform.runLater(() -> statusLabel.setText("Blue Team Wins!"));
        } else if (team2Count == 0) {
            pause();
            Platform.runLater(() -> statusLabel.setText("Red Team Wins!"));
        }
    }
    
    /**
     * Starts the game.
     */
    public void start() {
        running = true;
        gameLoop.start();
        statusLabel.setText("Running...");
    }
    
    /**
     * Pauses the game.
     */
    public void pause() {
        running = false;
        statusLabel.setText("Paused");
    }
    
    /**
     * Resets the game.
     */
    public void reset() {
        pause();
        initializeGame();
        gamePanel.setBoard(board);
        gamePanel.setGradients(gradients);
        statusLabel.setText("Ready");
    }
    
    /**
     * Returns to the main menu.
     */
    public void returnToMenu() {
        pause();
        gameLoop.stop();
        new MenuWindow(stage);
    }
}