package exercises.c2
 
import jcsp.userIO.*
import jcsp.lang.*


class Consumer implements CSProcess {
  
  def ChannelInput inChannel
  void run() {
    def i = inChannel.read()
    while ( i > 0 ) {
      //insert a modified println statement
      println "The multiplied value is $i"
      i = inChannel.read()
    }
    println "Finished"
  }
}

