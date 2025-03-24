import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.TrackedObject;


import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FusionSlamTest {

    private FusionSlam fusionSlam;

    @BeforeEach
    void setup() {
        fusionSlam = FusionSlam.getInstance();
        fusionSlam.clearLandmarks();
    }

    @Test
    void testGlobalTransformation() {
        Pose pose = new Pose(1, 1.0f, 1.0f, 45.0f);
        List<CloudPoint> localCoordinates = Arrays.asList(
                new CloudPoint(1.0, 1.0),
                new CloudPoint(2.0, 2.0),
                new CloudPoint(0.0, 1.0) 
        );
    
        List<CloudPoint> globalCoordinates = fusionSlam.transformToGlobal(localCoordinates, pose);
    
        assertNotNull(globalCoordinates, "Global transformation result should not be null.");
        assertEquals(3, globalCoordinates.size(), "The number of global coordinates should match the local coordinates.");
    
        // בדיקה נגד ערכים צפויים
        double sqrt2 = Math.sqrt(2) / 2;
        CloudPoint expectedPoint1 = new CloudPoint(1.0 + sqrt2 - sqrt2, 1.0 + sqrt2 + sqrt2);
        CloudPoint expectedPoint2 = new CloudPoint(1.0 + 2 * sqrt2 - 2 * sqrt2, 1.0 + 2 * sqrt2 + 2 * sqrt2);
        CloudPoint expectedPoint3 = new CloudPoint(1.0 + 0 * sqrt2 - 1 * sqrt2, 1.0 + 0 * sqrt2 + 1 * sqrt2);
    
        assertEquals(expectedPoint1.getX(), globalCoordinates.get(0).getX(), 0.0001, "First point X mismatch");
        assertEquals(expectedPoint1.getY(), globalCoordinates.get(0).getY(), 0.0001, "First point Y mismatch");
        assertEquals(expectedPoint2.getX(), globalCoordinates.get(1).getX(), 0.0001, "Second point X mismatch");
        assertEquals(expectedPoint2.getY(), globalCoordinates.get(1).getY(), 0.0001, "Second point Y mismatch");
        assertEquals(expectedPoint3.getX(), globalCoordinates.get(2).getX(), 0.0001, "Third point X mismatch");
        assertEquals(expectedPoint3.getY(), globalCoordinates.get(2).getY(), 0.0001, "Third point Y mismatch");
    }
    

    @Test
    void testAddNewLandmark() {
        Pose pose = new Pose(1, 1.0f, 0.0f, 1.0f);
        fusionSlam.addPose(pose);

        List<CloudPoint> coordinates = Arrays.asList(
                new CloudPoint(1.0, 1.0),
                new CloudPoint(2.0, 2.0)
        );

        TrackedObject trackedObject = new TrackedObject("Landmark1", 1, "Test Landmark", coordinates);
        fusionSlam.processTrackedObjects(Arrays.asList(trackedObject));

        List<LandMark> landmarks = fusionSlam.getLandmarksMod();
        assertEquals(1, landmarks.size(), "A new landmark should have been added.");
        assertEquals("Landmark1", landmarks.get(0).getId(), "Landmark ID should match.");
        assertEquals(2, landmarks.get(0).getCoordinates().size(), "Landmark should have correct number of coordinates.");
    }

    @Test
    void testUpdateExistingLandmark() {
        Pose pose = new Pose(1, 1.0f, 0.0f, 1.0f);
        fusionSlam.addPose(pose);

        List<CloudPoint> initialCoordinates = Arrays.asList(
                new CloudPoint(1.0, 1.0),
                new CloudPoint(2.0, 2.0)
        );

        LandMark existingLandmark = new LandMark("Landmark1", "Test Landmark", initialCoordinates);
        fusionSlam.addLandmark(existingLandmark);

        List<CloudPoint> newCoordinates = Arrays.asList(
                new CloudPoint(3.0, 3.0),
                new CloudPoint(4.0, 4.0)
        );

        TrackedObject trackedObject = new TrackedObject("Landmark1", 1, "Test Landmark", newCoordinates);
        fusionSlam.processTrackedObjects(Arrays.asList(trackedObject));

        List<LandMark> landmarks = fusionSlam.getLandmarksMod();
        assertEquals(1, landmarks.size(), "There should still be only one landmark.");
        assertEquals("Landmark1", landmarks.get(0).getId(), "Landmark ID should match.");
        assertEquals(2, landmarks.get(0).getCoordinates().size(), "Landmark should still have the same number of coordinates.");
    }

    @Test
    void testProcessTrackedObjectsWithPose() {
        FusionSlam fusionSlam = FusionSlam.getInstance();
        Pose pose = new Pose(2, 1.0f, 0.0f, 0.0f); 
        fusionSlam.addPose(pose); 
        List<CloudPoint> coordinates = Arrays.asList(
                new CloudPoint(1.0, 1.0),
                new CloudPoint(2.0, 2.0)
        );
        TrackedObject trackedObject = new TrackedObject("Landmark1", 2, "Test Landmark", coordinates);
        fusionSlam.processTrackedObjects(Arrays.asList(trackedObject));
        List<LandMark> landmarks = fusionSlam.getLandmarksMod();
        assertEquals(1, landmarks.size(), "One landmark should be added.");
        assertEquals("Landmark1", landmarks.get(0).getId(), "The added landmark should have the correct ID.");
        assertEquals(2, landmarks.get(0).getCoordinates().size(), "The landmark should have the correct number of coordinates.");
    }

    @Test
    void testProcessTrackedObjectsWithoutPose() {
        FusionSlam fusionSlam = FusionSlam.getInstance();
        fusionSlam.clearLandmarks();
        List<CloudPoint> coordinates = Arrays.asList(
                new CloudPoint(1.0, 1.0),
                new CloudPoint(2.0, 2.0)
        );
        TrackedObject trackedObject = new TrackedObject("Landmark1", 3, "Test Landmark", coordinates); 
        fusionSlam.processTrackedObjects(Arrays.asList(trackedObject));
        List<LandMark> landmarks = fusionSlam.getLandmarksMod();
        assertEquals(0, landmarks.size(), "No landmark should be added if no pose is available.");
    }


    @Test
    void testProcessTrackedObjectsWithMultipleObjects() {
        Pose pose = new Pose(1, 1.0f, 45.0f, 1.0f);
        fusionSlam.addPose(pose);

        List<CloudPoint> coordinates1 = Arrays.asList(
                new CloudPoint(1.0, 1.0),
                new CloudPoint(2.0, 2.0)
        );

        List<CloudPoint> coordinates2 = Arrays.asList(
                new CloudPoint(3.0, 3.0),
                new CloudPoint(4.0, 4.0)
        );

        TrackedObject trackedObject1 = new TrackedObject("Landmark1", 1, "First Landmark", coordinates1);
        TrackedObject trackedObject2 = new TrackedObject("Landmark2", 1, "Second Landmark", coordinates2);

        fusionSlam.processTrackedObjects(Arrays.asList(trackedObject1, trackedObject2));

        List<LandMark> landmarks = fusionSlam.getLandmarksMod();
        assertEquals(2, landmarks.size(), "Two landmarks should be added.");
        assertEquals("Landmark1", landmarks.get(0).getId(), "First landmark ID should match.");
        assertEquals("Landmark2", landmarks.get(1).getId(), "Second landmark ID should match.");
    }
}
