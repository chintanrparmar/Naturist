package app.chintan.naturist.model

import java.util.ArrayList

data class Post(
    var id: Long = 0,
    var imageUrl: String? = null,
    var title: String? = null,
    var description: String? = null,
    var favourite: ArrayList<String>? = null,
)
