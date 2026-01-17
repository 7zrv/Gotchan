package com.gotchan.adapter.out.persistence.item

import com.gotchan.domain.item.model.GachaItem
import org.springframework.data.jpa.repository.JpaRepository

interface ItemJpaRepository : JpaRepository<GachaItem, Long>
