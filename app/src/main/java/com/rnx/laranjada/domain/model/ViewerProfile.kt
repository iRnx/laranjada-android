package com.rnx.laranjada.domain.model

data class ViewerAvatar(
    val uuid: String,
    val name: String,
    val imageUrl: String?
)

data class ViewerProfile(
    val uuid: String,
    val name: String,
    val hasPin: Boolean,
    val isDefault: Boolean,
    val isSelected: Boolean,
    val avatar: ViewerAvatar?
)

data class ViewerProfileList(
    val selectedProfileUuid: String?,
    val profiles: List<ViewerProfile>
)