package io.zenandroid.onlinego.data.model.ogs

import com.squareup.moshi.Json
import org.threeten.bp.Instant

data class User (

    var id: Long = 0,
    var username: String? = null,
    var professional: Boolean? = null,
    var ranking: Float = 0f,
    var rating: Float? = null,
    var deviation: Float? = null,
    var ratings: Ratings? = null,
    var country: String? = null,
    var language: String? = null,
    var name: String? = null,
    var first_name: String? = null,
    var last_name: String? = null,
    var real_name_is_private: Boolean? = null,
    var about: String? = null,
    var ui_class_extra: Any? = null,
    var is_moderator: Boolean? = null,
    var is_superuser: Boolean? = null,
    var is_tournament_moderator: Boolean? = null,
    var is_bot: Boolean? = null,
    var timeout_provisional: Boolean? = null,
    var bot_ai: Any? = null,
    var bot_owner: Any? = null,
    var website: String? = null,
    var icon: String? = null,
    var registration_date: Instant? = null,
    var vacation_left: Long? = null,
    var on_vacation: Boolean? = null,

) {
	data class Ratings (                     var version: Int = 5,
		@Json(name = "overall")              var overall: OGSPlayer.Rating? = null,
		@Json(name = "9x9")                  var overall_9x9: OGSPlayer.Rating? = null,
		@Json(name = "13x13")                var overall_13x13: OGSPlayer.Rating? = null,
		@Json(name = "19x19")                var overall_19x19: OGSPlayer.Rating? = null,
		@Json(name = "blitz")                var blitz: OGSPlayer.Rating? = null,
		@Json(name = "blitz-9x9")            var blitz_9x9: OGSPlayer.Rating? = null,
		@Json(name = "blitz-13x13")          var blitz_13x13: OGSPlayer.Rating? = null,
		@Json(name = "blitz-19x19")          var blitz_19x19: OGSPlayer.Rating? = null,
		@Json(name = "live")                 var live: OGSPlayer.Rating? = null,
		@Json(name = "live-9x9")             var live_9x9: OGSPlayer.Rating? = null,
		@Json(name = "live-13x13")           var live_13x13: OGSPlayer.Rating? = null,
		@Json(name = "live-19x19")           var live_19x19: OGSPlayer.Rating? = null,
		@Json(name = "correspondence")       var correspondence: OGSPlayer.Rating? = null,
		@Json(name = "correspondence-9x9")   var correspondence_9x9: OGSPlayer.Rating? = null,
		@Json(name = "correspondence-13x13") var correspondence_13x13: OGSPlayer.Rating? = null,
		@Json(name = "correspondence-19x19") var correspondence_19x19: OGSPlayer.Rating? = null,
	)
}

