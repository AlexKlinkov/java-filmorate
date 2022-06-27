package ru.yandex.practicum.filmorate;

public class FilmControllerTest {
    /*
    ConfigurableApplicationContext context; // Поле для запуска и остановки web приложения
    Film film; // Поле фильм для проверки контроллера
    FilmController filmController; // Поле класса, который тестируем (контроллер)
    HttpClient client; // Клиент, для отправки запроса на сервер
    String urlString = "http://localhost:8080/films"; // Адрес к методам сервера

    Gson gson;

    // Метод, который создает фильм и инициализирует необходимые поля для тестирования эндпоинтов контроллера
    @BeforeEach
    public void create() {
        context = SpringApplication.run(FilmorateApplication.class); // Запускаем наше web приложение
        gson = new Gson();
        filmController = new FilmController( new FilmService(new InMemoryFilmStorage()));
        film = new Film();
        film.setId(1); // Устанавливаем ID
        film.setName("Люси"); // Название фильма
        film.setDescription("Фантастика"); // Описание фильма
        film.setDuration(Duration.ofMinutes(89)); // Продолжительность фильма в минутах
        film.setReleaseDate(LocalDate.of(2014, 8, 21)); // Дата выпуска фильма в прокат
        client = HttpClient.newBuilder().build(); // Инициализируем клиента для отпавки запроса на сервер
    }

    @AfterEach
    void stopServer() {
        SpringApplication.exit(context); // останавливаем работу нашего web приложения
    }

    // Метод тестирует создание фильма со всеми правильными параметрами
    @Test
    public void createWhenAllParamsIsGood() throws ValidationException {
        filmController.create(film); // Создаем/добавляем фильм
        assertEquals(film, filmController.getAllFilms().get(1),
                "Фильм добавленный будет тот же самый, что и в мапе");
    }

    // Метод тестирует выбрасывание исключения если film = null
    @Test
    public void createWhenFilmIsNull() {
        film = null;
        ValidationException exception = Assertions.assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                filmController.create(film);
            }
        });
        assertEquals("400 BAD_REQUEST", exception.getMessage(),
                "Фильм не должен быть создан/добавлен");
    }

    // Метод тестирует то, что не может быть создан фильм с пустым названием
    @Test
    public void createWhenNameIsEmpty() throws ValidationException {
        film.setName("");
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(gson.toJson(film));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .header("Content-Type", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .POST(body)
                .build();
        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
            assertTrue(filmController.getAllFilms().isEmpty(), "Мапа с фильмами должна быть пустой");
        } catch (IOException | InterruptedException e) {
            throw new ValidationException("Запрос завершился с ошибкой при попытке добавить фильм с пустым именем");
        }
    }

    // Метод тестирует выбрасывание исключения если продолжительность фильма не положительная
    @Test
    public void createWhenDurationOfFilmLessOrEqualZero() {
        film.setDuration(Duration.ZERO); // Устанавливаем продолжительность фильма равную нулю
        ValidationException exception = Assertions.assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                filmController.create(film);
            }
        });
        assertEquals("Продолжительность фильма должна быть больше 0", exception.getMessage(),
                "Фильм не должен быть создан/добавлен, так как ошибки с продолжительностью");
        assertTrue(filmController.getAllFilms().isEmpty(), "Мапа с фильмами должна быть пустой");
    }

    // Метод тестирует, что описание фильма не может быть больше, чем 200 символов
    @Test
    public void createWhenDescriptionIsMoreThan200Char() throws ValidationException {
        film.setDescription("**************************************************************************************" +
                "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++" +
                "**************************************************************************************************" +
                "++++++++++++++++++++++++++++++++++++++++++++++");
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(gson.toJson(film));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .header("Content-Type", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .POST(body)
                .build();
        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
            assertTrue(filmController.getAllFilms().isEmpty(), "Мапа с фильмами должна быть пустой");
        } catch (IOException | InterruptedException e) {
            throw new ValidationException("Запрос завершился с ошибкой при попытке добавить фильм с описанием " +
                    "больше, чем 200 символов");
        }
    }

    // Метод тестирует, что дата релиза фильма не может быть раньше, чем 28 декабря 1895 год
    @Test
    public void createWhenReleaseDateIsLessThan28121985year() throws ValidationException {
        film.setReleaseDate(LocalDate.of(1800,1,1));
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(gson.toJson(film));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .header("Content-Type", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .POST(body)
                .build();
        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
            assertTrue(filmController.getAllFilms().isEmpty(), "Мапа с фильмами должна быть пустой");
        } catch (IOException | InterruptedException e) {
            throw new ValidationException("Запрос завершился с ошибкой при попытке добавить фильм с датой релиза " +
                    "раньше, чем 28 декабря 1895 года");
        }
    }
     */
}
