package exercises.c5
     
import jcsp.lang.*
import groovyJCSP.*
import examples.c05.ScaledData
   
class Scale implements CSProcess {
  def int scaling
  def ChannelOutput outChannel
  def ChannelOutput factor
  def ChannelInput inChannel
  def ChannelInput suspend
  def ChannelInput injector
  
  void run () {
    def SECOND = 1000
    def DOUBLE_INTERVAL = 5 * SECOND
    def SUSPEND  = 0
    def INJECT   = 1
    def TIMER    = 2
    def INPUT    = 3
    
    def timer = new CSTimer()
    def scaleAlt = new ALT ( [ suspend, injector, timer, inChannel ] )
    
    def preCon = new boolean [4]
    preCon[SUSPEND] = true
    preCon[INJECT] = false
    preCon[TIMER] = true
    preCon[INPUT] = true
    def suspended = false
                                                                    
    def timeout = timer.read() + DOUBLE_INTERVAL
    timer.setAlarm ( timeout )
    
    while (true) {
      switch ( scaleAlt.priSelect(preCon) ) {
        case SUSPEND :
          //  deal with suspend input
          preCon[SUSPEND] = false
          suspended = true
          println "Suspended"
          suspend.read()
          factor.write(scaling)
          preCon[INJECT] = true
          break
        case INJECT:
          //  deal with inject input
          preCon[INJECT] = false
          preCon[SUSPEND] = true
          suspended = false
          scaling = injector.read()
          println "New scaling: $scaling"
          timeout = timer.read() + DOUBLE_INTERVAL
          timer.setAlarm( timeout )
          break
        case TIMER:
          //  deal with Timer input
          timeout = timer.read() + DOUBLE_INTERVAL
          timer.setAlarm( timeout )
          scaling = scaling * 2
          println "Normal Timer: new scaling timer is $scaling"
          break
        case INPUT:
          //   deal with Input channel
          def inValue = inChannel.read()
          def result = new ScaledData()
          if( suspended == true ) {
            result.original = inValue
            result.scaled = inValue
          }
          else {
            result.original = inValue
            result.scaled = inValue * scaling
          }
          outChannel.write( result )
          break
      } //end-switch
    } //end-while
  } //end-run
}