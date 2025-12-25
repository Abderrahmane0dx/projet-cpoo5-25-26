package fr.uparis.liquidwar.view;

import fr.uparis.liquidwar.model.Board;
import fr.uparis.liquidwar.model.Gradient;
import fr.uparis.liquidwar.model.Particle;
import fr.uparis.liquidwar.model.Position;
import fr.uparis.liquidwar.model.Team;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Map;

/**
 * JavaFX Canvas that renders the Liquid War game.
 * Displays particles, obstacles, and team cursors.
 */
public class GamePanel extends Canvas {
    private static final int CELL_SIZE = 4; // Size of each pixel/cell
    private static final Color BACKGROUND_COLOR = Color.BLACK;
    private static final Color OBSTACLE_COLOR = Color.DARKGRAY;
    private static final Color CURSOR_COLOR = Color.YELLOW;
    private static final int CURSOR_SIZE = 8;
    
    private Board board;
    private Map<Team, Gradient> gradients;
    private boolean showGradient = false;
    private Team gradientTeam = null;
    
    /**
     * Creates a game panel for a board.
     * 
     * @param board the game board
     */
    public GamePanel(Board board) {
        super(board.getWidth() * CELL_SIZE, board.getHeight() * CELL_SIZE);
        this.board = board;
        
        // Enable mouse events
        setFocusTraversable(true);
        
        render();
    }
    
    /**
     * Sets the board to display.
     * 
     * @param board new board
     */
    public void setBoard(Board board) {
        this.board = board;
        setWidth(board.getWidth() * CELL_SIZE);
        setHeight(board.getHeight() * CELL_SIZE);
    }
    
    /**
     * Sets the gradients for visualization.
     * 
     * @param gradients map of team gradients
     */
    public void setGradients(Map<Team, Gradient> gradients) {
        this.gradients = gradients;
    }
    
    /**
     * Toggles gradient visualization.
     * 
     * @param show true to show gradient
     * @param team team whose gradient to show (null to hide)
     */
    public void setShowGradient(boolean show, Team team) {
        this.showGradient = show;
        this.gradientTeam = team;
    }
    
    /**
     * Renders the entire game state.
     */
    public void render() {
        GraphicsContext gc = getGraphicsContext2D();
        
        // Clear background
        gc.setFill(BACKGROUND_COLOR);
        gc.fillRect(0, 0, getWidth(), getHeight());
        
        // Draw gradient if enabled
        if (showGradient && gradientTeam != null && gradients != null) {
            drawGradient(gc, gradients.get(gradientTeam));
        }
        
        // Draw obstacles
        drawObstacles(gc);
        
        // Draw particles
        drawParticles(gc);
        
        // Draw cursors
        if (gradients != null) {
            for (Team team : gradients.keySet()) {
                drawCursor(gc, team);
            }
        }
    }
    
    /**
     * Draws obstacles on the board.
     */
    private void drawObstacles(GraphicsContext gc) {
        gc.setFill(OBSTACLE_COLOR);
        
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                if (board.isObstacle(x, y)) {
                    gc.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }
    }
    
    /**
     * Draws all particles with colors based on energy.
     */
    private void drawParticles(GraphicsContext gc) {
        for (Particle particle : board.getAllParticles()) {
            Position pos = particle.getPosition();
            
            // Get color based on team and energy
            Color color = particle.getTeam().getParticleColor(particle.getEnergyRatio());
            gc.setFill(color);
            
            gc.fillRect(pos.x() * CELL_SIZE, pos.y() * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }
    }
    
    /**
     * Draws a team's cursor.
     */
    private void drawCursor(GraphicsContext gc, Team team) {
        Position cursor = team.getTargetPosition();
        
        // Draw crosshair
        gc.setStroke(CURSOR_COLOR);
        gc.setLineWidth(2);
        
        int x = cursor.x() * CELL_SIZE;
        int y = cursor.y() * CELL_SIZE;
        
        // Horizontal line
        gc.strokeLine(x - CURSOR_SIZE, y, x + CURSOR_SIZE, y);
        // Vertical line
        gc.strokeLine(x, y - CURSOR_SIZE, x, y + CURSOR_SIZE);
        
        // Draw circle
        gc.strokeOval(x - CURSOR_SIZE/2, y - CURSOR_SIZE/2, CURSOR_SIZE, CURSOR_SIZE);
    }
    
    /**
     * Draws gradient visualization (for debugging).
     */
    private void drawGradient(GraphicsContext gc, Gradient gradient) {
        if (gradient == null) return;
        
        // Find max distance for normalization
        int maxDist = 0;
        for (int y = 0; y < gradient.getHeight(); y++) {
            for (int x = 0; x < gradient.getWidth(); x++) {
                int dist = gradient.getDistance(x, y);
                if (dist < Gradient.INFINITE && dist > maxDist) {
                    maxDist = dist;
                }
            }
        }
        
        // Draw gradient as heatmap
        for (int y = 0; y < gradient.getHeight(); y++) {
            for (int x = 0; x < gradient.getWidth(); x++) {
                int dist = gradient.getDistance(x, y);
                
                if (dist < Gradient.INFINITE) {
                    // Normalize to 0.0-1.0
                    double ratio = maxDist > 0 ? (double) dist / maxDist : 0;
                    
                    // Blue (close) to Red (far)
                    Color color = Color.color(ratio, 0, 1.0 - ratio, 0.3);
                    gc.setFill(color);
                    gc.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }
    }
    
    /**
     * Converts screen coordinates to board position.
     * 
     * @param screenX screen X coordinate
     * @param screenY screen Y coordinate
     * @return board position
     */
    public Position screenToBoard(double screenX, double screenY) {
        int x = (int) (screenX / CELL_SIZE);
        int y = (int) (screenY / CELL_SIZE);
        return new Position(x, y);
    }
    
    /**
     * Converts board position to screen coordinates.
     * 
     * @param pos board position
     * @return array [screenX, screenY]
     */
    public double[] boardToScreen(Position pos) {
        return new double[] {
            pos.x() * CELL_SIZE,
            pos.y() * CELL_SIZE
        };
    }
}