package se.sics.gvod.simulator.croupier.utils;

import java.util.HashMap;

public class SkypeParse {
	public static HashMap<Integer, Integer> parse(String input) {
		int key;
		int value;
		HashMap<Integer, Integer> scenario = new HashMap<Integer, Integer>();
		
		String[] lineStr;
		String scenarioStr = FileIO.read(input);
		String[] lines = scenarioStr.split("\n");
		
		for (int i= 0; i < lines.length; i++) {
			lineStr = lines[i].split("\t");
			key = Integer.parseInt(lineStr[0]);
			value = Integer.parseInt(lineStr[1]);
		
			if (scenario.containsKey(key))
				scenario.put(key, scenario.get(key) + value);
			else
				scenario.put(key, value);
		}
		
		//System.out.println(scenario);
		return scenario;			
	}
}
