package pl.stasiak.pytel.controllers;

import javafx.util.Pair;
import org.snmp4j.smi.OID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import pl.stasiak.pytel.SnmpManager;
import pl.stasiak.pytel.entities.GetReply;
import pl.stasiak.pytel.entities.GetTableReply;

import java.io.IOException;
import java.util.List;

/**
 * Created by Kamil on 10.12.2016.
 */
@Controller
@RequestMapping("snmp")
public class SnmpController {


    public SnmpController() {

    }
    @RequestMapping(value = "/get/{ip}/{oid}", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody ResponseEntity<GetReply> get(@PathVariable String ip, @PathVariable String oid) {

        SnmpManager client = new SnmpManager("udp:" + ip + "/161");
        try {
            String value = client.getAsString(new OID(oid));
            return new ResponseEntity<GetReply>(new GetReply(value,oid), HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<GetReply>(new GetReply("", ""), HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/getNext/{ip}/{oid}", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody ResponseEntity<GetReply> getLogsRecordsFromDate(@PathVariable String ip, @PathVariable String oid) {

        SnmpManager client;
        client = new SnmpManager("udp:" + ip + "/161");
        Pair<String,String> result = null;
        try {
            result = client.getNextAsString(new OID(oid));

        } catch (IOException e) {
            e.printStackTrace();
        }
        if (result == null) {
            return new ResponseEntity<GetReply>(new GetReply("", ""),
                    HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<GetReply>(new GetReply(result.getValue(), result.getKey()),
                    HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/getTable/{ip}/{oid}", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody ResponseEntity<GetTableReply> getTable(@PathVariable String ip, @PathVariable String oid) {

        SnmpManager client;
        client = new SnmpManager("udp:" + ip + "/161");
        try {
            Pair<List<List<String>>,List<String>> result = client.getTable(new OID(oid));
            return new ResponseEntity<GetTableReply>(new GetTableReply(result.getValue(), result.getKey()), HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<GetTableReply>(new GetTableReply(null, null), HttpStatus.NO_CONTENT);
    }


    @RequestMapping(value = "/startMonitoring/{ip}/{oid}", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody ResponseEntity<String> startMonitoring(@PathVariable String ip, @PathVariable String oid) {

        SnmpManager client;
        client = new SnmpManager("udp:" + ip + "/161");

        client.startMonitoring(new OID(oid));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/getMonitoredValue/{ip}/{oid}", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody ResponseEntity<String> getMonitoredValue(@PathVariable String ip, @PathVariable String oid) {

        SnmpManager client;
        client = new SnmpManager("udp:" + ip + "/161");

        client.getMonitoredObjectValues();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
