// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import edu.wpi.first.wpilibj.motorcontrol.Spark;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Conveyor extends SubsystemBase {
  /** Creates a new ExampleSubsystem. */
  public Spark bottomC;
  public Spark topC;

  public Conveyor() {
    bottomC = new Spark(0);
    topC = new Spark(1);
  }

  public void convey() {
    bottomC.set(1);
    topC.set(-1);
  }

  public void reverseconvey() {
    bottomC.set(-1);
    topC.set(1);
  }

  public void stopconvey() {
    bottomC.set(0);
    topC.set(0);
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler 
    
  }

  @Override
  public void simulationPeriodic() {
    // This method will be called once per scheduler run during
  }
}
