/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.gradle.enums

fun getLevel(version: String, suffix: String?): ReleaseLevel {
    for (level in ReleaseLevel.values()) {
        if (level.selectionCriteria(version, suffix)) {
            return level
        }
    }

    // Fallback
    return ReleaseLevel.SNAPSHOT
}

enum class ReleaseLevel(val selectionCriteria: (String, String?) -> Boolean, val template: String, val isSnapshot: Boolean) {
    SNAPSHOT( { _, suffix -> suffix != null && suffix.endsWith("SNAPSHOT") } , "snapshot", true),
    ALPHA( { _, suffix -> suffix != null && suffix.contains("ALPHA") }, "alpha", false),
    BETA( { _, suffix -> suffix != null && suffix.contains("BETA") }, "beta", false),
    RELEASE_CANDIDATE( { _, suffix -> suffix != null && suffix.contains("RC") } , "rc", false),
    RELEASE_MAJOR( { version, _ -> version.endsWith(".0") } , "release-big", false),
    RELEASE_MINOR( { _, _ -> true }, "release", false);
}