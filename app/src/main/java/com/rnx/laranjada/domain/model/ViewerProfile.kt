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
    val profiles: List<ViewerProfile>,
    val maxProfiles: Int,
    val activeProfilesCount: Int,
    val remainingProfiles: Int,
    val canCreateProfile: Boolean
)

data class ViewerAvatarLibraryItem(
    val uuid: String,
    val name: String,
    val slug: String,
    val imageUrl: String?
)

data class ViewerAvatarGroup(
    val uuid: String,
    val name: String,
    val slug: String,
    val avatars: List<ViewerAvatarLibraryItem>
)

data class ViewerAvatarLibrary(
    val searchQuery: String,
    val groups: List<ViewerAvatarGroup>,
    val ungroupedAvatars:
    List<ViewerAvatarLibraryItem>
)