package Proc;

import java.util.List;

public class PredictResult {
    private String methodName;
    private String fileName;
    private float probability;

    public PredictResult() {
    }

    public PredictResult(String methodName, String fileName, float probability) {
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

    public float getProbability() {
        return probability;
    }

    public void setProbability(float probability) {
        this.probability = probability;
    }

    public static void main(String[] args) {
        Predict predict = new Predict();
        List<PredictResult> resultList = predict.predict("E:\\MyPaper\\venv\\Scripts\\python.exe", "D:\\tmp_feature\\");
        //System.out.println(resultList);
    }
}
