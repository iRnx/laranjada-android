package com.rnx.laranjada.domain.model

data class AccountOverview(
    val account: AccountUser,
    val profiles: AccountProfilesSummary,
    val devices: AccountDevicesSummary
)

data class AccountUser(
    val uuid: String,
    val name: String,
    val username: String,
    val email: String,
    val phone: String,
    val isEmailVerified: Boolean,
    val isActive: Boolean,
    val dateJoined: String?,
    val lastLogin: String?
)

data class AccountProfilesSummary(
    val count: Int,
    val maxProfiles: Int,
    val remainingProfiles: Int,
    val canCreateProfile: Boolean
)

data class AccountDevicesSummary(
    val count: Int,
    val connectedCount: Int,
    val otherConnectedCount: Int,
    val items: List<AccountDevice>
)

data class AccountDevice(
    val uuid: String,
    val deviceName: String,
    val deviceType: String,
    val platform: String,
    val browser: String,
    val os: String,
    val isConnected: Boolean,
    val isCurrentDevice: Boolean,
    val isActive: Boolean,
    val lastSeenAt: String?,
    val connectedAt: String?
)

data class AccountDeviceActionResult(
    val message: String,
    val currentSessionEnded: Boolean,
    val disconnectedCount: Int
)

data class AccountPasswordChangeResult(
    val message: String
)