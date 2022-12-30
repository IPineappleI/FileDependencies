import Model.FileDependencyMapper;
import Exceptions.CircularDependencyException;

import java.io.IOException;
import java.util.List;

public class Main {
    /**
     * Создаёт экземпляр класса FileDependencyMapper и выводит в консоль результат работы с ним.
     */
    public static void main(String[] args) {
        // Путь к корневой папке. Можно менять для удобства тестирования. По умолчанию "./Root".
        final var rootPath = "./Root";

        FileDependencyMapper fileDependencyMapper;
        try {
            fileDependencyMapper = new FileDependencyMapper(rootPath);
        } catch (IOException | CircularDependencyException e) {
            System.out.println(e.getMessage());
            return;
        } catch (Exception e) {
            System.out.println("Корневая папка не найдена.");
            return;
        }

        System.out.println("Сортированный список файлов:");
        List<String> sortedFileList = fileDependencyMapper.getSortedFileList();
        for (String path : sortedFileList) {
            System.out.println(path);
        }

        System.out.println("\nКонкатенация содержимого файлов в порядке сортированного списка:");
        try {
            System.out.print(fileDependencyMapper.getFileConcatenation());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
