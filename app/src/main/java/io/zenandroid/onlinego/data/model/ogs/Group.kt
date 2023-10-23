package io.zenandroid.onlinego.data.model.ogs

import java.time.Instant

data class Group(
    val id: Long,
    val name: String? = null,
    val short_description: String? = null,
    val description: String? = null,
    val website: String? = null,
    val location: String? = null,
    val require_invitation: Boolean = false,
    val is_public: Boolean = false,
    val admin_only_tournaments: Boolean = false,
    val hide_details: Boolean = false,
    val icon: String? = null,
    val banner: String? = null,
    val has_banner: Boolean = false,
    val has_icon: Boolean = false,
    val member_count: Int = -1,
    val bulletin: String? = null,
    val admins: List<OGSPlayer> = emptyList(),
    val latest_news: GroupNews? = null,
    val settings: Map<String, Any?>? = null,
    val is_member: Boolean = false,
    val ladder_ids: List<Long> = emptyList(),
    val founder: OGSPlayer? = null,
    val has_tournament_records: Boolean = false,
    val has_open_tournaments: Boolean = false,
    val has_active_tournaments: Boolean = false,
    val has_finished_tournaments: Boolean = false,
    val invitation_requests: Any? = null,
) {
    data class GroupNews(
        val id: Long = -1,
        val group: OGSGroup? = null,
        val author: OGSPlayer? = null,
        val posted: Instant? = null,
        val title: String = "",
        val content: String = "",
    )

    data class GroupInvitation(
        val id: Long? = null,
        val request_id: Long? = null,
        val group: Group? = null,
        val message: String? = null,
    )

    companion object {
        fun fromOGSGroup(ogsGroup: OGSGroup): Group =
            Group(
                id = ogsGroup.id,
                name = ogsGroup.name,
                short_description = ogsGroup.summary,
                require_invitation = ogsGroup.require_invitation,
                is_public = ogsGroup.is_public,
                admin_only_tournaments = ogsGroup.admin_only_tournaments,
                hide_details = ogsGroup.hide_details,
                member_count = ogsGroup.member_count,
                icon = ogsGroup.icon,
            )
    }
}
