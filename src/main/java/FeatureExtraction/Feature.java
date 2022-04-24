package FeatureExtraction;

import com.github.javaparser.ast.body.MethodDeclaration;

import java.util.ArrayList;
import java.util.List;

public class Feature {
    private String directory;
    private String methodName;
    private List<String> errType;
    private int containingMethodsNum;
    private String sourceCodeText;
    private boolean settingFlag;
    private boolean hasThrow;
    private boolean returnSpecialValue;
    private int tryCatchBlockNum;
    private boolean logged; //label
    public Feature(){}

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<String> getErrType() {
        return errType;
    }

    public void setErrType(List<String> errType) {
        this.errType = errType;
    }

    public void addErrType(String errType) {
        if(this.errType == null) {
            this.errType = new ArrayList<String>();
        }
        this.errType.add(errType);
    }

    public int getContainingMethodsNum() {
        return containingMethodsNum;
    }

    public void setContainingMethodsNum(int containingMethodsNum) {
        this.containingMethodsNum = containingMethodsNum;
    }

    public String getSourceCodeText() {
        return sourceCodeText;
    }

    public void setSourceCodeText(String sourceCodeText) {
        this.sourceCodeText = sourceCodeText;
    }

    public boolean isSettingFlag() {
        return settingFlag;
    }

    public void setSettingFlag(boolean settingFlag) {
        this.settingFlag = settingFlag;
    }

    public boolean isHasThrow() {
        return hasThrow;
    }

    public void setHasThrow(boolean hasThrow) {
        this.hasThrow = hasThrow;
    }

    public boolean isReturnSpecialValue() {
        return returnSpecialValue;
    }

    public void setReturnSpecialValue(boolean returnSpecialValue) {
        this.returnSpecialValue = returnSpecialValue;
    }

    public int getTryCatchBlockNum() {
        return tryCatchBlockNum;
    }

    public void setTryCatchBlockNum(int tryCatchBlockNum) {
        this.tryCatchBlockNum = tryCatchBlockNum;
    }

    public boolean isLogged() {
        return logged;
    }

    public void setLogged(boolean logged) {
        this.logged = logged;
    }

    @Override
    public String toString() {
        return "Feature{" +
                "directory='" + directory + '\'' +
                ", methodName='" + methodName + '\'' +
                ", errType=" + errType +
                ", containingMethodsNum=" + containingMethodsNum +
                ", sourceCodeText='" + sourceCodeText + '\'' +
                ", settingFlag=" + settingFlag +
                ", hasThrow=" + hasThrow +
                ", returnSpecialValue=" + returnSpecialValue +
                ", tryCatchBlockNum=" + tryCatchBlockNum +
                ", logged=" + logged +
                '}';
    }
}

class Counter {
    private int times;

    public int getTimes() {
        return times;
    }

    public void count() {
        this.times++;
    }

    public Counter(int times) {
        this.times = times;
    }
}