package com.kolaps;

import fr.lip6.move.pnml.framework.utils.exception.*;
import sootup.codepropertygraph.cdg.CdgCreator;
import sootup.codepropertygraph.ddg.DdgCreator;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.core.graph.StmtGraph;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.JDynamicInvokeExpr;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.SootClassMemberSignature;
import sootup.core.signatures.SootClassMemberSubSignature;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JrtFileSystemAnalysisInputLocation;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

import java.io.IOException;
import sootup.core.jimple.common.constant.MethodHandle;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Stream;

public class BytecodeParser {
    public JavaView view;
    private String path = null;

    BytecodeParser(String path) {
        this.path = path;
        parseProgram();
    }

    /*private void addClasses(JavaView view)
    {
        // путь к JDK (например, JAVA_HOME/lib)
        String jdkPath = System.getProperty("java.home"); // или указать явно

        view.get

        // добавляем системные классы JDK
        view.getInputLocations().add(
                new JavaClassPathInputLocation(Paths.get(jdkPath))
        );
    }*/

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

    private StmtGraph<?> retrieveMethodCFG(JavaView view, MethodSignature methodSignature) {
        Optional<JavaSootMethod> optMainMethod = view.getMethod(methodSignature);

        if (!optMainMethod.isPresent()) {
            return null;
        }
        SootMethod mainMethod = optMainMethod.get();
        StmtGraph<?> graph = mainMethod.getBody().getStmtGraph();
        return graph;
    }

    public void parseProgram() {
        AnalysisInputLocation inputLocation = new JavaClassPathAnalysisInputLocation(path);
        AnalysisInputLocation stdloc = new JrtFileSystemAnalysisInputLocation();
        List<AnalysisInputLocation> inputlocationList = Arrays.asList(inputLocation,stdloc);
        this.view = new JavaView(inputlocationList);
        JavaClassType mainClass;
        try {
            mainClass = view.getIdentifierFactory().getClassType(getMainClass(this.path));
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return;
        }
        JavaSootClass sootMainClass = null;
        if (!view.getClass(mainClass).isPresent()) {
            System.out.println("Main class not found");
            return;
        }
        sootMainClass = view.getClass(mainClass).get();
        Stream<JavaSootClass> classes = view.getClasses();
        Optional<JavaSootClass> optFoundClass = classes
                .filter(c -> c.getType().equals(mainClass))
                .findFirst();
        if (optFoundClass.isEmpty()) return;
        JavaSootClass foundClass = optFoundClass.get();
        Optional<JavaSootMethod> mainMethod = foundClass.getMethods().stream()
                .filter(m -> m.getName()
                        .equals("main")).findFirst();
        if (mainMethod.isEmpty()) return;
        PointerAnalyzer.startAnalysis(view, mainMethod.get().getSignature(),mainMethod.get());
        StmtGraph<?> graph = retrieveMethodCFG(view, mainMethod.get().getSignature());
        assert graph != null;
        for (Stmt stmt : graph.getStmts()) {
            if (stmt instanceof JAssignStmt) {
                Value rightOP = ((JAssignStmt) stmt).getRightOp();
                if(rightOP instanceof JNewExpr newExp)
                {
                    if(Objects.equals(newExp.getType().getClassName(), "Thread") && Objects.equals(newExp.getType().getPackageName().toString(), "java.lang"))
                    {

                    }

                }
                if (rightOP instanceof JDynamicInvokeExpr) {
                    JDynamicInvokeExpr expr = (JDynamicInvokeExpr) rightOP;
                    MethodSignature sigLambdaMethod = extractLambdaMethod(expr);
                    StmtGraph<?> lam = retrieveMethodCFG(view, sigLambdaMethod);
                    if (lam == null) continue;
                }
            }

        }

    }


    private MethodSignature extractLambdaMethod(JDynamicInvokeExpr expr)
    {
        MethodHandle h = (MethodHandle) expr.getBootstrapArg(1);
        MethodSignature sig = (MethodSignature) h.getReferenceSignature();
        return sig;
    }


}
