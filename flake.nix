{
  description = "Java + Maven template with a Nix-provided toolchain (direnv) for VSCode & IntelliJ IDEA";

  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";

  outputs =
    { self, nixpkgs }:
    let
      supportedSystems = [
        "x86_64-linux"
        "aarch64-linux"
        "x86_64-darwin"
        "aarch64-darwin"
      ];
      forAllSystems = nixpkgs.lib.genAttrs supportedSystems;
    in
    {
      devShells = forAllSystems (
        system:
        let
          pkgs = nixpkgs.legacyPackages.${system};
          # LTS Java SDK. jdk25 = OpenJDK 25 (LTS). `.home` (…/lib/openjdk) is the
          # real JAVA_HOME that IDEs expect as a JDK home directory.
          jdk = pkgs.jdk25;
        in
        {
          default = pkgs.mkShell {
            # The whole toolchain comes from Nix, so the IDE never downloads a
            # JDK or Maven into ~ or system dirs. (git is assumed to be installed
            # globally and is intentionally not pinned here.)
            packages = [
              jdk # OpenJDK 25 (LTS); its setup-hook exports JAVA_HOME
              pkgs.maven # Apache Maven (uses the JAVA_HOME above)
              # Add extra toolchain here WITH A COMMENT explaining why, e.g.:
              # pkgs.python3   # needed by some Claude Code agentic workflows
            ];

            shellHook = ''
              # Stable, project-relative symlinks (collected under .nix-tools/ to
              # keep the repo root clean) so IntelliJ IDEA and explicit configs
              # locate the Nix toolchain without hardcoding /nix/store hashes.
              # Recreated on every direnv reload when the toolchain changes.
              mkdir -p "$PWD/.nix-tools"
              ln -sfn ${jdk.home} "$PWD/.nix-tools/jdk" # JDK home  → IntelliJ Project SDK
              ln -sfn ${pkgs.maven}/maven "$PWD/.nix-tools/maven" # Maven home → IntelliJ Maven home
            '';
          };
        }
      );
    };
}
