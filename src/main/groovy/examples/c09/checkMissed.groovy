package examples.c09

import jcsp.lang.CSProcess
import jcsp.lang.ChannelInput
import jcsp.lang.ChannelOutput

class checkMissed implements  CSProcess{
    ChannelInput inChannel
    ChannelOutput checkedOut
    void run(){
        def eventObj = inChannel.read()
        def previousData = eventObj.data
        checkedOut.write(eventObj.copy())
        def expected = previousData + eventObj.missed + 1
        while (true){
            eventObj = inChannel.read().copy()
            expected = previousData + eventObj.missed + 1

            if(expected != eventObj.data){
                println "Expected does n ot equal previous"
            }

            println "[ Previous value: " + previousData + "] Missed: [" + eventObj.missed + "] Next expected ["+
                    expected + "]"
            previousData = expected
            checkedOut.write(eventObj)
        }
    }
}
