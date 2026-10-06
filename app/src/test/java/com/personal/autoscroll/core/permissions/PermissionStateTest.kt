package com.personal.autoscroll.core.permissions

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionStateTest {
    @Test fun enabledButUnboundServiceIsNotReady() {
        assertFalse(PermissionState(true, false, true).isAccessibilityReady)
    }

    @Test fun disabledServiceWithStaleConnectionIsNotReady() {
        assertFalse(PermissionState(false, true, true).isAccessibilityReady)
    }

    @Test fun enabledAndBoundServiceIsReady() {
        assertTrue(PermissionState(true, true, false).isAccessibilityReady)
    }
}
