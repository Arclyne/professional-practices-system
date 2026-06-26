package mx.uv.fei.domain.common.security;

import mx.uv.fei.domain.exceptions.ManagerException;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordHasher {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final String HASH_PREFIX = "$argon2id$";
    private static final int ITERATIONS = 3;
    private static final int MEMORY_KIBIBYTES = 19456;
    private static final int PARALLELISM = 1;
    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 32;

    private PasswordHasher() {
    }

    public static String hash(String plainPassword) throws ManagerException {
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new ManagerException("No se puede procesar una contraseña vacía.");
        }

        byte[] salt = new byte[SALT_LENGTH];
        SECURE_RANDOM.nextBytes(salt);
        byte[] computedHash = computeHash(plainPassword, salt, ITERATIONS, MEMORY_KIBIBYTES, PARALLELISM, HASH_LENGTH);

        return encode(salt, computedHash);
    }

    public static boolean matches(String plainPassword, String storedHash) {
        boolean isMatch = false;

        if (plainPassword != null && storedHash != null && storedHash.startsWith(HASH_PREFIX)) {
            try {
                String[] segments = storedHash.split("\\$");
                int[] costParameters = parseCostParameters(segments[3]);
                Base64.Decoder decoder = Base64.getDecoder();
                byte[] salt = decoder.decode(segments[4]);
                byte[] expectedHash = decoder.decode(segments[5]);
                byte[] computedHash = computeHash(plainPassword, salt,
                        costParameters[1], costParameters[0], costParameters[2], expectedHash.length);
                isMatch = MessageDigest.isEqual(computedHash, expectedHash);
            } catch (RuntimeException e) {
                isMatch = false;
            }
        }

        return isMatch;
    }

    private static byte[] computeHash(String plainPassword, byte[] salt,
                                      int iterations, int memoryKibibytes, int parallelism, int length) {
        Argon2Parameters parameters = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withIterations(iterations)
                .withMemoryAsKB(memoryKibibytes)
                .withParallelism(parallelism)
                .withSalt(salt)
                .build();

        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(parameters);

        byte[] derivedKey = new byte[length];
        generator.generateBytes(plainPassword.getBytes(StandardCharsets.UTF_8), derivedKey);

        return derivedKey;
    }

    private static String encode(byte[] salt, byte[] hash) {
        Base64.Encoder encoder = Base64.getEncoder().withoutPadding();

        return HASH_PREFIX
                + "v=" + Argon2Parameters.ARGON2_VERSION_13
                + "$m=" + MEMORY_KIBIBYTES + ",t=" + ITERATIONS + ",p=" + PARALLELISM
                + "$" + encoder.encodeToString(salt)
                + "$" + encoder.encodeToString(hash);
    }

    private static int[] parseCostParameters(String costSegment) {
        int memoryKibibytes = 0;
        int iterations = 0;
        int parallelism = 0;

        for (String parameter : costSegment.split(",")) {
            String[] keyValue = parameter.split("=");
            switch (keyValue[0]) {
                case "m":
                    memoryKibibytes = Integer.parseInt(keyValue[1]);
                    break;
                case "t":
                    iterations = Integer.parseInt(keyValue[1]);
                    break;
                case "p":
                    parallelism = Integer.parseInt(keyValue[1]);
                    break;
                default:
                    break;
            }
        }

        return new int[] {memoryKibibytes, iterations, parallelism};
    }
}
