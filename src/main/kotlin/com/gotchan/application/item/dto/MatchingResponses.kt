package com.gotchan.application.item.dto

import java.math.BigDecimal
import java.util.*

data class MatchResult(
    val partnerId: UUID,
    val partnerNickname: String,
    val partnerTrustScore: BigDecimal,
    val myHaveItem: ItemResponse,
    val partnerWantItem: ItemResponse,  // 상대방의 WISH (내 HAVE와 매칭)
    val partnerHaveItem: ItemResponse,
    val myWantItem: ItemResponse        // 나의 WISH (상대방 HAVE와 매칭)
)
