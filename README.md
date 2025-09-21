# Java-Filmorate

Template repository for Filmorate project.

## ER Diagram for a Filmorate Database

![image](/adsbvb/java-filmorate/blob/add_database_ER-diagram/ER%20diagram.png)

## Примеры SQL-запросов для основных операций

### Получение всех фильмов
```sql 
SELECT f.id, f.title, f.description, f.release_date, f.duration, r.name 
FROM Films AS f
JOIN MPA_Rating AS r ON r.rating_id=f.mpa_rating_id;
```  

### Получение всех пользователей
```sql
SELECT *
FROM Users;
```
