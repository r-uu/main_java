#!/usr/bin/env python3
"""Convert JUnit assertions to Hamcrest matchers in Java test files."""

import re
import sys
from pathlib import Path

def convert_file(file_path):
    """Convert a single Java test file to use Hamcrest matchers."""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    original = content

    # Replace import statement
    content = re.sub(
        r'import static org\.junit\.jupiter\.api\.Assertions\.\*;',
        'import static org.hamcrest.MatcherAssert.assertThat;\n'
        'import static org.hamcrest.Matchers.*;\n'
        'import static org.junit.jupiter.api.Assertions.assertNotNull;',
        content
    )

    # Replace assertEquals(expected, actual) with assertThat(actual, is(equalTo(expected)))
    content = re.sub(
        r'assertEquals\(([^,]+),\s*([^)]+)\);',
        r'assertThat(\2, is(equalTo(\1)));',
        content
    )

    # Replace assertTrue(condition) with assertThat(condition, is(true))
    content = re.sub(
        r'assertTrue\(([^)]+)\);',
        r'assertThat(\1, is(true));',
        content
    )

    # Replace assertTrue(condition, "message") with assertThat("message", condition, is(true))
    content = re.sub(
        r'assertTrue\(([^,]+),\s*("([^"]+)")\);',
        r'assertThat(\2, \1, is(true));',
        content
    )

    # Replace assertFalse(condition) with assertThat(condition, is(false))
    content = re.sub(
        r'assertFalse\(([^,]+)\);',
        r'assertThat(\1, is(false));',
        content
    )

    # Replace assertFalse(condition, "message") with assertThat("message", condition, is(false))
    content = re.sub(
        r'assertFalse\(([^,]+),\s*("([^"]+)")\);',
        r'assertThat(\2, \1, is(false));',
        content
    )

    # Replace assertSame(expected, actual) with assertThat(actual, is(sameInstance(expected)))
    content = re.sub(
        r'assertSame\(([^,]+),\s*([^)]+)\);',
        r'assertThat(\2, is(sameInstance(\1)));',
        content
    )

    # Replace assertSame(expected, actual, "message") with assertThat("message", actual, is(sameInstance(expected)))
    content = re.sub(
        r'assertSame\(([^,]+),\s*([^,]+),\s*("([^"]+)")\);',
        r'assertThat(\3, \2, is(sameInstance(\1)));',
        content
    )

    if content != original:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        return True
    return False

def main():
    if len(sys.argv) < 2:
        print("Usage: convert_to_hamcrest.py <directory>")
        sys.exit(1)

    root_dir = Path(sys.argv[1])
    count = 0

    for java_file in root_dir.rglob("*Test.java"):
        if convert_file(java_file):
            print(f"Converted: {java_file}")
            count += 1

    print(f"\nConverted {count} file(s)")

if __name__ == "__main__":
    main()

