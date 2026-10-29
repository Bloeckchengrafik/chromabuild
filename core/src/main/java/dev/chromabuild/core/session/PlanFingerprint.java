package dev.chromabuild.core.session;

import dev.chromabuild.core.PlacementPlan;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Content digest of ordered assignments; selection/settings revisions live in PlanSession. */
public final class PlanFingerprint {
    private PlanFingerprint() { }

    public static String of(PlacementPlan plan) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            for (var assignment : plan.assignments()) {
                var p = assignment.position();
                String line = p.x() + "," + p.y() + "," + p.z() + ":" +
                    assignment.blockStateId().length() + ":" + assignment.blockStateId() + "\n";
                digest.update(line.getBytes(StandardCharsets.UTF_8));
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
