# task-management-system-spring-boot

REST API для управления задачами на Java и Spring Boot. Позволяет создавать, изменять и удалять задачи, назначать исполнителя, переводить задачу по статусам (создана → в работе → завершена) и искать задачи с фильтрацией и пагинацией. Данные хранятся в PostgreSQL.

Проект выполнен в рамках интенсива по Spring Boot от Sorokin School.

## Технологии

- Java 17
- Spring Boot 4.1.1 (Spring Web MVC, Spring Data JPA, Bean Validation)
- PostgreSQL
- Maven
- Docker (для запуска базы данных)

## Функциональность

Задача (`Task`) содержит: `id`, `creatorId`, `assignedUserId`, `status`, `createDateTime`, `deadlineDate`, `priority`, `doneDateTime`.

- Статусы: `CREATED`, `IN_PROGRESS`, `DONE`
- Приоритеты: `LOW`, `MEDIUM`, `HIGH`

### Эндпоинты

| Метод | URL | Описание |
|---|---|---|
| `GET` | `/tasks/{id}` | получить задачу по id |
| `GET` | `/tasks` | поиск задач с фильтрами и пагинацией |
| `POST` | `/tasks` | создать задачу |
| `PUT` | `/tasks/{id}` | обновить задачу |
| `PATCH` | `/tasks/{id}/status?status=...` | изменить статус задачи |
| `DELETE` | `/tasks/{id}` | удалить задачу |
| `POST` | `/tasks/{id}/start` | взять задачу в работу |
| `POST` | `/tasks/{id}/complete` | завершить задачу |

### Поиск с фильтрацией и пагинацией

`GET /tasks` принимает необязательные параметры:

- `creatorId`, `assignedUserId` - фильтр по создателю и исполнителю
- `status`, `priority` - фильтр по статусу и приоритету
- `pageSize` (по умолчанию `10`), `pageNumber` (по умолчанию `0`) - пагинация

Фильтры можно комбинировать, неуказанные параметры игнорируются. Пример:

```
GET /tasks?assignedUserId=2&status=IN_PROGRESS&priority=HIGH&pageSize=5&pageNumber=0
```

### Бизнес-правила

- При создании `id` и `status` передавать нельзя: статус автоматически становится `CREATED`, дата создания проставляется сервером.
- Завершённую задачу (`DONE`) нельзя редактировать через `PUT`. Сначала нужно вернуть её в `IN_PROGRESS`.
- Допустимые переходы статуса: `CREATED → IN_PROGRESS`, `IN_PROGRESS → CREATED`, `DONE → IN_PROGRESS`. Перейти в `DONE` можно только через `/complete`.
- Чтобы взять задачу в работу (`/start`), у неё должен быть назначен исполнитель, а у исполнителя - меньше 5 задач в статусе `IN_PROGRESS`. Количество считается запросом `@Query` в базе данных.
- Чтобы завершить задачу (`/complete`), у неё должны быть назначены исполнитель и дедлайн, а статус - `IN_PROGRESS`.

### Обработка ошибок

Ошибки возвращаются в едином формате:

```json
{
  "message": "Bad request",
  "detailedMessage": "id должен быть пустым.",
  "errorTime": "2026-09-30T20:30:00"
}
```

- `400` - ошибка валидации или нарушение бизнес-правила
- `404` - задача не найдена
- `500` - непредвиденная ошибка сервера

## Структура проекта

```
src/main/java/linx7a/task_management_system/
├── tasks/   - Task, TaskEntity, TaskMapper, TaskController, TaskService,
│              TaskRepository, TaskSearchFilter, Status, Priority
├── web/     - GlobalExceptionHandler, ErrorResponseDto
└── TaskManagementSystemApplication.java   - точка входа
src/main/resources/application.properties  - настройки подключения к БД
```

- `Task` - модель для API, `TaskEntity` - сущность JPA, `TaskMapper` преобразует одно в другое.
- Код организован по функциональным модулям (package-by-feature).
- Ключевые места бизнес-логики и ошибки логируются через SLF4J.

## Как запустить

Требуется JDK 17+, Docker и Maven (или Maven Wrapper из проекта).

**1. Запустить PostgreSQL в контейнере:**

```bash
docker run --name spring-boot -e POSTGRES_PASSWORD=root -p 5434:5432 -v pgdata:/var/lib/postgresql/data -d postgres:16
```

Порт `5434` совпадает с настройкой в `src/main/resources/application.properties`. Если контейнер уже создан, достаточно `docker start spring-boot`.

**2. Задать пароль к базе данных** через переменную окружения `DB_PASSWORD`:

```bash
# Windows (cmd)
set DB_PASSWORD=root

# Linux / macOS
export DB_PASSWORD=root
```

В IntelliJ IDEA переменную можно указать в настройках запуска: Run → Edit Configurations → Environment variables.

**3. Запустить приложение:**

```bash
./mvnw spring-boot:run
```

или запустить класс `TaskManagementSystemApplication` из IDE. Таблица `tasks` создаётся автоматически (`spring.jpa.hibernate.ddl-auto=update`). По умолчанию приложение доступно на `http://localhost:8080`.

Для просмотра данных можно подключиться к базе любым клиентом (например, DBeaver): host `localhost`, port `5434`, database `postgres`, user `postgres`, password - значение из `POSTGRES_PASSWORD`.

## Пример работы

Создание задачи:

```
POST /tasks
Content-Type: application/json

{
  "creatorId": 1,
  "assignedUserId": 2,
  "deadlineDate": "2026-12-31T18:00:00",
  "priority": "HIGH"
}
```

Ответ `201 Created`:

```json
{
  "id": 1,
  "creatorId": 1,
  "assignedUserId": 2,
  "status": "CREATED",
  "createDateTime": "2026-09-30T20:30:00",
  "deadlineDate": "2026-12-31T18:00:00",
  "priority": "HIGH",
  "doneDateTime": null
}
```

Взять задачу в работу: `POST /tasks/1/start` → статус становится `IN_PROGRESS`.

Завершить задачу: `POST /tasks/1/complete` → статус `DONE`, заполняется `doneDateTime`.