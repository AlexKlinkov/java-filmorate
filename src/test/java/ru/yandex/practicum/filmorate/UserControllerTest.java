package ru.yandex.practicum.filmorate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserControllerTest {
    /*
    ConfigurableApplicationContext context; // Поле для запуска и остановки web приложения
    User user; // Поле пользователь для проверки контроллера
    InMemoryUserStorage inMemoryUserStorage; // Поле хранилища, а тестируем (контроллер)

    @Autowired
    public UserControllerTest(InMemoryUserStorage inMemoryUserStorage) {
        this.inMemoryUserStorage = inMemoryUserStorage;
    }

    HttpClient client; // Клиент, для отправки запроса на сервер
    String urlString = "http://localhost:8080/users"; // Адрес к методам сервера
    Gson gson;
    // Метод, который создает пользователя и инициализирует необходимые поля для тестирования эндпоинтов контроллера
    @BeforeEach
    public void create() {
        context = SpringApplication.run(FilmorateApplication.class); // Запускаем наше web приложение
        gson = new Gson();
        this.inMemoryUserStorage = new InMemoryUserStorage();
        user = new User();
        user.setId(1); // Устанавливаем ID
        user.setEmail("1Kot@mail.ru"); // Почта
        user.setLogin("123456"); // Логин
        user.setName("CatMan"); // Никнейм пользователя
        user.setBirthday(LocalDate.of(1994, 1,1)); // Инициализируем дату рождения пользователя
        client = HttpClient.newBuilder().build(); // Инициализируем клиента для отпавки запроса на сервер
    }

    @AfterEach
    void stopServer() {
        SpringApplication.exit(context); // останавливаем работу нашего web приложения
    }

    // Метод тестирует создание пользователя со всеми правильными параметрами
    @ParameterizedTest
    public void createWhenAllParamsIsGood() throws ValidationException {
        inMemoryUserStorage.create(user); // Создаем/добавляем фильм
        assertEquals(user, inMemoryUserStorage.getAllUsers().get(1),
                "Пользователь добавленный будет тот же самый, что и в мапе");
    }

    // Метод тестирует выбрасывание исключения если user = null
    @ParameterizedTest
    public void createWhenUserIsNull() {
        user = null;
        ValidationException exception = Assertions.assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                inMemoryUserStorage.create(user);
            }
        });
        assertEquals("400 BAD_REQUEST", exception.getMessage(),
                "Пользователь не должен быть создан/добавлен");
        assertTrue(inMemoryUserStorage.getAllUsers().isEmpty(), "Мапа с пользователями должна быть пустой");
    }

    // Метод тестирует то, что не может быть создан пользователь с неправильным email адресом или пустым адресом
    @Test
    public void createWhenEmailIsNotCorrect() throws ValidationException {
        user.setEmail("");
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(gson.toJson(user));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .header("Accept", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .POST(body)
                .build();
        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
            assertTrue(inMemoryUserStorage.getAllUsers().isEmpty(), "Мапа с пользователеми " +
                    "должна быть пустой");
        } catch (IOException | InterruptedException e) {
            throw new ValidationException("Запрос завершился с ошибкой при попытке добавить пользователя " +
                    "с некорректным email");
        }
    }

    // Метод тестирует то, что не может быть создан пользователь с неправильным логином (с пробелами)
    @Test
    public void createWhenLoginIsNotCorrect() throws ValidationException {
        user.setLogin("Vasi ly");
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(gson.toJson(user));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .header("Content-Type", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .POST(body)
                .build();
        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(inMemoryUserStorage.getAllUsers());
            assertTrue(inMemoryUserStorage.getAllUsers().isEmpty(), "Мапа с пользователеми " +
                    "должна быть пустой");
        } catch (IOException | InterruptedException e) {
            throw new ValidationException("Запрос завершился с ошибкой при попытке добавить пользователя " +
                    "с некорректным логином");
        }
    }

    // Метод тестирует то, что не может быть создан пользователь с датой рождения указанной в будущем
    @Test
    public void createWhenBirthdayInTheFuture() throws ValidationException {
        user.setBirthday(LocalDate.of(2010,12,12));
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(gson.toJson(user));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .header("Content-Type", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .POST(body)
                .build();
        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
            assertTrue(inMemoryUserStorage.getAllUsers().isEmpty(), "Мапа с пользователеми " +
                    "должна быть пустой");
        } catch (IOException | InterruptedException e) {
            throw new ValidationException("Запрос завершился с ошибкой при попытке добавить пользователя " +
                    "с датой рождения в будущем");
        }
    }

    // Метод тестирует, что если нет у пользователя имени (пустое), то вместо имени используется логин
    @Test
    public void createUserWithEmptyName() throws ValidationException {
      user.setName("");
      inMemoryUserStorage.create(user);
      assertEquals(user.getName(), user.getLogin(), "При пустом имени должен использоваться логин");
    }

     */
}
