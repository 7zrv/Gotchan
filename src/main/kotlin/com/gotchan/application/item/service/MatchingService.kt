package com.gotchan.application.item.service

import com.gotchan.application.item.dto.ItemResponse
import com.gotchan.application.item.dto.MatchResult
import com.gotchan.application.item.port.MatchingUseCase
import com.gotchan.common.exception.EntityNotFoundException
import com.gotchan.domain.item.model.ItemType
import com.gotchan.domain.item.port.ItemRepository
import com.gotchan.domain.user.port.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class MatchingService(
    private val itemRepository: ItemRepository,
    private val userRepository: UserRepository
) : MatchingUseCase {

    /**
     * 스마트 매칭 알고리즘
     *
     * 내가 가진 것(HAVE)을 원하는(WISH) 상대방 중에서,
     * 내가 원하는 것(WISH)을 가지고 있는(HAVE) 상대방을 찾는다.
     * (동일 시리즈 내에서만 매칭)
     */
    override fun findMatches(userId: UUID): List<MatchResult> {
        userRepository.findById(userId)
            ?: throw EntityNotFoundException("User", userId)

        // 내 HAVE 아이템 목록
        val myHaveItems = itemRepository.findByOwnerIdAndType(userId, ItemType.HAVE)
        if (myHaveItems.isEmpty()) {
            return emptyList()
        }

        // 내 WISH 아이템 목록
        val myWishItems = itemRepository.findByOwnerIdAndType(userId, ItemType.WISH)
        if (myWishItems.isEmpty()) {
            return emptyList()
        }

        val matchResults = mutableListOf<MatchResult>()

        // 각 HAVE 아이템에 대해 매칭 검색
        for (myHave in myHaveItems) {
            // 내 HAVE를 WISH하는 상대방 목록
            val partnersWantingMyItem = itemRepository.findAvailableByTypeAndSeriesNameAndItemName(
                ItemType.WISH,
                myHave.seriesName,
                myHave.itemName
            ).filter { it.owner.id != userId }

            if (partnersWantingMyItem.isEmpty()) continue

            // 각 잠재적 파트너에 대해
            for (partnerWish in partnersWantingMyItem) {
                val partnerId = partnerWish.owner.id

                // 내 WISH 중에서 이 파트너가 HAVE로 가진 것이 있는지 확인
                for (myWish in myWishItems.filter { it.seriesName == myHave.seriesName }) {
                    val partnerHaveItems = itemRepository.findAvailableByTypeAndSeriesNameAndItemName(
                        ItemType.HAVE,
                        myWish.seriesName,
                        myWish.itemName
                    ).filter { it.owner.id == partnerId }

                    for (partnerHave in partnerHaveItems) {
                        matchResults.add(
                            MatchResult(
                                partnerId = partnerId,
                                partnerNickname = partnerHave.owner.nickname,
                                partnerTrustScore = partnerHave.owner.trustScore,
                                myHaveItem = ItemResponse.from(myHave),
                                partnerWantItem = ItemResponse.from(partnerWish),
                                partnerHaveItem = ItemResponse.from(partnerHave),
                                myWantItem = ItemResponse.from(myWish)
                            )
                        )
                    }
                }
            }
        }

        // 중복 제거 (같은 파트너와의 동일한 교환 조합)
        return matchResults.distinctBy {
            Triple(it.partnerId, it.myHaveItem.id, it.partnerHaveItem.id)
        }
    }
}
