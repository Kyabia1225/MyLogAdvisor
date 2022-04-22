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

}
