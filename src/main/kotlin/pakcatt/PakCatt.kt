package pakcatt

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import pakcatt.tnc.TNCSerial
import pakcatt.tnc.kiss.KissFrame
import pakcatt.tnc.kiss.KissHandler

@SpringBootApplication
@EnableScheduling
@EnableAsync
open class PakCatt

fun main(args: Array<String>) {

    /*
    2021-05-25 21:57:32.336 	DEBUG: Received boundary of KISS frame after 124 bytes
    2021-05-25 21:57:32.336 	DEBUG: Received AX.25 frame:  0 -122 -94 64 64 64 64 -32 -84 -106 100 -84 -92 -98 97 3 -16 34 84 104 105 115 32 105 115 32 97 32 110 101 119 44 32 101 120 112 101 114 105 109 101 110 116 97 108 32 112 97 99 107 101 116 32 115 121 115 116 101 109 46 32 80 108 101 97 115 101 32 100 111 32 100 114 111 112 32 109 101 32 97 32 109 101 115 115 97 103 101 32 97 116 32 116 104 101 32 66 66 83 32 97 116 32 86 75 51 76 73 84 32 40 49 52 52 46 56 55 53 77 72 122 41 34

    2021-05-25 21:57:39.784 	DEBUG: Received boundary of KISS frame after 16 bytes
    2021-05-25 21:57:39.784 	DEBUG: Received AX.25 frame:  0 -84 -106 102 -104 -110 -88 -30 -84 -106 100 -84 -92 -98 97
    */

    // 0 -84 -106 102 -104 -110 -88 -30 -84 -106 100 -84 -92 -98 97
//    val frame = ByteArray(15)
/*
    frame[0] = Integer(0).toByte()
    frame[1] = Integer(172).toByte()
    frame[2] = Integer(172).toByte()
    frame[3] = Integer(172).toByte()
    frame[4] = Integer(172).toByte()
    frame[5] = Integer(172).toByte()
    frame[6] = Integer(172).toByte()
    frame[7] = Integer(97).toByte()
    frame[8] = Integer(172).toByte()
    frame[9] = Integer(172).toByte()
    frame[10] = Integer(172).toByte()
    frame[11] = Integer(172).toByte()
    frame[12] = Integer(172).toByte()
    frame[13] = Integer(172).toByte()
    frame[14] = Integer(97).toByte()

*/
/*
    frame[0] = 0
    frame[1] = -84
    frame[2] = -106
    frame[3] = 102
    frame[4] = -104
    frame[5] = -110
    frame[6] = -88
    frame[7] = -30
    frame[8] = -84
    frame[9] = -106
    frame[10] = 100
    frame[11] = -84
    frame[12] = -92
    frame[13] = -98
    frame[14] = 97


    val kissHandler = KissHandler(TNCSerial("/tmp/fake", 9600))

    val kissFrame = kissHandler.createKissFrame(frame)

    println("*********")
    println("From ${kissFrame.sourceCallsign()} to: ${kissFrame.destCallsign()}")
    println("*********")
*/

    runApplication<PakCatt>(*args)
}
