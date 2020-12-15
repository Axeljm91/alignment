//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.iba.pts.treatmentroomsession;

import com.iba.icomp.core.component.AbstractProxy;
import com.iba.icomp.core.component.ComponentDictionary;
import com.iba.icomp.core.component.ComponentProperty;

@ComponentDictionary(
        classpaths = {"classpath:config/treatmentRoomSession/treatmentRoomSessionProperties.xml"}
)
public class TreatmentSessionModeProxy extends AbstractProxy implements TreatmentSessionModeHolder {
    @ComponentProperty(
            definition = "TSM.TreatmentRoomSession"
    )
    private TreatmentRoomSession mTreatmentRoomSession;

    public TreatmentSessionModeProxy() {
        this.mTreatmentRoomSession = TreatmentRoomSession.None;
    }

    public TreatmentRoomSession getTreatmentRoomSession() {
        return this.mTreatmentRoomSession;
    }
}
