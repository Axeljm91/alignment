// ///////////////////////////////////////////////////////////////////
// Copyright (c) by Ion Beam Applications S.A.
// All rights reserved
//
// Rue du cyclotron, 3
// B-1348 Louvain-la-Neuve
// Tel : +32.10.47.58.11
// ///////////////////////////////////////////////////////////////////

package com.iba.icompx.ui.panel;

import com.iba.icompx.ui.panel.BooleanValueLabel.BooleanEnum;
import com.iba.pts.bms.bss.controller.api.BssController;

/**
 * Label whose display (icon, text and color) can be set according to a boolean value.
 * @author sdubois
 * @author swantie
 */
public class BooleanValueLabel extends EnumValueLabel<BooleanEnum>
{

//    public BooleanValueLabel(BssController.OperatingMode automatic, BssController.OperatingMode operatingMode, String[] strings) {
//        return;
//    }

    /**
     * Internal enumeration used to represent a boolean value in the EnumValueLabel.
     * @author sdubois
     */
    public enum BooleanEnum
    {
        /**
         * The enum value representing a FALSE boolean.
         */
        False(Boolean.FALSE),
        /**
         * The enum value representing a TRUE boolean.
         */
        True(Boolean.TRUE);

        /**
         * Constructs the boolean enum instance.
         * @param pBooleanValue The equivalent boolean value.
         */
        private BooleanEnum(Boolean pBooleanValue)
        {
            mBooleanValue = pBooleanValue;
        }

        /**
         * Gets the boolean value equivalent to the current boolean enum instance.
         * @return The boolean value of the current boolean enum instance.
         */
        Boolean getBooleanValue()
        {
            return mBooleanValue;
        }

        /**
         * Builds a boolean enum value from the provided boolean value.
         * @param pValue The boolean value to convert.
         * @return The converted boolean enum value.
         */
        static BooleanEnum fromBoolean(Boolean pValue)
        {
            return pValue == null ? null : (pValue ? BooleanEnum.True : BooleanEnum.False);
        }

        /**
         * The boolean value equivalent to the current boolean enum value.
         */
        private final Boolean mBooleanValue;
    }

    /**
     * Generated serialization ID.
     */
    private static final long serialVersionUID = 6073029635156745893L;

    /**
     * Creates the Label for the provided representational states.
     * <p>
     * Note that the boolean values will be displayed with their default string representation ('True' or
     * 'False').
     * </p>
     * @param pDisplayStates The representational states of the boolean values.
     * @throws IllegalArgumentException Thrown when the number of representational states is not correct (ie
     *         greater than 2).
     */
    public BooleanValueLabel(EnumValueLabel.State... pDisplayStates)
    {
        this(pDisplayStates, new String[] {});
    }

    /**
     * Creates the Label for the provided representational states and labels.
     * @param pDisplayState The unique representational state of the boolean values.
     * @param pDisplayLabels The displayed labels of the boolean values (max 3).
     * @throws IllegalArgumentException Thrown when the number of representational labels is not correct (ie
     *         greater than 3).
     */
    public BooleanValueLabel(EnumValueLabel.State pDisplayState, String... pDisplayLabels)
    {
        this(new EnumValueLabel.State[] { pDisplayState }, pDisplayLabels);
    }

    /**
     * Creates the Label for the provided representational states and labels.
     * @param pDisplayStateFalse The representational state of the False value.
     * @param pDisplayStateTrue The representational state of the True value.
     * @param pDisplayLabels The displayed labels of the boolean values (max 3).
     * @throws IllegalArgumentException Thrown when the number of representational labels is not correct (ie
     *         greater than 3).
     */
    public BooleanValueLabel(EnumValueLabel.State pDisplayStateFalse, EnumValueLabel.State pDisplayStateTrue,
                             String... pDisplayLabels)
    {
        this(new EnumValueLabel.State[] { pDisplayStateFalse, pDisplayStateTrue }, pDisplayLabels);
    }

    /**
     * Creates the Label for the provided representational states and labels.
     * @param pDisplayStateNull The representation state of the null value.
     * @param pDisplayStateFalse The representational state of the False value.
     * @param pDisplayStateTrue The representational state of the True value.
     * @param pDisplayLabels The displayed labels of the boolean values (max 3).
     * @throws IllegalArgumentException Thrown when the number of representational labels is not correct (ie
     *         greater than 3).
     */
    public BooleanValueLabel(EnumValueLabel.State pDisplayStateNull, EnumValueLabel.State pDisplayStateFalse,
                             EnumValueLabel.State pDisplayStateTrue, String... pDisplayLabels)
    {
        this(new EnumValueLabel.State[] { pDisplayStateNull, pDisplayStateFalse, pDisplayStateTrue }, pDisplayLabels);
    }

    /**
     * Creates the Label for the provided representational labels.
     * <p>
     * Note that the boolean values will be displayed with the default representational states as ERROR for
     * False, OK for True, and DISABLED for null.<br/>
     * If no representational label is provided, the strings 'False' and 'True' will be displayed by default.
     * If only one is provided, it will be displayed no matter what is the boolean value state (only the state
     * will change).
     * </p>
     * @param pDisplayLabels The displayed labels of the boolean values (max 3).
     * @throws IllegalArgumentException Thrown when the number of representational labels is not correct (ie
     *         greater than 3).
     */
    public BooleanValueLabel(String... pDisplayLabels)
    {
        this(new EnumValueLabel.State[] { EnumValueLabel.State.DISABLED, EnumValueLabel.State.ERROR, EnumValueLabel.State.OK },
                pDisplayLabels);
    }

    /**
     * Creates the Label for the provided representational states and labels.
     * @param pDisplayStates The array of representational states of the boolean values (max 3).
     * @param pDisplayLabels The displayed labels of the boolean values (max 3).
     * @throws IllegalArgumentException Thrown when the number of representational states or labels are not
     *         correct (ie greater than 3).
     */
    public BooleanValueLabel(EnumValueLabel.State[] pDisplayStates, String... pDisplayLabels)
    {
        super(BooleanEnum.class, pDisplayStates, pDisplayLabels);
    }

    /**
     * Sets the current boolean value to display.
     * @param pValue The current boolean value to display.
     */
    public void setBoolValue(Boolean pValue)
    {
        setValue(BooleanEnum.fromBoolean(pValue));
    }

    /**
     * Gets the current boolean value displayed in this Label.
     * @return The current boolean value.
     */
    public Boolean getBoolValue()
    {
        BooleanEnum enumValue = getValue();
        return enumValue == null ? null : enumValue.getBooleanValue();
    }
}
