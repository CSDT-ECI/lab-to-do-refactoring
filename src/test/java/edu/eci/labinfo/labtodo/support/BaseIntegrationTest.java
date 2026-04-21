package edu.eci.labinfo.labtodo.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for integration tests.
 *
 * FIRST guarantees provided by this class:
 *   Repeatable  — activates the "test" profile, which wires an H2 in-memory
 *                 database defined in application-test.properties.
 *   Independent — @Transactional rolls back every test after it finishes,
 *                 so no test can poison the database state for the next one.
 *   Fast        — Spring context is reused across all subclasses sharing the
 *                 same configuration, avoiding repeated startup overhead.
 *
 * AAA contract expected from every subclass:
 *   Every @Test method must have exactly one Arrange, one Act, and one Assert block,
 *   each delimited by a comment.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {
}
