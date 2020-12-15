//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.iba.pts.bms.tcrServiceScreens.actions;

import com.iba.ialign.Controller;
import com.iba.pts.bms.bds.devices.api.TcuVariableCollimator;
import com.iba.pts.bms.bds.devices.api.TcuVariableCollimator.AutoMoveRequest;
import com.iba.pts.bms.bds.tcu.devices.TcuVariableCollimatorsProxyImpl;
import com.jgoodies.binding.value.ValueModel;
import java.awt.event.ActionListener;
import javax.swing.JFormattedTextField;

public final class VceuActionFactory {
    public static ActionListener createAutoMoveAction(ValueModel pActionModel, TcuVariableCollimator pVceu, AutoMoveRequest pMoveRequest) {
        return (pEvent) -> {
            pActionModel.setValue(pMoveRequest.toString());
            pVceu.startAutoMove(pMoveRequest);
//            pVceu.moveToPositionInCm(pVceu.getXMinOpenSetpoint(), pVceu.getYMinOpenSetpoint());
//            Controller.beam.VCEU3.moveToPositionInCm(Controller.beam.VCEU3.getXMinOpenSetpoint(), Controller.beam.VCEU3.getYMinOpenSetpoint());
//            System.out.println(pVceu.getXOpenFeedback());
//            System.out.println(pVceu.getYVoltFeedback());
        };
    }

    public static ActionListener createAutoCalibrationAction(ValueModel pActionModel, TcuVariableCollimator pVceu) {
        return (pEvent) -> {
            pActionModel.setValue("Auto calibrate");
            pVceu.startAutoCalibration();
        };
    }

    public static ActionListener createSetpointCmSubmitAction(ValueModel pActionModel, TcuVariableCollimator pVceu, String pJawType, JFormattedTextField pTextField) {
        return (pEvent) -> {
            Object value = pTextField.getValue();
            if (value != null) {
                pActionModel.setValue("Set opening");
                double newValue = ((Number)value).doubleValue();
                byte var9 = -1;
                switch(pJawType.hashCode()) {
                    case 88:
                        if (pJawType.equals("X")) {
                            var9 = 0;
                        }
                        break;
                    case 89:
                        if (pJawType.equals("Y")) {
                            var9 = 1;
                        }
                }

                switch(var9) {
                    case 0:
                        pVceu.moveToPositionInCm(newValue, pVceu.getYOpenSetpoint());
                        break;
                    case 1:
                        pVceu.moveToPositionInCm(pVceu.getXOpenSetpoint(), newValue);
                        break;
                    default:
                        throw new IllegalArgumentException("Unrecognized jaw type " + pJawType);
                }
            }

        };
    }

    public static ActionListener createSetpointVoltSubmitAction(ValueModel pActionModel, TcuVariableCollimator pVceu, String pJawType, JFormattedTextField pTextField) {
        return (pEvent) -> {
            Object value = pTextField.getValue();
            if (value != null) {
                pActionModel.setValue("Set voltage");
                double newValue = ((Number)value).doubleValue();
                if (pJawType.equals("X")) {
                    pVceu.moveToPositionInVolt(newValue, pVceu.getYVoltSetpoint());
                } else if (pJawType.equals("Y")) {
                    pVceu.moveToPositionInVolt(pVceu.getXVoltSetpoint(), newValue);
                }
            }

        };
    }

    private VceuActionFactory() {
    }
}
