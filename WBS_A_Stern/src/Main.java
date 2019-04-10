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
	static List<Node> doneMedalNodes;             //Medaillon-Positionen f�r die der Algorithmus breits durchgelaufen ist
	static Node startNode, crownNode, medalNode;  //Startpunkt und Position der Krone, sowie aktuelle Madaillon-Position
	
	static Boolean medal = false;                 //Medaillon vorhanden?
	
	static String solutionPart1, solutionPart2, finalSolution, outputMessage;
	
	static int score = 10000;
	
	public static void main(String args[]) {
		init(); //Variablen initialisieren und Dateien einlesen
		
		//Ermittlung des voraussichtlich effizientesten Medaillons
		int potentialScore = 10000;
		while(doneMedalNodes.size()<medalNodes.size()) {  //Algorithmus l�uft f�r jedes Medaillon einmal, au�er eine bisher gefundene L�sung kann dadurch nicht verbessert werden
			potentialScore = 10000;
			for(Node n : medalNodes) {
				int minCost = Math.abs(startNode.x-n.x) + Math.abs(startNode.y-n.y) + Math.abs(crownNode.x-n.x) + Math.abs(crownNode.y-n.y); //minimaler Zeitaufwand von Start zu Medaillon und dann zur Krone
				if(!doneMedalNodes.contains(n) && minCost<potentialScore) {  //Algorithmus l�uft nur f�r Madaillons f�r die er noch nicht gelaufen ist, Auswahl des voraussichtlich besten Medaillons
					if(minCost<score) {            //Algorithmus k�nnte mit diesem Medaillon eine bessere L�sung finden als bisherige L�sung
						potentialScore = minCost;
						medalNode = n;
					}
					else {                   //Algorithmus kann nach der Sch�tzfunktion unm�glich eine bessere L�sung als die bisherige finden, wenn er dieses Medaillon verwendet 
						System.out.println("Sch�tzung f�r Amulett(" + n.x + "," + n.y + "): " + minCost + " --> Irrelevant");
						doneMedalNodes.add(n);   //Medaillon wird nicht betrachtet
					}		     
				}
			}
			if(doneMedalNodes.size()<medalNodes.size()) {
				System.out.println("Sch�tzung f�r Amulett(" + medalNode.x + "," + medalNode.y + "): " + potentialScore);
				outputMessage += "Sch�tzung f�r Amulett(" + medalNode.x + "," + medalNode.y + "): " + potentialScore + "\n";
				int currentScore = aStern(startNode, medalNode, 1, false) + aStern(medalNode, crownNode, 2, true);     //erreichter Punktwert als neue Grenze f�r eventuell weiteren Durchlauf mit neuem Medaillon
				if(currentScore < score) { //liefert true wenn gilt: L�sung gefunden (da sonst currentScore gr��er 10000), falls bereits L�sung vorhanden: neue L�sung ist besser
					score = currentScore;                           //neuer zu unterbietender Punktwert
					finalSolution = solutionPart1 + " A " + solutionPart2;  //L�sung speichern
					System.out.println("Ergebnis: " + score + ": " + finalSolution);
					outputMessage += "Ergebnis: " + score + ": " + finalSolution  + "\n";
				}
				else {
					System.out.println("Keine L�sung oder keine Verbesserung");
					outputMessage += "Keine L�sung oder keine Verbesserung\n";
				}
				doneMedalNodes.add(medalNode);          //Algorithmus f�r Medaillon erledigt
			}
		}
		
		//JOptionPane.showMessageDialog(null, outputMessage, "Ergebnis", JOptionPane.INFORMATION_MESSAGE);
	}
	
	static int aStern(Node beginNode, Node endNode, int stringNumber, Boolean haveMedaillon) {
		medal = haveMedaillon; //global speichern ob Medaillon vorhanden
		//Alle Nodes zur�ck in unknown, falls nicht der erste Durchlauf
		for(Node n : closedList) {
			unknown.add(n);
		}
		closedList.clear();
		for(Node n : openList) {
			unknown.add(n);
		}
		openList.clear();
		
		//Initialisieren f�r Durchlauf
		openList.add(beginNode);   //Startpunkt als erster Knoten in der OpenListe
		unknown.remove(beginNode); //Startpunkt aus unbekannten Nodes entfernen
		beginNode.g = 0; //Startpunkt mit Kosten 0 initialisieren
		int currentScore = 10000;
		while(!openList.isEmpty()) { //Algorithmus terminiert ohne Ergebnis falls openList leer ist
			Collections.sort(openList);  //openList nach Priorit�t sortiert
			Node node = openList.get(0); //erstes Element hat h�chste Priorit�t
			
			//Knoten expandieren und von openList in closedList verschieben
			openList.remove(node);
			closedList.add(node);
			
			if(node == endNode) {
				currentScore = node.g;  //Bewerten der gefundenen L�sung
				saveSolution(node, beginNode, stringNumber);
				break;
			}
			else
				expand(node, endNode);	
		}
		return currentScore;  //R�ckgabe des Zeitaufwands f�r diesen Weg, 10000 falls kein Weg vorhanden
	}
	
	static void expand(Node node, Node endNode) {
		//System.out.println(node.x + "," + node.y + " f: " + node.f + " g: " + node.g + " h: " + node.h);  //Auskommentieren um einzelne Arbeitsschritte zu sehen
		//Bewegung ist immer eine Option falls passender Knoten (oben, unten, rechts oder links in openList oder unknown
		
		//tempor�re Variablen, da man Listen nicht bearbeiten kann w�hrend man durch sie iteriert
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
				if(temp.g > node.g + temp.cost)                      //f�r bereits offenen Knoten k�rzerer Weg gefunden
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
				if(temp.g > node.g + temp.cost)                      //f�r bereits offenen Knoten k�rzerer Weg gefunden
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
				if(temp.g > node.g + temp.cost)                      //f�r bereits offenen Knoten k�rzerer Weg gefunden
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
				if(temp.g > node.g + temp.cost)                      //f�r bereits offenen Knoten k�rzerer Weg gefunden
					temp.open(node, endNode.x, endNode.y);           //aktualisieren des offenen Knotens
				expandToTemp = false;
			}
		}
		
		
	}
	
	static Boolean testLegalMove(Node from, Node to) {
		if(medal == false && (to.land == 0))  //Versuch �ber Wasser zu gehen ohne Medaillon nicht m�glich
			return false;
		if(medal) {                           //Medaillon vorhanden
			if((from.land == 1 && to.land == 3) || (from.land == 3 && to.land == 1))  //illegale Grenz�berschreitung zwischen Land 1 und 3 mit Medaillon
				return false;
		}
			return true;                      //erlaubter Zug
	}
	
	static void saveSolution(Node node, Node beginNode, int stringNumber) {  //Zusammensetzen der L�sung ausgehend von Zielknoten �ber Elternknoten bis zum Start
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
		JOptionPane.showMessageDialog(null, "Bitte erst Spielfelddatei, dann Datei mit Objektpositionen w�hlen.", "Auswahl",
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
		
		//Alle unbekannten Nodes erzeugen und der liste hinzuf�gen
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
