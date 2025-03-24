package bgu.spl.mics.application.services;

import java.nio.file.Paths;
import java.util.Comparator;
import java.util.PriorityQueue;
import bgu.spl.mics.*;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.messages.*;

/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
 */
public class FusionSlamService extends MicroService {
    private final FusionSlam fusionSlam;
    private PriorityQueue<TrackedObjectsEvent> waitingTrackedObjects = 
        new PriorityQueue<>(Comparator.comparingInt(e -> e.getTrackedObjects().get(0).getTime()));
        String outputFilePath;

    /**
     * Constructor for FusionSlamService.
     *
     * @param fusionSlam The FusionSLAM object responsible for managing the global map.
     */
    public FusionSlamService(FusionSlam fusionSlam, String configDirectory) {
        super("FusionSlamService");
        this.fusionSlam = FusionSlam.getInstance();
        this.outputFilePath = Paths.get(configDirectory, "output_file.json").toString();
        }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle events and broadcasts.
     */
    @Override
    protected void initialize() {
        // Register for TrackedObjectsEvent
        subscribeEvent(TrackedObjectsEvent.class, event -> {
            System.out.println(getName() + ": got TrackedObjectsEvent");
            if (fusionSlam.getPoseAtTime(event.getTrackedObjects().get(0).getTime())  != null){
                fusionSlam.processTrackedObjects(event.getTrackedObjects());
                System.out.println(getName()+ "processed TrackedObjectsEvent for objects from time"+ event.getTime() );
                complete(event, true);
            }
            else{
                System.out.println("this event had no pose");
                waitingTrackedObjects.add(event);
            }
            
        });

        // Register for PoseEvent
        subscribeEvent(PoseEvent.class, event -> {
            System.out.println(getName() + ": got PoseEvent");
            fusionSlam.addPose(event.getPose());
            System.out.println("PoseEvent from "+ event.getPose().getTime() +" has been processed in: " + getName());
            complete(event, true);
            while (!waitingTrackedObjects.isEmpty() && fusionSlam.getPoseAtTime(waitingTrackedObjects.peek().getTime()) != null){
                TrackedObjectsEvent e = waitingTrackedObjects.poll(); 
                fusionSlam.processTrackedObjects(e.getTrackedObjects());
                System.out.println(getName()+ "processed waiting TrackedObjectsEvent for objects from time"+ e.getTime() );
                complete(e, true);
            }
        });

        // Register for TickBroadcast
        subscribeBroadcast(TickBroadcast.class, broadcast -> {
            System.out.println(getName() + ": got a tick and the tick is, " + broadcast.getTime());
           fusionSlam.setTick(broadcast.getTime());
        });


        // Register for CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class, broadcast -> {
            System.out.println(getName() + ": got crashed and terminate");
            terminate();
            String errorDescription = broadcast.getErrorMessage(); 
            String faultySensor = broadcast.getSenderId(); 
            System.out.println(getName() + ": is printing an error output file");
            fusionSlam.generateOutputFileWithError(outputFilePath, errorDescription, faultySensor);
        });
        subscribeBroadcast(TerminatedBroadcast.class, broadcast -> {
            if (broadcast.getSenderId() != "TimeService "){
                fusionSlam.decreaseServiceCounter();
                System.out.println(getName() + ": counter is "+ fusionSlam.getserviceCounter()+ "because of" +broadcast.getSenderId() ) ;
                if (fusionSlam.getserviceCounter() == 0) {
                    System.out.println(getName() + ": counter is 0 to terminate");
                    terminate();
                    System.out.println(getName() + ": is terminated");
                    System.out.println(getName() + ": is printing an output file");
                    fusionSlam.generateOutputFileWithoutError(outputFilePath);
                }
            }
        });
    }
}
