package com.cliff.topology

import android.os.Handler
import android.os.Looper
import kotlin.properties.Delegates

/**
 * author:CliffLeopard
 * date:2022/2/28
 * time:16:25
 * email:precipiceleopard@gmail.com
 * link:
 * 表示一个拓补排序任务图
 */
class Topology(private val name: String, private val firstTask: TopologyTask, private val finalTask: TopologyTask) {
    /**
     * 执行任务图
     */
    fun run() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            firstTask.run()
        } else {
            Handler(Looper.getMainLooper()) { firstTask.run();true }.sendEmptyMessage(0)
        }
    }

    /**
     * 链接两个完整排序任务图
     */
    fun after(topology: Topology) {
        this.firstTask.after(topology.finalTask)
    }

    /**
     * 任务图启动和结束监听
     */
    interface TopologyTaskRunListener {
        fun beforeRun(taskName: String, mainThread: Boolean)
        fun afterRun(taskName: String, mainThread: Boolean, executeTime: Long)
    }

    interface TopologyRunListener {
        fun topologyStart()
        fun topologyFinish(executeTime: Long)
    }

    class Builder(private val name: String) {
        private var taskRunListener: TopologyTaskRunListener? = null
        private var topologyRunListener: TopologyRunListener? = null
        private val headTasks = mutableListOf<TopologyTask>()
        private var startTime by Delegates.notNull<Long>()
        fun build(): Topology {
            val firstTask = TopologyTask("${name}:startPoint\$Topology") {
                startTime = System.currentTimeMillis()
                topologyRunListener?.topologyStart()
            }
            val finalTask = TopologyTask("${name}:endPoint\$Topology") {
                val endTime = System.currentTimeMillis()
                topologyRunListener?.topologyFinish(endTime - startTime)
            }
            taskRunListener?.let { listen ->
                headTasks.forEach { task -> setListeners(task, listen) }
            }
            headTasks.forEach { it.after(firstTask) }
            addEndPoint(firstTask, finalTask)
            return Topology(name, firstTask, finalTask)
        }

        fun addTask(task: TopologyTask): Builder {
            headTasks.add(task)
            return this
        }

        fun setTaskRunListener(listener: TopologyTaskRunListener): Builder {
            this.taskRunListener = listener
            return this
        }

        fun setTopologyRunListener(listener: TopologyRunListener): Builder {
            this.topologyRunListener = listener
            return this
        }

        private fun setListeners(task: TopologyTask, listener: TopologyTaskRunListener) {
            if (task.listener == null) {
                task.listener = listener
                task.outDegrees.forEach { setListeners(it, listener) }
            }
        }

        private fun addEndPoint(task: TopologyTask, finalTask: TopologyTask) {
            if (task == finalTask) return
            if (task.outDegrees.size == 0) {
                finalTask.after(task)
            } else {
                task.outDegrees.forEach { addEndPoint(it, finalTask) }
            }
        }
    }
}