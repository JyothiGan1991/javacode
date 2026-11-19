package com.coned.mqtest;

import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;

import javax.jms.DeliveryMode;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.Queue;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public final class CisToBiztalkSender {
    private static final String PREFIX = "ibm.mq.cis.to.biztalk.";

    private CisToBiztalkSender() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 2 || (args.length > 0 && !"--send".equals(args[0]))) {
            throw new IllegalArgumentException("Usage: java -jar cis-to-biztalk-mq-test.jar [--send [config.properties]]");
        }
        Properties properties = loadProperties(args.length == 2 ? args[1] : null);
        MQConnectionFactory factory = connectionFactory(properties);
        String queueName = required(properties, "cis.to.biztalk.queue");
        List<String> messages = hardcodedMessages();
        System.out.printf("Queue manager=%s, channel=%s, host=%s, port=%s, queue=%s%n",
                factory.getQueueManager(), factory.getChannel(), factory.getHostName(), factory.getPort(), queueName);
        if (args.length == 0) {
            System.out.println("PREVIEW ONLY: no MQ connection opened. Use --send to publish these messages:");
            messages.forEach(System.out::println);
            return;
        }

        String username = System.getenv("MQ_USERNAME");
        String password = System.getenv("MQ_PASSWORD");
        if ((username == null) != (password == null)) {
            throw new IllegalArgumentException("Set both MQ_USERNAME and MQ_PASSWORD, or neither.");
        }
        try (JMSContext context = username == null
                ? factory.createContext(JMSContext.SESSION_TRANSACTED)
                : factory.createContext(username, password, JMSContext.SESSION_TRANSACTED)) {
            publish(context, queueName, messages);
            System.out.printf("Committed %d persistent text messages to %s.%n", messages.size(), queueName);
        }
    }

    static Properties loadProperties(String overridePath) throws Exception {
        Properties properties = new Properties();
        try (InputStream input = CisToBiztalkSender.class.getResourceAsStream("/application.properties")) {
            if (input == null) {
                throw new IllegalStateException("Missing application.properties");
            }
            properties.load(input);
        }
        if (overridePath != null) {
            try (InputStream input = Files.newInputStream(Paths.get(overridePath))) {
                properties.load(input);
            }
        }
        return properties;
    }

    static MQConnectionFactory connectionFactory(Properties properties) throws Exception {
        MQConnectionFactory factory = new MQConnectionFactory();
        factory.setQueueManager(required(properties, PREFIX + "queueManager"));
        factory.setChannel(required(properties, PREFIX + "channel"));
        factory.setHostName(required(properties, PREFIX + "connName"));
        int port = Integer.parseInt(required(properties, PREFIX + "port"));
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("MQ port must be between 1 and 65535.");
        }
        factory.setPort(port);
        factory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
        return factory;
    }

    static void publish(JMSContext context, String queueName, List<String> messages) {
        Queue queue = context.createQueue(queueName);
        JMSProducer producer = context.createProducer().setDeliveryMode(DeliveryMode.PERSISTENT);
        for (String message : messages) {
            producer.send(queue, message);
        }
        context.commit();
    }

    static List<String> hardcodedMessages() {
        return Collections.unmodifiableList(Arrays.asList(
                "<TRBL-MQ-RECORD-LAYOUT>\r\n" + 
                "	<MQ-CU-ACCTNUM>00205550007</MQ-CU-ACCTNUM>\r\n" + 
                "	<MQ-EMPL-NUM>DCX-MBL-T</MQ-EMPL-NUM>\r\n" + 
                "	<MQ-CU-NAME>CHURCH OF ST PETER &amp; PAUL</MQ-CU-NAME>\r\n" + 
                "	<MQ-CU-BORO>W</MQ-CU-BORO>\r\n" + 
                "	<MQ-CU-MUNI>21</MQ-CU-MUNI>\r\n" + 
                "	<MQ-CU-STREET>E BIRCH</MQ-CU-STREET>\r\n" + 
                "	<MQ-CU-ARTY>ST</MQ-CU-ARTY>\r\n" + 
                "	<MQ-CU-DIRECT />\r\n" + 
                "	<MQ-CU-HOUSE-NUM>110</MQ-CU-HOUSE-NUM>\r\n" + 
                "	<MQ-CU-PARTS />\r\n" + 
                "	<MQ-CU-PHONE>9146689815</MQ-CU-PHONE>\r\n" + 
                "	<MQ-CA-NAME>CHURCH OF ST PETER &amp; PAUL</MQ-CA-NAME>\r\n" + 
                "	<MQ-CA-BORO>W</MQ-CA-BORO>\r\n" + 
                "	<MQ-CA-MUNI>21</MQ-CA-MUNI>\r\n" + 
                "	<MQ-CA-STREET>E BIRCH</MQ-CA-STREET>\r\n" + 
                "	<MQ-CA-ARTY>ST</MQ-CA-ARTY>\r\n" + 
                "	<MQ-CA-DIRECT />\r\n" + 
                "	<MQ-CA-HOUSE-NUM>110</MQ-CA-HOUSE-NUM>\r\n" + 
                "	<MQ-CA-PARTS />\r\n" + 
                "	<MQ-CA-PHONE>5164326529</MQ-CA-PHONE>\r\n" + 
                "	<MQ-CALL-DATE>20260323</MQ-CALL-DATE>\r\n" + 
                "	<MQ-CALL-TIME>213823</MQ-CALL-TIME>\r\n" + 
                "	<MQ-REMARKS> (INET-IP:10.131.28.220 - ADDR )CUST REPORTED BY INTERNET -  NO LIGHTS; - CONTACT NAME: CHURCH OF ST PETER &amp; PAUL</MQ-REMARKS>\r\n" + 
                "	<MQ-XSTRET-DIR />\r\n" + 
                "	<MQ-XSTRET-NAME />\r\n" + 
                "	<MQ-XSTRET-ARTY />\r\n" + 
                "	<MQ-CLIC-IND />\r\n" + 
                "	<MQ-LSEP-IND />\r\n" + 
                "	<MQ-TOFF-IND />\r\n" + 
                "	<MQ-EMERG-TYPE>NL</MQ-EMERG-TYPE>\r\n" + 
                "	<MQ-SERVICE />\r\n" + 
                "	<MQ-PRIORITY>4</MQ-PRIORITY>\r\n" + 
                "	<MQ-GAS-SRCECODE />\r\n" + 
                "	<MQ-SYST-DATE>20260323</MQ-SYST-DATE>\r\n" + 
                "	<MQ-SYST-TIME>173833</MQ-SYST-TIME>\r\n" + 
                "	<MQ-LONGITUDE>73.832437</MQ-LONGITUDE>\r\n" + 
                "	<MQ-LATITUDE>40.926953</MQ-LATITUDE>\r\n" + 
                "	<MQ-FORC />\r\n" + 
                "	<MQ-CTEL />\r\n" + 
                "	<MQ-CLEAR-ACCESS-E />\r\n" + 
                "	<MQ-CLEAR-ACCESS-G />\r\n" + 
                "	<MQ-OPT-IN>N</MQ-OPT-IN>\r\n" + 
                "	<MQ-SOURCE>LSA</MQ-SOURCE>\r\n" + 
                "	<MQ-EXTERNALID>3020ba1b-5f01-4985-86a0-cfaedc6b5154</MQ-EXTERNALID>\r\n" + 
                "</TRBL-MQ-RECORD-LAYOUT>",
                "<TRBL-MQ-RECORD-LAYOUT\r\n" + 
                "	xmlns:ns0=\"http://www.ConEdison.com/EIE/20141119/\">\r\n" + 
                "	<MQ-CU-ACCTNUM>22916920006</MQ-CU-ACCTNUM>\r\n" + 
                "	<MQ-EMPL-NUM />\r\n" + 
                "	<MQ-CU-NAME>AOCC AMI TEST METER 2</MQ-CU-NAME>\r\n" + 
                "	<MQ-CU-BORO>W</MQ-CU-BORO>\r\n" + 
                "	<MQ-CU-MUNI>66</MQ-CU-MUNI>\r\n" + 
                "	<MQ-CU-STREET>VALLEY</MQ-CU-STREET>\r\n" + 
                "	<MQ-CU-ARTY>AVE</MQ-CU-ARTY>\r\n" + 
                "	<MQ-CU-DIRECT />\r\n" + 
                "	<MQ-CU-HOUSE-NUM>51</MQ-CU-HOUSE-NUM>\r\n" + 
                "	<MQ-CU-PARTS>TEST</MQ-CU-PARTS>\r\n" + 
                "	<MQ-CU-PHONE>2127806344</MQ-CU-PHONE>\r\n" + 
                "	<MQ-CA-NAME>FOR RECORDS ONLY</MQ-CA-NAME>\r\n" + 
                "	<MQ-CA-BORO />\r\n" + 
                "	<MQ-CA-MUNI />\r\n" + 
                "	<MQ-CA-STREET />\r\n" + 
                "	<MQ-CA-ARTY />\r\n" + 
                "	<MQ-CA-DIRECT />\r\n" + 
                "	<MQ-CA-HOUSE-NUM />\r\n" + 
                "	<MQ-CA-PARTS />\r\n" + 
                "	<MQ-CA-PHONE />\r\n" + 
                "	<MQ-CALL-DATE>20260913</MQ-CALL-DATE>\r\n" + 
                "	<MQ-CALL-TIME>190813</MQ-CALL-TIME>\r\n" + 
                "	<MQ-REMARKS>INITIATED FROM MDMS:</MQ-REMARKS>\r\n" + 
                "	<MQ-XSTRET-DIR />\r\n" + 
                "	<MQ-XSTRET-NAME />\r\n" + 
                "	<MQ-XSTRET-ARTY />\r\n" + 
                "	<MQ-CLIC-IND>N</MQ-CLIC-IND>\r\n" + 
                "	<MQ-LSEP-IND />\r\n" + 
                "	<MQ-TOFF-IND>N</MQ-TOFF-IND>\r\n" + 
                "	<MQ-EMERG-TYPE>HOT</MQ-EMERG-TYPE>\r\n" + 
                "	<MQ-SERVICE>E</MQ-SERVICE>\r\n" + 
                "	<MQ-PRIORITY>1</MQ-PRIORITY>\r\n" + 
                "	<MQ-GAS-SRCECODE />\r\n" + 
                "	<MQ-SYST-DATE>20260913</MQ-SYST-DATE>\r\n" + 
                "	<MQ-SYST-TIME>190813</MQ-SYST-TIME>\r\n" + 
                "	<MQ-LONGITUDE>-73.822241</MQ-LONGITUDE>\r\n" + 
                "	<MQ-LATITUDE>41.054225</MQ-LATITUDE>\r\n" + 
                "	<MQ-FORC />\r\n" + 
                "	<MQ-CTEL />\r\n" + 
                "	<MQ-CLEAR-ACCESS-E />\r\n" + 
                "	<MQ-CLEAR-ACCESS-G />\r\n" + 
                "	<MQ-OPT-IN />\r\n" + 
                "	<MQ-SOURCE>LSA</MQ-SOURCE>\r\n" + 
                "	<MQ-CORRELATION-ID>96925387642611</MQ-CORRELATION-ID>\r\n" + 
                "	<MQ-EXTERNALID>CCB96925387642611</MQ-EXTERNALID>\r\n" + 
                "</TRBL-MQ-RECORD-LAYOUT>"));
    }

    private static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required property: " + key);
        }
        return value.trim();
    }
}