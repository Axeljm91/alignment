// ///////////////////////////////////////////////////////////////////
// Copyright (c) by Ion Beam Applications S.A.
// All rights reserved
//
// Rue du cyclotron, 3
// B-1348 Louvain-la-Neuve
// Tel : +32.10.47.58.11
// ///////////////////////////////////////////////////////////////////

package com.iba.icompx.ui.panel;

import java.awt.Color;
import java.util.Arrays;

import javax.swing.Icon;
import javax.swing.JLabel;

import com.iba.icompx.ui.util.ResourceManager;
import com.iba.pts.bms.bss.controller.api.BssController;

/**
 * Label whose display (icon, text and color) can be set according to the value of an enumeration.
 * @author sdubois
 * @author swantie
 * @param <T> The type of the enum values to display in this Label.
 */
public class EnumValueLabel<T extends Enum<T>> extends JLabel
{
    /**
     * The representational state of the enum value to display.
     * @author sdubois
     */
    public enum State
    {
        /** OK display state (green V). */
        OK(RES.getImageIcon("icompx/check/OK"), RES.getNormalGreen()),

        /** Error display state (red X). */
        ERROR(RES.getImageIcon("icompx/check/ERROR"), Color.RED),

        /** Warning display state. */
        WARNING(RES.getImageIcon("icompx/check/WARNING"), RES.getSecondaryColor()),

        /** Information display state (black circle). */
        INFO(RES.getImageIcon("icompx/icons/disabled"), Color.BLACK),

        /** Disabled display state (gray circle). */
        DISABLED(RES.getImageIcon("icompx/icons/disabled"), Color.GRAY);

        /**
         * Constructs the state value.
         * @param pIcon The icon representing the state.
         * @param pColor The color of this icon.
         */
        private State(Icon pIcon, Color pColor)
        {
            mIcon = pIcon;
            mColor = pColor;
        }


        /**
         * Gets the icon representing the current state.
         * @return The current state icon.
         */
        Icon getIcon()
        {
            return mIcon;
        }

        /**
         * Gets the color of the current state icon.
         * @return The current state icon color.
         */
        Color getColor()
        {
            return mColor;
        }

        /**
         * The icon representing the current state.
         */
        private final Icon mIcon;
        /**
         * The color of the current state icon.
         */
        private final Color mColor;
    }

    /**
     * The states representing the enum values.
     */
    private final State[] mDisplayStates;
    /**
     * The labels of the enum values.
     */
    private final String[] mDisplayLabels;
    /**
     * The state representing a null value (default as DISABLED).
     */
    private State mNullValueDisplayState = State.DISABLED;
    /**
     * The displayed label of a null value.
     */
    private String mNullValueLabel = "Unknown";
    /**
     * The current enum value.
     */
    private T mValue;

    /**
     * Generated serialization ID.
     */
    private static final long serialVersionUID = 3211944291568730601L;
    /**
     * The resources manager instance.
     */
    protected static final ResourceManager RES = ResourceManager.getInstance();

    /**
     * Creates the Label for the provided enum type and representational states.
     * <p>
     * The numbers of representational states should be as follows:<br/>
     * - 0: The state INFO is used for all enum values.<br/>
     * - 1: Same state for all the enum values.<br/>
     * - Nb enum values: One state for each enum value.<br/>
     * - Nb enum values + 1 : One state for each enum value + first state for null value.<br/>
     * <br/>
     * Note that the enum values will be displayed with their default string representation (enum value name as
     * string).
     * </p>
     * @param pType The type of the enum values to display.
     * @param pDisplayStates The representational states of the enum values.
     * @throws IllegalArgumentException Thrown when the number of representational states is not correct.
     */
    public EnumValueLabel(Class<T> pType, State... pDisplayStates)
    {
        this(pType, pDisplayStates, new String[] {});
    }

    /**
     * Creates the Label for the provided enum type and values labels.
     * <p>
     * The numbers of provided labels:<br/>
     * - 0: The enum values name will be displayed.<br/>
     * - 1: The same label will be displayed for any enum value.<br/>
     * - Nb enum values: One label for each enum value.<br/>
     * - Nb enum values + 1 : One label for each enum value + first label for null value.<br/>
     * Note that no representational state will be displayed with this constructor.
     * </p>
     * @param pType The type of the enum values to display.
     * @param pDisplayLabelFirst The first enum value label.
     * @param pDisplayLabelOthers The other enum value labels.
     * @throws IllegalArgumentException Thrown when the number of representational states or labels is not
     *         correct.
     */
    public EnumValueLabel(Class<T> pType, String pDisplayLabelFirst, String... pDisplayLabelOthers)
    {
        this(pType, new State[] {}, getLabelParameters(pDisplayLabelFirst, pDisplayLabelOthers));
    }

    /**
     * Creates the Label for the provided enum type, representational states and enum values labels.
     * <p>
     * The numbers of representational states should be as follows:<br/>
     * - 0: The state INFO is used for all enum values.<br/>
     * - 1: Same state for all the enum values.<br/>
     * - Nb enum values: One state for each enum value.<br/>
     * - Nb enum values + 1 : One state for each enum value + first state for null value.<br/>
     * <br/>
     * A similar rule applies for the provided labels:<br/>
     * - 0: The enum values name will be displayed.<br/>
     * - 1: The same label will be displayed for any enum value.<br/>
     * - Nb enum values: One label for each enum value.<br/>
     * - Nb enum values + 1 : One label for each enum value + first label for null value.
     * </p>
     * @param pType The type of the enum values to display.
     * @param pDisplayStates The representational states of the enum values.
     * @param pDisplayLabels The enum value labels.
     * @throws IllegalArgumentException Thrown when the number of representational states or labels is not
     *         correct.
     */
    public EnumValueLabel(Class<T> pType, State[] pDisplayStates, String... pDisplayLabels)
    {
        final int nbEnumValues = pType.getEnumConstants().length;

        if (pDisplayStates == null || pDisplayStates.length == 0)
        {
            mDisplayStates = new State[nbEnumValues];
            Arrays.fill(mDisplayStates, State.INFO);
        }
        else if (pDisplayStates.length == 1)
        {
            mDisplayStates = new State[nbEnumValues];
            Arrays.fill(mDisplayStates, pDisplayStates[0]);
        }
        else if (pDisplayStates.length == nbEnumValues)
        {
            mDisplayStates = pDisplayStates;
        }
        else if (pDisplayStates.length == nbEnumValues + 1)
        {
            mNullValueDisplayState = pDisplayStates[0];
            mDisplayStates = new State[nbEnumValues];

            System.arraycopy(pDisplayStates, 1, mDisplayStates, 0, nbEnumValues);
        }
        else
        {
            throw new IllegalArgumentException("The number of representational states is not correct for type " + pType + " ("
                    + nbEnumValues + " elements)");
        }

        if (pDisplayLabels == null || pDisplayLabels.length == 0)
        {
            mDisplayLabels = new String[mDisplayStates.length];
            for (int i = 0; i < pType.getEnumConstants().length; ++i)
            {
                mDisplayLabels[i] = pType.getEnumConstants()[i].toString();
            }
        }
        else if (pDisplayLabels.length == 1)
        {
            mDisplayLabels = new String[mDisplayStates.length];
            Arrays.fill(mDisplayLabels, pDisplayLabels[0]);
        }
        else if (pDisplayLabels.length == nbEnumValues)
        {
            mDisplayLabels = pDisplayLabels;
        }
        else if (pDisplayLabels.length == nbEnumValues + 1)
        {
            mNullValueLabel = pDisplayLabels[0];
            mDisplayLabels = new String[nbEnumValues];
            System.arraycopy(pDisplayLabels, 1, mDisplayLabels, 0, nbEnumValues);
        }
        else
        {
            throw new IllegalArgumentException("The number of representational labels is not correct for type " + pType + " ("
                    + nbEnumValues + " elements)");
        }

        setValue(null);
    }

    /**
     * Sets the state representing a null value (default as DISABLED).
     * @param pNullValueDisplayState The null value display state.
     */
    public void setNullValueDisplayState(State pNullValueDisplayState)
    {
        mNullValueDisplayState = pNullValueDisplayState;
    }

    /**
     * Sets the displayed label of a null value (default as 'N/A').
     * @param pNullValueLabel The null value display label.
     */
    public void setNullValueLabel(String pNullValueLabel)
    {
        mNullValueLabel = pNullValueLabel;
    }

    /**
     * Sets the current enum value to display.
     * @param pValue The current enum value to display.
     */
    public final void setValue(T pValue)
    {
        mValue = pValue;

        if (pValue == null)
        {
            setText(mNullValueLabel);
            setIcon(mNullValueDisplayState.getIcon());
            setForeground(mNullValueDisplayState.getColor());
            return;
        }

        int i = pValue.ordinal();

        if (mDisplayLabels == null)
        {
            setText(pValue.toString());
            setIcon(mDisplayStates[i].getIcon());
            setForeground(mDisplayStates[i].getColor());
        }
        else
        {
            setText(mDisplayLabels[i]);
            setIcon(mDisplayStates[i].getIcon());
            setForeground(mDisplayStates[i].getColor());
        }
    }

    /**
     * Gets the current enum value displayed in this Label.
     * @return The current enum value.
     */
    public T getValue()
    {
        return mValue;
    }

    private static String[] getLabelParameters(String pLabelFirst, String... pOtherLabels)
    {
        String[] labels;

        if (pOtherLabels != null)
        {
            labels = new String[pOtherLabels.length + 1];
            labels[0] = pLabelFirst;
            System.arraycopy(pOtherLabels, 0, labels, 1, pOtherLabels.length);
        }
        else
        {
            labels = new String[] { pLabelFirst };
        }

        return labels;
    }
}
