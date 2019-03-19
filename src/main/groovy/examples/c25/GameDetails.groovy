package examples.c25

import java.io.Serializable;

class GameDetails implements Serializable {
	def gameId
	def turn
	def playerDetails = null
	def pairsSpecification = null
}
