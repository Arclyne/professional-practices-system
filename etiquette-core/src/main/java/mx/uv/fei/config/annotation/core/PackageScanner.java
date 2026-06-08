package mx.uv.fei.config.annotation.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.module.ModuleReader;
import java.lang.module.ResolvedModule;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


public class PackageScanner {

    private static final Logger LOG = LoggerFactory.getLogger(PackageScanner.class);

    public static List<Class<?>> findClasses(String targetPackageName) throws ClassNotFoundException, IOException {
        List<Class<?>> discoveredClasses = new ArrayList<>();

        discoveredClasses.addAll(findClassesFromClassLoader(targetPackageName));
        discoveredClasses.addAll(findClassesFromModuleLayer(targetPackageName));

        return discoveredClasses;
    }

    private static List<Class<?>> findClassesFromClassLoader(String targetPackageName) throws ClassNotFoundException, IOException {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        String targetPackagePath = targetPackageName.replace('.', '/');
        Enumeration<URL> packageResources = contextClassLoader.getResources(targetPackagePath);
        List<File> systemDirectories = new ArrayList<>();

        while (packageResources.hasMoreElements()) {
            URL currentPackageResource = packageResources.nextElement();
            String decodedPath = URLDecoder.decode(currentPackageResource.getFile(), StandardCharsets.UTF_8);
            systemDirectories.add(new File(decodedPath));
        }

        List<Class<?>> discoveredClasses = new ArrayList<>();

        for (File currentDirectory : systemDirectories) {
            discoveredClasses.addAll(findClassesInDirectory(currentDirectory, targetPackageName));
        }

        return discoveredClasses;
    }

    private static List<Class<?>> findClassesFromModuleLayer(String targetPackageName) {
        List<Class<?>> discoveredClasses = new ArrayList<>();
        String packagePrefix = targetPackageName + ".";

        for (ResolvedModule resolvedModule : ModuleLayer.boot().configuration().modules()) {
            try {
                ModuleReader reader = resolvedModule.reference().open();
                List<String> classNames = collectClassNamesFromModule(reader, packagePrefix);

                for (String className : classNames) {
                    loadModuleClass(className, discoveredClasses);
                }

                reader.close();
            } catch (IOException e) {
                LOG.warn("No se pudo leer el modulo: {}", resolvedModule.name(), e);
            }
        }

        return discoveredClasses;
    }

    private static List<String> collectClassNamesFromModule(ModuleReader reader, String packagePrefix) throws IOException {
        List<String> classNames = new ArrayList<>();
        String packagePath = packagePrefix.replace('.', '/');

        reader.list()
                .filter(resource -> resource.startsWith(packagePath) && resource.endsWith(".class") && !resource.contains("$"))
                .map(resource -> resource.replace('/', '.').substring(0, resource.length() - 6))
                .forEach(classNames::add);

        return classNames;
    }

    private static void loadModuleClass(String className, List<Class<?>> discoveredClasses) {
        try {
            Class<?> loadedClass = Class.forName(className, false, Thread.currentThread().getContextClassLoader());
            discoveredClasses.add(loadedClass);
        } catch (ClassNotFoundException e) {
            LOG.warn("Clase del modulo no accesible via ClassLoader, se omite: {}", className);
        } catch (LinkageError e) {
            LOG.error("Fallo critico de dependencias al cargar la clase del modulo: {}", className, e);
        }
    }

    private static List<Class<?>> findClassesInDirectory(File currentDirectory, String targetPackageName) throws ClassNotFoundException {
        List<Class<?>> discoveredClasses = new ArrayList<>();

        if (!currentDirectory.exists()) {
            LOG.warn("El directorio no existe o el sistema lo ha encapsulado: {}", currentDirectory.getAbsolutePath());
        } else {
            File[] directoryFiles = currentDirectory.listFiles();

            if (directoryFiles != null) {
                processDirectoryFiles(directoryFiles, targetPackageName, discoveredClasses);
            }
        }

        return discoveredClasses;
    }

    private static void processDirectoryFiles(
            File[] directoryFiles,
            String targetPackageName,
            List<Class<?>> discoveredClasses) throws ClassNotFoundException {

        for (File currentFile : directoryFiles) {
            if (currentFile.isDirectory()) {
                discoveredClasses.addAll(findClassesInDirectory(currentFile, targetPackageName + "." + currentFile.getName()));
            } else if (isValidClassFile(currentFile)) {
                discoveredClasses.add(loadClassFromFile(currentFile, targetPackageName));
            }
        }
    }

    private static boolean isValidClassFile(File file) {
        return file.getName().endsWith(".class") && !file.getName().contains("$");
    }

    private static Class<?> loadClassFromFile(File currentFile, String targetPackageName) throws ClassNotFoundException {
        String targetClassName = targetPackageName + '.' + currentFile.getName().substring(0, currentFile.getName().length() - 6);
        Class<?> loadedClass;

        try {
            loadedClass = Class.forName(targetClassName, false, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            LOG.error("El escaner no pudo encontrar la clase en tiempo de ejecucion: {}", targetClassName, e);
            throw new ClassNotFoundException("El escaner no pudo encontrar la clase en tiempo de ejecucion: " + targetClassName, e);
        } catch (LinkageError e) {
            LOG.error("Fallo critico de dependencias al intentar cargar la clase: {}", targetClassName, e);
            throw new IllegalStateException("Fallo critico de dependencias al intentar cargar la clase: " + targetClassName, e);
        }

        return loadedClass;
    }
}
