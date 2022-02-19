package frc.robot.commands.limelight;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.Limelight;

public class DriveCamera extends CommandBase{

    /**
     * Enables the driver camera for the limelight on command run
     */
    public DriveCamera() {}

    @Override
    public void initialize() {
        Limelight.setCameraMode(1);
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}