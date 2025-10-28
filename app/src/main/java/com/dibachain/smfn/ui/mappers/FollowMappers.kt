package com.dibachain.smfn.ui.mappers

import com.dibachain.smfn.data.remote.FollowUserRaw
import com.dibachain.smfn.activity.profile.FollowUserUi
import com.dibachain.smfn.activity.profile.Relation
import com.dibachain.smfn.core.Public // اگر بیس‌ URL اینجاست

private fun buildImageUrl(path: String?): String {
    if (path.isNullOrBlank()) return ""
    return if (path.startsWith("http", ignoreCase = true)) path
    else Public.BASE_URL_IMAGE.trimEnd('/') + "/" + path.trimStart('/')
}

fun FollowUserRaw.toUi(): FollowUserUi {
    val uid = id ?: java.util.UUID.randomUUID().toString()
    val name = username ?: uid.take(6)
    val avatar = buildImageUrl(link)
    // چون API رابطه نمی‌دهد، حالت پیش‌فرض:
    val relation = Relation.NotFollowing
    return FollowUserUi(
        id = uid,
        name = name,
        avatar = avatar,
        relation = relation
    )
}
