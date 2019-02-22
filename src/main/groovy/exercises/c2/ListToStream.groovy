package exercises.c2

import jcsp.lang.*

class ListToStream implements CSProcess{
	
	def ChannelInput inChannel
	def ChannelOutput outChannel
	
	void run (){
		def inList = inChannel.read()
		while (inList[0] != -1) {
			// hint: output	list elements as single integers
			for ( i in inList){
				outChannel.write(i)
			}
			inList = inChannel.read()
		}
		outChannel.write(-1)
	}
}