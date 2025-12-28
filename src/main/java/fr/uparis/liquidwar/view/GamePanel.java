package fr.uparis.liquidwar.view;

import fr.uparis.liquidwar.model.Board;
import fr.uparis.liquidwar.model.Gradient;
import fr.uparis.liquidwar.model.Particle;
import fr.uparis.liquidwar.model.Position;
import fr.uparis.liquidwar.model.Team;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;

import java.util.Map;

/**
 * Modern game panel with organic obstacles.
 */
public class GamePanel extends Canvas {
    private static final int CELL_SIZE = 4;
    private static final Color BACKGROUND_COLOR = Color.rgb(5, 5, 10);
    
    private Board board;
    private Map<Team, Gradient> gradients;
    
    public GamePanel(Board board) {
        super(board.getWidth() * CELL_SIZE, board.getHeight() * CELL_SIZE);
        this.board = board;
        setFocusTraversable(true);
        render();
    }
    
    public void setBoard(Board board) {
        this.board = board;
        setWidth(board.getWidth() * CELL_SIZE);
        setHeight(board.getHeight() * CELL_SIZE);
    }
    
    public void setGradients(Map<Team, Gradient> gradients) {
        this.gradients = gradients;
    }
    
    public void render() {
        GraphicsContext gc = getGraphicsContext2D();
        
        // Very dark background
        gc.setFill(BACKGROUND_COLOR);
        gc.fillRect(0, 0, getWidth(), getHeight());
        
        // Draw obstacles with organic shapes
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
    
    private void drawObstacles(GraphicsContext gc) {
        // Draw obstacles with smooth, organic shapes
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                if (board.isObstacle(x, y)) {
                    drawOrganicObstacle(gc, x, y);
                }
            }
        }
    }
    
    private void drawOrganicObstacle(GraphicsContext gc, int x, int y) {
        double px = x * CELL_SIZE;
        double py = y * CELL_SIZE;
        
        // Soft gray with slight blue tint
        Stop[] stops = new Stop[] {
            new Stop(0, Color.rgb(60, 65, 75)),
            new Stop(1, Color.rgb(35, 40, 50))
        };
        
        RadialGradient gradient = new RadialGradient(
            0, 0, 0.4, 0.4, 0.6, true,
            CycleMethod.NO_CYCLE, stops
        );
        
        gc.setFill(gradient);
        // Draw rounded shapes instead of squares
        gc.fillOval(px, py, CELL_SIZE, CELL_SIZE);
        
        // Subtle cyan edge
        gc.setStroke(Color.rgb(0, 140, 160, 0.3));
        gc.setLineWidth(0.5);
        gc.strokeOval(px, py, CELL_SIZE, CELL_SIZE);
    }
    
    private void drawParticles(GraphicsContext gc) {
        for (Particle particle : board.getAllParticles()) {
            drawParticle(gc, particle);
        }
    }
    
    private void drawParticle(GraphicsContext gc, Particle particle) {
        Position pos = particle.getPosition();
        double px = pos.x() * CELL_SIZE;
        double py = pos.y() * CELL_SIZE;
        
        Color baseColor = particle.getTeam().getBaseColor();
        double energyRatio = particle.getEnergyRatio();
        
        // Soft glow
        Color glowColor = baseColor.deriveColor(0, 1.0, 1.0 + energyRatio * 0.3, 0.4);
        gc.setFill(glowColor);
        gc.fillOval(px - 1.5, py - 1.5, CELL_SIZE + 3, CELL_SIZE + 3);
        
        // Main particle with smooth gradient
        Stop[] stops = new Stop[] {
            new Stop(0, baseColor.deriveColor(0, 1.0, 0.9 + energyRatio * 0.4, 1.0)),
            new Stop(1, baseColor.deriveColor(0, 1.0, 0.5 + energyRatio * 0.3, 1.0))
        };
        
        RadialGradient particleGradient = new RadialGradient(
            0, 0, 0.3, 0.3, 0.6, true,
            CycleMethod.NO_CYCLE, stops
        );
        
        gc.setFill(particleGradient);
        gc.fillOval(px, py, CELL_SIZE, CELL_SIZE);
        
        // Highlight
        gc.setFill(Color.WHITE.deriveColor(0, 1, 1, 0.25));
        gc.fillOval(px + 0.5, py + 0.5, CELL_SIZE / 2.5, CELL_SIZE / 2.5);
    }
    
    private void drawCursor(GraphicsContext gc, Team team) {
        Position cursor = team.getTargetPosition();
        Color color = team.getBaseColor();
        
        double x = cursor.x() * CELL_SIZE;
        double y = cursor.y() * CELL_SIZE;
        
        // Soft pulsing glow
        gc.setGlobalAlpha(0.2);
        gc.setFill(color);
        gc.fillOval(x - 15, y - 15, 30, 30);
        gc.setGlobalAlpha(1.0);
        
        // Modern crosshair
        gc.setStroke(color.brighter());
        gc.setLineWidth(2);
        
        // Horizontal lines
        gc.strokeLine(x - 12, y, x - 5, y);
        gc.strokeLine(x + 5, y, x + 12, y);
        
        // Vertical lines
        gc.strokeLine(x, y - 12, x, y - 5);
        gc.strokeLine(x, y + 5, x, y + 12);
        
        // Corner brackets
        gc.strokeLine(x - 10, y - 10, x - 6, y - 10);
        gc.strokeLine(x - 10, y - 10, x - 10, y - 6);
        
        gc.strokeLine(x + 10, y - 10, x + 6, y - 10);
        gc.strokeLine(x + 10, y - 10, x + 10, y - 6);
        
        gc.strokeLine(x - 10, y + 10, x - 6, y + 10);
        gc.strokeLine(x - 10, y + 10, x - 10, y + 6);
        
        gc.strokeLine(x + 10, y + 10, x + 6, y + 10);
        gc.strokeLine(x + 10, y + 10, x + 10, y + 6);
        
        // Center dot
        gc.setFill(color);
        gc.fillOval(x - 2, y - 2, 4, 4);
    }
    
    public Position screenToBoard(double screenX, double screenY) {
        int x = (int) (screenX / CELL_SIZE);
        int y = (int) (screenY / CELL_SIZE);
        return new Position(x, y);
    }
}