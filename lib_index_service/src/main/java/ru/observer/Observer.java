package ru.observer;

import ru.indexService.IndexService;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class Observer {

    private final WatchService watchService;
    private final Map<WatchKey, Path> watchKeys = new HashMap<>();
    private final IndexService indexService;

    private volatile boolean running = true;

    public Observer(IndexService indexService) {
        this.indexService = indexService;
        try {
            this.watchService = FileSystems.getDefault().newWatchService();

        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать WatchService", e);
        }
    }


    public void registerAll(Path start) {
        try (Stream<Path> stream = Files.walk(start)) {
            stream.filter(Files::isDirectory)
                    .forEach(dir -> {
                        try {
                            register(dir);

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("Ошибка регистрации директорий " + start, e);
        }
    }


    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(
                watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY
        );
        watchKeys.put(key, dir);
    }


    public void start() {
        Thread watcherThread = new Thread(this::processEvents);
        watcherThread.setDaemon(true);
        watcherThread.start();
    }


    private void processEvents() {
        while (running) {
            WatchKey key;
            try {
                key = watchService.take();

            } catch (InterruptedException | ClosedWatchServiceException e) {
                return;
            }

            Path dir = watchKeys.get(key);
            if (dir == null) {
                key.reset();
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {

                WatchEvent.Kind<?> kind = event.kind();
                Path fileName = (Path) event.context();
                Path fullPath = dir.resolve(fileName);

                handleEvent(kind, fullPath);
            }
            boolean valid = key.reset();
            if (!valid) {
                watchKeys.remove(key);
            }
        }
    }

    private void handleEvent(WatchEvent.Kind<?> kind, Path path) {
        try {
            if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                if (Files.isDirectory(path)) {
                    registerAll(path);
                    try (Stream<Path> stream = Files.walk(path)) {
                        stream.filter(Files::isRegularFile)
                                .forEach(indexService::addFile);
                    }

                } else if (Files.exists(path)) {
                    indexService.addFile(path);
                }

            } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                if (Files.exists(path) && Files.isRegularFile(path)) {
                    indexService.removeFile(path);
                    indexService.addFile(path);
                }
            } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                indexService.removeFile(path);
            }
        }  catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void stop() {
        running = false;
        try {
            watchService.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}