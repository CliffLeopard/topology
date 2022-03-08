package com.cliff.topology

import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * author:CliffLeopard
 * date:2022/3/7
 * time:10:25
 * email:precipiceleopard@gmail.com
 * link:
 */
object ThreadPool {
    private val cpuNum = Runtime.getRuntime().availableProcessors()
    val threadPool = ThreadPoolExecutor(
        cpuNum + 1, cpuNum + 1,
        60L, TimeUnit.SECONDS,
        LinkedBlockingDeque()
    )
    init {
        threadPool.allowCoreThreadTimeOut(true)
    }
}