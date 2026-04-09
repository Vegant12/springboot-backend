
You can run that class alone in two common ways:

### 1) Maven (from the backend module)

```bash
cd "/path/to/eterna-assignment/springboot-backend"
./mvnw -Dtest=FoodControllerTest test
```

If you use system Maven:

```bash
mvn -Dtest=FoodControllerTest test
```

That runs only `net.javaguides.springboot.controller.FoodControllerTest`.

To run a single method:

```bash
./mvnw -Dtest=FoodControllerTest#getFoodById_returns200_whenFound test
```

(Use the exact method name.)

### 2) IDE (IntelliJ / VS Code / Eclipse)

- Open `FoodControllerTest.java`.
- Use **Run** on the class (green play next to the class name) or on a single `@Test` method.

The class is at:

`springboot-backend/src/test/java/net/javaguides/springboot/controller/FoodControllerTest.java`

**Note:** Your full `mvn test` can still start other tests (e.g. `SpringbootBackendApplicationTests`) if you run the whole suite; `-Dtest=FoodControllerTest` limits it to this class.