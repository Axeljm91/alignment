// ///////////////////////////////////////////////////////////////////
// Copyright (c) by Ion Beam Applications S.A.
// All rights reserved
//
// Rue du cyclotron, 3
// B-1348 Louvain-la-Neuve
// Tel : +32.10.47.58.11
// ///////////////////////////////////////////////////////////////////

package com.iba.pts.treatmentroomsession;

import com.iba.icomp.core.property.*;

import com.iba.pts.treatmentroomsession.TreatmentRoomSession;

/**
 * A component or a proxy indicating the current Treatment Session Mode.
 *
 * @author swantie
 */
public interface TreatmentSessionModeHolder extends PropertyChangeProvider
{
    /**
     * Gets the current Treatment Session mode.
     *
     * @return The current Treatment Session mode.
     */
    TreatmentRoomSession getTreatmentRoomSession();

    /**
     * The name of the Treatment Session Mode component name.
     */
    public final static String COMPONENT_NAME = "TSM";
    /**
     * The name of the Treatment Session Mode property (for java bean synchronous PropertyChangeListener).
     */
    public final static String PROPERTY_NAME = "treatmentRoomSession";
    /**
     * The name of the Treatment Session Mode IComP property.
     */
    final static String INTERNAL_PROPERTY_NAME = "TreatmentRoomSession";

}
