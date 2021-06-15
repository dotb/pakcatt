package pakcatt.network.packet.protocol.aprs

import junit.framework.TestCase

class aprsServiceTest : TestCase() {


    /*
    Example location beacon from a Yaesu FT3D
    Received bytes:	 00 88 90 68 6a ae 6e 60 ac 96 66 98 92 a8 ee ae 92 88 8a 62 40 62 ae 92 88 8a 64 40 63 03 f0 60 48 54 7c 6c 21 3a 5b 2f 60 22 34 67 7d 31 34 36 2e 34 35 30 4d 48 7a 20 2d 20 42 72 61 64 20 2d 20 76 6b 33 6c 69 74 2e 63 6f 6d 5f 30 0d
    Decoded hex:	 00 88 90 68 6a ae 6e 60 ac 96 66 98 92 a8 ee ae 92 88 8a 62 40 62 ae 92 88 8a 64 40 63 03 f0 60 48 54 7c 6c 21 3a 5b 2f 60 22 34 67 7d 31 34 36 2e 34 35 30 4d 48 7a 20 2d 20 42 72 61 64 20 2d 20 76 6b 33 6c 69 74 2e 63 6f 6d 5f 30 0d
    Received frame:	 Receive Seq: 5 Send Seq: 7 Delivery Attempt: 0 protocolID: 92 From: VK3LIT-7 to: DH45W7-0 controlType: INFORMATION_8 Payload: ��b@b����d@c�`HT|l!:[/`"4g}146.450MHz - Brad - vk3lit.com_0
     */

    /*
    Example message from VK3LIT-7 to VK2VRO-7
    # First transmission
    Received bytes:	 00 82 a0 b2 60 66 88 60 ac 96 66 98 92 a8 ee ae 92 88 8a 62 40 62 ae 92 88 8a 64 40 63 03 f0 3a 56 4b 32 56 52 4f 2d 37 20 3a 68 65 6c 6c 6f 7b 34 37 0d
    Decoded hex:	 00 82 a0 b2 60 66 88 60 ac 96 66 98 92 a8 ee ae 92 88 8a 62 40 62 ae 92 88 8a 64 40 63 03 f0 3a 56 4b 32 56 52 4f 2d 37 20 3a 68 65 6c 6c 6f 7b 34 37 0d
    Received frame:	 Receive Seq: 5 Send Seq: 7 Delivery Attempt: 0 protocolID: 92 From: VK3LIT-7 to: APY03D-0 controlType: INFORMATION_8 Payload: ��b@b����d@c�:VK2VRO-7 :hello{47

    # A later transmission (about the 3rd)
    Received bytes:	 00 82 a0 b2 60 66 88 60 ac 96 66 98 92 a8 ee ae 92 88 8a 62 40 62 ae 92 88 8a 64 40 63 03 f0 3a 56 4b 32 56 52 4f 2d 37 20 3a 68 65 6c 6c 6f 7b 34 37 0d
    Decoded hex:	 00 82 a0 b2 60 66 88 60 ac 96 66 98 92 a8 ee ae 92 88 8a 62 40 62 ae 92 88 8a 64 40 63 03 f0 3a 56 4b 32 56 52 4f 2d 37 20 3a 68 65 6c 6c 6f 7b 34 37 0d
    Received frame:	 Receive Seq: 5 Send Seq: 7 Delivery Attempt: 0 protocolID: 92 From: VK3LIT-7 to: APY03D-0 controlType: INFORMATION_8 Payload: ��b@b����d@c�:VK2VRO-7 :hello{47
     */

    fun testHandleFrame() {


    }
}