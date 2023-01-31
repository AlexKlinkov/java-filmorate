# "java-filmorate" (backend of Web app)

## Description:

Кинопоиск для своих, позволяет:

- Заводить друзей,
- Удалять друзей из списка,
- Получать список общих друзей,
- Оценивать фильмы (ставить like или dislike),
- Оставлять комментарии к фильму,
- Получать список рекомендованных фильмов к просмотру, 
- Выводить n количество самых популярных фильмов и многое другое...

## Schema of BD:

![diagram_of_bd](https://github.com/AlexKlinkov/java-filmorate/blob/main/ER_DIAGRAM_OF_BD.jpg)

## Instruction of launch app:

С помощью IntelliJ IDEA (my version 2022.1)

1. Открываем проект.
2. IntelliJ IDEA сообщит, что "Maven Build Scripts Found", следует нажать "Load".
3. Запускаем команду mvn clean package spring-boot:repackage.
4. Запускаем команду **docker-compose up** в терминале IDEA, убедившись, что находимся в той же директории, что и файл "docker-compose.yml", при этом Docker daemon должен быть запущен.
5. Так же можно загрузить базовую коллекцию ([Ссылка на коллекцию](https://github.com/AlexKlinkov/java-filmorate/blob/main/SET_OF_REQUESTS.postman_collection.json)) в Postman и подергать разные ручки, посмотреть как работает приложение.

## Participation list:

- Алексей: https://github.com/AlexMaxpower
- Екатерина: https://github.com/Katibat
- Лилия: https://github.com/lilyerma
- Кирилл: https://github.com/krllrn

## Technology stack:

- Java 11.0.15
- Spring Boot 2.7.2
- Maven 4.0.0
- Lombok 1.18.24
- PostgreSQL 14.5
- JDBC 2.4.2
- Docker 3.8
- JUnit 5.0

## Future plans:

- Написать тесты (слои: сервисы и контроллеры)
