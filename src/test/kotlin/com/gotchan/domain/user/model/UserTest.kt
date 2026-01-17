package com.gotchan.domain.user.model

import com.gotchan.fixture.UserFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal

@DisplayName("User 엔티티")
class UserTest {

    @Nested
    @DisplayName("신뢰도 점수")
    inner class TrustScoreTest {

        @Test
        fun `초기 신뢰도는 36점5이다`() {
            // Given & When
            val user = UserFixture.createUser()

            // Then
            assertThat(user.trustScore).isEqualTo(BigDecimal("36.5"))
        }

        @Test
        fun `신뢰도를 증가시킬 수 있다`() {
            // Given
            val user = UserFixture.createUser()

            // When
            user.increaseTrustScore(BigDecimal("1.0"))

            // Then
            assertThat(user.trustScore).isEqualTo(BigDecimal("37.5"))
        }

        @Test
        fun `신뢰도를 감소시킬 수 있다`() {
            // Given
            val user = UserFixture.createUser()

            // When
            user.decreaseTrustScore(BigDecimal("1.0"))

            // Then
            assertThat(user.trustScore).isEqualTo(BigDecimal("35.5"))
        }

        @Test
        fun `신뢰도는 0 미만으로 내려가지 않는다`() {
            // Given
            val user = UserFixture.createUser()

            // When
            user.decreaseTrustScore(BigDecimal("100.0"))

            // Then
            assertThat(user.trustScore).isEqualTo(BigDecimal.ZERO)
        }
    }

    @Nested
    @DisplayName("정보 수정")
    inner class UpdateTest {

        @Test
        fun `닉네임을 변경할 수 있다`() {
            // Given
            val user = UserFixture.createUser(nickname = "oldNickname")

            // When
            user.updateNickname("newNickname")

            // Then
            assertThat(user.nickname).isEqualTo("newNickname")
        }

        @Test
        fun `배송지 주소를 변경할 수 있다`() {
            // Given
            val user = UserFixture.createUser()

            // When
            user.updateAddress("encryptedAddress123")

            // Then
            assertThat(user.addressHash).isEqualTo("encryptedAddress123")
        }
    }
}
