/**
 * Created by Pete on 29.11.2015.
 */


@Grapes(
        @Grab(group='org.apache.commons', module='commons-lang3', version='3.0')
)


File file = new File("prints.txt")
println file.readLines().size()

def print = "48973141298628;-34;238257140157761;-78;92676807119905;-75;238257141023941;-69;238257140157765;-78;238257141023938;-69;238257141023936;-67;238257140692594;-69;238257140692593;-68;238257140692592;-69;238257139252917;-91;238257141258448;-75;238257141258449;-72;238257141258450;-72;238257139252912;-65;238257139252914;-64;238257139252913;-63;238257140692597;-67"
printToCoordinates(print, file.text)


def printToCoordinates(String print, String trainingData){
    def trainingMap = stringToPrints(trainingData);
    println trainingMap.size()
    Map<String, FingerPrintByMacAndLocation> printMap = new LinkedHashMap<>();
    stringToPrints(print)

}

/**
 * Returns K closest neighbours ordered by their euclidean distance
 * @param trainingPrints
 * @param print
 * @return
 */
private List getKNeighbours(Map<String, FingerPrintByMacAndLocation> trainingPrints, Map<String, FingerPrintByMacAndLocation> print){
    Iterator<String> iterator = trainingPrints.keySet().iterator();
    Map<String, List<FingerPrintByMacAndLocation>> trainingPrintsWithCorrespondingMacAddress = new LinkedHashMap<>();
    Set<String> macs = getSetOfMacs(print.keySet())

    //Find the prints that have the mac address we want to use
    while( iterator.hasNext() ){
        String key = iterator.next()
        String mac = trainingPrints.get(key).getPrints().get(0).getMac();
        if( macs.contains(mac) ){
            List<FingerPrintByMacAndLocation> list = trainingPrintsWithCorrespondingMacAddress.get(mac)
            if( list == null){
                list = new ArrayList<>();
            }
            list.add(trainingPrints.get(key))
            trainingPrintsWithCorrespondingMacAddress.put(mac, list) 
        }
    }
    
    //Find the closest neighbours
    iterator = print.keySet().iterator();
    while(iterator.hasNext()){
        String key = iterator.next()
        mac = print.get( key )
       //TODO calculate the difference
    }
    
}

private Set<String> getSetOfMacs(Set<String> keySet){
    Set<String> macs = new HashSet<>();
    Iterator<String> iterator = keySet.iterator();
    while( iterator.hasNext() ){
        macs.add(iterator.next())
    }
    return macs
}



/**
 * First we collect into a map each mac address - coordinate pair
 * They are in the map so that the key is mac;x;y;z and the value is an object that has a list of the
 * scan results, i.e. mac addresses and signal strengths represented by a WifiFingerPrint object.
 * Finally we go through the map calculating an average of the signal strengths for these mac address - coordinate pair pairs
 * @param str
 * @return
 */
public Map stringToPrints(String str){
    Map<String, FingerPrintByMacAndLocation> printsByMacAndCoords = new LinkedHashMap<>();
    List<String[]> arrs = new ArrayList();
    str.eachLine{ line ->
        arrs.add(line.split(";"))
    }

    arrs.each{ arr ->
        String x = null
        String y = null
        String z = null
        String[] arrayWithoutCoords = arr;
        //this is a real fingerprint not training data (because of the three coordinates in the beginning of the training prints
        if( arr.length %2 != 0){
            x = arr[1]
            y = arr[2]
            z = arr[0]
            arrayWithoutCoords = Arrays.copyOfRange(arr, 3, arr.length)
        }
        strArrToAvgPrints(arrayWithoutCoords, printsByMacAndCoords, x, y, z)
    }

    printsByMacAndCoords = printsByMacAndCoords.sort { a, b -> b.key <=> a.key }

    def retMap = calculateAverageValuesForMacCoordPairs(printsByMacAndCoords);
    return retMap;
}

def calculateAverageValuesForMacCoordPairs(Map<String, FingerPrintByMacAndLocation> prints){
    Iterator<String> iterator = prints.keySet().iterator();
    while(iterator.hasNext()){
        String key = iterator.next();
        FingerPrintByMacAndLocation avgPrint = prints.get(key)
        avgPrint.setAverageRssi(avgPrint.getPrints().rssi.sum() / avgPrint.getPrints().size());
        def parts = key.split(';')
        def mac = decToMacAddress(parts[0])
        def x = ""
        def y = ""
        def z = ""
        if( parts.length > 1){
            x = parts[1]
            y = parts[2]
            z = parts[3]
        }
        println("mac: " + mac + " average at " + x + ", " +
                y + ", " + z + " " + prints[key].getAverageRssi())
    }
}

//Gets a fingerprint string without the x, y and z coordinates and turns that into an instance of AveragePrint
public void strArrToAvgPrints(String[] print, Map<String, FingerPrintByMacAndLocation> printsByMacAndCoords,
                              String x, String y, String z){
    Float floatX = stringToFloat(x)
    Float floatY = stringToFloat(y)
    Float floatZ = stringToFloat(z)
    def currentMacLocation = 0
    while( currentMacLocation < print.length-1 ){
        def key = createKey(print[currentMacLocation], x, y, z)
        FingerPrintByMacAndLocation avgPrint = printsByMacAndCoords.get(key)
        if( avgPrint == null){
            avgPrint = new FingerPrintByMacAndLocation();
        }
        avgPrint.addPrint(new Observation(
                floatX,
                floatY,
                floatZ,
                Integer.parseInt(print[currentMacLocation+1]),  //rssi
                print[currentMacLocation],    //mac
                null,                       //networkname
                null)                       //timestamp
        )
        printsByMacAndCoords[key] = avgPrint
        currentMacLocation += 2
    }
}

def stringToFloat(String str){
    if(str == null){
        return null
    }
    return Float.parseFloat(str)
}

def createKey(mac, x, y, z){
    if(x == null || y == null || z == null){
        return mac
    }
    return mac + ";" + x + ";" + y + ";" + z
}

def decToMacAddress(String dec){
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


//Represents an average fingerprint for a given mac address and location
public class FingerPrintByMacAndLocation {
    private averageRssi;
    private List<Observation> prints;

    public FingerPrintByMacAndLocation(){
        this.prints = new ArrayList<>();
    }

    public void addPrint(Observation print){
        this.prints.add(print)
    }

    public void setAverageRssi( avg){
        this.averageRssi = avg;
    }

    public getAverageRssi(){
        return averageRssi;
    }

    public List<Observation> getPrints(){
        return prints;
    }
}


/**
 * Created by Pete on 22.11.2015.
 */
public class Observation {
    private Float x;
    private Float y;
    private Float z;
    private int rssi;
    private String mac;
    private String networkName;
    private Long timeStamp;

    public Observation(){
    }

    public Observation(Float x, Float y, Float z, Integer rssi, String mac, String networkName, Long timeStamp){
        this.x = x;
        this.y = y;
        this.z = z;
        this.rssi = rssi;
        this.mac = mac;
        this.networkName = networkName;
        this.timeStamp = timeStamp;
    }

    public float getY() {
        return y;
    }

    public float getX() {
        return x;
    }

    public float getZ() {
        return z;
    }

    public int getRssi() {
        return rssi;
    }

    public String getMac() {
        return mac;
    }

    public String getNetworkName() {
        return networkName;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }


    @Override
    public String toString(){
        String ret = networkName + ": (" + x + ", " + y + ", " + z + "), " + rssi + ", " + mac;
        return ret;
    }
}