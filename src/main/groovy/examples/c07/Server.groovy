package examples.c07

// copyright 2012-18 Jon Kerridge
// Using Concurrency and Parallelism Effectively parts i & ii, 2014, bookboon.com

import jcsp.lang.*
import groovyJCSP.*


class Server implements CSProcess{
	  
  def ChannelInput clientRequest
  def ChannelOutput clientSend  
  def ChannelOutput thisServerRequest
  def ChannelInput thisServerReceive  
  def ChannelInput otherServerRequest
  def ChannelOutput otherServerSend  
  def dataMap = [ : ]
  def serverNumber
                
  void run () {
    def CLIENT = 0
    def OTHER_REQUEST = 1
    def THIS_RECEIVE = 2
    def serverAlt = new ALT ([clientRequest, 
		                      otherServerRequest, 
							  thisServerReceive])
    while (true) {
      def index = serverAlt.select()
	  
      switch (index) {		  
        case CLIENT :
          def key = clientRequest.read()
          println "CLIENT REQUEST Sever: $serverNumber clients request $key Datamap: $dataMap"
          if ( dataMap.containsKey(key) ) {
            clientSend.write(dataMap[key])
            println "CLIENT REQUEST Sever: $serverNumber Data map contains $key writing " + dataMap[key] + " to client " +
                    "Server $serverNumber has datamap $dataMap"
          }else {
            thisServerRequest.write(key)
            println "CLIENT REQUEST Sever: $serverNumber Data map does not contain $key writing $key to client " +
                    "Datamap: $dataMap"
          }
          //end if 
          break
        case OTHER_REQUEST :
          def key = otherServerRequest.read()
          println "OTHER_REQUEST Reading clients request $key Datamap: $dataMap"
          if ( dataMap.containsKey(key) ) {
            otherServerSend.write(dataMap[key])
            println "OTHER_REQUEST Data map contains $key writing " + dataMap[key] + " to client Server: $serverNumber " +
                    "has datamap $dataMap"
          }else {
            otherServerSend.write(-1)
            println "OTHER_REQUEST Data map does not contain $key writing -1. Datamap: $dataMap"
          }
          //end if
          break
        case THIS_RECEIVE :
          clientSend.write(thisServerReceive.read() )
          println "THIS_RECEIVE Server $serverNumber Recieveing A Value Datamap: $dataMap"
          break
      } // end switch              
    } //end while   
  } //end run
}
