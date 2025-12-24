package fr.uparis.liquidwar.model;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Particle class.
 */
class ParticleTest {
    
    private Team team1;
    private Team team2;
    private Position pos1;
    private Position pos2;
    
    @BeforeEach
    void setUp() {
        pos1 = new Position(10, 10);
        pos2 = new Position(20, 20);
        team1 = new Team(1, Color.RED, pos1);
        team2 = new Team(2, Color.BLUE, pos2);
    }
    
    @Test
    @DisplayName("Particle creation with default energy works")
    void testParticleCreation() {
        Particle p = new Particle(pos1, team1);
        
        assertEquals(pos1, p.getPosition());
        assertEquals(team1, p.getTeam());
        assertEquals(Particle.DEFAULT_ENERGY, p.getEnergy());
    }
    
    @Test
    @DisplayName("Particle creation with custom energy works")
    void testParticleCreationWithEnergy() {
        Particle p = new Particle(pos1, team1, 75);
        
        assertEquals(75, p.getEnergy());
    }
    
    @Test
    @DisplayName("Energy is clamped to valid range")
    void testEnergyClamping() {
        Particle p = new Particle(pos1, team1, 200);
        assertEquals(Particle.MAX_ENERGY, p.getEnergy());
        
        p.setEnergy(-50);
        assertEquals(Particle.MIN_ENERGY, p.getEnergy());
    }
    
    @Test
    @DisplayName("Setting position works")
    void testSetPosition() {
        Particle p = new Particle(pos1, team1);
        p.setPosition(pos2);
        
        assertEquals(pos2, p.getPosition());
    }
    
    @Test
    @DisplayName("Attack transfers energy correctly")
    void testAttack() {
        Particle attacker = new Particle(pos1, team1, 50);
        Particle target = new Particle(pos2, team2, 50);
        
        boolean converted = attacker.attack(target);
        
        assertFalse(converted); // Target shouldn't die yet
        assertEquals(60, attacker.getEnergy()); // Gained 10
        assertEquals(40, target.getEnergy());   // Lost 10
    }
    
    @Test
    @DisplayName("Attack converts particle when energy drops to zero")
    void testAttackConversion() {
        Particle attacker = new Particle(pos1, team1, 50);
        Particle target = new Particle(pos2, team2, 5); // Low energy
        
        boolean converted = attacker.attack(target);
        
        assertTrue(converted);
        assertEquals(team1, target.getTeam()); // Converted to attacker's team
        assertEquals(Particle.DEFAULT_ENERGY, target.getEnergy()); // Reset energy
    }
    
    @Test
    @DisplayName("Cannot attack ally")
    void testCannotAttackAlly() {
        Particle p1 = new Particle(pos1, team1);
        Particle p2 = new Particle(pos2, team1); // Same team
        
        int initialEnergy1 = p1.getEnergy();
        int initialEnergy2 = p2.getEnergy();
        
        boolean result = p1.attack(p2);
        
        assertFalse(result);
        assertEquals(initialEnergy1, p1.getEnergy());
        assertEquals(initialEnergy2, p2.getEnergy());
    }
    
    @Test
    @DisplayName("Heal transfers energy correctly")
    void testHeal() {
        Particle healer = new Particle(pos1, team1, 50);
        Particle ally = new Particle(pos2, team1, 30);
        
        boolean healed = healer.heal(ally);
        
        assertTrue(healed);
        assertEquals(40, healer.getEnergy()); // Lost 10
        assertEquals(40, ally.getEnergy());   // Gained 10
    }
    
    @Test
    @DisplayName("Cannot heal when at minimum energy")
    void testCannotHealAtMinimum() {
        Particle healer = new Particle(pos1, team1, Particle.MIN_ENERGY);
        Particle ally = new Particle(pos2, team1, 50);
        
        boolean healed = healer.heal(ally);
        
        assertFalse(healed);
    }
    
    @Test
    @DisplayName("Cannot heal enemy")
    void testCannotHealEnemy() {
        Particle p1 = new Particle(pos1, team1);
        Particle p2 = new Particle(pos2, team2); // Different team
        
        boolean healed = p1.heal(p2);
        
        assertFalse(healed);
    }
    
    @Test
    @DisplayName("isDead returns true when energy is zero or below")
    void testIsDead() {
        Particle p = new Particle(pos1, team1, 1);
        assertFalse(p.isDead());
        
        p.setEnergy(0);
        assertTrue(p.isDead());
    }
    
    @Test
    @DisplayName("Energy ratio calculation is correct")
    void testEnergyRatio() {
        Particle p = new Particle(pos1, team1, 50);
        assertEquals(0.5, p.getEnergyRatio(), 0.01);
        
        p.setEnergy(100);
        assertEquals(1.0, p.getEnergyRatio(), 0.01);
        
        p.setEnergy(25);
        assertEquals(0.25, p.getEnergyRatio(), 0.01);
    }
    
    @Test
    @DisplayName("Team conversion works correctly")
    void testTeamConversion() {
        Particle p = new Particle(pos1, team1);
        team1.addParticle(p);
        
        assertEquals(1, team1.getParticleCount());
        assertEquals(0, team2.getParticleCount());
        
        p.setTeam(team2);
        
        assertEquals(team2, p.getTeam());
        assertEquals(0, team1.getParticleCount());
        assertEquals(1, team2.getParticleCount());
    }
    
    @Test
    @DisplayName("Null checks throw exceptions")
    void testNullChecks() {
        assertThrows(IllegalArgumentException.class, 
            () -> new Particle(null, team1));
        assertThrows(IllegalArgumentException.class, 
            () -> new Particle(pos1, null));
    }
}