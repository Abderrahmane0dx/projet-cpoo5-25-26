package fr.uparis.liquidwar.model;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Board class.
 */
class BoardTest {
    
    private Board board;
    private Team team;
    
    @BeforeEach
    void setUp() {
        board = new Board(20, 15);
        team = new Team(1, Color.RED, new Position(10, 10));
    }
    
    @Test
    @DisplayName("Board creation works correctly")
    void testBoardCreation() {
        assertEquals(20, board.getWidth());
        assertEquals(15, board.getHeight());
    }
    
    @Test
    @DisplayName("Invalid board dimensions throw exception")
    void testInvalidDimensions() {
        assertThrows(IllegalArgumentException.class, () -> new Board(0, 10));
        assertThrows(IllegalArgumentException.class, () -> new Board(10, 0));
        assertThrows(IllegalArgumentException.class, () -> new Board(-5, 10));
    }
    
    @Test
    @DisplayName("isInBounds works correctly")
    void testIsInBounds() {
        assertTrue(board.isInBounds(0, 0));
        assertTrue(board.isInBounds(19, 14));
        assertTrue(board.isInBounds(10, 7));
        
        assertFalse(board.isInBounds(-1, 5));
        assertFalse(board.isInBounds(5, -1));
        assertFalse(board.isInBounds(20, 5));
        assertFalse(board.isInBounds(5, 15));
    }
    
    @Test
    @DisplayName("isInBounds with Position works")
    void testIsInBoundsPosition() {
        assertTrue(board.isInBounds(new Position(10, 7)));
        assertFalse(board.isInBounds(new Position(25, 7)));
    }
    
    @Test
    @DisplayName("Setting and checking obstacles works")
    void testObstacles() {
        assertFalse(board.isObstacle(5, 5));
        
        board.setObstacle(5, 5);
        assertTrue(board.isObstacle(5, 5));
        
        board.clearObstacle(5, 5);
        assertFalse(board.isObstacle(5, 5));
    }
    
    @Test
    @DisplayName("Out of bounds positions are treated as obstacles")
    void testOutOfBoundsIsObstacle() {
        assertTrue(board.isObstacle(-1, 5));
        assertTrue(board.isObstacle(25, 5));
    }
    
    @Test
    @DisplayName("Setting and getting particles works")
    void testParticles() {
        Particle p = new Particle(new Position(5, 5), team);
        
        assertNull(board.getParticle(5, 5));
        
        board.setParticle(p, 5, 5);
        assertEquals(p, board.getParticle(5, 5));
        assertEquals(new Position(5, 5), p.getPosition());
    }
    
    @Test
    @DisplayName("Setting particle updates its position")
    void testSetParticleUpdatesPosition() {
        Particle p = new Particle(new Position(10, 10), team);
        board.setParticle(p, 5, 5);
        
        assertEquals(new Position(5, 5), p.getPosition());
    }
    
    @Test
    @DisplayName("Moving particles works")
    void testMoveParticle() {
        Particle p = new Particle(new Position(5, 5), team);
        board.setParticle(p, 5, 5);
        
        boolean moved = board.moveParticle(new Position(5, 5), new Position(6, 6));
        
        assertTrue(moved);
        assertNull(board.getParticle(5, 5));
        assertEquals(p, board.getParticle(6, 6));
        assertEquals(new Position(6, 6), p.getPosition());
    }
    
    @Test
    @DisplayName("Cannot move to obstacle")
    void testCannotMoveToObstacle() {
        Particle p = new Particle(new Position(5, 5), team);
        board.setParticle(p, 5, 5);
        board.setObstacle(6, 6);
        
        boolean moved = board.moveParticle(new Position(5, 5), new Position(6, 6));
        
        assertFalse(moved);
        assertEquals(p, board.getParticle(5, 5)); // Still at original position
    }
    
    @Test
    @DisplayName("Cannot move out of bounds")
    void testCannotMoveOutOfBounds() {
        Particle p = new Particle(new Position(5, 5), team);
        board.setParticle(p, 5, 5);
        
        boolean moved = board.moveParticle(new Position(5, 5), new Position(25, 25));
        
        assertFalse(moved);
    }
    
    @Test
    @DisplayName("isFree works correctly")
    void testIsFree() {
        assertTrue(board.isFree(5, 5));
        
        board.setObstacle(5, 5);
        assertFalse(board.isFree(5, 5));
        
        board.clearObstacle(5, 5);
        assertTrue(board.isFree(5, 5));
        
        Particle p = new Particle(new Position(5, 5), team);
        board.setParticle(p, 5, 5);
        assertFalse(board.isFree(5, 5));
    }
    
    @Test
    @DisplayName("getAllParticles returns all particles")
    void testGetAllParticles() {
        Particle p1 = new Particle(new Position(5, 5), team);
        Particle p2 = new Particle(new Position(10, 10), team);
        Particle p3 = new Particle(new Position(15, 7), team);
        
        board.setParticle(p1, 5, 5);
        board.setParticle(p2, 10, 10);
        board.setParticle(p3, 15, 7);
        
        var particles = board.getAllParticles();
        assertEquals(3, particles.size());
        assertTrue(particles.contains(p1));
        assertTrue(particles.contains(p2));
        assertTrue(particles.contains(p3));
    }
    
    @Test
    @DisplayName("createBorderWalls creates walls on edges")
    void testCreateBorderWalls() {
        board.createBorderWalls();
        
        // Check corners
        assertTrue(board.isObstacle(0, 0));
        assertTrue(board.isObstacle(19, 0));
        assertTrue(board.isObstacle(0, 14));
        assertTrue(board.isObstacle(19, 14));
        
        // Check edges
        assertTrue(board.isObstacle(10, 0));
        assertTrue(board.isObstacle(10, 14));
        assertTrue(board.isObstacle(0, 7));
        assertTrue(board.isObstacle(19, 7));
        
        // Check interior is free
        assertFalse(board.isObstacle(10, 7));
    }
    
    @Test
    @DisplayName("createRandomObstacles respects density")
    void testCreateRandomObstacles() {
        board.createRandomObstacles(0.0);
        
        // With 0 density, no obstacles (except borders if created)
        int obstacleCount = 0;
        for (int y = 1; y < board.getHeight() - 1; y++) {
            for (int x = 1; x < board.getWidth() - 1; x++) {
                if (board.isObstacle(x, y)) {
                    obstacleCount++;
                }
            }
        }
        assertEquals(0, obstacleCount);
    }
    
    @Test
    @DisplayName("Invalid density throws exception")
    void testInvalidDensity() {
        assertThrows(IllegalArgumentException.class, 
            () -> board.createRandomObstacles(-0.1));
        assertThrows(IllegalArgumentException.class, 
            () -> board.createRandomObstacles(1.1));
    }
    
    @Test
    @DisplayName("toString returns readable format")
    void testToString() {
        String str = board.toString();
        assertTrue(str.contains("Board"));
        assertTrue(str.contains("20x15"));
    }
}