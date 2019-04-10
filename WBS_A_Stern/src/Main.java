import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Main  {
	//A*-Listen
	static List<Node> unknown;
	static List<Node> openList;
	static List<Node> closedList;
	
	static List<Node> medalNodes;                 //Medaillon-Positionen
	static List<Node> doneMedalNodes;             //Medaillon-Positionen für die der Algorithmus breits durchgelaufen ist
	static Node startNode, crownNode, medalNode;  //Startpunkt und Position der Krone, sowie aktuelle Madaillon-Position
	
	static Boolean medal = false;                 //Medaillon vorhanden?
	
	static String solutionPart1, solutionPart2, finalSolution, outputMessage;
	
	static int score = 10000;
	
	public static void main(String args[]) {
		init(); //Variablen initialisieren und Dateien einlesen
		
		//Ermittlung des voraussichtlich effizientesten Medaillons
		int potentialScore = 10000;
		while(doneMedalNodes.size()<medalNodes.size()) {  //Algorithmus läuft für jedes Medaillon einmal, außer eine bisher gefundene Lösung kann dadurch nicht verbessert werden
			potentialScore = 10000;
			for(Node n : medalNodes) {
				int minCost = Math.abs(startNode.x-n.x) + Math.abs(startNode.y-n.y) + Math.abs(crownNode.x-n.x) + Math.abs(crownNode.y-n.y); //minimaler Zeitaufwand von Start zu Medaillon und dann zur Krone
				if(!doneMedalNodes.contains(n) && minCost<potentialScore) {  //Algorithmus läuft nur für Madaillons für die er noch nicht gelaufen ist, Auswahl des voraussichtlich besten Medaillons
					if(minCost<score) {            //Algorithmus könnte mit diesem Medaillon eine bessere Lösung finden als bisherige Lösung
						potentialScore = minCost;
						medalNode = n;
					}
					else {                   //Algorithmus kann nach der Schätzfunktion unmöglich eine bessere Lösung als die bisherige finden, wenn er dieses Medaillon verwendet 
						System.out.println("Schätzung für Amulett(" + n.x + "," + n.y + "): " + minCost + " --> Irrelevant");
						doneMedalNodes.add(n);   //Medaillon wird nicht betrachtet
					}		     
				}
			}
			if(doneMedalNodes.size()<medalNodes.size()) {
				System.out.println("Schätzung für Amulett(" + medalNode.x + "," + medalNode.y + "): " + potentialScore);
				outputMessage += "Schätzung für Amulett(" + medalNode.x + "," + medalNode.y + "): " + potentialScore + "\n";
				int currentScore = aStern(startNode, medalNode, 1, false) + aStern(medalNode, crownNode, 2, true);     //erreichter Punktwert als neue Grenze für eventuell weiteren Durchlauf mit neuem Medaillon
				if(currentScore < score) { //liefert true wenn gilt: Lösung gefunden (da sonst currentScore größer 10000), falls bereits Lösung vorhanden: neue Lösung ist besser
					score = currentScore;                           //neuer zu unterbietender Punktwert
					finalSolution = solutionPart1 + " A " + solutionPart2;  //Lösung speichern
					System.out.println("Ergebnis: " + score + ": " + finalSolution);
					outputMessage += "Ergebnis: " + score + ": " + finalSolution  + "\n";
				}
				else {
					System.out.println("Keine Lösung oder keine Verbesserung");
					outputMessage += "Keine Lösung oder keine Verbesserung\n";
				}
				doneMedalNodes.add(medalNode);          //Algorithmus für Medaillon erledigt
			}
		}
		
		//JOptionPane.showMessageDialog(null, outputMessage, "Ergebnis", JOptionPane.INFORMATION_MESSAGE);
	}
	
	static int aStern(Node beginNode, Node endNode, int stringNumber, Boolean haveMedaillon) {
		medal = haveMedaillon; //global speichern ob Medaillon vorhanden
		//Alle Nodes zurück in unknown, falls nicht der erste Durchlauf
		for(Node n : closedList) {
			unknown.add(n);
		}
		closedList.clear();
		for(Node n : openList) {
			unknown.add(n);
		}
		openList.clear();
		
		//Initialisieren für Durchlauf
		openList.add(beginNode);   //Startpunkt als erster Knoten in der OpenListe
		unknown.remove(beginNode); //Startpunkt aus unbekannten Nodes entfernen
		beginNode.g = 0; //Startpunkt mit Kosten 0 initialisieren
		int currentScore = 10000;
		while(!openList.isEmpty()) { //Algorithmus terminiert ohne Ergebnis falls openList leer ist
			Collections.sort(openList);  //openList nach Priorität sortiert
			Node node = openList.get(0); //erstes Element hat höchste Priorität
			
			//Knoten expandieren und von openList in closedList verschieben
			openList.remove(node);
			closedList.add(node);
			
			if(node == endNode) {
				currentScore = node.g;  //Bewerten der gefundenen Lösung
				saveSolution(node, beginNode, stringNumber);
				break;
			}
			else
				expand(node, endNode);	
		}
		return currentScore;  //Rückgabe des Zeitaufwands für diesen Weg, 10000 falls kein Weg vorhanden
	}
	
	static void expand(Node node, Node endNode) {
		//System.out.println(node.x + "," + node.y + " f: " + node.f + " g: " + node.g + " h: " + node.h);  //Auskommentieren um einzelne Arbeitsschritte zu sehen
		//Bewegung ist immer eine Option falls passender Knoten (oben, unten, rechts oder links in openList oder unknown
		
		//temporäre Variablen, da man Listen nicht bearbeiten kann während man durch sie iteriert
		Node temp = null;
		Boolean expandToTemp = false;
		
		//oben
		for(Node n : unknown) {
			if((n.x == node.x-1) && (n.y == node.y) && testLegalMove(node, n)) {
				temp = n;
				expandToTemp = true;
				break;
			}
		}
		if(expandToTemp) {
			unknown.remove(temp);
			openList.add(temp);
			temp.open(node, endNode.x, endNode.y);
			expandToTemp = false;
		} else {
			for(Node n : openList) {
				if((n.x == node.x-1) && (n.y == node.y) && testLegalMove(node, n)) {
					temp = n;
					expandToTemp = true;
					break;
				}
			}
			if(expandToTemp) {
				if(temp.g > node.g + temp.cost)                      //für bereits offenen Knoten kürzerer Weg gefunden
					temp.open(node, endNode.x, endNode.y);           //aktualisieren des offenen Knotens
				expandToTemp = false;
			}
		}
		
		//unten
		for(Node n : unknown) {
			if((n.x == node.x+1) && (n.y == node.y) && testLegalMove(node, n)) {
				temp = n;
				expandToTemp = true;
				break;
			}
		}
		if(expandToTemp) {
			unknown.remove(temp);
			openList.add(temp);
			temp.open(node, endNode.x, endNode.y);
			expandToTemp = false;
		} else {
			for(Node n : openList) {
				if((n.x == node.x+1) && (n.y == node.y) && testLegalMove(node, n)) {
					temp = n;
					expandToTemp = true;
					break;
				}
			}
			if(expandToTemp) {
				if(temp.g > node.g + temp.cost)                      //für bereits offenen Knoten kürzerer Weg gefunden
					temp.open(node, endNode.x, endNode.y);           //aktualisieren des offenen Knotens
				expandToTemp = false;
			}
		}
		
		//links
		for(Node n : unknown) {
			if((n.x == node.x) && (n.y == node.y-1) && testLegalMove(node, n)) {
				temp = n;
				expandToTemp = true;
				break;
			}
		}
		if(expandToTemp) {
			unknown.remove(temp);
			openList.add(temp);
			temp.open(node, endNode.x, endNode.y);
			expandToTemp = false;
		} else {
			for(Node n : openList) {
				if((n.x == node.x) && (n.y == node.y-1) && testLegalMove(node, n)) {
					temp = n;
					expandToTemp = true;
					break;
				}
			}
			if(expandToTemp) {
				if(temp.g > node.g + temp.cost)                      //für bereits offenen Knoten kürzerer Weg gefunden
					temp.open(node, endNode.x, endNode.y);           //aktualisieren des offenen Knotens
				expandToTemp = false;
			}
		}
		
		//rechts
		for(Node n : unknown) {
			if((n.x == node.x) && (n.y == node.y+1) && testLegalMove(node, n)) {
				temp = n;
				expandToTemp = true;
				break;
			}
		}
		if(expandToTemp) {
			unknown.remove(temp);
			openList.add(temp);
			temp.open(node, endNode.x, endNode.y);
			expandToTemp = false;
		} else {
			for(Node n : openList) {
				if((n.x == node.x) && (n.y == node.y+1) && testLegalMove(node, n)) {
					temp = n;
					expandToTemp = true;
					break;
				}
			}
			if(expandToTemp) {
				if(temp.g > node.g + temp.cost)                      //für bereits offenen Knoten kürzerer Weg gefunden
					temp.open(node, endNode.x, endNode.y);           //aktualisieren des offenen Knotens
				expandToTemp = false;
			}
		}
		
		
	}
	
	static Boolean testLegalMove(Node from, Node to) {
		if(medal == false && (to.land == 0))  //Versuch über Wasser zu gehen ohne Medaillon nicht möglich
			return false;
		if(medal) {                           //Medaillon vorhanden
			if((from.land == 1 && to.land == 3) || (from.land == 3 && to.land == 1))  //illegale Grenzüberschreitung zwischen Land 1 und 3 mit Medaillon
				return false;
		}
			return true;                      //erlaubter Zug
	}
	
	static void saveSolution(Node node, Node beginNode, int stringNumber) {  //Zusammensetzen der Lösung ausgehend von Zielknoten über Elternknoten bis zum Start
		String solutionString = "(" + node.x + "," + node.y + ")";
		while(node != beginNode) {
			node = node.parent;
			solutionString = "(" + node.x + "," + node.y + "), " + solutionString; 
		}
		if(stringNumber == 1)
			solutionPart1 = solutionString;
		else
			solutionPart2 = solutionString;
	}
	
	static void init() {
		outputMessage = "";
		//Initialisiere Listen
		unknown = new ArrayList<Node>();
		openList = new ArrayList<Node>();
		closedList = new ArrayList<Node>();
		medalNodes = new ArrayList<Node>();
		doneMedalNodes = new ArrayList<Node>();
		
		//Spielfelddatei laden
		JOptionPane.showMessageDialog(null, "Bitte erst Spielfelddatei, dann Datei mit Objektpositionen wählen.", "Auswahl",
				JOptionPane.INFORMATION_MESSAGE);
		String filename = "";
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files", "csv");
		chooser.setFileFilter(filter);
		int returnValue = chooser.showOpenDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File selectedFile = chooser.getSelectedFile();
			System.out.println(selectedFile.getAbsolutePath());
			filename = selectedFile.getAbsolutePath();
		}
		List<String> lines = new ArrayList<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
			reader.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Fehler beim Lesen der Datei", "Fehler",
					JOptionPane.ERROR_MESSAGE);
		}
		
		//Alle unbekannten Nodes erzeugen und der liste hinzufügen
		int x = 0,  y= 0;
		for(String s : lines) {
			String[] fields = s.split(";");
			for(String field : fields) {
				unknown.add(new Node(x, y, Integer.parseInt(field)));
				x++;
			}
			y++;
			x = 0;
		}
		
		//Datei mit Objektpositionen laden
		chooser = new JFileChooser();
		FileNameExtensionFilter filter2 = new FileNameExtensionFilter("TXT Files", "txt");
		chooser.setFileFilter(filter2);
		returnValue = chooser.showOpenDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File selectedFile = chooser.getSelectedFile();
			System.out.println(selectedFile.getAbsolutePath());
			filename = selectedFile.getAbsolutePath();
		}
		lines = new ArrayList<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
			reader.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Fehler beim Lesen der Datei", "Fehler",
					JOptionPane.ERROR_MESSAGE);
		}
		
		//Objektpositionen auslesen und speichern
		for(String l : lines) {
			if(l.contains("Startposition")) {
				String[] temp = l.split(";");
				for(Node n : unknown) {
					if(n.x == Integer.parseInt(temp[1]) && n.y == Integer.parseInt(temp[2]))
						startNode = n;
				}
			} else if(l.contains("Krone")) {
				String[] temp = l.split(";");
				for(Node n : unknown) {
					if(n.x == Integer.parseInt(temp[1]) && n.y == Integer.parseInt(temp[2]))
						crownNode = n;
				}
			} else if(l.contains("Medaillon")) {
				String[] temp = l.split(";");
				for(Node n : unknown) {
					if(n.x == Integer.parseInt(temp[1]) && n.y == Integer.parseInt(temp[2]))
						medalNodes.add(n);
				}
			}
		}
	}

}
