// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.RamseteController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.DifferentialDriveKinematics;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryConfig;
import edu.wpi.first.math.trajectory.TrajectoryGenerator;
import edu.wpi.first.math.trajectory.TrajectoryUtil;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Constants.DriveConstants;
import frc.robot.commands.DefaultDrive;
import frc.robot.commands.ExampleCommand;
import frc.robot.commands.FollowTrajectory;
import frc.robot.commands.Turn;
import frc.robot.Constants.IO;
import frc.robot.commands.SimpleAuto;
import frc.robot.commands.TimedCommand;
import frc.robot.commands.ConveyorBackwardCommand;
import frc.robot.commands.ConveyorCommand;
import frc.robot.commands.ExampleCommand;
import frc.robot.commands.IntakeCommand;
import frc.robot.commands.IntakeReverse;
import frc.robot.commands.ShooterCommand;
import frc.robot.commands.TimedDrive;
import frc.robot.commands.LowerArm;
import frc.robot.commands.RaiseArm;
import frc.robot.subsystems.Arm;
import frc.robot.subsystems.Conveyor;
import frc.robot.Constants.AutonomousConstants;
import frc.robot.Constants.DriveConstants;
import frc.robot.commands.DefaultDrive;
import frc.robot.subsystems.Drivetrain;
import frc.robot.subsystems.ExampleSubsystem;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.Shooter;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.RamseteCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;

/**
 * This class is where the bulk of the robot should be declared. Since
 * Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in
 * the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of
 * the robot (including
 * subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
  private ExampleSubsystem emptySubsystem = new ExampleSubsystem();
  private ExampleCommand emptyCommand = new ExampleCommand(emptySubsystem);

  // The robot's subsystems and commands are defined here...
  private final Drivetrain drivetrain = new Drivetrain();
  private final DefaultDrive driveCommand = new DefaultDrive(drivetrain);

  private final Intake intake = new Intake();
  private final Conveyor conveyor = new Conveyor();
  private final Shooter shooter = new Shooter();
  private final Arm arm = new Arm();

  private final IntakeCommand intakeCommand = new IntakeCommand(intake, conveyor);
  private final IntakeReverse intakeReverse = new IntakeReverse(intake);
  private final ConveyorCommand conveyorCommand = new ConveyorCommand(conveyor);
  private final ConveyorBackwardCommand conveyorBackwardCommand = new ConveyorBackwardCommand(conveyor);
  private final ShooterCommand shooterCommand = new ShooterCommand(shooter);

  private final LowerArm lowerArm = new LowerArm(arm);
  private final RaiseArm raiseArm = new RaiseArm(arm);  

  // Emergency autonomous. not actual autonomous unfortunately
  TrajectoryConfig trajectoryConfig = new TrajectoryConfig(1, 1);
  TrajectoryConfig backwardConfig = new TrajectoryConfig(1, 1);

  Trajectory goToBall;
  // from left ball to hub
  Trajectory goBackToGoal;

  // from right ball to hub
  Trajectory goToSecondBall;
  Trajectory goBackToGoal2;
  Trajectory goBackToGoalFromSecondBall;

  // the position we want to be at when autonomous ends and we're on the left
  Trajectory leftTeleopPosition;
  Trajectory rightTeleopPosition;

  /**
   * The container for the robot. Contains subsystems, OI devices, and commands.
   */
  public RobotContainer() {
    // Configure the button
    configureButtonBindings();

    backwardConfig.setReversed(false);
    trajectoryConfig.setReversed(true);

    goToBall = TrajectoryGenerator.generateTrajectory(
      new Pose2d(0, 0, Rotation2d.fromDegrees(0)),
      List.of(),
      new Pose2d(-1, 0, Rotation2d.fromDegrees(0)),
      trajectoryConfig
    );
    goBackToGoal =  TrajectoryGenerator.generateTrajectory(
      new Pose2d(0, 0, Rotation2d.fromDegrees(0)),
      List.of(),
      new Pose2d(1.7, -0.35, Rotation2d.fromDegrees(-45)),
      backwardConfig
    );
    goBackToGoal2 = TrajectoryGenerator.generateTrajectory(
      new Pose2d(0, 0, Rotation2d.fromDegrees(0)),
      List.of(),
      new Pose2d(1.7, 0.3, Rotation2d.fromDegrees(22.5)), 
      backwardConfig
    );
    goToSecondBall = TrajectoryGenerator.generateTrajectory(
      new Pose2d(0, 0, Rotation2d.fromDegrees(0)), 
      List.of(), 
      new Pose2d(-1.2, -1.5, Rotation2d.fromDegrees(22.5)), 
      trajectoryConfig
    );
    goBackToGoalFromSecondBall = TrajectoryGenerator.generateTrajectory(
      new Pose2d(), 
      List.of(), 
      new Pose2d(1.8, 0.3, Rotation2d.fromDegrees(-45)), 
      backwardConfig
    );
    leftTeleopPosition = TrajectoryGenerator.generateTrajectory(
      new Pose2d(), 
      List.of(), 
      new Pose2d(-1.2, -1.1, Rotation2d.fromDegrees(180)),
      trajectoryConfig
    );
    rightTeleopPosition = TrajectoryGenerator.generateTrajectory(
      new Pose2d(), 
      List.of(), 
      new Pose2d(-1.2, 1.1, Rotation2d.fromDegrees(180)),
      trajectoryConfig
    );

    drivetrain.setDefaultCommand(driveCommand);
  }

  /**
   * Use this method to define your button->command mappings. Buttons can be
   * created by
   * instantiating a {@link GenericHID} or one of its subclasses ({@link
   * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing
   * it to a {@link
   * edu.wpi.first.wpilibj2.command.button.JoystickButton}.
   */
  private void configureButtonBindings() {
    Joystick leftJoystick = new Joystick(IO.leftJoystickPort);
    Joystick rightJoystick = new Joystick(IO.rightJoystickPort);
    XboxController xbox = new XboxController(IO.xboxPort);

    JoystickButton lowGearButton = new JoystickButton(leftJoystick, 3);
    JoystickButton highGearButton = new JoystickButton(rightJoystick, 3);
    JoystickButton leftTrigger = new JoystickButton(leftJoystick, Joystick.ButtonType.kTrigger.value);
    JoystickButton rightTrigger = new JoystickButton(rightJoystick, Joystick.ButtonType.kTrigger.value);

    JoystickButton aButton = new JoystickButton(xbox, 1);
    JoystickButton bButton = new JoystickButton(xbox, 2);
    JoystickButton xButton = new JoystickButton(xbox, 3);
    JoystickButton yButton = new JoystickButton(xbox, 4);
    JoystickButton leftBumper = new JoystickButton(xbox, 5);
    JoystickButton rightBumper = new JoystickButton(xbox, 6);
    JoystickButton backButton = new JoystickButton(xbox, 7);
    JoystickButton startButton = new JoystickButton(xbox, 8);
    JoystickButton leftStick = new JoystickButton(xbox, 9);
    JoystickButton rightStick = new JoystickButton(xbox, 10);

    // Operator
    aButton.whileHeld(intakeCommand);
    xButton.whileHeld(shooterCommand);
    bButton.whileHeld(conveyorCommand);
    yButton.whenPressed(() -> intake.extendOrRetract());

    startButton.whenPressed(raiseArm).whenPressed(shooterCommand);
    backButton.whenPressed(lowerArm).cancelWhenPressed(shooterCommand);

    leftStick.whenPressed(intakeReverse);
    rightStick.whenPressed(conveyorBackwardCommand);

    leftBumper.whileHeld(() -> arm.lowerArm()).whenReleased(() -> arm.stopArm()).cancelWhenPressed(raiseArm).cancelWhenPressed(lowerArm);
    rightBumper.whileHeld(() -> arm.raiseArm()).whenReleased(() -> arm.stopArm()).cancelWhenPressed(raiseArm).cancelWhenPressed(lowerArm);

    // Driver:
    highGearButton.whenPressed(() -> drivetrain.highGear());
    lowGearButton.whenPressed(() -> drivetrain.lowGear());
    leftTrigger.or(rightTrigger).whenActive(() -> driveCommand.setSpeedLimit(DriveConstants.secondSpeedLimit)).whenInactive(() -> driveCommand.resetSpeedLimit());
    leftTrigger.and(rightTrigger).whenActive(() -> driveCommand.removeSpeedLimit()).whenInactive(() -> driveCommand.resetSpeedLimit());
  }

  public void setDrivetrainMotorsToBrake(){
    drivetrain.setMotorsToBrake();
  }

  public void setDrivetrainMotorsToCoast(){
    drivetrain.setMotorsToCoast();
  }

  public Trajectory openAutoPath(){
    String trajectoryJSON = "paths/FarthestBlue.wpilib.json";
    Trajectory trajectory = null;


    try {
      Path trajectoryPath = Filesystem.getDeployDirectory().toPath().resolve(trajectoryJSON);
      trajectory = TrajectoryUtil.fromPathweaverJson(trajectoryPath);
      System.out.println("Success");
    } catch (IOException ex) {
      System.out.println("Unable to open trajectory: " + trajectoryJSON);
    }

    return trajectory;
  }

  public void turnOffEverything(){
    intake.intakeStop();
    conveyor.stopConveyor();
    shooter.stopShooting();
    arm.stopArm();
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */

   // NOTE: autonomous is inverted. Battery is front. Left is positive. Right is negative.
  public Command getAutonomousCommand() {
    drivetrain.resetSensors();
    CommandBase command;
    
    // https://docs.wpilib.org/en/stable/docs/software/pathplanning/trajectory-tutorial/creating-following-trajectory.html?highlight=ramsetecommand#creating-the-ramsetecommand 
    RamseteCommand followBallPath = new FollowTrajectory(goToBall, drivetrain);
    RamseteCommand followGoalLeftPath = new FollowTrajectory(goBackToGoal, drivetrain); // goes to hub when we are on the left
    RamseteCommand followGoalRightPath = new FollowTrajectory(goBackToGoal2, drivetrain); // goest to hub when we are on the right
    RamseteCommand followSecondBall = new FollowTrajectory(goToSecondBall, drivetrain);
    RamseteCommand followGoalFromSecondBall = new FollowTrajectory(goBackToGoalFromSecondBall, drivetrain);
    CommandBase precommands = new ParallelCommandGroup(new LowerArm(arm), new TimedCommand(new IntakeCommand(intake, conveyor), 3));

    switch(ShuffleboardControl.getAutonomousMode()){
      case 0:
        command = new ExampleCommand(emptySubsystem);
        break;
      case 1:
        command = getTwoBallAuto(true, precommands, followBallPath, followGoalLeftPath, followGoalRightPath, new RaiseArm(arm));
        break;
      case 2:
        command = getTwoBallAuto(false, precommands, followBallPath, followGoalLeftPath, followGoalRightPath, new RaiseArm(arm));
        break;
      case 3:
        command = getThreeBallAuto(precommands, followBallPath, followGoalRightPath, new RaiseArm(arm), followSecondBall, followGoalFromSecondBall);
        break;
      default:
        command = new ExampleCommand(emptySubsystem);
        break;
    }
    
    drivetrain.highGear();    


    // return twoBallAuto(false, precommands, followBallPath, followGoalLeftPath, followGoalRightPath, raiseArm);
    return command;
  }

  private CommandBase getThreeBallAuto(CommandBase precommands, CommandBase followBallPath, CommandBase followGoalRightPath, CommandBase raiseArm, CommandBase followSecondBall, CommandBase backToGoal){
    CommandBase goToGoal = followGoalRightPath;

    return (
      precommands.alongWith(followBallPath)
      .andThen(() -> drivetrain.resetSensors())
      .andThen(goToGoal.alongWith(raiseArm))
      .andThen(new WaitCommand(0.5))
      .andThen(new TimedCommand(new ConveyorCommand(conveyor), 1))
      .andThen(() -> drivetrain.resetSensors())
      .andThen((new LowerArm(arm)).alongWith(followSecondBall).alongWith(new IntakeCommand(intake, conveyor, false))
      .andThen(() -> drivetrain.resetSensors())
      .andThen( backToGoal.alongWith(new RaiseArm(arm)) )
      .andThen(new WaitCommand(0.5))
      .andThen(new TimedCommand(new ConveyorCommand(conveyor), 1))
      )
    )
    .alongWith(new TimedCommand(new ShooterCommand(shooter), 15));
  }

  private CommandBase getTwoBallAuto(boolean isLeft, CommandBase precommands, CommandBase followBallPath, CommandBase followGoalLeftPath, CommandBase followGoalRightPath, CommandBase raiseArm){
    CommandBase goToGoal = isLeft ? followGoalLeftPath : followGoalRightPath;
    CommandBase getRidOfOpponentBall = ( new FollowTrajectory( isLeft ? leftTeleopPosition:rightTeleopPosition , drivetrain) ).alongWith(new LowerArm(arm)).alongWith(new IntakeReverse(intake));


    return (
      precommands.alongWith(followBallPath)
      .andThen(() -> drivetrain.resetSensors())
      .andThen(goToGoal.alongWith(raiseArm))
      .andThen(new WaitCommand(0.5))
      .andThen(new TimedCommand(new ConveyorCommand(conveyor), 1))
      .andThen(() -> drivetrain.resetSensors())
      .andThen(getRidOfOpponentBall)
    )
    .alongWith(new TimedCommand(new ShooterCommand(shooter), 6.5));
  }

  private CommandBase getRamseteCommand(){
    // https://docs.wpilib.org/en/stable/docs/software/pathplanning/trajectory-tutorial/creating-following-trajectory.html?highlight=ramsetecommand#creating-the-ramsetecommand
    return null;
  }
}
