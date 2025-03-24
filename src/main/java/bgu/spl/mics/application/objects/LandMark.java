package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a landmark in the environment map.
 * Landmarks are identified and updated by the FusionSlam service.
 */
public class LandMark {
    // Fields
    private String id;  // The internal ID of the landmark
    private String description;  // Description of the landmark
    private List<CloudPoint> coordinates;  // List of coordinates representing the landmark

    public LandMark(String id, String description, List<CloudPoint> coordinates) {
        this.id = id;
        this.description = description;
        this.coordinates = coordinates;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public List<CloudPoint> getCoordinates() {
        return coordinates;
    }
    
    public void addCoordinate(CloudPoint coordinate) {
        this.coordinates.add(coordinate);
        StatisticalFolder.getInstance().updateNumLandmarks(1);  
    }

    public void setCoordinates(List<CloudPoint> coordinates) {
        this.coordinates = new ArrayList<>(coordinates); 
    }

    @Override
    public String toString() {
        return String.format("{\"id\":\"%s\",\"description\":\"%s\",\"coordinates\":%s}", id, description, coordinates);
    }


}
