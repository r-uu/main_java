#!/bin/bash
set -e

echo "=== Maven Module Renaming: . → _ ==="
echo ""

# Array of directories to rename (deepest first to avoid path conflicts)
declare -a RENAMES=(
  "root/app/jeeeraaah/frontend/api_client/ws_rs:root/app/jeeeraaah/frontend/api_client/ws_rs"
  "root/app/jeeeraaah/frontend/api_client:root/app/jeeeraaah/frontend/api_client"
  "root/app/jeeeraaah/backend/api/ws_rs:root/app/jeeeraaah/backend/api/ws_rs"
  "root/app/jeeeraaah/common/api/ws_rs:root/app/jeeeraaah/common/api/ws_rs"
  "root/app/jeeeraaah/backend/common/mapping.jpa.dto:root/app/jeeeraaah/backend/common/mapping_jpa_dto"
  "root/app/jeeeraaah/frontend/common/mapping.bean.fxbean:root/app/jeeeraaah/frontend/common/mapping_bean_fxbean"
  "root/app/jeeeraaah/frontend/ui/fx.executable:root/app/jeeeraaah/frontend/ui/fx_executable"
  "root/app/jeeeraaah/frontend/ui/fx.model:root/app/jeeeraaah/frontend/ui/fx_model"
  "root/lib/gen/java/fx/bean.editor.demo:root/lib/gen/java/fx/bean_editor_demo"
  "root/lib/gen/java/fx/bean.editor:root/lib/gen/java/fx/bean_editor"
  "root/lib/jpa/core.mapstruct.demo.bidirectional:root/lib/jpa/core_mapstruct_demo_bidirectional"
  "root/lib/jpa/core.mapstruct.test:root/lib/jpa/core_mapstruct_test"
  "root/lib/jpa/core.mapstruct:root/lib/jpa/core_mapstruct"
  "root/lib/jpa/se.hibernate.postgres.demo:root/lib/jpa/se_hibernate_postgres_demo"
  "root/lib/jpa/se.hibernate.postgres:root/lib/jpa/se_hibernate_postgres"
  "root/lib/jpa/se.hibernate:root/lib/jpa/se_hibernate"
  "root/lib/fx/comp.demo:root/lib/fx/comp_demo"
  "root/lib/docker.health:root/lib/docker_health"
  "root/lib/keycloak.admin:root/lib/keycloak_admin"
  "root/lib/mp.config:root/lib/mp_config"
  "root/lib/postgres.util.ui:root/lib/postgres_util_ui"
  "root/lib/ws_rs:root/lib/ws_rs"
)

# Step 1: Rename directories
echo "Step 1: Renaming directories..."
for entry in "${RENAMES[@]}"; do
  OLD_PATH="${entry%%:*}"
  NEW_PATH="${entry##*:}"
  
  if [ -d "$OLD_PATH" ]; then
    echo "  $OLD_PATH -> $NEW_PATH"
    git mv "$OLD_PATH" "$NEW_PATH"
  else
    echo "  Skipping $OLD_PATH (not found)"
  fi
done

echo ""
echo "✓ Directory renaming complete"
echo ""
echo "Now updating references in files..."
echo ""

# Step 2: Update artifactId in each module's pom.xml
echo "Step 2: Updating artifactId in pom.xml files..."
find root -name "pom.xml" -type f -exec sed -i 's|<artifactId>r-uu\.lib\.ws\.rs</artifactId>|<artifactId>r-uu.lib.ws_rs</artifactId>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<artifactId>r-uu\.lib\.mp\.config</artifactId>|<artifactId>r-uu.lib.mp_config</artifactId>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<artifactId>r-uu\.lib\.keycloak\.admin</artifactId>|<artifactId>r-uu.lib.keycloak_admin</artifactId>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<artifactId>r-uu\.lib\.docker\.health</artifactId>|<artifactId>r-uu.lib.docker_health</artifactId>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<artifactId>r-uu\.lib\.postgres\.util\.ui</artifactId>|<artifactId>r-uu.lib.postgres_util_ui</artifactId>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<artifactId>r-uu\.lib\.fx\.comp\.demo</artifactId>|<artifactId>r-uu.lib.fx.comp_demo</artifactId>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<artifactId>r-uu\.lib\.gen\.java\.fx\.bean\.editor</artifactId>|<artifactId>r-uu.lib.gen.java.fx.bean_editor</artifactId>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<artifactId>r-uu\.lib\.gen\.java\.fx\.bean\.editor\.demo</artifactId>|<artifactId>r-uu.lib.gen.java.fx.bean_editor_demo</artifactId>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<artifactId>r-uu\.lib\.jpa\.se\.hibernate</artifactId>|<artifactId>r-uu.lib.jpa.se_hibernate</artifactId>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<artifactId>r-uu\.lib\.jpa\.se\.hibernate\.postgres</artifactId>|<artifactId>r-uu.lib.jpa.se_hibernate_postgres</artifactId>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<artifactId>r-uu\.lib\.jpa\.se\.hibernate\.postgres\.demo</artifactId>|<artifactId>r-uu.lib.jpa.se_hibernate_postgres_demo</artifactId>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<artifactId>r-uu\.lib\.jpa\.core\.mapstruct</artifactId>|<artifactId>r-uu.lib.jpa.core_mapstruct</artifactId>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<artifactId>r-uu\.lib\.jpa\.core\.mapstruct\.test</artifactId>|<artifactId>r-uu.lib.jpa.core_mapstruct_test</artifactId>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<artifactId>r-uu\.lib\.jpa\.core\.mapstruct\.demo\.bidirectional</artifactId>|<artifactId>r-uu.lib.jpa.core_mapstruct_demo_bidirectional</artifactId>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<artifactId>r-uu\.app\.jeeeraaah\.common\.api\.ws\.rs</artifactId>|<artifactId>r-uu.app.jeeeraaah.common.api.ws_rs</artifactId>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<artifactId>r-uu\.app\.jeeeraaah\.backend\.api\.ws\.rs</artifactId>|<artifactId>r-uu.app.jeeeraaah.backend.api.ws_rs</artifactId>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<artifactId>r-uu\.app\.jeeeraaah\.backend\.common\.mapping\.jpa\.dto</artifactId>|<artifactId>r-uu.app.jeeeraaah.backend.common.mapping_jpa_dto</artifactId>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<artifactId>r-uu\.app\.jeeeraaah\.frontend\.api\.client</artifactId>|<artifactId>r-uu.app.jeeeraaah.frontend.api_client</artifactId>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<artifactId>r-uu\.app\.jeeeraaah\.frontend\.api\.client\.ws\.rs</artifactId>|<artifactId>r-uu.app.jeeeraaah.frontend.api_client.ws_rs</artifactId>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<artifactId>r-uu\.app\.jeeeraaah\.frontend\.common\.mapping\.bean\.fxbean</artifactId>|<artifactId>r-uu.app.jeeeraaah.frontend.common.mapping_bean_fxbean</artifactId>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<artifactId>r-uu\.app\.jeeeraaah\.frontend\.ui\.fx\.model</artifactId>|<artifactId>r-uu.app.jeeeraaah.frontend.ui.fx_model</artifactId>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<artifactId>r-uu\.app\.jeeeraaah\.frontend\.ui\.fx\.executable</artifactId>|<artifactId>r-uu.app.jeeeraaah.frontend.ui.fx_executable</artifactId>|g' {} +

# Step 3: Update <module> entries in parent pom.xml files
echo "Step 3: Updating <module> entries..."
find root -name "pom.xml" -type f -exec sed -i 's|<module>ws\.rs</module>|<module>ws_rs</module>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<module>mp\.config</module>|<module>mp_config</module>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<module>keycloak\.admin</module>|<module>keycloak_admin</module>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<module>docker\.health</module>|<module>docker_health</module>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<module>postgres\.util\.ui</module>|<module>postgres_util_ui</module>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<module>comp\.demo</module>|<module>comp_demo</module>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<module>bean\.editor</module>|<module>bean_editor</module>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<module>bean\.editor\.demo</module>|<module>bean_editor_demo</module>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<module>se\.hibernate</module>|<module>se_hibernate</module>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<module>se\.hibernate\.postgres</module>|<module>se_hibernate_postgres</module>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<module>se\.hibernate\.postgres\.demo</module>|<module>se_hibernate_postgres_demo</module>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<module>core\.mapstruct</module>|<module>core_mapstruct</module>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<module>core\.mapstruct\.test</module>|<module>core_mapstruct_test</module>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<module>core\.mapstruct\.demo\.bidirectional</module>|<module>core_mapstruct_demo_bidirectional</module>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<module>api\.client</module>|<module>api_client</module>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<module>fx\.model</module>|<module>fx_model</module>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<module>fx\.executable</module>|<module>fx_executable</module>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<module>mapping\.jpa\.dto</module>|<module>mapping_jpa_dto</module>|g' {} +
find root -name "pom.xml" -type f -exec sed -i 's|<module>mapping\.bean\.fxbean</module>|<module>mapping_bean_fxbean</module>|g' {} +

echo "✓ pom.xml updates complete"
echo ""
echo "✓ Module renaming complete!"
echo "Next steps: Update module-info.java files manually if needed"
