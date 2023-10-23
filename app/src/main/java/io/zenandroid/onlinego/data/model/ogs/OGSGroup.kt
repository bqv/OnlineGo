package io.zenandroid.onlinego.data.model.ogs

data class OGSGroup(
    val id: Long,
    val name: String? = null,
    val summary: String? = null,
    val require_invitation: Boolean = false,
    val is_public: Boolean = false,
    val admin_only_tournaments: Boolean = false,
    val hide_details: Boolean = false,
    val member_count: Int = -1,
    val icon: String? = null,
)
