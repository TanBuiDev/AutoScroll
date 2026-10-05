package com.personal.autoscroll.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.personal.autoscroll.domain.model.AppProfile
import com.personal.autoscroll.domain.model.GestureAxis
import com.personal.autoscroll.domain.model.GestureConfig
import com.personal.autoscroll.domain.model.IntentDirection
import com.personal.autoscroll.domain.model.OverlayConfig
import com.personal.autoscroll.domain.model.OverlayOrientation
import com.personal.autoscroll.domain.model.OverlaySize
import com.personal.autoscroll.domain.model.PresetType
import com.personal.autoscroll.domain.model.ProfileStatus
import com.personal.autoscroll.domain.model.ScrollMode
import com.personal.autoscroll.domain.model.TimingConfig

@Entity(
    tableName = "app_profiles",
    indices = [Index(value = ["packageName"], unique = true)],
)
data class AppProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val packageName: String,
    val appName: String,
    val enabled: Boolean,
    val presetType: String,
    val profileStatus: String,
    val intentDirection: String,
    val gestureAxis: String,
    val distancePercent: Int,
    val startXPercent: Int,
    val startYPercent: Int,
    @ColumnInfo(name = "speedLevel")
    val swipeDurationMillis: Long,
    val invertPhysicalDirection: Boolean,
    val scrollMode: String,
    val delayMillis: Long,
    val startDelayMillis: Long,
    val repeatCount: Int?,
    val durationMillis: Long?,
    val stopOnAppChange: Boolean,
    val compactPositionX: Int,
    val compactPositionY: Int,
    val expandedPositionX: Int,
    val expandedPositionY: Int,
    val opacity: Float,
    val overlaySize: String,
    @ColumnInfo(defaultValue = "'Vertical'")
    val overlayOrientation: String,
    val showNextPrevious: Boolean,
    val autoCollapse: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)

fun AppProfile.toEntity(): AppProfileEntity = AppProfileEntity(
    id = id,
    packageName = packageName,
    appName = appName,
    enabled = enabled,
    presetType = presetType.name,
    profileStatus = profileStatus.name,
    intentDirection = gestureConfig.intentDirection.name,
    gestureAxis = gestureConfig.axis.name,
    distancePercent = gestureConfig.distancePercent,
    startXPercent = gestureConfig.startXPercent,
    startYPercent = gestureConfig.startYPercent,
    swipeDurationMillis = gestureConfig.swipeDurationMillis,
    invertPhysicalDirection = gestureConfig.invertPhysicalDirection,
    scrollMode = timingConfig.mode.name,
    delayMillis = timingConfig.delayMillis,
    startDelayMillis = timingConfig.startDelayMillis,
    repeatCount = timingConfig.repeatCount,
    durationMillis = timingConfig.durationMillis,
    stopOnAppChange = timingConfig.stopOnAppChange,
    compactPositionX = overlayConfig.compactPositionX,
    compactPositionY = overlayConfig.compactPositionY,
    expandedPositionX = overlayConfig.expandedPositionX,
    expandedPositionY = overlayConfig.expandedPositionY,
    opacity = overlayConfig.opacity,
    overlaySize = overlayConfig.size.name,
    overlayOrientation = overlayConfig.orientation.name,
    showNextPrevious = overlayConfig.showNextPrevious,
    autoCollapse = overlayConfig.autoCollapse,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun AppProfileEntity.toDomain(): AppProfile = AppProfile(
    id = id,
    packageName = packageName,
    appName = appName,
    enabled = enabled,
    presetType = PresetType.valueOf(presetType),
    profileStatus = ProfileStatus.valueOf(profileStatus),
    gestureConfig = GestureConfig(
        intentDirection = IntentDirection.valueOf(intentDirection),
        axis = GestureAxis.valueOf(gestureAxis),
        distancePercent = distancePercent,
        startXPercent = startXPercent,
        startYPercent = startYPercent,
        swipeDurationMillis = normalizeStoredSwipeDuration(swipeDurationMillis),
        invertPhysicalDirection = invertPhysicalDirection,
    ),
    timingConfig = TimingConfig(
        mode = ScrollMode.valueOf(scrollMode),
        delayMillis = delayMillis,
        startDelayMillis = startDelayMillis,
        repeatCount = repeatCount,
        durationMillis = durationMillis,
        stopOnAppChange = stopOnAppChange,
    ),
    overlayConfig = OverlayConfig(
        compactPositionX = compactPositionX,
        compactPositionY = compactPositionY,
        expandedPositionX = expandedPositionX,
        expandedPositionY = expandedPositionY,
        opacity = opacity,
        size = OverlaySize.valueOf(overlaySize),
        orientation = runCatching { OverlayOrientation.valueOf(overlayOrientation) }
            .getOrDefault(OverlayOrientation.Vertical),
        showNextPrevious = showNextPrevious,
        autoCollapse = autoCollapse,
    ),
    createdAt = createdAt,
    updatedAt = updatedAt,
)

private fun normalizeStoredSwipeDuration(value: Long): Long =
    if (value in 1L..10L) {
        4_000L - ((value - 1L) * 3_100L / 9L)
    } else {
        value
    }
