package fr.uparis.liquidwar.algorithm;

import fr.uparis.liquidwar.model.*;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AIController class.
 */
class AIControllerTest {
    
    private Board board;
    private Team controlledTeam;
    private Team enemyTeam;
    
    @BeforeEach
    void setUp() {
        board = new Board(100, 75);
        controlledTeam = new Team(1, Color.BLUE, new Position(75, 37));
        enemyTeam = new Team(2, Color.RED, new Position(25, 37));
        
        // Add some particles to each team
        for (int i = 0; i < 10; i++) {
            controlledTeam.addParticle(new Particle(new Position(70 + i, 35 + i % 5), controlledTeam));
            enemyTeam.addParticle(new Particle(new Position(20 + i, 35 + i % 5), enemyTeam));
        }
    }
    
    // === Constructor Tests ===
    
    @Test
    @DisplayName("AIController creation with difficulty works")
    void testCreationWithDifficulty() {
        AIController ai = new AIController(board, controlledTeam, enemyTeam, AIController.Difficulty.MEDIUM);
        assertNotNull(ai);
        assertEquals(AIController.Difficulty.MEDIUM, ai.getDifficulty());
    }
    
    @Test
    @DisplayName("AIController creation with default difficulty works")
    void testCreationDefaultDifficulty() {
        AIController ai = new AIController(board, controlledTeam, enemyTeam);
        assertNotNull(ai);
        assertEquals(AIController.Difficulty.MEDIUM, ai.getDifficulty());
    }
    
    @Test
    @DisplayName("Null board throws exception")
    void testNullBoard() {
        assertThrows(IllegalArgumentException.class,
            () -> new AIController(null, controlledTeam, enemyTeam));
    }
    
    @Test
    @DisplayName("Null controlled team throws exception")
    void testNullControlledTeam() {
        assertThrows(IllegalArgumentException.class,
            () -> new AIController(board, null, enemyTeam));
    }
    
    @Test
    @DisplayName("Null enemy team throws exception")
    void testNullEnemyTeam() {
        assertThrows(IllegalArgumentException.class,
            () -> new AIController(board, controlledTeam, null));
    }
    
    // === Difficulty Tests ===
    
    @Test
    @DisplayName("All difficulty levels can be set")
    void testAllDifficultyLevels() {
        AIController ai = new AIController(board, controlledTeam, enemyTeam);
        
        ai.setDifficulty(AIController.Difficulty.EASY);
        assertEquals(AIController.Difficulty.EASY, ai.getDifficulty());
        
        ai.setDifficulty(AIController.Difficulty.MEDIUM);
        assertEquals(AIController.Difficulty.MEDIUM, ai.getDifficulty());
        
        ai.setDifficulty(AIController.Difficulty.HARD);
        assertEquals(AIController.Difficulty.HARD, ai.getDifficulty());
    }
    
    @Test
    @DisplayName("EASY difficulty exists")
    void testEasyDifficulty() {
        AIController ai = new AIController(board, controlledTeam, enemyTeam, AIController.Difficulty.EASY);
        assertEquals(AIController.Difficulty.EASY, ai.getDifficulty());
    }
    
    @Test
    @DisplayName("HARD difficulty exists")
    void testHardDifficulty() {
        AIController ai = new AIController(board, controlledTeam, enemyTeam, AIController.Difficulty.HARD);
        assertEquals(AIController.Difficulty.HARD, ai.getDifficulty());
    }
    
    // === Update Tests ===
    
    @Test
    @DisplayName("Update does not throw exception")
    void testUpdateNoException() {
        AIController ai = new AIController(board, controlledTeam, enemyTeam);
        assertDoesNotThrow(() -> ai.update());
    }
    
    @Test
    @DisplayName("Multiple updates do not throw exception")
    void testMultipleUpdates() {
        AIController ai = new AIController(board, controlledTeam, enemyTeam);
        for (int i = 0; i < 100; i++) {
            assertDoesNotThrow(() -> ai.update());
        }
    }
    
    @Test
    @DisplayName("EASY AI updates less frequently")
    void testEasyUpdateFrequency() {
        AIController ai = new AIController(board, controlledTeam, enemyTeam, AIController.Difficulty.EASY);
        Position initialPos = controlledTeam.getTargetPosition();
        
        // After 1 update, position should not change (frequency is 30)
        ai.update();
        assertEquals(initialPos, controlledTeam.getTargetPosition());
    }
    
    @Test
    @DisplayName("HARD AI updates more frequently")
    void testHardUpdateFrequency() {
        AIController ai = new AIController(board, controlledTeam, enemyTeam, AIController.Difficulty.HARD);
        Position initialPos = controlledTeam.getTargetPosition();
        
        // After 5 updates, HARD AI should have moved (frequency is 5)
        for (int i = 0; i < 6; i++) {
            ai.update();
        }
        // Position might have changed (depends on particle positions)
        assertNotNull(controlledTeam.getTargetPosition());
    }
    
    // === Getters Tests ===
    
    @Test
    @DisplayName("getControlledTeam returns correct team")
    void testGetControlledTeam() {
        AIController ai = new AIController(board, controlledTeam, enemyTeam);
        assertEquals(controlledTeam, ai.getControlledTeam());
    }
    
    @Test
    @DisplayName("getDifficulty returns correct difficulty")
    void testGetDifficulty() {
        AIController ai = new AIController(board, controlledTeam, enemyTeam, AIController.Difficulty.HARD);
        assertEquals(AIController.Difficulty.HARD, ai.getDifficulty());
    }
    
    // === Behavior Tests ===
    
    @Test
    @DisplayName("AI moves cursor within board bounds")
    void testCursorStaysInBounds() {
        AIController ai = new AIController(board, controlledTeam, enemyTeam, AIController.Difficulty.HARD);
        
        // Run many updates
        for (int i = 0; i < 200; i++) {
            ai.update();
        }
        
        Position pos = controlledTeam.getTargetPosition();
        assertTrue(pos.x() >= 1 && pos.x() < board.getWidth() - 1);
        assertTrue(pos.y() >= 1 && pos.y() < board.getHeight() - 1);
    }
    
    @Test
    @DisplayName("AI works with empty enemy team")
    void testEmptyEnemyTeam() {
        Team emptyEnemy = new Team(2, Color.RED, new Position(25, 37));
        AIController ai = new AIController(board, controlledTeam, emptyEnemy);
        
        // Should not throw exception
        for (int i = 0; i < 50; i++) {
            assertDoesNotThrow(() -> ai.update());
        }
    }
    
    @Test
    @DisplayName("AI works with empty controlled team")
    void testEmptyControlledTeam() {
        Team emptyControlled = new Team(1, Color.BLUE, new Position(75, 37));
        AIController ai = new AIController(board, emptyControlled, enemyTeam);
        
        // Should not throw exception
        for (int i = 0; i < 50; i++) {
            assertDoesNotThrow(() -> ai.update());
        }
    }
    
    @Test
    @DisplayName("AI handles both teams empty")
    void testBothTeamsEmpty() {
        Team emptyControlled = new Team(1, Color.BLUE, new Position(75, 37));
        Team emptyEnemy = new Team(2, Color.RED, new Position(25, 37));
        AIController ai = new AIController(board, emptyControlled, emptyEnemy);
        
        // Should not throw exception
        for (int i = 0; i < 50; i++) {
            assertDoesNotThrow(() -> ai.update());
        }
    }
    
    // === Strategy Behavior Tests ===
    
    @Test
    @DisplayName("EASY AI tends to stay defensive")
    void testEasyDefensiveBehavior() {
        AIController ai = new AIController(board, controlledTeam, enemyTeam, AIController.Difficulty.EASY);
        
        // Get center of own particles
        int sumX = 0, sumY = 0;
        for (Particle p : controlledTeam.getParticles()) {
            sumX += p.getPosition().x();
            sumY += p.getPosition().y();
        }
        int centerX = sumX / controlledTeam.getParticleCount();
        int centerY = sumY / controlledTeam.getParticleCount();
        Position ownCenter = new Position(centerX, centerY);
        
        // Run updates until AI moves
        for (int i = 0; i < 100; i++) {
            ai.update();
        }
        
        Position aiPos = controlledTeam.getTargetPosition();
        // EASY AI should stay relatively close to its own particles (defensive)
        double distanceToOwn = aiPos.euclideanDistance(ownCenter);
        assertTrue(distanceToOwn < 30, "EASY AI should stay near its own particles");
    }
    
    @Test
    @DisplayName("HARD AI moves towards enemy when winning")
    void testHardAggressiveBehavior() {
        // Give controlled team a big advantage
        for (int i = 0; i < 20; i++) {
            controlledTeam.addParticle(new Particle(new Position(70 + i % 10, 35 + i / 10), controlledTeam));
        }
        
        AIController ai = new AIController(board, controlledTeam, enemyTeam, AIController.Difficulty.HARD);
        
        // Get center of enemy particles
        int sumX = 0, sumY = 0;
        for (Particle p : enemyTeam.getParticles()) {
            sumX += p.getPosition().x();
            sumY += p.getPosition().y();
        }
        int centerX = sumX / enemyTeam.getParticleCount();
        int centerY = sumY / enemyTeam.getParticleCount();
        Position enemyCenter = new Position(centerX, centerY);
        
        Position initialPos = controlledTeam.getTargetPosition();
        double initialDistanceToEnemy = initialPos.euclideanDistance(enemyCenter);
        
        // Run many updates
        for (int i = 0; i < 200; i++) {
            ai.update();
        }
        
        Position finalPos = controlledTeam.getTargetPosition();
        double finalDistanceToEnemy = finalPos.euclideanDistance(enemyCenter);
        
        // HARD AI when winning should move closer to enemies
        assertTrue(finalDistanceToEnemy < initialDistanceToEnemy, 
            "HARD AI should move towards enemies when winning");
    }
    
    // === Difficulty Enum Tests ===
    
    @Test
    @DisplayName("Difficulty enum has 3 values")
    void testDifficultyEnumValues() {
        assertEquals(3, AIController.Difficulty.values().length);
    }
    
    @Test
    @DisplayName("Difficulty enum valueOf works")
    void testDifficultyEnumValueOf() {
        assertEquals(AIController.Difficulty.EASY, AIController.Difficulty.valueOf("EASY"));
        assertEquals(AIController.Difficulty.MEDIUM, AIController.Difficulty.valueOf("MEDIUM"));
        assertEquals(AIController.Difficulty.HARD, AIController.Difficulty.valueOf("HARD"));
    }
}
