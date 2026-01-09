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
 * Strategies:
 * - AGGRESSIVE: Move towards the center of enemy particles
 * - DEFENSIVE: Move away from enemies, protect own particles
 * - BALANCED: Mix of offensive and defensive moves
 * - HUNT: Target the weakest enemy cluster
 */
public class AIController {
    
    /**
     * AI difficulty levels affecting decision quality.
     */
    public enum Difficulty {
        EASY,    // Random movements, slow updates
        MEDIUM,  // Basic strategy, moderate updates
        HARD     // Optimal strategy, fast updates
    }
    
    /**
     * AI strategy types.
     */
    public enum Strategy {
        AGGRESSIVE,  // Always attack
        DEFENSIVE,   // Protect own particles
        BALANCED,    // Mix of both
        HUNT         // Target weakest cluster
    }
    
    private final Board board;
    private final Team controlledTeam;
    private final Team enemyTeam;
    private final Random random;
    
    private Difficulty difficulty;
    private Strategy strategy;
    
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
        this.strategy = Strategy.BALANCED;
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
     * Calculates the best target position based on current strategy.
     * 
     * @return optimal cursor position
     */
    private Position calculateTargetPosition() {
        return switch (strategy) {
            case AGGRESSIVE -> calculateAggressiveTarget();
            case DEFENSIVE -> calculateDefensiveTarget();
            case BALANCED -> calculateBalancedTarget();
            case HUNT -> calculateHuntTarget();
        };
    }
    
    /**
     * Aggressive strategy: Target center of enemy particles.
     */
    private Position calculateAggressiveTarget() {
        List<Particle> enemyParticles = enemyTeam.getParticles();
        
        if (enemyParticles.isEmpty()) {
            return lastTargetPosition;
        }
        
        // Calculate center of mass of enemy particles
        return calculateCenterOfMass(enemyParticles);
    }
    
    /**
     * Defensive strategy: Stay near own particles' center.
     */
    private Position calculateDefensiveTarget() {
        List<Particle> ownParticles = controlledTeam.getParticles();
        
        if (ownParticles.isEmpty()) {
            return lastTargetPosition;
        }
        
        // Calculate center of mass of own particles
        return calculateCenterOfMass(ownParticles);
    }
    
    /**
     * Balanced strategy: Mix of offensive and defensive.
     * Considers both team positions and current advantage.
     */
    private Position calculateBalancedTarget() {
        List<Particle> ownParticles = controlledTeam.getParticles();
        List<Particle> enemyParticles = enemyTeam.getParticles();
        
        if (ownParticles.isEmpty() || enemyParticles.isEmpty()) {
            return lastTargetPosition;
        }
        
        Position ownCenter = calculateCenterOfMass(ownParticles);
        Position enemyCenter = calculateCenterOfMass(enemyParticles);
        
        // Calculate advantage ratio
        double ratio = (double) ownParticles.size() / enemyParticles.size();
        
        // If winning, be more aggressive
        // If losing, be more defensive
        double aggressiveness;
        if (ratio > 1.2) {
            aggressiveness = 0.8; // Very aggressive when winning
        } else if (ratio < 0.8) {
            aggressiveness = 0.3; // Defensive when losing
        } else {
            aggressiveness = 0.5; // Balanced
        }
        
        // Add some randomness based on difficulty
        if (difficulty == Difficulty.EASY) {
            aggressiveness += (random.nextDouble() - 0.5) * 0.4;
            aggressiveness = Math.max(0.1, Math.min(0.9, aggressiveness));
        }
        
        // Interpolate between defensive and aggressive position
        int targetX = (int) (ownCenter.x() * (1 - aggressiveness) + enemyCenter.x() * aggressiveness);
        int targetY = (int) (ownCenter.y() * (1 - aggressiveness) + enemyCenter.y() * aggressiveness);
        
        return clampToBoard(new Position(targetX, targetY));
    }
    
    /**
     * Hunt strategy: Target the weakest/isolated enemy cluster.
     */
    private Position calculateHuntTarget() {
        List<Particle> enemyParticles = enemyTeam.getParticles();
        
        if (enemyParticles.isEmpty()) {
            return lastTargetPosition;
        }
        
        // Find the most isolated enemy particle (furthest from enemy center)
        Position enemyCenter = calculateCenterOfMass(enemyParticles);
        
        Particle weakestTarget = null;
        double maxDistance = 0;
        
        for (Particle p : enemyParticles) {
            double dist = p.getPosition().euclideanDistance(enemyCenter);
            // Also consider low energy as weakness
            double weakness = dist + (100 - p.getEnergy()) * 0.5;
            
            if (weakness > maxDistance) {
                maxDistance = weakness;
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
     * Ensures position is within board bounds and not on obstacle.
     */
    private Position clampToBoard(Position pos) {
        int x = Math.max(1, Math.min(board.getWidth() - 2, pos.x()));
        int y = Math.max(1, Math.min(board.getHeight() - 2, pos.y()));
        
        // If position is on obstacle, find nearest free cell
        if (board.isObstacle(x, y)) {
            Position free = findNearestFreeCell(x, y);
            if (free != null) {
                return free;
            }
        }
        
        return new Position(x, y);
    }
    
    /**
     * Finds nearest cell that is not an obstacle.
     */
    private Position findNearestFreeCell(int x, int y) {
        for (int radius = 1; radius < 20; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    int nx = x + dx;
                    int ny = y + dy;
                    if (board.isInBounds(nx, ny) && !board.isObstacle(nx, ny)) {
                        return new Position(nx, ny);
                    }
                }
            }
        }
        return null;
    }
    
    // === Getters and Setters ===
    
    public Difficulty getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
        setDifficultyParameters();
    }
    
    public Strategy getStrategy() {
        return strategy;
    }
    
    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }
    
    public Team getControlledTeam() {
        return controlledTeam;
    }
    
    /**
     * Dynamically adjusts strategy based on game state.
     * Call this periodically for adaptive AI behavior.
     */
    public void adaptStrategy() {
        int ownCount = controlledTeam.getParticleCount();
        int enemyCount = enemyTeam.getParticleCount();
        
        if (ownCount == 0 || enemyCount == 0) {
            return;
        }
        
        double ratio = (double) ownCount / enemyCount;
        
        if (ratio > 1.5) {
            // Winning big - hunt down remaining enemies
            strategy = Strategy.HUNT;
        } else if (ratio > 1.1) {
            // Slightly winning - stay aggressive
            strategy = Strategy.AGGRESSIVE;
        } else if (ratio < 0.6) {
            // Losing badly - defensive
            strategy = Strategy.DEFENSIVE;
        } else {
            // Close game - balanced
            strategy = Strategy.BALANCED;
        }
    }
}
