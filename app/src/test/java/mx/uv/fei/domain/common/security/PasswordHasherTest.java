package mx.uv.fei.domain.common.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.Test;

public class PasswordHasherTest {

    private static final String PLAIN_PASSWORD = "ClaveSegura2026";

    private static final String EXPECTED_HASH =
            "$argon2id$v=19$m=19456,t=3,p=1$DCg1S9In6YGYy7www34CHw$G+vLCRPOM6Lidsn4xrKHC3Q7n3Pbc8Q66I0wbEAMTGw";
    private static final String EXPECTED_PLAINTEXT = "ClaveFei2026";

    @Test
    void matches_PlaintextAgainstExpectedHash_ReturnsTrue() {
        assertTrue(PasswordHasher.matches(EXPECTED_PLAINTEXT, EXPECTED_HASH));
    }

    @Test
    void matches_WrongPlaintextAgainstExpectedHash_ReturnsFalse() {
        assertFalse(PasswordHasher.matches("OtraClave2026", EXPECTED_HASH));
    }

    @Test
    void hash_ValidPassword_ProducesArgon2idHashDifferentFromPlain() throws ManagerException {
        String hashedPassword = PasswordHasher.hash(PLAIN_PASSWORD);

        assertTrue(hashedPassword.startsWith("$argon2id$"));
        assertNotEquals(PLAIN_PASSWORD, hashedPassword);
    }

    @Test
    void hash_SamePasswordTwice_ProducesDifferentHashes() throws ManagerException {
        String firstHash = PasswordHasher.hash(PLAIN_PASSWORD);
        String secondHash = PasswordHasher.hash(PLAIN_PASSWORD);

        assertNotEquals(firstHash, secondHash);
    }

    @Test
    void matches_FreshlyHashedPassword_ReturnsTrue() throws ManagerException {
        String hashedPassword = PasswordHasher.hash(PLAIN_PASSWORD);

        assertTrue(PasswordHasher.matches(PLAIN_PASSWORD, hashedPassword));
    }

    @Test
    void matches_IncorrectPassword_ReturnsFalse() throws ManagerException {
        String hashedPassword = PasswordHasher.hash(PLAIN_PASSWORD);

        assertFalse(PasswordHasher.matches("ClaveIncorrecta99", hashedPassword));
    }

    @Test
    void matches_NullPlainPassword_ReturnsFalse() {
        assertFalse(PasswordHasher.matches(null, EXPECTED_HASH));
    }

    @Test
    void matches_NullStoredHash_ReturnsFalse() {
        assertFalse(PasswordHasher.matches(EXPECTED_PLAINTEXT, null));
    }

    @Test
    void matches_LegacyPlaintextStored_ReturnsFalse() {
        assertFalse(PasswordHasher.matches(EXPECTED_PLAINTEXT, "ClaveFei2026"));
    }

    @Test
    void matches_MalformedHash_ReturnsFalse() {
        assertFalse(PasswordHasher.matches(EXPECTED_PLAINTEXT, "$argon2id$datos-corruptos"));
    }

    @Test
    void hash_NullPassword_ThrowsManagerException() {
        assertThrows(ManagerException.class, () -> PasswordHasher.hash(null));
    }

    @Test
    void hash_BlankPassword_ThrowsManagerException() {
        assertThrows(ManagerException.class, () -> PasswordHasher.hash("   "));
    }
}
