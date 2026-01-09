package fr.uparis.liquidwar.algorithm;

import fr.uparis.liquidwar.model.Board;
import fr.uparis.liquidwar.model.Particle;
import fr.uparis.liquidwar.model.Position;
import fr.uparis.liquidwar.model.Team;

import java.util.List;
import java.util.Random;

/**
 * AI Controller for automated team movement.
 * Controls the cursor position for a team to maximize particle capture.
 * 
 * Difficulty levels determine the strategy:
 * - EASY: Defensive, slow updates, random movements
 * - MEDIUM: Balanced strategy, moderate updates
 * - HARD: Aggressive and adaptive, fast updates
 */
public class AIController {
    
    /**
     * AI difficulty levels affecting decision quality and strategy.
     */
    public enum Difficulty {
        EASY,    // Defensive, slow updates
        MEDIUM,  // Balanced, moderate updates
        HARD     // Aggressive/adaptive, fast updates
    }
    
    private final Board board;
    private final Team controlledTeam;
    private final Team enemyTeam;
    private final Random random;
    
    private Difficulty difficulty;
    
    private int updateCounter = 0;
    private int updateFrequency; // How often to recalculate (in frames)
    
    private Position lastTargetPosition;
    
    /**
     * Creates an AI controller for a team.
     * 
     * @param board the game board
     * @param controlledTeam team controlled by AI
     * @param enemyTeam enemy team
     * @param difficulty AI difficulty level
     */
    public AIController(Board board, Team controlledTeam, Team enemyTeam, Difficulty difficulty) {
        if (board == null || controlledTeam == null || enemyTeam == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }
        this.board = board;
        this.controlledTeam = controlledTeam;
        this.enemyTeam = enemyTeam;
        this.random = new Random();
        this.difficulty = difficulty;
        this.lastTargetPosition = controlledTeam.getTargetPosition();
        
        setDifficultyParameters();
    }
    
    /**
     * Creates an AI controller with default medium difficulty.
     */
    public AIController(Board board, Team controlledTeam, Team enemyTeam) {
        this(board, controlledTeam, enemyTeam, Difficulty.MEDIUM);
    }
    
    /**
     * Sets parameters based on difficulty level.
     */
    private void setDifficultyParameters() {
        switch (difficulty) {
            case EASY -> {
                updateFrequency = 30; // Update every 30 frames (~0.5s at 60fps)
            }
            case MEDIUM -> {
                updateFrequency = 15; // Update every 15 frames (~0.25s)
            }
            case HARD -> {
                updateFrequency = 5;  // Update every 5 frames (~0.08s)
            }
        }
    }
    
    /**
     * Updates the AI and moves the cursor if needed.
     * Should be called every game frame.
     */
    public void update() {
        updateCounter++;
        
        if (updateCounter >= updateFrequency) {
            updateCounter = 0;
            calculateAndMoveCursor();
        }
    }
    
    /**
     * Calculates the best cursor position and moves it.
     */
    private void calculateAndMoveCursor() {
        Position newTarget = calculateTargetPosition();
        
        if (newTarget != null && !newTarget.equals(lastTargetPosition)) {
            // Smooth movement - don't teleport, move gradually
            Position smoothTarget = smoothMovement(lastTargetPosition, newTarget);
            controlledTeam.setTargetPosition(smoothTarget);
            lastTargetPosition = smoothTarget;
        }
    }
    
    /**
     * Calculates the best target position based on difficulty.
     * 
     * @return optimal cursor position
     */
    private Position calculateTargetPosition() {
        return switch (difficulty) {
            case EASY -> calculateEasyTarget();
            case MEDIUM -> calculateMediumTarget();
            case HARD -> calculateHardTarget();
        };
    }
    
    /**
     * EASY: Defensive strategy - stay near own particles with some randomness.
     */
    private Position calculateEasyTarget() {
        List<Particle> ownParticles = controlledTeam.getParticles();
        
        if (ownParticles.isEmpty()) {
            return lastTargetPosition;
        }
        
        // Calculate center of mass of own particles (defensive)
        Position ownCenter = calculateCenterOfMass(ownParticles);
        
        // Add randomness for easy mode
        int randomX = ownCenter.x() + random.nextInt(21) - 10;
        int randomY = ownCenter.y() + random.nextInt(21) - 10;
        
        return clampToBoard(new Position(randomX, randomY));
    }
    
    /**
     * MEDIUM: Balanced strategy - mix of offensive and defensive.
     */
    private Position calculateMediumTarget() {
        List<Particle> ownParticles = controlledTeam.getParticles();
        List<Particle> enemyParticles = enemyTeam.getParticles();
        
        if (ownParticles.isEmpty() || enemyParticles.isEmpty()) {
            return lastTargetPosition;
        }
        
        Position ownCenter = calculateCenterOfMass(ownParticles);
        Position enemyCenter = calculateCenterOfMass(enemyParticles);
        
        // Calculate advantage ratio
        double ratio = (double) ownParticles.size() / enemyParticles.size();
        
        // If winning, be more aggressive; if losing, be more defensive
        double aggressiveness;
        if (ratio > 1.2) {
            aggressiveness = 0.7;
        } else if (ratio < 0.8) {
            aggressiveness = 0.3;
        } else {
            aggressiveness = 0.5;
        }
        
        // Interpolate between defensive and aggressive position
        int targetX = (int) (ownCenter.x() * (1 - aggressiveness) + enemyCenter.x() * aggressiveness);
        int targetY = (int) (ownCenter.y() * (1 - aggressiveness) + enemyCenter.y() * aggressiveness);
        
        return clampToBoard(new Position(targetX, targetY));
    }
    
    /**
     * HARD: Aggressive and adaptive - targets enemies intelligently.
     */
    private Position calculateHardTarget() {
        List<Particle> ownParticles = controlledTeam.getParticles();
        List<Particle> enemyParticles = enemyTeam.getParticles();
        
        if (enemyParticles.isEmpty()) {
            return lastTargetPosition;
        }
        
        if (ownParticles.isEmpty()) {
            return lastTargetPosition;
        }
        
        double ratio = (double) ownParticles.size() / enemyParticles.size();
        
        // Winning big - hunt isolated enemies
        if (ratio > 1.5) {
            return calculateHuntTarget(enemyParticles);
        }
        
        // Otherwise aggressive - go for enemy center
        Position enemyCenter = calculateCenterOfMass(enemyParticles);
        return clampToBoard(enemyCenter);
    }
    
    /**
     * Hunt strategy: Target the weakest/isolated enemy particle.
     */
    private Position calculateHuntTarget(List<Particle> enemyParticles) {
        Position enemyCenter = calculateCenterOfMass(enemyParticles);
        
        Particle weakestTarget = null;
        double maxWeakness = 0;
        
        for (Particle p : enemyParticles) {
            double dist = p.getPosition().euclideanDistance(enemyCenter);
            double weakness = dist + (100 - p.getEnergy()) * 0.5;
            
            if (weakness > maxWeakness) {
                maxWeakness = weakness;
                weakestTarget = p;
            }
        }
        
        if (weakestTarget != null) {
            return weakestTarget.getPosition();
        }
        
        return enemyCenter;
    }
    
    /**
     * Calculates center of mass for a list of particles.
     */
    private Position calculateCenterOfMass(List<Particle> particles) {
        if (particles.isEmpty()) {
            return new Position(board.getWidth() / 2, board.getHeight() / 2);
        }
        
        long sumX = 0;
        long sumY = 0;
        
        for (Particle p : particles) {
            sumX += p.getPosition().x();
            sumY += p.getPosition().y();
        }
        
        int centerX = (int) (sumX / particles.size());
        int centerY = (int) (sumY / particles.size());
        
        return new Position(centerX, centerY);
    }
    
    /**
     * Smoothly moves towards target to avoid jerky movements.
     */
    private Position smoothMovement(Position current, Position target) {
        // Movement speed based on difficulty
        int maxStep = switch (difficulty) {
            case EASY -> 3;
            case MEDIUM -> 5;
            case HARD -> 8;
        };
        
        int dx = target.x() - current.x();
        int dy = target.y() - current.y();
        
        // Clamp movement to max step
        if (Math.abs(dx) > maxStep) {
            dx = dx > 0 ? maxStep : -maxStep;
        }
        if (Math.abs(dy) > maxStep) {
            dy = dy > 0 ? maxStep : -maxStep;
        }
        
        return clampToBoard(new Position(current.x() + dx, current.y() + dy));
    }
    
    /**
     * Ensures position is within board bounds.
     * The cursor can traverse obstacles, so no obstacle check is needed.
     */
    private Position clampToBoard(Position pos) {
        int x = Math.max(1, Math.min(board.getWidth() - 2, pos.x()));
        int y = Math.max(1, Math.min(board.getHeight() - 2, pos.y()));
        
        return new Position(x, y);
    }
    
    // === Getters and Setters ===
    
    public Difficulty getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
        setDifficultyParameters();
    }
    
    public Team getControlledTeam() {
        return controlledTeam;
    }
}
