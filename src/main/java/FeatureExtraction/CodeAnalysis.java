package FeatureExtraction;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.SourceRoot;
import org.checkerframework.checker.units.qual.A;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class CodeAnalysis {
    public static boolean containsIgnoreCase(String cmp, String...val) {
        cmp = cmp.toLowerCase(Locale.ROOT);
        for(String c:val){
            if(cmp.contains(c.toLowerCase(Locale.ROOT))){
                return true;
            }
        }
        return false;
    }
    public static void main(String[] args) {

        String path = "E:\\MyPaper\\hadoop\\hadoop-hdfs-project\\hadoop-hdfs-client\\src\\main\\java\\org\\apache\\hadoop\\hdfs";
        //ProjectRoot projectRoot = new ParserCollectionStrategy().collect(Paths.get(path));
        SourceRoot sourceRoot = new SourceRoot(Paths.get(path));
        CompilationUnit cu = sourceRoot.parse("", "BlockReader.java");

        ArrayList<String> classNameList = new ArrayList<>();
        //read all className
        cu.accept(new VoidVisitorAdapter<List<String>>() {
            @Override
            public void visit(ClassOrInterfaceDeclaration cORid, List<String> classNameList) {
                classNameList.add(cORid.getNameAsString());
                super.visit(cORid, classNameList);
            }
        }, classNameList);

        List<Feature>features = new ArrayList<>();
        //split code block
        for(String className:classNameList) {
            Optional<ClassOrInterfaceDeclaration> clazz = cu.getClassByName(className);
            if(clazz.isPresent()) {
                //get all methods
                List<MethodDeclaration> methods = clazz.get().getMethods();
                for(MethodDeclaration method:methods) {
                    Feature feature = new Feature();
                    feature.setDirectory(cu.getStorage().get().getDirectory().toString());
                    feature.setMethodName(method.getNameAsString());
                    String methodCodeText = method.getDeclarationAsString(false, true, true)
                            +"\n" + method.getBody().get() + "\n";
                    feature.setSourceCodeText(methodCodeText);
                    Counter tryCatchBlockNum = new Counter(0);
                    //visit try stmt
                    method.accept(new ModifierVisitor<Counter>() {
                        @Override
                        public Visitable visit(TryStmt stmt, Counter tryCatchBlockNum) {
                            tryCatchBlockNum.count();
                            NodeList<CatchClause> catchClauses = stmt.getCatchClauses();
                            for(CatchClause catchClause:catchClauses) {
                                feature.addErrType(catchClause.getParameter().getTypeAsString());
                            }
                            return super.visit(stmt, tryCatchBlockNum);
                        }
                    }, tryCatchBlockNum);
                    feature.setTryCatchBlockNum(tryCatchBlockNum.getTimes());
                    //visit whether there is an assignment stmt with an assigned special value
                    method.accept(new VoidVisitorAdapter<Void>() {
                        @Override
                        public void visit(AssignExpr expr, Void arg) {
                            String value = expr.getValue().toString();
                            if(containsIgnoreCase(value, "null", "empty", "-1", "fail", "exit", "warn")) {
                                feature.setSettingFlag(true);
                            }
                        }
                    }, null);
                    feature.setHasThrow(!method.getThrownExceptions().isEmpty());
                    method.accept(new ModifierVisitor<Void>() {
                        @Override
                        public Visitable visit(ReturnStmt stmt, Void arg) {
                            if(stmt.getExpression().isPresent()) {
                                Expression expression = stmt.getExpression().get();
                                if(containsIgnoreCase(expression.toString(), "null", "empty", "-1", "fail", "0", "exit", "warn")) {
                                    feature.setReturnSpecialValue(true);
                                }
                            } else {
                                //optional expression is not present, which means it returns null here
                                feature.setReturnSpecialValue(true);
                            }
                            return super.visit(stmt, arg);
                        }
                    }, null);
                    feature.setLogged(feature.getSourceCodeText().toLowerCase(Locale.ROOT).contains("log."));
                    Counter containingMethodsCounter = new Counter(0);
                    method.accept(new VoidVisitorAdapter<Counter>() {
                        @Override
                        public void visit(MethodCallExpr methodCallExpr, Counter counter) {
                            counter.count();
                            //System.out.println(methodCallExpr.getName());
                            //todo: containing methods' name
                        }
                    }, containingMethodsCounter);
                    feature.setContainingMethodsNum(containingMethodsCounter.getTimes());
                    features.add(feature);
                }
            }
        }
        System.out.println(features.size());

    }
}
