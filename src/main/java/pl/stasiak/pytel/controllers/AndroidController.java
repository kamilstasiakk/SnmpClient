package pl.stasiak.pytel.controllers;

import javafx.util.Pair;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import pl.stasiak.pytel.SnmpManager;
import pl.stasiak.pytel.entities.AndroidGetReply;
import pl.stasiak.pytel.entities.GetReply;
import pl.stasiak.pytel.entities.GetTableReply;
import pl.stasiak.pytel.entities.TableRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kamil on 07.01.2017.
 */
@Controller
@RequestMapping("android")
public class AndroidController {
    SnmpManager client;
    public AndroidController()  {
        client = new SnmpManager("udp:127.0.0.1/161");
    }

    @RequestMapping(value = "/get/SysUpTime", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody ResponseEntity<AndroidGetReply> getSysUpTime() {

        try {
            String value = client.getAsString(new OID(".1.3.6.1.2.1.25.1.1.0"));
            return new ResponseEntity<AndroidGetReply>(new AndroidGetReply(value, "System up time"), HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<AndroidGetReply>(new AndroidGetReply("", ""), HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/get/usersCount", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody ResponseEntity<AndroidGetReply> getUsersCount() {

        try {
            String value = client.getAsString(new OID(".1.3.6.1.2.1.25.1.5.0"));
            return new ResponseEntity<AndroidGetReply>(new AndroidGetReply(value, "System users count"), HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<AndroidGetReply>(new AndroidGetReply("", ""), HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/get/systemName", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody ResponseEntity<AndroidGetReply> getSystemName() {

        try {
            String value = client.getAsString(new OID("1.3.6.1.2.1.1.5.0"));
            return new ResponseEntity<AndroidGetReply>(new AndroidGetReply(value, "System users count"), HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<AndroidGetReply>(new AndroidGetReply("", ""), HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/get/TcpConnections", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody ResponseEntity<GetTableReply> getTcpConnections() {

        String[] columnNames = {"State", "Local address", "Local port", "Remote address", "Remote port"};
        return getTable(new OID(".1.3.6.1.2.1.6.13"), columnNames);
    }

    @RequestMapping(value = "/get/UdpOpenPorts", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody ResponseEntity<GetTableReply> getUdpOpenPorts() {

        String[] columnNames = {"Local address", "Local port"};
        return getTable(new OID(".1.3.6.1.2.1.7.5"), columnNames);
    }


    @RequestMapping(value = "/get/Interfaces", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody ResponseEntity<GetTableReply> getInterfaces() {

        String[] columnNames = {"ID","Description","MTU", "Speed", "MAC address",
                "Administrative Status", "Operational Status", "Last change", "In Octets",
                "In unicast Packet", "In non-Unicast Packet", "In Discards", "Unknown protocol",
                "Out Octets", "Out Unicast Packet", "Out non-Unicast Packet", "Out Discards",
                "Out Queue Lenght", "Doesn't matter"};
        ResponseEntity<GetTableReply> response = getTable(new OID(".1.3.6.1.2.1.2.2"), columnNames);
        List<TableRecord> table = response.getBody().getValues();
        List<TableRecord> newTable = new ArrayList<>();
        for (TableRecord tableRecord : table) {
            String hexDescription = tableRecord.getFields().get(1);
            String[] characters = hexDescription.split(":");
            StringBuilder sb = new StringBuilder();
            for (String character : characters) {
                if (!character.equals("00")) {
                    sb.append(hexToChar(character));
                }
            }
            List<String> oldFields = tableRecord.getFields();
            List<String> newFields = new ArrayList<>();
            for (int fieldNumber = 0; fieldNumber < oldFields.size() -2;fieldNumber++) {
                if (fieldNumber == 2) {
                    continue;
                }
                if (fieldNumber == 1) {
                    newFields.add(sb.toString());
                } else {
                    newFields.add(oldFields.get(fieldNumber));
                }

            }
            newTable.add(new TableRecord(newFields));
        }
        return new ResponseEntity<GetTableReply>(new GetTableReply(response.getBody().getColumnsNames(), newTable),
                response.getStatusCode());
    }

    private ResponseEntity<GetTableReply> getTable(OID oid, String[] columnNames)
    {
        List<String> columnNamesList= new ArrayList<>();

        for (String name : columnNames) {
            columnNamesList.add(name);
        }

        try {
            Pair<List<List<String>>, List<String>> result = client.getTable(oid);
            List<List<String>> value = result.getKey();
            List<TableRecord> table = new ArrayList<>();
            int columnCount = value.size();
            int rowCount = value.get(0).size();
            for (int rowNumber = 0; rowNumber < rowCount; rowNumber++) {
                List<String> row = new ArrayList<>();
                for (int columnNumber = 0; columnNumber < columnCount; columnNumber++) {
                    row.add(value.get(columnNumber).get(rowNumber));
                }
                table.add(new TableRecord(row));
            }
            return new ResponseEntity<GetTableReply>(new GetTableReply(columnNamesList, table), HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<GetTableReply>(new GetTableReply(null, null), HttpStatus.NO_CONTENT);
    }

    private char hexToChar(String hex) {
        int old = hex.toCharArray()[0];
        if (old > 47 && old < 58) {//liczba
            old = (old -48) *16;
        } else {
            old = (old - 87) * 16;
        }
        int young = hex.toCharArray()[1];
        if (young > 47 && young < 58) {//liczba
            young = (young -48);
        } else {
            young = (young - 87);
        }
        return (char) (old + young);
    }
}
