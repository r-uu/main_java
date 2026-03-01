#!/bin/bash
# Run DashAppRunner with proper module-path (IntelliJ-Äquivalent for command line)
# Usage: ./run-dash.sh

cd "$(dirname "$0")"

# Set config file
export CONFIG_FILE_NAME="../../../testing.properties"

# Build module-path from Maven dependencies
MODULEPATH=""
for jar in $(mvn dependency:build-classpath -DincludeScope=runtime -q | tail -1 | tr ':' '\n'); do
  MODULEPATH="$MODULEPATH:$jar"
done
MODULEPATH="$MODULEPATH:target/classes"

echo "Starting DashAppRunner..."
java \
  --module-path "$MODULEPATH" \
  --add-modules ALL-MODULE-PATH \
  -Dglass.gtk.uiScale=1.5 \
  -Dconfig.file.name="$CONFIG_FILE_NAME" \
  -m de.ruu.app.jeeeraaah.frontend.ui.fx/de.ruu.app.jeeeraaah.frontend.ui.fx.dash.DashAppRunner
