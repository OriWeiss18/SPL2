import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObject;
import bgu.spl.mics.application.objects.DetectedObject;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CameraTest {

    private Camera camera;

    @BeforeEach
    void setup() {
        // Create detected objects manually for testing
        List<StampedDetectedObject> detectedObjectsList = new ArrayList<>();

        // Add detected objects for various times
        detectedObjectsList.add(new StampedDetectedObject(13, List.of(
            new DetectedObject("Wall_1", "Wall"),
            new DetectedObject("Wall_2", "Wall")
        )));
        detectedObjectsList.add(new StampedDetectedObject(8, List.of(
            new DetectedObject("Chair_Base_2", "A Blue Chair with wooden legs"),
            new DetectedObject("ERROR", "GLaDOS has repurposed the robot to conduct endless cake-fetching tests. Success is a lie."),
            new DetectedObject("furniture_3", "Bed")
        )));
        detectedObjectsList.add(new StampedDetectedObject(12, List.of()));

        // Initialize the camera with the manually created data
        camera = new Camera(1, 5, detectedObjectsList, 13); // Max time is 13
    }

    @Test
    void testGetDetectedObjectsAtValidTimeWithoutError() {
        StampedDetectedObject result = camera.getDetectedObjectsAtTime(13);
        assertNotNull(result, "The result should not be null for valid time.");
        assertEquals(13, result.getTime(), "The returned time should match the requested time.");
        assertEquals(2, result.getDetectedObjects().size(), "The detected objects list size should be correct.");
        assertEquals("Wall_1", result.getDetectedObjects().get(0).getId(), "The object ID should match.");
        assertEquals("Wall", result.getDetectedObjects().get(0).getDescription(), "The object description should match.");
    }

    @Test
    void testGetDetectedObjectsAtValidTimeWithError() {
        StampedDetectedObject result = camera.getDetectedObjectsAtTime(8);
        assertNotNull(result, "The result should not be null for valid time.");
        assertEquals(8, result.getTime(), "The returned time should match the requested time.");
        assertEquals(3, result.getDetectedObjects().size(), "The detected objects list size should be correct.");

        assertEquals(STATUS.ERROR, camera.getStatus(), "Camera status should be set to ERROR.");
        assertEquals(
            "GLaDOS has repurposed the robot to conduct endless cake-fetching tests. Success is a lie.",
            camera.getErrMString(),
            "Error message string should match the detected object's description."
        );
    }

    @Test
    void testGetDetectedObjectsAtInvalidTime() {
        StampedDetectedObject result = camera.getDetectedObjectsAtTime(20);
        assertNull(result, "The result should be null for a time with no detected objects.");
    }

    @Test
    void testCheckIfDoneSetsStatusToDown() {
        camera.getDetectedObjectsAtTime(12);
        assertEquals(STATUS.UP, camera.getStatus(), "For time 12, Camera status should remain UP for valid time.");

        camera.getDetectedObjectsAtTime(20);
        assertEquals(STATUS.DOWN, camera.getStatus(), "For time 20, Camera status should be set to DOWN after maxTime.");
    }

    @Test
    void testLoadEmptyJsonFile() {
        // Create an empty detected objects list
        Camera emptyCamera = new Camera(1, 5, new ArrayList<>(), 0); // maxTime = 0
        assertTrue(emptyCamera.getDetectedObjectsList().isEmpty(), "Detected objects list should be empty for an empty JSON.");
    }

    @Test
    void testLoadSpecificCameraKey() {
        // Camera 1 data
        List<StampedDetectedObject> camera1Data = List.of(
            new StampedDetectedObject(3, List.of(
                new DetectedObject("Wall_3", "Wall")
            ))
        );

        Camera camera1 = new Camera(1, 5, camera1Data, 3);
        StampedDetectedObject result = camera1.getDetectedObjectsAtTime(3);
        assertNotNull(result, "Detected objects for camera1 should not be null.");
        assertEquals("Wall_3", result.getDetectedObjects().get(0).getId(), "The object ID should match.");

        // Camera 2 data
        List<StampedDetectedObject> camera2Data = List.of(
            new StampedDetectedObject(3, List.of(
                new DetectedObject("Wall_1", "Wall")
            ))
        );

        Camera camera2 = new Camera(2, 5, camera2Data, 3);
        StampedDetectedObject secondResult = camera2.getDetectedObjectsAtTime(3);
        assertNotNull(secondResult, "Detected objects for camera2 should not be null.");
        assertEquals("Wall_1", secondResult.getDetectedObjects().get(0).getId(), "The object ID should match for camera2.");
    }

    @Test
    void testEmptyDetectedObjectsAtValidTime() {
        List<StampedDetectedObject> detectedObjectsList = List.of(
            new StampedDetectedObject(12, new ArrayList<>()) // Empty list at time 12
        );

        Camera camera = new Camera(1, 5, detectedObjectsList, 12);
        StampedDetectedObject result = camera.getDetectedObjectsAtTime(12);
        assertNotNull(result, "The result should not be null even if there are no detected objects.");
        assertTrue(result.getDetectedObjects().isEmpty(), "The detected objects list should be empty.");
    }

    @Test
    void testCameraFrequency() {
        assertEquals(5, camera.getFrequency(), "Camera frequency should be initialized correctly.");
    }
}
