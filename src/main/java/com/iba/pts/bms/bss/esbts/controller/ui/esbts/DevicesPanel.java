// ///////////////////////////////////////////////////////////////////
// Copyright (c) by Ion Beam Applications S.A.
// All rights reserved
//
// Rue du cyclotron, 3
// B-1348 Louvain-la-Neuve
// Tel : +32.10.47.58.11
// ///////////////////////////////////////////////////////////////////

package com.iba.pts.bms.bss.esbts.controller.ui.esbts;

import com.iba.icomp.core.util.Logger;
import com.iba.icomp.devices.Device;
import com.iba.pts.bms.bss.esbts.Beamline;
import com.iba.pts.bms.bss.esbts.BeamlineSection;
import com.iba.pts.bms.datatypes.api.TreatmentMode;
import com.iba.tcs.beam.bss.devices.api.BeamStop;
import com.iba.tcs.beam.bss.devices.api.Bpm;
import com.iba.tcs.beam.bss.devices.api.Degrader;
import com.iba.tcs.beam.bss.devices.api.Group3;
import com.iba.tcs.beam.bss.devices.api.Magnet;
import com.iba.tcs.beam.bss.devices.api.Magnet.Applicability;
import com.iba.tcs.beam.bss.devices.api.Slits;
import com.iba.tcs.beam.bss.devices.api.Valve;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class DevicesPanel extends JPanel
{
    /**
     * Updates the devices panel, i.e. rebuilds it.
     * @param pCurrentBeamline the beamline to which beam is currently allocated or null if beam is not allocated
     * @param pTreatmentMode the treatment mode or null if not yet known
     */
    public void updatePanel(Beamline pCurrentBeamline, TreatmentMode pTreatmentMode)
    {
        mLabelList.forEach(DevicesPanel.CustomLabel::cleanup);
        mLabelList.clear(); // remove listeners

        removeAll();

        setLayout(new BorderLayout());
        JPanel contents = new JPanel(new BorderLayout());

        JPanel top = new JPanel();

        // First Case: Beam is not allocated
        if (pCurrentBeamline == null)
        {
            top.setLayout(new BorderLayout());
            top.add(new JLabel("Beam currently not allocated to any beam supply point", SwingConstants.CENTER), BorderLayout.CENTER);
            contents.add(top, BorderLayout.CENTER);
            add(new JScrollPane(contents), BorderLayout.CENTER);
            validate();
            return;
        }

        // Second Case: Beam is allocated to some beam supply point
        top.setLayout(new BoxLayout(top, BoxLayout.PAGE_AXIS));
        top.add(Box.createRigidArea(new Dimension(0, 10)));
        JLabel beamlineLabel = new JLabel("Devices status for beam supply point " + pCurrentBeamline.getBeamSupplyPointId(),
                SwingConstants.CENTER);
        beamlineLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        top.add(beamlineLabel);
        top.add(Box.createRigidArea(new Dimension(0, 10)));
        contents.add(top, BorderLayout.NORTH);

        DefaultFormBuilder builder = new DefaultFormBuilder(
                new FormLayout("pref:grow,3dlu,pref:grow,3dlu,pref:grow,3dlu,pref:grow,3dlu,pref:grow"));
        builder.appendSeparator();
        builder.append(new JLabel("Range control devices"), 3);
        builder.nextLine();
        for (BeamlineSection section : pCurrentBeamline.getSections())
        {
            builder.appendSeparator();
            for (Device device : section.getRangeControlDevices())
            {
                String deviceType = "";
                if (device instanceof Magnet)
                {
                    if (pTreatmentMode != null && pTreatmentMode != TreatmentMode.PENCIL_BEAM_SCANNING
                            && ((Magnet) device).getApplicability() == Applicability.PBS_ONLY)
                    {
                        // skip PBS magnets for non-PBS treatment modes
                        continue;
                    }

                    deviceType = "magnet";
                }
                else if (device instanceof Degrader)
                {
                    deviceType = "degrader";
                }
                else if (device instanceof Group3)
                {
                    deviceType = "group3";
                }
                else if (device instanceof Slits)
                {
                    deviceType = "slits";
                }
                else if (device instanceof Valve)
                {
                    deviceType = "valve";
                }
                AsRequestedLabel label1 = new AsRequestedLabel(device);
                mLabelList.add(label1);
                SetPointLabel label2 = new SetPointLabel(device);
                mLabelList.add(label2);
                FeedbackLabel label3 = new FeedbackLabel(device);
                mLabelList.add(label3);

                builder.append(new JLabel(device.getDeviceName()));
                builder.append(new JLabel("(" + deviceType + ")"));
                builder.append(label1);
                builder.append(label2);
                builder.append(label3);
                builder.nextLine();
            }
        }
        JPanel upper = new JPanel(new BorderLayout());
        upper.add(builder.getPanel(), BorderLayout.NORTH);

        builder = new DefaultFormBuilder(new FormLayout("pref:grow,3dlu,pref:grow,3dlu,pref:grow,3dlu,pref:grow"));
        builder.appendSeparator();
        builder.append(new JLabel("Beam enable devices"), 3);
        builder.nextLine();
        for (BeamlineSection section : pCurrentBeamline.getSections())
        {
            builder.appendSeparator();
            for (Device device : section.getBeamEnableDevices())
            {
                String deviceType = "";
                if (device instanceof BeamStop)
                {
                    deviceType = "beamstop";
                }
                else if (device instanceof Bpm)
                {
                    deviceType = "bpm";
                }
                AsRequestedLabel label1 = new AsRequestedLabel(device);
                mLabelList.add(label1);
                StatusLabel label2 = new StatusLabel(device);
                mLabelList.add(label2);

                builder.append(new JLabel(device.getDeviceName()));
                builder.append(new JLabel("(" + deviceType + ")"));
                builder.append(label1);
                builder.append(label2);
                builder.nextLine();
            }
        }
        JPanel lower = new JPanel(new BorderLayout());
        lower.add(builder.getPanel(), BorderLayout.NORTH);

        contents.add(upper, BorderLayout.CENTER);
        contents.add(lower, BorderLayout.SOUTH);

        add(new JScrollPane(contents), BorderLayout.CENTER);
        validate();
    }

    public void cleanupPanel()
    {
        updatePanel(null, null);
    }

    private class AsRequestedLabel extends JLabel implements CustomLabel
    {

        public AsRequestedLabel(Device pDevice)
        {
            mDevice = pDevice;
            setBooleanValue(pDevice.isAsRequested(), mDevice);
            mListener = pEvt -> {
                if (pEvt.getPropertyName().endsWith(Device.AS_REQUESTED_PROPERTY))
                {
                    setBooleanValue((Boolean) pEvt.getNewValue(), mDevice);

                }
            };
            mDevice.addPropertyChangeListener(mListener);
        }

        private void setBooleanValue(boolean pValue, Device pDevice)
        {
            SwingUtilities.invokeLater(() -> {
                if (pValue)
                {
                    setForeground(Color.green);
                    setText("OK");
                    LOGGER.warn(pDevice.getDeviceName() + pDevice.isAsRequested());
                }
                else
                {
                    setForeground(Color.red);
                    setText("NOK");
                }
            });
        }

        @Override
        public void cleanup()
        {
            if (mListener != null)
            {
                mDevice.removePropertyChangeListener(mListener);
            }
        }

        private final Device mDevice;
        private final PropertyChangeListener mListener;
    }

    private class StatusLabel extends JLabel implements CustomLabel
    {

        public StatusLabel(Device pDevice)
        {
            mDevice = pDevice;
            if (pDevice instanceof BeamStop)
            {
                BeamStop bs = (BeamStop) pDevice;
                SwingUtilities.invokeLater(() -> setText(bs.getInternalStatus().toString()));
                mListener = pEvt -> {
                    if (pEvt.getPropertyName().endsWith(BeamStop.BEAM_STOP_STATUS_PROPERTY))
                    {
                        SwingUtilities.invokeLater(() -> setText(pEvt.getNewValue().toString()));
                    }
                };
                mDevice.addPropertyChangeListener(mListener);
            }
            else if (pDevice instanceof Bpm)
            {
                Bpm bpm = (Bpm) pDevice;
                SwingUtilities.invokeLater(() -> setText(bpm.getInternalStatus().toString()));
                mListener = pEvt -> {
                    if (pEvt.getPropertyName().endsWith(Bpm.BPM_STATUS_PROPERTY))
                    {
                        SwingUtilities.invokeLater(() -> setText(pEvt.getNewValue().toString()));
                    }
                };
                mDevice.addPropertyChangeListener(mListener);
            }
            else
            {
                SwingUtilities.invokeLater(() -> setText("NA"));
            }
        }

        @Override
        public void cleanup()
        {
            if (mListener != null)
            {
                mDevice.removePropertyChangeListener(mListener);
            }
        }

        private final Device mDevice;
        private PropertyChangeListener mListener;
    }

    private class SetPointLabel extends JLabel implements CustomLabel
    {

        public SetPointLabel(Device pDevice)
        {
            mDevice = pDevice;
            if (pDevice instanceof Degrader)
            {
                Degrader degrader = (Degrader) pDevice;
                setSetpoint(degrader.getRequestedPosition());
                mListener = pEvt -> {
                    if (pEvt.getPropertyName().endsWith(Degrader.DEGRADER_REQUESTED_POSITION_PROPERTY))
                    {
                        setSetpoint(pEvt.getNewValue());
                    }
                };
                mDevice.addPropertyChangeListener(mListener);
            }
            else if (pDevice instanceof Magnet)
            {
                Magnet magnet = (Magnet) pDevice;
                setSetpoint(DOUBLE_FORMAT.format(magnet.getRequestedCurrent()));
                mListener = pEvt -> {
                    if (pEvt.getPropertyName().endsWith(Magnet.MAGNET_REQUESTED_CURRENT_PROPERTY))
                    {
                        setSetpoint(DOUBLE_FORMAT.format(pEvt.getNewValue()));
                    }
                };
                mDevice.addPropertyChangeListener(mListener);
            }
            else if (pDevice instanceof Group3)
            {
                Group3 group3 = (Group3) pDevice;
                setSetpoint(DOUBLE_FORMAT.format(group3.getExpectedField()));
                mListener = pEvt -> {
                    if (pEvt.getPropertyName().endsWith(Group3.GROUP3_EXPECTED_FIELD_PROPERTY))
                    {
                        setSetpoint(DOUBLE_FORMAT.format(pEvt.getNewValue()));
                    }
                };
                mDevice.addPropertyChangeListener(mListener);
            }
            else if (pDevice instanceof Slits)
            {
                Slits slits = (Slits) pDevice;
                setSetpoint(DOUBLE_FORMAT.format(slits.getRequestedWidth()));
                mListener = pEvt -> {
                    if (pEvt.getPropertyName().endsWith(Slits.SLITS_REQUESTED_WIDTH_PROPERTY))
                    {
                        setSetpoint(DOUBLE_FORMAT.format(pEvt.getNewValue()));
                    }
                };
                mDevice.addPropertyChangeListener(mListener);
            }
            else if (pDevice instanceof Valve)
            {
                setSetpoint("opened");
            }
            else
            {
                SwingUtilities.invokeLater(() -> setText("NA"));
            }
        }

        @Override
        public void cleanup()
        {
            if (mListener != null)
            {
                mDevice.removePropertyChangeListener(mListener);
            }
        }

        private void setSetpoint(Object pSetpoint)
        {
            SwingUtilities.invokeLater(() -> setText("s:" + pSetpoint));
        }

        private final Device mDevice;
        private PropertyChangeListener mListener;
    }

    private class FeedbackLabel extends JLabel implements CustomLabel
    {

        public FeedbackLabel(Device pDevice)
        {
            mDevice = pDevice;
            if (pDevice instanceof Degrader)
            {
                Degrader degrader = (Degrader) pDevice;
                setFeedback(degrader.getPosition());
                mListener = pEvt -> {
                    if (pEvt.getPropertyName().endsWith(Degrader.DEGRADER_POSITION_PROPERTY))
                    {
                        setFeedback(pEvt.getNewValue());
                    }
                };
                mDevice.addPropertyChangeListener(mListener);
            }
            else if (pDevice instanceof Magnet)
            {
                Magnet magnet = (Magnet) pDevice;
                setFeedback(DOUBLE_FORMAT.format(magnet.getCurrent()));
                mListener = pEvt -> {
                    if (pEvt.getPropertyName().endsWith(Magnet.MAGNET_CURRENT_PROPERTY))
                    {
                        setFeedback(DOUBLE_FORMAT.format(pEvt.getNewValue()));
                    }
                };
                mDevice.addPropertyChangeListener(mListener);
            }
            else if (pDevice instanceof Group3)
            {
                Group3 group3 = (Group3) pDevice;
                setFeedback(DOUBLE_FORMAT.format(group3.getField()));
                mListener = pEvt -> {
                    if (pEvt.getPropertyName().endsWith(Group3.GROUP3_FIELD_PROPERTY))
                    {
                        setFeedback(DOUBLE_FORMAT.format(pEvt.getNewValue()));
                    }
                };
                mDevice.addPropertyChangeListener(mListener);
            }
            else if (pDevice instanceof Slits)
            {
                Slits slits = (Slits) pDevice;
                setFeedback(DOUBLE_FORMAT.format(slits.getWidth()));
                mListener = pEvt -> {
                    if (pEvt.getPropertyName().endsWith(Slits.SLITS_WIDTH_PROPERTY))
                    {
                        setFeedback(DOUBLE_FORMAT.format(pEvt.getNewValue()));
                    }
                };
                mDevice.addPropertyChangeListener(mListener);
            }
            else if (pDevice instanceof Valve)
            {
                final Valve valve = (Valve) pDevice;
                setValveFeedback(valve);
                mListener = pEvt -> {
                    if (pEvt.getPropertyName().equals(Valve.VALVE_OPENED) || pEvt.getPropertyName().equals(Valve.VALVE_CLOSED))
                    {
                        setValveFeedback(valve);
                    }
                };
                mDevice.addPropertyChangeListener(mListener);
            }
            else
            {
                SwingUtilities.invokeLater(() -> setText("NA"));
            }
        }

        @Override
        public void cleanup()
        {
            if (mListener != null)
            {
                mDevice.removePropertyChangeListener(mListener);
            }
        }

        private void setFeedback(Object pFeedback)
        {
            SwingUtilities.invokeLater(() -> setText("f:" + pFeedback));
        }

        private void setValveFeedback(Valve pValve)
        {
            setFeedback(pValve.isValveOpened() ? "opened" : (pValve.isValveClosed() ? "closed" : "invalid"));
        }

        private final Device mDevice;
        private PropertyChangeListener mListener;
    }

    /** Custom label interface. */
    private interface CustomLabel
    {
        void cleanup();
    }

    private static final NumberFormat DOUBLE_FORMAT = NumberFormat.getInstance(Locale.ENGLISH);
    static
    {
        DOUBLE_FORMAT.setMinimumFractionDigits(1);
        DOUBLE_FORMAT.setMaximumFractionDigits(3);
    }

    protected static final Logger LOGGER = Logger.getLogger();

    private List<CustomLabel> mLabelList = new ArrayList<>();
}
