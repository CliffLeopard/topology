package com.cliff.topology

import android.os.Handler
import android.os.Looper
import android.os.Message
import java.util.concurrent.atomic.AtomicInteger

/**
 * author:CliffLeopard
 * date:2022/2/28
 * time:16:25
 * email:precipiceleopard@gmail.com
 * link:
 * 一个处于拓补排序任务图中的任务
 */
class TopologyTask(
    val name: String,
    private val mainThread: Boolean = true,
    private val runnable: Runnable,
) : Runnable {
    private val inDegrees = mutableSetOf<TopologyTask>()
    val outDegrees = mutableSetOf<TopologyTask>()
    private var state = AtomicInteger(0)  // 0:初始化完成；1:运行中；2:运行结束
    var listener: Topology.TopologyTaskRunListener? = null

    /**
     * 链接入度任务
     */
    fun after(inDegreeTask: TopologyTask): TopologyTask {
        if (isCircle(inDegreeTask)) {
            throw CircleException("$name can't after ${inDegreeTask.name}, make circle Task")
        }
        this.inDegrees.add(inDegreeTask)
        inDegreeTask.before(this)
        return this
    }

    /**
     * 链接出度任务
     */
    private fun before(outDegreeTask: TopologyTask): TopologyTask {
        outDegrees.add(outDegreeTask)
        return this
    }

    /**
     * 入度任务已执行，删除
     */
    @Synchronized
    private fun removeInDegree(inDegreeTask: TopologyTask): TopologyTask {
        inDegrees.remove(inDegreeTask)
        return this
    }

    /**
     * 执行当前任务
     */
    override fun run() {
        if (state.get() > 0) return
        state.addAndGet(1)
        listener?.beforeRun(name,mainThread)
        val startTime = System.currentTimeMillis();
        runnable.run()
        state.addAndGet(1)
        listener?.afterRun(name, mainThread,System.currentTimeMillis() - startTime)
        schedule()
    }

    /**
     * 任务调度
     */
    private fun schedule() {
        outDegrees.forEach { it.removeInDegree(this) }
        val next = outDegrees.filter { it.inDegrees.size == 0 }
        if (next.isEmpty()) return
        val sameIndex = next.indexOfFirst { it.mainThread == mainThread }
        next.forEachIndexed { index, task ->
            if (sameIndex != index) {
                if (task.mainThread)
                    sendToMainThread(task)
                else
                    ThreadPool.threadPool.execute(task)
            }
        }
        if (sameIndex > -1) next[sameIndex].run()
    }

    /**
     * 环检测
     */
    private fun isCircle(inDegreeTask: TopologyTask): Boolean {
        if (inDegreeTask == this)
            return true

        inDegreeTask.inDegrees.firstOrNull { isCircle(it) }?.let {
            return true
        }
        return false
    }

    companion object {
        private val handler = Handler(Looper.getMainLooper()) { msg ->
            (msg.obj as TopologyTask).run()
            true
        }

        fun sendToMainThread(task: Runnable) {
            val message = Message()
            message.obj = task
            handler.sendMessage(message)
        }
    }

    class CircleException(message: String) : Exception(message)
}