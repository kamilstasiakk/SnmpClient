package pl.stasiak.pytel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


import com.sun.org.apache.xml.internal.utils.ThreadControllerWrapper;
import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.util.Pair;
import org.ietf.jgss.Oid;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.log.LogFactory;
import org.snmp4j.mp.*;
import org.snmp4j.security.Priv3DES;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.smi.*;
import org.snmp4j.tools.console.SnmpRequest;
import org.snmp4j.transport.AbstractTransportMapping;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;
import pl.stasiak.pytel.entities.Trap;
import pl.stasiak.pytel.entities.VarBindings;

/**
 * Created by Kamil on 10.12.2016.
 */
public class SnmpManager {

    Snmp snmp = null;
    String address = null;
    OID monitoredOID;
    StringBuilder monitoredObjectValue;
    Lock lock = new Lock();
    List<Trap> trapList;
    boolean monitorStarted = false;
    boolean trapListenerStarted = false;


    /**
     * Constructor
     *
     * @param add
     */
    public SnmpManager(String add) {
        address = add;
        monitoredObjectValue = new StringBuilder();
        monitoredOID = null;
        trapList = new ArrayList<>();
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
     *
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
            System.out.println(event.getResponse().get(i).getOid().toString() + ": " +
                    event.getResponse().get(i).getVariable().toString());
        }
    }


    public OID[] GetNextRequest(OID[] oids) throws IOException {
        ResponseEvent event = getNext(oids);
        OID[] receivedOIDs = new OID[oids.length];
        for (int i = 0; i < oids.length; i++) {
            System.out.println(event.getResponse().get(i).getOid().toString() + ": " +
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

    public List<Trap> getTrapList() {

        return TrapListener.traps;

    }

    public Pair<List<List<String>>, List<String>> getTable(OID oid) throws IOException {
        String rootOID = oid.toString() + ".1";
        OID previousOID = oid;
        OID currentOID;
        List<List<String>> table = new ArrayList<List<String>>();
        List<String> oids = new ArrayList<>();

        oids.add(rootOID + ".1");
        int columnNumber = 1;
        while (true) {
            ResponseEvent responseEvent = getNext(new OID[]{previousOID});
            currentOID = responseEvent.getResponse().get(0).getOid();
            if (!currentOID.toString().startsWith(rootOID)) {
                break;
            }
            if (!currentOID.toString().startsWith(rootOID + "." + columnNumber)) {
                columnNumber++;
                oids.add(rootOID + "." + columnNumber);
            }
            if (table.size() <= columnNumber - 1) {
                table.add(new ArrayList<String>());
            }
            table.get(columnNumber - 1).add(responseEvent.getResponse().get(0).getVariable().toString());

            previousOID = currentOID;
        }
        return new Pair<List<List<String>>, List<String>>(table, oids);
    }

    /**
     * Method which takes a single OID and returns the response from the agent as a String.
     *
     * @param oid
     * @return
     * @throws IOException
     */
    public String getAsString(OID oid) throws IOException {
        ResponseEvent event = get(new OID[]{oid});
        return event.getResponse().get(0).getVariable().toString();
    }

    public Pair<String, String> getNextAsString(OID oid) throws IOException {
        ResponseEvent event = getNext(new OID[]{oid});
        return new Pair<>(event.getResponse().get(0).getOid().toString(),
                event.getResponse().get(0).getVariable().toString());
    }

    /**
     * This method is capable of handling multiple OIDs
     *
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
        if (event != null) {
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
        if (event != null) {
            return event;
        }
        throw new RuntimeException("GET timed out");
    }

    /**
     * This method returns a Target, which contains information about
     * where the data should be fetched and how.
     *
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

    public Pair<String, String> getMonitoredObjectValues() {
        try {
            lock.lock();
            String value = monitoredObjectValue.toString();
            String oid = monitoredOID.toString();
            lock.unlock();
            return new Pair<>(value, oid);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void startMonitoring(OID oid) {
        try {
            lock.lock();
            monitoredOID = oid;
            monitoredObjectValue.delete(0, monitoredObjectValue.length() - 1);
            lock.unlock();
            if (!monitorStarted) {
                monitorStarted = true;
                startMonitor();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void startTrapListener() {

        if (!trapListenerStarted) {
            trapListenerStarted = true;
            startTrapListen();
        }

    }


    public static class TrapListener implements Runnable, CommandResponder {

        boolean trapListeningStarted;
        Lock lock;
        static List<Trap> traps;
        SnmpManager client;

        public TrapListener(Lock lock, boolean bool, List<Trap> listTraps, String adress) {
            this.lock = lock;
            trapListeningStarted = bool;
            traps = listTraps;
            this.client = new SnmpManager(adress);
        }

        public void run() {
            try {
                listen(new UdpAddress("localhost/162"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        public synchronized void listen(TransportIpAddress address) throws IOException {
            AbstractTransportMapping transport;
            if (address instanceof TcpAddress) {
                transport = new DefaultTcpTransportMapping((TcpAddress) address);
            } else {
                transport = new DefaultUdpTransportMapping((UdpAddress) address);
            }

            ThreadPool threadPool = ThreadPool.create("DispatcherPool", 10);
            MessageDispatcher mtDispatcher = new MultiThreadedMessageDispatcher(threadPool, new MessageDispatcherImpl());

            // add message processing models
            mtDispatcher.addMessageProcessingModel(new MPv1());
            mtDispatcher.addMessageProcessingModel(new MPv2c());

            // add all security protocols
            SecurityProtocols.getInstance().addDefaultProtocols();
            SecurityProtocols.getInstance().addPrivacyProtocol(new Priv3DES());

            //Create Target
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString("public"));

            Snmp snmp = new Snmp(mtDispatcher, transport);
            snmp.addCommandResponder(this);

            transport.listen();
            System.out.println("Listening on " + address);

            try {
                this.wait();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        /**
         * This method will be called whenever a pdu is received on the given port specified in the listen() method
         */
        public synchronized void processPdu(CommandResponderEvent cmdRespEvent) {
            System.out.println("Received PDU...");
            PDU pdu = cmdRespEvent.getPDU();

            if (pdu != null) {
                int pduType = pdu.getType();
                if ((pduType == PDU.TRAP) || (pduType == PDU.V1TRAP)) {

                    Trap trap = new Trap();
                    trap.setSourceAdress(cmdRespEvent.getPeerAddress().toString());
                    Vector<VariableBinding> variableBindings = pdu.getVariableBindings();
                    List<VarBindings> varBindings = new ArrayList<>();
                    for (VariableBinding vb : variableBindings) {
                        VarBindings varb = new VarBindings();
                        varb.setOid(vb.getOid().toString());
                        varb.setVariable(vb.getVariable().toString());
                        varBindings.add(varb);
                    }
                    trap.setVariableBindings(varBindings);
                    if (pdu.getType() == -92)
                        trap.setType("SNMPv1 Trap");
                    if (pdu.getType() == -89)
                        trap.setType("SNMPv2 Trap");

                    traps.add(trap);

                    System.out.println("Source adress: " + cmdRespEvent.getPeerAddress());
                    System.out.println("Trap Type = " + pdu.getType());
                    System.out.println("Variable Bindings : ");
                    cmdRespEvent.getPeerAddress();
                    Vector<VariableBinding> variableBinding = pdu.getVariableBindings();
                    for (VariableBinding vb : variableBinding) {
                        System.out.println(vb.getOid() + "  : " + vb.getVariable());
                    }


                }
                if ((pduType != PDU.TRAP) && (pduType != PDU.V1TRAP) && (pduType != PDU.REPORT)
                        && (pduType != PDU.RESPONSE)) {
                    pdu.setErrorIndex(0);
                    pdu.setErrorStatus(0);
                    pdu.setType(PDU.RESPONSE);
                    StatusInformation statusInformation = new StatusInformation();
                    StateReference ref = cmdRespEvent.getStateReference();
                    try {
                        System.out.println(cmdRespEvent.getPDU());
                        cmdRespEvent.getMessageDispatcher().returnResponsePdu(cmdRespEvent.getMessageProcessingModel(),
                                cmdRespEvent.getSecurityModel(), cmdRespEvent.getSecurityName(), cmdRespEvent.getSecurityLevel(),
                                pdu, cmdRespEvent.getMaxSizeResponsePDU(), ref, statusInformation);
                    } catch (MessageException ex) {
                        System.err.println("Error while sending response: " + ex.getMessage());
                        LogFactory.getLogger(SnmpRequest.class).error(ex);
                    }
                }
            }
        }
    }

    public void startTrapListen() {
        new Thread(new TrapListener(lock, trapListenerStarted, trapList, address)).start();
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
            while (monitorStarted) {
                try {
                    lock.lock();
                    monitoredValue.delete(0, monitoredValue.length() - 1);
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


    public class Lock {

        private boolean isLocked = false;

        public synchronized void lock()
                throws InterruptedException {
            while (isLocked) {
                wait();
            }
            isLocked = true;
        }

        public synchronized void unlock() {
            isLocked = false;
            notify();
        }
    }


}
