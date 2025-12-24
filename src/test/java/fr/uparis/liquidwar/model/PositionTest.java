package fr.uparis.liquidwar.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Position class.
 */
class PositionTest {
    
    @Test
    @DisplayName("Position creation works correctly")
    void testPositionCreation() {
        Position pos = new Position(5, 10);
        assertEquals(5, pos.x());
        assertEquals(10, pos.y());
    }
    
    @Test
    @DisplayName("Manhattan distance calculation is correct")
    void testManhattanDistance() {
        Position p1 = new Position(0, 0);
        Position p2 = new Position(3, 4);
        
        assertEquals(7, p1.manhattanDistance(p2));
        assertEquals(7, p2.manhattanDistance(p1)); // symmetric
    }
    
    @Test
    @DisplayName("Manhattan distance to self is zero")
    void testManhattanDistanceToSelf() {
        Position pos = new Position(5, 5);
        assertEquals(0, pos.manhattanDistance(pos));
    }
    
    @Test
    @DisplayName("Add offset creates new position correctly")
    void testAdd() {
        Position pos = new Position(10, 20);
        Position newPos = pos.add(5, -3);
        
        assertEquals(15, newPos.x());
        assertEquals(17, newPos.y());
        
        // Original should be unchanged (immutability)
        assertEquals(10, pos.x());
        assertEquals(20, pos.y());
    }
    
    @Test
    @DisplayName("isInBounds returns true for valid positions")
    void testIsInBounds() {
        Position pos = new Position(5, 5);
        assertTrue(pos.isInBounds(10, 10));
        assertTrue(pos.isInBounds(6, 6));
    }
    
    @Test
    @DisplayName("isInBounds returns false for out of bounds positions")
    void testIsOutOfBounds() {
        Position pos1 = new Position(-1, 5);
        assertFalse(pos1.isInBounds(10, 10));
        
        Position pos2 = new Position(5, -1);
        assertFalse(pos2.isInBounds(10, 10));
        
        Position pos3 = new Position(10, 5);
        assertFalse(pos3.isInBounds(10, 10));
        
        Position pos4 = new Position(5, 10);
        assertFalse(pos4.isInBounds(10, 10));
    }
    
    @Test
    @DisplayName("Edge positions are handled correctly")
    void testEdgePositions() {
        Position corner = new Position(0, 0);
        assertTrue(corner.isInBounds(10, 10));
        
        Position almostOut = new Position(9, 9);
        assertTrue(almostOut.isInBounds(10, 10));
    }
    
    @Test
    @DisplayName("Euclidean distance calculation is correct")
    void testEuclideanDistance() {
        Position p1 = new Position(0, 0);
        Position p2 = new Position(3, 4);
        
        assertEquals(5.0, p1.euclideanDistance(p2), 0.001);
    }
    
    @Test
    @DisplayName("Positions with same coordinates are equal")
    void testEquality() {
        Position p1 = new Position(5, 10);
        Position p2 = new Position(5, 10);
        Position p3 = new Position(5, 11);
        
        assertEquals(p1, p2);
        assertNotEquals(p1, p3);
    }
    
    @Test
    @DisplayName("toString returns readable format")
    void testToString() {
        Position pos = new Position(7, 13);
        assertEquals("(7, 13)", pos.toString());
    }
}