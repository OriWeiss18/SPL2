package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents a list of detected objects with a timestamp.
 */
public class StampedDetectedObject {

    private int time; 
    private List<DetectedObject> detectedObjects;  

    public StampedDetectedObject(int time, List<DetectedObject> detectedObjects) {
        this.time = time;
        this.detectedObjects = detectedObjects;
    }

    public int getTime() {
        return time;
    }  

    public List<DetectedObject> getDetectedObjects() {
        return detectedObjects;
    }

    public void addDetectedObject(DetectedObject detectedObject) {
        this.detectedObjects.add(detectedObject);
    }

}
