package com.dibachain.smfn.preview

import com.dibachain.smfn.activity.feature.product.ProductPayload

object ProductPreviewStore {
    @Volatile var lastPayload: ProductPayload? = null
}
