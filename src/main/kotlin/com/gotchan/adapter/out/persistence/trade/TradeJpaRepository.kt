package com.gotchan.adapter.out.persistence.trade

import com.gotchan.domain.trade.model.Trade
import org.springframework.data.jpa.repository.JpaRepository

interface TradeJpaRepository : JpaRepository<Trade, Long>
