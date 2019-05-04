package pl.stasiak.pytel;

import javafx.util.Pair;
import org.snmp4j.smi.OID;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class SnmpClientApplication {

	public static void main(String[] args) {
		//SpringApplication.run(SnmpClientApplication.class, args);
		SnmpManager snmpManager = new SnmpManager("udp:172.18.0.61/161");
		try {
			String reponse = snmpManager.getAsString(new OID(".1.3.6.1.4.1.9.10.106.1.2.1.22.1"));
			System.out.println(reponse);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
