# Pull request: compat: MC 1.21.x fixes

Branch: fix/mc-1.21-compat

Summary:
- Fix KeyBinding constructor change (String -> KeyBinding.Category) via a reflection shim in [`src/main/java/com/spyxar/glowup/GlowUpMod.java`](src/main/java/com/spyxar/glowup/GlowUpMod.java:81).
- Add compatibility ColorHelper for getRed/getGreen/getBlue in [`src/main/java/com/spyxar/glowup/ColorHelper.java`](src/main/java/com/spyxar/glowup/ColorHelper.java:1).
- Fix mixin @ModifyArgs descriptors for OutlineVertexConsumerProvider.setColor (int vs float) in [`src/main/java/com/spyxar/glowup/mixin/WorldRendererMixin.java`](src/main/java/com/spyxar/glowup/mixin/WorldRendererMixin.java:19).
- Update CI driver [`build-all.ps1`](build-all.ps1:1) to build multiple 1.21.x targets and archive per-version jars.

Key commits (local):
- 28bda33f — KeyBinding compatibility shim
- cdd165f — Add ColorHelper
- 8025c91 — Mixin descriptor fixes (int/float)
- 110f10c — Update build-all.ps1 to include more versions

Built artifacts (archived by the script under build-archive):
- build-archive\1.21\GlowUp-1.4.0+1.21.7.jar
- build-archive\1.21.1\GlowUp-1.4.0+1.21.1.jar
- build-archive\1.21.2\GlowUp-1.4.0+1.21.2.jar
- build-archive\1.21.3\GlowUp-1.4.0+1.21.3.jar
- build-archive\1.21.4\GlowUp-1.4.0+1.21.4.jar
- build-archive\1.21.5\GlowUp-1.4.0+1.21.5.jar
- build-archive\1.21.6\GlowUp-1.4.0+1.21.6.jar
- build-archive\1.21.7\GlowUp-1.4.0+1.21.7.jar
- build-archive\1.21.8\GlowUp-1.4.0+1.21.8.jar
- build-archive\1.21.9\GlowUp-1.4.0+1.21.9.jar
- build-archive\1.21.10\GlowUp-1.4.0+1.21.10.jar

How to reproduce locally:
1. git checkout fix/mc-1.21-compat
2. powershell -ExecutionPolicy Bypass -File .\build-all.ps1
   (script will place per-version jars in `build-archive\<version>\`)

How to open the PR (options):
- GitHub web:
  - Create PR from `fix/mc-1.21-compat` → `main`
  - Paste this file (`PULL_REQUEST.md`) as the PR description
  - Drag the jars from `build-archive\` into the PR to attach test artifacts
- GitHub CLI:
  - git push -u origin fix/mc-1.21-compat
  - gh pr create --base main --head fix/mc-1.21-compat --title "compat: MC 1.21.x fixes" --body-file PULL_REQUEST.md

Notes / recommended merge policy:
- Prefer a squash-and-merge with the PR description summarizing the per-version approach.
- Verify at least one runtime test in-game (recommend 1.21.9) before merging.
- Single-jar alternative: this PR also includes a runtime reflection shim at [`src/main/java/com/spyxar/glowup/compat/ReflectionShim.java`](src/main/java/com/spyxar/glowup/compat/ReflectionShim.java:1) which adapts to the KeyBinding constructor and optional Fabric KeyBindingHelper at runtime. To produce a single artifact supporting multiple 1.21.x micro-versions, build normally (do not pass targetProps) and remap once:
  - Windows: `.\gradlew.bat clean remapJar`
  - Other OS: `./gradlew clean remapJar`
  The resulting jar in `build/libs` will include the reflection shim and should run across the 1.21.x targets; keep `build-all.ps1` available for producing per-version archived artifacts for testing if desired.
