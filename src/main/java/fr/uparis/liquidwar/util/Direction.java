package fr.uparis.liquidwar.util;

import fr.uparis.liquidwar.model.Position;

/**
 * Enumeration of the 8 possible movement directions.
 * Represents the 8 neighbors of a cell on a grid.
 */
public enum Direction {
    NORTH(0, -1),
    NORTH_EAST(1, -1),
    EAST(1, 0),
    SOUTH_EAST(1, 1),
    SOUTH(0, 1),
    SOUTH_WEST(-1, 1),
    WEST(-1, 0),
    NORTH_WEST(-1, -1);
    
    private final int dx;
    private final int dy;
    
    /**
     * Constructor for a direction.
     * 
     * @param dx horizontal displacement
     * @param dy vertical displacement
     */
    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }
    
    /**
     * @return horizontal displacement of this direction
     */
    public int getDx() {
        return dx;
    }
    
    /**
     * @return vertical displacement of this direction
     */
    public int getDy() {
        return dy;
    }
    
    /**
     * Applies this direction to coordinates.
     * 
     * @param x starting x coordinate
     * @param y starting y coordinate
     * @return new Position after applying direction
     */
    public Position apply(int x, int y) {
        return new Position(x + dx, y + dy);
    }
    
    /**
     * Applies this direction to a position.
     * 
     * @param pos starting position
     * @return new Position after applying direction
     */
    public Position apply(Position pos) {
        return new Position(pos.x() + dx, pos.y() + dy);
    }
    
    /**
     * Checks if this direction is diagonal.
     * 
     * @return true if diagonal (both dx and dy are non-zero)
     */
    public boolean isDiagonal() {
        return dx != 0 && dy != 0;
    }
    
    /**
     * Checks if this direction is cardinal (N, S, E, W).
     * 
     * @return true if cardinal (only one of dx or dy is non-zero)
     */
    public boolean isCardinal() {
        return !isDiagonal();
    }
    
    /**
     * Gets the opposite direction.
     * 
     * @return opposite direction
     */
    public Direction opposite() {
        return switch (this) {
            case NORTH -> SOUTH;
            case NORTH_EAST -> SOUTH_WEST;
            case EAST -> WEST;
            case SOUTH_EAST -> NORTH_WEST;
            case SOUTH -> NORTH;
            case SOUTH_WEST -> NORTH_EAST;
            case WEST -> EAST;
            case NORTH_WEST -> SOUTH_EAST;
        };
    }
}