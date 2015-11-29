@Grapes(
    @Grab(group='org.apache.commons', module='commons-lang3', version='3.0')
)


File file = new File("prints.txt")
println file.readLines().size() 
def printsByMacAndCoords = [:]

List<String[]> arrs = new ArrayList();
file.eachLine{ line ->
    arrs.add(line.split(";"))
}


arrs.each{ arr -> 
    def x = arr[1]
    def y = arr[2]
    def z = arr[0]
    def currentMacLocation = 3

    while( currentMacLocation < arr.length-1 ){
        def key = createKey(arr[currentMacLocation], x, y, z)
        def printMap = printsByMacAndCoords.get(key)
        if( printMap == null){
            printMap = [:]
            printMap ["data"] = []
        }
        printMap["data"].add(new WifiFingerPrint(
            Float.parseFloat(x),        
            Float.parseFloat(y),        
            Float.parseFloat(z),        
            Integer.parseInt(arr[currentMacLocation+1]),  //rssi
            arr[currentMacLocation],    //mac
            null,                       //networkname
            null)                       //timestamp
        )
        printsByMacAndCoords[key] = printMap
        currentMacLocation += 2
    }   
}

printsByMacAndCoords.keySet().each{ key -> 
    def printMap = printsByMacAndCoords.get(key)
    printMap["aver"] = printsByMacAndCoords[key]["data"].rssi.sum()/printsByMacAndCoords[key]["data"].size()
    def parts = key.split(';')
    def mac = parts[0]
    def x = parts[1]
    def y = parts[2]
    def z = parts[3]
    println "mac: " + Long.toHexString(Long.parseLong(mac) + " average at " + x + ", " + y + ", " + z + " " +
         printsByMacAndCoords[key]["aver"]
}



def createKey(mac, x, y, z){
    return mac + ";" + x + ";" y + ";" + z
}





/**
 * Created by Pete on 22.11.2015.
 */
public class WifiFingerPrint {
    private float x;
    private float y;
    private float z;
    private int rssi;
    private String mac;
    private String networkName;
    private Long timeStamp;

    public WifiFingerPrint(){
    }

    public WifiFingerPrint(float x, float y, float z, Integer rssi, String mac, String networkName, Long timeStamp){
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