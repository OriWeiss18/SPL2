package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a LiDar worker on the robot.
 * Responsible for tracking objects in the environment at regular intervals.
 */
public class LiDarWorkerTracker {

    private int id;
    private int frequency;
    private STATUS status;
    private List<TrackedObject> lastTrackedObjects;
    private LiDarDataBase liDarDataBase;
    private int currentTick = 0;
    private int maxTime;
    

    public LiDarWorkerTracker(int id, int frequency, String lidarDataFilePath) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.lastTrackedObjects = new ArrayList<>();
        this.liDarDataBase = LiDarDataBase.getInstance(lidarDataFilePath); 
        this.maxTime = calculateMaxTime(); 
        
    }
    public LiDarWorkerTracker (int id, int frequency, String lidarDataFilePath, int maxTime){
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.lastTrackedObjects = new ArrayList<>();
        this.liDarDataBase = LiDarDataBase.getInstance(lidarDataFilePath); 
        this.maxTime = maxTime;
    }
    

    public int getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public STATUS getStatus() {
        return status;
    }
    
    public int getCurrentTick() {
        return currentTick;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }


    public List<TrackedObject> getLastTrackedObjects() {
        return lastTrackedObjects;
    }

    public List<StampedCloudPoints> getLiDarData() {
        return liDarDataBase.getCloudPoints();
    }

    public void setLastTrackedObjects (List<TrackedObject> newlist){ 
        this.lastTrackedObjects = newlist;
    }

    public List<CloudPoint> getCoordinates(String id, int time) {
        List<StampedCloudPoints> cloudPointsList = liDarDataBase.getCloudPoints();
        for (StampedCloudPoints stampedCloudPoints : cloudPointsList) {
            if (stampedCloudPoints.getId().equals(id) && stampedCloudPoints.getTime() == time) {
                liDarDataBase.decrementCounter();
                if (liDarDataBase.getCounter() == 0) {
                    setStatus(STATUS.DOWN);
                }
                return stampedCloudPoints.listToCloudPoints();
            }
        }
        return new ArrayList<>();
    }
    
    
    

    public void checkForErrorInCloudPointsAtTime(int time) {
        List<StampedCloudPoints> cloudPointsList = liDarDataBase.getCloudPoints();
        for (StampedCloudPoints stampedCloudPoints : cloudPointsList) {
            if ("ERROR".equals(stampedCloudPoints.getId()) && stampedCloudPoints.getTime() == time) {
                setStatus(STATUS.ERROR);
                break;
            }
        }
    }
    
    
    

    public List<TrackedObject> prosseingEvent(StampedDetectedObject stampedDetectedObjects) {
        List<TrackedObject> trackedObjectsToReturn = new ArrayList<>();
        int detectionTime = stampedDetectedObjects.getTime();
        List<DetectedObject> detectedObjects = stampedDetectedObjects.getDetectedObjects();
        checkForErrorInCloudPointsAtTime(detectionTime);
        if (this.status == STATUS.UP){
            for (DetectedObject detectedObject : detectedObjects){
                TrackedObject trackedObject = new TrackedObject(
                    detectedObject.getId(),
                    detectionTime,
                    detectedObject.getDescription(),
                    getCoordinates(detectedObject.getId(), detectionTime)
                    );
                    trackedObjectsToReturn.add(trackedObject);
            }
        }
        
        return trackedObjectsToReturn;
    }

    public void updateTick(int time) {
        this.currentTick = time;
        if (currentTick >= maxTime) {
            this.status = STATUS.DOWN;
        }
    }

    private int calculateMaxTime() {
        return liDarDataBase.getCloudPoints().stream().mapToInt(StampedCloudPoints::getTime).max().orElse(0); 
    }
}
