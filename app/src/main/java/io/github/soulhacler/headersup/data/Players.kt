package io.github.soulhacler.headersup.data

import android.content.Context
import org.json.JSONObject

/** A footballer from assets/players.json. */
data class Player(val name: String, val tags: Set<String>)

/** The categories a round can be played with. [tag] matches the tags in players.json. */
enum class Category(val title: String, val subtitle: String, val tag: String?) {
    ALL("All Players", "Everyone in the squad", null),
    HOUSEHOLD_NAMES("Household Names", "Everyone knows these", "famous"),
    PREMIER_LEAGUE_LEGENDS("Premier League Legends", "Icons of the PL era", "premier_league_legend"),
    CURRENT_STARS("Current Stars", "Today's big names", "current"),
    WORLD_LEGENDS("World Football Legends", "Greats from every continent", "world_legend"),
    ENGLAND("England Players", "Three Lions past and present", "england"),
    DIFFICULT("Difficult Players", "Cult heroes and deep cuts", "difficult");

    fun includes(player: Player): Boolean = tag == null || tag in player.tags
}

object PlayerRepository {
    private const val ASSET = "players.json"

    fun load(context: Context): List<Player> {
        val text = context.assets.open(ASSET).bufferedReader(Charsets.UTF_8).use { it.readText() }
        val array = JSONObject(text).getJSONArray("players")
        return List(array.length()) { i ->
            val obj = array.getJSONObject(i)
            val tags = obj.optJSONArray("tags")
            Player(
                name = obj.getString("name"),
                tags = if (tags == null) emptySet() else List(tags.length()) { tags.getString(it) }.toSet()
            )
        }
    }
}
