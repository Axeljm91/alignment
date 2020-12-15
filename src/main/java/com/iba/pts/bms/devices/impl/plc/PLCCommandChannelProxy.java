// ///////////////////////////////////////////////////////////////////
// Copyright (c) by Ion Beam Applications S.A.
// All rights reserved
//
// Rue du cyclotron, 3
// B-1348 Louvain-la-Neuve
// Tel : +32.10.47.58.11
// ///////////////////////////////////////////////////////////////////

package com.iba.pts.bms.devices.impl.plc;

import com.iba.icomp.core.component.AbstractProxy;
import com.iba.icomp.core.component.ComponentProperty;

/**
 * Proxy for the BLPSCU command channel (actually the BLPSCU writer).
 */
public class PLCCommandChannelProxy extends AbstractProxy implements PLCCommandChannel
{
    @Override
    public void sendCommand(int pCommandId)
    {
        proxySet("command", pCommandId);
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "UUF_UNUSED_FIELD", justification = "This field is used by AbstractProxy")
    @ComponentProperty(definition = "PLC.Command")
    private int mCommand;
}