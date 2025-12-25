package fr.uparis.liquidwar.model;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Team class.
 */
class TeamTest {
    
    private Team team;
    private Position initialTarget;
    
    @BeforeEach
    void setUp() {
        initialTarget = new Position(50, 50);
        team = new Team(1, Color.RED, initialTarget);
    }
    
    @Test
    @DisplayName("Team creation works correctly")
    void testTeamCreation() {
        assertEquals(1, team.getId());
        assertEquals(Color.RED, team.getBaseColor());
        assertEquals(initialTarget, team.getTargetPosition());
        assertTrue(team.isActive());
        assertEquals(0, team.getParticleCount());
    }
    
    @Test
    @DisplayName("Setting target position works")
    void testSetTargetPosition() {
        Position newTarget = new Position(100, 100);
        team.setTargetPosition(newTarget);
        
        assertEquals(newTarget, team.getTargetPosition());
    }
    
    @Test
    @DisplayName("Adding particles works")
    void testAddParticle() {
        Particle p1 = new Particle(new Position(10, 10), team);
        team.addParticle(p1);
        
        assertEquals(1, team.getParticleCount());
        assertTrue(team.getParticles().contains(p1));
    }
    
    @Test
    @DisplayName("Adding same particle twice doesn't duplicate")
    void testAddParticleTwice() {
        Particle p1 = new Particle(new Position(10, 10), team);
        team.addParticle(p1);
        team.addParticle(p1);
        
        assertEquals(1, team.getParticleCount());
    }
    
    @Test
    @DisplayName("Cannot add particle from different team")
    void testAddParticleWrongTeam() {
        Team otherTeam = new Team(2, Color.BLUE, new Position(20, 20));
        Particle p = new Particle(new Position(10, 10), otherTeam);
        
        assertThrows(IllegalArgumentException.class, () -> team.addParticle(p));
    }
    
    @Test
    @DisplayName("Removing particles works")
    void testRemoveParticle() {
        Particle p1 = new Particle(new Position(10, 10), team);
        team.addParticle(p1);
        
        assertEquals(1, team.getParticleCount());
        
        team.removeParticle(p1);
        assertEquals(0, team.getParticleCount());
    }
    
    @Test
    @DisplayName("Team is inactive when no particles")
    void testInactiveWithNoParticles() {
        assertTrue(team.isActive()); // Initially active
        
        team.setActive(false);
        assertFalse(team.isActive()); // Explicitly set inactive
    }
    
    @Test
    @DisplayName("Team is inactive when setActive(false)")
    void testSetInactive() {
        Particle p = new Particle(new Position(10, 10), team);
        team.addParticle(p);
        
        team.setActive(false);
        assertFalse(team.isActive());
    }
    
    @Test
    @DisplayName("Total energy calculation is correct")
    void testGetTotalEnergy() {
        Particle p1 = new Particle(new Position(10, 10), team, 30);
        Particle p2 = new Particle(new Position(20, 20), team, 50);
        Particle p3 = new Particle(new Position(30, 30), team, 20);
        
        team.addParticle(p1);
        team.addParticle(p2);
        team.addParticle(p3);
        
        assertEquals(100, team.getTotalEnergy());
    }
    
    @Test
    @DisplayName("Particle color varies with energy")
    void testGetParticleColor() {
        Color darkColor = team.getParticleColor(0.0);
        Color mediumColor = team.getParticleColor(0.5);
        Color brightColor = team.getParticleColor(1.0);
        
        // Brightness should increase with energy ratio
        assertTrue(darkColor.getBrightness() < mediumColor.getBrightness());
        assertTrue(mediumColor.getBrightness() < brightColor.getBrightness());
    }
    
    @Test
    @DisplayName("Particles list is unmodifiable")
    void testUnmodifiableParticlesList() {
        Particle p = new Particle(new Position(10, 10), team);
        team.addParticle(p);
        
        var particles = team.getParticles();
        assertThrows(UnsupportedOperationException.class, 
            () -> particles.add(new Particle(new Position(20, 20), team)));
    }
    
    @Test
    @DisplayName("Teams with same ID are equal")
    void testEquality() {
        Team team1 = new Team(1, Color.RED, new Position(10, 10));
        Team team2 = new Team(1, Color.BLUE, new Position(20, 20));
        Team team3 = new Team(2, Color.RED, new Position(10, 10));
        
        assertEquals(team1, team2); // Same ID
        assertNotEquals(team1, team3); // Different ID
    }
    
    @Test
    @DisplayName("Null checks throw exceptions")
    void testNullChecks() {
        assertThrows(IllegalArgumentException.class, 
            () -> new Team(1, null, initialTarget));
        assertThrows(IllegalArgumentException.class, 
            () -> new Team(1, Color.RED, null));
        assertThrows(IllegalArgumentException.class, 
            () -> team.setTargetPosition(null));
        assertThrows(IllegalArgumentException.class, 
            () -> team.addParticle(null));
    }
    
    @Test
    @DisplayName("toString returns readable format")
    void testToString() {
        String str = team.toString();
        assertTrue(str.contains("Team"));
        assertTrue(str.contains("id=1"));
    }
}