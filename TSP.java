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
     *return true if a < b
     * 
     *@param a chromosome to be tested
     *@param b chromosome to be tested against
     */
    public static boolean better(Chromosome a, Chromosome b){
    	if (a == null || b == null)
    		return false;
    	if (a.getCost() <= b.getCost())
    		return true;
    	return false;
    }
    /*
     * mutates chromosome by swapping two adjacent cities
     * 
     * @param a chromosome to be mutated
     */
    public static Chromosome invert(Chromosome a){
    	Random rand = new Random();
    	Chromosome mutant = new Chromosome(cities);
    	mutant.setCities(a.cityList);
    	mutant.calculateCost(cities);
    	//System.out.println("hi");
    	int count = 0;
    	
    	while(better(a,mutant) && count < mutant.cityList.length){
    		int cityA = rand.nextInt(mutant.cityList.length);
    		cityA = mutant.getCity(mutant.findCity(cityA));
    		int cityB = mutant.getCity((mutant.findCity(cityA)+1)%mutant.cityList.length);
    		
    		mutant.setCity((mutant.findCity(cityA))%mutant.cityList.length, cityB);
    		mutant.setCity((mutant.findCity(cityB))%mutant.cityList.length, cityA);
    		mutant.calculateCost(cities);
    		count++;
    	}
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
    	int cityA = rand.nextInt(a.cityList.length-3)+1;
    	int cityB = cityA;
    	while (cityA >= cityB)
    		cityB = rand.nextInt(a.cityList.length);
		//int[] recombAList = Arrays.copyOfRange(a.cityList,cityA,cityB);
		int[] recombBList = Arrays.copyOfRange(b.cityList,cityA,cityB);
		//int temp ;//= Arrays.copyOfRange(recombAList,);
		//Arrays.asList(recombAList).
		/*for (int k = 0; k < recombAList.length; k++){
			temp = recombAList[k];
			if (Arrays.asList(recombBList).contains(temp) && Arrays.asList(recombAList).contains(recombBList[k])){
				recombAList[k] = recombBList[k];
				recombBList[k] = temp;
			}
		}*/
		
		Chromosome.remove(a, recombBList);
		
		Chromosome.add(a, recombBList, cityA, cityB);
		
		//int[] newAList = join(Arrays.copyOfRange(a.cityList,0,cityA), recombAList, Arrays.copyOfRange(a.cityList,cityB,a.cityList.length));// Arrays.copyOfRange(recombA.cityList, 0, cityA);
		
		//a.setCities(newAList);
		a.calculateCost(cities);
		
    	return a;
    	
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
		//int[] recombAList = Arrays.copyOfRange(a.cityList,cityA,cityB);
		int[] recombBList = Arrays.copyOfRange(b.cityList,cityA,cityB);
		//int temp ;//= Arrays.copyOfRange(recombAList,);
		/*for (int k = recombAList.length-1; k > 0; k--){
			temp = recombAList[k];
			if (Arrays.asList(recombBList).contains(temp) && Arrays.asList(recombAList).contains(recombBList[k])){
				recombAList[k] = recombBList[k];
				recombBList[k] = temp;
			}
		}*/
		//int[] newAList = join(Arrays.copyOfRange(a.cityList,0,cityA), recombAList, Arrays.copyOfRange(a.cityList,cityB,a.cityList.length));// Arrays.copyOfRange(recombA.cityList, 0, cityA);
		Chromosome.remove(a, recombBList);
		Chromosome.add(a, recombBList, cityB, cityA);
		
		//a.setCities(newAList);
		a.calculateCost(cities);
		
    	return a;
    	
    }
    
    /*
     * Implements a solution for ensuring a solution is  3-opt
     * 
     * @param a chromosome to optimise
     */
    public Chromosome  threeOpt(Chromosome a){
    	return a;
    }
    public static void evolve() {
    	
        //Write evolution code here.
    	//matingPopulationSize = populationSize;
    	//Chromosome[] tempList = chromosomes.clone();
    	//ArrayList<Chromosome> children = new ArrayList();
    	/*ArrayList<Chromosome> mutants = new ArrayList();
    	ArrayList<Chromosome> recombinations = new ArrayList();
    	Chromosome parentA;
    	Chromosome parentB;
    	Chromosome recombA = null;
    	Chromosome recombB;
    	//Chromosome child;
    	Chromosome mutant;*/
    	Random rand = new Random();
    	int count = 0;
    	
    	//Chromosome.sortChromosomes(chromosomes, chromosomes.length);
    	while (count < chromosomes.length){
    		double probability = (count/chromosomes.length);
    		double reverseP = rand.nextDouble();
    		double num = rand.nextDouble();
    		boolean uniqueInList = false;
    		boolean retry = false;
    		Chromosome child = new Chromosome(cities);
    		while (!uniqueInList){
    			//System.out.println("hi");
    			uniqueInList = true;
    			if (!retry){
    				child.setCities(chromosomes[count%chromosomes.length].cityList);
    				child.calculateCost(cities);
    			}
    			
    			if (reverseP > 0.5){
    				//System.out.println("hi");
        			child = recombinate(child, chromosomes[((count+1)%chromosomes.length)]);
    			}
    			/*else{
    				//System.out.println("hi173534");
    				child = reverseRecombinate(child, chromosomes[((count+1)%chromosomes.length)]);
    			}*/
    			//child = recombinate(child, chromosomes[((count+1)%chromosomes.length)]);
        		child = invert(child);
        		for (int i = 0; i < chromosomes.length; i++){
        			
        			if (child.equal(chromosomes[i])){
        				//child = invert(child);
        				retry = true;
        				//System.out.println(count + " " + child.cityList + " " +(chromosomes[i].cityList) +" " +i );
        				uniqueInList = false;
        			}
        				//break;
        			//System.out.println(uniqueInList);
        		}
        		//System.out.println(uniqueInList);
    		}
    		
    		if (better(child, chromosomes[count%chromosomes.length]) /*|| num < probability */){//&& count > chromosomes.length/2)
    			//System.out.println(num + "hi" + probability);
    			chromosomes[count%chromosomes.length] = child;
    		}
    		count++;
    	}
    	
    	/*while (mutants.size() < matingPopulationSize) {
    		//if (count == 0 || count == tempList.length){
    		//	count++;
    		//}
    		parentA = tempList[count%tempList.length];
    		parentB = tempList[(count+1)%tempList.length];
    		mutant = parentA;
    		probability = (count/tempList.length);
    		int cityA = -1;
    		int cityB = -1;
    		
    		//offspring
    		for (int i = 0; i < parentA.cityList.length; i++){
    			double num = Math.random();
    			if (mutants.size() < matingPopulationSize){
    				if(num<=probability){
    					if (cityA == -1){
    						cityA = i;
    					}
    					else if (cityB == -1){
    						cityB = i;
    						int cutLength = (cityB - cityA);
    						
    						//recombine
    						if (recombinations.size() < matingPopulationSize){
    							recombA = parentA;
    							recombB = parentB;
    							int[] recombAList = Arrays.copyOfRange(recombA.cityList,cityA,cityB);
    							int[] recombBList = Arrays.copyOfRange(recombB.cityList,cityA,cityB);
    							int temp ;//= Arrays.copyOfRange(recombAList,);
    							for (int k = cityA; k < cutLength; k++){
    								temp = recombAList[k];
    								if (Arrays.asList(recombBList).contains(temp) && Arrays.asList(recombAList).contains(recombBList[k])){
    									recombAList[k] = recombBList[k];
    									recombBList[k] = temp;
    								}
    							}
    							int[] newAList = join(Arrays.copyOfRange(recombA.cityList,0,cityA), recombAList, Arrays.copyOfRange(recombA.cityList,cityB,recombA.cityList.length));// Arrays.copyOfRange(recombA.cityList, 0, cityA);
    							//newAList = ArrayUtils.addAll(recombAList);
    							//Arrays.asList(newAList).add(Arrays.copyOfRange(newAList, cityB, recombA.cityList.length));
    							int[] newBList =  join(Arrays.copyOfRange(recombB.cityList,0,cityA), recombBList, Arrays.copyOfRange(recombB.cityList,cityB,recombA.cityList.length));//Arrays.copyOfRange(recombB.cityList, 0, cityA);
    							//Arrays.asList(newBList).add(recombBList);
    							//Arrays.asList(newBList).add(Arrays.copyOfRange(newBList, cityB, recombB.cityList.length));
    							
    							recombA.setCities(newAList);
    							recombB.setCities(newBList);
    							recombA.calculateCost(cities);
    							recombB.calculateCost(cities);
    							recombinations.add(recombA);
    							recombinations.add(recombB);
    						}
    						cityA = -1;
    						cityB = -1;
    						//break;
    					}
    						//birth
    					if (recombinations.size() == matingPopulationSize){
    						for (int x = 0; mutants.size() < matingPopulationSize; x++){
    							//mutate
    							Random rand = new Random();
    							cityA =  rand.nextInt(recombinations.get(0).cityList.length);
    							cityB = cityA;
    							while(cityB == cityA){
    								cityB = rand.nextInt(recombinations.get(0).cityList.length);
    							}
    							//System.out.println(cityA);
    	    					cityA = mutant.getCity(mutant.findCity(cityA));//recombinations.get(x).getCity(cityA);
    	    					cityB = mutant.getCity(mutant.findCity(cityA))+1;//recombinations.get(x).getCity(cityB);
    	    					mutant.setCity(mutant.findCity(cityA)%mutant.cityList.length, cityB);
    	    					mutant.setCity((mutant.findCity(cityB))%mutant.cityList.length, cityA);
    	    					mutant.calculateCost(cities);
    	    					mutants.add(mutant);
    						}
    							//child = parentA;		
								//int[] childList = child.cityList;
								//for (int j = cityA; j<cutLength; j++){
								//	childList[j] = parentB.cityList[j];
								//}
								//child.setCities(childList);
								//child.calculateCost(cities);
								//children.add(child);
    							
    					}
    				}
    			}    		
    		}
    		count++;  		
    		
    	}*/


    	//Chromosome[] tempC = new Chromosome[children.size()];
    	/*Chromosome[] tempM = new Chromosome[mutants.size()];
    	Chromosome[] tempR = new Chromosome[recombinations.size()];
    	Chromosome[] mutArray =  mutants.toArray(tempM);
    	//Chromosome[] childArray = children.toArray(tempM);
    	Chromosome[] recombArray = recombinations.toArray(tempR);
    	Chromosome.sortChromosomes(mutArray, mutants.size());
    	//Chromosome.sortChromosomes(childArray, children.size());
    	Chromosome.sortChromosomes(recombArray, recombinations.size());
    	int mutCount = 0, /*childCount = 0*/ //recombCount = 0;
    	
		/*for (int i = 0; i <chromosomes.length; i++){
			
			//double fittestFitness =  Math.min(chromosomes[i].getCost(),/*recombArray[recombCount].getCost()),*///mutArray[mutCount].getCost());
			//double number = 
			//if (fittestFitness == chromosomes[i].getCost()){
			//	chromosomes[i] = chromosomes[i]; 
			//}
			//else if(fittestFitness == mutArray[mutCount].getCost()){				
				//System.out.print(fittestFitness + " " + chromosomes[i].getCost() + " " + mutArray[mutCount].getCost() );
			//	chromosomes[i] = mutArray[mutCount];
			//	mutCount++;
		//	}*/
			//else if(fittestFitness == childArray[childCount].getCost()){
				//System.out.print("hi");
			//	chromosomes[i] = childArray[childCount];
			//	childCount++;
			//}
			/*else if(fittestFitness == recombArray[recombCount].getCost()){
				//System.out.print(fittestFitness + " " + chromosomes[i].getCost() + " " + mutArray[mutCount].getCost() + " " + recombArray[recombCount].getCost());
				chromosomes[i] = recombArray[recombCount];
				recombCount++;
			}*/
				
		//}
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
                    }

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