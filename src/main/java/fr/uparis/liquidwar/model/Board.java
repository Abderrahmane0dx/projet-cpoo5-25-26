package fr.uparis.liquidwar.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the game board (map) with obstacles and particles.
 */
public class Board {
    private final int width;
    private final int height;
    private final boolean[][] obstacles;
    private final Particle[][] particles;
    
    /**
     * Creates an empty board.
     * 
     * @param width board width
     * @param height board height
     */
    public Board(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Board dimensions must be positive");
        }
        this.width = width;
        this.height = height;
        this.obstacles = new boolean[height][width];
        this.particles = new Particle[height][width];
    }
    
    /**
     * @return board width
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * @return board height
     */
    public int getHeight() {
        return height;
    }
    
    /**
     * Checks if position is within board bounds.
     * 
     * @param x x coordinate
     * @param y y coordinate
     * @return true if in bounds
     */
    public boolean isInBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
    
    /**
     * Checks if position is within board bounds.
     * 
     * @param pos position to check
     * @return true if in bounds
     */
    public boolean isInBounds(Position pos) {
        return isInBounds(pos.x(), pos.y());
    }
    
    /**
     * Checks if a cell contains an obstacle.
     * 
     * @param x x coordinate
     * @param y y coordinate
     * @return true if obstacle, or if out of bounds
     */
    public boolean isObstacle(int x, int y) {
        if (!isInBounds(x, y)) return true;
        return obstacles[y][x];
    }
    
    /**
     * Checks if position contains an obstacle.
     * 
     * @param pos position to check
     * @return true if obstacle
     */
    public boolean isObstacle(Position pos) {
        return isObstacle(pos.x(), pos.y());
    }
    
    /**
     * Places an obstacle at given position.
     * 
     * @param x x coordinate
     * @param y y coordinate
     */
    public void setObstacle(int x, int y) {
        if (isInBounds(x, y)) {
            obstacles[y][x] = true;
        }
    }
    
    /**
     * Places an obstacle at given position.
     * 
     * @param pos position
     */
    public void setObstacle(Position pos) {
        setObstacle(pos.x(), pos.y());
    }
    
    /**
     * Removes obstacle at given position.
     * 
     * @param x x coordinate
     * @param y y coordinate
     */
    public void clearObstacle(int x, int y) {
        if (isInBounds(x, y)) {
            obstacles[y][x] = false;
        }
    }
    
    /**
     * Gets particle at given position.
     * 
     * @param x x coordinate
     * @param y y coordinate
     * @return particle, or null if empty/out of bounds
     */
    public Particle getParticle(int x, int y) {
        if (!isInBounds(x, y)) return null;
        return particles[y][x];
    }
    
    /**
     * Gets particle at given position.
     * 
     * @param pos position
     * @return particle, or null if empty
     */
    public Particle getParticle(Position pos) {
        return getParticle(pos.x(), pos.y());
    }
    
    /**
     * Places a particle at given position.
     * 
     * @param particle particle to place (can be null to clear)
     * @param x x coordinate
     * @param y y coordinate
     */
    public void setParticle(Particle particle, int x, int y) {
        if (!isInBounds(x, y)) return;
        particles[y][x] = particle;
        if (particle != null) {
            particle.setPosition(new Position(x, y));
        }
    }
    
    /**
     * Places a particle at given position.
     * 
     * @param particle particle to place
     * @param pos position
     */
    public void setParticle(Particle particle, Position pos) {
        setParticle(particle, pos.x(), pos.y());
    }
    
    /**
     * Moves a particle from one position to another.
     * 
     * @param from source position
     * @param to destination position
     * @return true if move succeeded
     */
    public boolean moveParticle(Position from, Position to) {
        if (!isInBounds(from) || !isInBounds(to)) return false;
        if (isObstacle(to)) return false;
        
        Particle particle = getParticle(from);
        if (particle == null) return false;
        
        setParticle(null, from);
        setParticle(particle, to);
        return true;
    }
    
    /**
     * Checks if a position is free (no obstacle, no particle).
     * 
     * @param x x coordinate
     * @param y y coordinate
     * @return true if free
     */
    public boolean isFree(int x, int y) {
        return isInBounds(x, y) && !isObstacle(x, y) && getParticle(x, y) == null;
    }
    
    /**
     * Checks if a position is free.
     * 
     * @param pos position
     * @return true if free
     */
    public boolean isFree(Position pos) {
        return isFree(pos.x(), pos.y());
    }
    
    /**
     * Gets all particles on the board.
     * 
     * @return list of all particles
     */
    public List<Particle> getAllParticles() {
        List<Particle> result = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (particles[y][x] != null) {
                    result.add(particles[y][x]);
                }
            }
        }
        return result;
    }
    
    /**
     * Creates border walls around the board.
     */
    public void createBorderWalls() {
        for (int x = 0; x < width; x++) {
            setObstacle(x, 0);
            setObstacle(x, height - 1);
        }
        for (int y = 0; y < height; y++) {
            setObstacle(0, y);
            setObstacle(width - 1, y);
        }
    }
    
    /**
     * Creates random obstacles on the board.
     * 
     * @param density obstacle density (0.0 to 1.0)
     */
    public void createRandomObstacles(double density) {
        if (density < 0.0 || density > 1.0) {
            throw new IllegalArgumentException("Density must be between 0.0 and 1.0");
        }
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (Math.random() < density) {
                    setObstacle(x, y);
                }
            }
        }
    }
    
    @Override
    public String toString() {
        return "Board{" + width + "x" + height + 
               ", particles=" + getAllParticles().size() + "}";
    }
}