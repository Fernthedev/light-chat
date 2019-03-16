package com.github.fernthedev.gui;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MainGUI extends JPanel {

    private JList<String> actionList;
    private JButton onButton;
    private JSpinner pin;
    private JSpinner selectedFrame;
    private JSpinner fps;
    private JComboBox<String> whichpi;
    private JTextField saveOpenPath;
    private JPanel panel;
    private JButton saveButton;
    private JButton openButton;
    private JScrollPane scrollPane;
    private JLabel statusText;

    private PinData[] pinDatas = new PinData[32];
    private PinData selectedPinData;

    private int pinMax;

    private int frameInt;

    private List<String> actionLog = new ArrayList<>();

    private List<String> logs = new ArrayList<>();

    public static void main(String[] args) {
        JFrame frame = new JFrame("MainGUI");
        frame.setContentPane(new MainGUI().panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        //whichpi.Items.Add("custom");




    }

    public MainGUI() {
        whichpi.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                BoardType selectedPi = BoardType.valueOf(Objects.requireNonNull(whichpi.getSelectedItem()).toString());

                pinMax = allPins(selectedPi);
            }
        });

        List<BoardType> boardTypes = Arrays.asList(BoardType.values());
        System.out.println(boardTypes);
        for(BoardType boardType : boardTypes) {
            whichpi.addItem(boardType.toString());
        }

        whichpi.setSelectedIndex(0);

        for (int i = 0; i < pinDatas.length; i++) {
            PinData pinData = new PinData(i);
            pinDatas[i] = pinData;
        }

        frameInt = (int)fps.getValue();

        selectedPinData = pinDatas[(int)pin.getValue()];

        selectedPinData.getFrames().add(new PinData.FrameData(frameInt, PinData.Pinmode.OFF));

        fps.setValue(10);




       statusText.setText("The current status is not set");
        pin.addChangeListener(new ChangeListener() {
            /**
             * Invoked when the target of the listener has changed its state.
             *
             * @param e a ChangeEvent object
             */
            @Override
            public void stateChanged(ChangeEvent e) {
                if((int)pin.getValue() > pinMax) {
                    pin.setValue(pinMax);
                }

                if((int)pin.getValue() < 0) {
                    pin.setValue(0);
                }

                selectedPinData = pinDatas[(int)pin.getValue()];
                updateLog();
                if(selectedPinData.getFrames().size() > 0 && getCurrentFrame() != null) {
                    checkButton(getCurrentFrame());
                }
            }
        });

        selectedFrame.addChangeListener(new ChangeListener() {
            /**
             * Invoked when the target of the listener has changed its state.
             *
             * @param e a ChangeEvent object
             */
            @Override
            public void stateChanged(ChangeEvent e) {
                if((int)selectedFrame.getValue() < 0) {
                    selectedFrame.setValue(0);
                }

                frameInt = (int)selectedFrame.getValue() - 1;

                int frameTime = frameInt + 1;


                if (selectedPinData.getFrames().toArray().length < frameTime) {
                    int times = frameTime - selectedPinData.getFrames().toArray().length;

                    for (int i = 0; i < times; i++) {
                        int newFrame = frameTime - i;
                        selectedPinData.getFrames().add(new PinData.FrameData(newFrame, PinData.Pinmode.OFF));
                    }
                }

                if (frameInt < 0) frameInt = 0;

                PinData.FrameData frameData = selectedPinData.getFrames().get(frameInt);
                updateFrame(frameData);
            }
        });

        onButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                statusText.setText(String.valueOf(selectedPinData.getFrames().toArray().length));

                checkButton(getCurrentFrame());
            }
        });
        saveButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        openButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
    }

    private void checkButton(PinData.FrameData frameData) {
        String statusNow = onButton.getText();

        boolean toggle = false;

        if(statusNow.equalsIgnoreCase("on")) {
            onButton.setText("OFF");
            toggle = true;
        }
        if(statusNow.equalsIgnoreCase("off")) {
            onButton.setText("ON");
            toggle = false;
        }



        updateFrame(frameData, toggle);
    }

    private void updateFrame(PinData.FrameData frameData, boolean status) {
        if (status) {
            frameData.setPinMode(PinData.Pinmode.ON);
        } else {
            frameData.setPinMode(PinData.Pinmode.OFF);
        }

        updateFrame(frameData);

    }

    private void updateFrame(PinData.FrameData frameData) {
        if (frameData.GetPinmode().equals(PinData.Pinmode.ON)) {
            statusText.setText("The current status is on");
        } else {
            statusText.setText("The current status is off");
        }
        updateLog();
    }

    private void updateLog() {
        List<String> empty = new ArrayList<>();
        actionList.setListData(empty.toArray(new String[0]));
        actionLog.clear();

        for (int i = 0; i < selectedPinData.getFrames().size(); i++) {
            PinData.FrameData frameData = selectedPinData.getFrames().get(i);
            actionLog.add("Frame" + frameData.getFrame() + " is " + frameData.GetPinmode().toString());
            actionList.setListData(actionLog.toArray(new String[0]));
        }
    }

    private PinData.FrameData getCurrentFrame() {
        return selectedPinData.getFrames().get(frameInt);
    }

    private int allPins(BoardType selectedPi) {
        int max;
        max = 16;

        // no further pins to add for Model B Rev 1 boards
        if (selectedPi == BoardType.RaspberryPi_B_Rev1) {
            // return pins collection
            return max;
        }

        // add pins exclusive to Model A and Model B (Rev2)
        if (selectedPi == BoardType.RaspberryPi_A ||
                selectedPi == BoardType.RaspberryPi_B_Rev2) {
            max = 20;
        }

        // add pins exclusive to Models A+, B+, 2B, 3B, and Zero
        else {
            max = 31;
        }

        // return pins collection
        return max;
    }

    public enum BoardType {
        UNKNOWN,
        //------------------------
        RaspberryPi_A,
        RaspberryPi_B_Rev1,
        RaspberryPi_B_Rev2,
        RaspberryPi_A_Plus,
        RaspberryPi_B_Plus,
        RaspberryPi_ComputeModule,
        RaspberryPi_2B,
        RaspberryPi_3B,
        RaspberryPi_3B_Plus,
        RaspberryPi_Zero,
        RaspberryPi_ComputeModule3,
        RaspberryPi_ZeroW,
        RaspberryPi_Alpha,
        RaspberryPi_Unknown,
        //------------------------
        // (LEMAKER BANANAPI)
        BananaPi,
        BananaPro,
        //------------------------
        // (SINOVOIP BANANAPI)  (see: https://github.com/BPI-SINOVOIP/WiringPi/blob/master/wiringPi/wiringPi_bpi.c#L1318)
        Bpi_M1,
        Bpi_M1P,
        Bpi_M2,
        Bpi_M2P,
        Bpi_M2P_H2_Plus,
        Bpi_M2P_H5,
        Bpi_M2U,
        Bpi_M2U_V40,
        Bpi_M2M,
        Bpi_M3,
        Bpi_R1,
        Bpi_M64,
        //------------------------
        Odroid,
        //------------------------
        OrangePi,
        //------------------------
        NanoPi_M1,
        NanoPi_M1_Plus,
        NanoPi_M3,
        NanoPi_NEO,
        NanoPi_NEO2,
        NanoPi_NEO2_Plus,
        NanoPi_NEO_Air,
        NanoPi_S2,
        NanoPi_A64,
        NanoPi_K2
        //------------------------
    }


}
