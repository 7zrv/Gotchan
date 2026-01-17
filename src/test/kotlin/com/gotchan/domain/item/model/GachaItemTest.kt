package com.gotchan.domain.item.model

import com.gotchan.common.exception.InvalidStateException
import com.gotchan.fixture.ItemFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@DisplayName("GachaItem 엔티티")
class GachaItemTest {

    @Nested
    @DisplayName("거래 시작")
    inner class StartTradingTest {

        @Test
        fun `AVAILABLE 상태의 아이템은 거래를 시작할 수 있다`() {
            // Given
            val item = ItemFixture.createHaveItem()

            // When
            item.startTrading()

            // Then
            assertThat(item.status).isEqualTo(ItemStatus.TRADING)
        }

        @Test
        fun `TRADING 상태의 아이템은 거래를 시작할 수 없다`() {
            // Given
            val item = ItemFixture.createTradingItem()

            // When & Then
            assertThrows<InvalidStateException> {
                item.startTrading()
            }
        }

        @Test
        fun `COMPLETED 상태의 아이템은 거래를 시작할 수 없다`() {
            // Given
            val item = ItemFixture.createCompletedItem()

            // When & Then
            assertThrows<InvalidStateException> {
                item.startTrading()
            }
        }
    }

    @Nested
    @DisplayName("거래 취소")
    inner class CancelTradingTest {

        @Test
        fun `TRADING 상태의 아이템은 거래를 취소할 수 있다`() {
            // Given
            val item = ItemFixture.createTradingItem()

            // When
            item.cancelTrading()

            // Then
            assertThat(item.status).isEqualTo(ItemStatus.AVAILABLE)
        }

        @Test
        fun `AVAILABLE 상태의 아이템은 거래를 취소할 수 없다`() {
            // Given
            val item = ItemFixture.createHaveItem()

            // When & Then
            assertThrows<InvalidStateException> {
                item.cancelTrading()
            }
        }
    }

    @Nested
    @DisplayName("거래 완료")
    inner class CompleteTradingTest {

        @Test
        fun `TRADING 상태의 아이템은 거래를 완료할 수 있다`() {
            // Given
            val item = ItemFixture.createTradingItem()

            // When
            item.completeTrading()

            // Then
            assertThat(item.status).isEqualTo(ItemStatus.COMPLETED)
        }

        @Test
        fun `AVAILABLE 상태의 아이템은 거래를 완료할 수 없다`() {
            // Given
            val item = ItemFixture.createHaveItem()

            // When & Then
            assertThrows<InvalidStateException> {
                item.completeTrading()
            }
        }
    }

    @Nested
    @DisplayName("정보 수정")
    inner class UpdateInfoTest {

        @Test
        fun `AVAILABLE 상태의 아이템 정보를 수정할 수 있다`() {
            // Given
            val item = ItemFixture.createHaveItem()

            // When
            item.updateInfo(
                seriesName = "나루토",
                itemName = "사스케",
                imageUrl = "https://example.com/image.jpg"
            )

            // Then
            assertThat(item.seriesName).isEqualTo("나루토")
            assertThat(item.itemName).isEqualTo("사스케")
            assertThat(item.imageUrl).isEqualTo("https://example.com/image.jpg")
        }

        @Test
        fun `TRADING 상태의 아이템 정보는 수정할 수 없다`() {
            // Given
            val item = ItemFixture.createTradingItem()

            // When & Then
            assertThrows<InvalidStateException> {
                item.updateInfo("나루토", "사스케", null)
            }
        }
    }

    @Nested
    @DisplayName("상태 확인")
    inner class StatusCheckTest {

        @Test
        fun `AVAILABLE 상태의 아이템은 isAvailable이 true이다`() {
            // Given
            val item = ItemFixture.createHaveItem()

            // When & Then
            assertThat(item.isAvailable()).isTrue()
        }

        @Test
        fun `TRADING 상태의 아이템은 isAvailable이 false이다`() {
            // Given
            val item = ItemFixture.createTradingItem()

            // When & Then
            assertThat(item.isAvailable()).isFalse()
        }
    }
}
