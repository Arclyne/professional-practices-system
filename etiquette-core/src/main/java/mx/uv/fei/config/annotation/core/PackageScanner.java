package mx.uv.fei.config.annotation.core;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class PackageScanner {

    public static List<Class<?>> findClasses(String targetPackageName) throws ClassNotFoundException, IOException {
        ClassLoader systemClassLoader = Thread.currentThread().getContextClassLoader();
        String targetPackagePath = targetPackageName.replace('.', '/');
        Enumeration<URL> packageResourcesEnumeration = systemClassLoader.getResources(targetPackagePath);
        List<File> systemDirectoriesList = new ArrayList<>();

        while (packageResourcesEnumeration.hasMoreElements()) {
            URL currentPackageResource = packageResourcesEnumeration.nextElement();
            systemDirectoriesList.add(new File(currentPackageResource.getFile()));
        }

        List<Class<?>> discoveredClassesList = new ArrayList<>();
        for (File currentDirectory : systemDirectoriesList) {
            discoveredClassesList.addAll(findClasses(currentDirectory, targetPackageName));
        }

        return discoveredClassesList;
    }

    private static List<Class<?>> findClasses(File currentDirectoryToScan, String targetPackageName) throws ClassNotFoundException {
        List<Class<?>> discoveredClassesList = new ArrayList<>();

        if (currentDirectoryToScan.exists()) {
            File[] directoryFilesArray = currentDirectoryToScan.listFiles();

            if (directoryFilesArray != null) {
                for (File currentFile : directoryFilesArray) {
                    if (currentFile.isDirectory()) {
                        discoveredClassesList.addAll(findClasses(currentFile, targetPackageName + "." + currentFile.getName()));
                    } else if (currentFile.getName().endsWith(".class") && !currentFile.getName().contains("$")) {
                        String targetClassName = targetPackageName + '.' + currentFile.getName().substring(0, currentFile.getName().length() - 6);

                        try {
                            discoveredClassesList.add(Class.forName(targetClassName));
                        } catch (ClassNotFoundException classNotFoundException) {
                            throw new ClassNotFoundException("El escaner de paquetes no pudo encontrar la clase solicitada en tiempo de ejecucion: " + targetClassName, classNotFoundException);
                        } catch (LinkageError linkageError) {
                            throw new IllegalStateException("Fallo critico de dependencias al intentar cargar la clase solicitada: " + targetClassName, linkageError);
                        }
                    }
                }
            }
        }

        return discoveredClassesList;
    }
}