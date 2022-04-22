package FeatureExtraction;

public class Feature {
    private String directory;
    private String methodName;
    private String errType;
    private int containingMethodsNum;
    private int invokedMethodsNum;
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

    public String getErrType() {
        return errType;
    }

    public void setErrType(String errType) {
        this.errType = errType;
    }

    public int getContainingMethodsNum() {
        return containingMethodsNum;
    }

    public void setContainingMethodsNum(int containingMethodsNum) {
        this.containingMethodsNum = containingMethodsNum;
    }

    public int getInvokedMethodsNum() {
        return invokedMethodsNum;
    }

    public void setInvokedMethodsNum(int invokedMethodsNum) {
        this.invokedMethodsNum = invokedMethodsNum;
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
}
