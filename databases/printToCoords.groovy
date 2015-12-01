import org.apache.ivy.util.StringUtils

/**
 * Created by Pete on 30.11.2015.
 */

@Grapes(
        @Grab(group='org.apache.commons', module='commons-lang3', version='3.0')
)

def printToUse = 13;

file = new File("prints_original_first_floor.txt")
List<String> lines = file.readLines();
def fileout = new File("print.txt")
String[] arr = lines.get(printToUse).split(';')
String[] subArray = Arrays.copyOfRange(arr, 3, arr.length);
String lineWithoutCoords = StringUtils.join(subArray, ';')

fileout.write lineWithoutCoords
println "testPrint: " + lines.get(printToUse)

File file = new File("trainingData.txt")
println "training data path: " + file.absolutePath
def print = new File("print.txt").text
List<FingerPrint> kClosest = getKNeighbours(lineToFingerprint(print, false), dataToFingerprints(file.text, true), 1, new DistanceCalculator2())
FingerPrint closest = kClosest.get(0);
System.out.println("The estimated location is at coordinates z:" + closest.getZ() + " x: " + closest.getX() + " y: " + closest.getY());

//calculateAverageError("prints_original_first_floor.txt", "trainingData.txt")

private void calculateAverageError(String fileNamePrints, String fileNameTraining){
    file = new File(fileNamePrints)
    File trainingFile = new File(fileNameTraining)
    List<String> lines = file.readLines();

    double distanceSquaredSum = 0;

    def errorsX = []
    def errorsY = []
    def errorsZ = []
    lines.each{ line ->
        def fileout = new File("print.txt")
        String[] arr = line.split(';')
        Float trueZ = Float.parseFloat(arr[0]);
        Float trueX = Float.parseFloat(arr[1]);
        Float trueY = Float.parseFloat(arr[2]);
        String[] subArray = Arrays.copyOfRange(arr, 3, arr.length);
        String lineWithoutCoords = StringUtils.join(subArray, ';')
        fileout.write lineWithoutCoords
        def print = new File("print.txt").text

        FingerPrintDistanceCalculator calc = new DistanceCalculator2();
        List<FingerPrint> kClosest = getKNeighbours(lineToFingerprint(print, false), dataToFingerprints(trainingFile.text, true), 1, calc)
        FingerPrint closest = kClosest.get(0);

        double distance = Math.abs(closest.getX() - trueX) + Math.abs(closest.getY() - trueY);
        double squaredDistance = distance * distance;
        distanceSquaredSum += squaredDistance;

        errorsX.add(Math.abs(closest.getX() - trueX))
        errorsY.add(Math.abs(closest.getY() - trueY))
        errorsZ.add(Math.abs(closest.getZ() - trueZ))
    }
    double euclideanDiff = Math.sqrt(distanceSquaredSum)
    println "Total average distance with respect to x and y: " + euclideanDiff/lines.size()
    println "Max error in x: " + errorsX.max();
    println "Max error in y: " + errorsY.max();
    println "Max error in z: " + errorsZ.max();
    println errorsX.toString()
}

private List<FingerPrint> getKNeighbours(FingerPrint print, List<FingerPrint> trainingData, int k, FingerPrintDistanceCalculator calc){
    for(FingerPrint trainingPrint: trainingData){
        double dist = calc.calculateAverageEuclideanDistance(print, trainingPrint);
        trainingPrint.setDistance(dist);
    }
    trainingData.sort(new Comparator<FingerPrint>(){
        int compare(FingerPrint o1, FingerPrint o2) {
            return o1.getDistance().compareTo(o2.getDistance());
        }
    });

    int count = 1;
 /*   println "Prints ordered by distance: "
    for( FingerPrint trainingPrint: trainingData){
        println count + ": " + trainingPrint.getDistanceDebugPrint();
        count++;
    }*/

    return trainingData.subList(0, k)
}

public List<FingerPrint> dataToFingerprints(String data, boolean includesCoords){
    List<FingerPrint> prints = new ArrayList<>();
    data.eachLine{ line ->
        prints.add(lineToFingerprint(line, includesCoords));
    }
    return prints;
}

public FingerPrint lineToFingerprint(String row, boolean includesCoords){
    String[] arr = row.split(";");

    String x = null;
    String y = null;
    String z = null;
    int obserVationStartLocation = 0;
    if( includesCoords ){
        x = arr[1];
        y = arr[2];
        z = arr[0];
        obserVationStartLocation = 3;
    }
    FingerPrint print = new FingerPrint(stringToFloat(z), stringToFloat(x), stringToFloat(y));

    while( obserVationStartLocation < arr.length - 1 ){
        print.addObservation(new Observation( Integer.parseInt(arr[obserVationStartLocation +1]), arr[obserVationStartLocation]));
        obserVationStartLocation += 2;
    }
    return print;
}

private Float stringToFloat(String str){
    if(str == null){
        return null;
    }
    return Float.parseFloat(str);
}

private String decToMacAddress(String dec){
    def mac = Long.toHexString(Long.parseLong(dec))
    def newMac = ""
    for(int i = 0; i < mac.length(); i++){
        if( i > 0 && i < mac.length() - 1 && i % 2 == 0){
            newMac = newMac + ":" + mac.charAt(i)
        }else{
            newMac = newMac + mac.charAt(i)
        }
    }
    return newMac
}


public class FingerPrint{
    private Float z;
    private Float x;
    private Float y;
    private List<Observation> observations;
    private Map<String, Integer> averageRssi;
    private Double distance;

    public FingerPrint(Float z, Float x, Float y){
        this.z = z;
        this.x = x;
        this.y = y;
        this.observations = new ArrayList<>();
        this.averageRssi = new LinkedHashMap<>();
    }

    public void addObservation(Observation obs){
        observations.add(obs);
    }

    public List<Observation> getObservations(){
        return this.observations;
    }

    public void setAverageRssi(String mac, Integer avg){
        averageRssi.put(mac, avg);
    }

    public int getAverageRssi(String mac){
        return averageRssi.get(mac);
    }

    public Set<String> getDistinctMacs(){
        Set<String> ret = new HashSet<>();
        for(int i = 0; i < observations.size(); i++){
            ret.add(observations.get(i).getMac());
        }
        return ret;
    }

    public Map<String, Integer> getAveragesByMac(){
        if( averageRssi.isEmpty() ){
            calculateAveragesByMac();
        }
        return averageRssi;
    }

    private void calculateAveragesByMac(){
        Map<String, Integer> sumRssi = new LinkedHashMap<>();
        Map<String, Integer> macCount = new HashMap<>();

        //first sum all the rssi into averageRssi map (by mac address)
        //keep the count of observations by a specific mac address in macCount map
        for(int i = 0; i <  observations.size(); i++){
            Observation observation = observations.get(i);
            Integer count = macCount.get(observation.getMac());
            if( count == null){
                count = 0;
            }
            macCount.put(observation.getMac(), count+1);

            Integer sum = sumRssi.get(observation.getMac());
            if( sum == null){
                sum = 0;
            }
            sumRssi.put(observation.getMac(), sum+observation.getRssi());
        }

        //Finally go through the macs and calculate the average for each mac
        Iterator<String> iter = sumRssi.keySet().iterator();
        while( iter.hasNext() ){
            String key = iter.next();
            Integer sum = sumRssi.get(key);
            Integer count = macCount.get(key);

            Integer average = sum/count;
            setAverageRssi(key, average);
        }
    }

    public String getDistanceDebugPrint(){
        return "z: " + z + ", x: " + x + ", y: " + y + ", Distance: " + distance;
    }

    public Float getX(){
        return x;
    }
    public Float getY(){
        return y;
    }
    public Float getZ(){
        return z;
    }
    public Double getDistance(){
        return this.distance;
    }
    public void setDistance(Double distance){
        this.distance = distance;
    }
}


public class Observation {
    private int rssi;
    private String mac;

    public Observation(){
    }

    public Observation(Integer rssi, String mac){
        this.rssi = rssi;
        this.mac = mac;
    }

    public int getRssi() {
        return rssi;
    }

    public String getMac() {
        return mac;
    }

    @Override
    public String toString(){
        String ret =  mac  + ": " +  rssi;
        return ret;
    }
}
public interface FingerPrintDistanceCalculator{
    public double calculateAverageEuclideanDistance(FingerPrint print, FingerPrint trainingPrint);
}

public class DistanceCalculator1 implements FingerPrintDistanceCalculator{
    double calculateAverageEuclideanDistance(FingerPrint print, FingerPrint trainingPrint) {
        Map<String, Integer> rssiByMac = print.getAveragesByMac();
        Map<String, Integer> trainingRssiByMac = trainingPrint.getAveragesByMac();
        Iterator<String> iter = rssiByMac.keySet().iterator();
        int distanceSquaredSum = 0;
        int count = 0;          //how many same mac addresses the prints have
        while( iter.hasNext() ){
            String mac = iter.next();
            if( trainingRssiByMac.get(mac) != null ){
                int distance = rssiByMac.get(mac) - trainingRssiByMac.get(mac);
                int differenceSquared = distance * distance;
                distanceSquaredSum += differenceSquared;
                count ++;

                /* if(trainingPrint.getX() == 952 && trainingPrint.getY() == 866){
                     println "training signal: " + trainingRssiByMac.get(mac) + ", real signal: " + rssiByMac.get(mac)
                 }*/
            }
        }
        double euclideanDiff = Math.sqrt(distanceSquaredSum)

        if(count == 0){
            return Double.MAX_VALUE;
        }
        double averageEuclideanDiff = euclideanDiff/count;
        return averageEuclideanDiff;
    }
}

public class DistanceCalculator2 implements FingerPrintDistanceCalculator {

    public double calculateAverageEuclideanDistance(FingerPrint print, FingerPrint trainingPrint) {
        Map<String, Integer> rssiByMac = print.getAveragesByMac();
        Map<String, Integer> trainingRssiByMac = trainingPrint.getAveragesByMac();
        Iterator<String> iter = rssiByMac.keySet().iterator();
        int distanceSquaredSum = 0;
        int count = 0;          //how many observations
        while( iter.hasNext() ){
            String mac = iter.next();
            int trainingRssi = -100;        //min possible value
            int observerdRssi = rssiByMac.get(mac);
            if( trainingRssiByMac.get(mac) != null ){
                trainingRssi = trainingRssiByMac.get(mac);
            }
            int distance = observerdRssi - trainingRssi;
            int differenceSquared = distance * distance;
            distanceSquaredSum += differenceSquared;
            count ++;
        }
        double euclideanDiff = Math.sqrt(distanceSquaredSum)

        if(count == 0){
            return Double.MAX_VALUE;
        }
        double averageEuclideanDiff = euclideanDiff/count;
        return averageEuclideanDiff;

    }
}