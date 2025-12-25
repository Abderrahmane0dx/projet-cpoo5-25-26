package fr.uparis.liquidwar.algorithm;

import fr.uparis.liquidwar.model.Board;
import fr.uparis.liquidwar.model.Gradient;
import fr.uparis.liquidwar.model.Particle;
import fr.uparis.liquidwar.model.Position;
import fr.uparis.liquidwar.util.Direction;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles particle movement according to Liquid War rules.
 * 
 * Movement Rules (in priority order):
 * 1. Main direction free → move there
 * 2. Good direction free → move there
 * 3. Acceptable direction free → move there
 * 4. Main direction has enemy → attack
 * 5. Good direction has enemy → attack
 * 6. Main direction has ally → heal
 * 7. Otherwise → do nothing
 */
public class ParticleMovement {
    private final Board board;
    
    /**
     * Creates a particle movement handler.
     * 
     * @param board the game board
     */
    public ParticleMovement(Board board) {
        if (board == null) {
            throw new IllegalArgumentException("Board cannot be null");
        }
        this.board = board;
    }
    
    /**
     * Attempts to move a particle according to the gradient.
     * Returns true if any action was taken (move, attack, heal).
     * 
     * @param particle particle to move
     * @param gradient gradient for particle's team
     * @return true if particle moved/attacked/healed
     */
    public boolean moveParticle(Particle particle, Gradient gradient) {
        if (particle == null || gradient == null) {
            return false;
        }
        
        Position current = particle.getPosition();
        int currentDist = gradient.getDistance(current);
        
        // If already at target or unreachable, don't move
        if (currentDist == 0 || currentDist == Gradient.INFINITE) {
            return false;
        }
        
        // Analyze all 8 directions
        List<Direction> mainDirs = new ArrayList<>();
        List<Direction> goodDirs = new ArrayList<>();
        List<Direction> acceptableDirs = new ArrayList<>();
        
        int bestDist = currentDist;
        
        for (Direction dir : Direction.values()) {
            Position neighbor = dir.apply(current);
            
            if (!board.isInBounds(neighbor)) {
                continue;
            }
            
            int neighborDist = gradient.getDistance(neighbor);
            
            // Categorize direction based on distance
            if (neighborDist < bestDist) {
                // Found better direction, reset main dirs
                if (neighborDist < bestDist - 5) { // Significantly better
                    mainDirs.clear();
                    mainDirs.add(dir);
                    bestDist = neighborDist;
                } else {
                    mainDirs.add(dir);
                }
            } else if (neighborDist < currentDist) {
                goodDirs.add(dir); // Gets closer
            } else if (neighborDist == currentDist) {
                acceptableDirs.add(dir); // Maintains distance
            }
            // else: bad direction (increases distance), ignore
        }
        
        // Apply movement rules in priority order
        
        // Rule 1: Main direction free
        if (tryMove(particle, mainDirs)) return true;
        
        // Rule 2: Good direction free
        if (tryMove(particle, goodDirs)) return true;
        
        // Rule 3: Acceptable direction free
        if (tryMove(particle, acceptableDirs)) return true;
        
        // Rule 4: Main direction has enemy
        if (tryAttack(particle, mainDirs)) return true;
        
        // Rule 5: Good direction has enemy
        if (tryAttack(particle, goodDirs)) return true;
        
        // Rule 6: Main direction has ally
        if (tryHeal(particle, mainDirs)) return true;
        
        // Rule 7: Do nothing
        return false;
    }
    
    /**
     * Tries to move particle in one of the given directions.
     * 
     * @param particle particle to move
     * @param directions possible directions
     * @return true if moved
     */
    private boolean tryMove(Particle particle, List<Direction> directions) {
        for (Direction dir : directions) {
            Position newPos = dir.apply(particle.getPosition());
            
            if (board.isFree(newPos)) {
                // Move particle
                board.moveParticle(particle.getPosition(), newPos);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Tries to attack enemy in one of the given directions.
     * 
     * @param particle particle that attacks
     * @param directions possible directions
     * @return true if attacked
     */
    private boolean tryAttack(Particle particle, List<Direction> directions) {
        for (Direction dir : directions) {
            Position targetPos = dir.apply(particle.getPosition());
            Particle target = board.getParticle(targetPos);
            
            if (target != null && target.getTeam() != particle.getTeam()) {
                // Attack enemy (returns true if target was converted)
                particle.attack(target);
                return true; // Attack happened
            }
        }
        return false;
    }
    
    /**
     * Tries to heal ally in one of the given directions.
     * 
     * @param particle particle that heals
     * @param directions possible directions
     * @return true if healed
     */
    private boolean tryHeal(Particle particle, List<Direction> directions) {
        for (Direction dir : directions) {
            Position allyPos = dir.apply(particle.getPosition());
            Particle ally = board.getParticle(allyPos);
            
            if (ally != null && ally.getTeam() == particle.getTeam() && ally != particle) {
                // Heal ally
                return particle.heal(ally);
            }
        }
        return false;
    }
    
    /**
     * Finds the best direction for a particle to move.
     * Returns null if no good direction exists.
     * 
     * @param particle particle to check
     * @param gradient gradient for particle's team
     * @return best direction, or null
     */
    public Direction getBestDirection(Particle particle, Gradient gradient) {
        if (particle == null || gradient == null) {
            return null;
        }
        
        Position current = particle.getPosition();
        int currentDist = gradient.getDistance(current);
        
        Direction bestDir = null;
        int bestDist = currentDist;
        
        for (Direction dir : Direction.values()) {
            Position neighbor = dir.apply(current);
            
            if (!board.isInBounds(neighbor) || board.isObstacle(neighbor)) {
                continue;
            }
            
            int neighborDist = gradient.getDistance(neighbor);
            
            if (neighborDist < bestDist) {
                bestDist = neighborDist;
                bestDir = dir;
            }
        }
        
        return bestDir;
    }
    
    /**
     * Checks if a particle can move in any direction.
     * 
     * @param particle particle to check
     * @return true if can move
     */
    public boolean canMove(Particle particle) {
        if (particle == null) {
            return false;
        }
        
        Position current = particle.getPosition();
        
        for (Direction dir : Direction.values()) {
            Position neighbor = dir.apply(current);
            
            if (board.isFree(neighbor)) {
                return true;
            }
        }
        
        return false;
    }
}