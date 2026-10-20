# Sourced by the other scripts: puts the XDCL toolkit's bin/ on PATH.
# The toolkit version must match the IntelliJ plugin and the Gradle distribution in the wrapper.
XDCL_TOOLKIT_VERSION="0.1.0-dev.20261008.gd14cf948"
XDCL_TOOLKIT="${XDCL_TOOLKIT:-$HOME/xdcl/xdcl-toolkit-$XDCL_TOOLKIT_VERSION}"

if [ ! -x "$XDCL_TOOLKIT/bin/xdcl-gradle-sync" ]; then
  echo "error: XDCL toolkit not found at $XDCL_TOOLKIT (see xdcl/README.md, or set XDCL_TOOLKIT)" >&2
  exit 2
fi
export PATH="$XDCL_TOOLKIT/bin:$PATH"
