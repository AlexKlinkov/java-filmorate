# Название: java-filmorate

## Описание/Функциональность

Кинопоиск для своих, позволяет:

- Заводить друзей,
- Удалять друзей из списка,
- Получать список общих друзей,
- Оценивать фильмы (ставить like или dislike),
- Оставлять комментарии к фильму,
- Получать список рекомендованных фильмов к просмотру, 
- Выводить n количество самых популярных фильмов.

## Требования к приложению (Спецификация) 
### API для Swagger :

Главный сервер: [ewm-main-service-spec.json](https://github.com/AlexKlinkov/explore-with-me/blob/main/ewm-main-service-spec.json)

Сервер статистики: [ewm-stats-service-spec.json](https://github.com/AlexKlinkov/explore-with-me/blob/main/ewm-stats-service-spec.json)

## Запуск приложения:

С помощью IntelliJ IDEA

1. Открываем проект
2. Запускаем команду **mvn clean install** в Maven
3. Запускаем команду **docker-compose up** в терминале IDEA (Docker daemon должен быть запущен)

## Стек технологий

- Spring Boot 2.6.7
- Maven
- MapStruct 1.5.2
- Lombok 1.18.20
- Gson 2.9.0
- JDBC
- H2database

## Список участников:


![ER-diagram-4](https://user-images.githubusercontent.com/97181431/173665710-7f3dfd96-7eaa-46a0-a1c4-e6a5c9398c7a.png)
