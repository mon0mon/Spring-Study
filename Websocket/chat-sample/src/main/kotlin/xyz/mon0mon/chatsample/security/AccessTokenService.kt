package xyz.mon0mon.chatsample.security

interface AccessTokenService {
    /**
     * 액세스 토큰을 갱신한다
     */
    fun refresh(accessToken: String): String

    /**
     * 액세스 토큰을 생성한다
     */
    fun create(userId: Long): String

    /**
     * 액세스 토큰으로부터 userId를 구해온다
     */
    fun getUserId(accessToken: String): Long

    /**
     * 액세스 토큰을 검증한다.
     */
    fun validate(accessToken: String)
}
