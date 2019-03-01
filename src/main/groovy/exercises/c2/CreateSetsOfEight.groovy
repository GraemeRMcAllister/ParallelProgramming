package exercises.c2

import jcsp.lang.*

class CreateSetsOfEight implements CSProcess{
	
	def ChannelInput inChannel
	def ChannelOutput outChannel
	def outList = []
	def allLists = []
	void run(){
		def v = inChannel.read()
		while (v != -1){
			outList = []
			for ( i in 0 .. 7 ) {
				// put v into outList and read next input
				outList.add(v)
				v = inChannel.read()
			}
			println " Eight Object is ${outList}"
			allLists.add(outList)
		}
		println "Finished"
	}
}