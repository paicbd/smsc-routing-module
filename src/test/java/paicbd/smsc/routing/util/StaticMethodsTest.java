package paicbd.smsc.routing.util;

import com.paicbd.smsc.dto.MessageEvent;
import com.paicbd.smsc.utils.UtilsEnum;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaticMethodsTest {
    @Test
    void testConstructorIsPrivate() throws NoSuchMethodException {
        Constructor<StaticMethods> constructor = StaticMethods.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        assertThrows(InvocationTargetException.class, constructor::newInstance);
    }

    @Test
    void getMessageType() {
        MessageEvent messageEvent = new MessageEvent();
        messageEvent.setOriginProtocol("SS7");
        messageEvent.setImsi(null);
        messageEvent.setMoMessage(true);
        assertEquals(UtilsEnum.MessageType.MESSAGE, StaticMethods.getMessageType(messageEvent));

        messageEvent.setImsi("100223311");
        assertEquals(UtilsEnum.MessageType.DELIVER, StaticMethods.getMessageType(messageEvent));

        messageEvent.setMoMessage(false);
        assertEquals(UtilsEnum.MessageType.MESSAGE, StaticMethods.getMessageType(messageEvent));


        messageEvent.setMoMessage(false);
        messageEvent.setOriginProtocol("HTTP");
        messageEvent.setDlr(true);
        assertEquals(UtilsEnum.MessageType.DELIVER, StaticMethods.getMessageType(messageEvent));

        messageEvent.setMoMessage(true);
        assertEquals(UtilsEnum.MessageType.DELIVER, StaticMethods.getMessageType(messageEvent));


        messageEvent.setDlr(false);
        messageEvent.setOriginProtocol("SMPP");
        assertEquals(UtilsEnum.MessageType.MESSAGE, StaticMethods.getMessageType(messageEvent));

        messageEvent.setMoMessage(true);
        assertEquals(UtilsEnum.MessageType.MESSAGE, StaticMethods.getMessageType(messageEvent));

        messageEvent.setOriginProtocol("UNKNOWN");
        assertThrows(IllegalStateException.class, () -> StaticMethods.getMessageType(messageEvent));
    }

    @Test
    void stringAsEvent() {
        String msgRaw = "{\"msisdn\":null,\"id\":\"1722442615535-7914373631079\",\"message_id\":\"1722442615535-7914373573310\",\"system_id\":\"smppsp\",\"deliver_sm_id\":null,\"deliver_sm_server_id\":null,\"command_status\":0,\"sequence_number\":4,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"6666\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"5555\",\"esm_class\":3,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"Hello!\",\"delivery_receipt\":null,\"status\":null,\"error_code\":null,\"check_submit_sm_response\":null,\"optional_parameters\":null,\"origin_network_type\":\"SP\",\"origin_protocol\":\"SMPP\",\"origin_network_id\":3,\"dest_network_type\":\"GW\",\"dest_protocol\":\"SMPP\",\"dest_network_id\":1,\"routing_id\":1,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":\"\",\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":-1,\"network_id_to_permanent_failure\":-1,\"drop_temp_failure\":false,\"network_id_temp_failure\":-1,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":\"0\",\"udh_json\":null,\"parent_id\":null,\"is_dlr\":false,\"message_parts\":null}";
        assertNotNull(StaticMethods.stringAsEvent(msgRaw));

        msgRaw = "null";
        assertNull(StaticMethods.stringAsEvent(msgRaw));

        msgRaw = "{a}";
        assertNull(StaticMethods.stringAsEvent(msgRaw));
    }
}