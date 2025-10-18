# Manifest override for Minecraft version

This document explains the change that adds a configurable manifest version range without affecting per-version builds.

Files changed

- [`build.gradle`](build.gradle:19) — added a computed property `manifest_minecraft_version` (defaults to `~${project.minecraft_version}`) and exposed it to resource expansion.
- [`src/main/resources/fabric.mod.json`](src/main/resources/fabric.mod.json:31) — manifest now uses `${manifest_minecraft_version}` for the "minecraft" depends entry.

How it works

- During `processResources`, Gradle expands `manifest_minecraft_version` into `fabric.mod.json`. See the `processResources` block in [`build.gradle`](build.gradle:49).
- `manifest_minecraft_version` defaults to `~${project.minecraft_version}` unless overridden with `-Pmanifest_minecraft_version=...`.

Usage examples

- Default per-version build (unchanged):
  gradlew build

- Override the manifest range (single jar allowing 1.21.9 up to but not including 1.22):
  gradlew build -Pmanifest_minecraft_version=">=1.21.9 <1.22"

Notes

- `project.minecraft_version` (from [`gradle.properties`](gradle.properties:3) or `-PtargetProps`) still controls dependency resolution, archive folder and jar naming.
- `build-all.ps1` continues to build per-version; each per-version run will default the manifest to `~<that-version>`.

Resulting manifest snippet

```json
{
  "depends": {
    "minecraft": ">=1.21.9 <1.22"
  }
}
```

Troubleshooting

- If you want a truly versionless jar name, consider passing a separate `-Puniversal` flag and adjusting `archiveFileName` (not implemented here).