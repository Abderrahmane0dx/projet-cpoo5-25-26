package fr.uparis.liquidwar.model;

/**
 * Represents a gradient (distance map) for a team.
 * Stores the distance from each cell to the team's target.
 */
public class Gradient {
    /** Value representing infinite distance (obstacle or unreachable) */
    public static final int INFINITE = Integer.MAX_VALUE;
    
    private final int width;
    private final int height;
    private final int[][] distances;
    private final Team team;
    
    /**
     * Creates a gradient for a team.
     * All distances are initialized to INFINITE.
     * 
     * @param width board width
     * @param height board height
     * @param team associated team
     */
    public Gradient(int width, int height, Team team) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Gradient dimensions must be positive");
        }
        if (team == null) {
            throw new IllegalArgumentException("Team cannot be null");
        }
        this.width = width;
        this.height = height;
        this.team = team;
        this.distances = new int[height][width];
        reset();
    }
    
    /**
     * @return associated team
     */
    public Team getTeam() {
        return team;
    }
    
    /**
     * @return gradient width
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * @return gradient height
     */
    public int getHeight() {
        return height;
    }
    
    /**
     * Gets distance at given coordinates.
     * 
     * @param x x coordinate
     * @param y y coordinate
     * @return distance, or INFINITE if out of bounds
     */
    public int getDistance(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return INFINITE;
        }
        return distances[y][x];
    }
    
    /**
     * Gets distance at given position.
     * 
     * @param pos position
     * @return distance
     */
    public int getDistance(Position pos) {
        return getDistance(pos.x(), pos.y());
    }
    
    /**
     * Sets distance at given coordinates.
     * 
     * @param x x coordinate
     * @param y y coordinate
     * @param distance new distance value
     */
    public void setDistance(int x, int y, int distance) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            distances[y][x] = distance;
        }
    }
    
    /**
     * Sets distance at given position.
     * 
     * @param pos position
     * @param distance new distance value
     */
    public void setDistance(Position pos, int distance) {
        setDistance(pos.x(), pos.y(), distance);
    }
    
    /**
     * Resets all distances to INFINITE.
     */
    public void reset() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                distances[y][x] = INFINITE;
            }
        }
    }
    
    /**
     * Copies distances from another gradient.
     * 
     * @param other source gradient
     */
    public void copyFrom(Gradient other) {
        if (other.width != this.width || other.height != this.height) {
            throw new IllegalArgumentException("Incompatible gradient dimensions");
        }
        for (int y = 0; y < height; y++) {
            System.arraycopy(other.distances[y], 0, this.distances[y], 0, width);
        }
    }
    
    /**
     * Checks if a position is reachable (distance < INFINITE).
     * 
     * @param x x coordinate
     * @param y y coordinate
     * @return true if reachable
     */
    public boolean isReachable(int x, int y) {
        return getDistance(x, y) < INFINITE;
    }
    
    /**
     * Checks if a position is reachable.
     * 
     * @param pos position
     * @return true if reachable
     */
    public boolean isReachable(Position pos) {
        return isReachable(pos.x(), pos.y());
    }
    
    @Override
    public String toString() {
        return "Gradient{team=" + team.getId() + ", " + width + "x" + height + "}";
    }
}