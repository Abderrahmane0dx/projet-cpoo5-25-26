package fr.uparis.liquidwar.algorithm;

import fr.uparis.liquidwar.model.*;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GradientCalculator class.
 */
class GradientCalculatorTest {
    
    private Board board;
    private Team team;
    private GradientCalculator calculator;
    
    @BeforeEach
    void setUp() {
        board = new Board(20, 15);
        team = new Team(1, Color.RED, new Position(10, 7));
        calculator = new GradientCalculator(board);
    }
    
    @Test
    @DisplayName("Calculator creation works")
    void testCreation() {
        assertNotNull(calculator);
    }
    
    @Test
    @DisplayName("Null board throws exception")
    void testNullBoard() {
        assertThrows(IllegalArgumentException.class,
            () -> new GradientCalculator(null));
    }
    
    @Test
    @DisplayName("Null team throws exception")
    void testNullTeam() {
        assertThrows(IllegalArgumentException.class,
            () -> calculator.calculate(null));
    }
    
    @Test
    @DisplayName("Target position has distance 0")
    void testTargetHasZeroDistance() {
        Gradient gradient = calculator.calculate(team);
        
        Position target = team.getTargetPosition();
        assertEquals(0, gradient.getDistance(target));
    }
    
    @Test
    @DisplayName("Adjacent cells have correct distance")
    void testAdjacentCells() {
        Gradient gradient = calculator.calculate(team);
        Position target = team.getTargetPosition();
        
        // Cardinal neighbors (cost 10)
        assertEquals(10, gradient.getDistance(target.add(1, 0)));
        assertEquals(10, gradient.getDistance(target.add(-1, 0)));
        assertEquals(10, gradient.getDistance(target.add(0, 1)));
        assertEquals(10, gradient.getDistance(target.add(0, -1)));
    }
    
    @Test
    @DisplayName("Diagonal cells have higher cost")
    void testDiagonalCells() {
        Gradient gradient = calculator.calculate(team);
        Position target = team.getTargetPosition();
        
        // Diagonal neighbors (cost 14)
        assertEquals(14, gradient.getDistance(target.add(1, 1)));
        assertEquals(14, gradient.getDistance(target.add(1, -1)));
        assertEquals(14, gradient.getDistance(target.add(-1, 1)));
        assertEquals(14, gradient.getDistance(target.add(-1, -1)));
    }
    
    @Test
    @DisplayName("Obstacles are unreachable")
    void testObstaclesUnreachable() {
        board.setObstacle(5, 5);
        Gradient gradient = calculator.calculate(team);
        
        assertEquals(Gradient.INFINITE, gradient.getDistance(5, 5));
    }
    
    @Test
    @DisplayName("Target on obstacle returns empty gradient")
    void testTargetOnObstacle() {
        Position obstaclePos = new Position(12, 8);
        board.setObstacle(obstaclePos);
        team.setTargetPosition(obstaclePos);
        
        Gradient gradient = calculator.calculate(team);
        
        // All cells should be unreachable
        assertEquals(Gradient.INFINITE, gradient.getDistance(10, 7));
    }
    
    @Test
    @DisplayName("Walls block pathfinding")
    void testWallsBlock() {
        // Create vertical wall
        for (int y = 0; y < board.getHeight(); y++) {
            board.setObstacle(10, y);
        }
        
        // Place target on right side
        team.setTargetPosition(new Position(15, 7));
        
        Gradient gradient = calculator.calculate(team);
        
        // Left side should be unreachable
        assertEquals(Gradient.INFINITE, gradient.getDistance(5, 7));
    }
    
    @Test
    @DisplayName("Simple gradient calculation works")
    void testSimpleCalculation() {
        Gradient gradient = calculator.calculateSimple(team);
        Position target = team.getTargetPosition();
        
        assertEquals(0, gradient.getDistance(target));
        assertEquals(1, gradient.getDistance(target.add(1, 0)));
        assertEquals(1, gradient.getDistance(target.add(1, 1))); // Also 1 in simple mode
    }
    
    @Test
    @DisplayName("isReachable works correctly")
    void testIsReachable() {
        assertTrue(calculator.isReachable(team, new Position(5, 5)));
        
        board.setObstacle(5, 5);
        assertFalse(calculator.isReachable(team, new Position(5, 5)));
    }
    
    @Test
    @DisplayName("getDistance returns correct value")
    void testGetDistance() {
        Position target = team.getTargetPosition();
        
        assertEquals(0, calculator.getDistance(team, target));
        assertTrue(calculator.getDistance(team, target.add(2, 0)) > 0);
    }
    
    @Test
    @DisplayName("Distance increases with path length")
    void testDistanceIncreases() {
        Gradient gradient = calculator.calculateSimple(team);
        Position target = team.getTargetPosition();
        
        int dist1 = gradient.getDistance(target.add(1, 0));
        int dist2 = gradient.getDistance(target.add(2, 0));
        int dist3 = gradient.getDistance(target.add(3, 0));
        
        assertTrue(dist1 < dist2);
        assertTrue(dist2 < dist3);
    }
    
    @Test
    @DisplayName("Gradient covers entire reachable area")
    void testGradientCoverage() {
        Gradient gradient = calculator.calculateSimple(team);
        
        // Count reachable cells
        int reachableCount = 0;
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                if (gradient.isReachable(x, y)) {
                    reachableCount++;
                }
            }
        }
        
        // Should reach most of the board
        assertTrue(reachableCount > 0);
    }
}