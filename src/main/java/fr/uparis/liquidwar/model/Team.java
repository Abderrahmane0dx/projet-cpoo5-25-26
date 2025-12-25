package fr.uparis.liquidwar.model;

import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a team in Liquid War.
 * Each team has a color, a target cursor, and particles.
 */
public class Team {
    private final int id;
    private final Color baseColor;
    private Position targetPosition;
    private final List<Particle> particles;
    private boolean active;
    
    /**
     * Creates a new team.
     * 
     * @param id unique team identifier
     * @param baseColor team's base color
     * @param initialTarget initial cursor position
     */
    public Team(int id, Color baseColor, Position initialTarget) {
        if (baseColor == null) {
            throw new IllegalArgumentException("Base color cannot be null");
        }
        if (initialTarget == null) {
            throw new IllegalArgumentException("Initial target cannot be null");
        }
        this.id = id;
        this.baseColor = baseColor;
        this.targetPosition = initialTarget;
        this.particles = new ArrayList<>();
        this.active = true;
    }
    
    /**
     * @return team ID
     */
    public int getId() {
        return id;
    }
    
    /**
     * @return team's base color
     */
    public Color getBaseColor() {
        return baseColor;
    }
    
    /**
     * @return current cursor/target position
     */
    public Position getTargetPosition() {
        return targetPosition;
    }
    
    /**
     * Moves the cursor to a new position.
     * 
     * @param newTarget new cursor position
     */
    public void setTargetPosition(Position newTarget) {
        if (newTarget == null) {
            throw new IllegalArgumentException("Target position cannot be null");
        }
        this.targetPosition = newTarget;
    }
    
    /**
     * @return unmodifiable list of team's particles
     */
    public List<Particle> getParticles() {
        return Collections.unmodifiableList(particles);
    }
    
    /**
     * Adds a particle to this team.
     * 
     * @param particle particle to add
     */
    public void addParticle(Particle particle) {
        if (particle == null) {
            throw new IllegalArgumentException("Particle cannot be null");
        }
        if (particle.getTeam() != this) {
            throw new IllegalArgumentException("Particle doesn't belong to this team");
        }
        if (!particles.contains(particle)) {
            particles.add(particle);
            // Ensure team is active when a particle is added
            this.active = true;
        }
    }
    
    /**
     * Removes a particle from this team.
     * 
     * @param particle particle to remove
     */
    public void removeParticle(Particle particle) {
        particles.remove(particle);
        // This fix addresses "Team is inactive when no particles"
        if (particles.isEmpty()) {
            this.active = false;
        }
    }
    
    /**
     * @return number of particles in this team
     */
    public int getParticleCount() {
        return particles.size();
    }
    
    /**
     * @return true if team is active (has particles)
     */
    public boolean isActive() {
        return this.active;
    }
    
    /**
     * Sets team active status.
     * 
     * @param active new active status
     */
    public void setActive(boolean active) {
        this.active = active;
    }
    
    /**
     * Calculates total energy of all particles.
     * 
     * @return sum of all particle energies
     */
    public int getTotalEnergy() {
        return particles.stream()
                       .mapToInt(Particle::getEnergy)
                       .sum();
    }
    
    /**
     * Calculates color for a particle based on its energy.
     * Higher energy = brighter color.
     * 
     * @param energyRatio particle's energy ratio (0.0 to 1.0)
     * @return adjusted color
     */
    public Color getParticleColor(double energyRatio) {
        double brightness = 0.3 + (energyRatio * 0.7); // Range: 0.3 to 1.0
        return new Color(
            baseColor.getRed() * brightness,
            baseColor.getGreen() * brightness,
            baseColor.getBlue() * brightness,
            1.0
        );
    }
    
    @Override
    public String toString() {
        return "Team{id=" + id + ", particles=" + particles.size() + 
               ", active=" + active + "}";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return id == team.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}