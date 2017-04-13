import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.Time;
import java.text.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.Arrays;
import java.util.ArrayList;
import java.awt.*; 

import javax.swing.*;

public class TSP {

	private static final int cityShiftAmount = 60; //DO NOT CHANGE THIS.
	
    /**
     * How many cities to use.
     */
    protected static int cityCount;

    /**
     * How many chromosomes to use.
     */
    protected static int populationSize = 100; //DO NOT CHANGE THIS.

    /**
     * The part of the population eligable for mating.
     */
    protected static int matingPopulationSize;

    /**
     * The part of the population selected for mating.
     */
    protected static int selectedParents;

    /**
     * The current generation
     */
    protected static int generation;

    /**
     * The list of cities (with current movement applied).
     */
    protected static City[] cities;
    
    /**
     * The list of cities that will be used to determine movement.
     */
    private static City[] originalCities;

    /**
     * The list of chromosomes.
     */
    protected static Chromosome[] chromosomes;
    

    /**
    * Frame to display cities and paths
    */
    private static JFrame frame;

    /**
     * Integers used for statistical data
     */
    private static double min;
    private static double avg;
    private static double max;
    private static double sum;
    private static double genMin;

    /**
     * Width and Height of City Map, DO NOT CHANGE THESE VALUES!
     */
    private static int width = 600;
    private static int height = 600;


    private static Panel statsArea;
    private static TextArea statsText;


    /*
     * Writing to an output file with the costs.
     */
    private static void writeLog(String content) {
        String filename = "results.out";
        FileWriter out;

        try {
            out = new FileWriter(filename, true);
            out.write(content + "\n");
            out.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*
     *  Deals with printing same content to System.out and GUI
     */
    private static void print(boolean guiEnabled, String content) {
        if(guiEnabled) {
            statsText.append(content + "\n");
        }

        System.out.println(content);
    }
    
    /**
     *compare two chromosomes by cost
     *return true if a <= b
     * 
     *@param a chromosome to be tested
     *@param b chromosome to be tested against
     */
    public static boolean better(Chromosome a, Chromosome b){
    	if (a == null || b == null)
    		return false;
    	if (a.getCost() < b.getCost())
    		return true;
    	return false;
    }
    /*
     * mutates a chromosome by swapping two cities such that 
     * a shorter sequence is created 
     * 
     * @param a chromosome to be mutated
     */
    public static Chromosome climb(Chromosome a){
    	Random rand = new Random();
    	int [] tempCityList = Arrays.copyOfRange(a.cityList, 0, a.cityList.length);
    	Chromosome child = new Chromosome(cities);
    	child.setCities(tempCityList);
    	int cityA = 0, cityB = 0;
    	for (int i = 0; i < tempCityList.length; i++){
    		int tempCityA = tempCityList[i];
    		for (int j = 0; j < tempCityList.length; j++){
    			if (j == i){
    				j = j +2;
    			}
    			int tempCityB = tempCityList[j%tempCityList.length];
    			int[] checkAList = {tempCityList[(tempCityList.length-1+i)%tempCityList.length],tempCityA, tempCityList[(i+1)%tempCityList.length], tempCityList[(i+2)%tempCityList.length]};
    			int[] checkBList = {tempCityList[(tempCityList.length-1+j)%tempCityList.length], tempCityB, tempCityList[(j+1)%tempCityList.length], tempCityList[(j+2)%tempCityList.length]};
    			int[] checkABList = {tempCityList[(tempCityList.length-1+i)%tempCityList.length],tempCityA, tempCityB, tempCityList[(i+2)%tempCityList.length]};
    			if (distance(checkABList)<distance(checkAList)&& distance(checkBList)<distance(checkABList)){
    				cityA = tempCityList[(i+1)%tempCityList.length];
    				cityB = tempCityB;
    			}
    		}
    	}
    	int indexA = child.findCity(cityA);
    	int indexB = child.findCity(cityB);
    	
    	child.setCity(indexA, cityB);
    	child.setCity(indexB, cityA);
    	return child;
    }
    /*
     * mutates a chromosome by swapping two adjacent cities
     * 
     * @param a chromosome to be mutated
     */
    public static Chromosome invert(Chromosome a){
    	Random rand = new Random();
    	Chromosome mutant = new Chromosome(cities);
    	mutant.setCities(a.cityList);
    	mutant.calculateCost(cities);
		int cityA = rand.nextInt(mutant.cityList.length);
		System.out.println(cityA);
		int indexA = mutant.findCity(cityA);
		cityA = mutant.getCity(indexA);
		int cityB = mutant.getCity((mutant.findCity(cityA)+1)%mutant.cityList.length);
		
    	int indexB = mutant.findCity(cityB);
		mutant.setCity(indexA, cityB);
		mutant.setCity(indexB, cityA);
		mutant.calculateCost(cities);
    	return mutant;
    	
    }
    /*
     * recombines two chromosomes
     * return first chromosome
     * 
     * @param a chromosome to be recombined
     * @param b chromosome to be recombined
     * 
     * 
     */
    public static Chromosome recombinate (Chromosome a, Chromosome b){
    	Random rand = new Random();
    	int cityA = rand.nextInt(b.cityList.length-3)+1;
    	int cityB = cityA;
    	while (cityA >= cityB)
    		cityB = rand.nextInt(b.cityList.length);
		int[] recombBList = Arrays.copyOfRange(b.cityList,cityA,cityB);
				
		Chromosome.remove(a, recombBList);
		Chromosome.add(a, recombBList, cityA, cityB);
		a.calculateCost(cities);
		
    	return a;
    	
    }
    /*
     * scrambles the list of given cities
     * returns the list of scrambled cities
     * 
     * @param cities the list of cities to be reversed
     */
    public static int[] scramble(int[] cities){
    	//int temp;
    	Random rand = new Random();
    	int [] temp = Arrays.copyOf(cities, cities.length); 
    	ArrayList<Integer> positions = new ArrayList<>();
    	for (int i = 0; i < cities.length; i++){ 
    		Integer position = rand.nextInt(cities.length);
    		if (i == 0){
    			positions.add(position);
    		}
    		else{
    			while (positions.contains(position)){
    				position = rand.nextInt(cities.length);
    			}
    			positions.add(position);
    		}
    		cities[position] = temp[i];
    	}
    	
    	return cities;
    }
    /*
     * reverses the list of given cities
     * returns the list of reversed cities
     * 
     * @param cities the list of cities to be reversed
     */
    public static int[] reverse(int[] cities){
    	//int temp;
    	int [] temp = Arrays.copyOf(cities, cities.length);    	
    	for (int i = cities.length; i > 0; i--){    		
    		cities[cities.length-i] = temp[i-1];
    	}
    	
    	return cities;
    }
    
    /*
     * recombines two chromosomes in reverse order
     * return first chromosome
     * 
     * @param a chromosome to be recombined
     * @param b chromosome to be recombined
     * 
     * 
     */
    public static Chromosome reverseRecombinate (Chromosome a, Chromosome b){
    	
    	Random rand = new Random();
    	int cityA = rand.nextInt(a.cityList.length-3)+1;
    	int cityB = cityA;
    	while (cityA >= cityB)
    		cityB = rand.nextInt(a.cityList.length);
		
		int[] recombBList = Arrays.copyOfRange(b.cityList,cityA,cityB);
		recombBList = reverse(recombBList);
		
		Chromosome.remove(a, recombBList);
		Chromosome.add(a, recombBList, cityA, cityB);
		a.calculateCost(cities);
		
    	return a;
    	
    }
    /*
     * determines the cost of a list of cities
     * returns the cost of the list
     * 
     * @param list the list of cities for which a cost must be determined
     */
    public static double distance (int[] list){
    	double total = 0.0;
    	
    	for (int i = 0; i < list.length; i++){    		
    		total += cities[list[i]].proximity(cities[list[(i+1)%list.length]]);
    	}
    	return total;
    }
    /*
     * recombine the shortest section of a specified length
     * return recombined child
     * 
     * @param a chromosome to recombine from
     * @param b chromosome to recombine to
     * @param length the length of the sub-list
     * 
     */
    public static Chromosome shortestRecombine(Chromosome a, Chromosome b, int length){
    	Chromosome child = new Chromosome(cities);
    	child.setCities(b.cityList);
    	int [] recombList = Arrays.copyOf(a.cityList, length);
    	int cityA = 0, cityB = length;
    	for (int i = 0; i < a.cityList.length; i++){
    		int tempCityA = i%a.cityList.length;
    		int tempCityB = (i+length)%a.cityList.length;
    		
    		int[] newList = recombList; 
    		if (tempCityA < tempCityB){
    			newList = Arrays.copyOfRange(a.cityList, i%a.cityList.length, (i+length)%a.cityList.length);
    		}
    		else{
    			newList = join(Arrays.copyOfRange(a.cityList, tempCityB, a.cityList.length),Arrays.copyOfRange(a.cityList, 0, tempCityA), Arrays.copyOfRange(a.cityList, 0, 0));
    		}
    		if (distance(newList) > distance(recombList)){
    			recombList = newList;
    			cityA = i%a.cityList.length;
    			cityB = (i+length)%a.cityList.length;
    		}
    	}
    	recombList = scramble(recombList);
    	//recombList = reverse(recombList);
    	if (cityA<cityB){
    		recombList = join(Arrays.copyOfRange(a.cityList, 0, cityA), recombList, Arrays.copyOfRange(a.cityList, cityB, a.cityList.length));
    	}
    	else{
    		recombList = join(Arrays.copyOfRange(recombList,a.cityList.length-cityA+1,recombList.length), Arrays.copyOfRange(a.cityList, cityB, cityA), Arrays.copyOfRange(recombList, 0, a.cityList.length-cityA+1));
    		
    	}
    	child.setCities(recombList);
    	child.calculateCost(cities);
    	return child;
    }
    
    /*
     * recombine the shortest reversed section (of length 3) of a specified length
     * return recombined child
     * 
     * @param a chromosome to recombine from
     * @param b chromosome to recombine to
     * @param length the length of the sub-list
     * 
     */
    public static Chromosome shortestTRRecombine(Chromosome a, Chromosome b, int length){
    	Chromosome child = new Chromosome(cities);
    	child.setCities(b.cityList);
    	int [] recombList = Arrays.copyOf(a.cityList, length);
    	int cityA = 0, cityB = length;
    	for (int i = 0; i < a.cityList.length; i++){
    		int tempCityA = i%a.cityList.length;
    		int tempCityB = (i+length)%a.cityList.length;
    		
    		int[] newList = recombList; 
    		if (tempCityA < tempCityB){
    			newList = Arrays.copyOfRange(a.cityList, i%a.cityList.length, (i+length)%a.cityList.length);
    		}
    		else{
    			newList = join(Arrays.copyOfRange(a.cityList, tempCityB, a.cityList.length),Arrays.copyOfRange(a.cityList, 0, tempCityB), Arrays.copyOfRange(a.cityList, 0, 0));
    		}
    		if (distance(newList) > distance(recombList)){
    			recombList = newList;
    			cityA = i%a.cityList.length;
    			cityB = (i+length)%a.cityList.length;
    		}
    	}
    	//Arrays.reverse
    	if (cityA<cityB){
    		recombList = join(Arrays.copyOfRange(a.cityList, 0, cityA), recombList, Arrays.copyOfRange(a.cityList, cityB, a.cityList.length));
    	}
    	else{
    		recombList = join(Arrays.copyOfRange(recombList,a.cityList.length-1-cityA,recombList.length), Arrays.copyOfRange(a.cityList, cityB+1, cityA), Arrays.copyOfRange(recombList, 0, a.cityList.length-1-cityA));
    		
    	}
    	child.setCities(recombList);
    	child.calculateCost(cities);
    	return child;
    }
    /*
     * an implementation of the pmx (partial match crossover) crossover variation operator
     * returns the resultant child
     * 
     * @param a parent 2 of the pmx algorithm
     * @param b parent 1 whose swath is crossovered
     */
    public static int gc = 0;
    public static Chromosome pmx(Chromosome a, Chromosome b){
    	gc++;
    	Random rand = new Random();
    	int cityA = rand.nextInt(b.cityList.length-3)+1;
    	int cityB = cityA;
    	while (cityA >= cityB)
    		cityB = rand.nextInt(b.cityList.length);
		int[] recombBList = Arrays.copyOfRange(b.cityList,cityA,cityB);
		int[] aList = Arrays.copyOfRange(a.cityList,cityA,cityB);
		int[] newList = new int[b.cityList.length];
		ArrayList<Integer> positions = new ArrayList();
		int count = 0;
		Chromosome child = new Chromosome(cities);
		//boolean notIn = true;
		//add those in parent 2 swath not in parent 1 swath
		
		for (int c = 0; c < aList.length; c++){
			boolean notIn = true;
			for (int j = 0; j <recombBList.length; j++){
				if (aList[c] == recombBList[j]){
					notIn = false;
					j = recombBList.length;
				}
			}
			if (notIn){
				int value = aList[c];
				int searchValue = recombBList[c];
				for (int k = 0; k <a.cityList.length; k++){
					if (searchValue == a.cityList[k] && (k < cityA || k >= cityB)){
						newList[k] = value;
						positions.add(k);
						k = a.cityList.length;
					}
					else if (searchValue == a.cityList[k] && (k >=cityA && k < cityB)){
						searchValue = b.cityList[k];						
						k=0;
					}
				}				
			}
		}
		int oCount =0;
		for (int i = 0; i<newList.length; i++){
			
			if(i>=cityA && i < cityB && count < recombBList.length){
				newList[i] = recombBList[count];
				//System.out.println(cityB + " " + i);
				count++;
				
			}
			else if ((i < cityA || i >= cityB)){
				if (!positions.contains(i)){
					newList[i] = a.cityList[i];
					oCount++;
				}					
			}
			//if (i >= cityB && count < recombBlist.length -)
			
		}
		int length = positions.size() + count +oCount;
		/*for(int p = 0; p < newList.length; p++){
    		System.out.print(newList[p] + " ");        	
    	}
    	System.out.println("|"+gc+"|" + length);
    	for(int p = 0; p < a.cityList.length; p++){
    		System.out.print(a.cityList[p] + " ");        	
    	}
    	System.out.println("|a|" + length);
    	for(int p = 0; p < b.cityList.length; p++){
    		System.out.print(b.cityList[p] + " ");        	
    	}
    	System.out.println("|b|" + length);
    	for(int p = 0; p < aList.length; p++){
    		System.out.print(aList[p] + " ");        	
    	}
    	System.out.println("|a|" + length);
    	for(int p = 0; p < recombBList.length; p++){
    		System.out.print(recombBList[p] + " ");        	
    	}
    	System.out.println("|b|" + length);*/
		child.setCities(newList);
		
		child.calculateCost(cities);
    	return child;
    }
    /*
     * Implements a solution for ensuring a solution is  3-opt
     * 
     * @param a chromosome to optimise
     */
    public static Chromosome  twoOpt(Chromosome a){
    	
    	Chromosome optT = a;
    	for (int i = 0; i < optT.cityList.length; i++){
    		
    		Chromosome tempC = optT;
    		int cityA = tempC.getCity(i%tempC.cityList.length);
    		int cityB = tempC.getCity((i+1)%tempC.cityList.length);
    		tempC.setCity(i%tempC.cityList.length, cityB);
    		tempC.setCity((i+1)%tempC.cityList.length, cityA);
    		tempC.calculateCost(cities);
    		if (better(tempC,optT)){
    			System.out.println("hi");
    			optT = tempC;
    		}
    	}
    	/*for (int i = 0; i < optT.cityList.length/2; i++){
    		int[] temp = Arrays.copyOfRange(optT.cityList, i, optT.cityList.length-i);
    		temp = reverse(temp);
    		temp = join(Arrays.copyOfRange(optT.cityList, 0, i), temp, Arrays.copyOfRange(optT.cityList, optT.cityList.length-i, optT.cityList.length));
    		Chromosome newTempC = optT;
    		newTempC.setCities(temp);
    		newTempC.calculateCost(cities);
    		if (better(newTempC,optT)){
    			System.out.println("hi");
    			optT = newTempC;
    		}
    	}*/
			
    	
    	return optT;
    }
    public static void evolve() {
    	
        //Write evolution code here
    	Chromosome[] children = new Chromosome[chromosomes.length];
    	
    	Random rand = new Random();
    	int count = 0;
    	double[] roulette = new double[chromosomes.length];
    	double total = 0;
    	for(int r = 0; r < roulette.length; r++){
    		total += chromosomes[r].getCost();
    		roulette[r] = total;    		
    	}

    	while (count < chromosomes.length){
    		
    		
    		int num = rand.nextInt((int)total);
    		boolean uniqueInList = false;
    		boolean retry = false;
    		Chromosome child = new Chromosome(cities);
    		int key = 0;
    		for (int k = 0; k < roulette.length; k++){
    			if (roulette[k] < num){
    				key = k;
    				k = roulette.length;
    			}
    		}
    		//key = 0;
    		while (!uniqueInList){
    			double recomboP = rand.nextDouble();
    			uniqueInList = true;
    			if (!retry){
    				child.setCities(chromosomes[key%chromosomes.length].cityList);
    				child.calculateCost(cities);
    				
        			
    			}
    			//child = reverseRecombinate(child, chromosomes[((key+1)%chromosomes.length)]);
    			//child = invert(child);
    			//child = shortestRecombine(child, chromosomes[(key+1)%chromosomes.length], 3);
    			child = invert(child);
    			child = shortestRecombine(child, chromosomes[(key+1)%chromosomes.length], rand.nextInt(chromosomes[(key+1)%chromosomes.length].cityList.length-1) +1);
    			//if ( recomboP >= 0.7){
    				//child = invert(child); 
	    		//	child = reverseRecombinate(child, chromosomes[((key+1)%chromosomes.length)]);
	    			
				//}
	    		/*else if (recomboP >= 0.33 && recomboP < 0.66){ 
	    			//child = invert(child);	    			
	    			child = recombinate(child, chromosomes[((key+1)%chromosomes.length)]); 
	    			
	    		}*/
	    		//else if(recomboP < 0.2){
	    			//child = invert(child); 
	    			//child = reverseRecombinate(child, chromosomes[((key+1)%chromosomes.length)]);
	    			
	    			//child = climb(child); 
	    		//	child = shortestRecombine(child, chromosomes[(key+1)%chromosomes.length], rand.nextInt(chromosomes[(key+1)%chromosomes.length].cityList.length-1) +1);
	    			 
	    		//}
        		for (int i = 0; i < chromosomes.length; i++){        			
        			if (child.equal(chromosomes[i])){
        				retry = true;
        				uniqueInList = false;
        			}        				
        		}
    		}
    		children[count] = (child);
    		count++;
    		//key++;
    	}
    	
    	count = 0;
    	for (int w = 0; w < chromosomes.length && count < children.length; w++){
    		double probability = (count/Math.pow(chromosomes.length, generation));
    		double check = rand.nextDouble();
    		if((better(children[count], chromosomes[w%chromosomes.length]) || check > probability)){
    			chromosomes[w%chromosomes.length] = children[count];
    			count++;
    		}
    	}
    	
    }
    public static int[] join(int[] a, int[] b, int[] c) {
    	int aLen = a.length;
    	int bLen = b.length;
    	int cLen = c.length;
    	int[] d = new int[aLen+bLen+cLen];
    	System.arraycopy(a, 0, d, 0, aLen);
    	System.arraycopy(b, 0, d, aLen, bLen);
    	System.arraycopy(c, 0, d, aLen+bLen, cLen);
    	
    	return d;
    }
    /**
     * Update the display
     */
    public static void updateGUI() {
        Image img = frame.createImage(width, height);
        Graphics g = img.getGraphics();
        FontMetrics fm = g.getFontMetrics();

        g.setColor(Color.black);
        g.fillRect(0, 0, width, height);

        if (true && (cities != null)) {
            for (int i = 0; i < cityCount; i++) {
                int xpos = cities[i].getx();
                int ypos = cities[i].gety();
                g.setColor(Color.green);
                g.fillOval(xpos - 5, ypos - 5, 10, 10);
                
                //// SHOW Outline of movement boundary
                 //xpos = originalCities[i].getx();
                 //ypos = originalCities[i].gety();
                 //g.setColor(Color.darkGray);
                 //g.drawLine(xpos + cityShiftAmount, ypos, xpos, ypos + cityShiftAmount);
                 //g.drawLine(xpos, ypos + cityShiftAmount, xpos - cityShiftAmount, ypos);
                 //g.drawLine(xpos - cityShiftAmount, ypos, xpos, ypos - cityShiftAmount);
                 //g.drawLine(xpos, ypos - cityShiftAmount, xpos + cityShiftAmount, ypos);
            }

            g.setColor(Color.gray);
            for (int i = 0; i < cityCount; i++) {
                int icity = chromosomes[0].getCity(i);
                if (i != 0) {
                    int last = chromosomes[0].getCity(i - 1);
                    g.drawLine(
                        cities[icity].getx(),
                        cities[icity].gety(),
                        cities[last].getx(),
                        cities[last].gety());
                }
            }
                        
            int homeCity = chromosomes[0].getCity(0);
            int lastCity = chromosomes[0].getCity(cityCount - 1);
                        
            //Drawing line returning home
            g.drawLine(
                    cities[homeCity].getx(),
                    cities[homeCity].gety(),
                    cities[lastCity].getx(),
                    cities[lastCity].gety());
        }
        frame.getGraphics().drawImage(img, 0, 0, frame);
    }

    private static City[] LoadCitiesFromFile(String filename, City[] citiesArray) {
        ArrayList<City> cities = new ArrayList<City>();
        try 
        {
            FileReader inputFile = new FileReader(filename);
            BufferedReader bufferReader = new BufferedReader(inputFile);
            String line;
            while ((line = bufferReader.readLine()) != null) { 
                String [] coordinates = line.split(", ");
                cities.add(new City(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1])));
            }

            bufferReader.close();

        } catch (Exception e) {
            System.out.println("Error while reading file line by line:" + e.getMessage());                      
        }
        
        citiesArray = new City[cities.size()];
        return cities.toArray(citiesArray);
    }

    private static City[] MoveCities(City[]cities) {
    	City[] newPositions = new City[cities.length];
        Random randomGenerator = new Random();

        for(int i = 0; i < cities.length; i++) {
        	int x = cities[i].getx();
        	int y = cities[i].gety();
        	
            int position = randomGenerator.nextInt(5);
            
            if(position == 1) {
            	y += cityShiftAmount;
            } else if(position == 2) {
            	x += cityShiftAmount;
            } else if(position == 3) {
            	y -= cityShiftAmount;
            } else if(position == 4) {
            	x -= cityShiftAmount;
            }
            
            newPositions[i] = new City(x, y);
        }
        
        return newPositions;
    }

    public static void main(String[] args) {
        DateFormat df = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        Date today = Calendar.getInstance().getTime();
        String currentTime  = df.format(today);

        int runs;
        boolean display = false;
        String formatMessage = "Usage: java TSP 1 [gui] \n java TSP [Runs] [gui]";

        if (args.length < 1) {
            System.out.println("Please enter the arguments");
            System.out.println(formatMessage);
            display = false;
        } else {

            if (args.length > 1) {
                display = true; 
            }

            try {
                cityCount = 50;
                populationSize = 100;
                runs = Integer.parseInt(args[0]);

                if(display) {
                    frame = new JFrame("Traveling Salesman");
                    statsArea = new Panel();

                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.pack();
                    frame.setSize(width + 300, height);
                    frame.setResizable(false);
                    frame.setLayout(new BorderLayout());
                    
                    statsText = new TextArea(35, 35);
                    statsText.setEditable(false);

                    statsArea.add(statsText);
                    frame.add(statsArea, BorderLayout.EAST);
                    
                    frame.setVisible(true);
                }


                min = 0;
                avg = 0;
                max = 0;
                sum = 0;

                originalCities = cities = LoadCitiesFromFile("CityList.txt", cities);

                writeLog("Run Stats for experiment at: " + currentTime);
                for (int y = 1; y <= runs; y++) {
                    genMin = 0;
                    print(display,  "Run " + y + "\n");

                // create the initial population of chromosomes
                    chromosomes = new Chromosome[populationSize];
                    for (int x = 0; x < populationSize; x++) {
                        chromosomes[x] = new Chromosome(cities);
                        //chromosomes[x] = twoOpt(chromosomes[x]);
                    }
                    Chromosome.sortChromosomes(chromosomes, populationSize);
                    generation = 0;
                    double thisCost = 0.0;

                    while (generation < 100) {
                        evolve();
                        if(generation % 5 == 0 ) 
                            cities = MoveCities(originalCities); //Move from original cities, so they only move by a maximum of one unit.
                        generation++;

                        Chromosome.sortChromosomes(chromosomes, populationSize);
                        double cost = chromosomes[0].getCost();
                        thisCost = cost;

                        if (thisCost < genMin || genMin == 0) {
                            genMin = thisCost;
                        }
                        
                        NumberFormat nf = NumberFormat.getInstance();
                        nf.setMinimumFractionDigits(2);
                        nf.setMinimumFractionDigits(2);

                        print(display, "Gen: " + generation + " Cost: " + (int) thisCost);

                        if(display) {
                            updateGUI();
                        }
                    }

                    writeLog(genMin + "");

                    if (genMin > max) {
                        max = genMin;
                    }

                    if (genMin < min || min == 0) {
                        min = genMin;
                    }

                    sum +=  genMin;

                    print(display, "");
                }

                avg = sum / runs;
                print(display, "Statistics after " + runs + " runs");
                print(display, "Solution found after " + generation + " generations." + "\n");
                print(display, "Statistics of minimum cost from each run \n");
                print(display, "Lowest: " + min + "\nAverage: " + avg + "\nHighest: " + max + "\n");

            } catch (NumberFormatException e) {
                System.out.println("Please ensure you enter integers for cities and population size");
                System.out.println(formatMessage);
            }
        }
    }
}