package edu.eci.labinfo.labtodo.support;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Base class for unit tests.
 *
 * FIRST guarantees provided by this class:
 *   Fast        — no Spring context is loaded; Mockito is the only framework dependency.
 *   Independent — per-method lifecycle is enforced in junit-platform.properties;
 *                 subclasses must build all data in @BeforeEach or inside each test.
 *
 * AAA contract expected from every subclass:
 *   Every @Test method must have exactly one Arrange, one Act, and one Assert block,
 *   each delimited by a comment.
 */
@ExtendWith(MockitoExtension.class)
public abstract class BaseUnitTest {
    // Intentionally empty.
    // MockitoExtension initialises @Mock and @InjectMocks fields before each test.
}
