# Название: "java-filmorate" (backend of Web app)

## Описание/Функциональность

Кинопоиск для своих, позволяет:

- Заводить друзей,
- Удалять друзей из списка,
- Получать список общих друзей,
- Оценивать фильмы (ставить like или dislike),
- Оставлять комментарии к фильму,
- Получать список рекомендованных фильмов к просмотру, 
- Выводить n количество самых популярных фильмов и многое другое...

## Схема реляционной базы данных проекта

![diagram_of_bd](https://github.com/AlexKlinkov/java-filmorate/blob/main/ER_DIAGRAM_OF_BD.jpg)

## Запуск приложения:

С помощью IntelliJ IDEA

1. Открываем проект
2. Запускаем команду mvn clean package spring-boot:repackage
3. Запускаем команду docker-compose up в терминале IDEA (Docker daemon должен быть запущен)
4. Так же можно, загрузить базовую коллекцию (![Ссылка на коллекцию](https://github.com/AlexKlinkov/java-filmorate/blob/main/SET_OF_REQUESTS.postman_collection.json)) в Postman и подергать разные ручки, посмотреть как работает приложение.

## Список участников:

- Алексей: https://github.com/AlexMaxpower
- Екатерина: https://github.com/Katibat
- Лилия: https://github.com/lilyerma
- Кирилл: https://github.com/krllrn

## Стек технологий

- Spring Boot 2.7.2
- PostgreSQL
- JDBC
- Maven
- Docker
- Lombok
- JUnit

## Планы на будущее

- Написать тесты (слои: сервисы и контроллеры)
