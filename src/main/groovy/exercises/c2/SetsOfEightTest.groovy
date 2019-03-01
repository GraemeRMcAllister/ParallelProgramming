package exercises.c2

import groovyJCSP.*
import jcsp.lang.*

class SetsOfEightTest extends GroovyTestCase {
    void testMessage() {
        One2OneChannel connect1 = Channel.createOne2One()
        One2OneChannel connect2 = Channel.createOne2One()
        One2OneChannel toTest = Channel.createOne2One()
        def generateSetsOfThree = new GenerateSetsOfThree( outChannel: connect1.out() )
        def listToStream = new ListToStream( inChannel: connect1.in(), outChannel: connect2.out() )
        def setsOfEight = new CreateSetsOfEight( inChannel: connect2.in(), outChannel: toTest.out())
        def processList = [generateSetsOfThree, listToStream, setsOfEight]

        new PAR(processList).run()

        def expected = [[1, 2 , 3, 4, 5, 6, 7, 8], [9, 10, 11, 12, 13, 14, 15, 16], [17, 18, 19, 20, 21, 22, 23, 24]]
        def actual = setsOfEight.allLists
        assertTrue(actual == expected)
    }
}
