package examples.c25

import jcsp.awt.*
import groovyJCSP.*
import jcsp.lang.*
import java.awt.*
import java.awt.Color.*
import jcsp.net2.*;
import jcsp.net2.tcpip.*;
import jcsp.net2.mobile.*;
import java.awt.event.*

class PlayerManager implements CSProcess {
	DisplayList dList
	ChannelOutputList playerNames
	ChannelOutputList pairsWon
	ChannelOutput IPlabel
	ChannelInput IPfield
	ChannelOutput IPconfig
	ChannelInput withdrawButton
	ChannelInput nextButton
	ChannelOutput getValidPoint
	ChannelInput validPoint
	ChannelOutput nextPairConfig
	
	int maxPlayers = 8
	int side = 50
	int minPairs = 3
	int maxPairs = 6
	int boardSize = 6


	
	void run() {


		int gap = 5
		def offset = [gap, gap]
		int graphicsPos = (side / 2)
		def rectSize = ((side + gap) * boardSize) + gap

		def displaySize = 4 + (5 * boardSize * boardSize)
		GraphicsCommand[] display = new GraphicsCommand[displaySize]
		GraphicsCommand[] changeGraphics = new GraphicsCommand[5]
		changeGraphics[0] = new GraphicsCommand.SetColor(Color.WHITE)
		changeGraphics[1] = new GraphicsCommand.FillRect(0, 0, 0, 0)
		changeGraphics[2] = new GraphicsCommand.SetColor(Color.BLACK)
		changeGraphics[3] = new GraphicsCommand.DrawRect(0, 0, 0, 0)
		changeGraphics[4] = new GraphicsCommand.DrawString("   ", graphicsPos, graphicsPos)

		def createBoard = {
			display[0] = new GraphicsCommand.SetColor(Color.WHITE)
			display[1] = new GraphicsCommand.FillRect(0, 0, rectSize, rectSize)
			display[2] = new GraphicsCommand.SetColor(Color.BLACK)
			display[3] = new GraphicsCommand.DrawRect(0, 0, rectSize, rectSize)
			def cg = 4
			for (x in 0..(boardSize - 1)) {
				for (y in 0..(boardSize - 1)) {
					def int xPos = offset[0] + (gap * x) + (side * x)
					def int yPos = offset[1] + (gap * y) + (side * y)
					//print " $x, $y, $xPos, $yPos, $cg, "
					display[cg] = new GraphicsCommand.SetColor(Color.WHITE)
					cg = cg + 1
					display[cg] = new GraphicsCommand.FillRect(xPos, yPos, side, side)
					cg = cg + 1
					display[cg] = new GraphicsCommand.SetColor(Color.BLACK)
					cg = cg+1
					display[cg] = new GraphicsCommand.DrawRect(xPos, yPos, side, side)				
					cg = cg +1
					xPos = xPos + graphicsPos
					yPos = yPos + graphicsPos
					display[cg] = new GraphicsCommand.DrawString("   ",xPos, yPos)
					//println "$cg"		
					cg = cg +1
				}
			}			
		} // end createBoard
		
		def pairLocations = []
		def colours = [Color.MAGENTA, Color.CYAN, Color.YELLOW, Color.PINK]

		def changePairs = { x, y, colour, p ->
			def int xPos = offset[0] + (gap * x) + (side * x)
			def int yPos = offset[1] + (gap * y) + (side * y)
			changeGraphics[0] = new GraphicsCommand.SetColor(colour)
			changeGraphics[1] = new GraphicsCommand.FillRect(xPos, yPos, side, side)
			changeGraphics[2] = new GraphicsCommand.SetColor(Color.BLACK)
			changeGraphics[3] = new GraphicsCommand.DrawRect(xPos, yPos, side, side)
			xPos = xPos + graphicsPos
			yPos = yPos + graphicsPos
			if (p >= 0)
				changeGraphics[4] = new GraphicsCommand.DrawString("   " + p, xPos, yPos)
			else
				changeGraphics[4] = new GraphicsCommand.DrawString(" ??", xPos, yPos)
			dList.change(changeGraphics, 4 + (x * 5 * boardSize) + (y * 5))
		}

		def pairsMatch = { pairsMap, cp ->
			// cp is a list comprising two elements each of which is a list with the [x,y]
			// location of a square
			// returns 0 if only one square has been chosen so far
			//         1 if the two chosen squares have the same value (and colour)
			//         2 if the chosen squares have different values
			if (cp[1] == null) return 0
			else {
				if (cp[0] != cp[1]) {
					def p1Data = pairsMap.get(cp[0])
					def p2Data = pairsMap.get(cp[1])
					if (p1Data[0] == p2Data[0]) return 1 else return 2
				} else return 2
			}
		}


		createBoard()
		dList.set(display)
		IPlabel.write("What is your name?")
		def playerName = IPfield.read()
		IPconfig.write("")
		IPlabel.write("What is the IP address of the game controller?")
		def controllerIP = IPfield.read().trim()
		IPconfig.write("")
		IPlabel.write("Connecting to the GameController")

		// create Node and Net Channel Addresses

		def nodeAddr = new TCPIPNodeAddress(4000)
		Node.getInstance().init(nodeAddr)


		def toControllerAddr = new TCPIPNodeAddress ( controllerIP, 3000)
		def toController = NetChannel.any2net(toControllerAddr, 50 )
		def fromController = NetChannel.net2one()
		def fromControllerLoc = fromController.getLocation()


		def turnAlt = new ALT([validPoint, withdrawButton, fromController])
		def VALIDPOINT = 0
		def WITHDRAW = 1
		def UPDATE = 2
		
		// connect to game controller
		IPconfig.write("Now Connected - sending your name to Controller")

		def enrolPlayer = new EnrolPlayer( name: playerName,
										   toPlayerChannelLocation: fromControllerLoc)

		toController.write(enrolPlayer)
		def enrolDetails = (EnrolDetails)fromController.read()
		def myPlayerId = enrolDetails.id
		def enroled = true
		def unclaimedPairs = 0
		if (myPlayerId == -1) {
			enroled = false
			IPlabel.write("Sorry " + playerName + ", there are too many players enroled in this PAIRS game")
			IPconfig.write("  Please close the game window")
		}
		else {
			IPlabel.write("Hi " + playerName + ", you are now enroled in the PAIRS game")
			IPconfig.write(" ")	
			
			// main loop

			toController.write(new GetGameDetails(id: myPlayerId))
			println("requested game updated")
			def gameDetails = (GameDetails) fromController.read()
			def gameId = gameDetails.gameId
			def playerMap = gameDetails.playerDetails
			def pairsMap = gameDetails.pairsSpecification
			def turnID = gameDetails.turn
			myPlayerId = gameDetails.playerID

			def waitAlt = new ALT ([fromController, withdrawButton])
			def FROMCONTROLLER = 0

			def pointFull = false

			while (enroled) {
				println "BACK TO TOP"
				def chosenPairs = [null, null]
				createBoard()
				dList.change(display, 0)
				toController.write(new GetGameDetails(id: myPlayerId))
				gameDetails = (GameDetails) fromController.read()
				//GameDetails = (GameDetails)fc
				gameId = gameDetails.gameId
				playerMap = gameDetails.playerDetails
				pairsMap = gameDetails.pairsSpecification
				turnID = gameDetails.turn
				myPlayerId = gameDetails.playerID



				println("updated game")
				int gameIdDisplay = gameId + 1
				def playerIds = playerMap.keySet()
				println playerMap.keySet()
				def turnName = playerMap.get(turnID)[0]
				IPconfig.write("Playing Game Number - $gameIdDisplay - " + turnName+"'s turn")

				/*int i
				playerNames.size() { p ->
					def pData = playerMap.get(p)
					playerNames[p].write("Player " + i)
					pairsWon[p].write(" " + 0)

				} */
				for (x in 0..(playerMap.size())) {
					def pData = playerMap.get(x)
					if (pData != null) {
						playerNames[x].write(pData[0])
						pairsWon[x].write(" " + pData[1])
					}else{
						playerNames[x].write('Player ' + x)
					//	pairsWon[x].write(" ")
					}

				}
				/*playerIds.each { p ->
					def pData = playerMap.get(p)
					playerNames[p].write(pData[0])
					pairsWon[p].write(" " + pData[1])

				}*/

				// now use pairsMap to create the board
				def pairLocs = pairsMap.keySet()
				pairLocs.each {loc ->
					changePairs(loc[0], loc[1], Color.LIGHT_GRAY, -1)
				}
				def currentPair = 0
				def notMatched = true
				println("$turnID + $myPlayerId")

				while(turnID != myPlayerId) {
					//IPlabel.write("Player $turnID's turn")
					println("current turnID $turnID")
					println "Waiting for other player"
					switch ( waitAlt.select() ) {
						case FROMCONTROLLER:
							def fromC = fromController.read()
							if (fromC instanceof GameDetails) {
								println "heard that"
								gameDetails = (GameDetails) fromC
								turnID = gameDetails.turn
								println("$turnID and $myPlayerId")
								pairsMap = gameDetails.pairsSpecification
								myPlayerId = gameDetails.playerID
								notMatched = false
							} else if (fromC instanceof selectedTile) {
								changePairs(fromC.vpoint[0], fromC.vpoint[1], fromC.pairdata[1], fromC.pairdata[0])
								pairsMap = gameDetails.pairsSpecification
								notMatched = false
							}
							break
						case WITHDRAW:
							withdrawButton.read()
							toController.write(new WithdrawFromGame(id: myPlayerId))
							enroled = false
							IPlabel.write("Goodbye " + playerName + ", please close game window")
							createBoard()
							break
					}
				}
				while ((chosenPairs[1] == null) && (enroled) && (notMatched) && (turnID == myPlayerId)) {
					println "My Turn"
					//IPlabel.write("Your Turn")
					nextPairConfig.write("Your Turn")
					pairsMap = gameDetails.pairsSpecification
					if(!pointFull) {
						getValidPoint.write(new GetValidPoint(side: side,
								gap: gap,
								pairsMap: pairsMap))
						pointFull = true
					}

					switch ( turnAlt.select() ) {
						case WITHDRAW:	
							withdrawButton.read()
							toController.write(new WithdrawFromGame(id: myPlayerId))
							enroled = false
							createBoard()
							break						
						case VALIDPOINT:
                            println("valid point")
							def vPoint = ((SquareCoords)validPoint.read()).location
							pointFull = false
							chosenPairs[currentPair] = vPoint
							currentPair = currentPair + 1
							def pairData = pairsMap.get(vPoint)
							println vPoint
							toController.write(new selectedTile(vpoint:vPoint, pairdata:pairData))
							def fromC = (selectedTile)fromController.read()
							changePairs(fromC.vpoint[0], fromC.vpoint[1], fromC.pairdata[1], fromC.pairdata[0])

							def matchOutcome = pairsMatch(pairsMap, chosenPairs)
							if ( matchOutcome == 2) {
								nextPairConfig.write("Turn Over")
								println("next")
								def timer = new CSTimer()
								timer.sleep(1500)
								nextPairConfig.write("Awaiting Next Turn")
								def p1 = chosenPairs[0]
								def p2 = chosenPairs[1]
								changePairs(p1[0], p1[1], Color.LIGHT_GRAY, -1)
								changePairs(p2[0], p2[1], Color.LIGHT_GRAY, -1)
								chosenPairs = [null, null]
								currentPair = 0
								notMatched = false
								println("playerID:$myPlayerId my turn is over GameID:$gameId")
								toController.write(new turnOver(playerID: myPlayerId, gameID: gameId))
								gameDetails = (GameDetails) fromController.read()
								turnID = gameDetails.turn
							} else if ( matchOutcome == 1) {
								def timer= new CSTimer()
								timer.sleep(1500)
								notMatched = false
								toController.write(new ClaimPair ( id: myPlayerId,
												   	   			   gameId: gameId,
																   p1: chosenPairs[0],
																   p2: chosenPairs[1]))
								gameDetails = (GameDetails)fromController.read()
								pairsMap = gameDetails.pairsSpecification
								createBoard()
							}
							break
						case UPDATE:
							println ("updating")
							gameDetails = (GameDetails)fromController.read()
							gameId = gameDetails.gameId
							playerMap = gameDetails.playerDetails
							pairsMap = gameDetails.pairsSpecification
							turnID = gameDetails.turn
							myPlayerId = gameDetails.playerID
							notMatched = false
							break
					}// end of outer switch
				} // end of while getting two pairs


			} // end of while enrolled loop

			IPlabel.write("Goodbye " + playerName + ", please close game window")
		} //end of enrolling test
	} // end run


}

