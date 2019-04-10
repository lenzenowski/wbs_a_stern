
public class Node implements Comparable<Node>{

	int f, g, h;  //f = g + h, wobei g die Kosten vom Start bis zum Knoten und h die geschätzten verbleibenden Kosten sind.
	int x,y; //Koordinaten des Knotens
	int land; //1, 2 oder 3 für die Länder 1-3 oder 0 für Wasser
	int cost; //Zeitaufwand für das Feld
	
	Node parent = null; //Elternknoten
	
	Node(int x, int y, int land) {
		this.x = x;
		this.y = y;
		this.land = land;
		if(this.land == 0)
			this.cost = 2;
		else
			this.cost = 1;
	}
	
	public void open(Node parent, int targetX, int targetY) {
		this.g= parent.g + this.cost;   //Kosten für Land: Elternkosten + 1
		this.h = Math.abs(this.x - targetX) + Math.abs(this.y - targetY); //optimistische Schätzfunktion (Mindestaufwand = Abstand(x-Richtung) + Abstand(y-Richtung))
		this.f = this.g + this.h;   //Bewertung des Knotens für den Algorithmus
		this.parent = parent;
	}
	
	public int compareTo(Node n) {
		if(this.f < n.f)    //Sortierfunktion um Knoten automatisch nach Priorität ordnen zu können
			return -1;      //aktueller Knoten hat höhere Priorität als Vergleichsknoten
		if(this.f == n.f) { //aktueller Knoten ist voraussichtlich genau so sinnvoll wie Vergleichsknoten
			if(this.h < n.h)
				return -1;
			if(this.h > n.h)
				return 1;
			else
				return 0;
		}
		return 1;           //aktueller Knoten bekommt geringere Priorität als Vergleichsknoten
	}
}
