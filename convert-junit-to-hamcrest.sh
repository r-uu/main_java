#!/bin/bash
# Convert all remaining JUnit assertions to Hamcrest in mapping tests

cd /home/r-uu/develop/github/main/root/app/jeeeraaah/common/api/mapping/src/test/java

# Find all test files with JUnit assertions
for file in $(find . -name "*Test.java" -type f); do
    echo "Processing: $file"

    # Replace import statement
    sed -i 's/import static org\.junit\.jupiter\.api\.Assertions\.\*;/import static org.hamcrest.MatcherAssert.assertThat;\nimport static org.hamcrest.Matchers.*;\nimport static org.junit.jupiter.api.Assertions.assertNotNull;/g' "$file"

    # Replace assertEquals(expected, actual) -> assertThat(actual, is(equalTo(expected)))
    # This is tricky with sed, so we'll use perl
    perl -i -pe 's/assertEquals\(([^,]+),\s*([^)]+)\);/assertThat(\2, is(equalTo(\1)));/g' "$file"

    # Replace assertTrue(condition) -> assertThat(condition, is(true))
    perl -i -pe 's/assertTrue\(([^,)]+)\);/assertThat(\1, is(true));/g' "$file"

    # Replace assertTrue(condition, "message") -> assertThat("message", condition, is(true))
    perl -i -pe 's/assertTrue\(([^,]+),\s*("([^"]+)")\);/assertThat(\2, \1, is(true));/g' "$file"

    # Replace assertFalse(condition) -> assertThat(condition, is(false))
    perl -i -pe 's/assertFalse\(([^,)]+)\);/assertThat(\1, is(false));/g' "$file"

    # Replace assertFalse(condition, "message") -> assertThat("message", condition, is(false))
    perl -i -pe 's/assertFalse\(([^,]+),\s*("([^"]+)")\);/assertThat(\2, \1, is(false));/g' "$file"

    # Replace assertSame(expected, actual) -> assertThat(actual, is(sameInstance(expected)))
    perl -i -pe 's/assertSame\(([^,]+),\s*([^)]+)\);/assertThat(\2, is(sameInstance(\1)));/g' "$file"

    # Replace assertSame(expected, actual, "message") -> assertThat("message", actual, is(sameInstance(expected)))
    perl -i -pe 's/assertSame\(([^,]+),\s*([^,]+),\s*("([^"]+)")\);/assertThat(\3, \2, is(sameInstance(\1)));/g' "$file"
done

echo "✅ Conversion complete!"

