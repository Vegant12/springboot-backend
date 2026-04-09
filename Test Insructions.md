
How to run tests:

### 1. Maven (from the backend module)

Navigate to the backend project:
```bash
cd "/path/to/springboot-backend"
```

✅ Run all tests:
Using Maven Wrapper:
./mvnw test

On Windows:
mvnw.cmd test

Or using system Maven:
mvn test

✅ Run a specific test class:
```bash
./mvnw -Dtest=YourTestClassName test
```
Example:
```bash
./mvnw -Dtest=FoodControllerTest test
```

✅ Run a single test method:
```bash
./mvnw -Dtest=YourTestClassName#yourTestMethod test
```
Example:
```bash
./mvnw -Dtest=FoodControllerTest#getFoodById_returns200_whenFound test
```


### 2. IDE (VS Code / Eclipse)

- Open `FoodControllerTest.java`.
- Use ▶️ **Run** on the class (green play next to the class name) or on a single `@Test` method.


The test scripts are at:
`springboot-backend/src/test/java/net/javaguides/springboot/controller`
