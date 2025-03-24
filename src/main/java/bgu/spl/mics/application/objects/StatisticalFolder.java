package bgu.spl.mics.application.objects;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import bgu.spl.mics.Event;

public class StatisticalFolder {
  
    // Fields using AtomicInteger for thread-safety
    private AtomicInteger systemRuntime;        
    private AtomicInteger numDetectedObjects;     
    private AtomicInteger numTrackedObjects;   
    private AtomicInteger numLandmarks;           
    private Map<String, Event<?>> lastFrames;      

    public StatisticalFolder() {
        this.systemRuntime = new AtomicInteger(0);
        this.numDetectedObjects = new AtomicInteger(0);
        this.numTrackedObjects = new AtomicInteger(0);
        this.numLandmarks = new AtomicInteger(0);
        this.lastFrames = new ConcurrentHashMap<>();
    }
    // Singleton Holder for thread-safe מימוש כמו בכיתה
    private static class SingletonHolderStatisticalFolder {
        private static final StatisticalFolder INSTANCE = new StatisticalFolder();
    }

    public static StatisticalFolder getInstance() {
        return SingletonHolderStatisticalFolder.INSTANCE;
    }
    
    public int getSystemRuntime() {
        return systemRuntime.get();   
    }

    public int getNumDetectedObjects() {
        return numDetectedObjects.get();
    }

    public int getNumTrackedObjects() {
        return numTrackedObjects.get();
    }

    public int getNumLandmarks() {
        return numLandmarks.get();
    }


    public void updateSystemRuntime(int timeTick) {
        this.systemRuntime.addAndGet(timeTick); 
    }

    public void updateNumDetectedObjects(int detectedObjectsCount) {
        this.numDetectedObjects.addAndGet(detectedObjectsCount); 
    }

    public void updateNumTrackedObjects(int trackedObjectsCount) {
        this.numTrackedObjects.addAndGet(trackedObjectsCount); 
    }

    public void updateNumLandmarks(int newLandmarksCount) {
        this.numLandmarks.addAndGet(newLandmarksCount); 
    }

    public void updateLastFrame(String name, Event<?> event) {
        lastFrames.put(name, event);
    }

    public Map<String, Event<?>> getLastFrames() {
        return lastFrames;
    }
}