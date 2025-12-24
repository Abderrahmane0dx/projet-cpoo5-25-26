package fr.uparis.liquidwar.model;

/**
 * Represents a position on the game board.
 * Uses Java record for immutability and automatic methods.
 * 
 * @param x horizontal coordinate
 * @param y vertical coordinate
 */
public record Position(int x, int y) {
    
    /**
     * Calculates Manhattan distance between two positions.
     * Manhattan distance = |x1 - x2| + |y1 - y2|
     * 
     * @param other the other position
     * @return Manhattan distance
     */
    public int manhattanDistance(Position other) {
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }
    
    /**
     * Creates a new position by adding offsets.
     * 
     * @param dx horizontal offset
     * @param dy vertical offset
     * @return new position with added offsets
     */
    public Position add(int dx, int dy) {
        return new Position(x + dx, y + dy);
    }
    
    /**
     * Checks if this position is within given bounds.
     * 
     * @param width maximum width (exclusive)
     * @param height maximum height (exclusive)
     * @return true if position is within bounds
     */
    public boolean isInBounds(int width, int height) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
    
    /**
     * Calculates Euclidean distance between two positions.
     * 
     * @param other the other position
     * @return Euclidean distance
     */
    public double euclideanDistance(Position other) {
        int dx = this.x - other.x;
        int dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}