package fr.uparis.liquidwar.model;

/**
 * Represents a particle (pixel) in Liquid War.
 * Each particle belongs to a team and has energy.
 */
public class Particle {
    /** Minimum energy a particle can have */
    public static final int MIN_ENERGY = 1;
    /** Maximum energy a particle can have */
    public static final int MAX_ENERGY = 100;
    /** Default starting energy */
    public static final int DEFAULT_ENERGY = 50;
    /** Energy transfer amount per attack/heal */
    public static final int ENERGY_TRANSFER = 10;
    
    private Position position;
    private Team team;
    private int energy;
    
    /**
     * Creates a particle with default energy.
     * 
     * @param position initial position
     * @param team owning team
     */
    public Particle(Position position, Team team) {
        this(position, team, DEFAULT_ENERGY);
    }
    
    /**
     * Creates a particle with specified energy.
     * 
     * @param position initial position
     * @param team owning team
     * @param energy initial energy (clamped to MIN-MAX range)
     */
    public Particle(Position position, Team team, int energy) {
        if (position == null) {
            throw new IllegalArgumentException("Position cannot be null");
        }
        if (team == null) {
            throw new IllegalArgumentException("Team cannot be null");
        }
        this.position = position;
        this.team = team;
        this.energy = energy; // Don't clamp yet, allow checking isDead first
    }
    
    /**
     * @return current position
     */
    public Position getPosition() {
        return position;
    }
    
    /**
     * Moves particle to new position.
     * 
     * @param newPosition new position
     */
    public void setPosition(Position newPosition) {
        if (newPosition == null) {
            throw new IllegalArgumentException("Position cannot be null");
        }
        this.position = newPosition;
    }
    
    /**
     * @return owning team
     */
    public Team getTeam() {
        return team;
    }
    
    /**
     * Converts particle to another team (when killed).
     * Resets energy to default.
     * 
     * @param newTeam new owning team
     */
    public void setTeam(Team newTeam) {
        if (newTeam == null) {
            throw new IllegalArgumentException("Team cannot be null");
        }
        if (this.team != null) {
            this.team.removeParticle(this);
        }
        this.team = newTeam;
        this.energy = DEFAULT_ENERGY;
        newTeam.addParticle(this);
    }
    
    /**
     * @return current energy
     */
    public int getEnergy() {
        return energy;
    }
    
    /**
     * Sets energy. Values below MIN_ENERGY are allowed (for death checking).
     * 
     * @param energy new energy value
     */
    public void setEnergy(int energy) {
        // Clamp to MAX but allow going below MIN (for death)
        this.energy = Math.min(MAX_ENERGY, energy);
    }
    
    /**
     * Attacks another particle (steals energy).
     * If target dies (energy <= 0), it converts to attacker's team.
     * 
     * @param target particle to attack
     * @return true if target was converted (killed)
     */
    public boolean attack(Particle target) {
        if (target == null || target.getTeam() == this.team) {
            return false;
        }
        
        // Transfer energy
        int stolen = Math.min(ENERGY_TRANSFER, target.getEnergy());
        target.setEnergy(target.getEnergy() - stolen);
        
        // Cap attacker's energy at MAX
        this.setEnergy(Math.min(MAX_ENERGY, this.getEnergy() + stolen));
        
        // Check if target died
        if (target.isDead()) {
            target.setTeam(this.team);
            return true;
        }
        return false;
    }
    
    /**
     * Heals an ally (gives energy).
     * Only works if this particle has energy above minimum
     * and ally has energy below maximum.
     * 
     * @param ally particle to heal
     * @return true if energy was transferred
     */
    public boolean heal(Particle ally) {
        if (ally == null || ally.getTeam() != this.team) {
            return false;
        }
        
        // Only transfer if conditions are met
        if (this.energy > MIN_ENERGY && ally.energy < MAX_ENERGY) {
            int transfer = Math.min(ENERGY_TRANSFER, this.energy - MIN_ENERGY);
            transfer = Math.min(transfer, MAX_ENERGY - ally.energy);
            
            this.setEnergy(this.energy - transfer);
            ally.setEnergy(ally.energy + transfer);
            return true;
        }
        return false;
    }
    
    /**
     * @return true if particle is dead (energy <= 0)
     */
    public boolean isDead() {
        return energy <= 0;
    }
    
    /**
     * Calculates energy ratio for display purposes.
     * 
     * @return value between 0.0 and 1.0
     */
    public double getEnergyRatio() {
        return Math.max(0.0, Math.min(1.0, (double) energy / MAX_ENERGY));
    }
    
    @Override
    public String toString() {
        return "Particle{pos=" + position + ", team=" + team.getId() + 
               ", energy=" + energy + "}";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Particle particle = (Particle) o;
        return position.equals(particle.position);
    }
    
    @Override
    public int hashCode() {
        return position.hashCode();
    }
}