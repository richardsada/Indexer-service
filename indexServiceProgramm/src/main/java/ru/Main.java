package ru;

import ru.indexService.IndexService;
import ru.indexService.IndexServiceImpl;
import ru.indexer.WordIndexer;
import ru.observer.Observer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Set;

public class Main {

    private static final Path ROOT_DIR = Paths.get("./data");

    public static void main(String[] args) {

        IndexService indexService = new IndexServiceImpl(new WordIndexer());
        Observer observer = new Observer(indexService);

        try {
            if (!Files.exists(ROOT_DIR)) {
                Files.createDirectories(ROOT_DIR);
            }
            indexService.addDir(ROOT_DIR);
            observer.registerAll(ROOT_DIR);
            observer.start();

            System.out.println("Индексируемая директория: " + ROOT_DIR.toAbsolutePath());

        } catch (Exception e) {
            System.out.println("Ошибка запуска: " + e.getMessage());
            return;
        }

        printHelp();
        Scanner scanner = new Scanner(System.in);

        while (true) {

            System.out.print("\n");
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) continue;

            String[] parts = line.split(" ");
            String command = parts[0];

            try {
                switch (command) {

                    case "createDir":
                        createDirectory(parts);
                        break;

                    case "createFile":
                        createFile(parts);
                        break;

                    case "delete":
                        deletePath(parts);
                        break;

                    case "search":
                        search(indexService, parts);
                        break;

                    case "help":
                        printHelp();
                        break;

                    case "exit":
                        observer.stop();
                        return;

                    default:
                        System.out.println("Неизвестная команда");
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage() + e.getCause());
            }
        }
    }

    private static void createDirectory(String[] parts) throws Exception {

        if (parts.length != 2) {
            System.out.println("createDir <dir>");
            return;
        }

        Path dir = ROOT_DIR.resolve(parts[1]);
        Files.createDirectories(dir);

        System.out.println("Директория создана: " + dir);
    }

    private static void createFile(String[] parts) throws Exception {

        if (parts.length < 3) {
            System.out.println("createFile <file> <text>");
            return;
        }

        Path file = ROOT_DIR.resolve(parts[1]);

        if (!Files.exists(file.getParent())) {
            Files.createDirectories(file.getParent());
        }
        String text = String.join(" ", Arrays.copyOfRange(parts, 2, parts.length));
        Files.writeString(file, text);

        System.out.println("Файл создан: " + file);
    }

    private static void deletePath(String[] parts) throws Exception {

        if (parts.length != 2) {
            System.out.println("delete <path>");
            return;
        }

        Path path = ROOT_DIR.resolve(parts[1]);

        Files.deleteIfExists(path);

        System.out.println("Удалено: " + path);
    }

    private static void search(IndexService indexService, String[] parts) {

        if (parts.length != 2) {
            System.out.println("search <word>");
            return;
        }

        Set<Path> result = indexService.search(parts[1]);

        if (result.isEmpty()) {
            System.out.println("Ничего не найдено");
        } else {
            System.out.println("Найдено в файлах:");
            result.forEach(p -> System.out.println("  " + p));
        }
    }

    private static void printHelp() {

        System.out.println("""
                
                Команды:
                
                createDir <dir>           создать директорию
                createFile <file> <text>  создать файл с текстом
                delete <path>             удалить файл/директорию
                search <word>             поиск слова
                help                      помощь
                exit                      выход
                
                Все файлы создаются внутри\t""" + ROOT_DIR + "\n");
    }
}