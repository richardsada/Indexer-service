
# Indexer-service

Библиотека и простое приложение для индексации файлов по словам.

# Требования

Для запуска проекта необходимо:

* **Java 17+**
* **Maven 3.8+**


# Тестирование и сборка проекта библиотеки

Запустить все тесты можно командой:

```bash
mvn test
```

Для сборки библиотеки выполните:

```bash
mvn clean package
```

или через IDE — через интерфейс Maven:

```text
-кнопка test
-кнопка package
```


скомпилированную библиотеку можете найти по

```text
\lib_index_service\target\lib_index_service-1.0.jar
```

---

# Запуск приложения

Перед запуском приложения импортируйте библиотеку в виде jar в проект

Через IDE(IntelliJ IDEA)
```text
File → Project Structure (Ctrl+Alt+Shift+S)

Libraries → + → Java

Выберите ваш lib_index_service-1.0.jar

Нажмите OK
```
Через maven (pom.xml)

```text
<dependencies>
    <dependency>
        <groupId>ru</groupId>
        <artifactId>lib_index_service</artifactId>
        <version>1.0</version>
        <scope>system</scope>
        <systemPath>${ваш путь до библиотеки}/lib_index_service-1.0.jar</systemPath>
    </dependency>
</dependencies>
```


Запуск приложения:
```bash
mvn exec:java -Dexec.mainClass="ru.app.Main"
```

или через IDE — запустив класс:

```text
ru.app.Main
```

---

# Команды приложения

После запуска доступны команды:

```text
createDir <dir>           создать директорию
createFile <file> <text>  создать файл с текстом
delete <path>             удалить файл/директорию
search <word>             поиск слова
help                      помощь
exit                      выход
```

---

# Пример работы

```text
createDir papa
Директория создана: .\data\papa

createFile papa\child1.txt 123 hello
Файл создан: .\data\papa\child1.txt

createFile noChild.txt hello 1
Файл создан: .\data\noChild.txt

search 1
Найдено в файлах:
  .\data\noChild.txt

search hello
Найдено в файлах:
  .\data\noChild.txt
  .\data\papa\child1.txt

delete noChild.txt
Удалено: .\data\noChild.txt

search hello
Найдено в файлах:
  .\data\papa\child1.txt

```

---

# Замечания

1) Папки удаляются только, если они пустые. Поэтому сначала удаляем все файлы внутри папки
2) Что соответсвует заданию - Папки и файлы необязательно удалять и добавлять через программу. Это можно делать и через проводник и все будет переиндексировано правильно

