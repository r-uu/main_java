#!/bin/bash
# Überprüft die Java/GraalVM Installation

echo "🔍 Java/GraalVM Installations-Check"
echo "===================================="
echo ""

# Farben
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

check_ok() {
    echo -e "${GREEN}✓${NC} $1"
}

check_fail() {
    echo -e "${RED}✗${NC} $1"
}

check_warn() {
    echo -e "${YELLOW}⚠${NC} $1"
}

# 1. Java Version
echo "1️⃣ Java Version:"
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java --version 2>&1 | head -n 1)
    JAVA_FULL=$(java --version 2>&1)
    echo "   $JAVA_VERSION"

    if echo "$JAVA_VERSION" | grep -q "java 25"; then
        check_ok "Java 25 erkannt"
    else
        check_warn "Java 25 wird erwartet, aber nicht gefunden"
    fi

    if echo "$JAVA_FULL" | grep -qi "graalvm\|oracle.*lts"; then
        check_ok "GraalVM erkannt (Oracle GraalVM)"
    else
        check_warn "GraalVM wird erwartet, aber nicht gefunden"
    fi
else
    check_fail "Java nicht gefunden"
fi
echo ""

# 2. JAVA_HOME
echo "2️⃣ JAVA_HOME:"
if [ -n "$JAVA_HOME" ]; then
    echo "   $JAVA_HOME"

    if [ "$JAVA_HOME" = "/opt/graalvm-jdk-25" ]; then
        check_ok "JAVA_HOME korrekt gesetzt"
    else
        check_warn "JAVA_HOME erwartet: /opt/graalvm-jdk-25"
    fi

    if [ -d "$JAVA_HOME" ]; then
        check_ok "JAVA_HOME Verzeichnis existiert"
    else
        check_fail "JAVA_HOME Verzeichnis nicht gefunden"
    fi
else
    check_fail "JAVA_HOME nicht gesetzt"
fi
echo ""

# 3. Java Binary Pfad
echo "3️⃣ Java Binary:"
if command -v java &> /dev/null; then
    JAVA_BIN=$(which java)
    echo "   $JAVA_BIN"

    if [ -L "$JAVA_BIN" ]; then
        JAVA_LINK=$(readlink -f "$JAVA_BIN")
        echo "   → $JAVA_LINK"

        if echo "$JAVA_LINK" | grep -q "graalvm-jdk-25"; then
            check_ok "Java Binary zeigt auf GraalVM 25"
        else
            check_warn "Java Binary zeigt nicht auf GraalVM 25"
        fi
    fi
else
    check_fail "Java Binary nicht gefunden"
fi
echo ""

# 4. Maven
echo "4️⃣ Maven:"
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn --version 2>&1 | head -n 1)
    echo "   $MVN_VERSION"

    MVN_JAVA=$(mvn --version 2>&1 | grep "Java version" | head -n 1)
    echo "   $MVN_JAVA"

    if echo "$MVN_JAVA" | grep -q "25"; then
        check_ok "Maven nutzt Java 25"
    else
        check_warn "Maven nutzt nicht Java 25"
    fi
else
    check_fail "Maven nicht gefunden"
fi
echo ""

# 5. Native Image
echo "5️⃣ Native Image:"
if command -v native-image &> /dev/null; then
    check_ok "Native Image verfügbar"
    NI_VERSION=$(native-image --version 2>&1 | head -n 1)
    echo "   $NI_VERSION"
else
    check_warn "Native Image nicht installiert (optional)"
    echo "   Installieren mit: sudo \$JAVA_HOME/bin/gu install native-image"
fi
echo ""

# 6. GraalVM Updater
echo "6️⃣ GraalVM Updater (gu):"
if [ -f "$JAVA_HOME/bin/gu" ]; then
    check_ok "GraalVM Updater verfügbar"
else
    check_warn "GraalVM Updater nicht gefunden"
fi
echo ""

# 7. Aliase
echo "7️⃣ r-uu Aliase:"
if type ruu-graalvm-version &> /dev/null; then
    check_ok "r-uu Aliase geladen"
    RUU_COUNT=$(alias 2>/dev/null | grep "ruu-" | wc -l)
    echo "   $RUU_COUNT Aliase verfügbar"
else
    check_warn "r-uu Aliase nicht geladen"
    echo "   Füge zur ~/.bashrc hinzu:"
    echo "   source ~/develop/github/java/main/config/shared/wsl/aliases.sh"
fi
echo ""

# Zusammenfassung
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📊 Zusammenfassung:"
echo ""

if command -v java &> /dev/null && java --version 2>&1 | grep -q "java 25"; then
    if [ "$JAVA_HOME" = "/opt/graalvm-jdk-25" ]; then
        check_ok "System bereit für Entwicklung mit GraalVM 25"
    else
        check_warn "JAVA_HOME sollte /opt/graalvm-jdk-25 sein"
    fi
else
    check_fail "Bitte GraalVM 25 installieren"
    echo ""
    echo "Installationsanleitung:"
    echo "  cd ~/develop/github/java/main/config/shared/scripts"
    echo "  sudo ./install-graalvm.sh"
fi
echo ""

