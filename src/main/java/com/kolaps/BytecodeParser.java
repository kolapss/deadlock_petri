package com.kolaps;

import sootup.core.graph.StmtGraph;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class BytecodeParser {

    private String path=null;

    BytecodeParser(String path)
    {
        this.path = path;
        createCFG();
    }

    private String getMainClass(String jarFilePath) throws IOException {
        try (JarFile jarFile = new JarFile(jarFilePath)) {
            Manifest manifest = jarFile.getManifest();
            if (manifest != null) {
                Attributes attributes = manifest.getMainAttributes();
                return attributes.getValue("Main-Class");
            }
        }
        return null;
    }

    public void createCFG()
    {
        AnalysisInputLocation inputLocation = new JavaClassPathAnalysisInputLocation(path);
        JavaView view = new JavaView(inputLocation);
        JavaClassType mainClass;
        try {
            mainClass = view.getIdentifierFactory().getClassType(getMainClass(this.path));
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return;
        }
        JavaSootClass sootMainClass;
        if(!view.getClass(mainClass).isPresent()){
            System.out.println("Main class not found");
            return;
        }
        sootMainClass = view.getClass(mainClass).get();

        MethodSignature mainMethodSignature =
                view
                        .getIdentifierFactory()
                        .getMethodSignature(
                                "main", // method name
                                mainClass.getClassName(),
                                "void", // return type
                                Collections.singletonList("java.lang.String[]")); // args

        Optional<JavaSootMethod> optMainMethod = view.getMethod(mainMethodSignature);

        if(!optMainMethod.isPresent()){
            return;
        }
        SootMethod mainMethod = optMainMethod.get();
        StmtGraph<?> graph = mainMethod.getBody().getStmtGraph();

    }



}
