package Model;

import java.io.*;
import java.util.*;

import Exceptions.CircularDependencyException;

/**
 * Maps file dependencies, gives useful data.
 */
public class FileDependencyMapper {
    private final String rootPath;

    /**
     * The keys are file paths, the values are file dependencies.
     */
    private final Map<String, List<String>> fileDependencyMap = new HashMap<>();

    /**
     * This list is sorted in such a way that the dependent files always come after their dependencies.
     */
    private final List<String> sortedFileList = new LinkedList<>();

    /**
     * Constructs a {@link FileDependencyMapper} in the specified root directory.
     * @param rootPath the root directory.
     * @throws IOException thrown when an attempt to read a file has failed.
     * @throws CircularDependencyException thrown when a circular file dependency has been found.
     */
    public FileDependencyMapper(final String rootPath) throws IOException, CircularDependencyException {
        this.rootPath = rootPath + '/';

        mapFile(new File(rootPath));

        for (String filePath : fileDependencyMap.keySet()) {
            checkDependencies(filePath);
        }
    }

    /**
     * Puts "file path - file dependencies" pairs into {@link FileDependencyMapper#fileDependencyMap}.
     * @param file the file that needs to be mapped.
     * @throws IOException thrown when an attempt to read a file has failed.
     */
    private void mapFile(final File file) throws IOException {
        if (file.isDirectory()) {
            mapDirectory(file);
        } else {
            String filePath = file.getPath().substring(rootPath.length()).replace('\\', '/');
            fileDependencyMap.put(filePath, new LinkedList<>());

            try (var reader = new BufferedReader(new FileReader(file.getPath()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Метод принимает как строчки вида "require ‘<>’" (как в примере в ТЗ), так и строчки вида
                    // "require '<>'".
                    if (line.startsWith("require ") && (line.charAt(8) == '‘' && line.endsWith("’") ||
                            line.charAt(8) == '\'' && line.endsWith("'"))) {
                        fileDependencyMap.get(filePath).add(line.substring(9, line.length() - 1));
                    }
                }
            }
        }
    }

    /**
     * Maps files inside the specified directory.
     * @param directory the directory.
     * @throws IOException thrown when an attempt to read a file has failed.
     */
    private void mapDirectory(final File directory) throws IOException {
        for (final File child : Objects.requireNonNull(directory.listFiles())) {
            mapFile(child);
        }
    }

    /**
     * Contains all files which can cause a circular dependency at the moment.
     */
    private final LinkedList<String> potentialCircularDependencyFiles = new LinkedList<>();

    /**
     * Checks all file dependencies, fills the {@link FileDependencyMapper#sortedFileList}.
     * @param filePath the path of the file which needs to be checked.
     * @throws CircularDependencyException thrown when a circular file dependency has been found.
     */
    private void checkDependencies(final String filePath) throws CircularDependencyException {
        if (sortedFileList.contains(filePath)) {
            return;
        }

        potentialCircularDependencyFiles.add(filePath);

        List<String> dependencies = fileDependencyMap.get(filePath);
        for (String dependency : dependencies) {
            if (potentialCircularDependencyFiles.contains(dependency)) {
                throw new CircularDependencyException("Обнаружена циклическая зависимость в файле '" + filePath + '\'');
            }
            checkDependencies(dependency);
        }

        sortedFileList.add(filePath);

        potentialCircularDependencyFiles.remove(filePath);
    }

    /**
     * Copies the {@link FileDependencyMapper#sortedFileList}.
     * @return the {@link FileDependencyMapper#sortedFileList} copy.
     */
    public List<String> getSortedFileList() {
        return new LinkedList<>(sortedFileList);
    }

    /**
     * Concatenates the files in the order of the {@link FileDependencyMapper#sortedFileList}.
     * @return the concatenation result.
     * @throws IOException thrown when an attempt to read a file has failed.
     */
    public String getFileConcatenation() throws IOException {
        var concatenation = new StringBuilder();
        for (String path : sortedFileList) {
            try (var reader = new BufferedReader(new FileReader(rootPath + path))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    concatenation.append(line).append('\n');
                }
                concatenation.append('\n');
            }
        }
        return concatenation.toString();
    }
}
