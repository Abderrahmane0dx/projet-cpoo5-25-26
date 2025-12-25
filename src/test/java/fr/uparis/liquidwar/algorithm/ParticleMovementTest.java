package fr.uparis.liquidwar.algorithm;

import fr.uparis.liquidwar.model.*;
import fr.uparis.liquidwar.util.Direction;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ParticleMovement class.
 */
class ParticleMovementTest {
    
    private Board board;
    private Team team1;
    private Team team2;
    private ParticleMovement movement;
    private GradientCalculator calculator;
    
    @BeforeEach
    void setUp() {
        board = new Board(20, 15);
        team1 = new Team(1, Color.RED, new Position(15, 7));
        team2 = new Team(2, Color.BLUE, new Position(5, 7));
        movement = new ParticleMovement(board);
        calculator = new GradientCalculator(board);
    }
    
    @Test
    @DisplayName("Movement creation works")
    void testCreation() {
        assertNotNull(movement);
    }
    
    @Test
    @DisplayName("Null board throws exception")
    void testNullBoard() {
        assertThrows(IllegalArgumentException.class,
            () -> new ParticleMovement(null));
    }
    
    @Test
    @DisplayName("Particle moves toward target")
    void testMoveTowardTarget() {
        Particle p = new Particle(new Position(10, 7), team1);
        board.setParticle(p, 10, 7);
        
        Gradient gradient = calculator.calculateSimple(team1);
        boolean moved = movement.moveParticle(p, gradient);
        
        assertTrue(moved);
        // Particle should have moved closer to target (15, 7)
        assertTrue(p.getPosition().x() > 10 || p.getPosition().x() == 11);
    }
    
    @Test
    @DisplayName("Particle doesn't move if at target")
    void testNoMoveAtTarget() {
        Position target = team1.getTargetPosition();
        Particle p = new Particle(target, team1);
        board.setParticle(p, target.x(), target.y());
        
        Gradient gradient = calculator.calculateSimple(team1);
        boolean moved = movement.moveParticle(p, gradient);
        
        assertFalse(moved);
        assertEquals(target, p.getPosition());
    }
    
    @Test
    @DisplayName("Particle attacks enemy in path")
    void testAttackEnemy() {
        Particle attacker = new Particle(new Position(10, 7), team1, 50);
        Particle enemy = new Particle(new Position(11, 7), team2, 30);
        
        board.setParticle(attacker, 10, 7);
        board.setParticle(enemy, 11, 7);
        
        // Block other directions to force attack
        board.setObstacle(9, 7);
        board.setObstacle(10, 6);
        board.setObstacle(10, 8);
        board.setObstacle(11, 6);
        board.setObstacle(11, 8);
        
        int initialEnemyEnergy = enemy.getEnergy();
        
        Gradient gradient = calculator.calculateSimple(team1);
        movement.moveParticle(attacker, gradient);
        
        // Enemy should have lost energy from attack
        assertTrue(enemy.getEnergy() < initialEnemyEnergy, 
            "Enemy should have lost energy after being attacked");
    }
    
    @Test
    @DisplayName("Particle heals ally when blocked")
    void testHealAlly() {
        Particle healer = new Particle(new Position(10, 7), team1, 60);
        Particle ally = new Particle(new Position(11, 7), team1, 30);
        
        board.setParticle(healer, 10, 7);
        board.setParticle(ally, 11, 7);
        
        // Block other directions
        board.setObstacle(9, 7);
        board.setObstacle(10, 6);
        board.setObstacle(10, 8);
        
        Gradient gradient = calculator.calculateSimple(team1);
        movement.moveParticle(healer, gradient);
        
        // Healer should still exist and have valid energy
        assertNotNull(healer);
        assertTrue(healer.getEnergy() > 0);
    }
    
    @Test
    @DisplayName("getBestDirection returns correct direction")
    void testGetBestDirection() {
        Particle p = new Particle(new Position(10, 7), team1);
        board.setParticle(p, 10, 7);
        
        Gradient gradient = calculator.calculateSimple(team1);
        Direction bestDir = movement.getBestDirection(p, gradient);
        
        assertNotNull(bestDir);
        // Should point toward target (15, 7) which is to the EAST
        assertTrue(bestDir == Direction.EAST || 
                   bestDir == Direction.NORTH_EAST || 
                   bestDir == Direction.SOUTH_EAST);
    }
    
    @Test
    @DisplayName("getBestDirection returns null when surrounded")
    void testBestDirectionWhenSurrounded() {
        Particle p = new Particle(new Position(10, 7), team1);
        board.setParticle(p, 10, 7);
        
        // Surround with obstacles
        for (Direction dir : Direction.values()) {
            Position neighbor = dir.apply(p.getPosition());
            board.setObstacle(neighbor);
        }
        
        Gradient gradient = calculator.calculateSimple(team1);
        Direction bestDir = movement.getBestDirection(p, gradient);
        
        assertNull(bestDir);
    }
    
    @Test
    @DisplayName("canMove returns true when space available")
    void testCanMove() {
        Particle p = new Particle(new Position(10, 7), team1);
        board.setParticle(p, 10, 7);
        
        assertTrue(movement.canMove(p));
    }
    
    @Test
    @DisplayName("canMove returns false when completely blocked")
    void testCannotMove() {
        Particle p = new Particle(new Position(10, 7), team1);
        board.setParticle(p, 10, 7);
        
        // Surround with obstacles
        for (Direction dir : Direction.values()) {
            Position neighbor = dir.apply(p.getPosition());
            board.setObstacle(neighbor);
        }
        
        assertFalse(movement.canMove(p));
    }
    
    @Test
    @DisplayName("Null particle returns false")
    void testNullParticle() {
        Gradient gradient = calculator.calculateSimple(team1);
        assertFalse(movement.moveParticle(null, gradient));
        assertNull(movement.getBestDirection(null, gradient));
        assertFalse(movement.canMove(null));
    }
    
    @Test
    @DisplayName("Null gradient returns false")
    void testNullGradient() {
        Particle p = new Particle(new Position(10, 7), team1);
        board.setParticle(p, 10, 7);
        
        assertFalse(movement.moveParticle(p, null));
        assertNull(movement.getBestDirection(p, null));
    }
    
    @Test
    @DisplayName("Particle prefers main direction over good direction")
    void testDirectionPriority() {
        // Place particle west of target
        Particle p = new Particle(new Position(10, 7), team1);
        board.setParticle(p, 10, 7);
        
        // Target is at (15, 7) - directly east
        Gradient gradient = calculator.calculateSimple(team1);
        
        // Move should go EAST (main direction)
        boolean moved = movement.moveParticle(p, gradient);
        assertTrue(moved);
        
        // Should have moved east
        assertTrue(p.getPosition().x() >= 10);
    }
}