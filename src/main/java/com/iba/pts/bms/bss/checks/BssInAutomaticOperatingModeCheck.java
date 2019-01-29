package com.iba.pts.bms.bss.checks;

import com.iba.icomp.core.checks.AbstractInternalCheck;
import com.iba.icomp.core.checks.CheckResult;
import com.iba.pts.bms.bss.controller.api.BssController;
import com.iba.pts.bms.bss.controller.api.BssController.OperatingMode;
import com.iba.pts.bms.common.utilities.OperatorFeedback;


public class BssInAutomaticOperatingModeCheck extends AbstractInternalCheck {
    private BssController mBssController;

    public BssInAutomaticOperatingModeCheck(BssController pBssController) {
        this.setGroup("BSS");
        this.setName(this.getClass().getSimpleName());
        this.mBssController = pBssController;
    }

    protected void doPerform(long pTime) {
        if (this.mBssController.getOperatingMode() != OperatingMode.AUTOMATIC) {
            this.setResult(CheckResult.OK);
        } else {
            String message = String.format("%s failed: BSS is not in manual operating mode", this.getName());
            this.getLogger().error(message, new Object[0]);
            if (this.getResult() != CheckResult.ERROR) {
                OperatorFeedback.error(message, new Object[0]);
            }

            this.setResult(CheckResult.ERROR);
        }

    }
}
