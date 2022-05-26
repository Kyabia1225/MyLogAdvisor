package Proc;


public class PredictResult {
    private String methodName;
    private String fileName;
    private Float probability;

    public PredictResult() {
    }

    public PredictResult(String methodName, String fileName, Float probability) {
        this.methodName = methodName;
        this.fileName = fileName;
        this.probability = probability;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Float getProbability() {
        return probability;
    }

    public void setProbability(Float probability) {
        this.probability = probability;
    }
}
