package com.androidbolts.minitiktok.features.feed.data.fake


import com.androidbolts.minitiktok.core.common.FeedDto
import com.androidbolts.minitiktok.core.common.UserDto
import com.androidbolts.minitiktok.core.common.enums.FeedType

object FakeFeedDataSource {

    private val fakeAuthors = listOf(
        UserDto(id = "u_001", username = "dance.vibes",       displayName = "Dance Vibes",       email = "u001@gmail.com", avatarUrl = "https://i.pravatar.cc/150?img=1"),
        UserDto(id = "u_002", username = "foodie.adventures", displayName = "Foodie Adventures", email = "u002@gmail.com" , avatarUrl = "https://i.pravatar.cc/150?img=2"),
        UserDto(id = "u_003", username = "travel.with.me",    displayName = "Travel With Me",    email = "u003@gmail.com", avatarUrl = "https://i.pravatar.cc/150?img=3"),
        UserDto(id = "u_004", username = "comedy.central.pk", displayName = "Comedy Central PK", email = "u004@gmail.com", avatarUrl = "https://i.pravatar.cc/150?img=4"),
        UserDto(id = "u_005", username = "fitness.mode",      displayName = "Fitness Mode",      email = "u005@gmail.com", avatarUrl = "https://i.pravatar.cc/150?img=5"),
        UserDto(id = "u_006", username = "tech.simplified",   displayName = "Tech Simplified",   email = "u006@gmail.com", avatarUrl = "https://i.pravatar.cc/150?img=6"),
        UserDto(id = "u_007", username = "aesthetic.life",    displayName = "Aesthetic Life",    email = "u007@gmail.com" , avatarUrl = "https://i.pravatar.cc/150?img=7"),
        UserDto(id = "u_008", username = "petlover99",        displayName = "Pet Lover 99",      email = "u008@gmail.com" , avatarUrl = "https://i.pravatar.cc/150?img=8")
    )

    private val fakeVideoUrls = listOf(
        "https://www.pexels.com/download/video/33192024/",
        "https://www.pexels.com/download/video/6634640/",
        "https://www.pexels.com/download/video/36780079/",
        "https://www.pexels.com/download/video/31552890/",
        "https://www.pexels.com/download/video/36623622/",
        "https://www.pexels.com/download/video/35813844/",
        "https://www.pexels.com/download/video/36668858/",
        "https://www.pexels.com/download/video/35008768/"
    )

    private val fakeTitles = listOf(
        "Morning routine 🌅",
        "Wait for the drop 🔥",
        "Recipe you need in your life 🍜",
        "Hidden gem I found 💎",
        "POV: Monday motivation 💪",
        "This trick changed everything ✨",
        "You won't believe this 😱",
        "Day in my life 🎬",
        "3 things I wish I knew earlier 📖",
        "Satisfying video of the day 😌"
    )

    private val fakeCaptions = listOf(
        "Follow for more daily content! #fyp #viral",
        "Tag someone who needs to see this 👇 #trending",
        "Part 2 is coming 👀 #foryou #explore",
        "Drop a ❤️ if you agree #relatable #fyp",
        "Try this at home! #lifehack #tips",
        "Can't stop watching this 😍 #satisfying #fyp",
        "This is how I start every morning ☀️ #routine",
        "Nobody talks about this enough 🤫 #facts #fyp",
        "Comment your city 🌍 #travel #wanderlust",
        "Stitch this if you agree 👀 #foryoupage"
    )

    private val fakeSounds = listOf(
        "original sound - dance.vibes",
        "Flowers - Miley Cyrus",
        "Calm Down - Rema & Selena Gomez",
        "As It Was - Harry Styles",
        "Shakira: Bzrp Music Sessions Vol. 53",
        "Cruel Summer - Taylor Swift",
        "Cupid (Twin Ver.) - FIFTY FIFTY",
        "original sound - aesthetic.life",
        "Die For You - The Weeknd",
        "Unholy - Sam Smith"
    )

    private val fakeLikes    = listOf("8.4M", "320K", "1.5M", "54K",  "2.2M", "780K", "430K", "9.1M", "67K",  "3.3M")
    private val fakeComments = listOf("42K",  "8.1K", "73K",  "1.2K", "98K",  "15.4K","6.7K", "180K", "3.3K", "61K")
    private val fakeShares   = listOf("18K",  "2.3K", "29K",  "550",  "41K",  "7.2K", "3.1K", "85K",  "1.4K", "24K")

    private val fakeDates = listOf(
        "2025-01-05", "2025-02-14", "2025-03-22", "2025-04-10",
        "2025-05-01", "2025-06-18", "2025-07-04", "2025-08-30",
        "2025-09-12", "2025-10-25"
    )

    // Fake API list

    fun getForYouFeed(page: Int): List<FeedDto> = buildFeed(page, FeedType.FOR_YOU)

    fun getFollowingFeed(page: Int): List<FeedDto> = buildFeed(page, FeedType.FOLLOWING)

    private fun buildFeed(page: Int, type: FeedType): List<FeedDto> {
        val offset = (page - 1) * 10
        return (0 until 10).map { i ->
            val index = offset + i
            FeedDto(
                id = "feed_${type.name.lowercase()}_$index",
                title = fakeTitles[index % fakeTitles.size],
                author = fakeAuthors[index % fakeAuthors.size],
                mediaUrl = fakeVideoUrls[index % fakeVideoUrls.size],
                mediaType = "video/mp4",
                isVerified = index % 3 == 0,
                caption = fakeCaptions[index % fakeCaptions.size],
                soundName = fakeSounds[index % fakeSounds.size],
                likeCount = fakeLikes[index % fakeLikes.size],
                commentCount = fakeComments[index % fakeComments.size],
                shareCount = fakeShares[index % fakeShares.size],
                avatarUrl = "https://i.pravatar.cc/150?img=${(index % 70) + 1}",
                soundAvatarUrl = "https://picsum.photos/seed/sound$index/100/100",
                createdAt = fakeDates[index % fakeDates.size],
                updatedAt = fakeDates[index % fakeDates.size]
            )
        }
    }
}