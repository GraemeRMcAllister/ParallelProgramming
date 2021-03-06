package exercises.c4


import jcsp.lang.*
import groovyJCSP.*

class ResetSuccessor implements CSProcess {
	  
  def ChannelOutput outChannel
  def ChannelInput  inChannel
  def ChannelInput  resetChannel
	  
  void run () {
    def guards = [ resetChannel, inChannel  ]
    def alt = new ALT ( guards )
	while (true) {
	  // deal with inputs from resetChannel and inChannel
        def index = alt.priSelect()
        if (index == 0 ) {    // resetChannel input
            def resetValue = resetChannel.read()
            outChannel.write(resetValue)
            resetChannel.read()
        }else {
            outChannel.write(inChannel.read() + 1)
        }
    }
  }
}
