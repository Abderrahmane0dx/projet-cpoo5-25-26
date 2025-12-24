package fr.uparis.liquidwar.util;

import fr.uparis.liquidwar.model.Position;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Direction enum.
 */
class DirectionTest {
    
    @Test
    @DisplayName("Direction offsets are correct")
    void testDirectionOffsets() {
        assertEquals(0, Direction.NORTH.getDx());
        assertEquals(-1, Direction.NORTH.getDy());
        
        assertEquals(1, Direction.EAST.getDx());
        assertEquals(0, Direction.EAST.getDy());
        
        assertEquals(0, Direction.SOUTH.getDx());
        assertEquals(1, Direction.SOUTH.getDy());
        
        assertEquals(-1, Direction.WEST.getDx());
        assertEquals(0, Direction.WEST.getDy());
    }
    
    @Test
    @DisplayName("Apply direction to coordinates works")
    void testApplyToCoordinates() {
        Position result = Direction.NORTH.apply(5, 5);
        assertEquals(new Position(5, 4), result);
        
        result = Direction.EAST.apply(5, 5);
        assertEquals(new Position(6, 5), result);
        
        result = Direction.SOUTH.apply(5, 5);
        assertEquals(new Position(5, 6), result);
        
        result = Direction.WEST.apply(5, 5);
        assertEquals(new Position(4, 5), result);
    }
    
    @Test
    @DisplayName("Apply direction to Position works")
    void testApplyToPosition() {
        Position start = new Position(10, 10);
        
        Position north = Direction.NORTH.apply(start);
        assertEquals(new Position(10, 9), north);
        
        Position northEast = Direction.NORTH_EAST.apply(start);
        assertEquals(new Position(11, 9), northEast);
    }
    
    @Test
    @DisplayName("Diagonal directions are identified correctly")
    void testIsDiagonal() {
        assertTrue(Direction.NORTH_EAST.isDiagonal());
        assertTrue(Direction.SOUTH_EAST.isDiagonal());
        assertTrue(Direction.SOUTH_WEST.isDiagonal());
        assertTrue(Direction.NORTH_WEST.isDiagonal());
        
        assertFalse(Direction.NORTH.isDiagonal());
        assertFalse(Direction.EAST.isDiagonal());
        assertFalse(Direction.SOUTH.isDiagonal());
        assertFalse(Direction.WEST.isDiagonal());
    }
    
    @Test
    @DisplayName("Cardinal directions are identified correctly")
    void testIsCardinal() {
        assertTrue(Direction.NORTH.isCardinal());
        assertTrue(Direction.EAST.isCardinal());
        assertTrue(Direction.SOUTH.isCardinal());
        assertTrue(Direction.WEST.isCardinal());
        
        assertFalse(Direction.NORTH_EAST.isCardinal());
        assertFalse(Direction.SOUTH_EAST.isCardinal());
        assertFalse(Direction.SOUTH_WEST.isCardinal());
        assertFalse(Direction.NORTH_WEST.isCardinal());
    }
    
    @Test
    @DisplayName("Opposite directions are correct")
    void testOpposite() {
        assertEquals(Direction.SOUTH, Direction.NORTH.opposite());
        assertEquals(Direction.WEST, Direction.EAST.opposite());
        assertEquals(Direction.NORTH, Direction.SOUTH.opposite());
        assertEquals(Direction.EAST, Direction.WEST.opposite());
        
        assertEquals(Direction.SOUTH_WEST, Direction.NORTH_EAST.opposite());
        assertEquals(Direction.NORTH_WEST, Direction.SOUTH_EAST.opposite());
        assertEquals(Direction.SOUTH_EAST, Direction.NORTH_WEST.opposite());
        assertEquals(Direction.NORTH_EAST, Direction.SOUTH_WEST.opposite());
    }
    
    @Test
    @DisplayName("All 8 directions exist")
    void testAllDirectionsExist() {
        Direction[] allDirections = Direction.values();
        assertEquals(8, allDirections.length);
    }
    
    @Test
    @DisplayName("Applying opposite direction returns to start")
    void testOppositeReturnsToStart() {
        Position start = new Position(10, 10);
        
        for (Direction dir : Direction.values()) {
            Position moved = dir.apply(start);
            Position back = dir.opposite().apply(moved);
            assertEquals(start, back, "Failed for direction: " + dir);
        }
    }
}