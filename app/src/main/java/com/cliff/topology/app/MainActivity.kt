package com.cliff.topology.app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cliff.topology.Topology
import com.cliff.topology.TopologyTask
import com.cliff.topology.app.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var taskText: TextView
    private var logText: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        taskText = binding.taskText

        val taskListener = object : Topology.TopologyTaskRunListener {
            override fun beforeRun(taskName: String, mainThread: Boolean) {
                if (mainThread) {
                    logText += "$taskName beginRun: mainThread:$mainThread \n"
                    taskText.text = logText
                } else {
                    Handler(Looper.getMainLooper()).post {
                        logText += "$taskName beginRun: mainThread:$mainThread \n"
                        taskText.text = logText
                    }
                }
            }

            override fun afterRun(taskName: String, mainThread: Boolean, executeTime: Long) {
                if (mainThread) {
                    logText += "$taskName afterRun: mainThread:$mainThread executeTime:${executeTime}ms\n"
                    taskText.text = logText
                } else {
                    Handler(Looper.getMainLooper()).post {
                        logText += "$taskName afterRun: mainThread:$mainThread executeTime:${executeTime}ms\n"
                        taskText.text = logText
                    }
                }
            }
        }

        val topologyListener = object : Topology.TopologyRunListener {
            override fun topologyStart() {
                logText += "TopologyStart:\n"
                taskText.text = logText
            }

            override fun topologyFinish(executeTime: Long) {
                logText += "TopologyFinish: executeTime:${executeTime}ms\n"
                taskText.text = logText
            }
        }

        binding.clickRun.setOnClickListener {
            val topology = Topology.Builder("cliff-topology")
                .addTask(prepareTask())
                .setTaskRunListener(taskListener)
                .setTopologyRunListener(topologyListener)
                .build()
            topology.run()
        }
    }

    /**
     *    1 --> 2 ---> 4
     *      --> 3 ---> 5 ----> 6 -----> 8
     *      --> 7
     *               (5,7) --->9
     *
     */
    private fun prepareTask(): TopologyTask {
        val task1 = TopologyTask("Task1", true) {
            println("Task1 -- thread:${Thread.currentThread().name}")
        }

        val task2 = TopologyTask("Task2", false) {
            println("Task2 -- thread:${Thread.currentThread().name}")
        }.after(task1)

        val task3 = TopologyTask("Task3", false) {
            println("Task3 -- thread:${Thread.currentThread().name}  begin")
            Thread.sleep(2000)
            println("Task3 -- thread:${Thread.currentThread().name}  end")
        }.after(task1)

        TopologyTask("Task4", false) {
            println("Task4 -- thread:${Thread.currentThread().name}")
        }.after(task2)

        val task5 = TopologyTask("Task5", false) {
            println("Task5 -- thread:${Thread.currentThread().name}")
        }.after(task3)

        val task6 = TopologyTask("Task6", false) {
            println("Task6 -- thread:${Thread.currentThread().name} begin")
            Thread.sleep(3000)
            println("Task6 -- thread:${Thread.currentThread().name} end")
        }.after(task5)

        val task7 = TopologyTask("Task7", false) {
            println("Task7 -- thread:${Thread.currentThread().name}")
        }.after(task1)

        TopologyTask("Task8", false) {
            println("Task8 -- thread:${Thread.currentThread().name}")
        }.after(task6)

        TopologyTask("Task9", false) {
            println("Task9 -- thread:${Thread.currentThread().name}")
        }.after(task5).after(task7)

        return task1
    }
}