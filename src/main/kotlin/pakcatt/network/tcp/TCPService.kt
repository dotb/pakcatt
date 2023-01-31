package pakcatt.network.tcp

import org.springframework.core.task.TaskExecutor
import org.springframework.stereotype.Service

@Service
class TCPService(tcpServerListener: TCPServerListener,
                 taskExecutor: TaskExecutor,
                 private val tcpInteractiveEnabled: Boolean) {

    init {
        if (tcpInteractiveEnabled) {
            taskExecutor.execute(tcpServerListener)
        }
    }

}