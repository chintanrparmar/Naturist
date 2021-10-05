package app.chintan.naturist.model

data class UserProfile(var uid: String? = null, var role: UserRole? = null)

enum class UserRole {
    ADMIN, USER
}