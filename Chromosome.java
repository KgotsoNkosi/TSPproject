import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

class Chromosome {

    /**
     * The list of cities, which are the genes of this chromosome.
     */
    protected int[] cityList;

    /**
     * The cost of following the cityList order of this chromosome.
     */
    protected double cost;

    /**
     * @param cities The order that this chromosome would visit the cities.
     */
    Chromosome(City[] cities) {
        Random generator = new Random();
        cityList = new int[cities.length];
        //cities are visited based on the order of an integer representation [o,n] of each of the n cities.
        for (int x = 0; x < cities.length; x++) {
            cityList[x] = x;
        }

        //shuffle the order so we have a random initial order
        for (int y = 0; y < cityList.length; y++) {
            int temp = cityList[y];
            int randomNum = generator.nextInt(cityList.length);
            cityList[y] = cityList[randomNum];
            cityList[randomNum] = temp;
        }

        calculateCost(cities);
    }

    /**
     * Calculate the cost of the specified list of cities.
     *
     * @param cities A list of cities.
     */
    void calculateCost(City[] cities) {
        cost = 0;
        for (int i = 0; i < cityList.length - 1; i++) {
            double dist = cities[cityList[i]].proximity(cities[cityList[i + 1]]);
            cost += dist;
        }

        cost += cities[cityList[0]].proximity(cities[cityList[cityList.length - 1]]); //Adding return home
    }

    /**
     * Get the cost for this chromosome. This is the amount of distance that
     * must be traveled.
     */
    double getCost() {
        return cost;
    }

    /**
     * @param i The city you want.
     * @return The ith city.
     */
    int getCity(int i) {
        return cityList[i];
    }

    /**
     * Set the order of cities that this chromosome would visit.
     *
     * @param list A list of cities.
     */
    void setCities(int[] list) {
        for (int i = 0; i < cityList.length; i++) {
            cityList[i] = list[i];
        }
    }

    /**
     * Set the index'th city in the city list.
     *
     * @param index The city index to change
     * @param value The city number to place into the index.
     */
    void setCity(int index, int value) {
        cityList[index] = value;
    }

    /**
     * Find the index of a city
     * 
     * @param city the int value of the city being searched for
     */
    int findCity(int city){
    	int pos = -1;
    	for (int i = 0; i < cityList.length; i++){
    		if (cityList[i] == city){
    			pos = i;
    		}
    	}
    	return pos;
    }
    
    /*
     * removes a list of cities from a list
     * 
     * @param cities list of city names
     */
    public static void remove(Chromosome a, int[] cities){
    	
    	int count = 0;
    	boolean replace=true;
    	while (replace){
    		for (int i = 0; i < a.cityList.length; i++){
    			if (count < cities.length){
    				if (cities[count]==a.cityList[i]){
    					a.cityList[i] = -1;
    					count++;
    				}
    			}
    			else{
    				replace = false;
    			}
    		}
    	}
    }
    /*
     * adds a list of cities from a list
     * 
     * @param cities list of city names
     */
    public static void add(Chromosome a, int[] cities, int start, int end){
    	int count = 0, i = 0;
    	ArrayList<Integer> temp = new ArrayList();
    	boolean flag = false;
    	
    	while (i < a.cityList.length || count < cities.length){

    		if (i%a.cityList.length == start && i < a.cityList.length){    			
    			flag = true;    			
    		}
    		else if(i%a.cityList.length == (end)%a.cityList.length){    			
    			flag = false;
    		}
    		if (flag && a.cityList[i%a.cityList.length] != -1){    			
    			temp.add(a.cityList[i%a.cityList.length]);
    			a.cityList[i%a.cityList.length] = cities[count];
				count++;
			}	
    		else if (flag && a.cityList[i%a.cityList.length] == -1){
    			a.cityList[i%a.cityList.length] = cities[(count)];
    			count++;
    		}
    		else if (a.cityList[i%a.cityList.length] == -1){
    			if (temp.isEmpty()){
    				if (a.cityList[(i+1)%a.cityList.length] != -1){
    					a.cityList[i%a.cityList.length] = a.cityList[(i+1)%a.cityList.length];
    					a.cityList[(i+1)%a.cityList.length] = -1;
    				}
    				else{
    					int incr = 0;
    					while (a.cityList[(i+1+incr)%a.cityList.length] == -1){
    						incr++;
    					}
    					a.cityList[i%a.cityList.length] = a.cityList[(i+1+incr)%a.cityList.length];
    					a.cityList[(i+1+incr)%a.cityList.length] = -1;
    				}
    				
    				
    			}
    			else {
    				a.cityList[i%a.cityList.length] =  temp.get(0).intValue();
    				temp.remove(0);
    				
    			}
    		}
    		i++;
    	}
    }
    
    /*
     * Check for equality of this chromosome and another
     * return true if they are equal
     * 
     * @param other the other chromosome to check against
     */
    public boolean equal(Chromosome other){
    	if (cityList.length == other.cityList.length)
    		if (cost == other.cost)
    			if (Arrays.equals(cityList, other.cityList))
    				return true;
    	return false;
    }
    /**
     * Sort the chromosomes by their cost.
     *
     * @param chromosomes An array of chromosomes to sort.
     * @param num         How much of the chromosome list to sort.
     */
    public static void sortChromosomes(Chromosome chromosomes[], int num) {
        Chromosome ctemp;
        boolean swapped = true;
        while (swapped) {
            swapped = false;
            for (int i = 0; i < num - 1; i++) {
                if (chromosomes[i].getCost() > chromosomes[i + 1].getCost()) {
                    ctemp = chromosomes[i];
                    chromosomes[i] = chromosomes[i + 1];
                    chromosomes[i + 1] = ctemp;
                    swapped = true;
                }
            }
        }
    }
}
