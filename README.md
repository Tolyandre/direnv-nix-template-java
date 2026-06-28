# direnv-nix-template-java

Template for Java projects whose **entire toolchain comes from Nix** and is activated
automatically by **direnv**. There is no system-wide or IDE-downloaded JDK/Maven — the
editor uses exactly the OpenJDK and Maven pinned in [`flake.nix`](flake.nix). Works in
both **VSCode** and **IntelliJ IDEA**.

Includes a minimal Maven hello-world (`com.example.App`) with a JUnit 5 test.

## How it works

- [`flake.nix`](flake.nix) defines a dev shell with **OpenJDK 25 (LTS)** and **Maven**.
  The JDK's Nix setup-hook exports `JAVA_HOME`, and both `java` and `mvn` land on `PATH`.
- [`.envrc`](.envrc) runs `use flake`, so [direnv](https://direnv.net/) enters that dev
  shell automatically whenever you `cd` into the project (and your IDE picks the same env up).
- The shell hook also creates two stable, project-relative symlinks under `.nix-tools/`
  (git-ignored) that point into the Nix store — used to configure IntelliJ without
  hardcoding `/nix/store` hashes:
  - `.nix-tools/jdk` → the JDK home (IntelliJ Project SDK)
  - `.nix-tools/maven` → the Maven home (IntelliJ Maven home)
- **Note:** the *toolchain* (JDK + Maven) always comes from Nix. Maven *libraries*
  (e.g. JUnit) are still resolved from Maven Central into `~/.m2` as usual.

## Prerequisites

- [Nix](https://nixos.org/) with flakes enabled (`experimental-features = nix-command flakes`)
- [`direnv`](https://direnv.net/) and [`nix-direnv`](https://github.com/nix-community/nix-direnv)
  (the latter provides the `use flake` helper)

## First run

```sh
direnv allow            # builds the dev shell and creates the .nix-tools/ symlinks
java -version           # → openjdk version "25"
mvn -version            # → Maven 3.9.x, "Java version: 25", JAVA_HOME inside /nix/store
mvn test                # compiles with --release 25 and runs the JUnit test
mvn package && java -jar target/hello-world-1.0-SNAPSHOT.jar   # → Hello, World!
```

If `direnv allow` prints nothing about loading the flake, make sure `nix-direnv` is
installed and hooked into your shell.

## VSCode

1. Install the recommended extensions when prompted (see [`.vscode/extensions.json`](.vscode/extensions.json)):
   - **Extension Pack for Java** (`vscjava.vscode-java-pack`)
   - **direnv** (`mkhl.direnv`)
2. Allow direnv for the workspace (the `mkhl.direnv` extension will ask), then **reload the window**.

The `mkhl.direnv` extension injects the direnv environment — including `JAVA_HOME` and the
Nix `mvn`/`java` on `PATH` — into VSCode. The Java extension then auto-detects the Nix JDK
and Maven, so it never prompts you to download a JDK. [`.vscode/settings.json`](.vscode/settings.json)
additionally disables the Maven wrapper and Gradle import.

> **Fallback** (if the direnv env isn't picked up): set `java.jdt.ls.java.home` to the
> absolute path of `.nix-tools/jdk` in your user settings. The Red Hat Java extension does
> not support `${workspaceFolder}` in that setting, so it must be an absolute path.

## IntelliJ IDEA

IDEA stores SDK definitions globally (per machine), so the toolchain is wired up once via
the project-relative `.nix-tools/` symlinks:

1. *(Optional)* Install the **Direnv integration** plugin so the IDE terminal and run
   configurations inherit the direnv environment. Alternatively, launch IDEA from a terminal
   where direnv is already active.
2. **Project SDK:** `File → Project Structure → SDKs → + → Add JDK…` and select
   `<project>/.nix-tools/jdk`. Then set it as the **Project SDK** with **language level 25**.
3. **Maven:** `File → Settings → Build, Execution, Deployment → Build Tools → Maven`:
   - **Maven home directory** → `<project>/.nix-tools/maven`
   - **JDK for importer** → the `.nix-tools/jdk` SDK added above

## Adding more tools

Add packages to the `packages` list in [`flake.nix`](flake.nix), each with a short comment
explaining why it's needed, for example:

```nix
packages = [
  jdk
  pkgs.maven
  pkgs.python3   # needed by some Claude Code agentic workflows
];
```

Run `direnv reload` (or just `cd` out and back in) to pick up the change.
