package FeatureExtraction;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.SourceRoot;
import org.checkerframework.checker.units.qual.A;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CodeAnalysis {
    public static void main(String[] args) {
        String path = "E:\\MyPaper\\hadoop\\hadoop-yarn-project\\hadoop-yarn\\hadoop-yarn-client\\src\\main\\java\\org\\apache\\hadoop\\yarn\\client\\cli";
        //ProjectRoot projectRoot = new ParserCollectionStrategy().collect(Paths.get(path));
        SourceRoot sourceRoot = new SourceRoot(Paths.get(path));
        CompilationUnit cu = sourceRoot.parse("", "ClusterCLI.java");

        ArrayList<String> classNameList = new ArrayList<>();
        //read all className
        cu.accept(new VoidVisitorAdapter<List<String>>() {
            @Override
            public void visit(ClassOrInterfaceDeclaration cORid, List<String> classNameList) {
                classNameList.add(cORid.getNameAsString());
                super.visit(cORid, classNameList);
            }
        }, classNameList);
        List<Feature> features = new ArrayList<>();
        //split code block
        for(String className:classNameList) {
            Optional<ClassOrInterfaceDeclaration> clazz = cu.getClassByName(className);
            if(clazz.isPresent()) {
                List<MethodDeclaration> methods = clazz.get().getMethods();
                for(MethodDeclaration method:methods) {
                    String methodCodeText = method.getDeclarationAsString(false, true, true)
                            +"\n" + method.getBody().get() + "\n";
                    method.accept(new ModifierVisitor<Void>() {
                        @Override
                        public Visitable visit(TryStmt stmt, Void arg) {
                            System.out.println(stmt.getCatchClauses());
                            System.out.println(method.getThrownExceptions());
                            return super.visit(stmt, arg);
                        }
                    }, null);
                    Feature feature = new Feature();
                    feature.setDirectory(cu.getStorage().get().getDirectory().toString());
                    feature.setMethodName(method.getNameAsString());
                    feature.setSourceCodeText(methodCodeText);
                    features.add(feature);
                }
            }
        }

//        JavaParser javaParser = new JavaParser();
//        for(Feature feature:features) {
           // System.out.println(javaParser.parse(feature.getSourceCodeText()).getResult());
//            methodCU.accept(new ModifierVisitor<Void>(){
//                @Override
//                public Visitable visit(TryStmt stmt, Void arg) {
//                    System.out.println(stmt.getCatchClauses().toString());
//                    return super.visit(stmt, arg);
//                }
//            }, null);
        //}

    }
}
