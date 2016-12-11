package pl.stasiak.pytel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import com.sun.org.apache.xml.internal.utils.ThreadControllerWrapper;
import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.util.Pair;
import org.ietf.jgss.Oid;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

/**
 * Created by Kamil on 10.12.2016.
 */
public class SnmpManager {

    Snmp snmp = null;
    String address = null;
    OID monitoredOID;
    StringBuilder monitoredObjectValue;
    Lock lock = new Lock();
    boolean monitorStarted = false;



    /**
     * Constructor
     * @param add
     */
    public SnmpManager(String add)
    {
        address = add;
        monitoredObjectValue = new StringBuilder();
        monitoredOID = null;
        try {
            start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Start the Snmp session. If you forget the listen() method you will not
     * get any answers because the communication is asynchronous
     * and the listen() method listens for answers.
     * @throws IOException
     */
    private void start() throws IOException {
        TransportMapping transport = new DefaultUdpTransportMapping();
        snmp = new Snmp(transport);
// Do not forget this line!
        transport.listen();
    }

    public void GetRequest(OID[] oids) throws IOException {
        ResponseEvent event = get(oids);
        for (int i = 0; i < oids.length; i++) {
            System.out.println( event.getResponse().get(i).getOid().toString() + ": " +
                    event.getResponse().get(i).getVariable().toString());
        }
    }


    public OID[] GetNextRequest(OID[] oids) throws IOException {
        ResponseEvent event = getNext(oids);
        OID[] receivedOIDs = new OID[oids.length];
        for (int i = 0; i < oids.length; i++) {
            System.out.println( event.getResponse().get(i).getOid().toString() + ": " +
                    event.getResponse().get(i).getVariable().toString());
            receivedOIDs[i] = new OID(event.getResponse().get(i).getOid().toString());
        }
        return receivedOIDs;
    }

    public void listTable(OID oid) throws IOException {

        List<List<String>> table = getTable(oid).getKey();
        for (List<String> column : table) {
            for (String field : column) {
                System.out.println(field);
            }
        }
    }

    public Pair<List<List<String>>,List<String>> getTable(OID oid) throws IOException {
        String rootOID = oid.toString();
        OID previousOID = oid;
        OID currentOID;
        List<List<String>> table = new ArrayList<List<String>>();
        List<String> oids = new ArrayList<>();
        int columnNumber = -1;
        while(true) {
            ResponseEvent responseEvent = getNext(new OID[] { previousOID } );
            currentOID = responseEvent.getResponse().get(0).getOid();
            if (!currentOID.toString().startsWith(rootOID)) {
                break;
            }
            if(!currentOID.equals(previousOID)) {
                columnNumber++;
                oids.add(currentOID.toString());
            }
            if(table.size() <= columnNumber ) {
                table.add( new ArrayList<String>());
            }
            table.get(columnNumber).add(responseEvent.getResponse().get(0).getVariable().toString());

            previousOID = currentOID;
        }
        return  new Pair<List<List<String>>,List<String>> (table, oids);
    }

    /**
     * Method which takes a single OID and returns the response from the agent as a String.
     * @param oid
     * @return
     * @throws IOException
     */
    public String getAsString(OID oid) throws IOException {
        ResponseEvent event = get(new OID[] { oid });
        return event.getResponse().get(0).getVariable().toString();
    }

    public Pair<String, String> getNextAsString(OID oid) throws IOException {
        ResponseEvent event = getNext(new OID[] {oid});
        return new Pair<>(event.getResponse().get(0).getOid().toString(),
                event.getResponse().get(0).getVariable().toString());
    }

    /**
     * This method is capable of handling multiple OIDs
     * @param oids
     * @return
     * @throws IOException
     */
    public ResponseEvent get(OID oids[]) throws IOException {
        PDU pdu = new PDU();
        for (OID oid : oids) {
            pdu.add(new VariableBinding(oid));
        }
        pdu.setType(PDU.GET);
        ResponseEvent event = snmp.send(pdu, getTarget(), null);
        if(event != null) {
            return event;
        }
        throw new RuntimeException("GET timed out");
    }

    public ResponseEvent getNext(OID oids[]) throws IOException {
        PDU pdu = new PDU();
        for (OID oid : oids) {
            pdu.add(new VariableBinding(oid));
        }
        pdu.setType(PDU.GETNEXT);
        ResponseEvent event = snmp.send(pdu, getTarget(), null);
        if(event != null) {
            return event;
        }
        throw new RuntimeException("GET timed out");
    }

    /**
     * This method returns a Target, which contains information about
     * where the data should be fetched and how.
     * @return
     */
    private Target getTarget() {
        Address targetAddress = GenericAddress.parse(address);
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setAddress(targetAddress);
        target.setRetries(2);
        target.setTimeout(1500);
        target.setVersion(SnmpConstants.version2c);
        return target;
    }

    public String getMonitoredObjectValues () {
        try {
            lock.lock();
            String value = monitoredObjectValue.toString();
            lock.unlock();
            return value;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
    public void startMonitoring(OID oid) {
        try {
            lock.lock();
            monitoredOID = oid;
            monitoredObjectValue.delete(0,monitoredObjectValue.length()-1);
            lock.unlock();
            if(!monitorStarted) {
                monitorStarted = true;
                startMonitor();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class Monitor implements Runnable {
        StringBuilder monitoredValue;
        OID monitoredOid;
        Lock lock;
        SnmpManager client;
        boolean monitorStarted;
        public Monitor(StringBuilder monitoredValue, OID monitoredOid, Lock lock, boolean monitorStarted, String add) {
            this.monitoredValue = monitoredValue;
            this.lock = lock;
            this.monitoredOid = monitoredOid;
            this.client = new SnmpManager(add);
            this.monitorStarted = monitorStarted;
        }

        public void run() {
            while(monitorStarted) {
                try {
                    lock.lock();
                    monitoredValue.delete(0,monitoredValue.length()-1);
                    monitoredValue.append(client.getAsString(monitoredOid));
                    lock.unlock();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private void startMonitor() {
        new Thread(new Monitor(monitoredObjectValue, monitoredOID, lock, monitorStarted, address)).start();
    }



    public class Lock{

        private boolean isLocked = false;

        public synchronized void lock()
                throws InterruptedException{
            while(isLocked){
                wait();
            }
            isLocked = true;
        }

        public synchronized void unlock(){
            isLocked = false;
            notify();
        }
    }


}
