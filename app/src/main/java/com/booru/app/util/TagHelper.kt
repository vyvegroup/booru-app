package com.booru.app.util

/**
 * Tag synchronization utility.
 *
 * App format: "Pokemon 1girl~2girls~3girls"
 *   - Space-separated tags are AND tags
 *   - Tilde (~) separated within a group are OR tags
 *
 * Rule34.xxx format: "Pokemon (1girl ~ 2girls ~ 3girls)"
 *   - OR groups wrapped in parentheses with spaces around ~
 *
 * Gelbooru format: "Pokemon {1girl ~ 2girls ~ 3girls}"
 *   - OR groups wrapped in curly braces with spaces around ~
 */
object TagHelper {

    /**
     * Represents a parsed tag query from the app's input format.
     */
    data class ParsedTags(
        val andTags: List<String>,      // "Pokemon" → ["pokemon"]
        val orGroups: List<List<String>> // "1girl~2girls" → [["1girl", "2girls"]]
    )

    /**
     * Parse app-format tags string into structured AND/OR groups.
     *
     * Input: "Pokemon 1girl~2girls~3girls blue_hair"
     * Output: ParsedTags(
     *   andTags = ["pokemon", "blue_hair"],
     *   orGroups = [["1girl", "2girls", "3girls"]]
     * )
     */
    fun parseAppTags(input: String): ParsedTags {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return ParsedTags(emptyList(), emptyList())

        val tokens = trimmed.split(Regex("\\s+"))
        val andTags = mutableListOf<String>()
        val orGroups = mutableListOf<List<String>>()

        for (token in tokens) {
            if (token.contains("~")) {
                val orParts = token.split("~").map { it.trim().lowercase() }.filter { it.isNotBlank() }
                if (orParts.size >= 2) {
                    orGroups.add(orParts)
                } else if (orParts.size == 1) {
                    andTags.add(orParts[0])
                }
            } else if (token.isNotBlank()) {
                andTags.add(token.trim().lowercase())
            }
        }

        return ParsedTags(andTags, orGroups)
    }

    /**
     * Convert app-format tags to Rule34.xxx format.
     *
     * App: "Pokemon 1girl~2girls~3girls"
     * Rule34: "pokemon (1girl ~ 2girls ~ 3girls)"
     */
    fun toRule34Tags(appTags: String): String {
        val parsed = parseAppTags(appTags)
        val parts = mutableListOf<String>()

        parts.addAll(parsed.andTags)

        for (orGroup in parsed.orGroups) {
            parts.add("(${orGroup.joinToString(" ~ ")})")
        }

        return parts.joinToString(" ")
    }

    /**
     * Convert app-format tags to Gelbooru format.
     *
     * App: "Pokemon 1girl~2girls~3girls"
     * Gelbooru: "pokemon {1girl ~ 2girls ~ 3girls}"
     */
    fun toGelbooruTags(appTags: String): String {
        val parsed = parseAppTags(appTags)
        val parts = mutableListOf<String>()

        parts.addAll(parsed.andTags)

        for (orGroup in parsed.orGroups) {
            parts.add("{${orGroup.joinToString(" ~ ")}}")
        }

        return parts.joinToString(" ")
    }

    /**
     * Convert tags based on the active source.
     * @param appTags App-format tags string
     * @param source "gelbooru" or "rule34"
     * @return Source-specific formatted tags
     */
    fun convertForSource(appTags: String, source: String): String {
        return when (source.lowercase()) {
            "rule34" -> toRule34Tags(appTags)
            "gelbooru" -> toGelbooruTags(appTags)
            else -> appTags
        }
    }

    /**
     * Extract the top N most relevant tags from a space-separated tag string.
     * Used for FYP recommendations.
     *
     * Priority: character tags, then copyright, then general.
     * Simple heuristic: take the first N tags from the list.
     */
    fun extractTopTags(tags: String, count: Int = 3): List<String> {
        if (tags.isBlank()) return emptyList()
        return tags.split(" ")
            .filter { it.isNotBlank() && it.length > 2 }
            .take(count)
    }

    /**
     * Generate a search query from tags, excluding certain tags (e.g., current post's ID).
     */
    fun buildSearchQuery(topTags: List<String>): String {
        return topTags.joinToString(" ")
    }
}
