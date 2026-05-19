package frc.robot.subsystems;

import static edu.wpi.first.units.Units.RotationsPerSecond;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.configs.FeedbackConfigs;
import com.ctre.phoenix6.configs.Slot0Configs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.configs.TorqueCurrentConfigs;
import com.ctre.phoenix6.controls.CoastOut;
import com.ctre.phoenix6.controls.Follower;
import com.ctre.phoenix6.controls.VelocityTorqueCurrentFOC;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.FeedbackSensorSourceValue;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.MotorAlignmentValue;
import com.ctre.phoenix6.signals.NeutralModeValue;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class Shooter extends SubsystemBase {
    // The motor controller objects for the shooter.
    private static final double STATOR_CURRENT_LIMIT_AMPS = 80.0;
    private static final double SUPPLY_CURRENT_LIMIT_AMPS = 40.0;
    private static final double SENSOR_TO_MECHANISM_RATIO = 1.0;

    // Suggested starting values for VelocityTorqueCurrentFOC.
    // These gains are in torque-current units, so they are much smaller than velocity-voltage gains.
    private static final double SLOT0_KS = .1;
    private static final double SLOT0_KP = 15; // was 6
    private static final double SLOT0_KI = 0.0;
    private static final double SLOT0_KD = 0.0;
    private static final double SLOT0_KV = 0; // was 0.26
    private static final double SLOT0_KA = 0.0;
    private static final double REQUEST_FEEDFORWARD_AMPS = 2.0;
    private static final double FOLLOWER_STATUS_UPDATE_HZ = 100.0;

    private static final double PEAK_FORWARD_TORQUE_CURRENT_AMPS = 120.0;
    private static final double PEAK_REVERSE_TORQUE_CURRENT_AMPS = -120.0;
    private static final double TORQUE_NEUTRAL_DEADBAND_AMPS = 0.0;

    private double currentSpeedSetpointRps = 0.0;

    private final TalonFX left_shooter;
    private final TalonFX right_shooter;
    public Shooter() {
        left_shooter = new TalonFX(Constants.CAN_ID.LEFT_SHOOTER, Constants.CAN_BUS.CANIVORE);
        right_shooter = new TalonFX(Constants.CAN_ID.RIGHT_SHOOTER, Constants.CAN_BUS.CANIVORE);
        configureMotor(left_shooter, InvertedValue.CounterClockwise_Positive, "Left Shooter");
        configureMotor(right_shooter, InvertedValue.Clockwise_Positive, "Right Shooter");
    }
    







    private void configureMotor(TalonFX motor, InvertedValue invertedValue, String motorName) 
    {
        TalonFXConfiguration shooterConfigs = new TalonFXConfiguration()
                .withCurrentLimits(new CurrentLimitsConfigs()
                        .withStatorCurrentLimit(STATOR_CURRENT_LIMIT_AMPS)
                        .withSupplyCurrentLimit(SUPPLY_CURRENT_LIMIT_AMPS)
                        .withStatorCurrentLimitEnable(true)
                        .withSupplyCurrentLimitEnable(true))
                .withFeedback(new FeedbackConfigs()
                        .withFeedbackSensorSource(FeedbackSensorSourceValue.RotorSensor)
                        .withSensorToMechanismRatio(SENSOR_TO_MECHANISM_RATIO))
                .withSlot0(new Slot0Configs()
                        .withKS(SLOT0_KS)
                        .withKP(SLOT0_KP)
                        .withKI(SLOT0_KI)
                        .withKD(SLOT0_KD)
                        .withKV(SLOT0_KV)
                        .withKA(SLOT0_KA))
                .withTorqueCurrent(new TorqueCurrentConfigs()
                        .withPeakForwardTorqueCurrent(PEAK_FORWARD_TORQUE_CURRENT_AMPS)
                        .withPeakReverseTorqueCurrent(PEAK_REVERSE_TORQUE_CURRENT_AMPS)
                        .withTorqueNeutralDeadband(TORQUE_NEUTRAL_DEADBAND_AMPS));
        shooterConfigs.MotorOutput.Inverted = invertedValue;
        shooterConfigs.MotorOutput.NeutralMode = NeutralModeValue.Coast;

        StatusCode status = StatusCode.StatusCodeNotInitialized;
        for (int i = 0; i < Constants.CONFIG_RETRIES; ++i) {
            status = motor.getConfigurator().apply(shooterConfigs);
            if (status.isOK()) 
            {
                break;
            }
        }

        if (!status.isOK()) 
        {
            System.out.println("Could not apply configs for " + motorName + ", error code: " + status);
        }
    }
}
