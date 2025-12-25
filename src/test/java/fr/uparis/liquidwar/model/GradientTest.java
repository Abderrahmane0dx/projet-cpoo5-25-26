package fr.uparis.liquidwar.model;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Gradient class.
 */
class GradientTest {
    
    private Gradient gradient;
    private Team team;
    
    @BeforeEach
    void setUp() {
        team = new Team(1, Color.RED, new Position(10, 10));
        gradient = new Gradient(20, 15, team);
    }
    
    @Test
    @DisplayName("Gradient creation works correctly")
    void testGradientCreation() {
        assertEquals(20, gradient.getWidth());
        assertEquals(15, gradient.getHeight());
        assertEquals(team, gradient.getTeam());
    }
    
    @Test
    @DisplayName("Invalid gradient dimensions throw exception")
    void testInvalidDimensions() {
        assertThrows(IllegalArgumentException.class, 
            () -> new Gradient(0, 10, team));
        assertThrows(IllegalArgumentException.class, 
            () -> new Gradient(10, 0, team));
        assertThrows(IllegalArgumentException.class, 
            () -> new Gradient(-5, 10, team));
    }
    
    @Test
    @DisplayName("Null team throws exception")
    void testNullTeam() {
        assertThrows(IllegalArgumentException.class, 
            () -> new Gradient(10, 10, null));
    }
    
    @Test
    @DisplayName("All distances initialized to INFINITE")
    void testInitializedToInfinite() {
        for (int y = 0; y < gradient.getHeight(); y++) {
            for (int x = 0; x < gradient.getWidth(); x++) {
                assertEquals(Gradient.INFINITE, gradient.getDistance(x, y));
            }
        }
    }
    
    @Test
    @DisplayName("Setting and getting distance works")
    void testSetAndGetDistance() {
        gradient.setDistance(5, 5, 42);
        assertEquals(42, gradient.getDistance(5, 5));
        
        gradient.setDistance(new Position(10, 7), 100);
        assertEquals(100, gradient.getDistance(new Position(10, 7)));
    }
    
    @Test
    @DisplayName("Out of bounds returns INFINITE")
    void testOutOfBoundsReturnsInfinite() {
        assertEquals(Gradient.INFINITE, gradient.getDistance(-1, 5));
        assertEquals(Gradient.INFINITE, gradient.getDistance(25, 5));
        assertEquals(Gradient.INFINITE, gradient.getDistance(5, -1));
        assertEquals(Gradient.INFINITE, gradient.getDistance(5, 20));
    }
    
    @Test
    @DisplayName("Setting distance out of bounds does nothing")
    void testSetDistanceOutOfBounds() {
        gradient.setDistance(-1, 5, 10);
        gradient.setDistance(25, 5, 10);
        // Should not crash, just ignored
    }
    
    @Test
    @DisplayName("reset() clears all distances to INFINITE")
    void testReset() {
        gradient.setDistance(5, 5, 10);
        gradient.setDistance(10, 10, 20);
        gradient.setDistance(15, 7, 30);
        
        gradient.reset();
        
        assertEquals(Gradient.INFINITE, gradient.getDistance(5, 5));
        assertEquals(Gradient.INFINITE, gradient.getDistance(10, 10));
        assertEquals(Gradient.INFINITE, gradient.getDistance(15, 7));
    }
    
    @Test
    @DisplayName("copyFrom() copies distances correctly")
    void testCopyFrom() {
        Gradient source = new Gradient(20, 15, team);
        source.setDistance(5, 5, 10);
        source.setDistance(10, 10, 20);
        source.setDistance(15, 7, 30);
        
        gradient.copyFrom(source);
        
        assertEquals(10, gradient.getDistance(5, 5));
        assertEquals(20, gradient.getDistance(10, 10));
        assertEquals(30, gradient.getDistance(15, 7));
    }
    
    @Test
    @DisplayName("copyFrom() with incompatible dimensions throws exception")
    void testCopyFromIncompatible() {
        Gradient different = new Gradient(10, 10, team);
        
        assertThrows(IllegalArgumentException.class, 
            () -> gradient.copyFrom(different));
    }
    
    @Test
    @DisplayName("isReachable() works correctly")
    void testIsReachable() {
        assertFalse(gradient.isReachable(5, 5)); // Initially INFINITE
        
        gradient.setDistance(5, 5, 10);
        assertTrue(gradient.isReachable(5, 5));
        
        gradient.setDistance(5, 5, 0);
        assertTrue(gradient.isReachable(5, 5)); // 0 is reachable
        
        gradient.setDistance(5, 5, Gradient.INFINITE);
        assertFalse(gradient.isReachable(5, 5));
    }
    
    @Test
    @DisplayName("isReachable() with Position works")
    void testIsReachablePosition() {
        Position pos = new Position(7, 8);
        
        assertFalse(gradient.isReachable(pos));
        
        gradient.setDistance(pos, 15);
        assertTrue(gradient.isReachable(pos));
    }
    
    @Test
    @DisplayName("toString returns readable format")
    void testToString() {
        String str = gradient.toString();
        assertTrue(str.contains("Gradient"));
        assertTrue(str.contains("team=1"));
        assertTrue(str.contains("20x15"));
    }
    
    @Test
    @DisplayName("Distance at target position can be zero")
    void testZeroDistance() {
        gradient.setDistance(10, 10, 0);
        assertEquals(0, gradient.getDistance(10, 10));
        assertTrue(gradient.isReachable(10, 10));
    }
    
    @Test
    @DisplayName("Large distance values work correctly")
    void testLargeDistances() {
        gradient.setDistance(5, 5, 999999);
        assertEquals(999999, gradient.getDistance(5, 5));
        assertTrue(gradient.isReachable(5, 5));
    }
}