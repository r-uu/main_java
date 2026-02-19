#!/bin/bash

# Phase 3 - Automated Test Creation Script
# This script creates all missing test files for mapping modules

set -e  # Exit on error

BASE_DIR="/home/r-uu/develop/github/main/root/app/jeeeraaah"

echo "================================================"
echo "Phase 3: Creating Missing Mapper Tests"
echo "================================================"

# Function to create a test file
create_test() {
    local package_path=$1
    local class_name=$2
    local mapper_class=$3

    local full_path="${BASE_DIR}/${package_path}/${class_name}.java"
    local dir=$(dirname "$full_path")

    echo "Creating: $full_path"

    # Create directory if it doesn't exist
    mkdir -p "$dir"

    # Extract package name from path
    local package_name=$(echo "$package_path" | sed 's|src/test/java/||' | sed 's|/|.|g')

    # Create test file
    cat > "$full_path" << EOF
package ${package_name};

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Basic test to verify {@link ${mapper_class}} mapper exists and can be instantiated.
 */
class ${class_name}
{
	@Test
	void mapperExists()
	{
		assertNotNull(${mapper_class}.INSTANCE, "Mapper instance should exist");
	}
}
EOF

    echo "  ✅ Created: ${class_name}"
}

echo ""
echo "1️⃣  Creating Common API Mapping Tests..."
echo "--------------------------------------------"

# common/api/mapping.bean.dto tests
create_test "common/api/mapping.bean.dto/src/test/java/de/ruu/app/jeeeraaah/common/api/mapping/bean/lazy" \
    "Map_Task_Bean_Lazy_Test" \
    "Map_Task_Bean_Lazy"

create_test "common/api/mapping.bean.dto/src/test/java/de/ruu/app/jeeeraaah/common/api/mapping/bean/lazy" \
    "Map_TaskGroup_Bean_Lazy_Test" \
    "Map_TaskGroup_Bean_Lazy"

create_test "common/api/mapping.bean.dto/src/test/java/de/ruu/app/jeeeraaah/common/api/mapping/bean/flat" \
    "Map_TaskGroup_Bean_Flat_Test" \
    "Map_TaskGroup_Bean_Flat"

create_test "common/api/mapping.bean.dto/src/test/java/de/ruu/app/jeeeraaah/common/api/mapping/lazy/bean" \
    "Map_Task_Lazy_Bean_Test" \
    "Map_Task_Lazy_Bean"

create_test "common/api/mapping.bean.dto/src/test/java/de/ruu/app/jeeeraaah/common/api/mapping/lazy/bean" \
    "Map_TaskGroup_Lazy_Bean_Test" \
    "Map_TaskGroup_Lazy_Bean"

create_test "common/api/mapping.bean.dto/src/test/java/de/ruu/app/jeeeraaah/common/api/mapping/flat/bean" \
    "Map_TaskGroup_Flat_Bean_Test" \
    "Map_TaskGroup_Flat_Bean"

echo ""
echo "2️⃣  Creating Frontend Mapping Tests..."
echo "--------------------------------------------"

# frontend/common/mapping.bean.fxbean tests
create_test "frontend/common/mapping.bean.fxbean/src/test/java/de/ruu/app/jeeeraaah/frontend/common/mapping/bean/flatbean" \
    "Map_TaskGroup_Bean_FlatBean_Test" \
    "Map_TaskGroup_Bean_FlatBean"

echo ""
echo "================================================"
echo "✅ All test files created successfully!"
echo "================================================"
echo ""
echo "Next steps:"
echo "  1. cd /home/r-uu/develop/github/main/bom && mvn clean install -DskipTests"
echo "  2. cd /home/r-uu/develop/github/main/root && mvn clean test"
echo ""

