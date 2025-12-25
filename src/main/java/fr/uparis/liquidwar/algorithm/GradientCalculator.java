package fr.uparis.liquidwar.algorithm;

import fr.uparis.liquidwar.model.Board;
import fr.uparis.liquidwar.model.Gradient;
import fr.uparis.liquidwar.model.Position;
import fr.uparis.liquidwar.model.Team;
import fr.uparis.liquidwar.util.Direction;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Calculates gradient (distance map) for a team using BFS algorithm.
 * Based on the Liquid War 5 algorithm described at:
 * https://ufoot.org/liquidwar/v5/techinfo/algorithm
 */
public class GradientCalculator {
    private final Board board;
    
    /**
     * Creates a gradient calculator for a board.
     * 
     * @param board the game board
     */
    public GradientCalculator(Board board) {
        if (board == null) {
            throw new IllegalArgumentException("Board cannot be null");
        }
        this.board = board;
    }
    
    /**
     * Calculates gradient for a team using Breadth-First Search (BFS).
     * 
     * Algorithm:
     * 1. Start from team's cursor position (distance 0)
     * 2. For each neighbor (8 directions):
     *    - If not obstacle and not visited: set distance = current + 1
     *    - Add to queue for processing
     * 3. Repeat until all reachable cells are processed
     * 
     * @param team the team to calculate gradient for
     * @return gradient with distances from cursor
     */
    public Gradient calculate(Team team) {
        if (team == null) {
            throw new IllegalArgumentException("Team cannot be null");
        }
        
        Gradient gradient = new Gradient(board.getWidth(), board.getHeight(), team);
        Position target = team.getTargetPosition();
        
        // Check if target is valid
        if (!board.isInBounds(target) || board.isObstacle(target)) {
            // Target is unreachable, return empty gradient
            return gradient;
        }
        
        // BFS initialization
        Queue<Position> queue = new LinkedList<>();
        gradient.setDistance(target, 0);
        queue.add(target);
        
        // BFS main loop
        while (!queue.isEmpty()) {
            Position current = queue.poll();
            int currentDist = gradient.getDistance(current);
            
            // Check all 8 neighbors
            for (Direction dir : Direction.values()) {
                Position neighbor = dir.apply(current);
                
                // Skip if out of bounds or obstacle
                if (!board.isInBounds(neighbor) || board.isObstacle(neighbor)) {
                    continue;
                }
                
                // Calculate new distance
                // Diagonal moves cost slightly more (√2 ≈ 1.4)
                int moveCost = dir.isDiagonal() ? 14 : 10; // Using integer math (*10)
                int newDist = currentDist + moveCost;
                
                // Update if this path is shorter
                if (newDist < gradient.getDistance(neighbor)) {
                    gradient.setDistance(neighbor, newDist);
                    queue.add(neighbor);
                }
            }
        }
        
        return gradient;
    }
    
    /**
     * Calculates gradient using simple uniform cost (all moves cost 1).
     * Simpler but less accurate than the weighted version.
     * 
     * @param team the team to calculate gradient for
     * @return gradient with uniform distances
     */
    public Gradient calculateSimple(Team team) {
        if (team == null) {
            throw new IllegalArgumentException("Team cannot be null");
        }
        
        Gradient gradient = new Gradient(board.getWidth(), board.getHeight(), team);
        Position target = team.getTargetPosition();
        
        if (!board.isInBounds(target) || board.isObstacle(target)) {
            return gradient;
        }
        
        Queue<Position> queue = new LinkedList<>();
        gradient.setDistance(target, 0);
        queue.add(target);
        
        while (!queue.isEmpty()) {
            Position current = queue.poll();
            int currentDist = gradient.getDistance(current);
            
            for (Direction dir : Direction.values()) {
                Position neighbor = dir.apply(current);
                
                if (!board.isInBounds(neighbor) || board.isObstacle(neighbor)) {
                    continue;
                }
                
                int newDist = currentDist + 1;
                
                if (newDist < gradient.getDistance(neighbor)) {
                    gradient.setDistance(neighbor, newDist);
                    queue.add(neighbor);
                }
            }
        }
        
        return gradient;
    }
    
    /**
     * Checks if a position is reachable from the team's cursor.
     * 
     * @param team team to check for
     * @param position position to check
     * @return true if reachable
     */
    public boolean isReachable(Team team, Position position) {
        Gradient gradient = calculateSimple(team);
        return gradient.isReachable(position);
    }
    
    /**
     * Gets the shortest distance from a position to team's cursor.
     * 
     * @param team team to calculate for
     * @param position starting position
     * @return distance, or INFINITE if unreachable
     */
    public int getDistance(Team team, Position position) {
        Gradient gradient = calculateSimple(team);
        return gradient.getDistance(position);
    }
}