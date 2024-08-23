# to use install nix on your system
# enable flakes by adding `experimental-features = nix-command flakes`
# to `~/.config/nix/nix.conf` you might need to create the file
# create necessary custom setup scripts if needed in the shellHook
# start dev shell with `nix develop`

{
  description = "panorama development environment";

  inputs =
    {
      nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
      flake-utils.url = "github:numtide/flake-utils";
      clojure-lsp.url = "github:clojure-lsp/clojure-lsp?ref=2024.04.22-11.50.26";
    };

  outputs = { self, nixpkgs, flake-utils, clojure-lsp }:
    flake-utils.lib.eachDefaultSystem
      (system:
        let pkgs = import nixpkgs {
              inherit system;
              overlays = [
                clojure-lsp.overlays.default
              ];
            };
            gdk = pkgs.google-cloud-sdk.withExtraComponents( with pkgs.google-cloud-sdk.components; [
            gke-gcloud-auth-plugin
          ]);

        in
        with pkgs;
        {
          devShells.default =
            mkShell
              {
                nativeBuildInputs = [
                  watch
                  # docker # you need to install docker imperatively on macos
                  clojure
                  babashka
                  jdk21
                  neil
                  clojure-lsp.packages."${pkgs.system}".clojure-lsp
                  # TODO add flux

                  # nodejs-18_x # TODO build fails with storybook
                  # node canvas deps:
                  # pkg-config
                  # cairo
                  # pango
                  # libpng
                  # libjpeg
                  # giflib
                  # librsvg

                ];
              };
          # personal side effecty setup comment out if you dont need
          # you can for instance add secrets dynamically using
          # 1password or some such
          shellHook = ''
         '';
        }
    );
}

# to use install nix on your system
# enable flakes by adding `experimental-features = nix-command flakes`
# to `~/.config/nix/nix.conf` you might need to create the file
# create necessary custom setup scripts if needed in the shellHook
# start dev shell with `nix develop`
