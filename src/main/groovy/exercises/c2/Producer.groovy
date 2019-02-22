package exercises.c2

import jcsp.lang.CSProcess
import jcsp.lang.ChannelOutput

// copyright 2012-18 Jon Kerridge
// Using Concurrency and Parallelism Effectively parts i & ii, 2014, bookboon.com

import jcsp.userIO.Ask

class Producer implements CSProcess {
  
  def ChannelOutput outChannel
  
  void run() {
    def i = 1000
    while ( i > 0 ) {
      i = Ask.Int ("next: ", -100, 100)
      outChannel.write (i)
    }
  }
}
