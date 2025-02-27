# MapStruct Build Error Resolution Documentation

## Problem
The generated mapper code in `DealMapperImpl.java` is failing to build due to errors with the following imports:
- `java.time.ZoneId`
- `java.time.ZonedDateTime`

These errors indicate issues with the compatibility of the Java time APIs in the current build configuration.

## Approaches Evaluated

### Approach 1: Update Maven Compiler Plugin Settings
- **Objective:** Modify the Maven compiler configuration to target a Java release (at least Java 8 or preferably Java 11) that supports the `java.time` API.
- **Implementation:** Update the `<maven-compiler-plugin>` configuration in the `pom.xml` to set the release to 11.
- **Outcome:** The build errors are resolved since the Java time classes become available.
- **Constraint:** This approach directly modifies the `pom.xml`, which conflicts with the current global instructions not to modify it.
- **Conclusion:** Effective technically but cannot be used under the current constraints.

### Approach 2: Adjust Annotation Processor Options
- **Objective:** Configure the annotation processor (such as MapStruct) to correctly generate code that supports `java.time` types.
- **Implementation:** Pass processor-specific options (for example, enabling Java 8 support) in the mapping interface or via build configurations.
- **Outcome:** This may resolve the error if the processor is instructed to use the correct types. However, it requires modifications within source-level configurations.
- **Conclusion:** Feasible if source code changes are permitted; however, it depends on having centralized mapper interfaces that can be updated.

### Approach 3: Provide Custom Converters for Date/Time Types
- **Objective:** Introduce custom mapping methods or utility converters to handle conversions between legacy date/time types (e.g., `java.util.Date`) and the modern `java.time` API.
- **Implementation:**  
  - Create a dedicated converter class that provides methods to convert between `java.util.Date` and `java.time.ZoneId`/`java.time.ZonedDateTime`.  
  - In the mapper interfaces, use MapStruct’s `@Mapping` (or `@Mapper(uses = ...)`) to delegate conversion to these custom methods.
- **Outcome:** This approach avoids the need to change the global Maven configuration or annotation processor options while ensuring that mapping logic correctly handles date/time types.
- **Conclusion:** Optimal under the current constraints, as it resolves build errors without modifying the `pom.xml`.

## Recommendation and Next Steps
Given that modifying the `pom.xml` is not an option, **Approach 3** (custom converters) is recommended. The next steps include:
1. **Implementing a Converter Class:** Create a utility class with static methods for converting to and from `java.time.ZoneId`/`java.time.ZonedDateTime`.
2. **Refactoring Mapper Interfaces:** Update the relevant MapStruct mapper interfaces to use the converter methods (via the `@Mapper(uses = { YourConverter.class })` annotation).
3. **Testing:** Rigorously test the changes to ensure that the build errors are resolved and that the mapping logic functions as expected.
4. **Documentation:** Update project documentation to note this custom conversion strategy for future reference.

This systematic evaluation and implementation plan should resolve the build issues while adhering to current project constraints.