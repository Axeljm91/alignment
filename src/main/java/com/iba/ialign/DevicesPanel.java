package com.iba.ialign;

import com.iba.icomp.core.util.Logger;
import com.iba.icomp.devices.Device;
import com.iba.icompx.ui.i18n.ResourceDictionary;
import com.iba.icompx.ui.panel.BooleanValueLabel;
import com.iba.icompx.ui.util.ResourceManager;
import com.iba.pts.bms.bss.beamscheduler.api.PendingBeamRequest;
import com.iba.pts.bms.bss.controller.api.BssController;
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
import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.TitledBorder;

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

        Font bigFont = new Font(Font.DIALOG, Font.BOLD, 20);
        Font bigTitle = new Font(Font.DIALOG, Font.BOLD, 18);

        JPanel top = new JPanel();
        //top.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.lightGray)));
        TitledBorder title = new TitledBorder("MCR Panel");
        title.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.lightGray));
        top.setBorder(title);
        FormLayout DevicePanelLayout = new FormLayout("120dlu,pref:grow,18dlu", "9dlu,p:g,18dlu,p:g,p:g,p:g,p:g,p:g,27dlu,p:g,18dlu");
        final PanelBuilder devicePanelBuilder = new PanelBuilder(DevicePanelLayout, top);
        CellConstraints cc = new CellConstraints();
        mStartServiceBeam.addActionListener(pArg0 -> this.startServiceBeam());
        mAllocateButton.addActionListener(pE -> {
            List<PendingBeamRequest> reqs = Controller.beam.beamScheduler.getPendingBeamRequests();

            if (!reqs.isEmpty())
            {
                Controller.beam.beamScheduler.acceptBeamRequest(reqs.get(0).getBeamSupplyPointId());
            }
        });
        mStartServiceBeam.setEnabled(false);
        mStartServiceBeam.setFont(bigFont);
        mAllocateButton.setEnabled(false);

        mIdleLabelOperMode.setFont(bigFont);

        mIdleLabel1.setFont(bigFont);
        mIdleLabel2.setFont(bigTitle);
        mIdleLabel3.setFont(bigTitle);
        mIdleLabel4.setFont(bigTitle);
        //mIdleLabel4.setHorizontalAlignment(SwingConstants.CENTER);
        mIdleLabel5.setFont(bigTitle);
        mIdleLabel6.setFont(bigTitle);

        mIdleLabel2.setIcon(RES2.getImageIcon("icompx/icons/disabled"));
        mIdleLabel3.setIcon(RES2.getImageIcon("icompx/icons/disabled"));
        mIdleLabel4.setIcon(RES2.getImageIcon("icompx/icons/disabled"));
        mIdleLabel5.setIcon(RES2.getImageIcon("icompx/icons/disabled"));
        mIdleLabel6.setIcon(RES2.getImageIcon("icompx/icons/disabled"));

        mIdleLabel2.setHorizontalAlignment(JLabel.LEFT);
        mIdleLabel3.setHorizontalAlignment(JLabel.LEFT);
        mIdleLabel4.setHorizontalAlignment(JLabel.LEFT);
        mIdleLabel5.setHorizontalAlignment(JLabel.LEFT);
        mIdleLabel6.setHorizontalAlignment(JLabel.LEFT);

        mStartServiceBeam.setToolTipText("Enabled once all pre-requisites above are met.");



        //System.out.println(Controller.isSystemManual());

        //mIdleLabel4 = new BooleanValueLabel(BssController.OperatingMode.MANUAL.toState(), Controller.beam.bssController.getOperatingMode().toState(), new String[]{"BSS Operating Mode -- AUTO"});


        // First Case: Beam is not allocated
        if (pCurrentBeamline == null)
        {
            //top.setLayout(new BorderLayout());
            //top.add(mIdleLabelOperMode, cc.rchw(1, 1, 1, 3));
            top.add(mIdleLabel1, cc.rchw(2, 1, 1, 3));
            //top.add(mIdleLabel2, cc.rchw(7, 1, 1, 3));

            top.add(mIdleLabel4, cc.rchw(4, 2, 1, 1));
            top.add(mIdleLabel3, cc.rchw(5, 2, 1, 1));
            top.add(mIdleLabel2, cc.rchw(6, 2, 1, 1));
            top.add(mIdleLabel5, cc.rchw(7, 2, 1, 1));
            top.add(mIdleLabel6, cc.rchw(8, 2, 1, 1));

            top.add(mStartServiceBeam, cc.rchw(10, 1, 1, 3));
            //top.add(mIdleLabel6, cc.rchw(11, 1, 1, 3));
            //top.add(mIdleLabel5, cc.rchw(10, 1, 1, 3));
            //top.add(mAllocateButton, cc.rchw(3, 1, 1, 3));
            setLayout(new GridBagLayout());

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.CENTER;
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.weightx = 0.1;
            c.weighty = 0.1;
            add(top, c);
            //contents.add(top, BorderLayout.CENTER);
            //add(new JScrollPane(contents), BorderLayout.CENTER);
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
                new FormLayout("pref:grow,12dlu,pref:grow,3dlu,pref:grow,3dlu,pref:grow,3dlu,pref:grow"));
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
                DevicesPanel.AsRequestedLabel label1 = new DevicesPanel.AsRequestedLabel(device);
                mLabelList.add(label1);
                DevicesPanel.SetPointLabel label2 = new DevicesPanel.SetPointLabel(device);
                mLabelList.add(label2);
                DevicesPanel.FeedbackLabel label3 = new DevicesPanel.FeedbackLabel(device);
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
                DevicesPanel.AsRequestedLabel label1 = new DevicesPanel.AsRequestedLabel(device);
                mLabelList.add(label1);
                DevicesPanel.StatusLabel label2 = new DevicesPanel.StatusLabel(device);
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

    public void startServiceBeam(){
        //if (Controller.beam.bssController.getOperatingMode() == BssController.OperatingMode.MANUAL) {
        //Controller.selectBP(1);
       //System.out.println(Controller.beam.blpscu.isInitialized());
        //System.out.println(Controller.beam.bpsController.isInitialized());

        try {
            Gui.setServiceBeamMode();
            //Controller.beam.blpscu.setUseSimulator(false);
            //PLCCommandChannelProxy plcCommandChannelProxy = new PLCCommandChannelProxy();
            //plcCommandChannelProxy.set
            //Controller.beam.blpscu.setBlpscuCommandChannel("BlpscuCommand");
            //Controller.beam.blpscuCmdChannelProxy.sendCommand(79);
            //Controller.beam.blpscu.turnOffEssMagnetsPowerSupplies();
            //Controller.beam.blpscu.proxyPublish();
            //System.out.println(Controller.beam.ISEU1.isChainClosed());
        } catch(Exception e){
            e.printStackTrace();
        }
        //Controller.beam.blpscu.turnOffBeamlineMagnetsPowerSupplies(Controller.beam.blpscu.getSelectedBeamlineId());
        //Controller.beam.smpsController.startPrepareActivity(TreatmentMode.PENCIL_BEAM_SCANNING);
           // if (Controller.beam.beamScheduler.getCurrentBeamAllocation() == null) {
                //Controller.beam.beamScheduler.requestBeam("FBTR1", BeamPriority.Normal, 0, null);
          //  }
       // }
    }

    public void setLabelIcon(int i, Icon icon){
        if (i == 3) {
            mIdleLabel3.setIcon(icon);
        }else if (i == 4) {
            mIdleLabel4.setIcon(icon);
        }else if (i == 5) {
            mIdleLabel5.setIcon(icon);
        }else{
            return;
        }
    }

    public void allocateBeam(){
        if (Controller.beam.bssController.getOperatingMode() == BssController.OperatingMode.MANUAL) {
            if (Controller.beam.beamScheduler.getCurrentBeamAllocation() == null) {
                if (Controller.beam.beamScheduler.getPendingBeamRequests() != null) {
                    Controller.beam.beamScheduler.acceptBeamRequest("FBTR1");
                }
            }
        }
    }

    private class AsRequestedLabel extends JLabel implements DevicesPanel.CustomLabel
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

    private class StatusLabel extends JLabel implements DevicesPanel.CustomLabel
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

    private class SetPointLabel extends JLabel implements DevicesPanel.CustomLabel
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

    private class FeedbackLabel extends JLabel implements DevicesPanel.CustomLabel
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

    private List<DevicesPanel.CustomLabel> mLabelList = new ArrayList<>();

    public JLabel mIdleLabelOperMode = new JLabel("Main Control Room is in patient treatment mode", SwingConstants.CENTER);
    public JLabel mIdleLabel1 = new JLabel("Service Beam pre-requirements:", SwingConstants.CENTER);
    public JLabel mIdleLabel2 = new JLabel("Room Searched           -- SERVICE", SwingConstants.CENTER);
    public JLabel mIdleLabel3 = new JLabel("Beamline Selected      -- IBTR3-90", SwingConstants.CENTER);
    public JLabel mIdleLabel4 = new JLabel("MCR Operating Mode  -- AUTO", SwingConstants.CENTER);
    //new BooleanValueLabel(BssController.OperatingMode.MANUAL.toState(), Controller.beam.bssController.getOperatingMode().toState(), new String[]{"BSS Operating Mode -- AUTO"});
    public JLabel mIdleLabel5 = new JLabel("AdaPT Deliver              -- LOGOUT", SwingConstants.CENTER);
    public JLabel mIdleLabel6 = new JLabel("Xray-A Tube                 -- EXTRACTED", SwingConstants.CENTER);

    public JButton mStartServiceBeam = new JButton("Start Service Beam");
    public JButton mAllocateButton = new JButton(RES.getString("bms.beam_scheduler.accept"));
    private static final ResourceDictionary RES = ResourceManager.getInstance()
            .getDictionary("bms/bss/beamscheduler/ui/BeamSchedulerPanel");
    public static final ResourceManager RES2 = ResourceManager.getInstance();
}
